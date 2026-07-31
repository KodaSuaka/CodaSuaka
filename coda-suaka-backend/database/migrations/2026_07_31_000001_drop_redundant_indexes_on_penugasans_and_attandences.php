<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Hapus index redundan pada penugasans & attandences.
     *
     * Beberapa migration index sebelumnya menambahkan index yang sama
     * (atau yang merupakan leftmost-prefix dari index lain yang lebih
     * lengkap) dengan nama berbeda. Index redundan tidak mempercepat
     * SELECT apa pun — MySQL sudah bisa memakai index yang lebih panjang
     * lewat leftmost prefix — tapi tetap harus di-update di setiap
     * INSERT/UPDATE. Ini jadi beban tulis di jalur panas:
     * checkin/checkout (attandences) tiap hari per karyawan, dan
     * accept/complete/validasi (penugasans) tiap perubahan status.
     *
     * Yang dipertahankan menutupi semuanya sebagai leftmost prefix:
     * - penugasans: idx_penugasans_pj_status_urgency (penanggung_jawab_id, status, urgency)
     * - penugasans: idx_penugasans_created_by_status (created_by, status)
     * - penugasans: penugasans_status_divisi_idx     (status, divisi_id)
     * - attandences: attandences_user_id_tanggal_unique (user_id, tanggal) UNIQUE
     *
     * Semua foreign key tetap punya index pendukung (kolom FK selalu
     * jadi kolom terkiri pada index yang dipertahankan), jadi MySQL
     * tidak akan menolak DROP INDEX ini.
     */
    private const REDUNDANT = [
        'penugasans' => [
            // duplikat persis satu sama lain, sekaligus prefix dari
            // idx_penugasans_pj_status_urgency
            'idx_penugasans_pj_status',
            'penugasans_pj_status_idx',
            // prefix dari idx_penugasans_pj_status_urgency
            'idx_penugasans_penanggung_jawab_id',
            // prefix dari idx_penugasans_created_by_status
            'idx_penugasans_created_by',
            // prefix dari penugasans_status_divisi_idx
            'idx_penugasans_status',
        ],
        'attandences' => [
            // duplikat persis dari attandences_user_id_tanggal_unique
            'attandences_user_tanggal_idx',
        ],
    ];

    public function up(): void
    {
        foreach (self::REDUNDANT as $table => $indexes) {
            if (! Schema::hasTable($table)) {
                continue;
            }
            Schema::table($table, function (Blueprint $blueprint) use ($table, $indexes) {
                foreach ($indexes as $index) {
                    if (Schema::hasIndex($table, $index)) {
                        $blueprint->dropIndex($index);
                    }
                }
            });
        }
    }

    public function down(): void
    {
        Schema::table('penugasans', function (Blueprint $table) {
            if (! Schema::hasIndex('penugasans', 'idx_penugasans_pj_status')) {
                $table->index(['penanggung_jawab_id', 'status'], 'idx_penugasans_pj_status');
            }
            if (! Schema::hasIndex('penugasans', 'penugasans_pj_status_idx')) {
                $table->index(['penanggung_jawab_id', 'status'], 'penugasans_pj_status_idx');
            }
            if (! Schema::hasIndex('penugasans', 'idx_penugasans_penanggung_jawab_id')) {
                $table->index('penanggung_jawab_id', 'idx_penugasans_penanggung_jawab_id');
            }
            if (! Schema::hasIndex('penugasans', 'idx_penugasans_created_by')) {
                $table->index('created_by', 'idx_penugasans_created_by');
            }
            if (! Schema::hasIndex('penugasans', 'idx_penugasans_status')) {
                $table->index('status', 'idx_penugasans_status');
            }
        });

        Schema::table('attandences', function (Blueprint $table) {
            if (! Schema::hasIndex('attandences', 'attandences_user_tanggal_idx')) {
                $table->index(['user_id', 'tanggal'], 'attandences_user_tanggal_idx');
            }
        });
    }
};
