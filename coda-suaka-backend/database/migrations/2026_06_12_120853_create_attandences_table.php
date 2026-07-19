<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('attandences', function (Blueprint $table) {
            $table->id();
            $table->foreignId('user_id')->constrained('users')->cascadeOnDelete();
            $table->date('tanggal');
            $table->time('jam_checkin')->nullable();
            $table->time('jam_checkout')->nullable();
            $table->enum('status', ['hadir', 'izin', 'sakit', 'alpha', 'cuti'])->default('hadir');
            $table->text('keterangan')->nullable();
            $table->string('lokasi_checkin')->nullable()->comment('Koordinat GPS saat checkin');
            $table->timestamps();
            $table->unique(['user_id', 'tanggal']);
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('attandences');
    }
};
