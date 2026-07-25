<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Merge template_penugasans ke dalam penugasans.
     *
     * Perubahan:
     * 1. Tambah kolom `is_template` (boolean) & `instansi_id` (uuid, nullable) ke penugasans
     * 2. Buat `penanggung_jawab_id` nullable (template tidak punya penanggung jawab)
     * 3. Buat `created_by` nullable (template global tidak punya pembuat)
     * 4. Migrate data dari template_penugasans → penugasans (sebagai template record)
     * 5. Drop kolom `template_penugasan_id` dari penugasans
     * 6. Drop tabel `template_penugasans`
     */
    public function up(): void
    {
        // ── 1. Tambah kolom baru ke penugasans ──────────────────────
        Schema::table('penugasans', function (Blueprint $table) {
            if (!Schema::hasColumn('penugasans', 'is_template')) {
                $table->boolean('is_template')->default(false)->after('created_by');
            }
            if (!Schema::hasColumn('penugasans', 'instansi_id')) {
                $table->uuid('instansi_id')->nullable()->after('is_template');
            }
        });

        // ── 2. Buat penanggung_jawab_id nullable ────────────────────
        if (Schema::hasColumn('penugasans', 'penanggung_jawab_id')) {
            // Drop foreign key constraint menggunakan raw SQL
            $constraints = $this->getForeignKeyConstraints('penugasans', 'penanggung_jawab_id');
            foreach ($constraints as $constraintName) {
                Schema::table('penugasans', function (Blueprint $table) use ($constraintName) {
                    $table->dropForeign($constraintName);
                });
            }

            // Ubah kolom menjadi nullable
            DB::statement('ALTER TABLE penugasans MODIFY penanggung_jawab_id CHAR(36) NULL');

            // Re-add foreign key
            Schema::table('penugasans', function (Blueprint $table) {
                $table->foreign('penanggung_jawab_id')
                    ->references('id')
                    ->on('karyawans')
                    ->cascadeOnDelete();
            });
        }

        // ── 3. Buat created_by nullable ────────────────────────────
        if (Schema::hasColumn('penugasans', 'created_by')) {
            $constraints = $this->getForeignKeyConstraints('penugasans', 'created_by');
            foreach ($constraints as $constraintName) {
                Schema::table('penugasans', function (Blueprint $table) use ($constraintName) {
                    $table->dropForeign($constraintName);
                });
            }

            // Ubah kolom menjadi nullable
            DB::statement('ALTER TABLE penugasans MODIFY created_by BIGINT UNSIGNED NULL');

            // Re-add foreign key
            Schema::table('penugasans', function (Blueprint $table) {
                $table->foreign('created_by')
                    ->references('id')
                    ->on('users')
                    ->restrictOnDelete();
            });
        }

        // ── 4. Migrate data template_penugasans → penugasans ─────────
        if (Schema::hasTable('template_penugasans')) {
            $templates = DB::table('template_penugasans')->get();
            foreach ($templates as $t) {
                DB::table('penugasans')->insert([
                    'judul'               => $t->nama_template,
                    'deskripsi'           => $t->deskripsi_template,
                    'penanggung_jawab_id' => null,
                    'divisi_id'           => null,
                    'tenggat'             => null,
                    'status'              => 'belum',
                    'urgency'             => $t->urgency_default ?? 'sedang',
                    'poin'                => $t->poin_default ?? 0,
                    'is_template'         => true,
                    'instansi_id'         => $t->instansi_id,
                    'created_by'          => $t->created_by,
                    'created_at'          => $t->created_at ?? now(),
                    'updated_at'          => $t->updated_at ?? now(),
                ]);
            }
        }

        // ── 5. Drop kolom template_penugasan_id ─────────────────────
        if (Schema::hasColumn('penugasans', 'template_penugasan_id')) {
            $constraints = $this->getForeignKeyConstraints('penugasans', 'template_penugasan_id');
            foreach ($constraints as $constraintName) {
                Schema::table('penugasans', function (Blueprint $table) use ($constraintName) {
                    $table->dropForeign($constraintName);
                });
            }
            Schema::table('penugasans', function (Blueprint $table) {
                $table->dropColumn('template_penugasan_id');
            });
        }

        // ── 6. Add index untuk query template ───────────────────────
        Schema::table('penugasans', function (Blueprint $table) {
            if (!Schema::hasIndex('penugasans', 'idx_penugasans_is_template')) {
                $table->index('is_template', 'idx_penugasans_is_template');
            }
            if (!Schema::hasIndex('penugasans', 'idx_penugasans_instansi_id')) {
                $table->index('instansi_id', 'idx_penugasans_instansi_id');
            }
        });

        // ── 7. Drop tabel template_penugasans ────────────────────────
        Schema::dropIfExists('template_penugasans');
    }

    public function down(): void
    {
        // Recreate template_penugasans table
        Schema::create('template_penugasans', function (Blueprint $table) {
            $table->id();
            $table->string('nama_template', 100);
            $table->text('deskripsi_template')->nullable();
            $table->enum('urgency_default', ['urgent', 'sedang', 'rendah'])->default('sedang');
            $table->integer('poin_default')->default(0);
            $table->uuid('instansi_id')->nullable();
            $table->foreign('instansi_id')->references('id')->on('instansis')->cascadeOnDelete();
            $table->foreignId('created_by')->nullable()->constrained('users')->restrictOnDelete();
            $table->timestamps();
            $table->index('instansi_id');
        });

        // Migrate template records back
        $templates = DB::table('penugasans')->where('is_template', true)->get();
        foreach ($templates as $row) {
            DB::table('template_penugasans')->insert([
                'nama_template'      => $row->judul,
                'deskripsi_template' => $row->deskripsi,
                'urgency_default'    => $row->urgency,
                'poin_default'       => $row->poin,
                'instansi_id'        => $row->instansi_id,
                'created_by'         => $row->created_by,
                'created_at'         => $row->created_at,
                'updated_at'         => $row->updated_at,
            ]);
        }

        // Remove template records from penugasans
        DB::table('penugasans')->where('is_template', true)->delete();

        // Drop new columns
        Schema::table('penugasans', function (Blueprint $table) {
            if (Schema::hasIndex('penugasans', 'idx_penugasans_is_template')) {
                $table->dropIndex('idx_penugasans_is_template');
            }
            if (Schema::hasIndex('penugasans', 'idx_penugasans_instansi_id')) {
                $table->dropIndex('idx_penugasans_instansi_id');
            }
            $table->dropColumn(['is_template', 'instansi_id']);
        });

        // Re-add template_penugasan_id
        Schema::table('penugasans', function (Blueprint $table) {
            $table->foreignId('template_penugasan_id')
                ->nullable()
                ->after('created_by')
                ->constrained('template_penugasans')
                ->nullOnDelete();
        });

        // Make penanggung_jawab_id NOT NULL again
        $constraints = $this->getForeignKeyConstraints('penugasans', 'penanggung_jawab_id');
        foreach ($constraints as $constraintName) {
            Schema::table('penugasans', function (Blueprint $table) use ($constraintName) {
                $table->dropForeign($constraintName);
            });
        }
        DB::statement('ALTER TABLE penugasans MODIFY penanggung_jawab_id CHAR(36) NOT NULL');
        Schema::table('penugasans', function (Blueprint $table) {
            $table->foreign('penanggung_jawab_id')
                ->references('id')
                ->on('karyawans')
                ->cascadeOnDelete();
        });

        // Make created_by NOT NULL again
        $constraints = $this->getForeignKeyConstraints('penugasans', 'created_by');
        foreach ($constraints as $constraintName) {
            Schema::table('penugasans', function (Blueprint $table) use ($constraintName) {
                $table->dropForeign($constraintName);
            });
        }
        DB::statement('ALTER TABLE penugasans MODIFY created_by BIGINT UNSIGNED NOT NULL');
        Schema::table('penugasans', function (Blueprint $table) {
            $table->foreign('created_by')
                ->references('id')
                ->on('users')
                ->restrictOnDelete();
        });
    }

    /**
     * Helper: get foreign key constraint names for a specific column.
     */
    private function getForeignKeyConstraints(string $table, string $column): array
    {
        $database = DB::getDatabaseName();
        $results = DB::select("
            SELECT CONSTRAINT_NAME
            FROM information_schema.KEY_COLUMN_USAGE
            WHERE TABLE_SCHEMA = ?
              AND TABLE_NAME = ?
              AND COLUMN_NAME = ?
              AND REFERENCED_TABLE_NAME IS NOT NULL
        ", [$database, $table, $column]);

        return array_column($results, 'CONSTRAINT_NAME');
    }
};
