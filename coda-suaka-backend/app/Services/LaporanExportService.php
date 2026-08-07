<?php

namespace App\Services;

use Barryvdh\DomPDF\Facade\Pdf;
use Illuminate\Support\Str;
use PhpOffice\PhpSpreadsheet\Spreadsheet;
use PhpOffice\PhpSpreadsheet\Writer\Xlsx as XlsxWriter;

class LaporanExportService
{
    /**
     * Generate PDF Buku Kas.
     */
    public function generateBukuKasPdf(
        array $transaksis,
        string $startDate,
        string $endDate,
        string $instansiNama = '',
        float $totalMasuk = 0,
        float $totalKeluar = 0,
        ?string $penanggungJawab = null
    ) {
        $data = [
            'judul' => 'Laporan Buku Kas',
            'periode' => "$startDate s/d $endDate",
            'instansi' => $instansiNama,
            'transaksis' => $transaksis,
            'total_masuk' => $totalMasuk,
            'total_keluar' => $totalKeluar,
            'saldo_bersih' => $totalMasuk - $totalKeluar,
            'penanggung_jawab' => $penanggungJawab,
            'tanggal_cetak' => now()->isoFormat('DD MMMM YYYY'),
        ];

        $pdf = Pdf::loadView('laporan.buku_kas_pdf', $data);

        return $pdf->download("buku_kas_$startDate.pdf");
    }

    /**
     * Generate Excel Buku Kas — dikelompokkan per kategori (Pemasukan & Pengeluaran).
     */
    public function generateBukuKasExcel(array $transaksis, array $grouped, string $startDate, string $endDate, ?string $penanggungJawab = null)
    {
        $spreadsheet = new Spreadsheet;
        $sheet = $spreadsheet->getActiveSheet();

        $rows = [];

        // ─── Bagian 1: Semua Transaksi (detail) ───
        $rows[] = ['No', 'Tanggal', 'Kategori', 'Tipe', 'Nominal', 'Metode', 'Keterangan'];
        $no = 1;
        foreach ($transaksis as $t) {
            $rows[] = [
                $no++,
                $t['tanggal'],
                $t['kategori'] ?? '-',
                $t['tipe'] === 'masuk' ? 'Masuk' : 'Keluar',
                (float) $t['nominal'],
                $t['metode_pembayaran'] ?? '-',
                $t['keterangan'] ?? '',
            ];
        }

        // ─── Bagian 2: Ringkasan per Kategori Pemasukan ───
        $rows[] = []; // baris kosong
        $rows[] = ['RINGKASAN PER KATEGORI - PEMASUKAN'];
        $rows[] = ['Kategori', 'Jumlah Transaksi', 'Total Nominal'];
        $grandTotalMasuk = 0;
        if (! empty($grouped['pemasukan'])) {
            foreach ($grouped['pemasukan'] as $kategori => $items) {
                $total = array_sum(array_column($items, 'nominal'));
                $grandTotalMasuk += $total;
                $rows[] = [
                    $kategori,
                    count($items),
                    (float) $total,
                ];
            }
        }
        $rows[] = [
            'TOTAL PEMASUKAN',
            '',
            (float) $grandTotalMasuk,
        ];

        // ─── Bagian 3: Ringkasan per Kategori Pengeluaran ───
        $rows[] = []; // baris kosong
        $rows[] = ['RINGKASAN PER KATEGORI - PENGELUARAN'];
        $rows[] = ['Kategori', 'Jumlah Transaksi', 'Total Nominal'];
        $grandTotalKeluar = 0;
        if (! empty($grouped['pengeluaran'])) {
            foreach ($grouped['pengeluaran'] as $kategori => $items) {
                $total = array_sum(array_column($items, 'nominal'));
                $grandTotalKeluar += $total;
                $rows[] = [
                    $kategori,
                    count($items),
                    (float) $total,
                ];
            }
        }
        $rows[] = [
            'TOTAL PENGELUARAN',
            '',
            (float) $grandTotalKeluar,
        ];

        // ─── Footer: identitas penanggung jawab ───
        $rows[] = [];
        $rows[] = [
            'Penanggung Jawab',
            $penanggungJawab ?? '-',
            '',
            '',
            '',
        ];

        $sheet->fromArray($rows, null, 'A1');
        $this->applyWatermark($sheet);

        $filename = storage_path('app/public/buku_kas_'.$startDate.'_'.Str::random(8).'.xlsx');
        (new XlsxWriter($spreadsheet))->save($filename);
        $spreadsheet->disconnectWorksheets();

        return response()->download($filename)->deleteFileAfterSend(true);
    }

    /**
     * Generate PDF Laba Rugi — dengan rincian per kategori.
     */
    public function generateLabaRugiPdf(array $data, string $startDate, string $endDate, string $instansiNama = '', ?string $penanggungJawab = null)
    {
        $pdfData = [
            'judul' => 'Laporan Laba Rugi',
            'periode' => "$startDate s/d $endDate",
            'instansi' => $instansiNama,
            'total_pendapatan' => $data['total_pendapatan'] ?? 0,
            'total_hpp' => $data['total_hpp'] ?? 0,
            'total_beban' => $data['total_beban'] ?? 0,
            'laba_bersih' => $data['laba_bersih'] ?? 0,
            'pendapatan_per_kategori' => $data['pendapatan_per_kategori'] ?? [],
            'hpp_per_kategori' => $data['hpp_per_kategori'] ?? [],
            'beban_per_kategori' => $data['beban_per_kategori'] ?? [],
            'penanggung_jawab' => $penanggungJawab,
            'tanggal_cetak' => now()->isoFormat('DD MMMM YYYY'),
        ];

        $pdf = Pdf::loadView('laporan.laba_rugi_pdf', $pdfData);

        return $pdf->download("laba_rugi_$startDate.pdf");
    }

    /**
     * Generate PDF Arus Kas.
     */
    public function generateArusKasPdf(array $data, string $startDate, string $endDate, string $instansiNama = '', ?string $penanggungJawab = null)
    {
        $pdfData = [
            'judul' => 'Laporan Arus Kas',
            'periode' => "$startDate s/d $endDate",
            'instansi' => $instansiNama,
            'data' => $data,
            'penanggung_jawab' => $penanggungJawab,
            'tanggal_cetak' => now()->isoFormat('DD MMMM YYYY'),
        ];

        $pdf = Pdf::loadView('laporan.arus_kas_pdf', $pdfData);

        return $pdf->download("arus_kas_$startDate.pdf");
    }

    /**
     * Generate Excel Arus Kas.
     */
    public function generateArusKasExcel(array $data, string $startDate, string $endDate, ?string $penanggungJawab = null)
    {
        $spreadsheet = new Spreadsheet;
        $sheet = $spreadsheet->getActiveSheet();

        $rows = [];
        $rows[] = ['Aktivitas', 'Kategori', 'Masuk', 'Keluar', 'Bersih'];

        // Operasi
        foreach ($data['detail_operasi'] ?? [] as $item) {
            $bersih = ($item['masuk'] ?? 0) - ($item['keluar'] ?? 0);
            $rows[] = [
                'Operasi', $item['kategori'],
                (float) ($item['masuk'] ?? 0),
                (float) ($item['keluar'] ?? 0),
                (float) $bersih,
            ];
        }

        // Pendanaan
        foreach ($data['detail_pendanaan'] ?? [] as $item) {
            $bersih = ($item['masuk'] ?? 0) - ($item['keluar'] ?? 0);
            $rows[] = [
                'Pendanaan', $item['kategori'],
                (float) ($item['masuk'] ?? 0),
                (float) ($item['keluar'] ?? 0),
                (float) $bersih,
            ];
        }

        $rows[] = ['', 'Kenaikan Bersih Kas', '', '',
            (float) ($data['kenaikan_bersih_kas'] ?? 0)];
        $rows[] = ['', 'Saldo Akhir', '', '',
            (float) ($data['saldo_akhir'] ?? 0)];

        // ─── Footer: identitas penanggung jawab ───
        $rows[] = [];
        $rows[] = ['', 'Penanggung Jawab', '', '',
            $penanggungJawab ?? '-'];

        $sheet->fromArray($rows, null, 'A1');
        $this->applyWatermark($sheet);

        $filename = storage_path('app/public/arus_kas_'.$startDate.'_'.Str::random(8).'.xlsx');
        (new XlsxWriter($spreadsheet))->save($filename);
        $spreadsheet->disconnectWorksheets();

        return response()->download($filename)->deleteFileAfterSend(true);
    }

    /**
     * Tempelkan gambar watermark di lapisan kedua worksheet (di balik data).
     */
    private function applyWatermark(\PhpOffice\PhpSpreadsheet\Worksheet\Worksheet $sheet): void
    {
        $wmPath = resource_path('watermarks/koda-suaka.png');

        if (is_file($wmPath)) {
            $sheet->setBackgroundImage((string) file_get_contents($wmPath));
        }
    }
}
