<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Buat instansi_id nullable pada template_penugasans.
     * Template global (instansi_id = NULL) akan dilihat oleh semua instansi.
     */
    public function up(): void
    {
        Schema::table('template_penugasans', function (Blueprint $table) {
            if (Schema::hasColumn('template_penugasans', 'instansi_id')) {
                $table->uuid('instansi_id')->nullable()->change();
            }
        });
    }

    public function down(): void
    {
        Schema::table('template_penugasans', function (Blueprint $table) {
            if (Schema::hasColumn('template_penugasans', 'instansi_id')) {
                // Hapus baris NULL sebelum mengembalikan ke NOT NULL
                DB::table('template_penugasans')->whereNull('instansi_id')->delete();
                $table->uuid('instansi_id')->change();
            }
        });
    }
};
