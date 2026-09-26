<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Tambah index performa untuk query rekap kehadiran,
     * notifikasi, dan penugasan yang sering dijalankan.
     */
    public function up(): void
    {
        // ── pengajuans: rekap kehadiran cross-reference ──────
        Schema::table('pengajuans', function (Blueprint $table) {
            $table->index(['status', 'jenis', 'tanggal_mulai', 'tanggal_selesai'], 'pengajuans_rekap_idx');
            $table->index(['user_id', 'status'], 'pengajuans_user_status_idx');
        });

        // ── attandences: rekap kehadiran query ───────────────
        Schema::table('attandences', function (Blueprint $table) {
            $table->index(['tanggal', 'status'], 'attandences_tanggal_status_idx');
            $table->index(['user_id', 'tanggal'], 'attandences_user_tanggal_idx');
        });

        // ── notifications: unread count query ────────────────
        Schema::table('notifications', function (Blueprint $table) {
            $table->index(['user_id', 'is_read'], 'notifications_user_unread_idx');
        });

        // ── penugasans: filter & assign query ────────────────
        Schema::table('penugasans', function (Blueprint $table) {
            $table->index(['status', 'divisi_id'], 'penugasans_status_divisi_idx');
            $table->index(['penanggung_jawab_id', 'status'], 'penugasans_pj_status_idx');
        });
    }

    public function down(): void
    {
        Schema::table('pengajuans', function (Blueprint $table) {
            $table->dropIndex('pengajuans_rekap_idx');
            $table->dropIndex('pengajuans_user_status_idx');
        });

        Schema::table('attandences', function (Blueprint $table) {
            $table->dropIndex('attandences_tanggal_status_idx');
            $table->dropIndex('attandences_user_tanggal_idx');
        });

        Schema::table('notifications', function (Blueprint $table) {
            $table->dropIndex('notifications_user_unread_idx');
        });

        Schema::table('penugasans', function (Blueprint $table) {
            $table->dropIndex('penugasans_status_divisi_idx');
            $table->dropIndex('penugasans_pj_status_idx');
        });
    }
};
