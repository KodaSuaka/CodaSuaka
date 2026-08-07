<?php

namespace Database\Seeders;

use App\Models\paket;
use Illuminate\Database\Seeder;

class PaketSeeder extends Seeder
{
    /**
     * Seed paket langganan dengan harga FIX (per ketentuan user):
     *   - Standart: 49.000 / bulan, maks 5 karyawan (pemilik tidak dihitung), 1 outlet
     *   - Pro:      159.000 / bulan, maks 10 karyawan (pemilik tidak dihitung), 3 outlet
     *
     * idempotent: update harga & kuota jika nama_paket sudah ada, tanpa duplikat.
     */
    public function run(): void
    {
        $pakets = [
            [
                'nama_paket' => 'Standart',
                'harga' => 49000,
                'deskripsi' => 'Paket standar untuk UMKM kecil. Maksimal 5 karyawan (pemilik tidak dihitung).',
                'fitur' => json_encode(['Dashboard', 'Presensi', 'Penugasan Dasar', 'Laporan Harian']),
                'durasi_hari' => 30,
                'max_outlet' => 1,
                'max_karyawan_per_outlet' => 5,
                'is_active' => true,
            ],
            [
                'nama_paket' => 'Pro',
                'harga' => 159000,
                'deskripsi' => 'Paket lengkap untuk UMKM menengah-besar. Maksimal 10 karyawan (pemilik tidak dihitung) + multi-outlet.',
                'fitur' => json_encode([
                    'Dashboard', 'Presensi', 'Penugasan Lengkap', 'Laporan Keuangan',
                    'Chat Internal', 'Multi-Outlet', 'Export PDF/Excel', 'Approval Workflow',
                ]),
                'durasi_hari' => 30,
                'max_outlet' => 3,
                'max_karyawan_per_outlet' => 10,
                'is_active' => true,
            ],
        ];

        foreach ($pakets as $data) {
            paket::updateOrCreate(
                ['nama_paket' => $data['nama_paket']],
                $data
            );
        }
    }
}
