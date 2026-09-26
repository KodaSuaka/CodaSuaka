<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * SUG-4: tanggal mulai kerja karyawan. "Masa kerja" dihitung on-the-fly
     * dari tanggal ini (bukan disimpan terpisah, supaya tidak ada data
     * turunan yang bisa basi).
     */
    public function up(): void
    {
        Schema::table('karyawans', function (Blueprint $table) {
            $table->date('tanggal_mulai_kerja')->nullable()->after('alamat');
        });
    }

    public function down(): void
    {
        Schema::table('karyawans', function (Blueprint $table) {
            $table->dropColumn('tanggal_mulai_kerja');
        });
    }
};
