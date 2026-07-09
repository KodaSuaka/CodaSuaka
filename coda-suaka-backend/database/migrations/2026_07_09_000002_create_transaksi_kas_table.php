<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('transaksi_kas', function (Blueprint $table) {
            $table->id();
            $table->foreignId('instansi_id')->constrained('instansis')->cascadeOnDelete();
            $table->foreignId('outlet_id')->nullable()->constrained('outlets')->nullOnDelete();
            $table->foreignId('kategori_transaksi_id')->nullable()->constrained('kategori_transaksis')->nullOnDelete();
            $table->date('tanggal');
            $table->enum('tipe', ['masuk', 'keluar']);
            $table->decimal('nominal', 18, 2);
            $table->string('metode_pembayaran', 100)->nullable();
            $table->text('keterangan')->nullable();
            $table->string('lampiran_url', 255)->nullable();
            // dokumen_transaksi_id akan ditambahkan setelah tabel dokumen_transaksis dibuat (Fase 3-4)
            // $table->foreignId('dokumen_transaksi_id')->nullable()->constrained('dokumen_transaksis')->nullOnDelete();
            $table->unsignedBigInteger('dokumen_transaksi_id')->nullable();
            $table->foreignId('created_by')->nullable()->constrained('users')->nullOnDelete();
            $table->timestamps();

            $table->index('instansi_id');
            $table->index('outlet_id');
            $table->index('tanggal');
            $table->index(['instansi_id', 'tanggal']);
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('transaksi_kas');
    }
};
