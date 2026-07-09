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
        return app(PermissionService::class)->userHasPermission($user, 'manage:keuangan');
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
