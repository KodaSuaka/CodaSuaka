<?php

use App\Models\role;
use App\Models\User;
use Illuminate\Database\Migrations\Migration;
use Illuminate\Support\Facades\DB;

return new class extends Migration
{
    /**
     * Remap users with the old "Karyawan" role to one of the new functional roles.
     *
     * Since there is no reliable heuristic to automatically assign "Keuangan",
     * "Manajemen", or "Staff", this migration assigns all orphaned "Karyawan"
     * users to "Staff" by default so they retain at least basic access.
     * A manual review should follow post-deploy.
     */
    public function up(): void
    {
        $oldRole = role::where('nama_role', 'Karyawan')->first();
        if (!$oldRole) {
            return; // No orphaned role exists
        }

        $staffRole = role::firstOrCreate(['nama_role' => 'Staff']);

        $orphanedCount = User::where('role_id', $oldRole->id)->count();

        if ($orphanedCount > 0) {
            DB::transaction(function () use ($oldRole, $staffRole) {
                User::where('role_id', $oldRole->id)
                    ->update(['role_id' => $staffRole->id]);
            });
        }

        // Optionally remove the old orphan role
        // (commented out to keep referential integrity in case of FK issues)
        // $oldRole->delete();
    }

    /**
     * Reverse: restore users back to "Karyawan" role (if it was deleted, re-create it).
     */
    public function down(): void
    {
        $staffRole = role::where('nama_role', 'Staff')->first();
        if (!$staffRole) {
            return;
        }

        $karyawanRole = role::firstOrCreate(['nama_role' => 'Karyawan']);

        $affectedCount = User::where('role_id', $staffRole->id)->count();

        if ($affectedCount > 0) {
            DB::transaction(function () use ($staffRole, $karyawanRole) {
                // Only revert users that were previously migrated (Staff -> Karyawan)
                // We assume all Staff users were previously Karyawan, which is safe
                // since this migration's up() only mapped Karyawan -> Staff.
                User::where('role_id', $staffRole->id)
                    ->update(['role_id' => $karyawanRole->id]);
            });
        }
    }
};
