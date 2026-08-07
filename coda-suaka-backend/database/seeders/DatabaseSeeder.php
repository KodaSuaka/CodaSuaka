<?php

namespace Database\Seeders;

use Illuminate\Database\Console\Seeds\WithoutModelEvents;
use Illuminate\Database\Seeder;

class DatabaseSeeder extends Seeder
{
    use WithoutModelEvents;

    /**
     * Seed the application's database.
     */
    public function run(): void
    {
        // 1. Base seeders (roles, permissions, kategori)
        $this->call([
            RoleSeeder::class,
            RolePermissionSeeder::class,
            PaketSeeder::class,
            KategoriTransaksiSeeder::class,
            TemplatePenugasanSeeder::class,
        ]);

        // 2. Dummy data lengkap untuk presentasi
        $this->call([
            DummyDataSeeder::class,
        ]);

        // 3. Data akun pemilik UMKM (variasi paket standart & pro) untuk kebutuhan laporan
        $this->call([
            UmkmOwnerSeeder::class,
        ]);
    }
}
