<?php

namespace App\Policies;

use App\Models\BarangJasa;
use App\Models\User;
use App\Services\PermissionService;

class BarangJasaPolicy
{
    /**
     * User bisa melihat daftar barang/jasa milik instansinya.
     */
    public function viewAny(User $user): bool
    {
        return app(PermissionService::class)->userHasPermission($user, 'view:kasir');
    }

    /**
     * User hanya bisa melihat barang/jasa milik instansinya sendiri.
     */
    public function view(User $user, BarangJasa $barangJasa): bool
    {
        if ($user->instansi_id !== $barangJasa->instansi_id) {
            return false;
        }

        return app(PermissionService::class)->userHasPermission($user, 'view:kasir');
    }

    /**
     * User bisa membuat barang/jasa untuk instansinya.
     */
    public function create(User $user): bool
    {
        return app(PermissionService::class)->userHasPermission($user, 'manage:kasir');
    }

    /**
     * User hanya bisa mengedit barang/jasa milik instansinya sendiri.
     */
    public function update(User $user, BarangJasa $barangJasa): bool
    {
        if ($user->instansi_id !== $barangJasa->instansi_id) {
            return false;
        }

        return app(PermissionService::class)->userHasPermission($user, 'manage:kasir');
    }

    /**
     * User hanya bisa menghapus barang/jasa milik instansinya sendiri.
     */
    public function delete(User $user, BarangJasa $barangJasa): bool
    {
        if ($user->instansi_id !== $barangJasa->instansi_id) {
            return false;
        }

        return app(PermissionService::class)->userHasPermission($user, 'delete:kasir');
    }

    public function restore(User $user, BarangJasa $barangJasa): bool
    {
        return false;
    }

    public function forceDelete(User $user, BarangJasa $barangJasa): bool
    {
        return false;
    }
}
