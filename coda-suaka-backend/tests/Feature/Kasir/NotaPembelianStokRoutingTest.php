<?php

namespace Tests\Feature\Kasir;

use App\Models\Instansi;
use App\Models\KategoriTransaksi;
use App\Models\role;
use App\Models\role_permission;
use App\Models\Stok;
use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

/**
 * Routing item nota pembelian: item yang menunjuk stok_id adalah "barang
 * produksi" → menambah tabel Stok (via mutasi masuk), BUKAN BarangJasa
 * (produk jual). Dengan begitu barang produksi tak muncul di produk penjualan.
 */
class NotaPembelianStokRoutingTest extends TestCase
{
    use RefreshDatabase;

    private User $user;

    private Instansi $instansi;

    protected function setUp(): void
    {
        parent::setUp();

        $this->instansi = Instansi::factory()->create();
        $role = role::firstOrCreate(['nama_role' => 'Owner'], ['deskripsi' => 'Owner']);
        foreach (['view:kasir', 'manage:kasir', 'view:stok', 'manage:stok'] as $permission) {
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
            'nama_kategori' => 'Pembelian Bahan/Stok',
            'tipe' => 'keluar',
        ]);
    }

    public function test_item_produksi_menambah_stok_bukan_barang_jasa()
    {
        $stok = Stok::factory()->create([
            'instansi_id' => $this->instansi->id,
            'nama' => 'Tepung Terigu',
            'stok' => 10,
        ]);

        $response = $this->actingAs($this->user)->postJson('/api/notas', [
            'tipe' => 'pembelian',
            'tanggal' => now()->format('Y-m-d'),
            'metode_pembayaran' => 'Tunai',
            'items' => [
                [
                    'stok_id' => $stok->id,
                    'nama_item' => 'Tepung Terigu',
                    'jenis' => 'barang',
                    'kuantitas' => 5,
                    'harga_satuan' => 10000,
                ],
            ],
        ]);

        $response->assertStatus(201);

        // Stok bertambah 10 → 15 lewat mutasi masuk.
        $this->assertSame(15.0, (float) $stok->fresh()->stok);
        $this->assertDatabaseHas('stok_mutations', [
            'stok_id' => $stok->id,
            'jenis' => 'masuk',
            'jumlah' => 5,
        ]);

        // TIDAK membuat produk jual (BarangJasa) → tak muncul di produk penjualan.
        $this->assertDatabaseMissing('barang_jasas', ['nama' => 'Tepung Terigu']);
    }

    public function test_detail_nota_menyertakan_ringkasan_dan_target_item_produksi()
    {
        $stok = Stok::factory()->create([
            'instansi_id' => $this->instansi->id,
            'nama' => 'Gula Pasir',
            'stok' => 0,
        ]);

        $create = $this->actingAs($this->user)->postJson('/api/notas', [
            'tipe' => 'pembelian',
            'tanggal' => now()->format('Y-m-d'),
            'metode_pembayaran' => 'Transfer',
            'pihak_terkait' => 'CV Pemasok',
            'items' => [
                ['stok_id' => $stok->id, 'nama_item' => 'Gula Pasir', 'jenis' => 'barang', 'kuantitas' => 4, 'harga_satuan' => 12500],
            ],
        ]);
        $notaId = $create->json('data.id');

        $detail = $this->actingAs($this->user)->getJson("/api/notas/{$notaId}");
        $detail->assertStatus(200);

        // Ringkasan.
        $this->assertSame(1, $detail->json('data.jumlah_item'));
        $this->assertSame('CV Pemasok', $detail->json('data.pihak_terkait'));
        $this->assertNotNull($detail->json('data.transaksi_kas.status_approval'));

        // Item produksi harus terlihat menunjuk Stok (bukan BarangJasa).
        $this->assertSame($stok->id, $detail->json('data.items.0.stok.id'));
        $this->assertEquals(50000, $detail->json('data.items.0.subtotal')); // 4 x 12.500
    }
}
