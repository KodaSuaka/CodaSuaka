<?php

namespace Database\Seeders;

use Illuminate\Database\Seeder;
use App\Models\role;
use App\Models\role_permission;

class RolePermissionSeeder extends Seeder
{
    /**
     * Run the database seeds.
     *
     * Membaca dari config/permissions.php sebagai single source of truth.
     *
     * CATATAN: Untuk sinkronisasi yang lebih robust, gunakan:
     *   php artisan permission:sync
     *
     * Seeder ini hanya dipakai saat initial setup / fresh migrate.
     * Untuk update permission di production, gunakan permission:sync.
     */
    public function run(): void
    {
        $config = config('permissions');

        if (empty($config['roles'])) {
            $this->command->warn('⚠️  config/permissions.php kosong. Tidak ada permission yang di-seed.');
            return;
        }

        $registry = $config['registry'] ?? [];
        $totalAdded = 0;
        $totalRemoved = 0;

        foreach ($config['roles'] as $roleName => $permissions) {
            // Buat role jika belum ada
            $role = role::firstOrCreate(['nama_role' => $roleName]);

            // Filter: hanya permission yang valid di registry
            $validPermissions = array_filter(
                $permissions,
                fn($p) => $this->isPermissionInRegistry($p, $registry)
            );

            // 1. Tambahkan semua permission yang ada di config
            foreach ($validPermissions as $permission) {
                role_permission::updateOrInsert(
                    [
                        'role_id'    => $role->id,
                        'permission' => $permission,
                    ],
                    [
                        'created_at' => now(),
                        'updated_at' => now(),
                    ]
                );
                $totalAdded++;
            }

            // 2. Hapus permission yang TIDAK ada di config
            //    (termasuk permission lama yang sudah dikomentari/dihapus)
            $removed = role_permission::where('role_id', $role->id)
                ->whereNotIn('permission', array_values($validPermissions))
                ->delete();
            $totalRemoved += $removed;
        }

        $this->command->info("✅ Permission seeding selesai:");
        $this->command->info("   ➕ {$totalAdded} permission ditambahkan/diupdate");
        $this->command->info("   ➖ {$totalRemoved} permission dihapus (stale)");
    }

    /**
     * Cek apakah permission ID ada di registry.
     */
    private function isPermissionInRegistry(string $permission, array $registry): bool
    {
        foreach ($registry as $module) {
            if (isset($module['permissions'][$permission])) {
                return true;
            }
        }
        return false;
    }
}
