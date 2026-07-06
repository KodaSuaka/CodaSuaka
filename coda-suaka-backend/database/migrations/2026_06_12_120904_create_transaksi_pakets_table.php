<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('transaksi_pakets', function (Blueprint $table) {
            $table->id();
            $table->foreignUuid('instansi_id')->constrained('instansis')->cascadeOnDelete();
            $table->foreignId('paket_id')->constrained('pakets')->restrictOnDelete();
            $table->date('tanggal_mulai');
            $table->date('tanggal_berakhir')->nullable();
            $table->decimal('total_harga', 15, 2)->default(0);
            $table->enum('status', ['pending', 'aktif', 'kedaluwarsa', 'dibatalkan'])->default('pending');
            $table->string('bukti_pembayaran')->nullable();
            $table->timestamps();
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('transaksi_pakets');
    }
};
