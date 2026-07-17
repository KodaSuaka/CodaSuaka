<?php

use Illuminate\Http\Request;
use Illuminate\Support\Facades\Route;
use App\Http\Controllers\AuthController;
use App\Http\Controllers\ChatController;
use App\Http\Controllers\OutletController;
use App\Http\Controllers\RoleController;
use App\Http\Controllers\RolePermissionController;
use App\Http\Controllers\KaryawanController;
use App\Http\Controllers\DivisiController;
use App\Http\Controllers\AnggotaDivisiController;
use App\Http\Controllers\AttandenceController;
use App\Http\Controllers\PengajuanController;
use App\Http\Controllers\JadwalController;
use App\Http\Controllers\PenugasanController;
use App\Http\Controllers\PaketController;
use App\Http\Controllers\TransaksiPaketController;
use App\Http\Controllers\InstansiController;
use App\Http\Controllers\DashboardController;
use App\Http\Controllers\SuperAdminController;
use App\Http\Controllers\KategoriTransaksiController;
use App\Http\Controllers\TransaksiKasController;

/*
|--------------------------------------------------------------------------
| API Routes — CodaSuaka
| Semua endpoint menggunakan Sanctum untuk autentikasi token.
| CORS sudah dikonfigurasi di config/cors.php agar bisa diakses
| dari berbagai platform (Android, Web, iOS, dll).
|--------------------------------------------------------------------------
*/

// ─── Public Routes (tanpa auth) ─────────────────────────────────
Route::post('/register', [AuthController::class, 'register']);
Route::post('/register-super-admin', [AuthController::class, 'registerSuperAdmin']);
Route::post('/login', [AuthController::class, 'login']);

// ─── Protected Routes (memerlukan token Sanctum) ──────────────
Route::middleware('auth:sanctum')->group(function () {

    // ─── Auth / User ──────────────────────────────────────────
    Route::post('/logout', [AuthController::class, 'logout']);
    Route::get('/user', function (Request $request) {
        return response()->json([
            'status' => 'success',
            'data' => $request->user()->load(['role', 'profilKaryawan', 'outlet'])
        ]);
    });

    // ─── Dashboard ────────────────────────────────────────────
    Route::get('/dashboard', [DashboardController::class, 'index']);
    Route::get('/dashboard/omset', [DashboardController::class, 'omset']);
    Route::get('/karyawan/dashboard', [DashboardController::class, 'karyawanDashboard']);

    // ─── Instansi (profil perusahaan) ─────────────────────────
    Route::get('/instansi', [InstansiController::class, 'show']);
    Route::put('/instansi', [InstansiController::class, 'update']);

    // ─── Outlet CRUD ──────────────────────────────────────────
    Route::apiResource('/outlets', OutletController::class);

    // ─── Role & Permission ────────────────────────────────────
    Route::apiResource('/roles', RoleController::class);
    Route::get('/role-permissions', [RolePermissionController::class, 'index']);
    Route::post('/role-permissions', [RolePermissionController::class, 'store']);
    Route::post('/role-permissions/sync', [RolePermissionController::class, 'sync']);
    Route::delete('/role-permissions/{role_permission}', [RolePermissionController::class, 'destroy']);

    // ─── Karyawan CRUD ────────────────────────────────────────
    Route::get('/karyawans/me', [KaryawanController::class, 'me']);
    Route::apiResource('/karyawans', KaryawanController::class);

    // ─── Divisi CRUD ──────────────────────────────────────────
    Route::apiResource('/divisis', DivisiController::class);

    // ─── Anggota Divisi ───────────────────────────────────────
    Route::get('/anggota-divisis', [AnggotaDivisiController::class, 'index']);
    Route::post('/anggota-divisis', [AnggotaDivisiController::class, 'store']);
    Route::delete('/anggota-divisis/{anggotaDivisi}', [AnggotaDivisiController::class, 'destroy']);

    // ─── Presensi / Absensi ───────────────────────────────────
    Route::get('/presensis', [AttandenceController::class, 'index']);
    Route::post('/presensis/checkin', [AttandenceController::class, 'checkin']);
    Route::post('/presensis/checkout', [AttandenceController::class, 'checkout']);
    Route::get('/presensis/today', [AttandenceController::class, 'today']);

    // ─── Rekap Kehadiran ──────────────────────────────────────
    Route::get('/rekap-kehadiran', [AttandenceController::class, 'rekap']);

    // ─── Pengajuan Cuti/Izin/Sakit ────────────────────────────
    Route::get('/pengajuans', [PengajuanController::class, 'index']);
    Route::post('/pengajuans', [PengajuanController::class, 'store']);
    Route::get('/pengajuans/{pengajuan}', [PengajuanController::class, 'show']);
    Route::put('/pengajuans/{pengajuan}/approve', [PengajuanController::class, 'approve']);
    Route::put('/pengajuans/{pengajuan}/reject', [PengajuanController::class, 'reject']);

    // ─── Jadwal / Kalender ────────────────────────────────────
    Route::apiResource('/jadwals', JadwalController::class);

    // ─── Penugasan / Tugas ────────────────────────────────────
    Route::apiResource('/penugasans', PenugasanController::class);

    // ─── Paket ─────────────────────────────────────────────────
    // Regular users: read-only. Write only via Super Admin routes below.
    Route::get('/pakets', [PaketController::class, 'index']);
    Route::get('/pakets/{paket}', [PaketController::class, 'show']);

    // ─── Transaksi Paket ──────────────────────────────────────
    Route::get('/transaksi-pakets', [TransaksiPaketController::class, 'index']);
    Route::post('/transaksi-pakets', [TransaksiPaketController::class, 'store']);
    Route::get('/transaksi-pakets/{transaksi_paket}', [TransaksiPaketController::class, 'show']);
    Route::put('/transaksi-pakets/{transaksi_paket}', [TransaksiPaketController::class, 'update']);

    // ─── Keuangan: Kategori Transaksi ─────────────────────────
    Route::apiResource('/kategori-transaksis', KategoriTransaksiController::class);

    // ─── Keuangan: Buku Kas (Transaksi Kas) ───────────────────
    Route::prefix('transaksi-kas')->group(function () {
        Route::get('/', [TransaksiKasController::class, 'index']);
        Route::post('/', [TransaksiKasController::class, 'store']);
        Route::get('/saldo', [TransaksiKasController::class, 'saldo']);
        Route::get('/laporan/laba-rugi', [TransaksiKasController::class, 'labaRugi']);
        Route::get('/{transaksi_kas}', [TransaksiKasController::class, 'show']);
        Route::put('/{transaksi_kas}', [TransaksiKasController::class, 'update']);
        Route::delete('/{transaksi_kas}', [TransaksiKasController::class, 'destroy']);
    });

    // ─── Chat / Kontak ────────────────────────────────────────
    Route::prefix('chat')->group(function () {
        Route::get('/contacts', [ChatController::class, 'contacts']);
        Route::get('/messages/{user}', [ChatController::class, 'messages']);
        Route::post('/send', [ChatController::class, 'send']);
        Route::put('/read/{user}', [ChatController::class, 'markAsRead']);
    });

    // ═══════════════════════════════════════════════════════════
    //  SUPER ADMIN — Hanya untuk role "Super Admin"
    // ═══════════════════════════════════════════════════════════
    Route::prefix('super-admin')->middleware('role:Super Admin')->group(function () {

        // Dashboard Super Admin
        Route::get('/dashboard', [SuperAdminController::class, 'dashboard']);

        // ─── CRUD Instansi ─────────────────────────────────────
        Route::get('/instansis', [SuperAdminController::class, 'indexInstansi']);
        Route::get('/instansis/{instansi}', [SuperAdminController::class, 'showInstansi']);
        Route::post('/instansis', [SuperAdminController::class, 'storeInstansi']);
        Route::put('/instansis/{instansi}', [SuperAdminController::class, 'updateInstansi']);
        Route::delete('/instansis/{instansi}', [SuperAdminController::class, 'destroyInstansi']);

        // ─── CRUD Owner ────────────────────────────────────────
        Route::get('/owners', [SuperAdminController::class, 'indexOwner']);
        Route::get('/owners/{user}', [SuperAdminController::class, 'showOwner']);
        Route::post('/owners', [SuperAdminController::class, 'storeOwner']);
        Route::put('/owners/{user}', [SuperAdminController::class, 'updateOwner']);
        Route::delete('/owners/{user}', [SuperAdminController::class, 'destroyOwner']);

        // ─── CRUD Paket ────────────────────────────────────────
        Route::get('/pakets', [SuperAdminController::class, 'indexPaket']);
        Route::get('/pakets/{paket}', [SuperAdminController::class, 'showPaket']);
        Route::post('/pakets', [SuperAdminController::class, 'storePaket']);
        Route::put('/pakets/{paket}', [SuperAdminController::class, 'updatePaket']);
        Route::delete('/pakets/{paket}', [SuperAdminController::class, 'destroyPaket']);
    });
});
