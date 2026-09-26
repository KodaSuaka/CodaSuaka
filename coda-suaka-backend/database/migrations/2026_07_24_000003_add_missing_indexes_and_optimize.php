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
        if (Schema::hasTable('penugasans')) {
            Schema::table('penugasans', function (Blueprint $table) {
                if (! Schema::hasIndex('penugasans', 'idx_penugasans_pj_status')) {
                    $table->index(
                        ['penanggung_jawab_id', 'status'],
                        'idx_penugasans_pj_status'
                    );
                }
                if (! Schema::hasIndex('penugasans', 'idx_penugasans_pj_status_urgency')) {
                    $table->index(
                        ['penanggung_jawab_id', 'status', 'urgency'],
                        'idx_penugasans_pj_status_urgency'
                    );
                }
            });
        }

        // ─── approval_logs ──────────────────────────────────────
        if (Schema::hasTable('approval_logs')) {
            Schema::table('approval_logs', function (Blueprint $table) {
                if (! Schema::hasIndex('approval_logs', 'idx_approval_logs_diajukan_oleh')) {
                    $table->index('diajukan_oleh', 'idx_approval_logs_diajukan_oleh');
                }
                if (! Schema::hasIndex('approval_logs', 'idx_approval_logs_transaksi_kas_id')) {
                    $table->index('transaksi_kas_id', 'idx_approval_logs_transaksi_kas_id');
                }
                if (! Schema::hasIndex('approval_logs', 'idx_approval_logs_diajukan_status')) {
                    $table->index(
                        ['diajukan_oleh', 'status'],
                        'idx_approval_logs_diajukan_status'
                    );
                }
            });
        }

        // ─── attandences ────────────────────────────────────────
        if (Schema::hasTable('attandences')) {
            Schema::table('attandences', function (Blueprint $table) {
                if (! Schema::hasIndex('attandences', 'idx_attandences_status')) {
                    $table->index('status', 'idx_attandences_status');
                }
                if (! Schema::hasIndex('attandences', 'idx_attandences_user_status')) {
                    $table->index(
                        ['user_id', 'status'],
                        'idx_attandences_user_status'
                    );
                }
            });
        }

        // ─── kategori_transaksis ────────────────────────────────
        if (Schema::hasTable('kategori_transaksis')) {
            Schema::table('kategori_transaksis', function (Blueprint $table) {
                if (! Schema::hasIndex('kategori_transaksis', 'idx_kategori_transaksi_tipe')) {
                    $table->index('tipe', 'idx_kategori_transaksi_tipe');
                }
            });
        }
    }

    public function down(): void
    {
        Schema::table('penugasans', function (Blueprint $table) {
            if (Schema::hasIndex('penugasans', 'idx_penugasans_pj_status')) {
                $table->dropIndex('idx_penugasans_pj_status');
            }
            if (Schema::hasIndex('penugasans', 'idx_penugasans_pj_status_urgency')) {
                $table->dropIndex('idx_penugasans_pj_status_urgency');
            }
        });

        Schema::table('approval_logs', function (Blueprint $table) {
            if (Schema::hasIndex('approval_logs', 'idx_approval_logs_diajukan_oleh')) {
                $table->dropIndex('idx_approval_logs_diajukan_oleh');
            }
            if (Schema::hasIndex('approval_logs', 'idx_approval_logs_transaksi_kas_id')) {
                $table->dropIndex('idx_approval_logs_transaksi_kas_id');
            }
            if (Schema::hasIndex('approval_logs', 'idx_approval_logs_diajukan_status')) {
                $table->dropIndex('idx_approval_logs_diajukan_status');
            }
        });

        Schema::table('attandences', function (Blueprint $table) {
            if (Schema::hasIndex('attandences', 'idx_attandences_status')) {
                $table->dropIndex('idx_attandences_status');
            }
            if (Schema::hasIndex('attandences', 'idx_attandences_user_status')) {
                $table->dropIndex('idx_attandences_user_status');
            }
        });

        Schema::table('kategori_transaksis', function (Blueprint $table) {
            if (Schema::hasIndex('kategori_transaksis', 'idx_kategori_transaksi_tipe')) {
                $table->dropIndex('idx_kategori_transaksi_tipe');
            }
        });
    }
};
