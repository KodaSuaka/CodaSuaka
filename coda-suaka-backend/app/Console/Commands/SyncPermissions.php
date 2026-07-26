<?php

namespace App\Console\Commands;

use App\Models\role;
use App\Models\role_permission;
use Illuminate\Console\Command;
use Illuminate\Support\Facades\DB;

/**
 * ══════════════════════════════════════════════════════════════════════════════
 *  SyncPermissions — Sinkronisasi Permission dari Config ke Database
 * ══════════════════════════════════════════════════════════════════════════════
 *
 *  Command ini membaca config/permissions.php lalu menyinkronkan
 *  ke tabel role_permissions di database.
 *
 *  Fitur:
 *    - Tambah permission baru yang belum ada di DB
 *    - Hapus permission yang sudah tidak ada di config
 *    - Tampilkan ringkasan perubahan sebelum apply
 *    - Mode --dry-run untuk preview tanpa ubah DB
 *    - Mode --force untuk skip konfirmasi
 *
 *  Usage:
 *    php artisan permission:sync              # Interactive (with confirmation)
 *    php artisan permission:sync --dry-run    # Preview only, no DB changes
 *    php artisan permission:sync --force      # Skip confirmation prompt
 *    php artisan permission:sync --detail     # Show detailed per-role diff
 * ══════════════════════════════════════════════════════════════════════════════
 */
class SyncPermissions extends Command
{
    protected $signature = 'permission:sync
                            {--dry-run   : Tampilkan perubahan tanpa menyimpan ke database}
                            {--force     : Lewati konfirmasi sebelum apply}
                            {--detail    : Tampilkan detail diff per role}';

    protected $description = 'Sinkronisasi permission dari config/permissions.php ke database role_permissions';

    /**
     * All valid permission IDs from registry.
     */
    private array $registryPermissions = [];

    /**
     * Diff summary: role_id => ['add' => [...], 'remove' => [...]]
     */
    private array $diff = [];

    public function handle(): int
    {
        $config = config('permissions');

        if (empty($config['registry']) || empty($config['roles'])) {
            $this->error('❌ config/permissions.php kosong atau tidak valid.');

            return self::FAILURE;
        }

        // Build registry lookup (flatten all permission IDs)
        $this->buildRegistry($config['registry']);

        // Validate: cek semua permission di roles ada di registry
        $invalidPerms = $this->validateRoles($config['roles']);
        if (! empty($invalidPerms)) {
            foreach ($invalidPerms as $roleName => $perms) {
                $this->warn("  ⚠️  Role \"{$roleName}\" punya permission tidak dikenal:");
                foreach ($perms as $perm) {
                    $this->line("      - {$perm}");
                }
            }
            $this->newLine();
            $this->warn('Permission di atas akan di-SKIP. Pastikan ada di registry.');
            $this->newLine();
        }

        // Calculate diff
        $this->calculateDiff($config['roles']);

        // Display summary
        $this->displaySummary();

        if ($this->isDiffEmpty()) {
            $this->info('✅ Database sudah sinkron dengan config. Tidak ada perubahan.');

            return self::SUCCESS;
        }

        // Dry-run mode
        if ($this->option('dry-run')) {
            $this->newLine();
            $this->info('🔍 Mode DRY-RUN — Tidak ada perubahan yang disimpan.');
            $this->line('   Jalankan tanpa --dry-run untuk apply perubahan.');

            return self::SUCCESS;
        }

        // Confirmation
        if (! $this->option('force')) {
            if (! $this->confirm('Apply perubahan ke database?')) {
                $this->info('Dibatalkan oleh user.');

                return self::SUCCESS;
            }
        }

        // Apply
        return $this->applySync();
    }

    /**
     * Build flat list of all valid permission IDs from registry.
     */
    private function buildRegistry(array $registry): void
    {
        foreach ($registry as $module) {
            if (isset($module['permissions'])) {
                foreach ($module['permissions'] as $permId => $description) {
                    $this->registryPermissions[$permId] = [
                        'module' => $module['label'] ?? 'Unknown',
                        'permission' => $permId,
                        'description' => $description,
                    ];
                }
            }
        }
    }

    /**
     * Validate that all permission IDs in roles exist in registry.
     * Returns array of invalid permissions per role.
     */
    private function validateRoles(array $roles): array
    {
        $invalid = [];

        foreach ($roles as $roleName => $permissions) {
            foreach ($permissions as $perm) {
                if (! isset($this->registryPermissions[$perm])) {
                    $invalid[$roleName][] = $perm;
                }
            }
        }

        return $invalid;
    }

    /**
     * Calculate what needs to be added/removed for each role.
     */
    private function calculateDiff(array $roles): void
    {
        foreach ($roles as $roleName => $configPermissions) {
            // Filter out invalid permissions
            $validPermissions = array_filter($configPermissions, fn ($p) => isset($this->registryPermissions[$p]));
            $validPermissions = array_values($validPermissions);

            // Get existing DB permissions for this role
            $role = role::where('nama_role', $roleName)->first();

            if (! $role) {
                // Role belum ada di DB — semua permission perlu ditambah
                $this->diff[$roleName] = [
                    'role_id' => null,
                    'add' => $validPermissions,
                    'remove' => [],
                    'status' => 'NEW_ROLE',
                ];

                continue;
            }

            $dbPermissions = role_permission::where('role_id', $role->id)
                ->pluck('permission')
                ->toArray();

            $toAdd = array_diff($validPermissions, $dbPermissions);
            $toRemove = array_diff($dbPermissions, $validPermissions);

            $this->diff[$roleName] = [
                'role_id' => $role->id,
                'add' => array_values($toAdd),
                'remove' => array_values($toRemove),
                'status' => (! empty($toAdd) || ! empty($toRemove)) ? 'CHANGED' : 'OK',
            ];
        }
    }

    /**
     * Check if there are any changes to apply.
     */
    private function isDiffEmpty(): bool
    {
        foreach ($this->diff as $d) {
            if (! empty($d['add']) || ! empty($d['remove'])) {
                return false;
            }
        }

        return true;
    }

    /**
     * Display summary table of changes.
     */
    private function displaySummary(): void
    {
        $this->newLine();
        $this->info('═══════════════════════════════════════════════════════════════');
        $this->info('  PERMISSION SYNC — Ringkasan Perubahan');
        $this->info('═══════════════════════════════════════════════════════════════');
        $this->newLine();

        $totalAdd = 0;
        $totalRemove = 0;

        foreach ($this->diff as $roleName => $d) {
            $statusIcon = match ($d['status']) {
                'NEW_ROLE' => '🆕',
                'CHANGED' => '🔄',
                'OK' => '✅',
                default => '❓',
            };

            $addCount = count($d['add']);
            $removeCount = count($d['remove']);
            $totalAdd += $addCount;
            $totalRemove += $removeCount;

            $this->line("  {$statusIcon} {$roleName}");

            if ($d['status'] === 'NEW_ROLE') {
                $this->line("     Status: Role baru (akan dibuat + {$addCount} permission ditambahkan)");
            } else {
                $this->line("     Status: {$d['status']}".($d['status'] === 'OK' ? ' (sudah sinkron)' : ''));
            }

            // Detail mode: show each permission
            if ($this->option('detail')) {
                // Show current DB state
                $dbPerms = [];
                if ($d['role_id']) {
                    $dbPerms = role_permission::where('role_id', $d['role_id'])
                        ->pluck('permission')->toArray();
                }
                if (! empty($dbPerms)) {
                    $this->line('     DB saat ini ('.count($dbPerms).'): '.implode(', ', $dbPerms));
                } else {
                    $this->line('     DB saat ini: (kosong)');
                }

                $configPerms = $this->diff[$roleName]['add'] === null
                    ? []
                    : array_merge($this->diff[$roleName]['add'], array_diff(
                        (config('permissions.roles')[$roleName] ?? []),
                        $this->diff[$roleName]['add']
                    ));
                $configPerms = array_unique(array_filter($configPerms, fn ($p) => isset($this->registryPermissions[$p])));
                $this->line('     Config目标 ('.count($configPerms).'): '.implode(', ', $configPerms));
            }

            if ($addCount > 0) {
                $this->line("     ➕ Ditambahkan ({$addCount}): ".implode(', ', $d['add']));
            }
            if ($removeCount > 0) {
                $this->line("     ➖ Dihapus   ({$removeCount}): ".implode(', ', $d['remove']));
            }
            if ($addCount === 0 && $removeCount === 0) {
                $this->line('     (tidak ada perubahan)');
            }

            $this->newLine();
        }

        $this->info("  Total: {$totalAdd} ditambahkan, {$totalRemove} dihapus");
        $this->newLine();
    }

    /**
     * Apply the sync to database (within transaction).
     */
    private function applySync(): int
    {
        $this->newLine();
        $this->info('🔄 Menerapkan perubahan ke database...');

        try {
            DB::beginTransaction();

            $totalAdded = 0;
            $totalRemoved = 0;

            foreach ($this->diff as $roleName => $d) {
                // Skip roles with no changes
                if (empty($d['add']) && empty($d['remove'])) {
                    continue;
                }

                // Get or create role
                $role = role::firstOrCreate(
                    ['nama_role' => $roleName],
                    ['created_at' => now(), 'updated_at' => now()]
                );

                // Add new permissions
                foreach ($d['add'] as $perm) {
                    role_permission::updateOrInsert(
                        ['role_id' => $role->id, 'permission' => $perm],
                        ['created_at' => now(), 'updated_at' => now()]
                    );
                    $totalAdded++;
                }

                // Remove old permissions
                if (! empty($d['remove'])) {
                    role_permission::where('role_id', $role->id)
                        ->whereIn('permission', $d['remove'])
                        ->delete();
                    $totalRemoved += count($d['remove']);
                }
            }

            DB::commit();

            $this->newLine();
            $this->info('═══════════════════════════════════════════════════════════════');
            $this->info('  ✅ SYNC BERHASIL!');
            $this->info("     ➕ {$totalAdded} permission ditambahkan");
            $this->info("     ➖ {$totalRemoved} permission dihapus");
            $this->info('     📋 '.count($this->diff).' role diproses');
            $this->info('═══════════════════════════════════════════════════════════════');
            $this->newLine();

            return self::SUCCESS;

        } catch (\Throwable $e) {
            DB::rollBack();

            $this->error('❌ Gagal sync: '.$e->getMessage());

            return self::FAILURE;
        }
    }
}
