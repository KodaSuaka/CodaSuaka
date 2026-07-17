<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Run the migrations.
     */
    public function up(): void
    {
        Schema::table('transaksi_kas', function (Blueprint $table) {
            $table->enum('status_approval', ['pending', 'disetujui', 'ditolak'])
                ->default('disetujui')
                ->after('dokumen_transaksi_id');
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::table('transaksi_kas', function (Blueprint $table) {
            $table->dropColumn('status_approval');
        });
    }
};
