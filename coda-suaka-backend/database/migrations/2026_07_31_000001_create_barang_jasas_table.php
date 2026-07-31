<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('barang_jasas', function (Blueprint $table) {
            $table->id();
            $table->foreignUuid('instansi_id')->constrained('instansis')->cascadeOnDelete();
            $table->string('nama', 150);
            $table->enum('jenis', ['barang', 'jasa']);
            $table->string('satuan', 50)->default('pcs');
            $table->decimal('harga_jual', 15, 2);
            $table->decimal('harga_beli', 15, 2)->nullable();
            $table->integer('stok')->nullable(); // null untuk jenis=jasa (tidak dilacak stoknya)
            $table->boolean('is_active')->default(true);
            $table->text('keterangan')->nullable();
            $table->timestamps();

            $table->index('instansi_id');
            $table->index(['instansi_id', 'jenis']);
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('barang_jasas');
    }
};
