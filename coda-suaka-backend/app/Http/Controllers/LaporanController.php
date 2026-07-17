<?php

namespace App\Http\Controllers;

use App\Models\TransaksiKas;
use App\Models\KategoriTransaksi;
use App\Traits\ApiResponse;
use Carbon\Carbon;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;

class LaporanController extends Controller
{
    use ApiResponse;

    /**
     * GET /api/laporan/arus-kas
     * Laporan Arus Kas (Cash Flow Statement) berdasarkan periode & outlet.
     */
    public function arusKas(Request $request)
    {
        $user = $request->user();

        $request->validate([
            'start_date' => 'nullable|date',
            'end_date' => 'nullable|date|after_or_equal:start_date',
            'outlet_id' => 'nullable|exists:outlets,id,instansi_id,' . $user->instansi_id,
        ]);

        $startDate = $request->start_date ?? Carbon::now()->startOfMonth()->toDateString();
        $endDate = $request->end_date ?? Carbon::now()->toDateString();

        // Hitung saldo awal (sebelum start_date)
        $saldoAwal = TransaksiKas::where('instansi_id', $user->instansi_id)
            ->where('tanggal', '<', $startDate)
            ->when($request->outlet_id, fn($q) => $q->where('outlet_id', $request->outlet_id))
            ->selectRaw(
                "COALESCE(SUM(CASE WHEN tipe = 'masuk' THEN nominal ELSE 0 END), 0) -
                 COALESCE(SUM(CASE WHEN tipe = 'keluar' THEN nominal ELSE 0 END), 0) as saldo"
            )
            ->value('saldo') ?? 0;

        // Query transaksi dalam periode
        $transaksiQuery = TransaksiKas::with('kategoriTransaksi')
            ->where('instansi_id', $user->instansi_id)
            ->whereBetween('tanggal', [$startDate, $endDate])
            ->when($request->outlet_id, fn($q) => $q->where('outlet_id', $request->outlet_id));

        $transaksis = $transaksiQuery->get();

        // Kelompokkan berdasarkan tipe kategori untuk Arus Kas
        // Gunakan kategori_transaksi_id untuk menentukan aktivitas
        $arusKasOperasi = ['masuk' => 0, 'keluar' => 0];
        $arusKasInvestasi = ['masuk' => 0, 'keluar' => 0];
        $arusKasPendanaan = ['masuk' => 0, 'keluar' => 0];
        $detailOperasi = [];
        $detailPendanaan = [];

        foreach ($transaksis as $t) {
            $nominal = (float) $t->nominal;
            $kategori = $t->kategoriTransaksi;
            $kategoriNama = $kategori?->nama_kategori ?? 'Tanpa Kategori';
            $tipeAktivitas = $this->tentukanAktivitas($kategoriNama, $t->tipe);

            if ($tipeAktivitas === 'operasi') {
                $arusKasOperasi[$t->tipe] += $nominal;
                $detailOperasi[] = [
                    'kategori' => $kategoriNama,
                    'masuk' => $t->tipe === 'masuk' ? $nominal : 0,
                    'keluar' => $t->tipe === 'keluar' ? $nominal : 0,
                ];
            } elseif ($tipeAktivitas === 'investasi') {
                $arusKasInvestasi[$t->tipe] += $nominal;
            } elseif ($tipeAktivitas === 'pendanaan') {
                $arusKasPendanaan[$t->tipe] += $nominal;
                $detailPendanaan[] = [
                    'kategori' => $kategoriNama,
                    'masuk' => $t->tipe === 'masuk' ? $nominal : 0,
                    'keluar' => $t->tipe === 'keluar' ? $nominal : 0,
                ];
            }
        }

        $totalOperasi = $arusKasOperasi['masuk'] - $arusKasOperasi['keluar'];
        $totalInvestasi = $arusKasInvestasi['masuk'] - $arusKasInvestasi['keluar'];
        $totalPendanaan = $arusKasPendanaan['masuk'] - $arusKasPendanaan['keluar'];
        $kenaikanBersih = $totalOperasi + $totalInvestasi + $totalPendanaan;
        $saldoAkhir = $saldoAwal + $kenaikanBersih;

        return $this->success([
            'arus_kas_operasi' => $totalOperasi,
            'arus_kas_investasi' => $totalInvestasi,
            'arus_kas_pendanaan' => $totalPendanaan,
            'kenaikan_bersih_kas' => $kenaikanBersih,
            'saldo_awal' => (float) $saldoAwal,
            'saldo_akhir' => (float) $saldoAkhir,
            'start_date' => $startDate,
            'end_date' => $endDate,
            'detail_operasi' => $this->groupDetail($detailOperasi),
            'detail_pendanaan' => $this->groupDetail($detailPendanaan),
        ]);
    }

    /**
     * GET /api/laporan/ringkasan-keuangan
     * Data agregat per-bulan untuk grafik dashboard.
     */
    public function ringkasanKeuangan(Request $request)
    {
        $user = $request->user();
        $tahun = $request->tahun ?? Carbon::now()->year;

        $series = TransaksiKas::where('instansi_id', $user->instansi_id)
            ->whereYear('tanggal', $tahun)
            ->selectRaw(
                "DATE_FORMAT(tanggal, '%Y-%m') as bulan,
                 COALESCE(SUM(CASE WHEN tipe = 'masuk' THEN nominal ELSE 0 END), 0) as pendapatan,
                 COALESCE(SUM(CASE WHEN tipe = 'keluar' THEN nominal ELSE 0 END), 0) as beban"
            )
            ->groupBy('bulan')
            ->orderBy('bulan')
            ->get()
            ->map(function ($item) {
                return [
                    'bulan' => $item->bulan,
                    'pendapatan' => (float) $item->pendapatan,
                    'beban' => (float) $item->beban,
                    'laba' => (float) $item->pendapatan - (float) $item->beban,
                ];
            });

        return $this->success([
            'tahun' => $tahun,
            'series' => $series,
        ]);
    }

    /**
     * Tentukan aktivitas arus kas berdasarkan nama kategori.
     */
    public function tentukanAktivitas(string $namaKategori, string $tipe): string
    {
        $nama = strtolower($namaKategori);

        // Pendanaan: setoran modal, prive, pinjaman
        $keywordsPendanaan = ['modal', 'prive', 'pinjaman', 'dividen', 'saham', 'investor'];
        foreach ($keywordsPendanaan as $kw) {
            if (str_contains($nama, $kw)) return 'pendanaan';
        }

        // Investasi: aset tetap, properti, kendaraan, peralatan (untuk pembelian aset jangka panjang)
        $keywordsInvestasi = ['aset', 'tanah', 'bangunan', 'kendaraan', 'mesin', 'peralatan', 'investasi'];
        foreach ($keywordsInvestasi as $kw) {
            if (str_contains($nama, $kw)) return 'investasi';
        }

        // Default: operasi
        return 'operasi';
    }

    /**
     * Grouping detail array by kategori and sum nominal.
     */
    private function groupDetail(array $details): array
    {
        $grouped = [];
        foreach ($details as $d) {
            $key = $d['kategori'];
            if (!isset($grouped[$key])) {
                $grouped[$key] = ['kategori' => $key, 'masuk' => 0, 'keluar' => 0];
            }
            $grouped[$key]['masuk'] += $d['masuk'];
            $grouped[$key]['keluar'] += $d['keluar'];
        }
        return array_values($grouped);
    }
}
