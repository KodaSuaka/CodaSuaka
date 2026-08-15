<?php

namespace Database\Seeders;

use App\Models\BarangJasa;
use App\Models\Divisi;
use App\Models\Instansi;
use App\Models\KategoriTransaksi;
use App\Models\karyawan;
use App\Models\paket;
use App\Models\role;
use App\Models\Stok;
use App\Models\TransaksiKas;
use App\Models\User;
use App\Models\AnggotaDivisi;
use App\Models\outlet;
use Carbon\Carbon;
use Illuminate\Database\Seeder;
use Illuminate\Support\Str;
use Illuminate\Support\Facades\Hash;

/**
 * Seeder akun pemilik UMKM (Owner) dengan variasi paket — 18 UMKM untuk kebutuhan laporan:
 *   - Paket Standart (5 karyawan max, harga 49.000)  → 10 instansi
 *   - Paket Pro      (10 karyawan max, harga 159.000) → 8 instansi
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
    // [nama_instansi, paket, jumlah outlet, jumlah karyawan biasa, timezone, domain, nama_pemilik]
    // Semua di wilayah Kediri/Nganjuk (Jawa Timur) → WIB → Asia/Jakarta.
    // nama_pemilik dipakai untuk email owner organik: firstname.lastname@domain.
    private array $umkm = [
        // Paket Standart (max 5 karyawan)
        ['Bunda Catering',      'Standart', 1, 2, 'Asia/Jakarta', 'bundacatering.com',     'Sri Wahyuni'],
        ['Loundry Bersih',      'Standart', 1, 3, 'Asia/Jakarta', 'loundrybersih.com',     'Budi Santoso'],
        ['Warung Sederhana',    'Standart', 1, 5, 'Asia/Jakarta', 'warungsederhana.com',   'Dewi Lestari'],
        ['Laundry Kilat',       'Standart', 1, 1, 'Asia/Jakarta', 'laundrykilat.com',      'Agus Salim'],
        ['Toko Roti Manis',     'Standart', 1, 4, 'Asia/Jakarta', 'tokorotimanis.com',     'Rina Marlina'],
        ['Fotokopi Cepat',      'Standart', 1, 3, 'Asia/Jakarta', 'fotokopicepat.com',     'Hendra Wijaya'],
        ['Cuci Mobil Kinclong', 'Standart', 1, 5, 'Asia/Jakarta', 'cucimobilkinclong.com', 'Joko Susilo'],
        ['Warnet Gaming Zone',  'Standart', 1, 2, 'Asia/Jakarta', 'warnetgamingzone.com',  'Rizky Pratama'],
        ['Toko Bunga Melati',   'Standart', 1, 4, 'Asia/Jakarta', 'tokobungamelati.com',   'Melati Puspita'],
        ['Es Krim Gelato Roma', 'Standart', 1, 5, 'Asia/Jakarta', 'eskrimgelatoroma.com',  'Andi Kurniawan'],

        // Paket Pro (max 10 karyawan)
        ['Salon Cantik',         'Pro', 2, 6,  'Asia/Jakarta', 'saloncantik.com',        'Ayu Anggraini'],
        ['Barber Keren',         'Pro', 2, 8,  'Asia/Jakarta', 'barberkeren.com',        'Dimas Prasetyo'],
        ['Gym Sehat',            'Pro', 3, 10, 'Asia/Jakarta', 'gymsehat.com',           'Bayu Nugroho'],
        ['Kopi Kenangan Senja',  'Pro', 2, 7,  'Asia/Jakarta', 'kopikenangansenja.com',  'Fitri Handayani'],
        ['Bengkel Motor Jaya',   'Pro', 2, 8,  'Asia/Jakarta', 'bengkelmotorjaya.com',   'Slamet Riyadi'],
        ['Apotek Sehat Selalu',  'Pro', 1, 5,  'Asia/Jakarta', 'apoteksehatselalu.com',  'Ratna Sari'],
        ['Klinik Gigi Ceria',    'Pro', 1, 6,  'Asia/Jakarta', 'klinikgigiceria.com',    'Nia Ramadhani'],
        ['Studio Foto Kenangan', 'Pro', 1, 4,  'Asia/Jakarta', 'studiofotokenangan.com', 'Eko Purnomo'],
    ];

    private array $namaKaryawan = [
        'Ahmad Rizky Pratama', 'Siti Nurhaliza', 'Budi Santoso', 'Dewi Kartika Sari',
        'Fajar Nugroho', 'Rahmat Hidayat', 'Putri Amelia', 'Eko Prasetyo',
        'Nadia Safitri', 'Rizky Ramadhan', 'Intan Permata', 'Yoga Pratama',
        'Maya Anggraini', 'Bagus Setiawan', 'Lestari Wulandari',
    ];

    private array $kotaLahir = [
        'Bandung', 'Surabaya', 'Yogyakarta', 'Semarang', 'Makassar', 'Medan',
        'Malang', 'Solo', 'Bekasi', 'Depok', 'Bogor', 'Cirebon', 'Purwokerto',
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

        // domain (elemen ke-6) hanya metadata bisnis; email kini pakai gmail.com → slot dilewati.
        foreach ($this->umkm as [$namaInstansi, $namaPaket, $jumlahOutlet, $jmlKaryawan, $timezone, , $ownerNama]) {
            $index++;
            $paket = $pakets[$namaPaket];

            $instansi = Instansi::firstOrCreate(
                ['nama_instansi' => $namaInstansi],
                ['paket_id' => $paket->id, 'timezone' => $timezone]
            );

            // Transaksi paket aktif
            $this->createTransaksiPaket($instansi, $paket);

            // Owner (tidak dihitung kuota) — nama & email organik (firstname.lastname@gmail.com)
            $owner = $this->createUser($instansi, $roles, 'owner', 'Owner', $index, $ownerNama);

            // Outlet
            $outlets = $this->createOutletsForInstansi($instansi, $jumlahOutlet);
            $this->assignOutletsToUsers($owner, $outlets);

            // Role users: manager, keuangan, staff (dihitung kuota)
            $roleUsers = [];
            foreach (['manager' => 'Manager', 'keuangan' => 'Keuangan', 'staff' => 'Staff'] as $key => $roleName) {
                $roleUsers[$key] = $this->createUser($instansi, $roles, $key, $roleName, $index);
                $roleUsers[$key]->update(['outlet_id' => $outlets[0]->id]);
            }

            // Karyawan biasa (sisanya dari kuota)
            $karyawanUsers = [];
            for ($i = 0; $i < $jmlKaryawan; $i++) {
                $user = $this->createUser($instansi, $roles, "karyawan_{$i}", 'Staff', $index);
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

            // Katalog Barang/Jasa (produk jual) + Stok bahan/barang produksi — organik per jenis usaha
            $this->seedKatalogDanStok($instansi, $namaInstansi);

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

    private function createUser(Instansi $instansi, array $roles, string $key, string $roleName, int $instansiNum, ?string $namaFix = null): User
    {
        // Semua email akun memakai domain publik gmail.com (lebih organik).
        if ($namaFix !== null) {
            // Email organik dari nama pemilik: "Sri Wahyuni" → sri.wahyuni@gmail.com
            $nama = $namaFix.' ('.$roleName.')';
            $email = Str::of($namaFix)->lower()->replace(' ', '.').'@gmail.com';
        } else {
            $nama = $this->generateName($key, $instansiNum);
            $email = strtolower($key).$instansiNum.'@gmail.com';
        }

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
                'tempat_lahir' => $this->kotaLahir[array_rand($this->kotaLahir)],
                'tanggal_lahir' => now()->subYears(rand(20, 45))->subDays(rand(0, 364))->toDateString(),
                'tanggal_mulai_kerja' => now()->subMonths(rand(3, 60))->toDateString(),
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
                        ['karyawan_id' => $karyawan->id, 'divisi_id' => $divisi->id]
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

    /**
     * Seed katalog Barang/Jasa (produk jual) + Stok (bahan/barang produksi)
     * organik sesuai jenis usaha: catering punya bahan masak, laundry punya
     * deterjen, salon punya shampo, gym punya suplemen, dst.
     */
    private function seedKatalogDanStok(Instansi $instansi, string $namaInstansi): void
    {
        [$barangJasa, $stok] = $this->katalogUntuk($namaInstansi);

        foreach ($barangJasa as $bj) {
            BarangJasa::firstOrCreate(
                ['instansi_id' => $instansi->id, 'nama' => $bj['nama']],
                [
                    'jenis' => $bj['jenis'],
                    'kategori' => $bj['kategori'] ?? null,
                    'satuan' => $bj['satuan'],
                    'harga_jual' => $bj['harga_jual'],
                    'harga_beli' => $bj['harga_beli'] ?? null,
                    'stok' => $bj['jenis'] === 'barang' ? ($bj['stok'] ?? rand(10, 100)) : 0,
                    'is_active' => true,
                ]
            );
        }

        foreach ($stok as $s) {
            Stok::firstOrCreate(
                ['instansi_id' => $instansi->id, 'nama' => $s['nama']],
                [
                    'kategori' => $s['kategori'] ?? 'Bahan Baku',
                    'satuan' => $s['satuan'],
                    'stok' => $s['stok'],
                    'stok_minimum' => $s['stok_minimum'] ?? 5,
                    'harga_beli' => $s['harga_beli'],
                    'is_active' => true,
                ]
            );
        }
    }

    /**
     * Data katalog & stok per jenis usaha.
     *
     * @return array{0: array<int, array<string, mixed>>, 1: array<int, array<string, mixed>>}
     */
    private function katalogUntuk(string $namaInstansi): array
    {
        return match ($namaInstansi) {
            'Bunda Catering' => [
                [
                    ['nama' => 'Nasi Box Ayam', 'jenis' => 'barang', 'kategori' => 'Makanan', 'satuan' => 'box', 'harga_jual' => 25000, 'harga_beli' => 15000, 'stok' => 40],
                    ['nama' => 'Snack Box', 'jenis' => 'barang', 'kategori' => 'Makanan', 'satuan' => 'box', 'harga_jual' => 15000, 'harga_beli' => 9000, 'stok' => 60],
                    ['nama' => 'Tumpeng Mini', 'jenis' => 'barang', 'kategori' => 'Makanan', 'satuan' => 'porsi', 'harga_jual' => 150000, 'harga_beli' => 90000, 'stok' => 8],
                    ['nama' => 'Paket Prasmanan', 'jenis' => 'jasa', 'kategori' => 'Layanan', 'satuan' => 'pax', 'harga_jual' => 35000],
                ],
                [
                    ['nama' => 'Beras', 'satuan' => 'kg', 'stok' => 50, 'stok_minimum' => 10, 'harga_beli' => 12000],
                    ['nama' => 'Ayam Potong', 'satuan' => 'kg', 'stok' => 20, 'stok_minimum' => 5, 'harga_beli' => 35000],
                    ['nama' => 'Minyak Goreng', 'satuan' => 'liter', 'stok' => 15, 'stok_minimum' => 5, 'harga_beli' => 18000],
                    ['nama' => 'Telur Ayam', 'satuan' => 'kg', 'stok' => 12, 'stok_minimum' => 3, 'harga_beli' => 28000],
                ],
            ],
            'Loundry Bersih', 'Laundry Kilat' => [
                [
                    ['nama' => 'Cuci Kering Kiloan', 'jenis' => 'jasa', 'kategori' => 'Layanan', 'satuan' => 'kg', 'harga_jual' => 7000],
                    ['nama' => 'Cuci Setrika Kiloan', 'jenis' => 'jasa', 'kategori' => 'Layanan', 'satuan' => 'kg', 'harga_jual' => 10000],
                    ['nama' => 'Setrika Saja', 'jenis' => 'jasa', 'kategori' => 'Layanan', 'satuan' => 'kg', 'harga_jual' => 5000],
                    ['nama' => 'Cuci Bed Cover', 'jenis' => 'jasa', 'kategori' => 'Layanan', 'satuan' => 'pcs', 'harga_jual' => 25000],
                ],
                [
                    ['nama' => 'Deterjen', 'satuan' => 'kg', 'stok' => 25, 'stok_minimum' => 5, 'harga_beli' => 25000],
                    ['nama' => 'Pewangi Pakaian', 'satuan' => 'liter', 'stok' => 12, 'stok_minimum' => 3, 'harga_beli' => 30000],
                    ['nama' => 'Pelembut', 'satuan' => 'liter', 'stok' => 10, 'stok_minimum' => 3, 'harga_beli' => 28000],
                ],
            ],
            'Warung Sederhana' => [
                [
                    ['nama' => 'Nasi Rames', 'jenis' => 'barang', 'kategori' => 'Makanan', 'satuan' => 'porsi', 'harga_jual' => 15000, 'harga_beli' => 8000, 'stok' => 50],
                    ['nama' => 'Ayam Goreng', 'jenis' => 'barang', 'kategori' => 'Makanan', 'satuan' => 'potong', 'harga_jual' => 12000, 'harga_beli' => 7000, 'stok' => 40],
                    ['nama' => 'Es Teh Manis', 'jenis' => 'barang', 'kategori' => 'Minuman', 'satuan' => 'gelas', 'harga_jual' => 4000, 'harga_beli' => 1500, 'stok' => 100],
                    ['nama' => 'Gorengan', 'jenis' => 'barang', 'kategori' => 'Snack', 'satuan' => 'pcs', 'harga_jual' => 2000, 'harga_beli' => 800, 'stok' => 80],
                ],
                [
                    ['nama' => 'Beras', 'satuan' => 'kg', 'stok' => 40, 'stok_minimum' => 10, 'harga_beli' => 12000],
                    ['nama' => 'Minyak Goreng', 'satuan' => 'liter', 'stok' => 15, 'stok_minimum' => 5, 'harga_beli' => 18000],
                    ['nama' => 'Telur Ayam', 'satuan' => 'kg', 'stok' => 8, 'stok_minimum' => 3, 'harga_beli' => 28000],
                    ['nama' => 'Gula Pasir', 'satuan' => 'kg', 'stok' => 6, 'stok_minimum' => 2, 'harga_beli' => 15000],
                ],
            ],
            'Salon Cantik' => [
                [
                    ['nama' => 'Potong Rambut', 'jenis' => 'jasa', 'kategori' => 'Layanan', 'satuan' => 'sesi', 'harga_jual' => 35000],
                    ['nama' => 'Creambath', 'jenis' => 'jasa', 'kategori' => 'Layanan', 'satuan' => 'sesi', 'harga_jual' => 50000],
                    ['nama' => 'Facial', 'jenis' => 'jasa', 'kategori' => 'Layanan', 'satuan' => 'sesi', 'harga_jual' => 75000],
                    ['nama' => 'Cat Rambut', 'jenis' => 'jasa', 'kategori' => 'Layanan', 'satuan' => 'sesi', 'harga_jual' => 120000],
                    ['nama' => 'Smoothing', 'jenis' => 'jasa', 'kategori' => 'Layanan', 'satuan' => 'sesi', 'harga_jual' => 250000],
                ],
                [
                    ['nama' => 'Shampo Salon', 'satuan' => 'botol', 'stok' => 12, 'stok_minimum' => 3, 'harga_beli' => 45000],
                    ['nama' => 'Pewarna Rambut', 'satuan' => 'box', 'stok' => 20, 'stok_minimum' => 5, 'harga_beli' => 35000],
                    ['nama' => 'Krim Creambath', 'satuan' => 'kg', 'stok' => 5, 'stok_minimum' => 2, 'harga_beli' => 60000],
                ],
            ],
            'Barber Keren' => [
                [
                    ['nama' => 'Potong Rambut Pria', 'jenis' => 'jasa', 'kategori' => 'Layanan', 'satuan' => 'sesi', 'harga_jual' => 25000],
                    ['nama' => 'Cukur Jenggot', 'jenis' => 'jasa', 'kategori' => 'Layanan', 'satuan' => 'sesi', 'harga_jual' => 15000],
                    ['nama' => 'Potong + Keramas', 'jenis' => 'jasa', 'kategori' => 'Layanan', 'satuan' => 'sesi', 'harga_jual' => 35000],
                    ['nama' => 'Semir Rambut', 'jenis' => 'jasa', 'kategori' => 'Layanan', 'satuan' => 'sesi', 'harga_jual' => 40000],
                ],
                [
                    ['nama' => 'Pomade', 'satuan' => 'kaleng', 'stok' => 15, 'stok_minimum' => 4, 'harga_beli' => 30000],
                    ['nama' => 'Sabun Cukur', 'satuan' => 'botol', 'stok' => 10, 'stok_minimum' => 3, 'harga_beli' => 20000],
                    ['nama' => 'Cologne', 'satuan' => 'botol', 'stok' => 8, 'stok_minimum' => 2, 'harga_beli' => 35000],
                ],
            ],
            'Gym Sehat' => [
                [
                    ['nama' => 'Membership Bulanan', 'jenis' => 'jasa', 'kategori' => 'Membership', 'satuan' => 'bulan', 'harga_jual' => 200000],
                    ['nama' => 'Membership Tahunan', 'jenis' => 'jasa', 'kategori' => 'Membership', 'satuan' => 'tahun', 'harga_jual' => 1800000],
                    ['nama' => 'Personal Trainer', 'jenis' => 'jasa', 'kategori' => 'Layanan', 'satuan' => 'sesi', 'harga_jual' => 150000],
                    ['nama' => 'Day Pass', 'jenis' => 'jasa', 'kategori' => 'Layanan', 'satuan' => 'kali', 'harga_jual' => 35000],
                ],
                [
                    ['nama' => 'Suplemen Protein', 'satuan' => 'botol', 'stok' => 10, 'stok_minimum' => 2, 'harga_beli' => 250000],
                    ['nama' => 'Handuk Gym', 'satuan' => 'pcs', 'stok' => 30, 'stok_minimum' => 5, 'harga_beli' => 25000],
                    ['nama' => 'Air Mineral', 'satuan' => 'dus', 'stok' => 20, 'stok_minimum' => 5, 'harga_beli' => 30000],
                ],
            ],
            'Toko Roti Manis' => [
                [
                    ['nama' => 'Roti Tawar', 'jenis' => 'barang', 'kategori' => 'Roti', 'satuan' => 'bungkus', 'harga_jual' => 15000, 'harga_beli' => 8000, 'stok' => 40],
                    ['nama' => 'Donat Coklat', 'jenis' => 'barang', 'kategori' => 'Roti', 'satuan' => 'pcs', 'harga_jual' => 5000, 'harga_beli' => 2500, 'stok' => 80],
                    ['nama' => 'Bolu Pandan', 'jenis' => 'barang', 'kategori' => 'Kue', 'satuan' => 'loyang', 'harga_jual' => 35000, 'harga_beli' => 20000, 'stok' => 15],
                    ['nama' => 'Roti Sobek', 'jenis' => 'barang', 'kategori' => 'Roti', 'satuan' => 'bungkus', 'harga_jual' => 18000, 'harga_beli' => 10000, 'stok' => 30],
                    ['nama' => 'Kue Ulang Tahun', 'jenis' => 'barang', 'kategori' => 'Kue', 'satuan' => 'pcs', 'harga_jual' => 150000, 'harga_beli' => 90000, 'stok' => 5],
                ],
                [
                    ['nama' => 'Tepung Terigu', 'satuan' => 'kg', 'stok' => 50, 'stok_minimum' => 10, 'harga_beli' => 12000],
                    ['nama' => 'Mentega', 'satuan' => 'kg', 'stok' => 15, 'stok_minimum' => 4, 'harga_beli' => 45000],
                    ['nama' => 'Gula Halus', 'satuan' => 'kg', 'stok' => 20, 'stok_minimum' => 5, 'harga_beli' => 15000],
                    ['nama' => 'Telur Ayam', 'satuan' => 'kg', 'stok' => 25, 'stok_minimum' => 5, 'harga_beli' => 28000],
                    ['nama' => 'Ragi Instan', 'satuan' => 'bungkus', 'stok' => 30, 'stok_minimum' => 8, 'harga_beli' => 8000],
                ],
            ],
            'Fotokopi Cepat' => [
                [
                    ['nama' => 'Fotokopi HVS', 'jenis' => 'jasa', 'kategori' => 'Layanan', 'satuan' => 'lembar', 'harga_jual' => 500],
                    ['nama' => 'Print Warna', 'jenis' => 'jasa', 'kategori' => 'Layanan', 'satuan' => 'lembar', 'harga_jual' => 2000],
                    ['nama' => 'Jilid Spiral', 'jenis' => 'jasa', 'kategori' => 'Layanan', 'satuan' => 'buku', 'harga_jual' => 15000],
                    ['nama' => 'Laminating', 'jenis' => 'jasa', 'kategori' => 'Layanan', 'satuan' => 'lembar', 'harga_jual' => 5000],
                    ['nama' => 'Scan Dokumen', 'jenis' => 'jasa', 'kategori' => 'Layanan', 'satuan' => 'lembar', 'harga_jual' => 2000],
                ],
                [
                    ['nama' => 'Kertas HVS A4', 'satuan' => 'rim', 'stok' => 30, 'stok_minimum' => 8, 'harga_beli' => 55000],
                    ['nama' => 'Tinta Printer', 'satuan' => 'botol', 'stok' => 20, 'stok_minimum' => 5, 'harga_beli' => 85000],
                    ['nama' => 'Toner', 'satuan' => 'pcs', 'stok' => 10, 'stok_minimum' => 2, 'harga_beli' => 350000],
                    ['nama' => 'Plastik Laminating', 'satuan' => 'pack', 'stok' => 15, 'stok_minimum' => 4, 'harga_beli' => 45000],
                ],
            ],
            'Cuci Mobil Kinclong' => [
                [
                    ['nama' => 'Cuci Mobil Reguler', 'jenis' => 'jasa', 'kategori' => 'Layanan', 'satuan' => 'unit', 'harga_jual' => 35000],
                    ['nama' => 'Cuci Mobil + Wax', 'jenis' => 'jasa', 'kategori' => 'Layanan', 'satuan' => 'unit', 'harga_jual' => 60000],
                    ['nama' => 'Cuci Motor', 'jenis' => 'jasa', 'kategori' => 'Layanan', 'satuan' => 'unit', 'harga_jual' => 15000],
                    ['nama' => 'Salon Interior', 'jenis' => 'jasa', 'kategori' => 'Layanan', 'satuan' => 'unit', 'harga_jual' => 150000],
                    ['nama' => 'Poles Body', 'jenis' => 'jasa', 'kategori' => 'Layanan', 'satuan' => 'unit', 'harga_jual' => 250000],
                ],
                [
                    ['nama' => 'Sabun Cuci Mobil', 'satuan' => 'liter', 'stok' => 25, 'stok_minimum' => 5, 'harga_beli' => 35000],
                    ['nama' => 'Wax Poles', 'satuan' => 'botol', 'stok' => 12, 'stok_minimum' => 3, 'harga_beli' => 55000],
                    ['nama' => 'Semir Ban', 'satuan' => 'botol', 'stok' => 15, 'stok_minimum' => 4, 'harga_beli' => 25000],
                    ['nama' => 'Kanebo', 'satuan' => 'pcs', 'stok' => 20, 'stok_minimum' => 5, 'harga_beli' => 15000],
                ],
            ],
            'Warnet Gaming Zone' => [
                [
                    ['nama' => 'Paket 1 Jam Reguler', 'jenis' => 'jasa', 'kategori' => 'Layanan', 'satuan' => 'jam', 'harga_jual' => 4000],
                    ['nama' => 'Paket 3 Jam', 'jenis' => 'jasa', 'kategori' => 'Layanan', 'satuan' => 'paket', 'harga_jual' => 10000],
                    ['nama' => 'Paket VIP 1 Jam', 'jenis' => 'jasa', 'kategori' => 'Layanan', 'satuan' => 'jam', 'harga_jual' => 6000],
                    ['nama' => 'Paket Malam', 'jenis' => 'jasa', 'kategori' => 'Layanan', 'satuan' => 'paket', 'harga_jual' => 20000],
                ],
                [
                    ['nama' => 'Voucher Game', 'satuan' => 'pcs', 'stok' => 100, 'stok_minimum' => 20, 'harga_beli' => 10000],
                    ['nama' => 'Snack Ringan', 'satuan' => 'pcs', 'stok' => 50, 'stok_minimum' => 10, 'harga_beli' => 3000],
                    ['nama' => 'Minuman Kaleng', 'satuan' => 'pcs', 'stok' => 60, 'stok_minimum' => 12, 'harga_beli' => 5000],
                ],
            ],
            'Toko Bunga Melati' => [
                [
                    ['nama' => 'Buket Mawar', 'jenis' => 'barang', 'kategori' => 'Buket', 'satuan' => 'buket', 'harga_jual' => 150000, 'harga_beli' => 80000, 'stok' => 10],
                    ['nama' => 'Karangan Bunga Papan', 'jenis' => 'barang', 'kategori' => 'Karangan', 'satuan' => 'papan', 'harga_jual' => 350000, 'harga_beli' => 200000, 'stok' => 5],
                    ['nama' => 'Bunga Meja', 'jenis' => 'barang', 'kategori' => 'Rangkaian', 'satuan' => 'pcs', 'harga_jual' => 75000, 'harga_beli' => 40000, 'stok' => 15],
                    ['nama' => 'Buket Wisuda', 'jenis' => 'barang', 'kategori' => 'Buket', 'satuan' => 'buket', 'harga_jual' => 100000, 'harga_beli' => 55000, 'stok' => 12],
                ],
                [
                    ['nama' => 'Mawar Segar', 'satuan' => 'ikat', 'stok' => 30, 'stok_minimum' => 6, 'harga_beli' => 45000],
                    ['nama' => 'Pita Dekorasi', 'satuan' => 'roll', 'stok' => 20, 'stok_minimum' => 5, 'harga_beli' => 15000],
                    ['nama' => 'Kertas Buket', 'satuan' => 'pack', 'stok' => 25, 'stok_minimum' => 5, 'harga_beli' => 25000],
                    ['nama' => 'Floral Foam', 'satuan' => 'pcs', 'stok' => 40, 'stok_minimum' => 10, 'harga_beli' => 8000],
                ],
            ],
            'Es Krim Gelato Roma' => [
                [
                    ['nama' => 'Gelato Single Scoop', 'jenis' => 'barang', 'kategori' => 'Es Krim', 'satuan' => 'cup', 'harga_jual' => 18000, 'harga_beli' => 7000, 'stok' => 60],
                    ['nama' => 'Gelato Double Scoop', 'jenis' => 'barang', 'kategori' => 'Es Krim', 'satuan' => 'cup', 'harga_jual' => 30000, 'harga_beli' => 12000, 'stok' => 50],
                    ['nama' => 'Es Krim Cone', 'jenis' => 'barang', 'kategori' => 'Es Krim', 'satuan' => 'cone', 'harga_jual' => 12000, 'harga_beli' => 5000, 'stok' => 80],
                    ['nama' => 'Milkshake', 'jenis' => 'barang', 'kategori' => 'Minuman', 'satuan' => 'gelas', 'harga_jual' => 25000, 'harga_beli' => 10000, 'stok' => 40],
                    ['nama' => 'Banana Split', 'jenis' => 'barang', 'kategori' => 'Es Krim', 'satuan' => 'porsi', 'harga_jual' => 35000, 'harga_beli' => 15000, 'stok' => 25],
                ],
                [
                    ['nama' => 'Susu Full Cream', 'satuan' => 'liter', 'stok' => 40, 'stok_minimum' => 8, 'harga_beli' => 20000],
                    ['nama' => 'Gula Pasir', 'satuan' => 'kg', 'stok' => 25, 'stok_minimum' => 5, 'harga_beli' => 15000],
                    ['nama' => 'Perisa Buah', 'satuan' => 'botol', 'stok' => 15, 'stok_minimum' => 4, 'harga_beli' => 55000],
                    ['nama' => 'Cone Wafer', 'satuan' => 'box', 'stok' => 20, 'stok_minimum' => 5, 'harga_beli' => 45000],
                    ['nama' => 'Topping Coklat', 'satuan' => 'kg', 'stok' => 10, 'stok_minimum' => 2, 'harga_beli' => 65000],
                ],
            ],
            'Kopi Kenangan Senja' => [
                [
                    ['nama' => 'Kopi Susu Gula Aren', 'jenis' => 'barang', 'kategori' => 'Minuman', 'satuan' => 'cup', 'harga_jual' => 18000, 'harga_beli' => 7000, 'stok' => 60],
                    ['nama' => 'Americano', 'jenis' => 'barang', 'kategori' => 'Minuman', 'satuan' => 'cup', 'harga_jual' => 15000, 'harga_beli' => 5000, 'stok' => 50],
                    ['nama' => 'Cappuccino', 'jenis' => 'barang', 'kategori' => 'Minuman', 'satuan' => 'cup', 'harga_jual' => 22000, 'harga_beli' => 8000, 'stok' => 50],
                    ['nama' => 'Matcha Latte', 'jenis' => 'barang', 'kategori' => 'Minuman', 'satuan' => 'cup', 'harga_jual' => 25000, 'harga_beli' => 10000, 'stok' => 40],
                    ['nama' => 'Croissant', 'jenis' => 'barang', 'kategori' => 'Snack', 'satuan' => 'pcs', 'harga_jual' => 20000, 'harga_beli' => 12000, 'stok' => 30],
                ],
                [
                    ['nama' => 'Biji Kopi Arabika', 'satuan' => 'kg', 'stok' => 15, 'stok_minimum' => 3, 'harga_beli' => 120000],
                    ['nama' => 'Susu UHT', 'satuan' => 'liter', 'stok' => 30, 'stok_minimum' => 8, 'harga_beli' => 18000],
                    ['nama' => 'Gula Aren Cair', 'satuan' => 'liter', 'stok' => 10, 'stok_minimum' => 3, 'harga_beli' => 25000],
                    ['nama' => 'Sirup Vanilla', 'satuan' => 'botol', 'stok' => 8, 'stok_minimum' => 2, 'harga_beli' => 45000],
                ],
            ],
            'Bengkel Motor Jaya' => [
                [
                    ['nama' => 'Servis Ringan', 'jenis' => 'jasa', 'kategori' => 'Layanan', 'satuan' => 'unit', 'harga_jual' => 50000],
                    ['nama' => 'Ganti Oli', 'jenis' => 'jasa', 'kategori' => 'Layanan', 'satuan' => 'unit', 'harga_jual' => 45000],
                    ['nama' => 'Servis Besar', 'jenis' => 'jasa', 'kategori' => 'Layanan', 'satuan' => 'unit', 'harga_jual' => 150000],
                    ['nama' => 'Tambal Ban', 'jenis' => 'jasa', 'kategori' => 'Layanan', 'satuan' => 'titik', 'harga_jual' => 15000],
                    ['nama' => 'Ganti Kampas Rem', 'jenis' => 'jasa', 'kategori' => 'Layanan', 'satuan' => 'set', 'harga_jual' => 80000],
                ],
                [
                    ['nama' => 'Oli Mesin', 'satuan' => 'botol', 'stok' => 40, 'stok_minimum' => 8, 'harga_beli' => 45000],
                    ['nama' => 'Kampas Rem', 'satuan' => 'set', 'stok' => 20, 'stok_minimum' => 5, 'harga_beli' => 55000],
                    ['nama' => 'Busi', 'satuan' => 'pcs', 'stok' => 30, 'stok_minimum' => 8, 'harga_beli' => 25000],
                    ['nama' => 'Ban Dalam', 'satuan' => 'pcs', 'stok' => 15, 'stok_minimum' => 4, 'harga_beli' => 35000],
                ],
            ],
            'Apotek Sehat Selalu' => [
                [
                    ['nama' => 'Paracetamol', 'jenis' => 'barang', 'kategori' => 'Obat', 'satuan' => 'strip', 'harga_jual' => 5000, 'harga_beli' => 3000, 'stok' => 100],
                    ['nama' => 'Vitamin C', 'jenis' => 'barang', 'kategori' => 'Suplemen', 'satuan' => 'botol', 'harga_jual' => 25000, 'harga_beli' => 15000, 'stok' => 50],
                    ['nama' => 'Masker Medis', 'jenis' => 'barang', 'kategori' => 'Alkes', 'satuan' => 'box', 'harga_jual' => 35000, 'harga_beli' => 20000, 'stok' => 40],
                    ['nama' => 'Minyak Kayu Putih', 'jenis' => 'barang', 'kategori' => 'Obat', 'satuan' => 'botol', 'harga_jual' => 18000, 'harga_beli' => 11000, 'stok' => 35],
                    ['nama' => 'Plester Luka', 'jenis' => 'barang', 'kategori' => 'Alkes', 'satuan' => 'box', 'harga_jual' => 12000, 'harga_beli' => 7000, 'stok' => 45],
                ],
                [
                    ['nama' => 'Obat Generik', 'satuan' => 'box', 'stok' => 100, 'stok_minimum' => 20, 'harga_beli' => 30000],
                    ['nama' => 'Alkohol 70%', 'satuan' => 'botol', 'stok' => 40, 'stok_minimum' => 8, 'harga_beli' => 15000],
                    ['nama' => 'Perban', 'satuan' => 'roll', 'stok' => 50, 'stok_minimum' => 10, 'harga_beli' => 8000],
                    ['nama' => 'Hand Sanitizer', 'satuan' => 'botol', 'stok' => 30, 'stok_minimum' => 6, 'harga_beli' => 20000],
                ],
            ],
            'Klinik Gigi Ceria' => [
                [
                    ['nama' => 'Scaling / Bersih Karang', 'jenis' => 'jasa', 'kategori' => 'Layanan', 'satuan' => 'sesi', 'harga_jual' => 150000],
                    ['nama' => 'Tambal Gigi', 'jenis' => 'jasa', 'kategori' => 'Layanan', 'satuan' => 'gigi', 'harga_jual' => 200000],
                    ['nama' => 'Cabut Gigi', 'jenis' => 'jasa', 'kategori' => 'Layanan', 'satuan' => 'gigi', 'harga_jual' => 250000],
                    ['nama' => 'Konsultasi', 'jenis' => 'jasa', 'kategori' => 'Layanan', 'satuan' => 'sesi', 'harga_jual' => 50000],
                    ['nama' => 'Pemutihan Gigi', 'jenis' => 'jasa', 'kategori' => 'Layanan', 'satuan' => 'sesi', 'harga_jual' => 500000],
                ],
                [
                    ['nama' => 'Bahan Tambal Gigi', 'satuan' => 'box', 'stok' => 15, 'stok_minimum' => 3, 'harga_beli' => 250000],
                    ['nama' => 'Anestesi Lokal', 'satuan' => 'ampul', 'stok' => 40, 'stok_minimum' => 8, 'harga_beli' => 35000],
                    ['nama' => 'Sarung Tangan Medis', 'satuan' => 'box', 'stok' => 25, 'stok_minimum' => 5, 'harga_beli' => 45000],
                    ['nama' => 'Masker Medis', 'satuan' => 'box', 'stok' => 30, 'stok_minimum' => 6, 'harga_beli' => 30000],
                ],
            ],
            'Studio Foto Kenangan' => [
                [
                    ['nama' => 'Foto Studio Keluarga', 'jenis' => 'jasa', 'kategori' => 'Layanan', 'satuan' => 'sesi', 'harga_jual' => 150000],
                    ['nama' => 'Pas Foto', 'jenis' => 'jasa', 'kategori' => 'Layanan', 'satuan' => 'paket', 'harga_jual' => 25000],
                    ['nama' => 'Foto Wisuda', 'jenis' => 'jasa', 'kategori' => 'Layanan', 'satuan' => 'sesi', 'harga_jual' => 100000],
                    ['nama' => 'Cetak Foto 4R', 'jenis' => 'jasa', 'kategori' => 'Layanan', 'satuan' => 'lembar', 'harga_jual' => 5000],
                    ['nama' => 'Foto Produk', 'jenis' => 'jasa', 'kategori' => 'Layanan', 'satuan' => 'sesi', 'harga_jual' => 200000],
                ],
                [
                    ['nama' => 'Kertas Foto Glossy', 'satuan' => 'pack', 'stok' => 30, 'stok_minimum' => 6, 'harga_beli' => 65000],
                    ['nama' => 'Tinta Foto', 'satuan' => 'set', 'stok' => 15, 'stok_minimum' => 4, 'harga_beli' => 120000],
                    ['nama' => 'Bingkai Foto', 'satuan' => 'pcs', 'stok' => 40, 'stok_minimum' => 8, 'harga_beli' => 35000],
                    ['nama' => 'Background Kain', 'satuan' => 'pcs', 'stok' => 8, 'stok_minimum' => 2, 'harga_beli' => 150000],
                ],
            ],
            default => [
                [
                    ['nama' => 'Produk Umum', 'jenis' => 'barang', 'kategori' => 'Umum', 'satuan' => 'pcs', 'harga_jual' => 20000, 'harga_beli' => 12000, 'stok' => 30],
                    ['nama' => 'Layanan Umum', 'jenis' => 'jasa', 'kategori' => 'Layanan', 'satuan' => 'sesi', 'harga_jual' => 30000],
                ],
                [
                    ['nama' => 'Bahan Baku Umum', 'satuan' => 'kg', 'stok' => 20, 'stok_minimum' => 5, 'harga_beli' => 15000],
                ],
            ],
        };
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
