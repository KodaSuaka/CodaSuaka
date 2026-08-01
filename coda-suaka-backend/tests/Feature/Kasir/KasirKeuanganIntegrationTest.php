<?php

namespace Tests\Feature\Kasir;

use App\Models\BarangJasa;
use App\Models\Instansi;
use App\Models\KategoriTransaksi;
use App\Models\role;
use App\Models\role_permission;
use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

/**
 * Membuktikan janji integrasi Kasir <-> Keuangan lewat HTTP penuh:
 * nota yang dibuat lewat /api/notas harus muncul di Buku Kas
 * (/api/transaksi-kas), memengaruhi saldo, dan tertaut ke nota lewat
 * nota.transaksi_kas_id — bukan sekadar baris TransaksiKas yang mengambang.
 * Kalau tautan nota->kas putus, test ini gagal.
 *
 * Catatan: hanya tautan MAJU (nota->kas) yang diekspos API. Buku Kas belum
 * mengembalikan dokumen_transaksi_id, jadi arah balik (kas->nota) tidak bisa
 * diverifikasi dari sisi client — lihat temuan validasi.
 */
class KasirKeuanganIntegrationTest extends TestCase
{
    use RefreshDatabase;

    private User $user;

    private Instansi $instansi;

    protected function setUp(): void
    {
        parent::setUp();

        $this->instansi = Instansi::factory()->create();
        $role = role::firstOrCreate(['nama_role' => 'Owner'], ['deskripsi' => 'Owner']);

        foreach (['view:kasir', 'manage:kasir', 'delete:kasir', 'view:keuangan', 'manage:keuangan'] as $permission) {
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

        KategoriTransaksi::factory()->global()->create([
            'nama_kategori' => 'Penjualan Barang',
            'tipe' => 'masuk',
        ]);
        KategoriTransaksi::factory()->global()->create([
            'nama_kategori' => 'Pembelian Bahan/Stok',
            'tipe' => 'keluar',
        ]);
    }

    public function test_nota_penjualan_muncul_di_buku_kas_dan_menaikkan_saldo()
    {
        $barang = BarangJasa::factory()->create([
            'instansi_id' => $this->instansi->id,
            'jenis' => 'barang',
            'stok' => 10,
            'harga_jual' => 25000,
        ]);

        // Saldo awal (harus 0 sebelum ada transaksi).
        $saldoAwal = $this->actingAs($this->user)
            ->getJson('/api/transaksi-kas/saldo')
            ->json('data.saldo_akhir') ?? 0;

        // Buat nota penjualan 2 x 25.000 = 50.000.
        $create = $this->actingAs($this->user)->postJson('/api/notas', [
            'tipe' => 'penjualan',
            'tanggal' => now()->format('Y-m-d'),
            'metode_pembayaran' => 'Tunai',
            'items' => [
                ['barang_jasa_id' => $barang->id, 'kuantitas' => 2, 'harga_satuan' => 25000],
            ],
        ]);
        $create->assertStatus(201);
        $nomorNota = $create->json('data.nomor_nota');

        // Tautan maju nota->kas (kontrak yang benar-benar diekspos).
        $transaksiKasId = $create->json('data.transaksi_kas_id');
        $this->assertNotNull($transaksiKasId, 'Nota tidak tertaut ke TransaksiKas.');

        // 1. Entri kas muncul di Buku Kas, cocok dengan tautan nota, tipe & nominal benar.
        $bukuKas = $this->actingAs($this->user)->getJson('/api/transaksi-kas');
        $bukuKas->assertStatus(200);

        $entri = collect($bukuKas->json('data'))
            ->firstWhere('id', $transaksiKasId);

        $this->assertNotNull($entri, 'Entri penjualan tidak muncul di Buku Kas.');
        $this->assertSame("Penjualan {$nomorNota}", $entri['keterangan']);
        $this->assertSame('masuk', $entri['tipe']);
        $this->assertSame(50000.0, (float) $entri['nominal']);

        // 2. Saldo naik persis sebesar total nota.
        $saldoAkhir = $this->actingAs($this->user)
            ->getJson('/api/transaksi-kas/saldo')
            ->json('data.saldo_akhir');
        $this->assertSame((float) $saldoAwal + 50000.0, (float) $saldoAkhir);
    }

    public function test_nota_pembelian_besar_masuk_buku_kas_sebagai_pending_approval()
    {
        // Nominal di atas threshold default (1.000.000) -> harus pending approval.
        $create = $this->actingAs($this->user)->postJson('/api/notas', [
            'tipe' => 'pembelian',
            'tanggal' => now()->format('Y-m-d'),
            'metode_pembayaran' => 'Transfer',
            'items' => [
                ['nama_item' => 'Stok besar', 'jenis' => 'barang', 'kuantitas' => 1, 'harga_satuan' => 2000000],
            ],
        ]);
        $create->assertStatus(201);
        $nomorNota = $create->json('data.nomor_nota');

        $entri = collect($this->actingAs($this->user)->getJson('/api/transaksi-kas')->json('data'))
            ->firstWhere('keterangan', "Pembelian {$nomorNota}");

        $this->assertNotNull($entri, 'Entri pembelian tidak muncul di Buku Kas.');
        $this->assertSame('keluar', $entri['tipe']);
        $this->assertSame('pending', $entri['status_approval']);
    }
}
