<?php

namespace Tests\Feature\Kasir;

use App\Models\BarangJasa;
use App\Models\Instansi;
use App\Models\KategoriTransaksi;
use App\Models\Nota;
use App\Models\NotaItem;
use App\Models\role;
use App\Models\role_permission;
use App\Models\TransaksiKas;
use App\Models\User;
use App\Services\NotaExportService;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

class NotaPenjualanTest extends TestCase
{
    use RefreshDatabase;

    private User $user;

    private Instansi $instansi;

    protected function setUp(): void
    {
        parent::setUp();

        $this->instansi = Instansi::factory()->create();
        $role = role::firstOrCreate(
            ['nama_role' => 'Owner'],
            ['deskripsi' => 'Owner']
        );

        foreach (['view:kasir', 'manage:kasir', 'delete:kasir', 'export:kasir'] as $permission) {
            role_permission::firstOrCreate(
                ['role_id' => $role->id, 'permission' => $permission],
                ['created_at' => now(), 'updated_at' => now()]
            );
        }

        $this->user = User::factory()->create([
            'instansi_id' => $this->instansi->id,
            'role_id' => $role->id,
            'email_verified_at' => now(),
        ]);

        // Kategori default yang dicari KasirService::buatNotaPenjualan() by name.
        KategoriTransaksi::factory()->global()->create([
            'nama_kategori' => 'Penjualan Barang',
            'tipe' => 'masuk',
        ]);
    }

    public function test_buat_nota_penjualan_sukses_kurangi_stok_dan_buat_transaksi_kas()
    {
        $barangJasa = BarangJasa::factory()->create([
            'instansi_id' => $this->instansi->id,
            'jenis' => 'barang',
            'stok' => 10,
            'harga_jual' => 20000,
        ]);

        $response = $this->actingAs($this->user)->postJson('/api/nota', [
            'tipe' => 'penjualan',
            'tanggal' => now()->format('Y-m-d'),
            'metode_pembayaran' => 'Tunai',
            'items' => [
                [
                    'barang_jasa_id' => $barangJasa->id,
                    'kuantitas' => 3,
                    'harga_satuan' => 20000,
                ],
            ],
        ]);

        $response->assertStatus(201);

        $notaId = $response->json('data.id');
        $nota = Nota::findOrFail($notaId);

        $this->assertSame(10 - 3, $barangJasa->fresh()->stok);
        $this->assertNotNull($nota->transaksi_kas_id);

        $transaksiKas = TransaksiKas::findOrFail($nota->transaksi_kas_id);
        $this->assertSame('masuk', $transaksiKas->tipe);
        $this->assertEquals(60000, $transaksiKas->nominal);
        $this->assertSame($nota->id, $transaksiKas->dokumen_transaksi_id);
    }

    public function test_stok_tidak_cukup_rollback_tidak_ada_data_tersimpan()
    {
        $barangJasa = BarangJasa::factory()->create([
            'instansi_id' => $this->instansi->id,
            'jenis' => 'barang',
            'stok' => 2,
            'harga_jual' => 20000,
        ]);

        $notaCountBefore = Nota::count();
        $transaksiKasCountBefore = TransaksiKas::count();

        $response = $this->actingAs($this->user)->postJson('/api/nota', [
            'tipe' => 'penjualan',
            'tanggal' => now()->format('Y-m-d'),
            'metode_pembayaran' => 'Tunai',
            'items' => [
                [
                    'barang_jasa_id' => $barangJasa->id,
                    'kuantitas' => 5,
                    'harga_satuan' => 20000,
                ],
            ],
        ]);

        $response->assertStatus(422);

        $this->assertSame($notaCountBefore, Nota::count());
        $this->assertSame($transaksiKasCountBefore, TransaksiKas::count());
        $this->assertSame(2, $barangJasa->fresh()->stok);
    }

    public function test_render_pdf_tidak_melempar_exception()
    {
        $barangJasa = BarangJasa::factory()->create([
            'instansi_id' => $this->instansi->id,
            'jenis' => 'barang',
            'stok' => 10,
            'harga_jual' => 20000,
        ]);

        $nota = Nota::factory()->penjualan()->create([
            'instansi_id' => $this->instansi->id,
            'total' => 60000,
        ]);

        NotaItem::factory()->create([
            'nota_id' => $nota->id,
            'barang_jasa_id' => $barangJasa->id,
            'nama_item' => $barangJasa->nama,
            'jenis' => 'barang',
            'kuantitas' => 3,
            'harga_satuan' => 20000,
            'subtotal' => 60000,
        ]);

        $pdf = app(NotaExportService::class)->generateNotaPdf($nota);

        $this->assertNotNull($pdf);
    }

    public function test_item_tanpa_barang_jasa_id_berhasil_tanpa_menyentuh_stok()
    {
        $response = $this->actingAs($this->user)->postJson('/api/nota', [
            'tipe' => 'penjualan',
            'tanggal' => now()->format('Y-m-d'),
            'metode_pembayaran' => 'Tunai',
            'items' => [
                [
                    'nama_item' => 'Cuci Mobil',
                    'jenis' => 'jasa',
                    'satuan' => 'kali',
                    'kuantitas' => 1,
                    'harga_satuan' => 50000,
                ],
            ],
        ]);

        $response->assertStatus(201);

        $notaId = $response->json('data.id');
        $item = NotaItem::where('nota_id', $notaId)->firstOrFail();

        $this->assertNull($item->barang_jasa_id);
        $this->assertSame('Cuci Mobil', $item->nama_item);
        $this->assertSame(0, BarangJasa::count());
    }
}
