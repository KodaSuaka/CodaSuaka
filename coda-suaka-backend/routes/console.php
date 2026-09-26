<?php

use Illuminate\Foundation\Inspiring;
use Illuminate\Support\Facades\Artisan;
use Illuminate\Support\Facades\Schedule;

/*
|--------------------------------------------------------------------------
| Scheduled Tasks — CodaSuaka
|--------------------------------------------------------------------------
|
| Semua schedule dibaca dari config/schedule.php.
| Untuk enable/disable, cukup ubah value 'enabled' → true/false di config.
| Setelah ubah, jalankan: php artisan config:clear
|
*/

Artisan::command('inspire', function () {
    $this->comment(Inspiring::quote());
})->purpose('Display an inspiring quote');

// ─── Baca config schedule ──────────────────────────────────────────
$scheduleConfig = config('schedule', []);

// ─── Notifikasi: Pengingat tenggat penugasan ───────────────────────
if ($scheduleConfig['penugasan_deadline_reminder']['enabled'] ?? false) {
    Schedule::command('notification:penugasan-deadline')
        ->dailyAt('08:00')
        ->withoutOverlapping()
        ->runInBackground()
        ->description('Kirim pengingat tenggat penugasan yang mendekati deadline');
}

// ─── Notifikasi: Approval keuangan pending ──────────────────────────
if ($scheduleConfig['pending_approval_reminder']['enabled'] ?? false) {
    Schedule::command('notification:pending-approval')
        ->everyFourHours()
        ->withoutOverlapping()
        ->runInBackground()
        ->description('Kirim pengingat transaksi keuangan yang menunggu approval');
}

// ─── Maintenance: Cleanup expired tokens & notifikasi lama ──────────
if ($scheduleConfig['cleanup_expired_tokens']['enabled'] ?? false) {
    Schedule::command('auth:cleanup-tokens')
        ->dailyAt('00:00')
        ->withoutOverlapping()
        ->runInBackground()
        ->description('Bersihkan token expired dan notifikasi yang sudah lama');
}
