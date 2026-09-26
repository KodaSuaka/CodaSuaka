<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * TransaksiKasController::index() dan endpoint sejenis memfilter
     * `WHERE instansi_id = ? AND status_approval = ?` lalu ORDER BY tanggal.
     * Sebelumnya tidak ada index yang mencakup status_approval sama sekali,
     * sehingga MySQL scan seluruh baris instansi lalu filter status_approval
     * secara manual (Extra: "Using where"). Dikonfirmasi via EXPLAIN:
     * rows diperiksa turun dari 636 → 100 (filtered 33% → 100%) setelah
     * index ini ditambahkan, dan ORDER BY tanggal tunggal jadi backward
     * index scan tanpa filesort sama sekali.
     */
    public function up(): void
    {
        Schema::table('transaksi_kas', function (Blueprint $table) {
            if (! Schema::hasIndex('transaksi_kas', 'idx_transaksi_kas_instansi_status_tanggal')) {
                $table->index(
                    ['instansi_id', 'status_approval', 'tanggal'],
                    'idx_transaksi_kas_instansi_status_tanggal'
                );
            }
        });
    }

    public function down(): void
    {
        Schema::table('transaksi_kas', function (Blueprint $table) {
            if (Schema::hasIndex('transaksi_kas', 'idx_transaksi_kas_instansi_status_tanggal')) {
                $table->dropIndex('idx_transaksi_kas_instansi_status_tanggal');
            }
        });
    }
};
