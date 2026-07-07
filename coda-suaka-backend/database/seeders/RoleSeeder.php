<?php

namespace Database\Seeders;

use Illuminate\Database\Seeder;
use App\Models\role;

class RoleSeeder extends Seeder
{
    public function run(): void
    {
        role::firstOrCreate(['nama_role' => 'Owner']);
        role::firstOrCreate(['nama_role' => 'Karyawan']);
        role::firstOrCreate(['nama_role' => 'Super Admin']);
    }
}
