<?php

namespace App\Policies;

use App\Models\Instansi;
use App\Models\User;
use App\Services\PermissionService;

class InstansiPolicy
{
    public function viewAny(User $user): bool
    {
        return app(PermissionService::class)->userHasPermission($user, 'manage:instansi');
    }

    public function view(User $user, Instansi $instansi): bool
    {
        return $user->instansi_id === $instansi->id;
    }

    public function create(User $user): bool
    {
        return true; // Registration creates instansi
    }

    public function update(User $user, Instansi $instansi): bool
    {
        if ($user->instansi_id !== $instansi->id) {
            return false;
        }

        return app(PermissionService::class)->userHasPermission($user, 'manage:instansi');
    }

    public function delete(User $user, Instansi $instansi): bool
    {
        return false; // Only Super Admin can delete (handled via SuperAdminController)
    }

    public function restore(User $user, Instansi $instansi): bool
    {
        return false;
    }

    public function forceDelete(User $user, Instansi $instansi): bool
    {
        return false;
    }
}
