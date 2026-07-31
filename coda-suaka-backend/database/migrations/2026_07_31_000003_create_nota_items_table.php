<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('nota_items', function (Blueprint $table) {
            $table->id();
            $table->foreignId('nota_id')->constrained('notas')->cascadeOnDelete();
            $table->foreignId('barang_jasa_id')->nullable()->constrained('barang_jasas')->nullOnDelete();
            $table->string('nama_item', 150);
            $table->enum('jenis', ['barang', 'jasa']);
            $table->decimal('kuantitas', 12, 2);
            $table->string('satuan', 50);
            $table->decimal('harga_satuan', 15, 2);
            $table->decimal('subtotal', 18, 2);
            $table->timestamps();

            $table->index('nota_id');
            $table->index('barang_jasa_id');
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('nota_items');
    }
};
