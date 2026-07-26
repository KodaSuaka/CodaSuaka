<?php

namespace App\Http\Controllers;

use App\Http\Requests\LoginRequest;
use App\Http\Requests\RegisterRequest;
use App\Http\Requests\RegisterSuperAdminRequest;
use App\Models\Instansi;
use App\Models\karyawan;
use App\Models\role;
use App\Models\User;
use App\Services\PermissionService;
use App\Traits\ApiResponse;
use Database\Seeders\TemplatePenugasanSeeder;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Hash;

class AuthController extends Controller
{
    use ApiResponse;

    public function register(RegisterRequest $request)
    {
        $RoleOwner = role::where('nama_role', 'Owner')->first();

        if (! $RoleOwner) {
            return $this->error('Role belum tersedia', 500);
        }

        $user = DB::transaction(function () use ($request, $RoleOwner) {
            $instansi = Instansi::create([
                'nama_instansi' => $request->nama_instansi,
                'paket_id' => null,
            ]);

            $user = User::create([
                'name' => $request->nama_pemilik,
                'email' => $request->email,
                'password' => Hash::make($request->password),
                'role_id' => $RoleOwner->id,
                'instansi_id' => $instansi->id,
                'outlet_id' => null,
            ]);

            karyawan::create([
                'user_id' => $user->id,
                'nama_lengkap' => $request->nama_pemilik,
                'kontak' => null,
                'foto_profil' => null,
            ]);

            // Copy template tugas global ke instansi baru
            TemplatePenugasanSeeder::copyGlobalTemplatesToInstansi($instansi->id, $user->id);

            return $user;
        });

        $permissions = app(PermissionService::class)->getUserPermissions($user);

        $token = $user->createToken('auth_token')->plainTextToken;

        return $this->success([
            'user' => [
                'id' => $user->id,
                'name' => $user->name,
                'email' => $user->email,
                'role' => $user->role?->nama_role ?? 'Unknown',
                'instansi_id' => $user->instansi_id,
                'outlet_id' => $user->outlet_id,
            ],
            'permissions' => $permissions,
            'access_token' => $token,
            'token_type' => 'Bearer',
        ], 'Registrasi Owner dan Instansi berhasil', 201);

    }

    public function login(LoginRequest $request)
    {
        $user = User::where('email', $request->email)->first();
        if (! $user || ! Hash::check($request->password, $user->password)) {
            return $this->error('Email atau Password yang Anda masukkan salah.', 401);
        }

        // Validasi role harus ada
        if (! $user->role) {
            return $this->error('Akun Anda belum memiliki role yang valid. Hubungi administrator.', 403);
        }

        // Bersihkan token lama yang sudah expired atau tidak terpakai
        // untuk mencegah akumulasi token dan potensi unique constraint violation
        $user->tokens()
            ->where('name', 'auth_token')
            ->where(function ($query) {
                $query->whereNull('expires_at')
                    ->orWhere('expires_at', '<', now());
            })
            ->delete();

        $profil = karyawan::where('user_id', $user->id)->first();

        $permissions = app(PermissionService::class)->getUserPermissions($user);

        try {
            $token = $user->createToken('auth_token')->plainTextToken;
        } catch (\Exception $e) {
            // Jika gagal membuat token (misal unique constraint violation),
            // bersihkan semua token auth_token lama dan coba lagi
            $user->tokens()->where('name', 'auth_token')->delete();
            try {
                $token = $user->createToken('auth_token')->plainTextToken;
            } catch (\Exception $eRetry) {
                return $this->error('Gagal membuat sesi login. Silakan coba lagi.', 500);
            }
        }

        return $this->success([
            'user' => [
                'id' => $user->id,
                'email' => $user->email,
                'role' => $user->role?->nama_role ?? 'Unknown',
                'instansi_id' => $user->instansi_id,
                'outlet_id' => $user->outlet_id,
                'nama_lengkap' => $profil ? $profil->nama_lengkap : 'User',
            ],
            'permissions' => $permissions,
            'access_token' => $token,
            'token_type' => 'Bearer',
        ], 'Login berhasil');
    }

    public function registerSuperAdmin(RegisterSuperAdminRequest $request)
    {
        $roleSuperAdmin = role::where('nama_role', 'Super Admin')->first();

        if (! $roleSuperAdmin) {
            return $this->error('Role Super Admin belum tersedia. Jalankan seeder terlebih dahulu.', 500);
        }

        $user = User::create([
            'name' => $request->name,
            'email' => $request->email,
            'password' => Hash::make($request->password),
            'role_id' => $roleSuperAdmin->id,
            'instansi_id' => null,
            'outlet_id' => null,
        ]);

        $token = $user->createToken('auth_token')->plainTextToken;

        return $this->success([
            'user' => [
                'id' => $user->id,
                'name' => $user->name,
                'email' => $user->email,
                'role' => $user->role?->nama_role ?? 'Unknown',
            ],
            'access_token' => $token,
            'token_type' => 'Bearer',
        ], 'Registrasi Super Admin berhasil', 201);
    }

    public function logout(Request $request)
    {
        $request->user()->tokens()->delete();

        return $this->success(null, 'Logout berhasil');
    }
}
