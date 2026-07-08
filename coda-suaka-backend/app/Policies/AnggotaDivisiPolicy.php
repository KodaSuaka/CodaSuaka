<?php

namespace App\Policies;

use App\Models\AnggotaDivisi;
use App\Models\User;
use App\Services\PermissionService;

class AnggotaDivisiPolicy
{
    public function viewAny(User $user): bool
    {
        return $user->role?->nama_role === 'Super Admin' || $user->instansi_id !== null;
    }

    public function view(User $user, AnggotaDivisi $anggotaDivisi): bool
    {
        return $user->instansi_id === $anggotaDivisi->divisi?->outlet?->instansi_id;
    }

    public function create(User $user): bool
    {
        return app(PermissionService::class)->userHasPermission($user, 'manage:divisi');
    }

    public function update(User $user, AnggotaDivisi $anggotaDivisi): bool
    {
        if ($user->instansi_id !== $anggotaDivisi->divisi?->outlet?->instansi_id) {
            return false;
        }
        return app(PermissionService::class)->userHasPermission($user, 'manage:divisi');
    }

    public function delete(User $user, AnggotaDivisi $anggotaDivisi): bool
    {
        if ($user->instansi_id !== $anggotaDivisi->divisi?->outlet?->instansi_id) {
            return false;
        }
        return app(PermissionService::class)->userHasPermission($user, 'manage:divisi');
    }

    public function restore(User $user, AnggotaDivisi $anggotaDivisi): bool
    {
        return false;
    }

    public function forceDelete(User $user, AnggotaDivisi $anggotaDivisi): bool
    {
        return false;
    }
}
