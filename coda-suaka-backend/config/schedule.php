<?php

/**
 * Schedule Configuration — CodaSuaka
 *
 * Setiap schedule bisa diaktifkan/nonaktifkan tanpa edit code.
 * Cukup ubah value 'enabled' → true/false di sini.
 *
 * Setelah ubah, clear cache: php artisan config:clear
 */

return [

    // ─── Notifikasi: Pengingat tenggat penugasan ───────────────────
    // Kirim notifikasi jika tenggat tugas mendekati deadline
    'penugasan_deadline_reminder' => [
        'enabled' => true,
        'schedule' => 'dailyAt("08:00")',
    ],

    // ─── Notifikasi: Approval keuangan pending ──────────────────────
    // Kirim pengingat transaksi yang menunggu approval
    'pending_approval_reminder' => [
        'enabled' => false,  // [FITUR ADVANCE — belum diaktifkan]
        'schedule' => 'everyFourHours()',
    ],

    // ─── Maintenance: Cleanup expired tokens ────────────────────────
    // Bersihkan token expired & notifikasi lama (>30 hari)
    'cleanup_expired_tokens' => [
        'enabled' => true,
        'schedule' => 'dailyAt("00:00")',
    ],

];
