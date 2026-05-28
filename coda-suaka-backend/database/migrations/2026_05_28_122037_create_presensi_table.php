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
        Schema::create('presensi', function (Blueprint $table) {
            $table->uuid('id')->primary();
            $table->uuid('id_karyawan');
            
            $table->date('tanggal');
            $table->time('hadir');
            $table->time('kembali')->nullable(); // Nullable karena diisi pas pulang
            $table->enum('status', ['hadir', 'sakit', 'alpa'])->default('alpa');
            $table->string('koordinat')->nullable();
            
            $table->timestamp('created_at')->useCurrent();

            // Foreign key ke tabel karyawan
            $table->foreign('id_karyawan')->references('id')->on('karyawan')->onDelete('cascade');
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('presensi');
    }
};
