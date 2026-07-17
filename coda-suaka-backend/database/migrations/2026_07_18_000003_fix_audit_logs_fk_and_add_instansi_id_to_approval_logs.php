<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Run the migrations.
     *
     * 1. Fix audit_logs.instansi_id — tambah FK constraint ke tabel instansis
     * 2. Tambah kolom instansi_id di approval_logs untuk tenant isolation
     */
    public function up(): void
    {
        // Fix 1: Pastikan instansi_id di audit_logs bisa dijadikan foreign key
        // Ubah kolom dari string(36) menjadi foreignUuid, lalu tambah constraint
        Schema::table('audit_logs', function (Blueprint $table) {
            // Hapus index lama jika ada (karena type column berubah)
            // Drop instansi_id dulu
            $table->dropColumn('instansi_id');
        });

        Schema::table('audit_logs', function (Blueprint $table) {
            // Tambah ulang sebagai foreignUuid
            $table->foreignUuid('instansi_id')
                ->nullable()
                ->constrained('instansis', 'id')
                ->cascadeOnDelete()
                ->after('id');
        });

        // Fix 2: Tambah kolom instansi_id di approval_logs
        Schema::table('approval_logs', function (Blueprint $table) {
            $table->foreignUuid('instansi_id')
                ->nullable()
                ->constrained('instansis', 'id')
                ->cascadeOnDelete()
                ->after('transaksi_kas_id');

            // Index untuk query tenant isolation
            $table->index('instansi_id');
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        // Kembalikan audit_logs.instansi_id ke string(36)
        Schema::table('audit_logs', function (Blueprint $table) {
            $table->dropForeign(['instansi_id']);
            $table->dropColumn('instansi_id');
        });

        Schema::table('audit_logs', function (Blueprint $table) {
            $table->string('instansi_id', 36)->nullable()->after('id');
        });

        // Hapus kolom instansi_id dari approval_logs
        Schema::table('approval_logs', function (Blueprint $table) {
            $table->dropForeign(['instansi_id']);
            $table->dropIndex(['instansi_id']);
            $table->dropColumn('instansi_id');
        });
    }
};
