<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('notas', function (Blueprint $table) {
            $table->id();
            $table->foreignUuid('instansi_id')->constrained('instansis')->cascadeOnDelete();
            $table->foreignId('outlet_id')->nullable()->constrained('outlets')->nullOnDelete();
            $table->foreignId('kategori_transaksi_id')->nullable()->constrained('kategori_transaksis')->nullOnDelete();
            $table->foreignId('transaksi_kas_id')->nullable()->constrained('transaksi_kas')->nullOnDelete();
            $table->enum('tipe', ['penjualan', 'pembelian']);
            $table->string('nomor_nota', 50);
            $table->date('tanggal');
            $table->string('pihak_terkait', 150)->nullable();
            $table->string('metode_pembayaran', 100)->nullable();
            $table->decimal('total', 18, 2)->default(0);
            $table->enum('status', ['selesai', 'dibatalkan'])->default('selesai');
            $table->string('lampiran_url', 255)->nullable();
            $table->text('catatan')->nullable();
            $table->foreignId('created_by')->nullable()->constrained('users')->nullOnDelete();
            $table->timestamps();

            $table->index('instansi_id');
            $table->index('outlet_id');
            $table->index('tanggal');
            $table->index(['instansi_id', 'tipe', 'tanggal']);
            $table->unique(['instansi_id', 'nomor_nota']);
        });

        // FK yang sengaja ditunda saat transaksi_kas dibuat (lihat komentar di
        // migrasi 2026_07_09_000002_create_transaksi_kas_table.php) — dipasang
        // sekarang karena tabel notas baru saja ada.
        Schema::table('transaksi_kas', function (Blueprint $table) {
            $table->foreign('dokumen_transaksi_id')->references('id')->on('notas')->nullOnDelete();
        });
    }

    public function down(): void
    {
        Schema::table('transaksi_kas', function (Blueprint $table) {
            $table->dropForeign(['dokumen_transaksi_id']);
        });
        Schema::dropIfExists('notas');
    }
};
