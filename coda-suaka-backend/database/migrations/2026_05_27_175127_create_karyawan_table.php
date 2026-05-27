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
        Schema::create('karyawan', function (Blueprint $table) {
            $table->uuid('id')->primary();
            $table->uuid('id_pengguna');
            $table->uuid('id_Toko');
            
            $table->string('nama_lengkap');
            $table->enum('posisi', ['kasir', 'pramuniaga']);
            $table->enum('status_kerja', ['bebas', 'bekerja', 'libur'])->default('bebas');
            $table->integer('poin_performa')->default(0);
            
            // Sesuai ERD menggunakan tanggal_bergabung
            $table->timestamp('tanggal_bergabung')->useCurrent();

            // Relasi Foreign Key
            $table->foreign('id_pengguna')->references('id')->on('users')->onDelete('cascade');
            $table->foreign('id_Toko')->references('id')->on('Toko')->onDelete('cascade');
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('karyawan');
    }
};
