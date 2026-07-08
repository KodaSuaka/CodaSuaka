<?php

namespace App\Policies;

use App\Models\User;
use App\Models\pengajuan;
use App\Services\PermissionService;

class PengajuanPolicy
{
    public function viewAny(User $user): bool
    {
        return $user->role?->nama_role === 'Super Admin' || $user->instansi_id !== null;
    }

    public function view(User $user, pengajuan $pengajuan): bool
    {
        // Super Admin can view all
        if ($user->role?->nama_role === 'Super Admin') {
            return true;
        }
        // User can view their own pengajuan or if they are the approver
        return $user->id === $pengajuan->user_id || $user->id === $pengajuan->penyetuju_id;
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
        // Approval logic is handled in PengajuanController@approve for complex checks
        // but we define the base permission here.
        return app(PermissionService::class)->userHasPermission($user, 'manage:pengajuan');
    }

    public function reject(User $user, pengajuan $pengajuan): bool
    {
        return app(PermissionService::class)->userHasPermission($user, 'manage:pengajuan');
    }
}
