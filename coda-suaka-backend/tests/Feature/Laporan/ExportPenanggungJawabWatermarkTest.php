<?php

namespace Tests\Feature\Laporan;

use App\Models\Instansi;
use App\Models\KategoriTransaksi;
use App\Models\role;
use App\Models\TransaksiKas;
use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\View;
use PhpOffice\PhpSpreadsheet\IOFactory;
use Tests\TestCase;

/**
 * Fitur "Penanggung Jawab Export" + "Watermark koda suaka":
 *
 * 1. Setiap export PDF/Excel harus mencantumkan identitas user yang
 *    melakukan export (nama + role), terutama karyawan role Keuangan.
 * 2. Setiap export PDF/Excel/template harus memiliki watermark transparan
 *    "koda suaka" di lapisan kedua (di balik data).
 *
 * Test ditulis, eksekusi ditunda ke production (MySQL lokal mati).
 */
class ExportPenanggungJawabWatermarkTest extends TestCase
{
    use RefreshDatabase;

    private Instansi $instansi;

    private User $user;

    private const NAMA_USER = 'Rina Keuangan';

    private const EXPECTED_PJ = 'Rina Keuangan (Keuangan)';

    protected function setUp(): void
    {
        parent::setUp();

        $this->instansi = Instansi::factory()->create();

        $role = role::firstOrCreate(
            ['nama_role' => 'Keuangan'],
            ['deskripsi' => 'Keuangan']
        );

        DB::table('role_permissions')->insertOrIgnore([
            'role_id' => $role->id,
            'permission' => 'export:keuangan',
        ]);
        DB::table('role_permissions')->insertOrIgnore([
            'role_id' => $role->id,
            'permission' => 'export:keuangan-excel',
        ]);
        DB::table('role_permissions')->insertOrIgnore([
            'role_id' => $role->id,
            'permission' => 'export:laporan-keuangan',
        ]);

        $this->user = User::factory()->create([
            'instansi_id' => $this->instansi->id,
            'role_id' => $role->id,
            'name' => self::NAMA_USER,
            'email_verified_at' => now(),
        ]);
    }

    private function createTransaksi(string $tipe, float $nominal, KategoriTransaksi $kategori): void
    {
        TransaksiKas::factory()->create([
            'instansi_id' => $this->instansi->id,
            'kategori_transaksi_id' => $kategori->id,
            'tanggal' => '2026-07-15',
            'tipe' => $tipe,
            'nominal' => $nominal,
        ]);
    }

    private function capturePdfView(string $view, ?array &$dataView): void
    {
        View::composer($view, function ($view) use (&$dataView) {
            $dataView = $view->getData();
        });
    }

    // ─── PDF: penanggung jawab tercantum di data view ───

    public function test_pdf_buku_kas_memuat_penanggung_jawab(): void
    {
        $dataView = null;
        $this->capturePdfView('laporan.buku_kas_pdf', $dataView);

        $this->actingAs($this->user)
            ->get('/api/laporan/buku-kas/export/pdf?start_date=2026-07-01&end_date=2026-07-31')
            ->assertStatus(200);

        $this->assertNotNull($dataView, 'View PDF tidak pernah dirender.');
        $this->assertSame(self::EXPECTED_PJ, $dataView['penanggung_jawab']);
    }

    public function test_pdf_laba_rugi_memuat_penanggung_jawab(): void
    {
        $kategori = KategoriTransaksi::factory()->pemasukan()->create(['instansi_id' => $this->instansi->id]);
        $this->createTransaksi('masuk', 100000, $kategori);

        $dataView = null;
        $this->capturePdfView('laporan.laba_rugi_pdf', $dataView);

        $this->actingAs($this->user)
            ->get('/api/laporan/laba-rugi/export/pdf?start_date=2026-07-01&end_date=2026-07-31')
            ->assertStatus(200);

        $this->assertNotNull($dataView, 'View PDF tidak pernah dirender.');
        $this->assertSame(self::EXPECTED_PJ, $dataView['penanggung_jawab']);
    }

    public function test_pdf_arus_kas_memuat_penanggung_jawab(): void
    {
        $kategori = KategoriTransaksi::factory()->pemasukan()->create(['instansi_id' => $this->instansi->id]);
        $this->createTransaksi('masuk', 100000, $kategori);

        $dataView = null;
        $this->capturePdfView('laporan.arus_kas_pdf', $dataView);

        $this->actingAs($this->user)
            ->get('/api/laporan/arus-kas/export/pdf?start_date=2026-07-01&end_date=2026-07-31')
            ->assertStatus(200);

        $this->assertNotNull($dataView, 'View PDF tidak pernah dirender.');
        $this->assertSame(self::EXPECTED_PJ, $dataView['penanggung_jawab']);
    }

    // ─── Excel: penanggung jawab + watermark terpasang ───

    public function test_excel_buku_kas_memuat_penanggung_jawab_dan_watermark(): void
    {
        $kategoriMasuk = KategoriTransaksi::factory()->pemasukan()->create(['instansi_id' => $this->instansi->id]);
        $kategoriKeluar = KategoriTransaksi::factory()->pengeluaran()->create(['instansi_id' => $this->instansi->id]);
        $this->createTransaksi('masuk', 500000, $kategoriMasuk);
        $this->createTransaksi('keluar', 200000, $kategoriKeluar);

        $content = $this->downloadExcelContent('/api/laporan/buku-kas/export/excel?start_date=2026-07-01&end_date=2026-07-31');
        $sheet = $this->loadFirstSheet($content);

        $this->assertSheetContains($sheet, 'Penanggung Jawab');
        $this->assertSheetContains($sheet, self::EXPECTED_PJ);
        $this->assertNotEmpty($sheet->getBackgroundImage(), 'Watermark tidak terpasang di Excel Buku Kas.');
    }

    public function test_excel_arus_kas_memuat_penanggung_jawab_dan_watermark(): void
    {
        $kategori = KategoriTransaksi::factory()->pemasukan()->create(['instansi_id' => $this->instansi->id]);
        $this->createTransaksi('masuk', 100000, $kategori);

        $content = $this->downloadExcelContent('/api/laporan/arus-kas/export/excel?start_date=2026-07-01&end_date=2026-07-31');
        $sheet = $this->loadFirstSheet($content);

        $this->assertSheetContains($sheet, 'Penanggung Jawab');
        $this->assertSheetContains($sheet, self::EXPECTED_PJ);
        $this->assertNotEmpty($sheet->getBackgroundImage(), 'Watermark tidak terpasang di Excel Arus Kas.');
    }

    // ─── Template export: watermark terpasang ───

    public function test_template_export_memuat_watermark(): void
    {
        $content = $this->downloadExcelContent(
            '/api/laporan-keuangan/template/export?jenis=laba_rugi&tipe_usaha=barang&bulan=7&tahun=2026'
        );
        $sheet = $this->loadFirstSheet($content);

        $this->assertNotEmpty($sheet->getBackgroundImage(), 'Watermark tidak terpasang di export template laporan.');
    }

    // ─── Helper ───

    private function downloadExcelContent(string $url): string
    {
        $response = $this->actingAs($this->user)->get($url);

        $response->assertStatus(200);
        $this->assertStringContainsString(
            'spreadsheetml',
            (string) $response->headers->get('content-type'),
            "Response $url harus berupa file .xlsx"
        );

        $content = (string) $response->getFile()->getContent();
        $this->assertNotEmpty($content, "File $url kosong.");

        return $content;
    }

    private function loadFirstSheet(string $content): \PhpOffice\PhpSpreadsheet\Worksheet\Worksheet
    {
        $base = tempnam(sys_get_temp_dir(), 'wm_');
        $tmp = $base.'.xlsx';
        file_put_contents($tmp, $content);

        try {
            $spreadsheet = IOFactory::load($tmp);
            $sheet = $spreadsheet->getSheet(0);
            $spreadsheet->disconnectWorksheets();
        } finally {
            @unlink($tmp);
            @unlink($base);
        }

        return $sheet;
    }

    private function assertSheetContains(\PhpOffice\PhpSpreadsheet\Worksheet\Worksheet $sheet, string $needle): void
    {
        $ditemukan = false;
        foreach ($sheet->toArray() as $row) {
            if (in_array($needle, $row, true)) {
                $ditemukan = true;
                break;
            }
        }

        $this->assertTrue($ditemukan, "Sel '$needle' tidak ditemukan di sheet.");
    }
}
