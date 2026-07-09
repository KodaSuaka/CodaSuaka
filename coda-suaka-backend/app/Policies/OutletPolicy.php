<?php

namespace App\Policies;

use App\Models\User;
use App\Models\outlet;
use App\Services\PermissionService;

class OutletPolicy
{
    public function viewAny(User $user): bool
    {
        return app(PermissionService::class)->userHasPermission($user, 'manage:outlets');
    }

    public function view(User $user, outlet $outlet): bool
    {
        return $user->instansi_id === $outlet->instansi_id;
    }

    public function create(User $user): bool
    {
        return app(PermissionService::class)->userHasPermission($user, 'manage:outlets');
    }

    public function update(User $user, outlet $outlet): bool
    {
        if ($user->instansi_id !== $outlet->instansi_id) {
            return false;
        }
        return app(PermissionService::class)->userHasPermission($user, 'manage:outlets');
    }

    public function delete(User $user, outlet $outlet): bool
    {
        if ($user->instansi_id !== $outlet->instansi_id) {
            return false;
        }
        return app(PermissionService::class)->userHasPermission($user, 'manage:outlets');
    }

    public function restore(User $user, outlet $outlet): bool
    {
        return false;
    }

    public function forceDelete(User $user, outlet $outlet): bool
    {
        return false;
    }
}
