<?php

namespace App\Policies;

use App\Models\User;
use App\Models\transaksi_paket;

class TransaksiPaketPolicy
{
    public function viewAny(User $user): bool
    {
        return in_array($user->role?->nama_role, ['Owner', 'Super Admin']);
    }

    public function view(User $user, transaksi_paket $transaksiPaket): bool
    {
        return $user->instansi_id === $transaksiPaket->instansi_id;
    }

    public function create(User $user): bool
    {
        return in_array($user->role?->nama_role, ['Owner', 'Super Admin']);
    }

    public function update(User $user, transaksi_paket $transaksiPaket): bool
    {
        if ($user->instansi_id !== $transaksiPaket->instansi_id) {
            return false;
        }
        return in_array($user->role?->nama_role, ['Owner', 'Super Admin']);
    }

    public function delete(User $user, transaksi_paket $transaksiPaket): bool
    {
        if ($user->instansi_id !== $transaksiPaket->instansi_id) {
            return false;
        }
        return in_array($user->role?->nama_role, ['Owner', 'Super Admin']);
    }

    public function restore(User $user, transaksi_paket $transaksiPaket): bool
    {
        return false;
    }

    public function forceDelete(User $user, transaksi_paket $transaksiPaket): bool
    {
        return false;
    }
}
