<?php

namespace App\Policies;

use App\Models\User;
use App\Models\TransaksiKas;
use App\Services\PermissionService;

class TransaksiKasPolicy
{
    public function viewAny(User $user): bool
    {
        return app(PermissionService::class)->userHasPermission($user, 'view:keuangan');
    }

    public function view(User $user, TransaksiKas $transaksiKas): bool
    {
        if ($user->instansi_id !== $transaksiKas->instansi_id) {
            return false;
        }
        return app(PermissionService::class)->userHasPermission($user, 'view:keuangan');
    }

    public function create(User $user): bool
    {
        return app(PermissionService::class)->userHasPermission($user, 'manage:keuangan');
    }

    public function update(User $user, TransaksiKas $transaksiKas): bool
    {
        if ($user->instansi_id !== $transaksiKas->instansi_id) {
            return false;
        }
        return app(PermissionService::class)->userHasPermission($user, 'manage:keuangan');
    }

    public function delete(User $user, TransaksiKas $transaksiKas): bool
    {
        if ($user->instansi_id !== $transaksiKas->instansi_id) {
            return false;
        }
        return app(PermissionService::class)->userHasPermission($user, 'delete:keuangan');
    }

    /**
     * User bisa mengekspor data transaksi (PDF/Excel).
     */
    public function export(User $user): bool
    {
        return app(PermissionService::class)->userHasPermission($user, 'export:keuangan');
    }

    /**
     * User bisa menyetujui transaksi yang perlu approval.
     */
    public function approve(User $user, TransaksiKas $transaksiKas): bool
    {
        if ($user->instansi_id !== $transaksiKas->instansi_id) {
            return false;
        }
        return app(PermissionService::class)->userHasPermission($user, 'approve:keuangan');
    }

    public function restore(User $user, TransaksiKas $transaksiKas): bool
    {
        return false;
    }

    public function forceDelete(User $user, TransaksiKas $transaksiKas): bool
    {
        return false;
    }
}
