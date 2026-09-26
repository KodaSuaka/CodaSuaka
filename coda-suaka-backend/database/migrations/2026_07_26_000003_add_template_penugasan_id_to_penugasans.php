<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Re-add template_penugasan_id setelah migration 000005 menghapusnya
     * bersamaan dengan drop tabel template_penugasans.
     *
     * Kolom ini nullable tanpa FK constraint — hanya untuk tracking
     * task mana yang berasal dari template mana.
     */
    public function up(): void
    {
        Schema::table('penugasans', function (Blueprint $table) {
            if (! Schema::hasColumn('penugasans', 'template_penugasan_id')) {
                $table->unsignedBigInteger('template_penugasan_id')->nullable()->after('is_template');
            }
        });
    }

    public function down(): void
    {
        Schema::table('penugasans', function (Blueprint $table) {
            if (Schema::hasColumn('penugasans', 'template_penugasan_id')) {
                $table->dropColumn('template_penugasan_id');
            }
        });
    }
};
