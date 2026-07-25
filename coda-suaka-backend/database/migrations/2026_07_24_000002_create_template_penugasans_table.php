<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Tabel template tugas dinamis untuk berbagai jenis UMKM.
     * Maksimal 10 template per instansi.
     *
     * MySQL 5.7.44 compatible: tanpa CHECK constraint, tanpa DEFAULT pada TEXT.
     */
    public function up(): void
    {
        if (Schema::hasTable('template_penugasans')) {
            return;
        }

        Schema::create('template_penugasans', function (Blueprint $table) {
            $table->id();
            $table->string('nama_template', 100);
            $table->text('deskripsi_template')->nullable();
            $table->enum('urgency_default', ['urgent', 'sedang', 'rendah'])->default('sedang');
            $table->integer('poin_default')->default(0);
            $table->foreignId('instansi_id')->constrained('instansis')->cascadeOnDelete();
            $table->foreignId('created_by')->constrained('users')->restrictOnDelete();
            $table->timestamps();

            // Index untuk query cepat per instansi
            $table->index('instansi_id', 'idx_template_penugasans_instansi_id');
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('template_penugasans');
    }
};
