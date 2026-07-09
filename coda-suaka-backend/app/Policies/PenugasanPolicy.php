<?php

namespace App\Policies;

use App\Models\User;
use App\Models\penugasan;
use App\Services\PermissionService;

class PenugasanPolicy
{
    public function viewAny(User $user): bool
    {
        return $user->instansi_id !== null;
    }

    public function view(User $user, penugasan $penugasan): bool
    {
        return $user->instansi_id === $penugasan->divisi?->outlet?->instansi_id;
    }

    public function create(User $user): bool
    {
        return app(PermissionService::class)->userHasPermission($user, 'manage:penugasan');
    }

    public function update(User $user, penugasan $penugasan): bool
    {
        if ($user->instansi_id !== $penugasan->divisi?->outlet?->instansi_id) {
            return false;
        }
        return app(PermissionService::class)->userHasPermission($user, 'manage:penugasan');
    }

    public function delete(User $user, penugasan $penugasan): bool
    {
        if ($user->instansi_id !== $penugasan->divisi?->outlet?->instansi_id) {
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
