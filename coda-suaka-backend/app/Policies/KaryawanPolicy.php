<?php

namespace App\Policies;

use App\Models\User;
use App\Models\karyawan;
use App\Services\PermissionService;

class KaryawanPolicy
{
    public function viewAny(User $user): bool
    {
        return $user->role?->nama_role === 'Super Admin' || $user->instansi_id !== null;
    }

    public function view(User $user, karyawan $karyawan): bool
    {
        // Must be in same tenant
        return $user->instansi_id === $karyawan->user?->instansi_id;
    }

    public function create(User $user): bool
    {
        return app(PermissionService::class)->userHasPermission($user, 'manage:karyawan');
    }

    public function update(User $user, karyawan $karyawan): bool
    {
        if ($user->instansi_id !== $karyawan->user?->instansi_id) {
            return false;
        }
        return app(PermissionService::class)->userHasPermission($user, 'manage:karyawan');
    }

    public function delete(User $user, karyawan $karyawan): bool
    {
        if ($user->instansi_id !== $karyawan->user?->instansi_id) {
            return false;
        }
        return app(PermissionService::class)->userHasPermission($user, 'manage:karyawan');
    }

    public function restore(User $user, karyawan $karyawan): bool
    {
        return false;
    }

    public function forceDelete(User $user, karyawan $karyawan): bool
    {
        return false;
    }
}
