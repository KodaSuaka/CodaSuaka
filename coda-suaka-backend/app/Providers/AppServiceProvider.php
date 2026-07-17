<?php

namespace App\Providers;

use App\Models\User;
use App\Models\role;
use App\Models\outlet;
use App\Models\karyawan;
use App\Models\pengajuan;
use App\Models\Divisi;
use App\Models\jadwal;
use App\Models\penugasan;
use App\Models\AnggotaDivisi;
use App\Models\attandence;
use App\Models\transaksi_paket;
use App\Models\instansi;
use App\Models\paket;
use App\Models\KategoriTransaksi;
use App\Models\TransaksiKas;
use App\Policies\OutletPolicy;
use App\Policies\KaryawanPolicy;
use App\Policies\DivisiPolicy;
use App\Policies\JadwalPolicy;
use App\Policies\PenugasanPolicy;
use App\Policies\AnggotaDivisiPolicy;
use App\Policies\AttandencePolicy;
use App\Policies\TransaksiPaketPolicy;
use App\Policies\InstansiPolicy;
use App\Policies\UserPolicy;
use App\Policies\PaketPolicy;
use App\Policies\KategoriTransaksiPolicy;
use App\Policies\TransaksiKasPolicy;
use Illuminate\Support\ServiceProvider;
use Illuminate\Support\Facades\Gate;

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
        // ─── Register Policies ─────────────────────────────────────
        Gate::policy(outlet::class, OutletPolicy::class);
        Gate::policy(karyawan::class, KaryawanPolicy::class);
        Gate::policy(Divisi::class, DivisiPolicy::class);
        Gate::policy(jadwal::class, JadwalPolicy::class);
        Gate::policy(penugasan::class, PenugasanPolicy::class);
        Gate::policy(pengajuan::class, \App\Policies\PengajuanPolicy::class);
        Gate::policy(AnggotaDivisi::class, AnggotaDivisiPolicy::class);
        Gate::policy(attandence::class, AttandencePolicy::class);
        Gate::policy(transaksi_paket::class, TransaksiPaketPolicy::class);
        Gate::policy(instansi::class, InstansiPolicy::class);
        Gate::policy(paket::class, PaketPolicy::class);
        Gate::policy(User::class, UserPolicy::class);
        Gate::policy(KategoriTransaksi::class, KategoriTransaksiPolicy::class);
        Gate::policy(TransaksiKas::class, TransaksiKasPolicy::class);

        // ─── Gate definitions ─────────────────────────────────────

        // Only Owner of an instansi
        Gate::define('owner', function (User $user) {
            return $user->role?->nama_role === 'Owner';
        });

        // Manage roles & permissions — uses granular permission check
        Gate::define('manage-roles', function (User $user) {
            return app(\App\Services\PermissionService::class)->userHasPermission($user, 'manage:role_permissions');
        });

        // Access a specific instansi's data (tenant-scoped)
        Gate::define('access-instansi', function (User $user, $instansiId) {
            return $user->instansi_id === $instansiId;
        });

        // ─── Financial Gate Definitions ────────────────────────────

        // Export keuangan (PDF/Excel)
        Gate::define('export-keuangan', function (User $user) {
            return app(\App\Services\PermissionService::class)->userHasPermission($user, 'export:keuangan');
        });

        // Delete keuangan (penghapusan transaksi/kategori)
        Gate::define('delete-keuangan', function (User $user) {
            return app(\App\Services\PermissionService::class)->userHasPermission($user, 'delete:keuangan');
        });

        // Approve keuangan (persetujuan transaksi)
        Gate::define('approve-keuangan', function (User $user) {
            return app(\App\Services\PermissionService::class)->userHasPermission($user, 'approve:keuangan');
        });
    }
}
