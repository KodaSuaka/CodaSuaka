<?php

namespace Database\Seeders;

use Illuminate\Database\Seeder;
use App\Models\role;

class RoleSeeder extends Seeder
{
    public function run(): void
    {
        // Platform-level roles
        role::firstOrCreate(['nama_role' => 'Super Admin']);
        role::firstOrCreate(['nama_role' => 'Owner']);

        // Functional roles (replaces flat "Karyawan")
        role::firstOrCreate(['nama_role' => 'Keuangan']);
        role::firstOrCreate(['nama_role' => 'Manager']);
        role::firstOrCreate(['nama_role' => 'Staff']);

        // Cleanup: hapus role 'Manajemen' lama jika masih ada
        role::where('nama_role', 'Manajemen')->delete();
    }
}
