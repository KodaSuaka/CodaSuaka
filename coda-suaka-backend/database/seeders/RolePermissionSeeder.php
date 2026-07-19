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
            $role = role::where('nama_role', $roleName)->first();
            if (!$role) {
                continue;
            }

            foreach ($permissions as $permission) {
                role_permission::firstOrCreate([
                    'role_id' => $role->id,
                    'permission' => $permission,
                ]);
            }
        }
    }
}
