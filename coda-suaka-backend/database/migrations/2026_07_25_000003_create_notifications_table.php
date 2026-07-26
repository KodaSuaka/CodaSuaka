<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('notifications', function (Blueprint $table) {
            $table->id();
            $table->unsignedBigInteger('user_id'); // Penerima notifikasi
            $table->string('type'); // Jenis notifikasi: pengajuan, penugasan, presensi, keuangan, sistem
            $table->string('title'); // Judul notifikasi
            $table->text('body'); // Isi notifikasi
            $table->string('icon')->default('Bell'); // Icon untuk UI
            $table->string('color')->default('#6366F1'); // Warna notifikasi
            $table->boolean('is_read')->default(false); // Status baca
            $table->unsignedBigInteger('related_id')->nullable(); // ID terkait (pengajuan_id, penugasan_id, dll)
            $table->string('related_type')->nullable(); // Tipe model terkait
            $table->timestamps();

            // Index untuk performa
            $table->index(['user_id', 'is_read']);
            $table->index(['user_id', 'created_at']);
            $table->index(['type']);
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('notifications');
    }
};
