<?php

namespace App\Policies;

use App\Models\User;
use App\Models\penugasan;
use App\Services\PermissionService;

class PenugasanPolicy
{
    /**
     * Check if a penugasan belongs to the user's tenant.
     * Falls back to created_by when divisi_id is null.
     */
    private function isSameTenant(User $user, penugasan $penugasan): bool
    {
        if ($penugasan->divisi_id !== null) {
            return $user->instansi_id === $penugasan->divisi?->outlet?->instansi_id;
        }

        // Fallback: jika divisi_id null, scope via pembuat (created_by)
        return $penugasan->relationLoaded('pembuat')
            ? $user->instansi_id === $penugasan->pembuat?->instansi_id
            : $penugasan->pembuat()->value('instansi_id') === $user->instansi_id;
    }

    public function viewAny(User $user): bool
    {
        return $user->instansi_id !== null;
    }

    public function view(User $user, penugasan $penugasan): bool
    {
        return $this->isSameTenant($user, $penugasan);
    }

    public function create(User $user): bool
    {
        return app(PermissionService::class)->userHasPermission($user, 'manage:penugasan');
    }

    public function update(User $user, penugasan $penugasan): bool
    {
        if (! $this->isSameTenant($user, $penugasan)) {
            return false;
        }
        return app(PermissionService::class)->userHasPermission($user, 'manage:penugasan');
    }

    public function delete(User $user, penugasan $penugasan): bool
    {
        if (! $this->isSameTenant($user, $penugasan)) {
            return false;
        }
        return app(PermissionService::class)->userHasPermission($user, 'manage:penugasan');
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
