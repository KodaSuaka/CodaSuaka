<?php

namespace App\Policies;

use App\Models\User;
use App\Models\outlet;
use Illuminate\Auth\Access\Response;

class OutletPolicy
{
    /**
     * Determine whether the user can view any models.
     */
    public function viewAny(User $user): bool
    {
        return in_array($user->role?->nama_role, ['Owner', 'Super Admin', 'Admin', 'Karyawan']);
    }

    /**
     * Determine whether the user can view the model.
     */
    public function view(User $user, outlet $outlet): bool
    {
        return $user->instansi_id === $outlet->instansi_id;
    }

    /**
     * Determine whether the user can create models.
     */
    public function create(User $user): bool
    {
        return in_array($user->role?->nama_role, ['Owner', 'Super Admin']);
    }

    /**
     * Determine whether the user can update the model.
     */
    public function update(User $user, outlet $outlet): bool
    {
        if ($user->instansi_id !== $outlet->instansi_id) {
            return false;
        }
        return in_array($user->role?->nama_role, ['Owner', 'Super Admin']);
    }

    /**
     * Determine whether the user can delete the model.
     */
    public function delete(User $user, outlet $outlet): bool
    {
        if ($user->instansi_id !== $outlet->instansi_id) {
            return false;
        }
        return in_array($user->role?->nama_role, ['Owner', 'Super Admin']);
    }

    /**
     * Determine whether the user can restore the model.
     */
    public function restore(User $user, outlet $outlet): bool
    {
        return false;
    }

    /**
     * Determine whether the user can permanently delete the model.
     */
    public function forceDelete(User $user, outlet $outlet): bool
    {
        return false;
    }
}
