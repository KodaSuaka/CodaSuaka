<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Log trace setiap request API per user (untuk audit/forensik karyawan).
     * Disimpan di database, TIDAK ditampilkan di aplikasi — hanya super admin.
     */
    public function up(): void
    {
        Schema::create('request_logs', function (Blueprint $table) {
            $table->id();
            $table->string('instansi_id', 36)->nullable();
            $table->foreignId('user_id')->nullable()->constrained()->nullOnDelete();
            $table->string('method', 10);
            $table->string('path', 255);
            $table->string('full_url', 500)->nullable();
            $table->json('query_params')->nullable();
            $table->json('request_body')->nullable();
            $table->string('ip_address', 45)->nullable();
            $table->string('user_agent', 500)->nullable();
            $table->unsignedSmallInteger('status_code')->nullable();
            $table->unsignedInteger('duration_ms')->nullable();
            $table->timestamps();

            $table->index('instansi_id');
            $table->index('user_id');
            $table->index('method');
            $table->index('path');
            $table->index('status_code');
            $table->index('created_at');
            $table->index(['instansi_id', 'created_at']);
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('request_logs');
    }
};
