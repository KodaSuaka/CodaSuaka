<?php

namespace Database\Seeders;

use Illuminate\Database\Seeder;
use App\Models\role;
use App\Models\role_permission;

class RolePermissionSeeder extends Seeder
{
    /**
     * Run the database seeds.
     */
    public function run(): void
    {
        $permissionMap = [
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
                'manage:paket',
            ],
            'Keuangan' => [
                'view:keuangan',
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
