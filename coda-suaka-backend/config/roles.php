<?php

return [
    /*
    |--------------------------------------------------------------------------
    | Mapping Role → Permission Default
    |--------------------------------------------------------------------------
    |
    | Konfigurasi ini mendefinisikan permission default untuk setiap role
    | yang akan di-seed oleh RolePermissionSeeder.
    |
    | Format: 'nama_role' => ['permission1', 'permission2', ...]
    |
    */

    'permissions' => [
        'Owner' => [
            'view:presensi',
            'manage:presensi',
            'view:pengajuan',
            'manage:pengajuan',
            'view:divisi',
            'manage:divisi',
            'view:penugasan',
            'manage:penugasan',
            'view:jadwal',
            'manage:jadwal',
            'view:karyawan',
            'manage:karyawan',
            'view:outlets',
            'manage:outlets',
            'view:keuangan',
            'manage:keuangan',
            'approve:keuangan',
            'view:laporan',
            'manage:laporan',
            'view:audit',
            'manage:audit',
        ],

        'Keuangan' => [
            'view:keuangan',
            'manage:keuangan',
            'view:laporan',
            'view:presensi',
        ],

        'Manager' => [
            'view:presensi',
            'manage:presensi',
            'view:pengajuan',
            'manage:pengajuan',
            'view:divisi',
            'manage:divisi',
            'view:penugasan',
            'manage:penugasan',
            'view:jadwal',
            'manage:jadwal',
            'view:karyawan',
            'manage:karyawan',
            'view:audit',
            'manage:audit',
            // NOTE: Manager TIDAK punya akses keuangan (view:keuangan, manage:keuangan)
            // Fokus: kelola karyawan, persetujuan izin, dan audit
        ],

        'Staff' => [
            'view:presensi',
            'view:penugasan',
            'view:jadwal',
        ],
    ],
];
