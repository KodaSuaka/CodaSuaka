<?php

namespace App\Policies;

use App\Models\User;
use App\Models\jadwal;
use App\Services\PermissionService;

class JadwalPolicy
{
    /**
     * Check if a jadwal belongs to the user's tenant.
     * Falls back to created_by when outlet_id is null.
     */
    private function isSameTenant(User $user, jadwal $jadwal): bool
    {
        if ($jadwal->outlet_id !== null) {
            return $user->instansi_id === $jadwal->outlet?->instansi_id;
        }

        // Fallback: jika outlet_id null, scope via pembuat (created_by)
        return $jadwal->relationLoaded('pembuat')
            ? $user->instansi_id === $jadwal->pembuat?->instansi_id
            : $jadwal->pembuat()->value('instansi_id') === $user->instansi_id;
    }

    public function viewAny(User $user): bool
    {
        return $user->instansi_id !== null;
    }

    public function view(User $user, jadwal $jadwal): bool
    {
        return $this->isSameTenant($user, $jadwal);
    }

    public function create(User $user): bool
    {
        return app(PermissionService::class)->userHasPermission($user, 'manage:jadwal');
    }

    public function update(User $user, jadwal $jadwal): bool
    {
        if (! $this->isSameTenant($user, $jadwal)) {
            return false;
        }
        return app(PermissionService::class)->userHasPermission($user, 'manage:jadwal');
    }

    public function delete(User $user, jadwal $jadwal): bool
    {
        if (! $this->isSameTenant($user, $jadwal)) {
            return false;
        }
        return app(PermissionService::class)->userHasPermission($user, 'manage:jadwal');
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
