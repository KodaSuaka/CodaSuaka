<?php

namespace App\Policies;

use App\Models\User;
use App\Services\PermissionService;

class UserPolicy
{
    public function viewAny(User $user): bool
    {
        return app(PermissionService::class)->userHasPermission($user, 'manage:karyawan');
    }

    public function view(User $user, User $model): bool
    {
        return $user->instansi_id === $model->instansi_id || $user->id === $model->id;
    }

    public function create(User $user): bool
    {
        return true; // Registration
    }

    public function update(User $user, User $model): bool
    {
        if ($user->instansi_id !== $model->instansi_id) {
            return false;
        }

        // Users can update themselves, or users with manage:karyawan permission
        return $user->id === $model->id || app(PermissionService::class)->userHasPermission($user, 'manage:karyawan');
    }

    public function delete(User $user, User $model): bool
    {
        if ($user->instansi_id !== $model->instansi_id) {
            return false;
        }

        return app(PermissionService::class)->userHasPermission($user, 'manage:karyawan');
    }

    public function restore(User $user, User $model): bool
    {
        return false;
    }

    public function forceDelete(User $user, User $model): bool
    {
        return false;
    }
}
