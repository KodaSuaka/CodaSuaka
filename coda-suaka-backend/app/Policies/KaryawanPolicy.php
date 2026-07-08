<?php

namespace App\Policies;

use App\Models\User;
use App\Models\karyawan;

class KaryawanPolicy
{
    public function viewAny(User $user): bool
    {
        return in_array($user->role?->nama_role, ['Owner', 'Admin', 'Karyawan']);
    }

    public function view(User $user, karyawan $karyawan): bool
    {
        // Must be in same tenant
        return $user->instansi_id === $karyawan->user?->instansi_id;
    }

    public function create(User $user): bool
    {
        return in_array($user->role?->nama_role, ['Owner', 'Super Admin']);
    }

    public function update(User $user, karyawan $karyawan): bool
    {
        if ($user->instansi_id !== $karyawan->user?->instansi_id) {
            return false;
        }
        return in_array($user->role?->nama_role, ['Owner', 'Super Admin']);
    }

    public function delete(User $user, karyawan $karyawan): bool
    {
        if ($user->instansi_id !== $karyawan->user?->instansi_id) {
            return false;
        }
        return in_array($user->role?->nama_role, ['Owner', 'Super Admin']);
    }

    public function restore(User $user, karyawan $karyawan): bool
    {
        return false;
    }

    public function forceDelete(User $user, karyawan $karyawan): bool
    {
        return false;
    }
}
