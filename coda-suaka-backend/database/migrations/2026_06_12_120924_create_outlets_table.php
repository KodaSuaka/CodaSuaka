<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('outlets', function (Blueprint $table) {
            $table->id();
            $table->string('nama_outlet', 150);
            $table->text('alamat_outlet')->nullable();
            $table->foreignUuid('instansi_id')->constrained('instansis')->cascadeOnDelete();
            $table->boolean('is_active')->default(true);
            $table->timestamps();
        });

        // ─── Foreign keys untuk users & karyawans ─────────────
        // Tabel users dibuat duluan (0001_01_01), jadi FK-nya baru
        // bisa ditambahkan setelah semua tabel referensi siap.
        DB::statement('UPDATE users SET role_id = NULL WHERE role_id IS NOT NULL AND role_id NOT IN (SELECT id FROM roles)');
        DB::statement('UPDATE users SET instansi_id = NULL WHERE instansi_id IS NOT NULL AND instansi_id NOT IN (SELECT id FROM instansis)');
        DB::statement('UPDATE users SET outlet_id = NULL WHERE outlet_id IS NOT NULL AND outlet_id NOT IN (SELECT id FROM outlets)');
        DB::statement('UPDATE karyawans SET outlet_id = NULL WHERE outlet_id IS NOT NULL AND outlet_id NOT IN (SELECT id FROM outlets)');

        Schema::table('users', function (Blueprint $table) {
            $table->foreign('role_id')->references('id')->on('roles')->nullOnDelete();
            $table->foreign('instansi_id')->references('id')->on('instansis')->nullOnDelete();
            $table->foreign('outlet_id')->references('id')->on('outlets')->nullOnDelete();
        });

        Schema::table('karyawans', function (Blueprint $table) {
            $table->foreign('outlet_id')->references('id')->on('outlets')->nullOnDelete();
        });
    }

    public function down(): void
    {
        Schema::table('users', function (Blueprint $table) {
            $table->dropForeign(['role_id']);
            $table->dropForeign(['instansi_id']);
            $table->dropForeign(['outlet_id']);
        });

        Schema::table('karyawans', function (Blueprint $table) {
            $table->dropForeign(['outlet_id']);
        });

        Schema::dropIfExists('outlets');
    }
};
