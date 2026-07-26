<?php

namespace App\Services;

use App\Models\ApprovalLog;
use App\Models\TransaksiKas;
use App\Models\User;
use Carbon\Carbon;
use Illuminate\Contracts\Pagination\LengthAwarePaginator;

class ApprovalService
{
    /**
     * Cek apakah transaksi perlu melalui workflow approval.
     */
    public function perluApproval(TransaksiKas $transaksi): bool
    {
        return $transaksi->needsApproval();
    }

    /**
     * Ajukan transaksi untuk approval.
     * Otomatis mengubah status_approval transaksi menjadi 'pending'.
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
     */
    public function getPendingApprovals(User $user, array $filters = []): LengthAwarePaginator
    {
        return ApprovalLog::with([
            'transaksiKas' => fn ($q) => $q->select([
                'id', 'tanggal', 'tipe', 'nominal', 'metode_pembayaran',
                'keterangan', 'status_approval', 'created_by', 'outlet_id',
            ]),
            'transaksiKas.kategoriTransaksi' => fn ($q) => $q->select(['id', 'nama_kategori']),
            'transaksiKas.outlet' => fn ($q) => $q->select(['id', 'nama_outlet']),
            'transaksiKas.createdByUser' => fn ($q) => $q->select(['id', 'name']),
            'pengaju' => fn ($q) => $q->select(['id', 'name']),
        ])
            ->select([
                'id', 'transaksi_kas_id', 'diajukan_oleh', 'disetujui_oleh',
                'status', 'catatan', 'tanggal_diajukan', 'created_at',
            ])
            ->whereHas('transaksiKas', function ($q) use ($user, $filters) {
                $q->where('instansi_id', $user->instansi_id);

                if (! empty($filters['outlet_id'])) {
                    $q->where('outlet_id', $filters['outlet_id']);
                }
                if (! empty($filters['start_date'])) {
                    $q->whereDate('tanggal', '>=', $filters['start_date']);
                }
                if (! empty($filters['end_date'])) {
                    $q->whereDate('tanggal', '<=', $filters['end_date']);
                }
            })
            ->where('status', 'pending')
            ->orderBy('tanggal_diajukan', 'desc')
            ->paginate($filters['per_page'] ?? 50);
    }

    /**
     * Dapatkan riwayat approval (semua status) untuk suatu instansi.
     */
    public function getRiwayatApproval(User $user, array $filters = []): LengthAwarePaginator
    {
        $query = ApprovalLog::with([
            'transaksiKas' => fn ($q) => $q->select([
                'id', 'tanggal', 'tipe', 'nominal', 'metode_pembayaran',
                'keterangan', 'status_approval', 'created_by', 'outlet_id',
            ]),
            'transaksiKas.kategoriTransaksi' => fn ($q) => $q->select(['id', 'nama_kategori']),
            'transaksiKas.outlet' => fn ($q) => $q->select(['id', 'nama_outlet']),
            'transaksiKas.createdByUser' => fn ($q) => $q->select(['id', 'name']),
            'pengaju' => fn ($q) => $q->select(['id', 'name']),
            'pemeriksa' => fn ($q) => $q->select(['id', 'name']),
        ])
            ->select([
                'id', 'transaksi_kas_id', 'diajukan_oleh', 'disetujui_oleh',
                'status', 'catatan', 'tanggal_diajukan', 'tanggal_diproses',
                'created_at',
            ])
            ->whereHas('transaksiKas', function ($q) use ($user, $filters) {
                $q->where('instansi_id', $user->instansi_id);

                if (! empty($filters['outlet_id'])) {
                    $q->where('outlet_id', $filters['outlet_id']);
                }
                if (! empty($filters['start_date'])) {
                    $q->whereDate('tanggal', '>=', $filters['start_date']);
                }
                if (! empty($filters['end_date'])) {
                    $q->whereDate('tanggal', '<=', $filters['end_date']);
                }
            });

        // Filter by status
        if (! empty($filters['status']) && in_array($filters['status'], ['pending', 'disetujui', 'ditolak'])) {
            $query->where('status', $filters['status']);
        }

        return $query->orderBy('created_at', 'desc')
            ->paginate($filters['per_page'] ?? 50);
    }
}
