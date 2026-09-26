<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Tambah kolom jam operasional ke tabel instansis.
     * Format JSON: {"jam_buka": "08:00", "jam_tutup": "17:00", "hari_operasional": [1,2,3,4,5,6]}
     *
     * MySQL 5.7.44 compatible: menggunakan JSON + text, tanpa CHECK constraint.
     */
    public function up(): void
    {
        Schema::table('instansis', function (Blueprint $table) {
            if (! Schema::hasColumn('instansis', 'jam_operasional')) {
                $table->json('jam_operasional')->nullable()->after('timezone');
            }
        });
    }

    public function down(): void
    {
        Schema::table('instansis', function (Blueprint $table) {
            if (Schema::hasColumn('instansis', 'jam_operasional')) {
                $table->dropColumn('jam_operasional');
            }
        });
    }
};
