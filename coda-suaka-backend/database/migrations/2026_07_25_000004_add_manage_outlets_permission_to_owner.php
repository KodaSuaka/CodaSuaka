<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Support\Facades\DB;

return new class extends Migration
{
    /**
     * Fix: Tambahkan permission `manage:outlets` dan `view:outlets` untuk role Owner.
     *
     * Sebelumnya, permission ini tidak ada di config/roles.php sehingga Owner
     * tidak bisa membuat/mengelola outlet (error 403 Forbidden).
     *
     * Migration ini aman dijalankan berulang kali (upsert).
     */
    public function up(): void
    {
        $ownerRoleId = DB::table('roles')->where('nama_role', 'Owner')->value('id');

        if (!$ownerRoleId) {
            return;
        }

        $permissions = ['view:outlets', 'manage:outlets'];

        foreach ($permissions as $permission) {
            DB::table('role_permissions')->updateOrInsert(
                [
                    'role_id' => $ownerRoleId,
                    'permission' => $permission,
                ],
                [
                    'created_at' => now(),
                    'updated_at' => now(),
                ]
            );
        }
    }

    public function down(): void
    {
        $ownerRoleId = DB::table('roles')->where('nama_role', 'Owner')->value('id');

        if (!$ownerRoleId) {
            return;
        }

        DB::table('role_permissions')
            ->where('role_id', $ownerRoleId)
            ->whereIn('permission', ['view:outlets', 'manage:outlets'])
            ->delete();
    }
};
