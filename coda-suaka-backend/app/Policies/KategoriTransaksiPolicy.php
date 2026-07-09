<?php

namespace App\Policies;

use App\Models\User;
use App\Models\KategoriTransaksi;
use App\Services\PermissionService;

class KategoriTransaksiPolicy
{
    public function viewAny(User $user): bool
    {
        return app(PermissionService::class)->userHasPermission($user, 'view:keuangan');
    }

    public function view(User $user, KategoriTransaksi $kategoriTransaksi): bool
    {
        if ($user->instansi_id !== $kategoriTransaksi->instansi_id) {
            return false;
        }
        return app(PermissionService::class)->userHasPermission($user, 'view:keuangan');
    }

    public function create(User $user): bool
    {
        return app(PermissionService::class)->userHasPermission($user, 'manage:keuangan');
    }

    public function update(User $user, KategoriTransaksi $kategoriTransaksi): bool
    {
        if ($user->instansi_id !== $kategoriTransaksi->instansi_id) {
            return false;
        }
        return app(PermissionService::class)->userHasPermission($user, 'manage:keuangan');
    }

    public function delete(User $user, KategoriTransaksi $kategoriTransaksi): bool
    {
        if ($user->instansi_id !== $kategoriTransaksi->instansi_id) {
            return false;
        }
        return app(PermissionService::class)->userHasPermission($user, 'manage:keuangan');
    }

    public function restore(User $user, KategoriTransaksi $kategoriTransaksi): bool
    {
        return false;
    }

    public function forceDelete(User $user, KategoriTransaksi $kategoriTransaksi): bool
    {
        return false;
    }
}
