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
        Schema::create('penugasan_karyawan', function (Blueprint $table) {
            $table->uuid('id')->primary();
            $table->uuid('id_karyawan');
            $table->uuid('id_master_tugas');
            
            $table->date('tanggal_tugas');
            $table->enum('status', ['menunggu', 'dikerjakan', 'selesai'])->default('menunggu');
            $table->string('catatan_tambahan')->nullable();
            $table->timestamp('waktu_diselesaikan')->nullable();

            $table->foreign('id_karyawan')->references('id')->on('karyawan')->onDelete('cascade');
            $table->foreign('id_master_tugas')->references('id')->on('tugas')->onDelete('cascade');
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('penugasan_karyawan');
    }
};
