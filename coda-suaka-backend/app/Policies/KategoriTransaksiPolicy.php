<?php

namespace App\Policies;

use App\Models\User;
use App\Models\KategoriTransaksi;
use App\Services\PermissionService;

class KategoriTransaksiPolicy
{
    /**
     * User bisa melihat daftar kategori (global + custom miliknya).
     */
    public function viewAny(User $user): bool
    {
        return app(PermissionService::class)->userHasPermission($user, 'view:keuangan');
    }

    /**
     * User bisa melihat kategori global (instansi_id = null)
     * atau kategori milik instansinya sendiri.
     */
    public function view(User $user, KategoriTransaksi $kategoriTransaksi): bool
    {
        // Kategori global bisa dilihat semua user
        if ($kategoriTransaksi->isGlobal()) {
            return app(PermissionService::class)->userHasPermission($user, 'view:keuangan');
        }

        // Kategori custom hanya milik instansi yang sama
        if ($user->instansi_id !== $kategoriTransaksi->instansi_id) {
            return false;
        }

        return app(PermissionService::class)->userHasPermission($user, 'view:keuangan');
    }

    /**
     * User bisa membuat kategori custom untuk instansinya.
     */
    public function create(User $user): bool
    {
        return app(PermissionService::class)->userHasPermission($user, 'manage:keuangan');
    }

    /**
     * User hanya bisa mengedit kategori custom milik instansinya sendiri.
     * Kategori global tidak bisa diedit oleh user biasa.
     */
    public function update(User $user, KategoriTransaksi $kategoriTransaksi): bool
    {
        // Global template check dilakukan di Controller (return 422 + pesan jelas),
        // policy cukup memastikan user punya akses manage:keuangan.
        if ($kategoriTransaksi->isGlobal()) {
            return app(PermissionService::class)->userHasPermission($user, 'manage:keuangan');
        }

        if ($user->instansi_id !== $kategoriTransaksi->instansi_id) {
            return false;
        }

        return app(PermissionService::class)->userHasPermission($user, 'manage:keuangan');
    }

    /**
     * User hanya bisa menghapus kategori custom milik instansinya sendiri.
     */
    public function delete(User $user, KategoriTransaksi $kategoriTransaksi): bool
    {
        // Global template check dilakukan di Controller (return 422 + pesan jelas).
        if ($kategoriTransaksi->isGlobal()) {
            return app(PermissionService::class)->userHasPermission($user, 'manage:keuangan');
        }

        if ($user->instansi_id !== $kategoriTransaksi->instansi_id) {
            return false;
        }

        return app(PermissionService::class)->userHasPermission($user, 'manage:keuangan');
    }

    public function restore(User $user, KategoriTransaksi $kategoriTransaksi): bool
    {
        return false;
    }

    public function forceDelete(User $user, KategoriTransaksi $kategoriTransaksi): bool
    {
        return false;
    }
}
