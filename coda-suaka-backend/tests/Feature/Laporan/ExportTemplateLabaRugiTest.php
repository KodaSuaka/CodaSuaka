<?php

namespace Tests\Feature\Laporan;

use App\Models\Instansi;
use App\Models\KategoriTransaksi;
use App\Models\TransaksiKas;
use App\Services\LaporanTemplateExportService;
use Illuminate\Foundation\Testing\RefreshDatabase;
use OpenSpout\Reader\XLSX\Reader;
use PhpOffice\PhpSpreadsheet\IOFactory;
use Tests\TestCase;

/**
 * Export template Laba Rugi mengikuti layout divisi keuangan
 * (tests/fixtures/template-laporan-keuangan.xlsx): judul, label line item
 * tetap, sel input ter-prefill dari transaksi, sel formula ditulis sebagai
 * formula asli.
 */
class ExportTemplateLabaRugiTest extends TestCase
{
    use RefreshDatabase;

    private function bacaSheetPertama(string $path): array
    {
        $reader = new Reader;
        $reader->open($path);
        $rows = [];
        foreach ($reader->getSheetIterator() as $sheet) {
            $name = $sheet->getName();
            foreach ($sheet->getRowIterator() as $row) {
                $rows[] = array_map(fn ($c) => $c->getValue(), $row->getCells());
            }
            $reader->close();

            return ['name' => $name, 'rows' => $rows];
        }
        $reader->close();

        return ['name' => null, 'rows' => []];
    }

    public function test_output_mempertahankan_format_template_merged_cells()
    {
        $instansi = Instansi::factory()->create();

        $path = app(LaporanTemplateExportService::class)
            ->generateToFile($instansi, 'laba_rugi', 'barang', 7, 2026);

        // Read-and-fill: file hasil harus mewarisi merged cells template
        // (16 di sheet Laba Rugi), bukan grid polos hasil rebuild.
        $sheet = IOFactory::load($path)->getSheetByName('Laba Rugi - Barang');
        $this->assertNotNull($sheet, 'Sheet Laba Rugi - Barang tidak ada.');
        $this->assertGreaterThanOrEqual(
            16,
            count($sheet->getMergeCells()),
            'Merged cells template harus terjaga (read-and-fill), bukan hilang (rebuild).'
        );

        @unlink($path);
    }

    public function test_export_laba_rugi_barang_punya_judul_dan_label_baris()
    {
        $instansi = Instansi::factory()->create();

        $path = app(LaporanTemplateExportService::class)
            ->generateToFile($instansi, 'laba_rugi', 'barang', 7, 2026);

        $sheet = $this->bacaSheetPertama($path);

        $this->assertSame('Laba Rugi - Barang', $sheet['name']);

        // Judul di baris pertama.
        $this->assertSame('LAPORAN LABA RUGI', $sheet['rows'][0][0] ?? null);

        // Label line item tetap harus ada persis seperti template.
        $labels = array_map(fn ($r) => $r[1] ?? null, $sheet['rows']);
        $this->assertContains('Penjualan Kotor', $labels);
        $this->assertContains('Persediaan Awal', $labels);
        $this->assertContains('Beban Gaji & Upah', $labels);

        @unlink($path);
    }

    public function test_sel_input_penjualan_kotor_terisi_dari_total_transaksi_periode()
    {
        $instansi = Instansi::factory()->create();
        // Kategori per-instansi bernama sama dengan sumber prefill. (TransaksiKasFactory
        // menukar kategori global agar sesuai instansi, jadi pakai per-instansi di test;
        // logika prefill mengelompokkan per NAMA, bukan per global/per-instansi.)
        $kategori = KategoriTransaksi::factory()->create([
            'instansi_id' => $instansi->id,
            'nama_kategori' => 'Penjualan Barang',
            'tipe' => 'masuk',
        ]);

        // Dua transaksi dalam periode Juli 2026 → dijumlah.
        TransaksiKas::factory()->create([
            'instansi_id' => $instansi->id,
            'kategori_transaksi_id' => $kategori->id,
            'tipe' => 'masuk',
            'nominal' => 500000,
            'tanggal' => '2026-07-10',
        ]);
        TransaksiKas::factory()->create([
            'instansi_id' => $instansi->id,
            'kategori_transaksi_id' => $kategori->id,
            'tipe' => 'masuk',
            'nominal' => 250000,
            'tanggal' => '2026-07-25',
        ]);
        // Di luar periode (Juni) → TIDAK ikut.
        TransaksiKas::factory()->create([
            'instansi_id' => $instansi->id,
            'kategori_transaksi_id' => $kategori->id,
            'tipe' => 'masuk',
            'nominal' => 999000,
            'tanggal' => '2026-06-30',
        ]);

        $path = app(LaporanTemplateExportService::class)
            ->generateToFile($instansi, 'laba_rugi', 'barang', 7, 2026);

        $sheet = $this->bacaSheetPertama($path);

        // "Penjualan Kotor" = baris spreadsheet 6 → rows[5], kolom C (Jumlah) → index 2.
        $penjualanKotor = $sheet['rows'][5][2] ?? null;
        $this->assertEquals(750000, $penjualanKotor);

        @unlink($path);
    }

    public function test_sel_formula_ditulis_sebagai_formula_asli_sesuai_template()
    {
        $instansi = Instansi::factory()->create();

        $path = app(LaporanTemplateExportService::class)
            ->generateToFile($instansi, 'laba_rugi', 'barang', 7, 2026);

        $sheet = $this->bacaSheetPertama($path);

        // (baris_spreadsheet, kolom_index) => formula persis dari template.
        $harapan = [
            [8, 3, '=C6-C7'],                 // D8 Penjualan Bersih
            [14, 3, '=C10+C11+C12-C13'],      // D14 Barang Tersedia
            [16, 3, '=D14-C15'],              // D16 Total HPP
            [17, 4, '=D8-D16'],               // E17 Laba Kotor
            [27, 3, '=SUM(C19:C26)'],         // D27 Total Beban Operasional
            [28, 4, '=E17-D27'],              // E28 Laba/Rugi Bersih
        ];

        foreach ($harapan as [$baris, $kolom, $formula]) {
            $this->assertSame(
                $formula,
                $sheet['rows'][$baris - 1][$kolom] ?? null,
                "Formula pada baris {$baris} kolom {$kolom} harus {$formula}"
            );
        }

        @unlink($path);
    }

    public function test_export_laba_rugi_jasa_struktur_dan_formula_sesuai_template()
    {
        $instansi = Instansi::factory()->create();

        $path = app(LaporanTemplateExportService::class)
            ->generateToFile($instansi, 'laba_rugi', 'jasa', 7, 2026);

        $sheet = $this->bacaSheetPertama($path);

        $this->assertSame('Laba Rugi - Jasa', $sheet['name']);
        $this->assertSame('LAPORAN LABA RUGI', $sheet['rows'][0][0] ?? null);

        $labels = array_map(fn ($r) => $r[1] ?? null, $sheet['rows']);
        $this->assertContains('Pendapatan Jasa Utama', $labels);
        $this->assertContains('Beban Tenaga Kerja Langsung', $labels);

        $harapan = [
            [9, 3, '=C6+C7-C8'],       // D9 Total Pendapatan Jasa
            [15, 3, '=SUM(C11:C14)'],  // D15 Total Beban Langsung
            [16, 4, '=D9-D15'],        // E16 Laba Kotor
            [26, 3, '=SUM(C18:C25)'],  // D26 Total Beban Operasional
            [27, 4, '=E16-D26'],       // E27 Laba/Rugi Bersih
        ];
        foreach ($harapan as [$baris, $kolom, $formula]) {
            $this->assertSame($formula, $sheet['rows'][$baris - 1][$kolom] ?? null, "Formula {$baris}/{$kolom}");
        }

        @unlink($path);
    }
}
