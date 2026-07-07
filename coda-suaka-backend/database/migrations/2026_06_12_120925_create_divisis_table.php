<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('divisis', function (Blueprint $table) {
            $table->id();
            $table->string('nama_divisi', 100);
            $table->text('deskripsi')->nullable();
            $table->foreignUuid('ketua_karyawan_id')->nullable()->constrained('karyawans')->nullOnDelete();
            $table->foreignId('outlet_id')->constrained('outlets')->cascadeOnDelete();
            $table->timestamps();
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('divisis');
    }
};
