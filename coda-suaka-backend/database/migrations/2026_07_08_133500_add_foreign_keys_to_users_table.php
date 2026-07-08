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
        // Add foreign keys to users table (tables created after users)
        Schema::table('users', function (Blueprint $table) {
            $table->foreign('role_id')->references('id')->on('roles')->nullOnDelete();
            $table->foreign('instansi_id')->references('id')->on('instansis')->nullOnDelete();
            $table->foreign('outlet_id')->references('id')->on('outlets')->nullOnDelete();
        });

        // Add foreign key to karyawans.outlet_id (outlets created after karyawans)
        Schema::table('karyawans', function (Blueprint $table) {
            $table->foreign('outlet_id')->references('id')->on('outlets')->nullOnDelete();
        });
    }

    /**
     * Reverse the migrations.
     */
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
    }
};
