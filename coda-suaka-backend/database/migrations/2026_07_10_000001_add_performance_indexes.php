<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Tambahkan database indexes untuk query yang sering dijalankan.
     * Ini krusial untuk performa di server 2-core.
     *
     * Tabel users: sering di-query via instansi_id, role_id, email
     * Tabel penugasans: sering di-query via created_by, status, penanggung_jawab_id
     * Tabel pengajuans: sering di-query via user_id, status
     * Tabel karyawans: sering di-query via user_id, outlet_id
     * Tabel divisis: sering di-query via outlet_id
     * Tabel jadwals: sering di-query via outlet_id, tanggal
     * Tabel chats: sering di-query via pengirim_id, penerima_id
     */
    public function up(): void
    {
        // ─── users ───────────────────────────────────────────────
        if (Schema::hasTable('users')) {
            Schema::table('users', function (Blueprint $table) {
                if (! Schema::hasIndex('users', 'idx_users_instansi_id')) {
                    $table->index('instansi_id', 'idx_users_instansi_id');
                }
                if (! Schema::hasIndex('users', 'idx_users_role_id')) {
                    $table->index('role_id', 'idx_users_role_id');
                }
            });
        }

        // ─── penugasans ──────────────────────────────────────────
        if (Schema::hasTable('penugasans')) {
            Schema::table('penugasans', function (Blueprint $table) {
                if (! Schema::hasIndex('penugasans', 'idx_penugasans_created_by')) {
                    $table->index('created_by', 'idx_penugasans_created_by');
                }
                if (! Schema::hasIndex('penugasans', 'idx_penugasans_status')) {
                    $table->index('status', 'idx_penugasans_status');
                }
                if (! Schema::hasIndex('penugasans', 'idx_penugasans_penanggung_jawab_id')) {
                    $table->index('penanggung_jawab_id', 'idx_penugasans_penanggung_jawab_id');
                }
                if (! Schema::hasIndex('penugasans', 'idx_penugasans_created_by_status')) {
                    $table->index(['created_by', 'status'], 'idx_penugasans_created_by_status');
                }
            });
        }

        // ─── pengajuans ──────────────────────────────────────────
        if (Schema::hasTable('pengajuans')) {
            Schema::table('pengajuans', function (Blueprint $table) {
                if (! Schema::hasIndex('pengajuans', 'idx_pengajuans_user_id')) {
                    $table->index('user_id', 'idx_pengajuans_user_id');
                }
                if (! Schema::hasIndex('pengajuans', 'idx_pengajuans_status')) {
                    $table->index('status', 'idx_pengajuans_status');
                }
                if (! Schema::hasIndex('pengajuans', 'idx_pengajuans_user_id_status')) {
                    $table->index(['user_id', 'status'], 'idx_pengajuans_user_id_status');
                }
            });
        }

        // ─── karyawans ───────────────────────────────────────────
        if (Schema::hasTable('karyawans')) {
            Schema::table('karyawans', function (Blueprint $table) {
                if (! Schema::hasIndex('karyawans', 'idx_karyawans_user_id')) {
                    $table->index('user_id', 'idx_karyawans_user_id');
                }
                if (! Schema::hasIndex('karyawans', 'idx_karyawans_outlet_id')) {
                    $table->index('outlet_id', 'idx_karyawans_outlet_id');
                }
            });
        }

        // ─── divisis ─────────────────────────────────────────────
        if (Schema::hasTable('divisis')) {
            Schema::table('divisis', function (Blueprint $table) {
                if (! Schema::hasIndex('divisis', 'idx_divisis_outlet_id')) {
                    $table->index('outlet_id', 'idx_divisis_outlet_id');
                }
            });
        }

        // ─── jadwals ─────────────────────────────────────────────
        if (Schema::hasTable('jadwals')) {
            Schema::table('jadwals', function (Blueprint $table) {
                if (! Schema::hasIndex('jadwals', 'idx_jadwals_outlet_id')) {
                    $table->index('outlet_id', 'idx_jadwals_outlet_id');
                }
                if (! Schema::hasIndex('jadwals', 'idx_jadwals_tanggal')) {
                    $table->index('tanggal', 'idx_jadwals_tanggal');
                }
                if (! Schema::hasIndex('jadwals', 'idx_jadwals_outlet_tanggal')) {
                    $table->index(['outlet_id', 'tanggal'], 'idx_jadwals_outlet_tanggal');
                }
            });
        }

        // ─── chats ───────────────────────────────────────────────
        if (Schema::hasTable('chats')) {
            Schema::table('chats', function (Blueprint $table) {
                if (! Schema::hasIndex('chats', 'idx_chats_pengirim_id')) {
                    $table->index('pengirim_id', 'idx_chats_pengirim_id');
                }
                if (! Schema::hasIndex('chats', 'idx_chats_penerima_id')) {
                    $table->index('penerima_id', 'idx_chats_penerima_id');
                }
            });
        }

        // ─── transaksi_kas ───────────────────────────────────────
        if (Schema::hasTable('transaksi_kas')) {
            Schema::table('transaksi_kas', function (Blueprint $table) {
                if (! Schema::hasIndex('transaksi_kas', 'idx_transaksi_kas_tipe')) {
                    $table->index('tipe', 'idx_transaksi_kas_tipe');
                }
                if (! Schema::hasIndex('transaksi_kas', 'idx_transaksi_kas_instansi_tipe_tanggal')) {
                    $table->index(['instansi_id', 'tipe', 'tanggal'], 'idx_transaksi_kas_instansi_tipe_tanggal');
                }
            });
        }
    }

    public function down(): void
    {
        Schema::table('users', function (Blueprint $table) {
            if (Schema::hasIndex('users', 'idx_users_instansi_id')) {
                $table->dropIndex('idx_users_instansi_id');
            }
            if (Schema::hasIndex('users', 'idx_users_role_id')) {
                $table->dropIndex('idx_users_role_id');
            }
        });

        Schema::table('penugasans', function (Blueprint $table) {
            if (Schema::hasIndex('penugasans', 'idx_penugasans_created_by')) {
                $table->dropIndex('idx_penugasans_created_by');
            }
            if (Schema::hasIndex('penugasans', 'idx_penugasans_status')) {
                $table->dropIndex('idx_penugasans_status');
            }
            if (Schema::hasIndex('penugasans', 'idx_penugasans_penanggung_jawab_id')) {
                $table->dropIndex('idx_penugasans_penanggung_jawab_id');
            }
            if (Schema::hasIndex('penugasans', 'idx_penugasans_created_by_status')) {
                $table->dropIndex('idx_penugasans_created_by_status');
            }
        });

        Schema::table('pengajuans', function (Blueprint $table) {
            if (Schema::hasIndex('pengajuans', 'idx_pengajuans_user_id')) {
                $table->dropIndex('idx_pengajuans_user_id');
            }
            if (Schema::hasIndex('pengajuans', 'idx_pengajuans_status')) {
                $table->dropIndex('idx_pengajuans_status');
            }
            if (Schema::hasIndex('pengajuans', 'idx_pengajuans_user_id_status')) {
                $table->dropIndex('idx_pengajuans_user_id_status');
            }
        });

        Schema::table('karyawans', function (Blueprint $table) {
            if (Schema::hasIndex('karyawans', 'idx_karyawans_user_id')) {
                $table->dropIndex('idx_karyawans_user_id');
            }
            if (Schema::hasIndex('karyawans', 'idx_karyawans_outlet_id')) {
                $table->dropIndex('idx_karyawans_outlet_id');
            }
        });

        Schema::table('divisis', function (Blueprint $table) {
            if (Schema::hasIndex('divisis', 'idx_divisis_outlet_id')) {
                $table->dropIndex('idx_divisis_outlet_id');
            }
        });

        Schema::table('jadwals', function (Blueprint $table) {
            if (Schema::hasIndex('jadwals', 'idx_jadwals_outlet_id')) {
                $table->dropIndex('idx_jadwals_outlet_id');
            }
            if (Schema::hasIndex('jadwals', 'idx_jadwals_tanggal')) {
                $table->dropIndex('idx_jadwals_tanggal');
            }
            if (Schema::hasIndex('jadwals', 'idx_jadwals_outlet_tanggal')) {
                $table->dropIndex('idx_jadwals_outlet_tanggal');
            }
        });

        Schema::table('chats', function (Blueprint $table) {
            if (Schema::hasIndex('chats', 'idx_chats_pengirim_id')) {
                $table->dropIndex('idx_chats_pengirim_id');
            }
            if (Schema::hasIndex('chats', 'idx_chats_penerima_id')) {
                $table->dropIndex('idx_chats_penerima_id');
            }
        });

        Schema::table('transaksi_kas', function (Blueprint $table) {
            if (Schema::hasIndex('transaksi_kas', 'idx_transaksi_kas_tipe')) {
                $table->dropIndex('idx_transaksi_kas_tipe');
            }
            if (Schema::hasIndex('transaksi_kas', 'idx_transaksi_kas_instansi_tipe_tanggal')) {
                $table->dropIndex('idx_transaksi_kas_instansi_tipe_tanggal');
            }
        });
    }
};
