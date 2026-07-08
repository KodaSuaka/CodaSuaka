<?php

namespace App\Policies;

use App\Models\User;
use App\Models\paket;
use App\Services\PermissionService;

class PaketPolicy
{
    public function viewAny(User $user): bool
    {
        return true; // All authenticated users can see available packages
    }

    public function view(User $user, paket $paket): bool
    {
        return true; // Packages are public catalog
    }

    public function create(User $user): bool
    {
        return app(PermissionService::class)->userHasPermission($user, 'manage:paket');
    }

    public function update(User $user, paket $paket): bool
    {
        return app(PermissionService::class)->userHasPermission($user, 'manage:paket');
    }

    public function delete(User $user, paket $paket): bool
    {
        return app(PermissionService::class)->userHasPermission($user, 'manage:paket');
    }

    public function restore(User $user, paket $paket): bool
    {
        return false;
    }

    public function forceDelete(User $user, paket $paket): bool
    {
        return false;
    }
}
