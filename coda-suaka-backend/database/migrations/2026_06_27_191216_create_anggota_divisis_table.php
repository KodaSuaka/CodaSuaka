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
        Schema::create('anggota_divisis', function (Blueprint $table) {
            $table->id();
            $table->foreignId('divisi_id')->constrained('divisis')->cascadeOnDelete();
            $table->foreignId('karyawan_id')->constrained('karyawans')->cascadeOnDelete();
            $table->unique(['divisi_id', 'karyawan_id']);
            $table->timestamps();
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('anggota_divisis');
    }
};
