<?php

namespace App\Policies;

use App\Models\AnggotaDivisi;
use App\Models\User;

class AnggotaDivisiPolicy
{
    public function viewAny(User $user): bool
    {
        return in_array($user->role?->nama_role, ['Owner', 'Admin', 'Karyawan']);
    }

    public function view(User $user, AnggotaDivisi $anggotaDivisi): bool
    {
        return $user->instansi_id === $anggotaDivisi->divisi?->outlet?->instansi_id;
    }

    public function create(User $user): bool
    {
        return in_array($user->role?->nama_role, ['Owner', 'Super Admin', 'Admin']);
    }

    public function update(User $user, AnggotaDivisi $anggotaDivisi): bool
    {
        if ($user->instansi_id !== $anggotaDivisi->divisi?->outlet?->instansi_id) {
            return false;
        }
        return in_array($user->role?->nama_role, ['Owner', 'Super Admin', 'Admin']);
    }

    public function delete(User $user, AnggotaDivisi $anggotaDivisi): bool
    {
        if ($user->instansi_id !== $anggotaDivisi->divisi?->outlet?->instansi_id) {
            return false;
        }
        return in_array($user->role?->nama_role, ['Owner', 'Super Admin', 'Admin']);
    }

    public function restore(User $user, AnggotaDivisi $anggotaDivisi): bool
    {
        return false;
    }

    public function forceDelete(User $user, AnggotaDivisi $anggotaDivisi): bool
    {
        return false;
    }
}
