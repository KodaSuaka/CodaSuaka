<?php

namespace Tests\Feature\Keuangan;

use App\Models\Instansi;
use App\Models\KategoriTransaksi;
use App\Models\role;
use App\Models\TransaksiKas;
use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\View;
use Tests\TestCase;

/**
 * Tanggal pada export buku kas harus sama persis dengan tanggal tersimpan.
 *
 * Bug awalnya: cast 'date' pada TransaksiKas diserialisasi toArray() ke
 * ISO-8601 UTC, sehingga untuk timezone app +UTC (Asia/Jakarta) tanggal
 * mundur satu hari (2026-07-15 -> 2026-07-14T17:00:00Z). Test ini menguji
 * hasil akhir yang sampai ke view PDF, bukan cara memperbaikinya, jadi
 * tetap berlaku baik tanggal diperbaiki di cast maupun di controller.
 */
class ExportBukuKasTest extends TestCase
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
            'permission' => 'export:keuangan',
        ]);

        $this->user = User::factory()->create([
            'instansi_id' => $this->instansi->id,
            'role_id' => $role->id,
            'email_verified_at' => now(),
        ]);
    }

    public function test_tanggal_pada_export_pdf_tidak_bergeser_satu_hari()
    {
        $kategori = KategoriTransaksi::factory()
            ->pemasukan()
            ->create(['instansi_id' => $this->instansi->id]);

        TransaksiKas::factory()->create([
            'instansi_id' => $this->instansi->id,
            'kategori_transaksi_id' => $kategori->id,
            'tanggal' => '2026-07-15',
            'tipe' => 'masuk',
            'nominal' => 750000,
        ]);

        // Tangkap data yang benar-benar dikirim ke view PDF — isi PDF-nya
        // sendiri ter-encode sehingga tidak bisa di-assert lewat string.
        $dataView = null;
        View::composer('laporan.buku_kas_pdf', function ($view) use (&$dataView) {
            $dataView = $view->getData();
        });

        $response = $this->actingAs($this->user)
            ->get('/api/laporan/buku-kas/export/pdf?start_date=2026-07-01&end_date=2026-07-31');

        $response->assertStatus(200);

        $this->assertNotNull($dataView, 'View PDF tidak pernah dirender.');
        $this->assertCount(1, $dataView['transaksis']);
        $this->assertSame('2026-07-15', $dataView['transaksis'][0]['tanggal']);
        $this->assertSame(750000.0, (float) $dataView['transaksis'][0]['nominal']);
        // Saldo berjalan dihitung di controller, sekalian dikunci di sini.
        $this->assertSame(750000.0, (float) $dataView['transaksis'][0]['saldo_berjalan']);
    }
}
