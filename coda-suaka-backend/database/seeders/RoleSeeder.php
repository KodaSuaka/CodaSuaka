<?php

namespace Database\Seeders;

use Illuminate\Database\Seeder;
use App\Models\role;

class RoleSeeder extends Seeder
{
    public function run(): void
    {
        role::create(['nama_role' => 'Pemilik']);
    }
}
