<?php

namespace App\Policies;

use App\Models\penugasan;
use App\Models\User;
use App\Services\PermissionService;

class PenugasanPolicy
{
    /**
     * Check if a penugasan belongs to the user's tenant.
     * Falls back to created_by when divisi_id is null.
     */
    private function isSameTenant(User $user, penugasan $penugasan): bool
    {
        // Template: global (instansi_id=null) bisa diakses semua, instansi-specific hanya instansi yang sama
        if ($penugasan->is_template) {
            return is_null($penugasan->instansi_id) || $user->instansi_id === $penugasan->instansi_id;
        }

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

        // Hanya Owner/Manager yang boleh edit field tugas (judul, deskripsi, dll).
        // Karyawan yang ditugasi HANYA boleh pakai endpoint accept/complete.
        return app(PermissionService::class)->userHasPermission($user, 'manage:penugasan');
    }

    public function delete(User $user, penugasan $penugasan): bool
    {
        if (! $this->isSameTenant($user, $penugasan)) {
            return false;
        }

        return app(PermissionService::class)->userHasPermission($user, 'manage:penugasan');
    }

    /**
     * Allow karyawan yang ditugasi untuk accept/complete tugas.
     */
    public function accept(User $user, penugasan $penugasan): bool
    {
        if (! $this->isSameTenant($user, $penugasan)) {
            return false;
        }

        // User dengan manage:penugasan (Owner/Manager) bisa accept semua tugas di instansinya
        if (app(PermissionService::class)->userHasPermission($user, 'manage:penugasan')) {
            return true;
        }

        $karyawan = $user->profilKaryawan;

        if (! $karyawan) {
            return false;
        }

        // Template (penanggung_jawab_id = null): bisa diterima semua karyawan
        if ($penugasan->is_template) {
            return true;
        }

        // Tugas biasa: hanya bisa diterima oleh yang ditugaskan
        return $penugasan->penanggung_jawab_id === $karyawan->id;
    }

    public function complete(User $user, penugasan $penugasan): bool
    {
        return $this->accept($user, $penugasan);
    }

    /**
     * Hanya Owner/Manager (manage:penugasan) yang boleh memvalidasi tugas
     * yang menunggu_validasi — karyawan tidak boleh memvalidasi tugasnya sendiri.
     */
    public function validasi(User $user, penugasan $penugasan): bool
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
