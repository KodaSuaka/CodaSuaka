<?php

namespace App\Http\Controllers;

use App\Models\User;
use App\Models\TransaksiKas;
use App\Models\ApprovalLog;
use App\Traits\ApiResponse;
use App\Services\ApprovalService;
use App\Services\AuditService;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Gate;

class ApprovalController extends Controller
{
    use ApiResponse;

    protected ApprovalService $approvalService;
    protected AuditService $auditService;

    public function __construct(ApprovalService $approvalService, AuditService $auditService)
    {
        $this->approvalService = $approvalService;
        $this->auditService = $auditService;
    }

    /**
     * GET /api/approval/pending
     * Daftar transaksi yang menunggu approval.
     */
    public function pending(Request $request)
    {
        $user = $request->user();

        // Policy: hanya user dengan approve:keuangan yang bisa melihat
        if (!Gate::allows('approve-keuangan')) {
            return $this->error('Anda tidak memiliki izin untuk melihat daftar approval', 403);
        }

        $filters = $request->only(['outlet_id', 'start_date', 'end_date', 'per_page']);
        $approvals = $this->approvalService->getPendingApprovals($user, $filters);

        return $this->paginated($approvals);
    }

    /**
     * GET /api/approval/riwayat
     * Riwayat approval (semua status).
     */
    public function riwayat(Request $request)
    {
        $user = $request->user();

        if (!Gate::allows('approve-keuangan')) {
            return $this->error('Anda tidak memiliki izin untuk melihat riwayat approval', 403);
        }

        $filters = $request->only(['outlet_id', 'start_date', 'end_date', 'status', 'per_page']);
        $riwayat = $this->approvalService->getRiwayatApproval($user, $filters);

        return $this->paginated($riwayat);
    }

    /**
     * POST /api/approval/{transaksi_kas}/ajukan
     * Ajukan transaksi untuk approval.
     */
    public function ajukan(TransaksiKas $transaksi_kas, Request $request)
    {
        $user = $request->user();

        // Policy: user harus punya manage:keuangan
        $this->authorize('create', TransaksiKas::class);

        // Cek tenant
        if ($user->instansi_id !== $transaksi_kas->instansi_id) {
            return $this->error('Transaksi tidak ditemukan', 404);
        }

        // Cek status approval transaksi
        if ($transaksi_kas->status_approval === 'pending') {
            return $this->error('Transaksi ini sudah dalam proses approval. Harap tunggu sampai diproses.', 422);
        }
        if ($transaksi_kas->status_approval === 'ditolak') {
            return $this->error('Transaksi ini telah ditolak. Silakan edit terlebih dahulu sebelum mengajukan ulang.', 422);
        }

        // Cek apakah perlu approval
        if (!$this->approvalService->perluApproval($transaksi_kas)) {
            return $this->error('Transaksi ini tidak memerlukan approval', 422);
        }

        // Ajukan approval
        $log = $this->approvalService->ajukanApproval($transaksi_kas, $user);

        return $this->success($log, 'Transaksi berhasil diajukan untuk approval', 201);
    }

    /**
     * POST /api/approval/{approval_log}/setujui
     * Setujui transaksi yang diajukan.
     */
    public function setujui(ApprovalLog $approval_log, Request $request)
    {
        $user = $request->user();

        // Policy: hanya user dengan approve:keuangan
        if (!Gate::allows('approve-keuangan')) {
            return $this->error('Anda tidak memiliki izin untuk menyetujui transaksi', 403);
        }

        // Cek tenant
        if ($user->instansi_id !== $approval_log->transaksiKas?->instansi_id) {
            return $this->error('Approval log tidak ditemukan', 404);
        }

        // Cek status
        if ($approval_log->status !== 'pending') {
            return $this->error('Approval ini sudah diproses sebelumnya', 422);
        }

        $validator = \Illuminate\Support\Facades\Validator::make($request->all(), [
            'catatan' => 'nullable|string|max:500',
        ]);

        if ($validator->fails()) {
            return $this->error('Validasi gagal', 422, $validator->errors());
        }

        $this->approvalService->setujui($approval_log, $user, $request->catatan);

        // Audit log: approved
        if ($transaksi = $approval_log->transaksiKas) {
            $this->auditService->approval('approved', $transaksi, $user);
        }

        return $this->success([
            'approval_log' => $approval_log->fresh()->load(['pengaju', 'pemeriksa', 'transaksiKas']),
        ], 'Transaksi berhasil disetujui');
    }

    /**
     * POST /api/approval/{approval_log}/tolak
     * Tolak transaksi yang diajukan.
     */
    public function tolak(ApprovalLog $approval_log, Request $request)
    {
        $user = $request->user();

        // Policy: hanya user dengan approve:keuangan
        if (!Gate::allows('approve-keuangan')) {
            return $this->error('Anda tidak memiliki izin untuk menolak transaksi', 403);
        }

        // Cek tenant
        if ($user->instansi_id !== $approval_log->transaksiKas?->instansi_id) {
            return $this->error('Approval log tidak ditemukan', 404);
        }

        // Cek status
        if ($approval_log->status !== 'pending') {
            return $this->error('Approval ini sudah diproses sebelumnya', 422);
        }

        $validator = \Illuminate\Support\Facades\Validator::make($request->all(), [
            'catatan' => 'required|string|max:1000',
        ]);

        if ($validator->fails()) {
            return $this->error('Validasi gagal', 422, $validator->errors());
        }

        $this->approvalService->tolak($approval_log, $user, $request->catatan);

        // Audit log: rejected
        if ($transaksi = $approval_log->transaksiKas) {
            $this->auditService->approval('rejected', $transaksi, $user);
        }

        return $this->success([
            'approval_log' => $approval_log->fresh()->load(['pengaju', 'pemeriksa', 'transaksiKas']),
        ], 'Transaksi berhasil ditolak');
    }
}
