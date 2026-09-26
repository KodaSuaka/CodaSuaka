<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Tambah kolom workflow ke tabel penugasans:
     * - accepted_at: timestamp kapan tugas diterima/dikerjakan
     * - completed_at: timestamp kapan tugas selesai
     * - status_changed_by: user_id yang mengubah status
     *
     * MySQL 5.7.44 compatible: menggunakan nullable timestamp + nullable unsignedBigInteger.
     */
    public function up(): void
    {
        Schema::table('penugasans', function (Blueprint $table) {
            if (! Schema::hasColumn('penugasans', 'accepted_at')) {
                $table->timestamp('accepted_at')->nullable()->after('poin');
            }
            if (! Schema::hasColumn('penugasans', 'completed_at')) {
                $table->timestamp('completed_at')->nullable()->after('accepted_at');
            }
            if (! Schema::hasColumn('penugasans', 'status_changed_by')) {
                $table->unsignedBigInteger('status_changed_by')->nullable()->after('completed_at');
            }
        });
    }

    public function down(): void
    {
        Schema::table('penugasans', function (Blueprint $table) {
            if (Schema::hasColumn('penugasans', 'accepted_at')) {
                $table->dropColumn('accepted_at');
            }
            if (Schema::hasColumn('penugasans', 'completed_at')) {
                $table->dropColumn('completed_at');
            }
            if (Schema::hasColumn('penugasans', 'status_changed_by')) {
                $table->dropColumn('status_changed_by');
            }
        });
    }
};
