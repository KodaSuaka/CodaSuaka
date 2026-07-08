<?php

namespace App\Policies;

use App\Models\User;
use App\Models\transaksi_paket;
use App\Services\PermissionService;

class TransaksiPaketPolicy
{
    public function viewAny(User $user): bool
    {
        return app(PermissionService::class)->userHasPermission($user, 'manage:paket');
    }

    public function view(User $user, transaksi_paket $transaksiPaket): bool
    {
        return $user->instansi_id === $transaksiPaket->instansi_id;
    }

    public function create(User $user): bool
    {
        return app(PermissionService::class)->userHasPermission($user, 'manage:paket');
    }

    public function update(User $user, transaksi_paket $transaksiPaket): bool
    {
        if ($user->instansi_id !== $transaksiPaket->instansi_id) {
            return false;
        }
        return app(PermissionService::class)->userHasPermission($user, 'manage:paket');
    }

    public function delete(User $user, transaksi_paket $transaksiPaket): bool
    {
        if ($user->instansi_id !== $transaksiPaket->instansi_id) {
            return false;
        }
        return app(PermissionService::class)->userHasPermission($user, 'manage:paket');
    }

    public function restore(User $user, transaksi_paket $transaksiPaket): bool
    {
        return false;
    }

    public function forceDelete(User $user, transaksi_paket $transaksiPaket): bool
    {
        return false;
    }
}
