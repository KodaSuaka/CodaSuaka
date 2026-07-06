<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('jadwals', function (Blueprint $table) {
            $table->id();
            $table->string('nama_event', 200);
            $table->text('deskripsi')->nullable();
            $table->date('tanggal');
            $table->enum('kategori', ['meeting', 'training', 'event', 'libur', 'lainnya'])->default('lainnya');
            $table->foreignId('outlet_id')->nullable()->constrained('outlets')->cascadeOnDelete();
            $table->foreignId('created_by')->constrained('users')->restrictOnDelete();
            $table->timestamps();
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('jadwals');
    }
};
