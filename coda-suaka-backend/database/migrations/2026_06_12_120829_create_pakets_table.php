<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('pakets', function (Blueprint $table) {
            $table->id();
            $table->string('nama_paket');
            $table->decimal('harga', 15, 2)->default(0);
            $table->text('deskripsi')->nullable();
            $table->text('fitur')->nullable()->comment('JSON: daftar fitur yang termasuk');
            $table->integer('durasi_hari')->default(30);
            $table->integer('max_outlet')->nullable();
            $table->integer('max_karyawan_per_outlet')->nullable();
            $table->boolean('is_active')->default(true);
            $table->timestamps();
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('pakets');
    }
};
