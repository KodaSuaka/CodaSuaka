<?php

return [

    /*
    |--------------------------------------------------------------------------
    | Cross-Origin Resource Sharing (CORS) Configuration
    |--------------------------------------------------------------------------
    |
    | Here you may configure your settings for cross-origin resource sharing
    | or "CORS". This determines what cross-origin operations may execute
    | in web browsers. You are free to adjust these settings as needed.
    |
    | To learn more: https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS
    |
    */

    'paths' => ['api/*', 'sanctum/csrf-cookie'],

    'allowed_methods' => ['*'],

    /*
    |--------------------------------------------------------------------------
    | Allowed Origins
    |--------------------------------------------------------------------------
    |
    | Daftar origin yang diizinkan mengakses API dari browser.
    | Android menggunakan OkHttp yang tidak terikat CORS, jadi hanya
    | domain web yang perlu didaftarkan di sini.
    |
    | Format: comma-separated list di .env, contoh:
    |   FRONTEND_URLS=https://codasuaka.my.id,https://admin.codasuaka.my.id
    |
    | Default: hanya mengizinkan https://codasuaka.my.id
    */
    'allowed_origins' => array_filter(array_map('trim', explode(',', env(
        'FRONTEND_URLS',
        'https://codasuaka.my.id'
    )))),

    'allowed_origins_patterns' => [],

    'allowed_headers' => ['*'],

    'exposed_headers' => [],

    'max_age' => 0,

    'supports_credentials' => false,

];
