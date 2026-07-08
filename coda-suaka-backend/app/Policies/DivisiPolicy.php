<?php

namespace App\Policies;

use App\Models\User;
use App\Models\Divisi;

class DivisiPolicy
{
    public function viewAny(User $user): bool
    {
        return in_array($user->role?->nama_role, ['Owner', 'Admin', 'Karyawan']);
    }

    public function view(User $user, Divisi $divisi): bool
    {
        return $user->instansi_id === $divisi->outlet?->instansi_id;
    }

    public function create(User $user): bool
    {
        return in_array($user->role?->nama_role, ['Owner', 'Super Admin']);
    }

    public function update(User $user, Divisi $divisi): bool
    {
        if ($user->instansi_id !== $divisi->outlet?->instansi_id) {
            return false;
        }
        return in_array($user->role?->nama_role, ['Owner', 'Super Admin']);
    }

    public function delete(User $user, Divisi $divisi): bool
    {
        if ($user->instansi_id !== $divisi->outlet?->instansi_id) {
            return false;
        }
        return in_array($user->role?->nama_role, ['Owner', 'Super Admin']);
    }

    public function restore(User $user, Divisi $divisi): bool
    {
        return false;
    }

    public function forceDelete(User $user, Divisi $divisi): bool
    {
        return false;
    }
}
