<?php

namespace App\Support;

/**
 * Peta layout template laporan keuangan divisi keuangan
 * (tests/fixtures/template-laporan-keuangan.xlsx). SATU sumber kebenaran yang
 * dipakai bersama export (dan import saat fiturnya diambil).
 *
 * Tiap baris = array kolom A..E. Sel:
 *  - skalar string/int   → teks literal (judul, label, nomor)
 *  - ['t'=>'input','src'=>?] → sel input KUNING; 'src' = nama kategori sumber
 *    prefill (null jika harus diisi manual keuangan, mis. penyusutan/persediaan)
 *  - ['t'=>'formula','expr'=>'=..'] → sel formula BIRU (formula asli template)
 *  - '' / null → sel kosong
 *
 * Koordinat & formula diambil PERSIS dari file template (lihat docs/plan).
 */
class LaporanTemplateMap
{
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
     * @return array<int, array<int, mixed>>
     */
    public static function rows(string $jenis, string $tipeUsaha, int $bulan, int $tahun): array
    {
        if ($jenis === 'laba_rugi' && $tipeUsaha === 'barang') {
            return self::labaRugiBarang($bulan, $tahun);
        }
        if ($jenis === 'laba_rugi' && $tipeUsaha === 'jasa') {
            return self::labaRugiJasa($bulan, $tahun);
        }
        if ($jenis === 'arus_kas' && $tipeUsaha === 'barang') {
            return self::arusKasBarang($bulan, $tahun);
        }
        if ($jenis === 'arus_kas' && $tipeUsaha === 'jasa') {
            return self::arusKasJasa($bulan, $tahun);
        }

        throw new \InvalidArgumentException("Peta untuk {$jenis}/{$tipeUsaha} belum didefinisikan.");
    }

    private static function input(?string $src): array
    {
        return ['t' => 'input', 'src' => $src];
    }

    private static function formula(string $expr): array
    {
        return ['t' => 'formula', 'expr' => $expr];
    }

    private static function labaRugiBarang(int $bulan, int $tahun): array
    {
        $in = fn (?string $src) => self::input($src);
        $f = fn (string $e) => self::formula($e);

        return [
            ['LAPORAN LABA RUGI', '', '', '', ''],
            ['USAHA DAGANG (BARANG)', '', '', '', ''],
            [self::periodeLabel($bulan, $tahun), '', '', '', ''],
            ['No', 'Keterangan', 'Jumlah (Rp)', 'Sub Total (Rp)', 'Total (Rp)'],
            ['A. PENDAPATAN', '', '', '', ''],
            ['1', 'Penjualan Kotor', $in('Penjualan Barang'), '', ''],
            ['2', 'Retur & Potongan Penjualan', $in(null), '', ''],
            ['', 'Penjualan Bersih', '', $f('=C6-C7'), ''],
            ['B. HARGA POKOK PENJUALAN (HPP)', '', '', '', ''],
            ['1', 'Persediaan Awal', $in(null), '', ''],
            ['2', 'Pembelian Bersih', $in('Pembelian Bahan/Stok'), '', ''],
            ['3', 'Beban Angkut Pembelian', $in(null), '', ''],
            ['4', 'Retur Pembelian', $in(null), '', ''],
            ['', 'Barang Tersedia untuk Dijual', '', $f('=C10+C11+C12-C13'), ''],
            ['5', 'Persediaan Akhir', $in(null), '', ''],
            ['', 'Total HPP', '', $f('=D14-C15'), ''],
            ['LABA KOTOR', '', '', '', $f('=D8-D16')],
            ['C. BEBAN OPERASIONAL', '', '', '', ''],
            ['1', 'Beban Gaji & Upah', $in('Gaji & Upah'), '', ''],
            ['2', 'Beban Sewa', $in('Sewa Tempat'), '', ''],
            ['3', 'Beban Listrik & Air', $in('Listrik, Air, Internet'), '', ''],
            ['4', 'Beban Transportasi', $in(null), '', ''],
            ['5', 'Beban Pemasaran', $in(null), '', ''],
            ['6', 'Beban Penyusutan', $in(null), '', ''],
            ['7', 'Beban Administrasi', $in(null), '', ''],
            ['8', 'Beban Lain-lain', $in('Operasional Lain-lain'), '', ''],
            ['', 'Total Beban Operasional', '', $f('=SUM(C19:C26)'), ''],
            ['LABA / RUGI BERSIH', '', '', '', $f('=E17-D27')],
        ];
    }

    private static function labaRugiJasa(int $bulan, int $tahun): array
    {
        $in = fn (?string $src) => self::input($src);
        $f = fn (string $e) => self::formula($e);

        return [
            ['LAPORAN LABA RUGI', '', '', '', ''],
            ['USAHA JASA', '', '', '', ''],
            [self::periodeLabel($bulan, $tahun), '', '', '', ''],
            ['No', 'Keterangan', 'Jumlah (Rp)', 'Sub Total (Rp)', 'Total (Rp)'],
            ['A. PENDAPATAN JASA', '', '', '', ''],
            ['1', 'Pendapatan Jasa Utama', $in('Pendapatan Jasa'), '', ''],
            ['2', 'Pendapatan Jasa Lain-lain', $in(null), '', ''],
            ['3', 'Potongan/Diskon Jasa', $in(null), '', ''],
            ['', 'Total Pendapatan Jasa', '', $f('=C6+C7-C8'), ''],
            ['B. BEBAN LANGSUNG JASA', '', '', '', ''],
            ['1', 'Beban Bahan/Material', $in(null), '', ''],
            ['2', 'Beban Tenaga Kerja Langsung', $in(null), '', ''],
            ['3', 'Beban Peralatan & Perlengkapan', $in(null), '', ''],
            ['4', 'Beban Lain-lain Langsung', $in(null), '', ''],
            ['', 'Total Beban Langsung', '', $f('=SUM(C11:C14)'), ''],
            ['LABA KOTOR', '', '', '', $f('=D9-D15')],
            ['C. BEBAN OPERASIONAL', '', '', '', ''],
            ['1', 'Beban Gaji & Upah', $in('Gaji & Upah'), '', ''],
            ['2', 'Beban Sewa Kantor', $in('Sewa Tempat'), '', ''],
            ['3', 'Beban Listrik & Air', $in('Listrik, Air, Internet'), '', ''],
            ['4', 'Beban Komunikasi & Internet', $in(null), '', ''],
            ['5', 'Beban Pemasaran', $in(null), '', ''],
            ['6', 'Beban Penyusutan Aset', $in(null), '', ''],
            ['7', 'Beban Administrasi & Umum', $in(null), '', ''],
            ['8', 'Beban Lain-lain', $in('Operasional Lain-lain'), '', ''],
            ['', 'Total Beban Operasional', '', $f('=SUM(C18:C25)'), ''],
            ['LABA / RUGI BERSIH', '', '', '', $f('=E16-D26')],
        ];
    }

    private static function arusKasBarang(int $bulan, int $tahun): array
    {
        $in = fn (?string $src) => self::input($src);
        $f = fn (string $e) => self::formula($e);

        // Kolom: A No, B Keterangan, C Arus Masuk, D Arus Keluar.
        // Baris 28 SENGAJA kosong (sesuai template) supaya RINGKASAN mulai di
        // baris 29 & formula lintas-sel C34=C30+C31+C32+C33 menunjuk sel benar.
        return [
            ['LAPORAN ARUS KAS', '', '', ''],
            ['USAHA DAGANG (BARANG)', '', '', ''],
            [self::periodeLabel($bulan, $tahun), '', '', ''],
            ['No', 'Keterangan', 'Arus Masuk (Rp)', 'Arus Keluar (Rp)'],
            ['A. ARUS KAS DARI AKTIVITAS OPERASI', '', '', ''],
            ['1', 'Penerimaan dari Penjualan Tunai', $in('Penjualan Barang'), ''],
            ['2', 'Penerimaan Piutang Dagang', $in(null), ''],
            ['3', 'Penerimaan Lain-lain', $in(null), ''],
            ['4', 'Pembayaran Pembelian Barang Dagangan', '', $in('Pembelian Bahan/Stok')],
            ['5', 'Pembayaran Beban Gaji', '', $in('Gaji & Upah')],
            ['6', 'Pembayaran Beban Sewa', '', $in('Sewa Tempat')],
            ['7', 'Pembayaran Beban Operasional', '', $in('Operasional Lain-lain')],
            ['8', 'Pembayaran Hutang Dagang', '', $in(null)],
            ['9', 'Pembayaran Pajak', '', $in(null)],
            ['', 'Subtotal Arus Kas Operasi', $f('=SUM(C6:C8)'), $f('=SUM(D9:D14)')],
            ['', 'NET ARUS KAS OPERASI', '', $f('=C15-D15')],
            ['B. ARUS KAS DARI AKTIVITAS INVESTASI', '', '', ''],
            ['1', 'Penerimaan Penjualan Aset Tetap', $in(null), ''],
            ['2', 'Pembelian Aset Tetap/Peralatan', '', $in(null)],
            ['3', 'Pembelian Perlengkapan Usaha', '', $in(null)],
            ['', 'NET ARUS KAS INVESTASI', '', $f('=C18-D19-D20')],
            ['C. ARUS KAS DARI AKTIVITAS PENDANAAN', '', '', ''],
            ['1', 'Penerimaan Pinjaman/Modal', $in('Setoran Modal'), ''],
            ['2', 'Penerimaan Tambahan Investasi', $in(null), ''],
            ['3', 'Pembayaran Cicilan Pinjaman', '', $in('Bayar Cicilan Pinjaman')],
            ['4', 'Penarikan Modal/Prive', '', $in('Prive (Ambil Pribadi)')],
            ['', 'NET ARUS KAS PENDANAAN', '', $f('=C23+C24-D25-D26')],
            ['', '', '', ''], // baris 28 KOSONG — jangan hapus
            ['RINGKASAN SALDO KAS', '', '', ''],
            ['', 'Saldo Kas Awal Periode', $in(null), ''],
            ['', 'Net Arus Kas Operasi', $f('=D16'), ''],
            ['', 'Net Arus Kas Investasi', $f('=D21'), ''],
            ['', 'Net Arus Kas Pendanaan', $f('=D27'), ''],
            ['', 'SALDO KAS AKHIR PERIODE', $f('=C30+C31+C32+C33'), ''],
        ];
    }

    private static function arusKasJasa(int $bulan, int $tahun): array
    {
        $in = fn (?string $src) => self::input($src);
        $f = fn (string $e) => self::formula($e);

        // Struktur & posisi formula identik dengan arus kas barang (termasuk
        // baris 28 kosong); hanya label operasi/investasi yang khas jasa.
        return [
            ['LAPORAN ARUS KAS', '', '', ''],
            ['USAHA JASA', '', '', ''],
            [self::periodeLabel($bulan, $tahun), '', '', ''],
            ['No', 'Keterangan', 'Arus Masuk (Rp)', 'Arus Keluar (Rp)'],
            ['A. ARUS KAS DARI AKTIVITAS OPERASI', '', '', ''],
            ['1', 'Penerimaan Pendapatan Jasa Tunai', $in('Pendapatan Jasa'), ''],
            ['2', 'Penerimaan Piutang Jasa', $in(null), ''],
            ['3', 'Penerimaan Pendapatan Lain-lain', $in(null), ''],
            ['4', 'Pembayaran Beban Gaji & Tenaga Kerja', '', $in('Gaji & Upah')],
            ['5', 'Pembayaran Beban Material/Bahan', '', $in(null)],
            ['6', 'Pembayaran Beban Sewa', '', $in('Sewa Tempat')],
            ['7', 'Pembayaran Beban Komunikasi & Internet', '', $in('Listrik, Air, Internet')],
            ['8', 'Pembayaran Beban Operasional Lain', '', $in('Operasional Lain-lain')],
            ['9', 'Pembayaran Pajak', '', $in(null)],
            ['', 'Subtotal Arus Kas Operasi', $f('=SUM(C6:C8)'), $f('=SUM(D9:D14)')],
            ['', 'NET ARUS KAS OPERASI', '', $f('=C15-D15')],
            ['B. ARUS KAS DARI AKTIVITAS INVESTASI', '', '', ''],
            ['1', 'Penerimaan Penjualan Peralatan', $in(null), ''],
            ['2', 'Pembelian Peralatan/Software', '', $in(null)],
            ['3', 'Pembelian Aset Tidak Berwujud', '', $in(null)],
            ['', 'NET ARUS KAS INVESTASI', '', $f('=C18-D19-D20')],
            ['C. ARUS KAS DARI AKTIVITAS PENDANAAN', '', '', ''],
            ['1', 'Penerimaan Pinjaman/Modal', $in('Setoran Modal'), ''],
            ['2', 'Penerimaan Investasi Tambahan', $in(null), ''],
            ['3', 'Pembayaran Cicilan Pinjaman', '', $in('Bayar Cicilan Pinjaman')],
            ['4', 'Penarikan Modal/Prive', '', $in('Prive (Ambil Pribadi)')],
            ['', 'NET ARUS KAS PENDANAAN', '', $f('=C23+C24-D25-D26')],
            ['', '', '', ''], // baris 28 KOSONG — jangan hapus
            ['RINGKASAN SALDO KAS', '', '', ''],
            ['', 'Saldo Kas Awal Periode', $in(null), ''],
            ['', 'Net Arus Kas Operasi', $f('=D16'), ''],
            ['', 'Net Arus Kas Investasi', $f('=D21'), ''],
            ['', 'Net Arus Kas Pendanaan', $f('=D27'), ''],
            ['', 'SALDO KAS AKHIR PERIODE', $f('=C30+C31+C32+C33'), ''],
        ];
    }
}
