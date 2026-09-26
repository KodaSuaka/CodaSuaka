<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Support\Facades\DB;

return new class extends Migration
{
    /**
     * SUG-3: tambah status 'menunggu_validasi' — karyawan menyelesaikan tugas
     * masuk status ini dulu, poin baru diberikan setelah pemilik memvalidasi.
     */
    public function up(): void
    {
        DB::statement("ALTER TABLE penugasans MODIFY status ENUM('belum', 'proses', 'menunggu_validasi', 'selesai', 'batal') DEFAULT 'belum'");
    }

    public function down(): void
    {
        DB::statement("UPDATE penugasans SET status = 'proses' WHERE status = 'menunggu_validasi'");
        DB::statement("ALTER TABLE penugasans MODIFY status ENUM('belum', 'proses', 'selesai', 'batal') DEFAULT 'belum'");
    }
};
