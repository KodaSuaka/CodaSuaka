<?php

namespace Database\Seeders;

use App\Models\AnggotaDivisi;
use App\Models\attandence;
use App\Models\Chat;
use App\Models\Divisi;
use App\Models\Instansi;
use App\Models\jadwal;
use App\Models\karyawan;
use App\Models\KategoriTransaksi;
use App\Models\outlet;
use App\Models\paket;
use App\Models\pengajuan;
use App\Models\penugasan;
use App\Models\role;
use App\Models\role_permission;
use App\Models\transaksi_paket;
use App\Models\TransaksiKas;
use App\Models\User;
use Carbon\Carbon;
use Illuminate\Database\Console\Seeds\WithoutModelEvents;
use Illuminate\Database\Seeder;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Str;

/**
 * Seeder data dummy lengkap untuk presentasi CodaSuaka.
 *
 * Membuat 2 instansi (bisnis) dengan data lengkap:
 * - Paket langganan
 * - Outlet, Divisi, Karyawan
 * - Presensi harian (30 hari terakhir)
 * - Penugasan aktif dan selesai
 * - Jadwal kalender
 * - Pengajuan cuti/sakit
 * - Transaksi keuangan (masuk & keluar)
 * - Chat antar karyawan
 * - Notifikasi
 *
 * Password semua user: "password"
 */
class DummyDataSeeder extends Seeder
{
    use WithoutModelEvents;

    private string $defaultPassword;

    // Nama-nama realistis untuk karyawan Indonesia
    private array $namaKaryawan = [
        'Ahmad Rizky Pratama',
        'Siti Nurhaliza',
        'Budi Santoso',
        'Dewi Kartika Sari',
        'Fajar Nugroho',
        'Gita Puspita Sari',
        'Hendra Wijaya',
        'Indah Permata Sari',
        'Joko Widodo',
        'Kartini Dewi',
        'Lukman Hakim',
        'Maya Anggraeni',
        'Nugroho Setiawan',
        'Ophelia Tan',
        'Putri Maharani',
        'Rudi Hartono',
        'Sari Dewi Lestari',
        'Tono Sugiarto',
        'Ulya Nadhira',
        'Vina Melati',
    ];

    public function run(): void
    {
        $this->defaultPassword = Hash::make('password');

        // ─── 1. Buat Paket Langganan ─────────────────────────────
        $this->command?->info('📦 Membuat paket langganan...');
        $paketStandart = $this->createPaketStandart();
        $paketPro = $this->createPaketPro();

        // ─── 2. Buat Instansi (2 bisnis) ─────────────────────────
        $this->command?->info('🏢 Membuat instansi/bisnis...');
        $instansi1 = $this->createInstansi('Toko Berkah Mart', $paketPro->id, 'Asia/Jakarta');
        $instansi2 = $this->createInstansi('Kopi Nusantara', $paketStandart->id, 'Asia/Makassar');

        // Batas karyawan per paket. Pemilik (Owner) TIDAK dihitung sebagai karyawan.
        // Yang dihitung: Manager, Keuangan, Staff, Karyawan (konsisten dgn KaryawanController::store)
        $maxKaryawanInstansi1 = $paketPro->max_karyawan_per_outlet;       // Pro: 10
        $maxKaryawanInstansi2 = $paketStandart->max_karyawan_per_outlet;  // Standart: 5

        // ─── 3. Buat Transaksi Paket (langganan aktif) ───────────
        $this->command?->info('💳 Membuat transaksi paket...');
        $this->createTransaksiPaket($instansi1, $paketPro);
        $this->createTransaksiPaket($instansi2, $paketStandart);

        // ─── 4. Buat Roles & Permissions ─────────────────────────
        $this->command?->info('🔐 Memastikan roles & permissions...');
        $roles = $this->ensureRoles();
        $this->ensureRolePermissions();

        // ─── 5. Buat Users & Karyawan Instansi 1 (Pro) ──────────
        $this->command?->info('👤 Membuat user & karyawan Instansi 1 (Pro, max '.$maxKaryawanInstansi1.' karyawan)...');
        $users1 = $this->createUsersForInstansi($instansi1, $roles, 1, $maxKaryawanInstansi1);

        // ─── 6. Buat Users & Karyawan Instansi 2 (Basic) ────────
        $this->command?->info('👤 Membuat user & karyawan Instansi 2 (Basic, max '.$maxKaryawanInstansi2.' karyawan)...');
        $users2 = $this->createUsersForInstansi($instansi2, $roles, 2, $maxKaryawanInstansi2);

        // ─── 7. Buat Outlet ──────────────────────────────────────
        $this->command?->info('🏪 Membuat outlet...');
        $outlets1 = $this->createOutletsForInstansi($instansi1);
        $outlets2 = $this->createOutletsForInstansi($instansi2);

        // ─── 8. Assign Outlet ke User ────────────────────────────
        $this->command?->info('🔗 Meng-assign outlet ke user...');
        $this->assignOutletsToUsers($users1, $outlets1);
        $this->assignOutletsToUsers($users2, $outlets2);

        // ─── 9. Buat Divisi ──────────────────────────────────────
        $this->command?->info('📂 Membuat divisi...');
        $divisis1 = $this->createDivisisForOutlets($outlets1, $users1);
        $divisis2 = $this->createDivisisForOutlets($outlets2, $users2);

        // ─── 10. Assign Karyawan ke Divisi ───────────────────────
        $this->command?->info('👥 Meng-assign karyawan ke divisi...');
        $this->assignKaryawanToDivisi($users1, $divisis1);
        $this->assignKaryawanToDivisi($users2, $divisis2);

        // ─── 11. Buat Kategori Transaksi Global ───────────────────
        $this->command?->info('📊 Memastikan kategori transaksi...');
        $kategoris = $this->ensureKategoriTransaksi();

        // ─── 12. Buat Transaksi Keuangan ──────────────────────────
        $this->command?->info('💰 Membuat transaksi keuangan...');
        $this->createTransaksiKeuangan($instansi1, $outlets1, $kategoris, $users1);
        $this->createTransaksiKeuangan($instansi2, $outlets2, $kategoris, $users2);

        // ─── 13. Buat Presensi ───────────────────────────────────
        $this->command?->info('📋 Membuat data presensi...');
        $this->createPresensi($users1);
        $this->createPresensi($users2);

        // ─── 14. Buat Penugasan ──────────────────────────────────
        $this->command?->info('📝 Membuat data penugasan...');
        $this->createPenugasans($users1, $divisis1, $instansi1);
        $this->createPenugasans($users2, $divisis2, $instansi2);

        // ─── 15. Buat Jadwal ─────────────────────────────────────
        $this->command?->info('📅 Membuat data jadwal...');
        $this->createJadwals($outlets1, $users1);
        $this->createJadwals($outlets2, $users2);

        // ─── 16. Buat Pengajuan ──────────────────────────────────
        $this->command?->info('📄 Membuat data pengajuan...');
        $this->createPengajuans($users1);
        $this->createPengajuans($users2);

        // ─── 17. Buat Chat ───────────────────────────────────────
        $this->command?->info('💬 Membuat data chat...');
        $this->createChats($users1);
        $this->createChats($users2);

        // ─── 18. Buat Template Penugasan ─────────────────────────
        $this->command?->info('📋 Memastikan template penugasan...');
        TemplatePenugasanSeeder::copyGlobalTemplatesToInstansi($instansi1->id, $users1['owner']->id);
        TemplatePenugasanSeeder::copyGlobalTemplatesToInstansi($instansi2->id, $users2['owner']->id);

        $this->command?->info('✅ Semua data dummy berhasil dibuat!');
        $this->command?->info('');
        $this->command?->info('═══════════════════════════════════════════════════════');
        $this->command?->info('  AKUN UNTUK LOGIN (Password: password untuk semua)');
        $this->command?->info('═══════════════════════════════════════════════════════');
        $this->command?->info('');
        $this->command?->info('--- Instansi 1: '.$instansi1->nama_instansi.' (Paket Pro, max '.$maxKaryawanInstansi1.' karyawan) ---');
        $this->command?->info('  Owner    : owner1@berkahmart.com');
        $this->command?->info('  Manager  : manager1@berkahmart.com');
        $this->command?->info('  Keuangan : keuangan1@berkahmart.com');
        $this->command?->info('  Staff    : staff1@berkahmart.com');
        $this->command?->info('  Karyawan : karyawan1_0@berkahmart.com s/d karyawan1_6@berkahmart.com');
        $this->command?->info('');
        $this->command?->info('--- Instansi 2: '.$instansi2->nama_instansi.' (Paket Standart, max '.$maxKaryawanInstansi2.' karyawan) ---');
        $this->command?->info('  Owner    : owner2@kopinusantara.com');
        $this->command?->info('  Manager  : manager2@kopinusantara.com');
        $this->command?->info('  Keuangan : keuangan2@kopinusantara.com');
        $this->command?->info('  Staff    : staff2@kopinusantara.com');
        $this->command?->info('  Karyawan : karyawan2_0@kopinusantara.com s/d karyawan2_1@kopinusantara.com');
        $this->command?->info('');
        $this->command?->info('--- Super Admin (register manual) ---');
        $this->command?->info('  POST /api/auth/register-super-admin');
    }

    // ═══════════════════════════════════════════════════════════
    //  PAKET LANGGANAN
    // ═══════════════════════════════════════════════════════════

    private function createPaketStandart(): paket
    {
        return paket::firstOrCreate(
            ['nama_paket' => 'Standart'],
            [
                'harga' => 49000,
                'deskripsi' => 'Paket standar untuk UMKM kecil. Maksimal 5 karyawan (pemilik tidak dihitung).',
                'fitur' => json_encode(['Dashboard', 'Presensi', 'Penugasan Dasar', 'Laporan Harian']),
                'durasi_hari' => 30,
                'max_outlet' => 1,
                'max_karyawan_per_outlet' => 5,
                'is_active' => true,
            ]
        );
    }

    private function createPaketPro(): paket
    {
        return paket::firstOrCreate(
            ['nama_paket' => 'Pro'],
            [
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
            ]
        );
    }

    // ═══════════════════════════════════════════════════════════
    //  INSTANSI
    // ═══════════════════════════════════════════════════════════

    private function createInstansi(string $nama, int $paketId, string $timezone): Instansi
    {
        return Instansi::firstOrCreate(
            ['nama_instansi' => $nama],
            [
                'paket_id' => $paketId,
                'timezone' => $timezone,
            ]
        );
    }

    private function createTransaksiPaket(Instansi $instansi, paket $paket): void
    {
        transaksi_paket::firstOrCreate(
            [
                'instansi_id' => $instansi->id,
                'paket_id' => $paket->id,
            ],
            [
                'tanggal_mulai' => Carbon::now()->subDays(15),
                'tanggal_berakhir' => Carbon::now()->addDays(15),
                'total_harga' => $paket->harga,
                'status' => 'aktif',
                'bukti_pembayaran' => null,
            ]
        );
    }

    // ═══════════════════════════════════════════════════════════
    //  ROLES & PERMISSIONS
    // ═══════════════════════════════════════════════════════════

    private function ensureRoles(): array
    {
        $roles = [];
        $roleNames = ['Super Admin', 'Owner', 'Keuangan', 'Manager', 'Staff'];

        foreach ($roleNames as $name) {
            $roles[$name] = role::firstOrCreate(
                ['nama_role' => $name],
                ['deskripsi' => "Role {$name}"]
            );
        }

        return $roles;
    }

    private function ensureRolePermissions(): void
    {
        // RolePermissionSeeder sudah dijalankan di DatabaseSeeder
        // Pastikan permission sudah ada
        if (role_permission::count() === 0) {
            $this->call(RolePermissionSeeder::class);
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  USERS & KARYAWAN
    // ═══════════════════════════════════════════════════════════

    private function createUsersForInstansi(Instansi $instansi, array $roles, int $instansiNum, int $maxKaryawan = 50): array
    {
        $suffix = $instansiNum;
        $domain = $instansiNum === 1 ? 'berkahmart.com' : 'kopinusantara.com';
        $users = [];

        // Role mapping: owner, manager, keuangan, staff — ini 4 user wajib
        $roleMapping = [
            'owner' => 'Owner',
            'manager' => 'Manager',
            'keuangan' => 'Keuangan',
            'staff' => 'Staff',
        ];

        // Hitung sisa slot karyawan. Pemilik (Owner) TIDAK dihitung dalam kuota.
        // Yang dihitung: Manager, Keuangan, Staff + karyawan biasa (konsisten dgn KaryawanController::store)
        $karyawanBiasaCount = max(0, $maxKaryawan - (count($roleMapping) - 1));
        $this->command?->info('   → Membuat '.count($roleMapping)." role users + {$karyawanBiasaCount} karyawan biasa (max {$maxKaryawan}, owner tidak dihitung)");

        foreach ($roleMapping as $key => $roleName) {
            $namaParts = explode(' ', $this->namaKaryawan[array_rand($this->namaKaryawan)]);
            $namaDepan = strtolower($namaParts[0]);
            $email = "{$key}{$suffix}@{$domain}";

            $user = User::firstOrCreate(
                ['email' => $email],
                [
                    'name' => $this->generateName($key),
                    'password' => $this->defaultPassword,
                    'role_id' => $roles[$roleName]->id,
                    'instansi_id' => $instansi->id,
                    'email_verified_at' => now(),
                    'remember_token' => Str::random(60),
                ]
            );

            // Buat profil karyawan
            $karyawanNames = $this->getUniqueNames(5, $key);
            $profilKaryawan = karyawan::firstOrCreate(
                ['user_id' => $user->id],
                [
                    'nama_lengkap' => $user->name,
                    'kontak' => '0812'.rand(10000000, 99999999),
                    'alamat' => $this->getRandomAddress(),
                    'foto_profil' => null,
                    'sisa_cuti' => rand(6, 12),
                ]
            );

            $users[$key] = $user;
        }

        // Buat karyawan biasa sesuai batas paket
        $semuaKaryawanNames = [
            'Ahmad Rizky Pratama',
            'Siti Nurhaliza',
            'Budi Santoso',
            'Dewi Kartika Sari',
            'Fajar Nugroho',
            'Rahmat Hidayat',
            'Putri Amelia',
            'Eko Prasetyo',
            'Nadia Safitri',
            'Rizky Ramadhan',
        ];

        // Ambil hanya sesuai slot yang tersisa
        $karyawanNames = array_slice($semuaKaryawanNames, 0, $karyawanBiasaCount);

        foreach ($karyawanNames as $i => $nama) {
            $namaLower = strtolower(str_replace(' ', '.', $nama));
            $email = "karyawan{$suffix}_{$i}@{$domain}";

            $user = User::firstOrCreate(
                ['email' => $email],
                [
                    'name' => $nama,
                    'password' => $this->defaultPassword,
                    'role_id' => $roles['Staff']->id,
                    'instansi_id' => $instansi->id,
                    'email_verified_at' => now(),
                    'remember_token' => Str::random(60),
                ]
            );

            karyawan::firstOrCreate(
                ['user_id' => $user->id],
                [
                    'nama_lengkap' => $nama,
                    'kontak' => '0812'.rand(10000000, 99999999),
                    'alamat' => $this->getRandomAddress(),
                    'foto_profil' => null,
                    'sisa_cuti' => rand(6, 12),
                ]
            );

            $users["karyawan_{$i}"] = $user;
        }

        return $users;
    }

    private function generateName(string $role): string
    {
        return match ($role) {
            'owner' => 'Budi Hartono (Owner)',
            'manager' => 'Rina Susanti (Manager)',
            'keuangan' => 'Dewi Lestari (Keuangan)',
            'staff' => 'Andi Pratama (Staff)',
            default => 'User '.$role,
        };
    }

    // ═══════════════════════════════════════════════════════════
    //  OUTLET
    // ═══════════════════════════════════════════════════════════

    private function createOutletsForInstansi(Instansi $instansi): array
    {
        $outletNames = [
            ['nama' => 'Outlet Pusat', 'alamat' => 'Jl. Merdeka No. 1, Jakarta Pusat'],
            ['nama' => 'Outlet Cabang', 'alamat' => 'Jl. Sudirman No. 25, Jakarta Selatan'],
        ];

        $outlets = [];
        foreach ($outletNames as $data) {
            $outlet = outlet::firstOrCreate(
                ['nama_outlet' => $data['nama'], 'instansi_id' => $instansi->id],
                [
                    'alamat_outlet' => $data['alamat'],
                    'is_active' => true,
                ]
            );
            $outlets[] = $outlet;
        }

        return $outlets;
    }

    private function assignOutletsToUsers(array $users, array $outlets): void
    {
        // Owner tidak di-assign ke outlet spesifik (akses semua outlet)
        foreach (['manager', 'keuangan', 'staff'] as $key) {
            if (isset($users[$key])) {
                $users[$key]->update(['outlet_id' => $outlets[0]->id]);
            }
        }

        // Karyawan biasa di-assign ke outlet secara acak
        for ($i = 0; $i < 5; $i++) {
            $key = "karyawan_{$i}";
            if (isset($users[$key])) {
                $outletIndex = $i % count($outlets);
                $users[$key]->update(['outlet_id' => $outlets[$outletIndex]->id]);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  DIVISI
    // ═══════════════════════════════════════════════════════════

    private function createDivisisForOutlets(array $outlets, array $users): array
    {
        $divisiData = [
            ['nama' => 'Operasional', 'deskripsi' => 'Divisi yang mengurus operasional harian toko'],
            ['nama' => 'Keuangan', 'deskripsi' => 'Divisi yang mengurus administrasi keuangan'],
            ['nama' => 'Marketing', 'deskripsi' => 'Divisi yang mengurus promosi dan pemasaran'],
        ];

        $allDivisis = [];
        foreach ($outlets as $outlet) {
            $divisis = [];
            foreach ($divisiData as $data) {
                // Ketua divisi: ambil karyawan pertama yang punya profil
                $ketuaUserId = $users['staff']->id ?? $users['manager']->id;
                $ketuaKaryawan = karyawan::where('user_id', $ketuaUserId)->first();

                $divisi = Divisi::firstOrCreate(
                    ['nama_divisi' => $data['nama'], 'outlet_id' => $outlet->id],
                    [
                        'deskripsi' => $data['deskripsi'],
                        'ketua_karyawan_id' => $ketuaKaryawan?->id,
                    ]
                );
                $divisis[] = $divisi;
            }
            $allDivisis[$outlet->id] = $divisis;
        }

        return $allDivisis;
    }

    private function assignKaryawanToDivisi(array $users, array $divisisByOutlet): void
    {
        foreach ($divisisByOutlet as $outletId => $divisis) {
            foreach ($divisis as $divisi) {
                // Assign 2-3 karyawan ke setiap divisi
                $karyawanCount = 0;
                for ($i = 0; $i < 5; $i++) {
                    $key = "karyawan_{$i}";
                    if (isset($users[$key])) {
                        $karyawan = karyawan::where('user_id', $users[$key]->id)->first();
                        if ($karyawan && $karyawan->outlet_id == $outletId) {
                            AnggotaDivisi::firstOrCreate(
                                ['karyawan_id' => $karyawan->id, 'divisi_id' => $divisi->id],
                                ['peran' => $karyawanCount === 0 ? 'ketua' : 'anggota']
                            );
                            $karyawanCount++;
                            if ($karyawanCount >= 2) {
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  KATEGORI TRANSAKSI
    // ═══════════════════════════════════════════════════════════

    private function ensureKategoriTransaksi(): array
    {
        $kategoris = [];
        $kategoriData = KategoriTransaksi::all();

        if ($kategoriData->isEmpty()) {
            $this->call(KategoriTransaksiSeeder::class);
            $kategoriData = KategoriTransaksi::all();
        }

        foreach ($kategoriData as $k) {
            $kategoris[$k->tipe][] = $k;
        }

        return $kategoris;
    }

    // ═══════════════════════════════════════════════════════════
    //  TRANSAKSI KEUANGAN
    // ═══════════════════════════════════════════════════════════

    private function createTransaksiKeuangan(
        Instansi $instansi,
        array $outlets,
        array $kategoris,
        array $users
    ): void {
        $metodes = ['Tunai', 'Transfer', 'QRIS'];

        // Buat transaksi masuk (pendapatan) selama 30 hari terakhir
        for ($hari = 30; $hari >= 0; $hari--) {
            $tanggal = Carbon::now()->subDays($hari);

            // 2-4 transaksi masuk per hari
            $jmlMasuk = rand(2, 4);
            for ($i = 0; $i < $jmlMasuk; $i++) {
                $outlet = $outlets[array_rand($outlets)];
                $kategori = $kategoris['masuk'][array_rand($kategoris['masuk'])];
                $nominal = rand(500000, 5000000);

                TransaksiKas::create([
                    'instansi_id' => $instansi->id,
                    'outlet_id' => $outlet->id,
                    'kategori_transaksi_id' => $kategori->id,
                    'tanggal' => $tanggal,
                    'tipe' => 'masuk',
                    'nominal' => $nominal,
                    'metode_pembayaran' => $metodes[array_rand($metodes)],
                    'keterangan' => $this->getKeteranganMasuk($kategori->nama_kategori),
                    'lampiran_url' => null,
                    'dokumen_transaksi_id' => null,
                    'created_by' => $users['keuangan']->id ?? $users['owner']->id,
                    'status_approval' => 'disetujui',
                ]);
            }

            // 1-2 transaksi keluar per hari
            $jmlKeluar = rand(1, 2);
            for ($i = 0; $i < $jmlKeluar; $i++) {
                $outlet = $outlets[array_rand($outlets)];
                $kategori = $kategoris['keluar'][array_rand($kategoris['keluar'])];
                $nominal = rand(100000, 2000000);

                TransaksiKas::create([
                    'instansi_id' => $instansi->id,
                    'outlet_id' => $outlet->id,
                    'kategori_transaksi_id' => $kategori->id,
                    'tanggal' => $tanggal,
                    'tipe' => 'keluar',
                    'nominal' => $nominal,
                    'metode_pembayaran' => $metodes[array_rand($metodes)],
                    'keterangan' => $this->getKeteranganKeluar($kategori->nama_kategori),
                    'lampiran_url' => null,
                    'dokumen_transaksi_id' => null,
                    'created_by' => $users['keuangan']->id ?? $users['owner']->id,
                    'status_approval' => 'disetujui',
                ]);
            }
        }
    }

    private function getKeteranganMasuk(string $kategori): string
    {
        return match (true) {
            str_contains($kategori, 'Penjualan') => 'Penjualan produk hari ini',
            str_contains($kategori, 'Jasa') => 'Pendapatan jasa servis',
            str_contains($kategori, 'Setoran') => 'Setoran modal dari pemilik',
            default => 'Pemasukan lainnya',
        };
    }

    private function getKeteranganKeluar(string $kategori): string
    {
        return match (true) {
            str_contains($kategori, 'Gaji') => 'Gaji karyawan bulanan',
            str_contains($kategori, 'Pembelian') => 'Pembelian stok barang',
            str_contains($kategori, 'Listrik') => 'Bayar tagihan listrik',
            str_contains($kategori, 'Sewa') => 'Bayar sewa tempat',
            str_contains($kategori, 'Operasional') => 'Biaya operasional harian',
            default => 'Pengeluaran lainnya',
        };
    }

    // ═══════════════════════════════════════════════════════════
    //  PRESENSI
    // ═══════════════════════════════════════════════════════════

    private function createPresensi(array $users): void
    {
        // Buat presensi untuk 30 hari terakhir (hari kerja saja)
        for ($hari = 30; $hari >= 0; $hari--) {
            $tanggal = Carbon::now()->subDays($hari);

            // Skip weekend
            if ($tanggal->isSaturday() || $tanggal->isSunday()) {
                continue;
            }

            // Presensi untuk semua karyawan
            for ($i = 0; $i < 5; $i++) {
                $key = "karyawan_{$i}";
                if (! isset($users[$key])) {
                    continue;
                }

                // Random: 80% hadir, 10% terlambat, 10% tidak hadir
                $rand = rand(1, 100);
                if ($rand <= 10) {
                    continue;
                } // Tidak hadir

                $jamCheckin = match (true) {
                    $rand <= 30 => $this->randomTime('07:00', '07:29'), // Checkin awal
                    $rand <= 80 => $this->randomTime('07:30', '08:00'), // Tepat waktu
                    default => $this->randomTime('08:01', '09:00'),      // Terlambat
                };

                $jamCheckout = $this->randomTime('16:30', '18:00');

                // Hari ini: mungkin belum checkout
                $checkoutTime = $hari === 0 ? null : $jamCheckout;

                attandence::create([
                    'user_id' => $users[$key]->id,
                    'tanggal' => $tanggal->toDateString(),
                    'jam_checkin' => $jamCheckin,
                    'jam_checkout' => $checkoutTime,
                    'status' => 'hadir',
                    'keterangan' => null,
                    'lokasi_checkin' => '-6.2088,106.8456',
                    'status_keterangan' => $this->getAttendanceStatus($jamCheckin, $checkoutTime),
                ]);
            }
        }
    }

    private function getAttendanceStatus(string $jamCheckin, ?string $jamCheckout): string
    {
        $checkinTime = Carbon::parse($jamCheckin);
        $standarCheckin = Carbon::parse('07:30');

        $status = $checkinTime->lte($standarCheckin) ? 'tepat_waktu' : 'checkin_terlambat';

        if ($jamCheckout) {
            $checkoutTime = Carbon::parse($jamCheckout);
            $standarCheckout = Carbon::parse('16:30');
            if ($checkoutTime->lt($standarCheckout)) {
                $status .= ',checkout_awal';
            }
        }

        return $status;
    }

    // ═══════════════════════════════════════════════════════════
    //  PENUGASAN
    // ═══════════════════════════════════════════════════════════

    private function createPenugasans(array $users, array $divisisByOutlet, Instansi $instansi): void
    {
        $tugasData = [
            ['judul' => 'Audit Stok Gudang Q1', 'deskripsi' => 'Melakukan audit stok barang di gudang untuk kuartal 1.', 'urgency' => 'urgent', 'status' => 'selesai'],
            ['judul' => 'Training Pelayanan Pelanggan', 'deskripsi' => 'Memberikan pelatihan pelayanan pelanggan kepada tim baru.', 'urgency' => 'sedang', 'status' => 'selesai'],
            ['judul' => 'Pembersihan Area Display', 'deskripsi' => 'Membersihkan dan merapikan area display produk.', 'urgency' => 'rendah', 'status' => 'selesai'],
            ['judul' => 'Pengecekan Harga Produk', 'deskripsi' => 'Memastikan semua harga produk sesuai dengan sistem.', 'urgency' => 'sedang', 'status' => 'proses'],
            ['judul' => 'Persiapan Promosi Bulanan', 'deskripsi' => 'Menyiapkan materi dan strategi promosi bulan depan.', 'urgency' => 'urgent', 'status' => 'proses'],
            ['judul' => 'Laporan Penjualan Harian', 'deskripsi' => 'Membuat laporan penjualan harian dan mengirim ke manager.', 'urgency' => 'sedang', 'status' => 'belum'],
            ['judul' => 'Restock Produk Populer', 'deskripsi' => 'Mengisi ulang produk populer yang sudah habis di rak.', 'urgency' => 'urgent', 'status' => 'belum'],
            ['judul' => 'Servis AC Outlet', 'deskripsi' => 'Memanggil teknisi untuk servis AC di outlet.', 'urgency' => 'rendah', 'status' => 'belum'],
            ['judul' => 'Update Harga Menu', 'deskripsi' => 'Memperbarui harga menu sesuai kebijakan terbaru.', 'urgency' => 'sedang', 'status' => 'belum'],
            ['judul' => 'Inventaris Peralatan', 'deskripsi' => 'Mencatat semua peralatan yang ada di outlet.', 'urgency' => 'rendah', 'status' => 'belum'],
        ];

        foreach ($divisisByOutlet as $outletId => $divisis) {
            foreach ($divisis as $divisi) {
                $anggotaIds = AnggotaDivisi::where('divisi_id', $divisi->id)->pluck('karyawan_id');

                foreach ($tugasData as $i => $tugas) {
                    if ($anggotaIds->isEmpty()) {
                        continue;
                    }

                    $penanggungId = $anggotaIds[$i % $anggotaIds->count()];
                    $karyawan = karyawan::find($penanggungId);

                    $tenggat = match ($tugas['status']) {
                        'selesai' => Carbon::now()->subDays(rand(1, 15)),
                        'proses' => Carbon::now()->addDays(rand(1, 7)),
                        default => Carbon::now()->addDays(rand(8, 30)),
                    };

                    penugasan::create([
                        'judul' => $tugas['judul'],
                        'deskripsi' => $tugas['deskripsi'],
                        'penanggung_jawab_id' => $penanggungId,
                        'divisi_id' => $divisi->id,
                        'tenggat' => $tenggat,
                        'status' => $tugas['status'],
                        'urgency' => $tugas['urgency'],
                        'poin' => penugasan::getPoinForUrgency($tugas['urgency']),
                        'created_by' => $users['owner']->id,
                        'is_template' => false,
                        'instansi_id' => $instansi->id,
                    ]);
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  JADWAL
    // ═══════════════════════════════════════════════════════════

    private function createJadwals(array $outlets, array $users): void
    {
        $eventData = [
            ['nama' => 'Meeting Mingguan', 'kategori' => 'meeting', 'hari' => 1],
            ['nama' => 'Inspeksi Rutin', 'kategori' => 'lainnya', 'hari' => 3],
            ['nama' => 'Training Tim', 'kategori' => 'training', 'hari' => 5],
            ['nama' => 'Evaluasi Bulanan', 'kategori' => 'meeting', 'hari' => -7],
            ['nama' => 'Promosi Akhir Pekan', 'kategori' => 'event', 'hari' => -14],
            ['nama' => 'Opening Store', 'kategori' => 'lainnya', 'hari' => 0],
            ['nama' => 'Closing Store', 'kategori' => 'lainnya', 'hari' => 0],
        ];

        foreach ($outlets as $outlet) {
            foreach ($eventData as $event) {
                $tanggal = Carbon::now()->addDays($event['hari']);

                jadwal::create([
                    'nama_event' => $event['nama'],
                    'deskripsi' => "Jadwal {$event['nama']} untuk {$outlet->nama_outlet}",
                    'tanggal' => $tanggal->toDateString(),
                    'kategori' => $event['kategori'],
                    'outlet_id' => $outlet->id,
                    'created_by' => $users['owner']->id,
                ]);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  PENGAJUAN
    // ═══════════════════════════════════════════════════════════

    private function createPengajuans(array $users): void
    {
        $pengajuanData = [
            [
                'jenis' => 'cuti_tahunan',
                'keterangan' => 'Cuti untuk acara keluarga',
                'status' => 'disetujui',
                'mulai' => -10,
                'selesai' => -8,
            ],
            [
                'jenis' => 'izin_sakit',
                'keterangan' => 'Sakit demam',
                'status' => 'disetujui',
                'mulai' => -5,
                'selesai' => -5,
            ],
            [
                'jenis' => 'cuti_tahunan',
                'keterangan' => 'Cuti untuk liburan',
                'status' => 'pending',
                'mulai' => 10,
                'selesai' => 14,
            ],
            [
                'jenis' => 'mendadak',
                'keterangan' => 'Izin urusan pribadi mendadak',
                'status' => 'ditolak',
                'mulai' => -3,
                'selesai' => -3,
            ],
        ];

        for ($i = 0; $i < 5; $i++) {
            $key = "karyawan_{$i}";
            if (! isset($users[$key])) {
                continue;
            }

            $pengajuan = $pengajuanData[$i % count($pengajuanData)];

            $pengajuanModel = pengajuan::create([
                'user_id' => $users[$key]->id,
                'jenis' => $pengajuan['jenis'],
                'tanggal_mulai' => Carbon::now()->addDays($pengajuan['mulai'])->toDateString(),
                'tanggal_selesai' => Carbon::now()->addDays($pengajuan['selesai'])->toDateString(),
                'keterangan' => $pengajuan['keterangan'],
                'status' => $pengajuan['status'],
                'disetujui_oleh' => $pengajuan['status'] !== 'pending' ? $users['owner']->id : null,
                'tanggal_disetujui' => $pengajuan['status'] !== 'pending' ? now() : null,
                'alasan_penolakan' => $pengajuan['status'] === 'ditolak' ? 'Jadwal sudah padat' : null,
            ]);
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  CHAT
    // ═══════════════════════════════════════════════════════════

    private function createChats(array $users): void
    {
        $messages = [
            ['sender' => 'owner', 'receiver_key' => 'karyawan_0', 'message' => 'Hari ini toko ramai ya, good job!'],
            ['sender_key' => 'karyawan_0', 'receiver' => 'owner', 'message' => 'Terima kasih, Pak! Memang banyak pelanggan hari ini.'],
            ['sender' => 'manager', 'receiver_key' => 'karyawan_1', 'message' => 'Tolong cek stok produk A ya.'],
            ['sender_key' => 'karyawan_1', 'receiver' => 'manager', 'message' => 'Siap, Bu! Saya cek sekarang.'],
            ['sender' => 'keuangan', 'receiver_key' => 'owner', 'message' => 'Laporan keuangan bulan ini sudah selesai.'],
        ];

        foreach ($messages as $msg) {
            $senderKey = $msg['sender_key'] ?? $msg['sender'];
            $receiverKey = $msg['receiver_key'] ?? $msg['receiver'];

            if (! isset($users[$senderKey]) || ! isset($users[$receiverKey])) {
                continue;
            }

            Chat::create([
                'pengirim_id' => $users[$senderKey]->id,
                'penerima_id' => $users[$receiverKey]->id,
                'pesan' => $msg['message'],
                'is_read' => rand(0, 1) === 1,
            ]);
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  HELPERS
    // ═══════════════════════════════════════════════════════════

    private function randomTime(string $start, string $end): string
    {
        $startMinutes = Carbon::parse($start)->hour * 60 + Carbon::parse($start)->minute;
        $endMinutes = Carbon::parse($end)->hour * 60 + Carbon::parse($end)->minute;
        $randomMinutes = rand($startMinutes, $endMinutes);

        return sprintf('%02d:%02d:00', intdiv($randomMinutes, 60), $randomMinutes % 60);
    }

    private function getRandomAddress(): string
    {
        $addresses = [
            'Jl. Sudirman No. 10, Jakarta Pusat',
            'Jl. Gatot Subroto No. 25, Jakarta Selatan',
            'Jl. Thamrin No. 5, Jakarta Pusat',
            'Jl. Kuningan No. 12, Jakarta Selatan',
            'Jl. Kemang No. 8, Jakarta Selatan',
            'Jl. Senopati No. 15, Jakarta Selatan',
            'Jl. Melawai No. 20, Jakarta Selatan',
            'Jl. Pondok Indah No. 3, Jakarta Selatan',
        ];

        return $addresses[array_rand($addresses)];
    }

    private function getUniqueNames(int $count, string $prefix): array
    {
        $shuffled = $this->namaKaryawan;
        shuffle($shuffled);

        return array_slice($shuffled, 0, $count);
    }
}
