<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Tambah kolom urgency (enum: urgent/sedang/rendah) dan poin (integer)
     * ke tabel penugasans untuk sistem poin kinerja.
     *
     * MySQL 5.7.44 compatible: menggunakan enum + integer, tanpa CHECK constraint.
     */
    public function up(): void
    {
        Schema::table('penugasans', function (Blueprint $table) {
            if (! Schema::hasColumn('penugasans', 'urgency')) {
                $table->enum('urgency', ['urgent', 'sedang', 'rendah'])->default('sedang')->after('status');
            }
            if (! Schema::hasColumn('penugasans', 'poin')) {
                $table->integer('poin')->default(0)->after('urgency');
            }
        });
    }

    public function down(): void
    {
        Schema::table('penugasans', function (Blueprint $table) {
            if (Schema::hasColumn('penugasans', 'urgency')) {
                $table->dropColumn('urgency');
            }
            if (Schema::hasColumn('penugasans', 'poin')) {
                $table->dropColumn('poin');
            }
        });
    }
};
