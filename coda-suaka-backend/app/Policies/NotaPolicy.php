<?php

namespace App\Policies;

use App\Models\Nota;
use App\Models\User;
use App\Services\PermissionService;

class NotaPolicy
{
    /**
     * User bisa melihat daftar nota milik instansinya.
     */
    public function viewAny(User $user): bool
    {
        return app(PermissionService::class)->userHasPermission($user, 'view:kasir');
    }

    /**
     * User hanya bisa melihat nota milik instansinya sendiri.
     */
    public function view(User $user, Nota $nota): bool
    {
        if ($user->instansi_id !== $nota->instansi_id) {
            return false;
        }

        return app(PermissionService::class)->userHasPermission($user, 'view:kasir');
    }

    /**
     * User bisa membuat nota untuk instansinya.
     */
    public function create(User $user): bool
    {
        return app(PermissionService::class)->userHasPermission($user, 'manage:kasir');
    }

    /**
     * User hanya bisa menghapus nota milik instansinya sendiri.
     */
    public function delete(User $user, Nota $nota): bool
    {
        if ($user->instansi_id !== $nota->instansi_id) {
            return false;
        }

        return app(PermissionService::class)->userHasPermission($user, 'delete:kasir');
    }

    /**
     * User bisa mengekspor/mencetak nota miliknya sendiri ke PDF.
     */
    public function export(User $user, Nota $nota): bool
    {
        if ($user->instansi_id !== $nota->instansi_id) {
            return false;
        }

        return app(PermissionService::class)->userHasPermission($user, 'export:kasir');
    }

    public function restore(User $user, Nota $nota): bool
    {
        return false;
    }

    public function forceDelete(User $user, Nota $nota): bool
    {
        return false;
    }
}
