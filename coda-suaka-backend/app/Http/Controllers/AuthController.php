<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use App\Models\User;
use App\Models\instansi;
use App\Models\role;
use App\Models\karyawan;
use Illuminate\Support\Facades\Hash;
use App\Http\Requests\RegisterRequest;
use App\Http\Requests\LoginRequest;

class AuthController extends Controller
{
    public function register(RegisterRequest $request){
        $instansi = instansi::create([
            'nama_instansi'=>$request->nama_instansi,
            'paket_id'=>null
        ]);
        $RoleOwner = role::where('nama_role','Owner')->first();

        if (!$RoleOwner) {
            return response()->json([
                'status'=>'error',
                'massage'=>'Role belum tersedia'
            ],500);
        }
        $user = User::create([
            'name'=> $request->nama_pemilik,
            'email'=> $request->email,
            'password'=>Hash::make($request->password),
            'role_id'=> $RoleOwner->id,
            'instansi_id'=>$instansi->id,
            'outlet_id'=> null,
        ]);
        $karyawan = karyawan::create([
            'user_id'=> $user->id,
            'nama_lengkap'=> $request->nama_pemilik,
            'kontak'=> null,
            'foto_profil'=> null,
        ]);

        $token = $user->createToken('auth_token')->plainTextToken;

        return response()->json([
            'status' => 'success',
            'message' => 'Registrasi Owner dan Instansi berhasil',
            'data' => [
                'user' => $user->load('role'),
                'access_token' => $token,
                'token_type' => 'Bearer'
            ]
        ], 201);

    }
    public function login(LoginRequest $request){
        $user = User::where('email', $request->email)->first();
        if (!$user || !Hash::check($request->password, $user->password)) {
            return response()->json([
                'status' => 'error',
                'message' => 'Email atau Password yang Anda masukkan salah.'
            ], 401);
        }
        $profil = karyawan::where('user_id', $user->id)->first();

        $token = $user->createToken('auth_token')->plainTextToken;

        return response()->json([
            'status' => 'success',
            'message' => 'Login berhasil',
            'data' => [
                'user' => [
                    'id' => $user->id,
                    'email' => $user->email,
                    'role' => $user->role->nama_role,
                    'instansi_id' => $user->instansi_id,
                    'outlet_id' => $user->outlet_id,
                    'nama_lengkap' => $profil ? $profil->nama_lengkap : 'User'
                ],
                'access_token' => $token,
                'token_type' => 'Bearer'
            ]
        ], 200);
    }

    public function logout(Request $request){
        $request->user()->currentAccessToken()->delete();

        return response()->json([
            'status' => 'success',
            'message' => 'Logout berhasil'
        ], 200);
    }
}
