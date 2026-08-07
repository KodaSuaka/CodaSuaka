<?php

namespace App\Policies;

use App\Models\Stok;
use App\Models\User;
use App\Services\PermissionService;

class StokPolicy
{
    /**
     * Lihat daftar stok (view:stok).
     */
    public function viewAny(User $user): bool
    {
        return app(PermissionService::class)->userHasPermission($user, 'view:stok')
            || app(PermissionService::class)->userHasPermission($user, 'manage:stok');
    }

    /**
     * Lihat detail stok (view:stok).
     */
    public function view(User $user, Stok $stok): bool
    {
        if (! $this->viewAny($user)) {
            return false;
        }

        return $stok->instansi_id === $user->instansi_id;
    }

    /**
     * Buat stok baru (manage:stok).
     */
    public function create(User $user): bool
    {
        return app(PermissionService::class)->userHasPermission($user, 'manage:stok');
    }

    /**
     * Update stok (manage:stok).
     */
    public function update(User $user, Stok $stok): bool
    {
        if (! $this->create($user)) {
            return false;
        }

        return $stok->instansi_id === $user->instansi_id;
    }

    /**
     * Hapus stok (manage:stok).
     */
    public function delete(User $user, Stok $stok): bool
    {
        if (! $this->create($user)) {
            return false;
        }

        return $stok->instansi_id === $user->instansi_id;
    }

    /**
     * Mutasi stok (manage:stok).
     */
    public function mutate(User $user, Stok $stok): bool
    {
        if (! app(PermissionService::class)->userHasPermission($user, 'manage:stok')) {
            return false;
        }

        return $stok->instansi_id === $user->instansi_id;
    }

    /**
     * Lihat riwayat mutasi stok (view:stok).
     */
    public function viewMutations(User $user, Stok $stok): bool
    {
        return $this->view($user, $stok);
    }
}
