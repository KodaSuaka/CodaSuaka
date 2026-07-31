<?php

namespace Tests\Feature\Kasir;

use App\Models\ApprovalLog;
use App\Models\BarangJasa;
use App\Models\Instansi;
use App\Models\KategoriTransaksi;
use App\Models\Nota;
use App\Models\NotaItem;
use App\Models\Notification;
use App\Models\role;
use App\Models\role_permission;
use App\Models\TransaksiKas;
use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Http\UploadedFile;
use Illuminate\Support\Facades\Storage;
use OpenSpout\Common\Entity\Row;
use OpenSpout\Writer\XLSX\Writer;
use Tests\TestCase;

class NotaPembelianTest extends TestCase
{
    use RefreshDatabase;

    private User $user;

    private Instansi $instansi;

    protected function setUp(): void
    {
        parent::setUp();

        $this->instansi = Instansi::factory()->create();

        $ownerRole = role::firstOrCreate(
            ['nama_role' => 'Owner'],
            ['deskripsi' => 'Owner']
        );
        foreach (['view:kasir', 'manage:kasir', 'delete:kasir', 'export:kasir', 'import:kasir'] as $permission) {
            role_permission::firstOrCreate(
                ['role_id' => $ownerRole->id, 'permission' => $permission],
                ['created_at' => now(), 'updated_at' => now()]
            );
        }

        $this->user = User::factory()->create([
            'instansi_id' => $this->instansi->id,
            'role_id' => $ownerRole->id,
            'email_verified_at' => now(),
        ]);

        // Kategori default yang dicari KasirService::buatNotaPembelian() by name.
        KategoriTransaksi::factory()->global()->create([
            'nama_kategori' => 'Pembelian Bahan/Stok',
            'tipe' => 'keluar',
        ]);
    }

    /**
     * Buat user approver (permission approve:keuangan) di instansi yang sama.
     */
    private function buatApprover(): User
    {
        $role = role::firstOrCreate(
            ['nama_role' => 'Manager'],
            ['deskripsi' => 'Manager']
        );
        role_permission::firstOrCreate(
            ['role_id' => $role->id, 'permission' => 'approve:keuangan'],
            ['created_at' => now(), 'updated_at' => now()]
        );

        return User::factory()->create([
            'instansi_id' => $this->instansi->id,
            'role_id' => $role->id,
            'email_verified_at' => now(),
        ]);
    }

    public function test_pembelian_kecil_tidak_perlu_approval_dan_stok_bertambah()
    {
        $barangJasa = BarangJasa::factory()->create([
            'instansi_id' => $this->instansi->id,
            'jenis' => 'barang',
            'stok' => 5,
            'harga_beli' => 10000,
        ]);

        $response = $this->actingAs($this->user)->postJson('/api/nota', [
            'tipe' => 'pembelian',
            'tanggal' => now()->format('Y-m-d'),
            'metode_pembayaran' => 'Tunai',
            'items' => [
                [
                    'barang_jasa_id' => $barangJasa->id,
                    'jenis' => 'barang',
                    'kuantitas' => 3,
                    'harga_satuan' => 10000,
                ],
            ],
        ]);

        $response->assertStatus(201);

        $notaId = $response->json('data.id');
        $nota = Nota::findOrFail($notaId);

        $this->assertSame('pembelian', $nota->tipe);
        $this->assertSame(5 + 3, $barangJasa->fresh()->stok);

        $transaksiKas = TransaksiKas::findOrFail($nota->transaksi_kas_id);
        $this->assertSame('keluar', $transaksiKas->tipe);
        $this->assertSame('disetujui', $transaksiKas->status_approval);
        $this->assertSame(0, ApprovalLog::where('transaksi_kas_id', $transaksiKas->id)->count());
    }

    public function test_pembelian_besar_butuh_approval_dan_notifikasi_terkirim()
    {
        $approver = $this->buatApprover();

        // Staff biasa tanpa permission approve:keuangan — tidak boleh ikut
        // dapat notifikasi (dulu bug-nya broadcast ke seluruh instansi).
        $staffRole = role::firstOrCreate(
            ['nama_role' => 'Staff'],
            ['deskripsi' => 'Staff']
        );
        $staff = User::factory()->create([
            'instansi_id' => $this->instansi->id,
            'role_id' => $staffRole->id,
            'email_verified_at' => now(),
        ]);

        $response = $this->actingAs($this->user)->postJson('/api/nota', [
            'tipe' => 'pembelian',
            'tanggal' => now()->format('Y-m-d'),
            'metode_pembayaran' => 'Transfer',
            'items' => [
                [
                    'nama_item' => 'Bahan Baku Besar',
                    'jenis' => 'barang',
                    'kuantitas' => 100,
                    'satuan' => 'kg',
                    'harga_satuan' => 15000,
                ],
            ],
        ]);

        $response->assertStatus(201);

        $notaId = $response->json('data.id');
        $nota = Nota::findOrFail($notaId);
        $transaksiKas = TransaksiKas::findOrFail($nota->transaksi_kas_id);

        $this->assertSame('pending', $transaksiKas->status_approval);

        $log = ApprovalLog::where('transaksi_kas_id', $transaksiKas->id)->first();
        $this->assertNotNull($log);
        $this->assertSame('pending', $log->status);
        $this->assertSame($this->user->id, $log->diajukan_oleh);

        $this->assertTrue(
            Notification::where('user_id', $approver->id)
                ->where('type', 'keuangan')
                ->where('title', 'Transaksi Menunggu Approval')
                ->exists()
        );

        // Pengaju sendiri tidak mendapat notifikasi.
        $this->assertFalse(
            Notification::where('user_id', $this->user->id)
                ->where('type', 'keuangan')
                ->exists()
        );

        // Staff tanpa permission approve:keuangan juga tidak mendapat
        // notifikasi — hanya user ber-permission approve:keuangan yang dituju.
        $this->assertFalse(
            Notification::where('user_id', $staff->id)
                ->where('type', 'keuangan')
                ->exists()
        );
    }

    public function test_import_excel_berhasil_buat_nota_pembelian_dengan_lampiran()
    {
        Storage::fake('public');

        $tmpPath = sys_get_temp_dir().'/nota_import_'.uniqid().'.xlsx';
        $writer = new Writer;
        $writer->openToFile($tmpPath);
        $writer->addRow(Row::fromValues(['nama_item', 'jenis', 'kuantitas', 'satuan', 'harga_satuan']));
        $writer->addRow(Row::fromValues(['Beras 5kg', 'barang', 10, 'karung', 65000]));
        $writer->addRow(Row::fromValues(['Ongkos Angkut', 'jasa', 1, 'kali', 50000]));
        $writer->close();

        $file = new UploadedFile($tmpPath, 'nota-pembelian.xlsx', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', null, true);

        $response = $this->actingAs($this->user)->post('/api/nota/import', [
            'tanggal' => now()->format('Y-m-d'),
            'metode_pembayaran' => 'Tunai',
            'file' => $file,
        ]);

        $response->assertStatus(201);

        $notaId = $response->json('data.id');
        $nota = Nota::findOrFail($notaId);

        $this->assertSame('pembelian', $nota->tipe);
        $this->assertNotNull($nota->lampiran_url);
        Storage::disk('public')->assertExists($nota->lampiran_url);
        $this->assertSame(2, NotaItem::where('nota_id', $nota->id)->count());
        $this->assertEquals(10 * 65000 + 1 * 50000, (float) $nota->total);

        @unlink($tmpPath);
    }

    public function test_import_baris_invalid_dikembalikan_422_tidak_ada_nota_tersimpan()
    {
        $tmpPath = sys_get_temp_dir().'/nota_import_invalid_'.uniqid().'.xlsx';
        $writer = new Writer;
        $writer->openToFile($tmpPath);
        $writer->addRow(Row::fromValues(['nama_item', 'jenis', 'kuantitas', 'satuan', 'harga_satuan']));
        $writer->addRow(Row::fromValues(['Barang Aneh', 'tidak_valid', 5, 'pcs', 10000]));
        $writer->close();

        $file = new UploadedFile($tmpPath, 'nota-pembelian-invalid.xlsx', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', null, true);

        $notaCountBefore = Nota::count();

        $response = $this->actingAs($this->user)->post('/api/nota/import', [
            'tanggal' => now()->format('Y-m-d'),
            'metode_pembayaran' => 'Tunai',
            'file' => $file,
        ]);

        $response->assertStatus(422);
        $this->assertSame($notaCountBefore, Nota::count());

        @unlink($tmpPath);
    }
}
