<?php

namespace App\Policies;

use App\Models\User;
use App\Models\pengajuan;
use App\Services\PermissionService;

class PengajuanPolicy
{
    public function viewAny(User $user): bool
    {
        return $user->instansi_id !== null;
    }

    public function view(User $user, pengajuan $pengajuan): bool
    {
        // User can view their own pengajuan
        if ($user->id === $pengajuan->user_id) {
            return true;
        }

        // User can view if they are the approver
        if ($user->id === $pengajuan->disetujui_oleh) {
            return true;
        }

        // User with manage:pengajuan permission in the same tenant can view any pengajuan
        // (needed for managers to review pending requests before approve/reject)
        if (app(PermissionService::class)->userHasPermission($user, 'manage:pengajuan')) {
            // Must be same tenant
            return $user->instansi_id === $pengajuan->user->instansi_id;
        }

        return false;
    }

    public function create(User $user): bool
    {
        return $user->instansi_id !== null;
    }

    public function update(User $user, pengajuan $pengajuan): bool
    {
        // Only the user who created it can update it, and only if it's still pending
        return $user->id === $pengajuan->user_id && $pengajuan->status === 'pending';
    }

    public function delete(User $user, pengajuan $pengajuan): bool
    {
        // Only the user who created it can delete it, and only if it's still pending
        return $user->id === $pengajuan->user_id && $pengajuan->status === 'pending';
    }

    public function approve(User $user, pengajuan $pengajuan): bool
    {
        // Must have manage:pengajuan permission AND be in the same tenant
        if (!app(PermissionService::class)->userHasPermission($user, 'manage:pengajuan')) {
            return false;
        }

        return $user->instansi_id === $pengajuan->user->instansi_id;
    }

    public function reject(User $user, pengajuan $pengajuan): bool
    {
        // Must have manage:pengajuan permission AND be in the same tenant
        if (!app(PermissionService::class)->userHasPermission($user, 'manage:pengajuan')) {
            return false;
        }

        return $user->instansi_id === $pengajuan->user->instansi_id;
    }
}
