<?php

namespace App\Services;

use App\Models\User;
use Illuminate\Support\Collection;

class PermissionService
{
    /**
     * Cache per request untuk hasil pengecekan permission.
     * Key: "user_{id}:permission_{name}" => bool
     */
    private array $permissionCache = [];

    /**
     * Cache permissionset per user untuk menghindari multiple query.
     * Key: "user_{id}" => Collection of permission strings
     */
    private array $userPermissionsCache = [];

    /**
     * Check if a user has a specific permission.
     * Single source of truth: database role_permissions table.
     * Hasil dicache per request untuk menghindari query berulang.
     */
    public function userHasPermission(User $user, string $permission): bool
    {
        $cacheKey = 'user_' . $user->id . ':permission_' . $permission;

        if (array_key_exists($cacheKey, $this->permissionCache)) {
            return $this->permissionCache[$cacheKey];
        }

        if ($user->role === null) {
            $this->permissionCache[$cacheKey] = false;
            return false;
        }

        // Gunakan relationship yang sudah di-load jika ada, hindari query
        if ($user->relationLoaded('role') && $user->role->relationLoaded('permissions')) {
            $result = $user->role->permissions->contains('permission', $permission);
        } else {
            $result = $user->role->permissions()
                ->where('permission', $permission)
                ->exists();
        }

        $this->permissionCache[$cacheKey] = $result;
        return $result;
    }

    /**
     * Check if a user has any of the given permissions.
     */
    public function userHasAnyPermission(User $user, array $permissions): bool
    {
        foreach ($permissions as $permission) {
            if ($this->userHasPermission($user, $permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get semua permissions untuk user dari database.
     * Hasil dicache per request.
     */
    public function getUserPermissions(User $user): Collection
    {
        $cacheKey = 'user_' . $user->id;

        if (array_key_exists($cacheKey, $this->userPermissionsCache)) {
            return $this->userPermissionsCache[$cacheKey];
        }

        if ($user->role === null) {
            $this->userPermissionsCache[$cacheKey] = collect();
            return collect();
        }

        $permissions = $user->role->permissions->pluck('permission');
        $this->userPermissionsCache[$cacheKey] = $permissions;
        return $permissions;
    }

    /**
     * Get menu items for Karyawan dashboard based on role.
     * Returns only menu items that the user is allowed to see.
     */
    public function getKaryawanDashboardMenu(User $user): array
    {
        $permissions = $this->getUserPermissions($user)->toArray();

        $allMenuItems = [
            [
                'id' => 'kelola_karyawan',
                'label' => 'Kelola Karyawan',
                'icon' => 'PeopleAlt',
                'route' => 'kelola_karyawan',
                'permission' => 'manage:karyawan',
            ],
            [
                'id' => 'laporan_keuangan',
                'label' => 'Laporan Keuangan',
                'icon' => 'AccountBalance',
                'route' => 'laporan_keuangan',
                'permission' => 'view:keuangan',
            ],
            [
                'id' => 'approval_keuangan',
                'label' => 'Approval Keuangan',
                'icon' => 'HowToReg',
                'route' => 'approval_keuangan',
                'permission' => 'approve:keuangan',
            ],
            [
                'id' => 'riwayat_absensi',
                'label' => 'Riwayat Absensi',
                'icon' => 'FactCheck',
                'route' => 'riwayat_kehadiran',
                'permission' => null, // All logged-in users can see this
            ],
            [
                'id' => 'tugas_tim',
                'label' => 'Tugas Tim',
                'icon' => 'Assignment',
                'route' => 'tugas_tim',
                'permission' => 'manage:penugasan',
            ],
            [
                'id' => 'pengajuan',
                'label' => 'Pengajuan',
                'icon' => 'Description',
                'route' => 'pengajuan',
                'permission' => 'manage:pengajuan',
            ],
        ];

        $menuItems = array_filter($allMenuItems, function ($item) use ($permissions) {
            if ($item['permission'] === null) {
                return true;
            }
            return in_array($item['permission'], $permissions, true);
        });

        return array_values($menuItems);
    }

    /**
     * Get additional content items for Karyawan dashboard.
     */
    public function getKaryawanAdditionalContent(User $user): array
    {
        return [
            [
                'id' => 'pelatihan',
                'label' => 'Pelatihan',
                'icon' => 'School',
                'route' => 'pelatihan',
            ],
            [
                'id' => 'penghargaan',
                'label' => 'Penghargaan',
                'icon' => 'EmojiEvents',
                'route' => 'penghargaan',
            ],
        ];
    }
}