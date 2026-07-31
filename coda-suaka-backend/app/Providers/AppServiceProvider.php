<?php

namespace App\Providers;

use App\Models\AnggotaDivisi;
use App\Models\attandence;
use App\Models\BarangJasa;
use App\Models\Divisi;
use App\Models\Instansi;
use App\Models\jadwal;
use App\Models\karyawan;
use App\Models\KategoriTransaksi;
use App\Models\Nota;
use App\Models\outlet;
use App\Models\paket;
use App\Models\pengajuan;
use App\Models\penugasan;
use App\Models\transaksi_paket;
use App\Models\TransaksiKas;
use App\Models\User;
use App\Policies\AnggotaDivisiPolicy;
use App\Policies\AttandencePolicy;
use App\Policies\BarangJasaPolicy;
use App\Policies\DivisiPolicy;
use App\Policies\InstansiPolicy;
use App\Policies\JadwalPolicy;
use App\Policies\KaryawanPolicy;
use App\Policies\KategoriTransaksiPolicy;
use App\Policies\NotaPolicy;
use App\Policies\OutletPolicy;
use App\Policies\PaketPolicy;
use App\Policies\PengajuanPolicy;
use App\Policies\PenugasanPolicy;
use App\Policies\TransaksiKasPolicy;
use App\Policies\TransaksiPaketPolicy;
use App\Policies\UserPolicy;
use App\Services\PermissionService;
use Carbon\Carbon;
use Illuminate\Cache\RateLimiting\Limit;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Gate;
use Illuminate\Support\Facades\RateLimiter;
use Illuminate\Support\ServiceProvider;

class AppServiceProvider extends ServiceProvider
{
    /**
     * Register any application services.
     */
    public function register(): void
    {
        //
    }

    /**
     * Bootstrap any application services.
     */
    public function boot(): void
    {
        // ─── Rate Limiting ──────────────────────────────────────────
        // H2: Rate limit untuk login — maks 5 percobaan per menit per IP
        RateLimiter::for('login', function (Request $request) {
            return Limit::perMinute(5)->by($request->ip());
        });

        // H2: Rate limit untuk register super admin — maks 3 per menit per IP
        RateLimiter::for('register-super-admin', function (Request $request) {
            return Limit::perMinute(3)->by($request->ip());
        });

        // Rate limit untuk register umum — maks 5 per menit per IP
        RateLimiter::for('register', function (Request $request) {
            return Limit::perMinute(5)->by($request->ip());
        });

        // ─── Set locale Carbon ke Indonesia ─────────────────────────
        // Agar now()->isoFormat('DD MMMM YYYY') menghasilkan "17 Juli 2026"
        Carbon::setLocale('id');

        // ─── Register Policies ─────────────────────────────────────
        Gate::policy(outlet::class, OutletPolicy::class);
        Gate::policy(karyawan::class, KaryawanPolicy::class);
        Gate::policy(Divisi::class, DivisiPolicy::class);
        Gate::policy(jadwal::class, JadwalPolicy::class);
        Gate::policy(penugasan::class, PenugasanPolicy::class);
        Gate::policy(pengajuan::class, PengajuanPolicy::class);
        Gate::policy(AnggotaDivisi::class, AnggotaDivisiPolicy::class);
        Gate::policy(attandence::class, AttandencePolicy::class);
        Gate::policy(transaksi_paket::class, TransaksiPaketPolicy::class);
        Gate::policy(Instansi::class, InstansiPolicy::class);
        Gate::policy(paket::class, PaketPolicy::class);
        Gate::policy(User::class, UserPolicy::class);
        Gate::policy(KategoriTransaksi::class, KategoriTransaksiPolicy::class);
        Gate::policy(TransaksiKas::class, TransaksiKasPolicy::class);
        Gate::policy(BarangJasa::class, BarangJasaPolicy::class);
        Gate::policy(Nota::class, NotaPolicy::class);

        // ─── Gate definitions ─────────────────────────────────────

        // Only Owner of an instansi
        Gate::define('owner', function (User $user) {
            return $user->role?->nama_role === 'Owner';
        });

        // Manage roles & permissions — uses granular permission check
        Gate::define('manage-roles', function (User $user) {
            return $user->role?->nama_role === 'Owner' || app(PermissionService::class)->userHasPermission($user, 'manage:role_permissions');
        });

        // Access a specific instansi's data (tenant-scoped)
        Gate::define('access-instansi', function (User $user, $instansiId) {
            return $user->instansi_id === $instansiId;
        });

        // ─── Financial Gate Definitions ────────────────────────────

        // Export keuangan (PDF/Excel)
        Gate::define('export-keuangan', function (User $user) {
            return app(PermissionService::class)->userHasPermission($user, 'export:keuangan');
        });

        // Delete keuangan (penghapusan transaksi/kategori)
        Gate::define('delete-keuangan', function (User $user) {
            return app(PermissionService::class)->userHasPermission($user, 'delete:keuangan');
        });

        // Approve keuangan (persetujuan transaksi)
        Gate::define('approve-keuangan', function (User $user) {
            return app(PermissionService::class)->userHasPermission($user, 'approve:keuangan');
        });
    }
}
