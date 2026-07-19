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
        Schema::create('approval_logs', function (Blueprint $table) {
            $table->id();
            $table->foreignId('transaksi_kas_id')
                ->constrained('transaksi_kas')
                ->cascadeOnDelete();
            $table->foreignId('diajukan_oleh')
                ->constrained('users');
            $table->foreignId('disetujui_oleh')
                ->nullable()
                ->constrained('users');
            $table->enum('status', ['pending', 'disetujui', 'ditolak'])
                ->default('pending');
            $table->text('catatan')
                ->nullable();
            $table->timestamp('tanggal_diajukan');
            $table->timestamp('tanggal_diproses')
                ->nullable();
            $table->timestamps();

            // Index untuk query approval pending
            $table->index(['status', 'created_at']);
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('approval_logs');
    }
};
