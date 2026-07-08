<?php

namespace App\Policies;

use App\Models\User;
use App\Models\instansi;
use App\Services\PermissionService;

class InstansiPolicy
{
    public function viewAny(User $user): bool
    {
        return $user->role?->nama_role === 'Super Admin' || app(PermissionService::class)->userHasPermission($user, 'manage:instansi');
    }

    public function view(User $user, instansi $instansi): bool
    {
        return $user->role?->nama_role === 'Super Admin' || $user->instansi_id === $instansi->id;
    }

    public function create(User $user): bool
    {
        return true; // Registration creates instansi
    }

    public function update(User $user, instansi $instansi): bool
    {
        if ($user->role?->nama_role === 'Super Admin') return true;
        if ($user->instansi_id !== $instansi->id) return false;
        return app(PermissionService::class)->userHasPermission($user, 'manage:instansi');
    }

    public function delete(User $user, instansi $instansi): bool
    {
        return $user->role?->nama_role === 'Super Admin';
    }

    public function restore(User $user, instansi $instansi): bool
    {
        return false;
    }

    public function forceDelete(User $user, instansi $instansi): bool
    {
        return false;
    }
}
