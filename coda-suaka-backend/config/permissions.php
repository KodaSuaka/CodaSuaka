<?php

/**
 * ══════════════════════════════════════════════════════════════════════════════
 *  MASTER PERMISSION REGISTRY — CodaSuaka
 * ══════════════════════════════════════════════════════════════════════════════
 *
 *  File ini adalah SINGLE SOURCE OF TRUTH untuk semua permission di aplikasi.
 *  Setiap kali ingin menambah/mengurangi/mengubah permission:
 *
 *    1. Edit file ini
 *    2. Jalankan: php artisan permission:sync
 *       (atau: php artisan permission:sync --dry-run untuk preview)
 *
 *  JANGAN edit database role_permissions secara manual!
 *
 *  Struktur:
 *    'registry'  → Daftar semua permission yang tersedia (dengan metadata)
 *    'roles'     → Mapping role → permission IDs
 *
 *  Format permission ID: 'action:module'  (contoh: 'view:keuangan', 'manage:karyawan')
 * ══════════════════════════════════════════════════════════════════════════════
 */

return [

    /*
    |--------------------------------------------------------------------------
    | Registry — Daftar Semua Permission yang Tersedia
    |--------------------------------------------------------------------------
    |
    | Setiap permission dikelompokkan per modul.
    | Format: 'permission_id' => 'Deskripsi untuk admin'
    |
    | Permission yang dikomentari/dihapus dari sini akan otomatis
    | dihapus dari database oleh `permission:sync`.
    |
    */
    'registry' => [

        // ─── Presensi / Absensi ──────────────────────────────────────
        'presensi' => [
            'label' => 'Presensi / Absensi',
            'permissions' => [
                'view:presensi'     => 'Melihat data presensi/absensi karyawan',
                'manage:presensi'   => 'Mengelola (create/edit/delete) data presensi',
            ],
        ],

        // ─── Pengajuan (Cuti / Izin / Sakit) ────────────────────────
        'pengajuan' => [
            'label' => 'Pengajuan (Cuti/Izin/Sakit)',
            'permissions' => [
                'view:pengajuan'    => 'Melihat daftar pengajuan',
                'manage:pengajuan'  => 'Mengelola & menyetujui/menolak pengajuan',
            ],
        ],

        // ─── Divisi ──────────────────────────────────────────────────
        'divisi' => [
            'label' => 'Divisi / Organisasi',
            'permissions' => [
                'view:divisi'       => 'Melihat data divisi',
                'manage:divisi'     => 'Mengelola divisi & anggota divisi',
            ],
        ],

        // ─── Penugasan / Tugas ───────────────────────────────────────
        'penugasan' => [
            'label' => 'Penugasan / Tugas Karyawan',
            'permissions' => [
                'view:penugasan'    => 'Melihat daftar penugasan & template',
                'manage:penugasan'  => 'Membuat, mengedit, menghapus penugasan & template',
            ],
        ],

        // ─── Jadwal / Kalender ───────────────────────────────────────
        'jadwal' => [
            'label' => 'Jadwal / Kalender',
            'permissions' => [
                'view:jadwal'       => 'Melihat jadwal kerja',
                'manage:jadwal'     => 'Mengelola jadwal kerja',
            ],
        ],

        // ─── Karyawan / HRD ──────────────────────────────────────────
        'karyawan' => [
            'label' => 'Karyawan / HRD',
            'permissions' => [
                'view:karyawan'     => 'Melihat data karyawan',
                'manage:karyawan'   => 'Mengelola data karyawan (tambah/edit/hapus)',
            ],
        ],

        // ─── Outlet / Lokasi ─────────────────────────────────────────
        'outlets' => [
            'label' => 'Outlet / Lokasi',
            'permissions' => [
                'view:outlets'      => 'Melihat daftar outlet',
                'manage:outlets'    => 'Mengelola outlet (tambah/edit/hapus)',
            ],
        ],

        // ─── Keuangan / Transaksi ────────────────────────────────────
        'keuangan' => [
            'label' => 'Keuangan / Transaksi Kas',
            'permissions' => [
                'view:keuangan'     => 'Melihat data keuangan & transaksi kas',
                'manage:keuangan'   => 'Mengelola transaksi kas (tambah/edit/hapus)',
                'delete:keuangan'   => 'Menghapus transaksi kas & kategori',
                'export:keuangan'   => 'Mengekspor laporan keuangan (PDF/Excel)',
                'approve:keuangan'  => 'Menyetujui/menolak transaksi keuangan', // [FITUR ADVANCE]
            ],
        ],

        // ─── Laporan ─────────────────────────────────────────────────
        'laporan' => [
            'label' => 'Laporan',
            'permissions' => [
                'view:laporan'      => 'Melihat laporan arus kas & ringkasan keuangan',
                'manage:laporan'    => 'Mengelola & generate laporan',
            ],
        ],

        // ─── Audit Log ───────────────────────────────────────────────
        'audit' => [
            'label' => 'Audit Log', // [FITUR ADVANCE]
            'permissions' => [
                'view:audit'        => 'Melihat log aktivitas sistem',
                'manage:audit'      => 'Mengelola log aktivitas sistem',
            ],
        ],

        // ─── Role & Permission ───────────────────────────────────────
        'role_permissions' => [
            'label' => 'Role & Permission',
            'permissions' => [
                'manage:role_permissions' => 'Mengelola role & permission (Super Admin only)',
            ],
        ],

        // ─── Paket & Transaksi Paket ─────────────────────────────────
        'paket' => [
            'label' => 'Paket & Transaksi Paket',
            'permissions' => [
                'view:paket'        => 'Melihat daftar paket',
                'manage:paket'      => 'Mengelola paket & transaksi paket',
            ],
        ],

        // ─── Instansi / Perusahaan ───────────────────────────────────
        'instansi' => [
            'label' => 'Instansi / Perusahaan',
            'permissions' => [
                'manage:instansi'   => 'Mengelola data instansi/perusahaan',
            ],
        ],

        // ─── Attendance (legacy — jika masih dipakai) ────────────────
        'attendance' => [
            'label' => 'Attendance (Legacy)',
            'permissions' => [
                'manage:attendance' => 'Mengelola kehadiran (legacy)',
            ],
        ],
    ],

    /*
    |--------------------------------------------------------------------------
    | Roles — Mapping Role → Permission IDs
    |--------------------------------------------------------------------------
    |
    | Setiap role mendapat daftar permission ID dari registry di atas.
    | Permission ID harus ada di registry. Jika tidak ada, akan di-skip
    | dan muncul warning saat sync.
    |
    | CATATAN: Permission yang dikomentari tidak akan di-sync ke database.
    |          Gunakan label [FITUR ADVANCE] untuk menandai yang belum aktif.
    |
    */
    'roles' => [

        'Owner' => [
            // Presensi
            'view:presensi',
            'manage:presensi',

            // Pengajuan
            'view:pengajuan',
            'manage:pengajuan',

            // Divisi
            'view:divisi',
            'manage:divisi',

            // Penugasan
            'view:penugasan',
            'manage:penugasan',

            // Jadwal
            'view:jadwal',
            'manage:jadwal',

            // Karyawan
            'view:karyawan',
            'manage:karyawan',

            // Outlet
            'view:outlets',
            'manage:outlets',

            // Keuangan
            'view:keuangan',
            'manage:keuangan',
            // 'approve:keuangan',  // [FITUR ADVANCE — belum diaktifkan]
            'export:keuangan',

            // Laporan
            'view:laporan',
            'manage:laporan',

            // Audit Log — [FITUR ADVANCE — belum diaktifkan]
            // 'view:audit',
            // 'manage:audit',
        ],

        'Keuangan' => [
            // Keuangan
            'view:keuangan',
            'manage:keuangan',
            'export:keuangan',

            // Laporan
            'view:laporan',

            // Presensi (read-only untuk rekap)
            'view:presensi',

            // Penugasan (read-only untuk melihat tugas)
            'view:penugasan',
        ],

        'Manager' => [
            // Presensi
            'view:presensi',
            'manage:presensi',

            // Pengajuan
            'view:pengajuan',
            'manage:pengajuan',

            // Divisi
            'view:divisi',
            'manage:divisi',

            // Penugasan
            'view:penugasan',
            'manage:penugasan',

            // Jadwal
            'view:jadwal',
            'manage:jadwal',

            // Karyawan
            'view:karyawan',
            'manage:karyawan',

            // Audit Log — [FITUR ADVANCE — belum diaktifkan]
            // 'view:audit',
            // 'manage:audit',

            // NOTE: Manager TIDAK punya akses keuangan
        ],

        'Staff' => [
            // Presensi
            'view:presensi',

            // Penugasan
            'view:penugasan',

            // Jadwal
            'view:jadwal',
        ],
    ],

];
