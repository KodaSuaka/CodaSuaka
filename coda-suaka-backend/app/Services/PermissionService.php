<?php

namespace App\Services;

use App\Models\User;
use Illuminate\Support\Collection;

class PermissionService
{
    /**
     * All recognized permission keys in the system.
     */
    public const PERMISSIONS = [
        'manage:outlets',
        'manage:karyawan',
        'manage:divisi',
        'manage:jadwal',
        'manage:penugasan',
        'view:keuangan',
        'manage:pengajuan',
        'view:presensi',
        'manage:paket',
        'manage:instansi',
        'manage:owners',
        'manage:role_permissions',
        'manage:attendance',
    ];

    /**
     * Hard-coded permission map for functional roles.
     * These are also seeded in the database, but this serves
     * as a fallback and quick reference.
     */
    private const ROLE_PERMISSION_MAP = [
        'Owner' => [
            'manage:outlets',
            'manage:karyawan',
            'manage:divisi',
            'manage:jadwal',
            'manage:penugasan',
            'view:keuangan',
            'manage:pengajuan',
            'view:presensi',
            'manage:role_permissions',
        ],
        'Keuangan' => [
            'view:keuangan',
            'manage:penugasan',
            'view:presensi',
        ],
        'Manajemen' => [
            'manage:outlets',
            'manage:karyawan',
            'manage:divisi',
            'manage:jadwal',
            'manage:penugasan',
            'manage:pengajuan',
            'view:presensi',
        ],
        'Staff' => [
            // No special permissions — task-based only
        ],
    ];

    /**
     * Check if a user has a specific permission.
     */
    public function userHasPermission(User $user, string $permission): bool
    {
        $roleName = $user->role?->nama_role;

        // Super Admin has all permissions
        if ($roleName === 'Super Admin') {
            return true;
        }

        // Check from database role_permissions first
        $hasPermission = $user->role?->permissions()
            ->where('permission', $permission)
            ->exists();

        if ($hasPermission) {
            return true;
        }

        // Fallback: check hard-coded map
        if (isset(self::ROLE_PERMISSION_MAP[$roleName])) {
            return in_array($permission, self::ROLE_PERMISSION_MAP[$roleName], true);
        }

        return false;
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
     * Get all permissions for a user.
     * Merges database role_permissions with hard-coded defaults.
     */
    public function getUserPermissions(User $user): Collection
    {
        $roleName = $user->role?->nama_role;

        // Super Admin — all permissions
        if ($roleName === 'Super Admin') {
            return collect(self::PERMISSIONS);
        }

        // Get from database role_permissions
        $dbPermissions = $user->role?->permissions->pluck('permission') ?? collect();

        // Merge with hard-coded map as fallback
        $defaultPermissions = collect(self::ROLE_PERMISSION_MAP[$roleName] ?? []);

        return $dbPermissions->merge($defaultPermissions)->unique()->values();
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