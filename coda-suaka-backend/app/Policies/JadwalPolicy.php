<?php

namespace App\Policies;

use App\Models\User;
use App\Models\jadwal;

class JadwalPolicy
{
    public function viewAny(User $user): bool
    {
        return in_array($user->role?->nama_role, ['Owner', 'Admin', 'Karyawan']);
    }

    public function view(User $user, jadwal $jadwal): bool
    {
        return $user->instansi_id === $jadwal->outlet?->instansi_id;
    }

    public function create(User $user): bool
    {
        return in_array($user->role?->nama_role, ['Owner', 'Super Admin', 'Admin']);
    }

    public function update(User $user, jadwal $jadwal): bool
    {
        if ($user->instansi_id !== $jadwal->outlet?->instansi_id) {
            return false;
        }
        return in_array($user->role?->nama_role, ['Owner', 'Super Admin', 'Admin']);
    }

    public function delete(User $user, jadwal $jadwal): bool
    {
        if ($user->instansi_id !== $jadwal->outlet?->instansi_id) {
            return false;
        }
        return in_array($user->role?->nama_role, ['Owner', 'Super Admin', 'Admin']);
    }

    public function restore(User $user, jadwal $jadwal): bool
    {
        return false;
    }

    public function forceDelete(User $user, jadwal $jadwal): bool
    {
        return false;
    }
}
