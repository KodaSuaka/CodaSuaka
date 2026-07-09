<?php

namespace Database\Seeders;

use App\Models\KategoriTransaksi;
use App\Models\instansi;
use Illuminate\Database\Seeder;

class KategoriTransaksiSeeder extends Seeder
{
    /**
     * Kategori default yang dibuat untuk setiap instansi baru.
     */
    private array $defaultKategoris = [
        ['nama_kategori' => 'Penjualan Barang',       'tipe' => 'masuk',  'sifat' => 'operasional',     'termasuk_hpp' => false],
        ['nama_kategori' => 'Pendapatan Jasa',        'tipe' => 'masuk',  'sifat' => 'operasional',     'termasuk_hpp' => false],
        ['nama_kategori' => 'Pembelian Bahan/Stok',   'tipe' => 'keluar', 'sifat' => 'operasional',     'termasuk_hpp' => true],
        ['nama_kategori' => 'Gaji & Upah',            'tipe' => 'keluar', 'sifat' => 'operasional',     'termasuk_hpp' => false],
        ['nama_kategori' => 'Sewa Tempat',            'tipe' => 'keluar', 'sifat' => 'operasional',     'termasuk_hpp' => false],
        ['nama_kategori' => 'Listrik, Air, Internet', 'tipe' => 'keluar', 'sifat' => 'operasional',     'termasuk_hpp' => false],
        ['nama_kategori' => 'Operasional Lain-lain',  'tipe' => 'keluar', 'sifat' => 'operasional',     'termasuk_hpp' => false],
        ['nama_kategori' => 'Setoran Modal',          'tipe' => 'masuk',  'sifat' => 'non_operasional',  'termasuk_hpp' => false],
        ['nama_kategori' => 'Prive (Ambil Pribadi)',  'tipe' => 'keluar', 'sifat' => 'non_operasional',  'termasuk_hpp' => false],
        ['nama_kategori' => 'Pinjaman Masuk',         'tipe' => 'masuk',  'sifat' => 'non_operasional',  'termasuk_hpp' => false],
        ['nama_kategori' => 'Bayar Cicilan Pinjaman', 'tipe' => 'keluar', 'sifat' => 'non_operasional',  'termasuk_hpp' => false],
    ];

    /**
     * Seed kategori default untuk semua instansi yang ada.
     */
    public function run(): void
    {
        $instansis = instansi::all();

        foreach ($instansis as $instansi) {
            foreach ($this->defaultKategoris as $kategori) {
                KategoriTransaksi::firstOrCreate(
                    [
                        'instansi_id' => $instansi->id,
                        'nama_kategori' => $kategori['nama_kategori'],
                    ],
                    [
                        'tipe' => $kategori['tipe'],
                        'sifat' => $kategori['sifat'],
                        'termasuk_hpp' => $kategori['termasuk_hpp'],
                        'is_default' => true,
                        'is_active' => true,
                    ]
                );
            }
        }
    }
}
