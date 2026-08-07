<?php

namespace Database\Seeders;

use App\Models\Divisi;
use App\Models\Instansi;
use App\Models\KategoriTransaksi;
use App\Models\karyawan;
use App\Models\paket;
use App\Models\role;
use App\Models\TransaksiKas;
use App\Models\User;
use App\Models\AnggotaDivisi;
use App\Models\outlet;
use Carbon\Carbon;
use Illuminate\Database\Seeder;
use Illuminate\Support\Str;
use Illuminate\Support\Facades\Hash;

/**
 * Seeder akun pemilik UMKM (Owner) dengan variasi paket:
 *   - Paket Standart (5 karyawan max, harga 49.000)  → 4 instansi
 *   - Paket Pro      (10 karyawan max, harga 159.000) → 3 instansi
 *
 * Ketentuan:
 *   - Pemilik (Owner) TIDAK dihitung sebagai karyawan (konsisten KaryawanController::store).
 *   - Yang dihitung terhadap kuota: Manager, Keuangan, Staff + karyawan biasa.
 *   - Semua akun password: password
 *   - Beberapa instansi dibuat di bawah kuota untuk simulasi growth.
 */
class UmkmOwnerSeeder extends Seeder
{
    private string $defaultPassword = 'password';

    // ─── Variasi instansi UMKM ──────────────────────────────────────
    // [nama_instansi, paket, jumlah outlet, jumlah karyawan biasa, timezone, domain]
    private array $umkm = [
        // Paket Standart (max 5 karyawan)
        ['Bunda Catering',   'Standart', 1, 2, 'Asia/Jakarta',    'bundacatering.com'],
        ['Loundry Bersih',   'Standart', 1, 3, 'Asia/Jakarta',    'loundrybersih.com'],
        ['Warung Sederhana', 'Standart', 1, 5, 'Asia/Makassar',   'warungsederhana.com'],
        ['Laundry Kilat',    'Standart', 1, 1, 'Asia/Makassar',   'laundrykilat.com'],

        // Paket Pro (max 10 karyawan)
        ['Salon Cantik',     'Pro',      2, 6, 'Asia/Jakarta',    'saloncantik.com'],
        ['Barber Keren',     'Pro',      2, 8, 'Asia/Jakarta',    'barberkeren.com'],
        ['Gym Sehat',        'Pro',      3, 10, 'Asia/Makassar',  'gymsehat.com'],
    ];

    private array $namaKaryawan = [
        'Ahmad Rizky Pratama', 'Siti Nurhaliza', 'Budi Santoso', 'Dewi Kartika Sari',
        'Fajar Nugroho', 'Rahmat Hidayat', 'Putri Amelia', 'Eko Prasetyo',
        'Nadia Safitri', 'Rizky Ramadhan', 'Intan Permata', 'Yoga Pratama',
        'Maya Anggraini', 'Bagus Setiawan', 'Lestari Wulandari',
    ];

    public function run(): void
    {
        $this->defaultPassword = Hash::make('password');

        $roles = $this->ensureRoles();
        $pakets = [
            'Standart' => paket::where('nama_paket', 'Standart')->firstOrFail(),
            'Pro' => paket::where('nama_paket', 'Pro')->firstOrFail(),
        ];
        $kategoris = $this->ensureKategoriTransaksi();

        $this->command?->info('🏪 Membuat akun pemilik UMKM...');

        $index = 0;
        $instansiList = [];

        foreach ($this->umkm as [$namaInstansi, $namaPaket, $jumlahOutlet, $jmlKaryawan, $timezone, $domain]) {
            $index++;
            $paket = $pakets[$namaPaket];

            $instansi = Instansi::firstOrCreate(
                ['nama_instansi' => $namaInstansi],
                ['paket_id' => $paket->id, 'timezone' => $timezone]
            );

            // Transaksi paket aktif
            $this->createTransaksiPaket($instansi, $paket);

            // Owner (tidak dihitung kuota)
            $owner = $this->createUser($instansi, $roles, $domain, 'owner', 'Owner', $index);

            // Outlet
            $outlets = $this->createOutletsForInstansi($instansi, $jumlahOutlet);
            $this->assignOutletsToUsers($owner, $outlets);

            // Role users: manager, keuangan, staff (dihitung kuota)
            $roleUsers = [];
            foreach (['manager' => 'Manager', 'keuangan' => 'Keuangan', 'staff' => 'Staff'] as $key => $roleName) {
                $roleUsers[$key] = $this->createUser($instansi, $roles, $domain, $key, $roleName, $index);
                $roleUsers[$key]->update(['outlet_id' => $outlets[0]->id]);
            }

            // Karyawan biasa (sisanya dari kuota)
            $karyawanUsers = [];
            for ($i = 0; $i < $jmlKaryawan; $i++) {
                $user = $this->createUser($instansi, $roles, $domain, "karyawan_{$i}", 'Staff', $index);
                $user->update(['outlet_id' => $outlets[$i % count($outlets)]->id]);
                $karyawanUsers["karyawan_{$i}"] = $user;
            }

            $users = array_merge(
                ['owner' => $owner],
                $roleUsers,
                $karyawanUsers
            );

            // Divisi + assign karyawan
            $divisis = $this->createDivisisForOutlets($outlets, $users);

            // Transaksi keuangan 30 hari
            $this->createTransaksiKeuangan($instansi, $outlets, $kategoris, $users);

            // Simpan untuk laporan
            $instansiList[] = [
                'nama' => $instansi->nama_instansi,
                'paket' => $namaPaket,
                'harga' => number_format((float) $paket->harga, 0, ',', '.'),
                'jml_outlet' => count($outlets),
                'jml_karyawan' => $jmlKaryawan,
                'max_karyawan' => $paket->max_karyawan_per_outlet,
                'owner_email' => $owner->email,
            ];
        }

        $this->printAkun($instansiList);
    }

    // ═══════════════════════════════════════════════════════════
    //  HELPERS
    // ═══════════════════════════════════════════════════════════

    private function ensureRoles(): array
    {
        $roles = [];
        foreach (['Super Admin', 'Owner', 'Keuangan', 'Manager', 'Staff'] as $name) {
            $roles[$name] = role::firstOrCreate(
                ['nama_role' => $name],
                ['deskripsi' => "Role {$name}"]
            );
        }
        return $roles;
    }

    private function createUser(Instansi $instansi, array $roles, string $domain, string $key, string $roleName, int $instansiNum): User
    {
        $nama = $this->generateName($key, $instansiNum);
        $email = strtolower($key).$instansiNum.'@'.$domain;

        $user = User::firstOrCreate(
            ['email' => $email],
            [
                'name' => $nama,
                'password' => $this->defaultPassword,
                'role_id' => $roles[$roleName]->id,
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
                'sisa_cuti' => rand(6, 12),
            ]
        );

        return $user;
    }

    private function generateName(string $key, int $instansiNum): string
    {
        $nama = $this->namaKaryawan[array_rand($this->namaKaryawan)];

        return match ($key) {
            'owner' => $nama.' (Owner)',
            'manager' => $nama.' (Manager)',
            'keuangan' => $nama.' (Keuangan)',
            'staff' => $nama.' (Staff)',
            default => $nama,
        };
    }

    private function createTransaksiPaket(Instansi $instansi, paket $paket): void
    {
        \App\Models\transaksi_paket::firstOrCreate(
            ['instansi_id' => $instansi->id, 'paket_id' => $paket->id],
            [
                'tanggal_mulai' => Carbon::now()->subDays(15),
                'tanggal_berakhir' => Carbon::now()->addDays(15),
                'total_harga' => $paket->harga,
                'status' => 'aktif',
                'bukti_pembayaran' => null,
            ]
        );
    }

    private function createOutletsForInstansi(Instansi $instansi, int $jumlah): array
    {
        $prefix = ['Pusat', 'Cabang', 'Tambahan'];
        $outlets = [];

        for ($i = 0; $i < $jumlah; $i++) {
            $outlet = outlet::firstOrCreate(
                ['nama_outlet' => 'Outlet '.$prefix[$i], 'instansi_id' => $instansi->id],
                [
                    'alamat_outlet' => $this->getRandomAddress(),
                    'is_active' => true,
                ]
            );
            $outlets[] = $outlet;
        }

        return $outlets;
    }

    private function assignOutletsToUsers(User $owner, array $outlets): void
    {
        // Owner tidak di-assign ke outlet spesifik (akses semua outlet)
    }

    private function createDivisisForOutlets(array $outlets, array $users): array
    {
        $divisiData = [
            ['nama' => 'Operasional', 'deskripsi' => 'Divisi yang mengurus operasional harian'],
            ['nama' => 'Keuangan', 'deskripsi' => 'Divisi yang mengurus administrasi keuangan'],
            ['nama' => 'Marketing', 'deskripsi' => 'Divisi yang mengurus promosi dan pemasaran'],
        ];

        $allDivisis = [];
        $staffId = $users['staff']->id ?? $users['manager']->id;

        foreach ($outlets as $outlet) {
            $divisis = [];
            foreach ($divisiData as $data) {
                $ketuaKaryawan = karyawan::where('user_id', $staffId)->first();
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

        // Assign 2 karyawan pertama ke setiap divisi
        foreach ($allDivisis as $outletId => $divisis) {
            $karyawanCount = 0;
            foreach ($users as $key => $user) {
                if (! str_starts_with($key, 'karyawan_') || $user->outlet_id !== $outletId) {
                    continue;
                }
                $karyawan = karyawan::where('user_id', $user->id)->first();
                if (! $karyawan) {
                    continue;
                }
                foreach ($divisis as $divisi) {
                    AnggotaDivisi::firstOrCreate(
                        ['karyawan_id' => $karyawan->id, 'divisi_id' => $divisi->id],
                        ['peran' => $karyawanCount === 0 ? 'ketua' : 'anggota']
                    );
                }
                $karyawanCount++;
                if ($karyawanCount >= 2) {
                    break;
                }
            }
        }

        return $allDivisis;
    }

    private function ensureKategoriTransaksi(): array
    {
        $kategoris = [];
        $data = KategoriTransaksi::all();

        if ($data->isEmpty()) {
            $this->call(KategoriTransaksiSeeder::class);
            $data = KategoriTransaksi::all();
        }

        foreach ($data as $k) {
            $kategoris[$k->tipe][] = $k;
        }

        return $kategoris;
    }

    private function createTransaksiKeuangan(Instansi $instansi, array $outlets, array $kategoris, array $users): void
    {
        $metodes = ['Tunai', 'Transfer', 'QRIS'];
        $keuanganId = $users['keuangan']->id ?? $users['owner']->id;

        for ($hari = 30; $hari >= 0; $hari--) {
            $tanggal = Carbon::now()->subDays($hari);

            $jmlMasuk = rand(2, 4);
            for ($i = 0; $i < $jmlMasuk; $i++) {
                $outlet = $outlets[array_rand($outlets)];
                $kategori = $kategoris['masuk'][array_rand($kategoris['masuk'])];
                TransaksiKas::create([
                    'instansi_id' => $instansi->id,
                    'outlet_id' => $outlet->id,
                    'kategori_transaksi_id' => $kategori->id,
                    'tanggal' => $tanggal,
                    'tipe' => 'masuk',
                    'nominal' => rand(500000, 5000000),
                    'metode_pembayaran' => $metodes[array_rand($metodes)],
                    'keterangan' => 'Pendapatan harian',
                    'lampiran_url' => null,
                    'dokumen_transaksi_id' => null,
                    'created_by' => $keuanganId,
                    'status_approval' => 'disetujui',
                ]);
            }

            $jmlKeluar = rand(1, 2);
            for ($i = 0; $i < $jmlKeluar; $i++) {
                $outlet = $outlets[array_rand($outlets)];
                $kategori = $kategoris['keluar'][array_rand($kategoris['keluar'])];
                TransaksiKas::create([
                    'instansi_id' => $instansi->id,
                    'outlet_id' => $outlet->id,
                    'kategori_transaksi_id' => $kategori->id,
                    'tanggal' => $tanggal,
                    'tipe' => 'keluar',
                    'nominal' => rand(100000, 2000000),
                    'metode_pembayaran' => $metodes[array_rand($metodes)],
                    'keterangan' => 'Pengeluaran operasional',
                    'lampiran_url' => null,
                    'dokumen_transaksi_id' => null,
                    'created_by' => $keuanganId,
                    'status_approval' => 'disetujui',
                ]);
            }
        }
    }

    private function getRandomAddress(): string
    {
        $jalan = ['Jl. Merdeka', 'Jl. Sudirman', 'Jl. Ahmad Yani', 'Jl. Gatot Subroto', 'Jl. Pahlawan'];
        $kota = ['Jakarta', 'Bandung', 'Surabaya', 'Makassar', 'Semarang'];

        return $jalan[array_rand($jalan)].' No. '.rand(1, 200).', '.$kota[array_rand($kota)];
    }

    private function printAkun(array $instansiList): void
    {
        $this->command?->info('');
        $this->command?->info('═══════════════════════════════════════════════════════');
        $this->command?->info('  AKUN PEMILIK UMKM (Password: password untuk semua)');
        $this->command?->info('═══════════════════════════════════════════════════════');

        foreach ($instansiList as $d) {
            $this->command?->info('');
            $this->command?->info("  {$d['nama']} — Paket {$d['paket']} (Rp {$d['harga']}/bulan)");
            $this->command?->info('    Owner      : '.$d['owner_email']);
            $this->command?->info('    Outlet     : '.$d['jml_outlet']);
            $this->command?->info("    Karyawan   : {$d['jml_karyawan']} (maks {$d['max_karyawan']}, pemilik tidak dihitung)");
        }

        $this->command?->info('');
        $this->command?->info('✅ Seeder akun pemilik UMKM selesai.');
    }
}
