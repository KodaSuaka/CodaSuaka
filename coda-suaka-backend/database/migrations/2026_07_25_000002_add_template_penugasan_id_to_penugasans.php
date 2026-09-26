<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Tambah kolom template_penugasan_id ke tabel penugasans
     * untuk membedakan tugas harian (dari template) vs tugas khusus (dari pemilik).
     *
     * MySQL 5.7.44 compatible.
     */
    public function up(): void
    {
        Schema::table('penugasans', function (Blueprint $table) {
            if (! Schema::hasColumn('penugasans', 'template_penugasan_id')) {
                $table->foreignId('template_penugasan_id')
                    ->nullable()
                    ->after('created_by')
                    ->constrained('template_penugasans')
                    ->nullOnDelete();
            }
        });
    }

    public function down(): void
    {
        Schema::table('penugasans', function (Blueprint $table) {
            if (Schema::hasColumn('penugasans', 'template_penugasan_id')) {
                $table->dropForeign(['template_penugasan_id']);
                $table->dropColumn('template_penugasan_id');
            }
        });
    }
};
