<?php

namespace App\Policies;

use App\Models\User;
use App\Models\penugasan;

class PenugasanPolicy
{
    public function viewAny(User $user): bool
    {
        return in_array($user->role?->nama_role, ['Owner', 'Admin', 'Karyawan']);
    }

    public function view(User $user, penugasan $penugasan): bool
    {
        return $user->instansi_id === $penugasan->divisi?->outlet?->instansi_id;
    }

    public function create(User $user): bool
    {
        return in_array($user->role?->nama_role, ['Owner', 'Super Admin', 'Admin']);
    }

    public function update(User $user, penugasan $penugasan): bool
    {
        if ($user->instansi_id !== $penugasan->divisi?->outlet?->instansi_id) {
            return false;
        }
        return in_array($user->role?->nama_role, ['Owner', 'Super Admin', 'Admin']);
    }

    public function delete(User $user, penugasan $penugasan): bool
    {
        if ($user->instansi_id !== $penugasan->divisi?->outlet?->instansi_id) {
            return false;
        }
        return in_array($user->role?->nama_role, ['Owner', 'Super Admin', 'Admin']);
    }

    public function restore(User $user, penugasan $penugasan): bool
    {
        return false;
    }

    public function forceDelete(User $user, penugasan $penugasan): bool
    {
        return false;
    }
}
