<?php

namespace App\Services;

use App\Models\TransaksiKas;
use Barryvdh\DomPDF\Facade\Pdf;
use OpenSpout\Writer\XLSX\Writer;
use OpenSpout\Common\Entity\Row;
use Illuminate\Support\Facades\Storage;

class LaporanExportService
{
    /**
     * Generate PDF Buku Kas.
     */
    public function generateBukuKasPdf(array $transaksis, string $startDate, string $endDate, string $instansiNama = '')
    {
        $data = [
            'judul' => 'Laporan Buku Kas',
            'periode' => "$startDate s/d $endDate",
            'instansi' => $instansiNama,
            'transaksis' => $transaksis,
            'tanggal_cetak' => now()->isoFormat('DD MMMM YYYY'),
        ];

        $pdf = Pdf::loadView('laporan.buku_kas_pdf', $data);
        return $pdf->download("buku_kas_$startDate.pdf");
    }

    /**
     * Generate Excel Buku Kas — dikelompokkan per kategori (Pemasukan & Pengeluaran).
     */
    public function generateBukuKasExcel(array $transaksis, array $grouped, string $startDate, string $endDate)
    {
        $writer = new Writer();
        $filename = storage_path("app/public/buku_kas_$startDate.xlsx");

        $writer->openToFile($filename);

        // ─── Sheet 1: Semua Transaksi (detail) ───
        $writer->addRow(Row::fromValues(['No', 'Tanggal', 'Kategori', 'Tipe', 'Nominal', 'Metode', 'Keterangan']));
        $no = 1;
        foreach ($transaksis as $t) {
            $writer->addRow(Row::fromValues([
                $no++,
                $t['tanggal'],
                $t['kategori'] ?? '-',
                $t['tipe'] === 'masuk' ? 'Masuk' : 'Keluar',
                number_format($t['nominal'], 0, ',', '.'),
                $t['metode_pembayaran'] ?? '-',
                $t['keterangan'] ?? '',
            ]));
        }

        // ─── Sheet 2: Ringkasan per Kategori Pemasukan ───
        $writer->addRow(Row::fromValues([])); // baris kosong
        $writer->addRow(Row::fromValues(['RINGKASAN PER KATEGORI - PEMASUKAN']));
        $writer->addRow(Row::fromValues(['Kategori', 'Jumlah Transaksi', 'Total Nominal']));
        $grandTotalMasuk = 0;
        if (!empty($grouped['pemasukan'])) {
            foreach ($grouped['pemasukan'] as $kategori => $items) {
                $total = array_sum(array_column($items, 'nominal'));
                $grandTotalMasuk += $total;
                $writer->addRow(Row::fromValues([
                    $kategori,
                    count($items),
                    number_format($total, 0, ',', '.'),
                ]));
            }
        }
        $writer->addRow(Row::fromValues([
            'TOTAL PEMASUKAN',
            '',
            number_format($grandTotalMasuk, 0, ',', '.'),
        ]));

        // ─── Sheet 3: Ringkasan per Kategori Pengeluaran ───
        $writer->addRow(Row::fromValues([])); // baris kosong
        $writer->addRow(Row::fromValues(['RINGKASAN PER KATEGORI - PENGELUARAN']));
        $writer->addRow(Row::fromValues(['Kategori', 'Jumlah Transaksi', 'Total Nominal']));
        $grandTotalKeluar = 0;
        if (!empty($grouped['pengeluaran'])) {
            foreach ($grouped['pengeluaran'] as $kategori => $items) {
                $total = array_sum(array_column($items, 'nominal'));
                $grandTotalKeluar += $total;
                $writer->addRow(Row::fromValues([
                    $kategori,
                    count($items),
                    number_format($total, 0, ',', '.'),
                ]));
            }
        }
        $writer->addRow(Row::fromValues([
            'TOTAL PENGELUARAN',
            '',
            number_format($grandTotalKeluar, 0, ',', '.'),
        ]));

        $writer->close();
        return response()->download($filename)->deleteFileAfterSend(true);
    }

    /**
     * Generate PDF Laba Rugi — dengan rincian per kategori.
     */
    public function generateLabaRugiPdf(array $data, string $startDate, string $endDate, string $instansiNama = '')
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
            'tanggal_cetak' => now()->isoFormat('DD MMMM YYYY'),
        ];

        $pdf = Pdf::loadView('laporan.laba_rugi_pdf', $pdfData);
        return $pdf->download("laba_rugi_$startDate.pdf");
    }

    /**
     * Generate PDF Arus Kas.
     */
    public function generateArusKasPdf(array $data, string $startDate, string $endDate, string $instansiNama = '')
    {
        $pdfData = [
            'judul' => 'Laporan Arus Kas',
            'periode' => "$startDate s/d $endDate",
            'instansi' => $instansiNama,
            'data' => $data,
            'tanggal_cetak' => now()->isoFormat('DD MMMM YYYY'),
        ];

        $pdf = Pdf::loadView('laporan.arus_kas_pdf', $pdfData);
        return $pdf->download("arus_kas_$startDate.pdf");
    }

    /**
     * Generate Excel Arus Kas.
     */
    public function generateArusKasExcel(array $data, string $startDate, string $endDate)
    {
        $writer = new Writer();
        $filename = storage_path("app/public/arus_kas_$startDate.xlsx");

        $writer->openToFile($filename);
        $writer->addRow(Row::fromValues(['Aktivitas', 'Kategori', 'Masuk', 'Keluar', 'Bersih']));

        // Operasi
        foreach ($data['detail_operasi'] ?? [] as $item) {
            $bersih = ($item['masuk'] ?? 0) - ($item['keluar'] ?? 0);
            $writer->addRow(Row::fromValues([
                'Operasi', $item['kategori'],
                number_format($item['masuk'] ?? 0, 0, ',', '.'),
                number_format($item['keluar'] ?? 0, 0, ',', '.'),
                number_format($bersih, 0, ',', '.'),
            ]));
        }

        // Pendanaan
        foreach ($data['detail_pendanaan'] ?? [] as $item) {
            $bersih = ($item['masuk'] ?? 0) - ($item['keluar'] ?? 0);
            $writer->addRow(Row::fromValues([
                'Pendanaan', $item['kategori'],
                number_format($item['masuk'] ?? 0, 0, ',', '.'),
                number_format($item['keluar'] ?? 0, 0, ',', '.'),
                number_format($bersih, 0, ',', '.'),
            ]));
        }

        $writer->addRow(Row::fromValues(['', 'Kenaikan Bersih Kas', '', '',
            number_format($data['kenaikan_bersih_kas'] ?? 0, 0, ',', '.')]));
        $writer->addRow(Row::fromValues(['', 'Saldo Akhir', '', '',
            number_format($data['saldo_akhir'] ?? 0, 0, ',', '.')]));

        $writer->close();
        return response()->download($filename)->deleteFileAfterSend(true);
    }
}
