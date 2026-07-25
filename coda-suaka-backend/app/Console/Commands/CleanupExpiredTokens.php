<?php

namespace App\Console\Commands;

use Illuminate\Console\Command;
use Illuminate\Support\Facades\DB;

class CleanupExpiredTokens extends Command
{
    /**
     * The name and signature of the console command.
     */
    protected $signature = 'auth:cleanup-tokens';

    /**
     * The console command description.
     */
    protected $description = 'Bersihkan token Sanctum yang sudah expired dan notifikasi yang sudah dibaca lebih dari 30 hari';

    /**
     * Execute the console command.
     *
     * NOTE: MySQL 5.7 compatible — tidak pakai CTE atau window function.
     */
    public function handle(): int
    {
        // 1. Hapus token Sanctum yang sudah expired
        $expiredTokens = DB::table('personal_access_tokens')
            ->whereNotNull('expires_at')
            ->where('expires_at', '<', now())
            ->delete();

        $this->info("Berhasil menghapus {$expiredTokens} token expired.");

        // 2. Hapus notifikasi yang sudah dibaca dan lebih dari 30 hari
        $oldReadNotifications = DB::table('notifications')
            ->where('is_read', true)
            ->where('created_at', '<', now()->subDays(30))
            ->delete();

        $this->info("Berhasil menghapus {$oldReadNotifications} notifikasi lama (>30 hari, sudah dibaca).");

        // 3. Hapus token yang tidak pernah dipakai (last_used_at null) dan lebih dari 7 hari
        // Ini untuk membersihkan token dari login yang gagal atau session lama
        $staleTokens = DB::table('personal_access_tokens')
            ->whereNull('last_used_at')
            ->where('created_at', '<', now()->subDays(7))
            ->delete();

        $this->info("Berhasil menghapus {$staleTokens} token tidak aktif (>7 hari, tidak pernah dipakai).");

        return Command::SUCCESS;
    }
}
