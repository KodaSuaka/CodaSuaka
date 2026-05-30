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
        Schema::create('Toko', function (Blueprint $table) {
            $table->uuid('id')->primary();
            $table->uuid('id_pemilik');
            
            $table->string('nama');
            $table->string('slug')->unique();
            
            // Kolom alamat dibuat nullable untuk mode MVP (bisa diisi nanti)
            $table->string('provinsi')->nullable();
            $table->string('kota')->nullable();
            $table->string('kecamatan')->nullable();
            $table->string('desa')->nullable();
            $table->text('alamat_lengkap')->nullable();
            
            // Koordinat juga nullable
            $table->decimal('latitude', 10, 8)->nullable();
            $table->decimal('longtitude', 11, 8)->nullable();
            
            $table->enum('berlangganan', ['gratis', 'berbayar'])->default('gratis');
            $table->timestamp('created_at')->useCurrent();

            // Relasi Foreign Key ke tabel pemilik
            $table->foreign('id_pemilik')->references('id')->on('pemilik')->onDelete('cascade');
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('toko');
    }
};
