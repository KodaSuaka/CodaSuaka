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
     * Mengambil mapping role → permission dari config/roles.php
     * sehingga bisa diubah tanpa mengubah seeder.
     */
    public function run(): void
    {
        $permissionMap = config('roles.permissions', []);

        foreach ($permissionMap as $roleName => $permissions) {
            // Buat role jika belum ada, atau ambil yang sudah ada
            $role = role::firstOrCreate(['nama_role' => $roleName]);

            // 1. Tambah/update permission yang ada di config
            foreach ($permissions as $permission) {
                role_permission::updateOrInsert(
                    [
                        'role_id' => $role->id,
                        'permission' => $permission,
                    ],
                    [
                        'created_at' => now(),
                        'updated_at' => now(),
                    ]
                );
            }

            // 2. Hapus permission yang TIDAK ada di config lagi
            //    (misalnya permission yang dikomentari/dihapus)
            role_permission::where('role_id', $role->id)
                ->whereNotIn('permission', $permissions)
                ->delete();
        }
    }
}
