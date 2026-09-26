<?php

namespace App\Console\Commands;

use App\Models\penugasan;
use App\Services\NotificationService;
use Illuminate\Console\Command;

class SendPenugasanDeadlineReminder extends Command
{
    /**
     * The name and signature of the console command.
     */
    protected $signature = 'notification:penugasan-deadline';

    /**
     * The console command description.
     */
    protected $description = 'Kirim notifikasi pengingat untuk penugasan yang mendekati tenggat (1 hari lagi)';

    /**
     * Execute the console command.
     *
     * NOTE: MySQL 5.7 tidak mendukung CTE/window function,
     * jadi kita gunakan query sederhana dengan whereDate.
     */
    public function handle(NotificationService $notificationService): int
    {
        $tomorrow = now()->addDay()->toDateString();
        $today = now()->toDateString();

        // Ambil semua penugasan yang tenggat = besok atau hari ini dan belum selesai/batal
        // Tanpa TenantScope (karena tidak ada auth user di scheduler)
        $penugasans = penugasan::withoutGlobalScopes()
            ->where('is_template', false)
            ->where('status', '!=', 'selesai')
            ->where('status', '!=', 'batal')
            ->whereNotNull('tenggat')
            ->whereIn('tenggat', [$today, $tomorrow])
            ->with(['penanggungJawab.user'])
            ->get();

        $count = 0;

        foreach ($penugasans as $penugasan) {
            $user = $penugasan->penanggungJawab?->user;
            if (! $user) {
                continue;
            }

            $hariTersisa = $penugasan->tenggat->isToday() ? 'hari ini' : 'besok';

            $notificationService->create(
                $user->id,
                'penugasan_deadline',
                'Pengingat Tenggat Tugas',
                "Tugas \"{$penugasan->judul}\" harus diselesaikan {$hariTersisa}!",
                'Warning',
                '#F59E0B',
                $penugasan->id,
                'App\\Models\\penugasan'
            );

            $count++;
        }

        $this->info("Berhasil mengirim {$count} notifikasi pengingat tenggat penugasan.");

        return Command::SUCCESS;
    }
}
