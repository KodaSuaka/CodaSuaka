<?php

namespace Tests\Feature\Keuangan;

use App\Models\Instansi;
use App\Models\KategoriTransaksi;
use App\Models\role;
use App\Models\TransaksiKas;
use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Support\Facades\DB;
use Tests\TestCase;

/**
 * Karakterisasi batas tahun untuk GET /api/laporan/ringkasan-keuangan.
 *
 * Query aslinya memakai whereYear('tanggal', $tahun) yang membungkus kolom
 * dalam fungsi (non-sargable) sehingga index (instansi_id, tanggal) tidak
 * terpakai. Test ini mengunci perilaku batas tahunnya — 31 Des tahun
 * sebelumnya dan 1 Jan tahun sesudahnya harus TETAP di luar hasil — supaya
 * penulisan ulang ke range biasa tidak diam-diam menggeser hasil.
 */
class RingkasanKeuanganTest extends TestCase
{
    use RefreshDatabase;

    private Instansi $instansi;

    private User $user;

    protected function setUp(): void
    {
        parent::setUp();

        $this->instansi = Instansi::factory()->create();

        $role = role::firstOrCreate(
            ['nama_role' => 'Owner'],
            ['deskripsi' => 'Owner']
        );

        DB::table('role_permissions')->insertOrIgnore([
            'role_id' => $role->id,
            'permission' => 'view:laporan',
        ]);

        $this->user = User::factory()->create([
            'instansi_id' => $this->instansi->id,
            'role_id' => $role->id,
            'email_verified_at' => now(),
        ]);
    }

    private function buatTransaksi(string $tanggal, string $tipe, float $nominal): void
    {
        $kategori = KategoriTransaksi::factory()
            ->{$tipe === 'masuk' ? 'pemasukan' : 'pengeluaran'}()
            ->create(['instansi_id' => $this->instansi->id]);

        TransaksiKas::factory()->create([
            'instansi_id' => $this->instansi->id,
            'kategori_transaksi_id' => $kategori->id,
            'tanggal' => $tanggal,
            'tipe' => $tipe,
            'nominal' => $nominal,
        ]);
    }

    public function test_ringkasan_hanya_memuat_transaksi_dalam_tahun_yang_diminta()
    {
        // Tepat di luar batas — harus diabaikan.
        $this->buatTransaksi('2025-12-31', 'masuk', 999000);
        $this->buatTransaksi('2027-01-01', 'masuk', 888000);

        // Tepat di batas dalam — harus ikut.
        $this->buatTransaksi('2026-01-01', 'masuk', 100000);
        $this->buatTransaksi('2026-12-31', 'masuk', 200000);

        // Tengah tahun, dua tipe di bulan yang sama.
        $this->buatTransaksi('2026-06-15', 'masuk', 500000);
        $this->buatTransaksi('2026-06-20', 'keluar', 300000);

        $response = $this->actingAs($this->user)
            ->getJson('/api/laporan/ringkasan-keuangan?tahun=2026');

        $response->assertStatus(200);

        $series = collect($response->json('data.series'))->keyBy('bulan');

        $this->assertSame([2026, '2026-01', '2026-06', '2026-12'], [
            $response->json('data.tahun'),
            ...$series->keys()->all(),
        ], 'Hanya bulan di tahun 2026 yang boleh muncul.');

        // JSON tidak membedakan int/float, jadi 100000.0 muncul sebagai
        // 100000 — bandingkan setelah cast supaya tidak menguji representasi.
        $this->assertSame(100000.0, (float) $series['2026-01']['pendapatan']);
        $this->assertSame(200000.0, (float) $series['2026-12']['pendapatan']);
        $this->assertSame(500000.0, (float) $series['2026-06']['pendapatan']);
        $this->assertSame(300000.0, (float) $series['2026-06']['beban']);
        $this->assertSame(200000.0, (float) $series['2026-06']['laba']);
    }
}
