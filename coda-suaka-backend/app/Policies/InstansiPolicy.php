<?php

namespace App\Policies;

use App\Models\User;
use App\Models\instansi;

class InstansiPolicy
{
    public function viewAny(User $user): bool
    {
        return $user->role?->nama_role === 'Super Admin';
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
        return $user->role?->nama_role === 'Super Admin' || $user->instansi_id === $instansi->id;
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
