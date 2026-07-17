<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('audit_logs', function (Blueprint $table) {
            $table->id();
            $table->string('instansi_id', 36)->nullable();
            $table->string('auditable_type', 255);      // model class
            $table->unsignedBigInteger('auditable_id');  // model PK
            $table->string('event');                     // created / updated / deleted / approved / rejected
            $table->json('old_values')->nullable();      // snapshot sebelum
            $table->json('new_values')->nullable();      // snapshot sesudah
            $table->unsignedBigInteger('user_id')->nullable(); // pelaku
            $table->string('ip_address', 45)->nullable();
            $table->string('user_agent', 500)->nullable();
            $table->timestamps();

            $table->index(['auditable_type', 'auditable_id']);
            $table->index('event');
            $table->index('created_at');
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('audit_logs');
    }
};
