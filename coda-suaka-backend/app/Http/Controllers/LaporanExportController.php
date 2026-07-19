<?php

namespace App\Http\Controllers;

use App\Models\TransaksiKas;
use App\Services\LaporanExportService;
use App\Traits\ApiResponse;
use Carbon\Carbon;
use Illuminate\Http\Request;

class LaporanExportController extends Controller
{
    use ApiResponse;

    public function __construct(
        protected LaporanExportService $exportService
    ) {}

    /**
     * GET /api/laporan/buku-kas/export/pdf
     */
    public function exportBukuKasPdf(Request $request)
    {
        $user = $request->user();
        $this->authorize('export', TransaksiKas::class);

        $startDate = $request->start_date ?? Carbon::now()->startOfMonth()->toDateString();
        $endDate = $request->end_date ?? Carbon::now()->toDateString();

        $transaksis = TransaksiKas::with('kategoriTransaksi')
            ->where('instansi_id', $user->instansi_id)
            ->whereBetween('tanggal', [$startDate, $endDate])
            ->when($request->outlet_id, fn($q) => $q->where('outlet_id', $request->outlet_id))
            ->orderBy('tanggal')
            ->get()
            ->toArray();

        $instansiNama = $user->instansi->nama_instansi ?? '';

        return $this->exportService->generateBukuKasPdf($transaksis, $startDate, $endDate, $instansiNama);
    }

    /**
     * GET /api/laporan/buku-kas/export/excel
     */
    public function exportBukuKasExcel(Request $request)
    {
        $user = $request->user();
        $this->authorize('export', TransaksiKas::class);

        $startDate = $request->start_date ?? Carbon::now()->startOfMonth()->toDateString();
        $endDate = $request->end_date ?? Carbon::now()->toDateString();

        $transaksis = TransaksiKas::with('kategoriTransaksi')
            ->where('instansi_id', $user->instansi_id)
            ->whereBetween('tanggal', [$startDate, $endDate])
            ->when($request->outlet_id, fn($q) => $q->where('outlet_id', $request->outlet_id))
            ->orderBy('tanggal')
            ->get()
            ->map(function ($t) {
                return [
                    'tanggal' => $t->tanggal,
                    'kategori' => $t->kategoriTransaksi?->nama_kategori,
                    'kategori_id' => $t->kategori_transaksi_id,
                    'tipe' => $t->tipe,
                    'nominal' => $t->nominal,
                    'metode_pembayaran' => $t->metode_pembayaran,
                    'keterangan' => $t->keterangan,
                ];
            })
            ->toArray();

        // Group per kategori
        $grouped = [];
        foreach ($transaksis as $t) {
            $key = $t['kategori'] ?? 'Tanpa Kategori';
            $grpTipe = $t['tipe'] === 'masuk' ? 'pemasukan' : 'pengeluaran';
            $grouped[$grpTipe][$key][] = $t;
        }

        return $this->exportService->generateBukuKasExcel($transaksis, $grouped, $startDate, $endDate);
    }

    /**
     * GET /api/laporan/laba-rugi/export/pdf
     */
    public function exportLabaRugiPdf(Request $request)
    {
        $user = $request->user();
        $this->authorize('export', TransaksiKas::class);

        $startDate = $request->start_date ?? Carbon::now()->startOfMonth()->toDateString();
        $endDate = $request->end_date ?? Carbon::now()->toDateString();

        // Ambil data laba rugi dari controller TransaksiKas
        $data = $this->getLabaRugiData($user, $startDate, $endDate, $request->outlet_id);
        $instansiNama = $user->instansi->nama_instansi ?? '';

        return $this->exportService->generateLabaRugiPdf($data, $startDate, $endDate, $instansiNama);
    }

    /**
     * GET /api/laporan/arus-kas/export/pdf
     */
    public function exportArusKasPdf(Request $request)
    {
        $user = $request->user();
        $this->authorize('export', TransaksiKas::class);

        $startDate = $request->start_date ?? Carbon::now()->startOfMonth()->toDateString();
        $endDate = $request->end_date ?? Carbon::now()->toDateString();

        $data = $this->getArusKasData($user, $startDate, $endDate, $request->outlet_id);
        $instansiNama = $user->instansi->nama_instansi ?? '';

        return $this->exportService->generateArusKasPdf($data, $startDate, $endDate, $instansiNama);
    }

    /**
     * GET /api/laporan/arus-kas/export/excel
     */
    public function exportArusKasExcel(Request $request)
    {
        $user = $request->user();
        $this->authorize('export', TransaksiKas::class);

        $startDate = $request->start_date ?? Carbon::now()->startOfMonth()->toDateString();
        $endDate = $request->end_date ?? Carbon::now()->toDateString();

        $data = $this->getArusKasData($user, $startDate, $endDate, $request->outlet_id);

        return $this->exportService->generateArusKasExcel($data, $startDate, $endDate);
    }

    /**
     * Helper: ambil data laba rugi dengan breakdown per kategori.
     */
    private function getLabaRugiData($user, string $startDate, string $endDate, $outletId = null): array
    {
        $query = TransaksiKas::with('kategoriTransaksi')
            ->where('instansi_id', $user->instansi_id)
            ->whereBetween('tanggal', [$startDate, $endDate])
            ->when($outletId, fn($q) => $q->where('outlet_id', $outletId));

        $transaksis = (clone $query)->get();

        // Breakdown pendapatan per kategori (tipe = masuk)
        $pendapatanPerKategori = [];
        $totalPendapatan = 0;
        foreach ($transaksis->where('tipe', 'masuk') as $t) {
            $kategori = $t->kategoriTransaksi?->nama_kategori ?? 'Tanpa Kategori';
            $pendapatanPerKategori[$kategori] = ($pendapatanPerKategori[$kategori] ?? 0) + (float) $t->nominal;
            $totalPendapatan += (float) $t->nominal;
        }

        // Breakdown HPP per kategori (keluar, termasuk_hpp = true)
        $hppPerKategori = [];
        $totalHpp = 0;
        foreach ($transaksis->where('tipe', 'keluar')->filter(fn($t) => $t->kategoriTransaksi?->termasuk_hpp) as $t) {
            $kategori = $t->kategoriTransaksi?->nama_kategori ?? 'Tanpa Kategori';
            $hppPerKategori[$kategori] = ($hppPerKategori[$kategori] ?? 0) + (float) $t->nominal;
            $totalHpp += (float) $t->nominal;
        }

        // Breakdown beban per kategori (keluar, operasional, bukan HPP)
        $bebanPerKategori = [];
        $totalBeban = 0;
        foreach ($transaksis->where('tipe', 'keluar')->filter(fn($t) => !$t->kategoriTransaksi?->termasuk_hpp) as $t) {
            $kategori = $t->kategoriTransaksi?->nama_kategori ?? 'Tanpa Kategori';
            $bebanPerKategori[$kategori] = ($bebanPerKategori[$kategori] ?? 0) + (float) $t->nominal;
            $totalBeban += (float) $t->nominal;
        }

        $labaBersih = $totalPendapatan - $totalHpp - $totalBeban;

        return [
            'total_pendapatan' => $totalPendapatan,
            'total_hpp' => $totalHpp,
            'total_beban' => $totalBeban,
            'laba_bersih' => $labaBersih,
            'pendapatan_per_kategori' => $pendapatanPerKategori,
            'hpp_per_kategori' => $hppPerKategori,
            'beban_per_kategori' => $bebanPerKategori,
        ];
    }

    /**
     * Helper: ambil data arus kas.
     */
    private function getArusKasData($user, string $startDate, string $endDate, $outletId = null): array
    {
        $transaksis = TransaksiKas::with('kategoriTransaksi')
            ->where('instansi_id', $user->instansi_id)
            ->whereBetween('tanggal', [$startDate, $endDate])
            ->when($outletId, fn($q) => $q->where('outlet_id', $outletId))
            ->get();

        $arusKasOperasi = ['masuk' => 0, 'keluar' => 0];
        $arusKasInvestasi = ['masuk' => 0, 'keluar' => 0];
        $arusKasPendanaan = ['masuk' => 0, 'keluar' => 0];
        $detailOperasi = [];
        $detailPendanaan = [];

        $controller = app(LaporanController::class);

        foreach ($transaksis as $t) {
            $nominal = (float) $t->nominal;
            $kategoriNama = $t->kategoriTransaksi?->nama_kategori ?? 'Tanpa Kategori';
            $tipeAktivitas = $controller->tentukanAktivitas($kategoriNama, $t->tipe);

            if ($tipeAktivitas === 'operasi') {
                $arusKasOperasi[$t->tipe] += $nominal;
                $detailOperasi[] = ['kategori' => $kategoriNama, 'masuk' => $t->tipe === 'masuk' ? $nominal : 0, 'keluar' => $t->tipe === 'keluar' ? $nominal : 0];
            } elseif ($tipeAktivitas === 'investasi') {
                $arusKasInvestasi[$t->tipe] += $nominal;
            } elseif ($tipeAktivitas === 'pendanaan') {
                $arusKasPendanaan[$t->tipe] += $nominal;
                $detailPendanaan[] = ['kategori' => $kategoriNama, 'masuk' => $t->tipe === 'masuk' ? $nominal : 0, 'keluar' => $t->tipe === 'keluar' ? $nominal : 0];
            }
        }

        $totalOperasi = $arusKasOperasi['masuk'] - $arusKasOperasi['keluar'];
        $totalInvestasi = $arusKasInvestasi['masuk'] - $arusKasInvestasi['keluar'];
        $totalPendanaan = $arusKasPendanaan['masuk'] - $arusKasPendanaan['keluar'];
        $kenaikanBersih = $totalOperasi + $totalInvestasi + $totalPendanaan;

        // Hitung saldo awal
        $saldoAwal = TransaksiKas::where('instansi_id', $user->instansi_id)
            ->where('tanggal', '<', $startDate)
            ->when($outletId, fn($q) => $q->where('outlet_id', $outletId))
            ->selectRaw("COALESCE(SUM(CASE WHEN tipe = 'masuk' THEN nominal ELSE 0 END), 0) - COALESCE(SUM(CASE WHEN tipe = 'keluar' THEN nominal ELSE 0 END), 0) as saldo")
            ->value('saldo') ?? 0;

        return [
            'arus_kas_operasi' => $totalOperasi,
            'arus_kas_investasi' => $totalInvestasi,
            'arus_kas_pendanaan' => $totalPendanaan,
            'kenaikan_bersih_kas' => $kenaikanBersih,
            'saldo_awal' => (float) $saldoAwal,
            'saldo_akhir' => (float) $saldoAwal + $kenaikanBersih,
            'detail_operasi' => $detailOperasi,
            'detail_pendanaan' => $detailPendanaan,
        ];
    }
}
