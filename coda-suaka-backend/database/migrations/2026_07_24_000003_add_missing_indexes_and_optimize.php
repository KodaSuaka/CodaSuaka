<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Tambahkan index yang belum ada untuk query performa optimal.
     * 
     * Berdasarkan analisis query di controller:
     * - penugasans: query poin kinerja filter (penanggung_jawab_id + status + urgency)
     * - approval_logs: query pending/riwayat filter (diajukan_oleh + status)
     * - attandences: query rekap filter (status)
     * - template_penugasans: query sudah ada index instansi_id
     *
     * MySQL 5.7.44 compatible: tidak ada CHECK constraint, tidak ada CTE.
     */
    public function up(): void
    {
        // ─── penugasans ──────────────────────────────────────────
        // Composite index untuk query poin kinerja (penanggung_jawab_id + status)
        // dan filter urgency
        Schema::table('penugasans', function (Blueprint $table) {
            $table->index(
                ['penanggung_jawab_id', 'status'],
                'idx_penugasans_pj_status'
            );
            $table->index(
                ['penanggung_jawab_id', 'status', 'urgency'],
                'idx_penugasans_pj_status_urgency'
            );
        });

        // ─── approval_logs ──────────────────────────────────────
        // Index untuk query approval per user (diajukan_oleh)
        Schema::table('approval_logs', function (Blueprint $table) {
            $table->index('diajukan_oleh', 'idx_approval_logs_diajukan_oleh');
            $table->index('transaksi_kas_id', 'idx_approval_logs_transaksi_kas_id');
            $table->index(
                ['diajukan_oleh', 'status'],
                'idx_approval_logs_diajukan_status'
            );
        });

        // ─── attandences ────────────────────────────────────────
        // Index untuk query rekap presensi per status
        Schema::table('attandences', function (Blueprint $table) {
            $table->index('status', 'idx_attandences_status');
            $table->index(
                ['user_id', 'status'],
                'idx_attandences_user_status'
            );
        });

        // ─── kategori_transaksis ────────────────────────────────
        // Index untuk query kategori transaksi per tipe
        Schema::table('kategori_transaksis', function (Blueprint $table) {
            $table->index('tipe', 'idx_kategori_transaksi_tipe');
        });

        // ─── transaksi_pakets ───────────────────────────────────
        // Index untuk query transaksi paket per user
        Schema::table('transaksi_pakets', function (Blueprint $table) {
            $table->index('user_id', 'idx_transaksi_pakets_user_id');
        });
    }

    public function down(): void
    {
        Schema::table('penugasans', function (Blueprint $table) {
            $table->dropIndex('idx_penugasans_pj_status');
            $table->dropIndex('idx_penugasans_pj_status_urgency');
        });

        Schema::table('approval_logs', function (Blueprint $table) {
            $table->dropIndex('idx_approval_logs_diajukan_oleh');
            $table->dropIndex('idx_approval_logs_transaksi_kas_id');
            $table->dropIndex('idx_approval_logs_diajukan_status');
        });

        Schema::table('attandences', function (Blueprint $table) {
            $table->dropIndex('idx_attandences_status');
            $table->dropIndex('idx_attandences_user_status');
        });

        Schema::table('kategori_transaksis', function (Blueprint $table) {
            $table->dropIndex('idx_kategori_transaksi_tipe');
        });

        Schema::table('transaksi_pakets', function (Blueprint $table) {
            $table->dropIndex('idx_transaksi_pakets_user_id');
        });
    }
};
