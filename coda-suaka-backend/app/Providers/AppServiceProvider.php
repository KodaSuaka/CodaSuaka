<?php

namespace App\Providers;

use App\Models\User;
use App\Models\role;
use App\Models\outlet;
use App\Models\karyawan;
use App\Models\Divisi;
use App\Models\jadwal;
use App\Models\penugasan;
use App\Models\AnggotaDivisi;
use App\Models\attandence;
use App\Models\transaksi_paket;
use App\Models\instansi;
use App\Models\paket;
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

        // ─── Gate definitions ─────────────────────────────────────

        // Only Super Admin
        Gate::define('super-admin', function (User $user) {
            return $user->role?->nama_role === 'Super Admin';
        });

        // Only Owner of an instansi
        Gate::define('owner', function (User $user) {
            return $user->role?->nama_role === 'Owner';
        });

        // Owner or Super Admin
        Gate::define('owner-or-super-admin', function (User $user) {
            return in_array($user->role?->nama_role, ['Owner', 'Super Admin']);
        });

        // Manage roles & permissions — prevents privilege escalation
        Gate::define('manage-roles', function (User $user) {
            return in_array($user->role?->nama_role, ['Owner', 'Super Admin']);
        });

        // Access a specific instansi's data (tenant-scoped)
        Gate::define('access-instansi', function (User $user, $instansiId) {
            if ($user->role?->nama_role === 'Super Admin') {
                return true;
            }
            return $user->instansi_id === (int) $instansiId;
        });
    }
}
