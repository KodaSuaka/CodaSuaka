<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Buat instansi_id dan created_by nullable pada template_penugasans.
     * Agar template global (tidak terikat instansi/user) bisa dibuat.
     */
    public function up(): void
    {
        Schema::table('template_penugasans', function (Blueprint $table) {
            if (Schema::hasColumn('template_penugasans', 'instansi_id')) {
                $table->uuid('instansi_id')->nullable()->change();
            }
            if (Schema::hasColumn('template_penugasans', 'created_by')) {
                $table->foreignId('created_by')->nullable()->change();
            }
        });
    }

    public function down(): void
    {
        Schema::table('template_penugasans', function (Blueprint $table) {
            if (Schema::hasColumn('template_penugasans', 'instansi_id')) {
                DB::table('template_penugasans')->whereNull('instansi_id')->delete();
                $table->uuid('instansi_id')->change();
            }
            if (Schema::hasColumn('template_penugasans', 'created_by')) {
                DB::table('template_penugasans')->whereNull('created_by')->delete();
                $table->foreignId('created_by')->constrained('users')->restrictOnDelete()->change();
            }
        });
    }
};
