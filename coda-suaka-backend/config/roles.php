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
            'view:keuangan',
            'manage:keuangan',
            'view:laporan',
            'manage:laporan',
        ],

        'Keuangan' => [
            'view:keuangan',
            'manage:keuangan',
            'view:laporan',
            'view:presensi',
        ],

        'Manajemen' => [
            'view:presensi',
            'manage:presensi',
            'view:pengajuan',
            'view:divisi',
            'view:penugasan',
            'manage:penugasan',
            'view:jadwal',
            'manage:jadwal',
            'view:karyawan',
            'manage:karyawan',
            'view:keuangan',
            'view:laporan',
        ],

        'Staff' => [
            'view:presensi',
            'view:penugasan',
            'view:jadwal',
        ],
    ],
];
