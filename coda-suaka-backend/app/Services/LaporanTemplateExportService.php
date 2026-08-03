<?php

namespace App\Services;

use App\Models\Instansi;
use App\Models\TransaksiKas;
use App\Support\LaporanTemplateMap;
use Carbon\Carbon;
use PhpOffice\PhpSpreadsheet\IOFactory;
use PhpOffice\PhpSpreadsheet\Writer\Xlsx as XlsxWriter;

/**
 * Generate laporan keuangan (Laba Rugi / Arus Kas) dengan pola READ-AND-FILL:
 * memuat file template divisi keuangan apa adanya (format, merged cell, border,
 * formula semua terjaga), lalu HANYA mengisi sel input dari total transaksi
 * periode. Sel tanpa sumber transaksi (penyusutan, persediaan, dll) dibiarkan
 * kosong untuk diisi manual keuangan.
 */
class LaporanTemplateExportService
{
    /**
     * @return string path absolut file .xlsx yang dihasilkan (hanya sheet yang diminta)
     */
    public function generateToFile(Instansi $instansi, string $jenis, string $tipeUsaha, int $bulan, int $tahun): string
    {
        $sheetName = LaporanTemplateMap::sheetName($jenis, $tipeUsaha);
        $prefill = LaporanTemplateMap::prefillCells($jenis, $tipeUsaha);
        $totalPerKategori = $this->totalPerKategori($instansi, $bulan, $tahun);

        $spreadsheet = IOFactory::load($this->templatePath());

        // Buang sheet lain → output hanya berisi sheet yang diminta.
        foreach ($spreadsheet->getSheetNames() as $nama) {
            if ($nama !== $sheetName && count($spreadsheet->getSheetNames()) > 1) {
                $spreadsheet->removeSheetByIndex(
                    $spreadsheet->getIndex($spreadsheet->getSheetByName($nama))
                );
            }
        }

        $sheet = $spreadsheet->getSheetByName($sheetName);

        // Label periode menggantikan placeholder template.
        $sheet->setCellValue(LaporanTemplateMap::PERIODE_CELL, LaporanTemplateMap::periodeLabel($bulan, $tahun));

        // Isi sel input dari total kategori (0 bila kategori tak bertransaksi).
        foreach ($prefill as $cell => $kategori) {
            $sheet->setCellValue($cell, $totalPerKategori[$kategori] ?? 0);
        }

        $path = sys_get_temp_dir().'/laporan_'.uniqid().'.xlsx';
        (new XlsxWriter($spreadsheet))->save($path);
        $spreadsheet->disconnectWorksheets();

        return $path;
    }

    private function templatePath(): string
    {
        return resource_path('templates/laporan-keuangan.xlsx');
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
