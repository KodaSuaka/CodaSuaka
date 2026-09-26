<?php

namespace App\Support;

/**
 * Peta pengisian template laporan keuangan divisi keuangan
 * (resources/templates/laporan-keuangan.xlsx).
 *
 * Sejak fitur read-and-fill: STRUKTUR, formula, format, merged cell semuanya
 * berasal dari file template — peta ini HANYA mendefinisikan sel input mana
 * (koordinat) diisi dari total kategori transaksi mana. Sel yang tak ada di
 * peta dibiarkan kosong untuk diisi manual divisi keuangan (penyusutan,
 * persediaan, retur, dll).
 */
class LaporanTemplateMap
{
    /** Sel tempat label periode ditulis (menggantikan "Periode: [Bulan/Tahun]"). */
    public const PERIODE_CELL = 'A3';

    private const BULAN = [
        1 => 'JANUARI', 2 => 'FEBRUARI', 3 => 'MARET', 4 => 'APRIL',
        5 => 'MEI', 6 => 'JUNI', 7 => 'JULI', 8 => 'AGUSTUS',
        9 => 'SEPTEMBER', 10 => 'OKTOBER', 11 => 'NOVEMBER', 12 => 'DESEMBER',
    ];

    public static function sheetName(string $jenis, string $tipeUsaha): string
    {
        $prefix = $jenis === 'laba_rugi' ? 'Laba Rugi' : 'Arus Kas';
        $suffix = $tipeUsaha === 'barang' ? 'Barang' : 'Jasa';

        return "{$prefix} - {$suffix}";
    }

    public static function periodeLabel(int $bulan, int $tahun): string
    {
        return 'Periode: '.(self::BULAN[$bulan] ?? $bulan).' '.$tahun;
    }

    /**
     * Koordinat sel input → nama kategori sumber prefill.
     *
     * @return array<string, string>
     */
    public static function prefillCells(string $jenis, string $tipeUsaha): array
    {
        $key = "{$jenis}/{$tipeUsaha}";

        return match ($key) {
            'laba_rugi/barang' => [
                'C6' => 'Penjualan Barang',
                'C11' => 'Pembelian Bahan/Stok',
                'C19' => 'Gaji & Upah',
                'C20' => 'Sewa Tempat',
                'C21' => 'Listrik, Air, Internet',
                'C26' => 'Operasional Lain-lain',
            ],
            'laba_rugi/jasa' => [
                'C6' => 'Pendapatan Jasa',
                'C18' => 'Gaji & Upah',
                'C19' => 'Sewa Tempat',
                'C20' => 'Listrik, Air, Internet',
                'C25' => 'Operasional Lain-lain',
            ],
            'arus_kas/barang' => [
                'C6' => 'Penjualan Barang',
                'D9' => 'Pembelian Bahan/Stok',
                'D10' => 'Gaji & Upah',
                'D11' => 'Sewa Tempat',
                'D12' => 'Operasional Lain-lain',
                'C23' => 'Setoran Modal',
                'D25' => 'Bayar Cicilan Pinjaman',
                'D26' => 'Prive (Ambil Pribadi)',
            ],
            'arus_kas/jasa' => [
                'C6' => 'Pendapatan Jasa',
                'D9' => 'Gaji & Upah',
                'D11' => 'Sewa Tempat',
                'D12' => 'Listrik, Air, Internet',
                'D13' => 'Operasional Lain-lain',
                'C23' => 'Setoran Modal',
                'D25' => 'Bayar Cicilan Pinjaman',
                'D26' => 'Prive (Ambil Pribadi)',
            ],
            default => throw new \InvalidArgumentException("Peta untuk {$key} belum didefinisikan."),
        };
    }
}
