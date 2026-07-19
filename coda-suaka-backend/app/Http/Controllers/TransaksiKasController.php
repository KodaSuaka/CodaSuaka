<?php

namespace App\Http\Controllers;

use App\Http\Requests\StoreTransaksiKasRequest;
use App\Http\Requests\UpdateTransaksiKasRequest;
use App\Models\TransaksiKas;
use App\Traits\ApiResponse;
use App\Services\ApprovalService;
use App\Services\AuditService;
use Carbon\Carbon;
use Illuminate\Http\Request;

class TransaksiKasController extends Controller
{
    use ApiResponse;

    protected ApprovalService $approvalService;
    protected AuditService $auditService;

    public function __construct(ApprovalService $approvalService, AuditService $auditService)
    {
        $this->approvalService = $approvalService;
        $this->auditService = $auditService;
        $this->authorizeResource(TransaksiKas::class, 'transaksi_kas');
    }

    /**
     * GET /api/transaksi-kas
     * Daftar entri Buku Kas dengan filter.
     */
    public function index(Request $request)
    {
        $user = $request->user();

        $query = TransaksiKas::with(['kategoriTransaksi', 'outlet', 'createdByUser'])
            ->where('instansi_id', $user->instansi_id);

        // Filter by outlet
        if ($request->has('outlet_id')) {
            $query->where('outlet_id', $request->outlet_id);
        }

        // Filter by tipe (masuk/keluar)
        if ($request->has('tipe')) {
            $query->where('tipe', $request->tipe);
        }

        // Filter by kategori
        if ($request->has('kategori_transaksi_id')) {
            $query->where('kategori_transaksi_id', $request->kategori_transaksi_id);
        }

        // Filter by date range
        if ($request->has('start_date')) {
            $query->whereDate('tanggal', '>=', $request->start_date);
        }
        if ($request->has('end_date')) {
            $query->whereDate('tanggal', '<=', $request->end_date);
        }

        // Filter by status_approval
        if ($request->has('status_approval')) {
            $query->where('status_approval', $request->status_approval);
        }

        // Default order: newest first
        $query->orderBy('tanggal', 'desc')->orderBy('created_at', 'desc');

        $transaksis = $query->paginate($request->get('per_page', 50));

        return $this->paginated($transaksis);
    }

    /**
     * POST /api/transaksi-kas
     * Buat transaksi baru. Jika perlu approval, otomatis ajukan.
     */
    public function store(StoreTransaksiKasRequest $request)
    {
        $user = $request->user();

        // ─── Cegah duplikat transaksi ─────────────────────────────────
        $duplicate = TransaksiKas::where('instansi_id', $user->instansi_id)
            ->where('tipe', $request->tipe)
            ->where('kategori_transaksi_id', $request->kategori_transaksi_id)
            ->where('nominal', $request->nominal)
            ->where('tanggal', $request->tanggal)
            ->where('metode_pembayaran', $request->metode_pembayaran)
            ->where('keterangan', $request->keterangan)
            ->first();

        if ($duplicate) {
            return $this->error('Transaksi duplikat terdeteksi. Data yang sama sudah ada.', 409);
        }

        // Tentukan status_approval awal
        $statusApproval = 'disetujui'; // default: langsung aktif

        $transaksi = TransaksiKas::create([
            'instansi_id' => $user->instansi_id,
            'outlet_id' => $request->outlet_id,
            'kategori_transaksi_id' => $request->kategori_transaksi_id,
            'tanggal' => $request->tanggal,
            'tipe' => $request->tipe,
            'nominal' => $request->nominal,
            'metode_pembayaran' => $request->metode_pembayaran,
            'keterangan' => $request->keterangan,
            'lampiran_url' => $request->lampiran_url,
            'created_by' => $user->id,
            'status_approval' => $statusApproval,
        ]);

        // Audit log: created
        $this->auditService->created($transaksi, $user);

        // Auto-submit approval jika perlu
        if ($this->approvalService->perluApproval($transaksi)) {
            $this->approvalService->ajukanApproval($transaksi, $user);
        }

        $transaksi->load(['kategoriTransaksi', 'outlet', 'createdByUser']);

        $message = 'Entri kas berhasil ditambahkan';
        if ($transaksi->status_approval === 'pending') {
            $message .= ' dan menunggu approval';
        }

        return $this->success($transaksi, $message, 201);
    }

    /**
     * GET /api/transaksi-kas/{transaksi_kas}
     */
    public function show(TransaksiKas $transaksi_kas)
    {
        $transaksi_kas->load([
            'kategoriTransaksi',
            'outlet',
            'createdByUser',
            'approvalLogs.pengaju',
            'approvalLogs.pemeriksa',
        ]);
        return $this->success($transaksi_kas);
    }

    /**
     * PUT /api/transaksi-kas/{transaksi_kas}
     * Hanya bisa update jika status_approval = disetujui (default) atau ditolak.
     * Jika transaksi dalam status pending, tidak bisa diedit.
     */
    public function update(UpdateTransaksiKasRequest $request, TransaksiKas $transaksi_kas)
    {
        $user = $request->user();

        // Cek apakah bisa diedit
        if ($transaksi_kas->status_approval === 'pending') {
            return $this->error('Transaksi yang sedang menunggu approval tidak dapat diedit. Batalkan pengajuan terlebih dahulu.', 422);
        }

        // Simpan snapshot before untuk audit
        $original = $transaksi_kas->getOriginal();

        $transaksi_kas->update($request->only([
            'tanggal', 'tipe', 'nominal', 'kategori_transaksi_id',
            'outlet_id', 'metode_pembayaran', 'keterangan', 'lampiran_url',
        ]));

        // Audit log: updated
        $this->auditService->updated($transaksi_kas, $original, $user);

        // Reset status_approval ke default setelah diupdate
        // jika sebelumnya ditolak, dan transaksi perlu approval, ajukan lagi
        if ($transaksi_kas->status_approval === 'ditolak') {
            $transaksi_kas->update(['status_approval' => 'disetujui']);

            if ($this->approvalService->perluApproval($transaksi_kas)) {
                $this->approvalService->ajukanApproval($transaksi_kas, $user);
            }
        }

        $transaksi_kas->load(['kategoriTransaksi', 'outlet', 'createdByUser']);

        return $this->success($transaksi_kas, 'Entri kas berhasil diperbarui');
    }

    /**
     * DELETE /api/transaksi-kas/{transaksi_kas}
     * Hanya bisa hapus jika status_approval bukan 'disetujui'.
     */
    public function destroy(TransaksiKas $transaksi_kas)
    {
        $user = request()->user();

        // Cegah hapus transaksi yang sudah disetujui (aktif)
        if ($transaksi_kas->status_approval === 'disetujui' && $transaksi_kas->dokumen_transaksi_id !== null) {
            return $this->error('Entri kas dari nota/invoice tidak dapat dihapus langsung. Hapus dokumen asalnya.', 422);
        }

        // Audit log: deleted (before actual delete)
        $this->auditService->deleted($transaksi_kas, $user);

        // Jika transaksi sudah disetujui dan tidak dari dokumen, tetap bisa dihapus dengan permission delete:keuangan
        $transaksi_kas->delete();
        return $this->success(null, 'Entri kas berhasil dihapus');
    }

    /**
     * GET /api/transaksi-kas/saldo
     * Hitung saldo (Total Masuk - Total Keluar) untuk suatu periode.
     */
    public function saldo(Request $request)
    {
        $this->authorize('viewAny', TransaksiKas::class);

        $user = $request->user();

        $query = TransaksiKas::where('instansi_id', $user->instansi_id);

        // Filter by outlet
        if ($request->has('outlet_id')) {
            $query->where('outlet_id', $request->outlet_id);
        }

        // Filter by date range
        if ($request->has('start_date')) {
            $query->whereDate('tanggal', '>=', $request->start_date);
        }
        if ($request->has('end_date')) {
            $query->whereDate('tanggal', '<=', $request->end_date);
        }

        $totalMasuk = (float) $query->clone()->where('tipe', 'masuk')->sum('nominal');
        $totalKeluar = (float) $query->clone()->where('tipe', 'keluar')->sum('nominal');

        return $this->success([
            'total_masuk' => $totalMasuk,
            'total_keluar' => $totalKeluar,
            'saldo_akhir' => $totalMasuk - $totalKeluar,
            'start_date' => $request->start_date,
            'end_date' => $request->end_date,
        ]);
    }

    /**
     * GET /api/transaksi-kas/laporan/laba-rugi
     * Hitung Pendapatan - Beban HPP - Beban Operasional = Laba/Rugi.
     */
    public function labaRugi(Request $request)
    {
        $this->authorize('viewAny', TransaksiKas::class);

        $user = $request->user();

        $query = TransaksiKas::with('kategoriTransaksi')
            ->where('instansi_id', $user->instansi_id);

        // Filter by outlet
        if ($request->has('outlet_id')) {
            $query->where('outlet_id', $request->outlet_id);
        }

        // Filter by date range
        if ($request->has('start_date')) {
            $query->whereDate('tanggal', '>=', $request->start_date);
        }
        if ($request->has('end_date')) {
            $query->whereDate('tanggal', '<=', $request->end_date);
        }

        // ─── Aggregate ───
        $totalPendapatan = (float) (clone $query)
            ->where('tipe', 'masuk')
            ->sum('nominal');

        $totalHpp = (float) (clone $query)
            ->where('tipe', 'keluar')
            ->whereHas('kategoriTransaksi', fn($q) => $q->where('termasuk_hpp', true))
            ->sum('nominal');

        $totalBeban = (float) (clone $query)
            ->where('tipe', 'keluar')
            ->whereHas('kategoriTransaksi', fn($q) => $q->where('termasuk_hpp', false))
            ->sum('nominal');

        $labaRugi = $totalPendapatan - $totalHpp - $totalBeban;

        // ─── Breakdown per kategori ───
        $transaksis = (clone $query)->get();

        $pendapatanPerKategori = [];
        foreach ($transaksis->where('tipe', 'masuk') as $t) {
            $kategori = $t->kategoriTransaksi?->nama_kategori ?? 'Tanpa Kategori';
            $pendapatanPerKategori[$kategori] = ($pendapatanPerKategori[$kategori] ?? 0) + (float) $t->nominal;
        }

        $hppPerKategori = [];
        foreach ($transaksis->where('tipe', 'keluar')->filter(fn($t) => $t->kategoriTransaksi?->termasuk_hpp) as $t) {
            $kategori = $t->kategoriTransaksi?->nama_kategori ?? 'Tanpa Kategori';
            $hppPerKategori[$kategori] = ($hppPerKategori[$kategori] ?? 0) + (float) $t->nominal;
        }

        $bebanPerKategori = [];
        foreach ($transaksis->where('tipe', 'keluar')->filter(fn($t) => !$t->kategoriTransaksi?->termasuk_hpp) as $t) {
            $kategori = $t->kategoriTransaksi?->nama_kategori ?? 'Tanpa Kategori';
            $bebanPerKategori[$kategori] = ($bebanPerKategori[$kategori] ?? 0) + (float) $t->nominal;
        }

        return $this->success([
            'pendapatan' => $totalPendapatan,
            'hpp' => $totalHpp,
            'beban_operasional' => $totalBeban,
            'laba_rugi' => $labaRugi,
            'pendapatan_per_kategori' => $pendapatanPerKategori,
            'hpp_per_kategori' => $hppPerKategori,
            'beban_per_kategori' => $bebanPerKategori,
            'start_date' => $request->start_date,
            'end_date' => $request->end_date,
        ]);
    }
}
