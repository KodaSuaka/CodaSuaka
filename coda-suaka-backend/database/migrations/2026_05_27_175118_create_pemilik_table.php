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
        Schema::create('pemilik', function (Blueprint $table) {
            $table->uuid('id')->primary();
            $table->uuid('id_pengguna');
            
            $table->string('nama_lengkap');
            // Dibuat nullable agar form pendaftaran awal tidak ribet
            $table->string('nik_ktp')->nullable();
            $table->string('akun_bank')->nullable();
            $table->string('nama_bank')->nullable();
            
            $table->timestamp('created_at')->useCurrent();

            // Relasi Foreign Key ke tabel users
            $table->foreign('id_pengguna')->references('id')->on('users')->onDelete('cascade');
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('pemilik');
    }
};
