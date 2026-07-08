<?php

namespace App\Policies;

use App\Models\User;
use App\Models\paket;

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
        return $user->role?->nama_role === 'Super Admin';
    }

    public function update(User $user, paket $paket): bool
    {
        return $user->role?->nama_role === 'Super Admin';
    }

    public function delete(User $user, paket $paket): bool
    {
        return $user->role?->nama_role === 'Super Admin';
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
