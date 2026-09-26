<?php

namespace Database\Seeders;

use App\Models\penugasan;
use Illuminate\Database\Seeder;

class TemplatePenugasanSeeder extends Seeder
{
    /**
     * Template tugas default global untuk semua UMKM.
     * Disimpan di tabel penugasan dengan is_template = true, instansi_id = NULL.
     * Akan di-copy ke instansi baru saat owner mendaftar.
     */
    private array $defaultTemplates = [
        [
            'judul' => 'Pembersihan Area Kerja',
            'deskripsi' => 'Membersihkan area kerja sesuai standar kebersihan. Meliputi sapu, pel, dan rapikan barang.',
            'urgency' => 'sedang',
            'poin' => 5,
        ],
        [
            'judul' => 'Pengecekan Stok Barang',
            'deskripsi' => 'Melakukan pengecekan stok barang secara fisik dan mencatat selisih dengan sistem.',
            'urgency' => 'sedang',
            'poin' => 8,
        ],
        [
            'judul' => 'Servis Pelanggan',
            'deskripsi' => 'Melayani pelanggan dengan ramah dan profesional. Pastikan kepuasan pelanggan terjaga.',
            'urgency' => 'urgent',
            'poin' => 10,
        ],
        [
            'judul' => 'Perawatan Mesin/Peralatan',
            'deskripsi' => 'Melakukan perawatan rutin pada mesin dan peralatan kerja sesuai jadwal maintenance.',
            'urgency' => 'sedang',
            'poin' => 7,
        ],
        [
            'judul' => 'Penyusunan Laporan Harian',
            'deskripsi' => 'Menyusun laporan harian aktivitas, pencapaian, dan kendala yang dihadapi.',
            'urgency' => 'rendah',
            'poin' => 5,
        ],
        [
            'judul' => 'Persiapan Opening Store',
            'deskripsi' => 'Persiapan pembukaan toko: nyalakan AC, cek display, siapkan kas, dan pastikan kebersihan.',
            'urgency' => 'urgent',
            'poin' => 10,
        ],
        [
            'judul' => 'Closing Store',
            'deskripsi' => 'Tutup toko: matikan peralatan, kunci pintu, rekap kas harian, dan kunci brankas.',
            'urgency' => 'urgent',
            'poin' => 10,
        ],
        [
            'judul' => 'Restock Produk',
            'deskripsi' => 'Mengisi ulang produk yang kosong dari gudang ke rak display.',
            'urgency' => 'sedang',
            'poin' => 6,
        ],
        [
            'judul' => 'Training Tim Baru',
            'deskripsi' => 'Memberikan pelatihan dan orientasi kepada anggota tim baru.',
            'urgency' => 'rendah',
            'poin' => 12,
        ],
        [
            'judul' => 'Audit Internal',
            'deskripsi' => 'Melakukan pengecekan internal terhadap prosedur dan kepatuhan operasional.',
            'urgency' => 'sedang',
            'poin' => 15,
        ],
    ];

    public function run(): void
    {
        // Cek template global yang sudah ada (is_template = true, instansi_id = NULL)
        $existingCount = penugasan::where('is_template', true)
            ->whereNull('instansi_id')
            ->count();

        $templatesToCreate = array_slice($this->defaultTemplates, 0, 10 - $existingCount);

        if (empty($templatesToCreate)) {
            $this->command?->info('Template penugasan global sudah lengkap (10 template).');

            return;
        }

        foreach ($templatesToCreate as $template) {
            penugasan::create([
                'judul' => $template['judul'],
                'deskripsi' => $template['deskripsi'],
                'urgency' => $template['urgency'],
                'poin' => $template['poin'],
                'status' => 'belum',
                'is_template' => true,
                'instansi_id' => null,
                'created_by' => null,
            ]);
        }

        $this->command?->info('Berhasil membuat '.count($templatesToCreate).' template penugasan global.');
    }

    /**
     * Helper: copy template global ke instansi tertentu.
     * Dipanggil dari AuthController saat owner baru mendaftar.
     */
    public static function copyGlobalTemplatesToInstansi(string $instansiId, int $createdBy): int
    {
        $globalTemplates = penugasan::where('is_template', true)
            ->whereNull('instansi_id')
            ->get();

        $count = 0;
        foreach ($globalTemplates as $template) {
            penugasan::create([
                'judul' => $template->judul,
                'deskripsi' => $template->deskripsi,
                'urgency' => $template->urgency,
                'poin' => $template->poin,
                'status' => 'belum',
                'is_template' => true,
                'instansi_id' => $instansiId,
                'created_by' => $createdBy,
            ]);
            $count++;
        }

        return $count;
    }
}
