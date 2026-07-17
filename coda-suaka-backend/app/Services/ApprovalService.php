<?php

namespace App\Services;

use App\Models\User;
use App\Models\TransaksiKas;
use App\Models\ApprovalLog;
use Carbon\Carbon;

class ApprovalService
{
    /**
     * Cek apakah transaksi perlu melalui workflow approval.
     *
     * @param TransaksiKas $transaksi
     * @return bool
     */
    public function perluApproval(TransaksiKas $transaksi): bool
    {
        return $transaksi->needsApproval();
    }

    /**
     * Ajukan transaksi untuk approval.
     * Otomatis mengubah status_approval transaksi menjadi 'pending'.
     *
     * @param TransaksiKas $transaksi
     * @param User $pengaju
     * @return ApprovalLog
     */
    public function ajukanApproval(TransaksiKas $transaksi, User $pengaju): ApprovalLog
    {
        // Update status transaksi
        $transaksi->update(['status_approval' => 'pending']);

        // Buat log approval
        $log = ApprovalLog::create([
            'transaksi_kas_id' => $transaksi->id,
            'diajukan_oleh' => $pengaju->id,
            'status' => 'pending',
            'tanggal_diajukan' => Carbon::now(),
        ]);

        return $log->load(['pengaju', 'transaksiKas']);
    }

    /**
     * Setujui transaksi yang diajukan.
     *
     * @param ApprovalLog $log
     * @param User $pemeriksa
     * @param string|null $catatan
     * @return void
     */
    public function setujui(ApprovalLog $log, User $pemeriksa, ?string $catatan = null): void
    {
        $log->update([
            'disetujui_oleh' => $pemeriksa->id,
            'status' => 'disetujui',
            'catatan' => $catatan,
            'tanggal_diproses' => Carbon::now(),
        ]);

        // Update status transaksi
        $log->transaksiKas()->update(['status_approval' => 'disetujui']);
    }

    /**
     * Tolak transaksi yang diajukan.
     *
     * @param ApprovalLog $log
     * @param User $pemeriksa
     * @param string $catatan
     * @return void
     */
    public function tolak(ApprovalLog $log, User $pemeriksa, string $catatan): void
    {
        $log->update([
            'disetujui_oleh' => $pemeriksa->id,
            'status' => 'ditolak',
            'catatan' => $catatan,
            'tanggal_diproses' => Carbon::now(),
        ]);

        // Update status transaksi
        $log->transaksiKas()->update(['status_approval' => 'ditolak']);
    }

    /**
     * Dapatkan daftar transaksi yang perlu approval (pending) untuk suatu instansi.
     *
     * @param User $user
     * @param array $filters
     * @return \Illuminate\Contracts\Pagination\LengthAwarePaginator
     */
    public function getPendingApprovals(User $user, array $filters = []): \Illuminate\Contracts\Pagination\LengthAwarePaginator
    {
        return ApprovalLog::with([
                'transaksiKas.kategoriTransaksi',
                'transaksiKas.outlet',
                'transaksiKas.createdByUser',
                'pengaju',
            ])
            ->whereHas('transaksiKas', function ($q) use ($user, $filters) {
                $q->where('instansi_id', $user->instansi_id);

                if (!empty($filters['outlet_id'])) {
                    $q->where('outlet_id', $filters['outlet_id']);
                }
                if (!empty($filters['start_date'])) {
                    $q->whereDate('tanggal', '>=', $filters['start_date']);
                }
                if (!empty($filters['end_date'])) {
                    $q->whereDate('tanggal', '<=', $filters['end_date']);
                }
            })
            ->where('status', 'pending')
            ->orderBy('tanggal_diajukan', 'desc')
            ->paginate($filters['per_page'] ?? 50);
    }

    /**
     * Dapatkan riwayat approval (semua status) untuk suatu instansi.
     *
     * @param User $user
     * @param array $filters
     * @return \Illuminate\Contracts\Pagination\LengthAwarePaginator
     */
    public function getRiwayatApproval(User $user, array $filters = []): \Illuminate\Contracts\Pagination\LengthAwarePaginator
    {
        $query = ApprovalLog::with([
                'transaksiKas.kategoriTransaksi',
                'transaksiKas.outlet',
                'transaksiKas.createdByUser',
                'pengaju',
                'pemeriksa',
            ])
            ->whereHas('transaksiKas', function ($q) use ($user, $filters) {
                $q->where('instansi_id', $user->instansi_id);

                if (!empty($filters['outlet_id'])) {
                    $q->where('outlet_id', $filters['outlet_id']);
                }
                if (!empty($filters['start_date'])) {
                    $q->whereDate('tanggal', '>=', $filters['start_date']);
                }
                if (!empty($filters['end_date'])) {
                    $q->whereDate('tanggal', '<=', $filters['end_date']);
                }
            });

        // Filter by status
        if (!empty($filters['status']) && in_array($filters['status'], ['pending', 'disetujui', 'ditolak'])) {
            $query->where('status', $filters['status']);
        }

        return $query->orderBy('created_at', 'desc')
            ->paginate($filters['per_page'] ?? 50);
    }
}
