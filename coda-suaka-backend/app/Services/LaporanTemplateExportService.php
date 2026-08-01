<?php

namespace App\Services;

use App\Models\Instansi;
use App\Models\TransaksiKas;
use App\Support\LaporanTemplateMap;
use Carbon\Carbon;
use OpenSpout\Common\Entity\Cell;
use OpenSpout\Common\Entity\Cell\FormulaCell;
use OpenSpout\Common\Entity\Row;
use OpenSpout\Common\Entity\Style\Style;
use OpenSpout\Writer\XLSX\Writer;

/**
 * Generate file Excel laporan keuangan (Laba Rugi / Arus Kas) mengikuti
 * layout template divisi keuangan, ter-prefill sebagian dari transaksi.
 * Sel input berwarna kuning, sel formula berwarna biru dengan formula asli.
 */
class LaporanTemplateExportService
{
    private const WARNA_INPUT = 'FFF2CC';   // kuning muda

    private const WARNA_FORMULA = 'DDEBF7'; // biru muda

    /**
     * @return string path absolut file .xlsx yang dihasilkan
     */
    public function generateToFile(Instansi $instansi, string $jenis, string $tipeUsaha, int $bulan, int $tahun): string
    {
        $rows = LaporanTemplateMap::rows($jenis, $tipeUsaha, $bulan, $tahun);
        $sheetName = LaporanTemplateMap::sheetName($jenis, $tipeUsaha);
        $totalPerKategori = $this->totalPerKategori($instansi, $bulan, $tahun);

        $path = sys_get_temp_dir().'/laporan_'.uniqid().'.xlsx';

        $styleInput = (new Style)->setBackgroundColor(self::WARNA_INPUT);
        $styleFormula = (new Style)->setBackgroundColor(self::WARNA_FORMULA);

        $writer = new Writer;
        $writer->openToFile($path);
        $writer->getCurrentSheet()->setName($sheetName);

        foreach ($rows as $row) {
            $cells = [];
            foreach ($row as $cell) {
                if (is_array($cell) && ($cell['t'] ?? null) === 'input') {
                    // src null → sel kuning kosong (diisi manual keuangan).
                    // src ada → total transaksi kategori itu di periode (0 bila tak ada).
                    $src = $cell['src'] ?? null;
                    $nilai = $src !== null ? ($totalPerKategori[$src] ?? 0) : '';
                    $cells[] = Cell::fromValue($nilai, $styleInput);
                } elseif (is_array($cell) && ($cell['t'] ?? null) === 'formula') {
                    $cells[] = new FormulaCell($cell['expr'], $styleFormula, null);
                } else {
                    $cells[] = Cell::fromValue($cell);
                }
            }
            $writer->addRow(new Row($cells));
        }

        $writer->close();

        return $path;
    }

    /**
     * Total nominal transaksi per nama kategori dalam periode (bulan/tahun).
     * Difilter eksplisit per instansi (tidak bergantung TenantScope).
     *
     * @return array<string, float>
     */
    private function totalPerKategori(Instansi $instansi, int $bulan, int $tahun): array
    {
        $start = Carbon::create($tahun, $bulan, 1)->startOfMonth()->toDateString();
        $end = Carbon::create($tahun, $bulan, 1)->endOfMonth()->toDateString();

        return TransaksiKas::where('instansi_id', $instansi->id)
            ->whereBetween('tanggal', [$start, $end])
            ->with('kategoriTransaksi:id,nama_kategori')
            ->get()
            ->groupBy(fn ($t) => $t->kategoriTransaksi?->nama_kategori)
            ->map(fn ($grup) => (float) $grup->sum('nominal'))
            ->all();
    }
}

