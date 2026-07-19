<?php

return [
    /*
    |--------------------------------------------------------------------------
    | Konfigurasi Keuangan — CodaSuaka
    |--------------------------------------------------------------------------
    */

    'approval' => [
        /*
        | Aktifkan workflow approval transaksi.
        */
        'enabled' => env('APPROVAL_ENABLED', true),

        /*
        | Threshold nominal — transaksi dengan nominal >= threshold
        | memerlukan approval atasan.
        */
        'threshold_nominal' => env('APPROVAL_THRESHOLD', 1000000),

        /*
        | Tipe transaksi yang perlu approval.
        | 'masuk' | 'keluar' | ['masuk', 'keluar']
        */
        'tipe_perlu_approval' => ['keluar'],

        /*
        | Role yang bisa menjadi pemeriksa (approver).
        */
        'role_pemeriksa' => ['Owner', 'Manajemen'],
    ],
];
