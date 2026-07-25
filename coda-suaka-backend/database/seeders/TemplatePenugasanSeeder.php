<?php

namespace Database\Seeders;

use App\Models\Scopes\TenantScope;
use App\Models\TemplatePenugasan;
use Illuminate\Database\Seeder;

class TemplatePenugasanSeeder extends Seeder
{
    /**
     * Template tugas default global untuk semua UMKM.
     * Tidak terikat instansi (instansi_id = NULL).
     * Akan di-copy ke instansi baru saat owner mendaftar.
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
        // Cek template global yang sudah ada (instansi_id = NULL)
        $existingCount = TemplatePenugasan::withoutGlobalScope(TenantScope::class)
            ->whereNull('instansi_id')
            ->count();
        $templatesToCreate = array_slice($this->defaultTemplates, 0, 10 - $existingCount);

        if (empty($templatesToCreate)) {
            $this->command?->info('Template penugasan global sudah lengkap (10 template).');
            return;
        }

        foreach ($templatesToCreate as $template) {
            TemplatePenugasan::withoutGlobalScope(TenantScope::class)->create([
                'nama_template' => $template['nama_template'],
                'deskripsi_template' => $template['deskripsi_template'],
                'urgency_default' => $template['urgency_default'],
                'poin_default' => $template['poin_default'],
                'instansi_id' => null,
                'created_by' => null,
            ]);
        }

        $this->command?->info("Berhasil membuat " . count($templatesToCreate) . " template penugasan global.");
    }

    /**
     * Helper: copy template global ke instansi tertentu.
     * Dipanggil dari AuthController saat owner baru mendaftar.
     */
    public static function copyGlobalTemplatesToInstansi(string $instansiId, int $createdBy): int
    {
        $globalTemplates = TemplatePenugasan::withoutGlobalScope(TenantScope::class)
            ->whereNull('instansi_id')
            ->get();

        $count = 0;
        foreach ($globalTemplates as $template) {
            TemplatePenugasan::withoutGlobalScope(TenantScope::class)->create([
                'nama_template' => $template->nama_template,
                'deskripsi_template' => $template->deskripsi_template,
                'urgency_default' => $template->urgency_default,
                'poin_default' => $template->poin_default,
                'instansi_id' => $instansiId,
                'created_by' => $createdBy,
            ]);
            $count++;
        }

        return $count;
    }
}
