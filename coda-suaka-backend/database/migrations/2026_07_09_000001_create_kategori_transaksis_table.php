<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('kategori_transaksis', function (Blueprint $table) {
            $table->id();
            // Nullable: null = template global, terisi = custom milik instansi
            $table->foreignUuid('instansi_id')
                ->nullable()
                ->constrained('instansis')
                ->cascadeOnDelete();
            $table->string('nama_kategori', 150);
            $table->enum('tipe', ['masuk', 'keluar']);
            $table->enum('sifat', ['operasional', 'non_operasional']);
            $table->boolean('termasuk_hpp')->default(false);
            $table->boolean('is_default')->default(false);
            $table->boolean('is_active')->default(true);
            $table->timestamps();

            $table->index('instansi_id');
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('kategori_transaksis');
    }
};
