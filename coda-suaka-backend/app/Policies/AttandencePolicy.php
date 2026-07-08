<?php

namespace App\Policies;

use App\Models\User;
use App\Models\attandence;

class AttandencePolicy
{
    public function viewAny(User $user): bool
    {
        return in_array($user->role?->nama_role, ['Owner', 'Admin', 'Karyawan']);
    }

    public function view(User $user, attandence $attandence): bool
    {
        return $user->instansi_id === $attandence->user?->instansi_id;
    }

    public function create(User $user): bool
    {
        return true; // Any authenticated user can check in
    }

    public function update(User $user, attandence $attandence): bool
    {
        // Users can only update their own attendance
        return $user->id === $attandence->user_id;
    }

    public function delete(User $user, attandence $attandence): bool
    {
        return in_array($user->role?->nama_role, ['Owner', 'Super Admin']);
    }

    public function restore(User $user, attandence $attandence): bool
    {
        return false;
    }

    public function forceDelete(User $user, attandence $attandence): bool
    {
        return false;
    }
}
