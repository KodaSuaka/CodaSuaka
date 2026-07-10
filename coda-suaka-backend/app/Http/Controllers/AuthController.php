<?php

namespace App\Http\Controllers;

use App\Models\User;
use App\Models\instansi;
use App\Models\role;
use App\Models\karyawan;
use App\Traits\ApiResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Hash;
use App\Http\Requests\RegisterRequest;
use App\Http\Requests\LoginRequest;
use App\Services\PermissionService;

class AuthController extends Controller
{
    use ApiResponse;

    public function register(RegisterRequest $request){
        $RoleOwner = role::where('nama_role','Owner')->first();

        if (!$RoleOwner) {
            return $this->error('Role belum tersedia', 500);
        }

        $user = DB::transaction(function () use ($request, $RoleOwner) {
            $instansi = instansi::create([
                'nama_instansi'=>$request->nama_instansi,
                'paket_id'=>null
            ]);

            $user = User::create([
                'name'=> $request->nama_pemilik,
                'email'=> $request->email,
                'password'=>Hash::make($request->password),
                'role_id'=> $RoleOwner->id,
                'instansi_id'=>$instansi->id,
                'outlet_id'=> null,
            ]);

            karyawan::create([
                'user_id'=> $user->id,
                'nama_lengkap'=> $request->nama_pemilik,
                'kontak'=> null,
                'foto_profil'=> null,
            ]);

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
            'token_type' => 'Bearer'
        ], 'Registrasi Owner dan Instansi berhasil', 201);

    }
    public function login(LoginRequest $request){
        $user = User::where('email', $request->email)->first();
        if (!$user || !Hash::check($request->password, $user->password)) {
            return $this->error('Email atau Password yang Anda masukkan salah.', 401);
        }
        $profil = karyawan::where('user_id', $user->id)->first();

        $permissions = app(PermissionService::class)->getUserPermissions($user);

        $token = $user->createToken('auth_token')->plainTextToken;

        return $this->success([
            'user' => [
                'id' => $user->id,
                'email' => $user->email,
                'role' => $user->role?->nama_role ?? 'Unknown',
                'instansi_id' => $user->instansi_id,
                'outlet_id' => $user->outlet_id,
                'nama_lengkap' => $profil ? $profil->nama_lengkap : 'User'
            ],
            'permissions' => $permissions,
            'access_token' => $token,
            'token_type' => 'Bearer'
        ], 'Login berhasil');
    }

    public function logout(Request $request){
        $request->user()->currentAccessToken()->delete();

        return $this->success(null, 'Logout berhasil');
    }
}
