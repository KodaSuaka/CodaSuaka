<?php

namespace Database\Seeders;

use App\Models\Instansi;
use App\Models\TemplatePenugasan;
use App\Models\User;
use Illuminate\Database\Seeder;

class TemplatePenugasanSeeder extends Seeder
{
    /**
     * Template tugas default untuk UMKM.
     *akan dibuat untuk setiap instansi yang ada.
     */
    private array $defaultTemplates = [
        [
            'nama_template' => 'Pembersihan Area Kerja',
            'deskripsi_template' => 'Membersihkan area kerja sesuai standar kebersihan. Meliputi sapu, pel, dan rapikan barang.',
            'urgency_default' => 'sedang',
            'poin_default' => 5,
        ],
        [
            'nama_template' => 'Pengecekan Stok Barang',
            'deskripsi_template' => 'Melakukan pengecekan stok barang secara fisik dan mencatat selisih dengan sistem.',
            'urgency_default' => 'sedang',
            'poin_default' => 8,
        ],
        [
            'nama_template' => 'Servis Pelanggan',
            'deskripsi_template' => 'Melayani pelanggan dengan ramah dan profesional. Pastikan kepuasan pelanggan terjaga.',
            'urgency_default' => 'urgent',
            'poin_default' => 10,
        ],
        [
            'nama_template' => 'Perawatan Mesin/Peralatan',
            'deskripsi_template' => 'Melakukan perawatan rutin pada mesin dan peralatan kerja sesuai jadwal maintenance.',
            'urgency_default' => 'sedang',
            'poin_default' => 7,
        ],
        [
            'nama_template' => 'Penyusunan Laporan Harian',
            'deskripsi_template' => 'Menyusun laporan harian aktivitas, pencapaian, dan kendala yang dihadapi.',
            'urgency_default' => 'rendah',
            'poin_default' => 5,
        ],
        [
            'nama_template' => 'Persiapan Opening Store',
            'deskripsi_template' => 'Persiapan pembukaan toko: nyalakan AC, cek display, siapkan kas, dan pastikan kebersihan.',
            'urgency_default' => 'urgent',
            'poin_default' => 10,
        ],
        [
            'nama_template' => 'Closing Store',
            'deskripsi_template' => 'Tutup toko: matikan peralatan, kunci pintu, rekap kas harian, dan kunci brankas.',
            'urgency_default' => 'urgent',
            'poin_default' => 10,
        ],
        [
            'nama_template' => 'Restock Produk',
            'deskripsi_template' => 'Mengisi ulang produk yang kosong dari gudang ke rak display.',
            'urgency_default' => 'sedang',
            'poin_default' => 6,
        ],
        [
            'nama_template' => 'Training Tim Baru',
            'deskripsi_template' => 'Memberikan pelatihan dan orientasi kepada anggota tim baru.',
            'urgency_default' => 'rendah',
            'poin_default' => 12,
        ],
        [
            'nama_template' => 'Audit Internal',
            'deskripsi_template' => 'Melakukan pengecekan internal terhadap prosedur dan kepatuhan operasional.',
            'urgency_default' => 'sedang',
            'poin_default' => 15,
        ],
    ];

    public function run(): void
    {
        // Ambil semua instansi yang ada
        $instansis = Instansi::all();

        if ($instansis->isEmpty()) {
            $this->command?->warn('Tidak ada instansi ditemukan. Seeder template penugasan dilewati.');
            return;
        }

        foreach ($instansis as $instansi) {
            // Cari user Owner atau Manager di instansi ini untuk created_by
            $creator = User::where('instansi_id', $instansi->id)
                ->whereHas('role', fn ($q) => $q->whereIn('nama_role', ['Owner', 'Manager']))
                ->first();

            if (!$creator) {
                // Fallback: ambil user pertama di instansi ini
                $creator = User::where('instansi_id', $instansi->id)->first();
            }

            if (!$creator) {
                $this->command?->warn("Tidak ada user ditemukan untuk instansi: {$instansi->nama_instansi}");
                continue;
            }

            // Maksimal 10 template per instansi
            $existingCount = TemplatePenugasan::where('instansi_id', $instansi->id)->count();
            $templatesToCreate = array_slice($this->defaultTemplates, 0, 10 - $existingCount);

            if (empty($templatesToCreate)) {
                $this->command?->info("Instansi '{$instansi->nama_instansi}' sudah memiliki 10 template.");
                continue;
            }

            foreach ($templatesToCreate as $template) {
                TemplatePenugasan::create([
                    'nama_template' => $template['nama_template'],
                    'deskripsi_template' => $template['deskripsi_template'],
                    'urgency_default' => $template['urgency_default'],
                    'poin_default' => $template['poin_default'],
                    'instansi_id' => $instansi->id,
                    'created_by' => $creator->id,
                ]);
            }

            $this->command?->info("Berhasil membuat " . count($templatesToCreate) . " template tugas untuk instansi: {$instansi->nama_instansi}");
        }
    }
}