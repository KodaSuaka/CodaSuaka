<?php

namespace App\Policies;

use App\Models\User;
use App\Models\jadwal;
use App\Services\PermissionService;

class JadwalPolicy
{
    public function viewAny(User $user): bool
    {
        return $user->role?->nama_role === 'Super Admin' || $user->instansi_id !== null;
    }

    public function view(User $user, jadwal $jadwal): bool
    {
        return $user->instansi_id === $jadwal->outlet?->instansi_id;
    }

    public function create(User $user): bool
    {
        return app(PermissionService::class)->userHasPermission($user, 'manage:jadwal');
    }

    public function update(User $user, jadwal $jadwal): bool
    {
        if ($user->instansi_id !== $jadwal->outlet?->instansi_id) {
            return false;
        }
        return app(PermissionService::class)->userHasPermission($user, 'manage:jadwal');
    }

    public function delete(User $user, jadwal $jadwal): bool
    {
        if ($user->instansi_id !== $jadwal->outlet?->instansi_id) {
            return false;
        }
        return app(PermissionService::class)->userHasPermission($user, 'manage:jadwal');
    }

    public function restore(User $user, jadwal $jadwal): bool
    {
        return false;
    }

    public function forceDelete(User $user, jadwal $jadwal): bool
    {
        return false;
    }
}
