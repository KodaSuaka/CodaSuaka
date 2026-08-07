<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Riwayat mutasi stok (masuk / keluar / penyesuaian).
     * Menyimpan snapshot stok sebelum & sesudah untuk audit trail.
     */
    public function up(): void
    {
        Schema::create('stok_mutations', function (Blueprint $table) {
            $table->id();
            $table->foreignUuid('instansi_id')->constrained()->cascadeOnDelete();
            $table->foreignId('stok_id')->constrained()->cascadeOnDelete();
            $table->enum('jenis', ['masuk', 'keluar', 'penyesuaian']);
            $table->decimal('jumlah', 15, 2);
            $table->decimal('stok_sebelum', 15, 2);
            $table->decimal('stok_sesudah', 15, 2);
            $table->string('keterangan')->nullable();
            $table->foreignId('user_id')->nullable()->constrained()->nullOnDelete();
            $table->timestamps();

            $table->index('stok_id');
            $table->index('instansi_id');
            $table->index(['instansi_id', 'created_at']);
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('stok_mutations');
    }
};
