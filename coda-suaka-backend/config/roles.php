<?php

/**
 * ══════════════════════════════════════════════════════════════════════════════
 *  DEPRECATED — Gunakan config/permissions.php sebagai Single Source of Truth
 * ══════════════════════════════════════════════════════════════════════════════
 *
 *  File ini DIPERTAHANKAN untuk backward compatibility.
 *  Mapping permission sekarang dikelola di config/permissions.php.
 *
 *  Untuk menambah/mengurangi/mengubah permission:
 *    1. Edit config/permissions.php
 *    2. Jalankan: php artisan permission:sync
 *
 *  File ini hanya meng-read dari config/permissions.php secara otomatis.
 * ══════════════════════════════════════════════════════════════════════════════
 */

// Ambil data dari config/permissions.php
$permissionsConfig = require __DIR__ . '/permissions.php';

return [
    /*
    |--------------------------------------------------------------------------
    | Mapping Role → Permission Default (DEPRECATED)
    |--------------------------------------------------------------------------
    |
    | ⚠️  Data ini sekarang diambil dari config/permissions.php secara otomatis.
    |     Jangan edit manual di sini. Edit di config/permissions.php instead.
    |
    */
    'permissions' => $permissionsConfig['roles'] ?? [],
];
