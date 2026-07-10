<?php

namespace App\Services;

use App\Models\User;
use Illuminate\Support\Collection;

class PermissionService
{
    /**
     * Check if a user has a specific permission.
     * Single source of truth: database role_permissions table.
     */
    public function userHasPermission(User $user, string $permission): bool
    {
        return $user->role?->permissions()
            ->where('permission', $permission)
            ->exists() ?? false;
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
     * Get all permissions for a user from the database.
     */
    public function getUserPermissions(User $user): Collection
    {
        return $user->role?->permissions->pluck('permission') ?? collect();
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