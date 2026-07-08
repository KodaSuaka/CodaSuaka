<?php

namespace App\Policies;

use App\Models\User;
use App\Models\Divisi;
use App\Services\PermissionService;

class DivisiPolicy
{
    public function viewAny(User $user): bool
    {
        return $user->role?->nama_role === 'Super Admin' || $user->instansi_id !== null;
    }

    public function view(User $user, Divisi $divisi): bool
    {
        return $user->instansi_id === $divisi->outlet?->instansi_id;
    }

    public function create(User $user): bool
    {
        return app(PermissionService::class)->userHasPermission($user, 'manage:divisi');
    }

    public function update(User $user, Divisi $divisi): bool
    {
        if ($user->instansi_id !== $divisi->outlet?->instansi_id) {
            return false;
        }
        return app(PermissionService::class)->userHasPermission($user, 'manage:divisi');
    }

    public function delete(User $user, Divisi $divisi): bool
    {
        if ($user->instansi_id !== $divisi->outlet?->instansi_id) {
            return false;
        }
        return app(PermissionService::class)->userHasPermission($user, 'manage:divisi');
    }

    public function restore(User $user, Divisi $divisi): bool
    {
        return false;
    }

    public function forceDelete(User $user, Divisi $divisi): bool
    {
        return false;
    }
}
