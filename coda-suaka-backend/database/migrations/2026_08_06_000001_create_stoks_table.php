<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Tabel stok bahan baku / barang produksi.
     * Terpisah dari barang_jasas (barang/jasa yang dijual di kasir).
     * Mengikuti pola tabel barang_jasas (tenant scope instansi_id).
     */
    public function up(): void
    {
        Schema::create('stoks', function (Blueprint $table) {
            $table->id();
            $table->foreignUuid('instansi_id')->constrained()->cascadeOnDelete();
            $table->string('nama', 150);
            $table->string('kategori', 50)->nullable();
            $table->string('satuan', 50)->default('pcs');
            $table->decimal('stok', 15, 2)->default(0);
            $table->decimal('stok_minimum', 15, 2)->nullable();
            $table->decimal('harga_beli', 15, 2)->nullable();
            $table->boolean('is_active')->default(true);
            $table->text('keterangan')->nullable();
            $table->timestamps();

            $table->index('instansi_id');
            $table->index(['instansi_id', 'nama']);
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('stoks');
    }
};
