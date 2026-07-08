<?php

namespace App\Policies;

use App\Models\User;
use App\Models\user as UserModel;

class UserPolicy
{
    public function viewAny(User $user): bool
    {
        return in_array($user->role?->nama_role, ['Owner', 'Super Admin']);
    }

    public function view(User $user, UserModel $model): bool
    {
        return $user->instansi_id === $model->instansi_id || $user->id === $model->id;
    }

    public function create(User $user): bool
    {
        return true; // Registration
    }

    public function update(User $user, UserModel $model): bool
    {
        if ($user->instansi_id !== $model->instansi_id) {
            return false;
        }
        // Users can update themselves, Owner can update anyone in their instansi
        return $user->id === $model->id || $user->role?->nama_role === 'Owner';
    }

    public function delete(User $user, UserModel $model): bool
    {
        if ($user->instansi_id !== $model->instansi_id) {
            return false;
        }
        return $user->role?->nama_role === 'Owner';
    }

    public function restore(User $user, UserModel $model): bool
    {
        return false;
    }

    public function forceDelete(User $user, UserModel $model): bool
    {
        return false;
    }
}
