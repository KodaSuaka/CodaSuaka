<?php

namespace Tests\Feature\Kasir;

use App\Models\BarangJasa;
use App\Models\Instansi;
use App\Models\Nota;
use App\Models\NotaItem;
use App\Models\role;
use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

/**
 * NotaItem harus ikut ter-scope per instansi.
 *
 * Nota dan BarangJasa sudah punya TenantScope, dan saat ini NotaItem hanya
 * pernah diakses lewat keduanya (relasi) atau lewat NotaItem::create(), jadi
 * belum ada kebocoran nyata. Tapi tanpa scope sendiri, query langsung
 * NotaItem::where(...) — yang wajar ditulis nanti saat menambah laporan atau
 * pencarian item — akan menembus batas tenant tanpa peringatan apa pun.
 *
 * Test ini mengunci batas itu sekarang, selagi murah.
 */
class NotaItemTenantScopeTest extends TestCase
{
    use RefreshDatabase;

    private function buatNotaDenganItem(Instansi $instansi, string $namaItem): NotaItem
    {
        $barang = BarangJasa::withoutGlobalScopes()->create([
            'instansi_id' => $instansi->id,
            'nama' => $namaItem,
            'jenis' => 'barang',
            'satuan' => 'pcs',
            'harga_jual' => 10000,
            'harga_beli' => 8000,
            'stok' => 10,
            'is_active' => true,
        ]);

        $nota = Nota::withoutGlobalScopes()->create([
            'instansi_id' => $instansi->id,
            'tipe' => 'penjualan',
            'nomor_nota' => 'NOTA-'.$namaItem,
            'tanggal' => '2026-07-15',
            'total' => 10000,
            'status' => 'selesai',
        ]);

        return NotaItem::create([
            'nota_id' => $nota->id,
            'barang_jasa_id' => $barang->id,
            'nama_item' => $namaItem,
            'jenis' => 'barang',
            'kuantitas' => 1,
            'satuan' => 'pcs',
            'harga_satuan' => 10000,
            'subtotal' => 10000,
        ]);
    }

    public function test_query_langsung_nota_item_tidak_menembus_instansi_lain()
    {
        $instansiA = Instansi::factory()->create();
        $instansiB = Instansi::factory()->create();

        $this->buatNotaDenganItem($instansiA, 'ItemMilikA');
        $this->buatNotaDenganItem($instansiB, 'ItemMilikB');

        $role = role::firstOrCreate(['nama_role' => 'Owner'], ['deskripsi' => 'Owner']);
        $userA = User::factory()->create([
            'instansi_id' => $instansiA->id,
            'role_id' => $role->id,
            'email_verified_at' => now(),
        ]);

        $this->actingAs($userA);

        $terlihat = NotaItem::pluck('nama_item')->all();

        $this->assertContains('ItemMilikA', $terlihat);
        $this->assertNotContains(
            'ItemMilikB',
            $terlihat,
            'NotaItem instansi lain bocor lewat query langsung.'
        );
    }
}
