<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

/**
 * Item nota bisa menunjuk barang_jasa_id (produk jual) ATAU stok_id (barang
 * produksi). Menambah stok_id supaya nota pembelian bisa mengarahkan item
 * produksi ke tabel Stok, bukan ke BarangJasa (produk penjualan).
 */
return new class extends Migration
{
    public function up(): void
    {
        Schema::table('nota_items', function (Blueprint $table) {
            $table->foreignId('stok_id')->nullable()->after('barang_jasa_id')
                ->constrained('stoks')->nullOnDelete();
        });
    }

    public function down(): void
    {
        Schema::table('nota_items', function (Blueprint $table) {
            $table->dropConstrainedForeignId('stok_id');
        });
    }
};
