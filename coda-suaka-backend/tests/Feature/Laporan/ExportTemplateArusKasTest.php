<?php

namespace Tests\Feature\Laporan;

use App\Models\Instansi;
use App\Services\LaporanTemplateExportService;
use Illuminate\Foundation\Testing\RefreshDatabase;
use OpenSpout\Reader\XLSX\Reader;
use Tests\TestCase;

/**
 * Export template Arus Kas. Struktur berbeda dari laba rugi (kolom Masuk/Keluar)
 * dan ada BARIS KOSONG (row 28) sebelum RINGKASAN SALDO KAS — wajib dijaga
 * supaya formula lintas-sel (C34=C30+C31+C32+C33) menunjuk sel yang benar.
 *
 * Baca dengan SHOULD_PRESERVE_EMPTY_ROWS agar nomor baris = baris absolut,
 * bukan urutan baris non-kosong.
 */
class ExportTemplateArusKasTest extends TestCase
{
    use RefreshDatabase;

    private function bacaSheetAbsolut(string $path): array
    {
        $options = new \OpenSpout\Reader\XLSX\Options;
        $options->SHOULD_PRESERVE_EMPTY_ROWS = true;
        $reader = new Reader($options);
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

    public function test_export_arus_kas_barang_struktur_dan_formula_lintas_sel()
    {
        $instansi = Instansi::factory()->create();

        $path = app(LaporanTemplateExportService::class)
            ->generateToFile($instansi, 'arus_kas', 'barang', 7, 2026);

        $sheet = $this->bacaSheetAbsolut($path);

        $this->assertSame('Arus Kas - Barang', $sheet['name']);
        $this->assertSame('LAPORAN ARUS KAS', $sheet['rows'][0][0] ?? null);

        $labels = array_map(fn ($r) => $r[1] ?? null, $sheet['rows']);
        $this->assertContains('Penerimaan dari Penjualan Tunai', $labels);
        $this->assertContains('SALDO KAS AKHIR PERIODE', $labels);

        // Formula pada baris ABSOLUT (row 28 kosong wajib ada agar ini pas).
        $harapan = [
            [15, 2, '=SUM(C6:C8)'],           // C15 subtotal masuk operasi
            [15, 3, '=SUM(D9:D14)'],          // D15 subtotal keluar operasi
            [16, 3, '=C15-D15'],              // D16 net operasi
            [21, 3, '=C18-D19-D20'],          // D21 net investasi
            [27, 3, '=C23+C24-D25-D26'],      // D27 net pendanaan
            [31, 2, '=D16'],                  // C31 ringkasan net operasi
            [34, 2, '=C30+C31+C32+C33'],      // C34 saldo akhir
        ];
        foreach ($harapan as [$baris, $kolom, $formula]) {
            $this->assertSame(
                $formula,
                $sheet['rows'][$baris - 1][$kolom] ?? null,
                "Formula pada baris absolut {$baris} kolom {$kolom} harus {$formula}"
            );
        }

        @unlink($path);
    }

    public function test_export_arus_kas_jasa_struktur_dan_formula_lintas_sel()
    {
        $instansi = Instansi::factory()->create();

        $path = app(LaporanTemplateExportService::class)
            ->generateToFile($instansi, 'arus_kas', 'jasa', 7, 2026);

        $sheet = $this->bacaSheetAbsolut($path);

        $this->assertSame('Arus Kas - Jasa', $sheet['name']);
        $labels = array_map(fn ($r) => $r[1] ?? null, $sheet['rows']);
        $this->assertContains('Penerimaan Pendapatan Jasa Tunai', $labels);
        $this->assertContains('SALDO KAS AKHIR PERIODE', $labels);

        // Formula lintas-sel di baris absolut sama seperti barang.
        $this->assertSame('=C15-D15', $sheet['rows'][16 - 1][3] ?? null);
        $this->assertSame('=D16', $sheet['rows'][31 - 1][2] ?? null);
        $this->assertSame('=C30+C31+C32+C33', $sheet['rows'][34 - 1][2] ?? null);

        @unlink($path);
    }
}
