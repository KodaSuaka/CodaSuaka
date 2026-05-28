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
        Schema::create('tugas', function (Blueprint $table) {
            $table->uuid('id')->primary();
            $table->uuid('id_Toko')->nullable();
            
            $table->string('nama_tugas');
            $table->integer('poin');
            
            $table->timestamp('created_at')->useCurrent();
            
            $table->foreign('id_Toko')->references('id')->on('Toko')->onDelete('cascade');
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('tugas');
    }
};
