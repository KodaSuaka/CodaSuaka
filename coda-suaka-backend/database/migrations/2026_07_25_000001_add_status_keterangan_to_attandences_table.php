<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::table('attandences', function (Blueprint $table) {
            $table->string('status_keterangan', 50)->nullable()->after('lokasi_checkin')
                ->comment('tepat_waktu, checkin_awal, checkin_terlambat, checkout_awal, checkout_terlambat');
        });
    }

    public function down(): void
    {
        Schema::table('attandences', function (Blueprint $table) {
            $table->dropColumn('status_keterangan');
        });
    }
};
