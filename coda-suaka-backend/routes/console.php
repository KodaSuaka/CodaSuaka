<?php

use Illuminate\Foundation\Inspiring;
use Illuminate\Support\Facades\Artisan;
use Illuminate\Support\Facades\Schedule;

/*
|--------------------------------------------------------------------------
| Scheduled Tasks — CodaSuaka
|--------------------------------------------------------------------------
|
| Berikut adalah scheduler untuk notifikasi dan maintenance otomatis.
| Jalankan `php artisan schedule:work` di production untuk menjalankan task.
|
*/

Artisan::command('inspire', function () {
    $this->comment(Inspiring::quote());
})->purpose('Display an inspiring quote');

// ─── Notifikasi: Pengingat tenggat penugasan ───────────────────
// Berjalan setiap jam 08:00 pagi — kirim notifikasi jika tenggat besok/hari ini
Schedule::command('notification:penugasan-deadline')
    ->dailyAt('08:00')
    ->withoutOverlapping()
    ->runInBackground()
    ->description('Kirim pengingat tenggat penugasan yang mendekati deadline');

// [DINONAKTIFKAN SEMENTARA] Approval — fitur advance, belum diaktifkan
// Schedule::command('notification:pending-approval')
//     ->everyFourHours()
//     ->withoutOverlapping()
//     ->runInBackground()
//     ->description('Kirim pengingat transaksi keuangan yang menunggu approval');

// ─── Maintenance: Cleanup expired tokens & notifikasi lama ──────
// Berjalan setiap tengah malam — bersihkan token expired & notifikasi >30 hari
Schedule::command('auth:cleanup-tokens')
    ->dailyAt('00:00')
    ->withoutOverlapping()
    ->runInBackground()
    ->description('Bersihkan token expired dan notifikasi yang sudah lama');
