<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use App\Models\User;
use App\Models\Pemilik;
use App\Models\Toko;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Str;

class AuthController extends Controller
{
    // --- 1. REGISTRASI OWNER (Sesuai Figma) ---
    public function registerOwner(Request $request)
    {
        // 1. Validasi Input dari Figma
        $request->validate([
            'nama' => 'required|string|max:255',
            'email' => 'required|string|email|max:255|unique:users',
            'nomor_telp' => 'required|string|unique:users',
            'password' => 'required|string|min:6',
            'alamat' => 'required|string'
        ]);

        DB::beginTransaction();

        try {
            // 2. Buat Akun User (Pintu Masuk)
            $user = User::create([
                'nomor_telp' => $request->nomor_telp,
                'email' => $request->email,
                'password' => Hash::make($request->password),
                'role' => 'pemilik',
                'is_active' => true
            ]);

            // 3. Buat Profil Pemilik
            $pemilik = Pemilik::create([
                'id_pengguna' => $user->id,
                'nama_lengkap' => $request->nama,
                // nik_ktp, akun_bank, dll dibiarkan null dulu sesuai MVP
            ]);

            // 4. Buat Toko Default Pertama si Bos
            // Ini wajib agar dia nanti bisa nambahin karyawan
            $toko = Toko::create([
                'id_pemilik' => $pemilik->id,
                'nama' => 'Toko ' . $request->nama, // Nama toko default sementara
                'slug' => Str::slug('Toko ' . $request->nama . ' ' . time()), // Biar pasti unik
                'alamat_lengkap' => $request->alamat,
                'berlangganan' => 'gratis'
            ]);

            DB::commit();

            // 5. Buat Token Langsung Login
            $token = $user->createToken('owner-mobile-token')->plainTextToken;

            return response()->json([
                'status' => 'success',
                'message' => 'Registrasi Owner dan Toko berhasil!',
                'data' => [
                    'user' => $user,
                    'pemilik' => $pemilik,
                    'toko' => $toko
                ],
                'access_token' => $token
            ], 201);

        } catch (\Exception $e) {
            DB::rollBack();
            return response()->json([
                'status' => 'error',
                'message' => 'Registrasi gagal: ' . $e->getMessage()
            ], 500);
        }
    }

    
    public function registerKaryawan(Request $request)
    {
        // 1. Validasi Input Karyawan
        $request->validate([
            'nama' => 'required|string|max:255',
            'email' => 'required|string|email|max:255|unique:users',
            'nomor_telp' => 'required|string|unique:users',
            'password' => 'required|string|min:6',
            'posisi' => 'required|in:kasir,pramuniaga' // Sesuai ENUM di ERD
        ]);

        // 2. Ambil data Toko milik Owner yang sedang login
        $userOwner = $request->user();
        
        // Cari profil pemiliknya
        $pemilik = Pemilik::where('id_pengguna', $userOwner->id)->first();
        if (!$pemilik) {
            return response()->json([
                'status' => 'error',
                'message' => 'Akses ditolak. Profil pemilik tidak ditemukan.'
            ], 403);
        }

        // Cari toko milik pemilik tersebut (Asumsi 1 owner = 1 toko untuk MVP)
        $toko = Toko::where('id_pemilik', $pemilik->id)->first();
        if (!$toko) {
            return response()->json([
                'status' => 'error',
                'message' => 'Toko tidak ditemukan. Silakan buat toko terlebih dahulu.'
            ], 404);
        }

        DB::beginTransaction();

        try {
            // 3. Buat Akun User untuk Karyawan
            $userKaryawan = User::create([
                'nomor_telp' => $request->nomor_telp,
                'email' => $request->email,
                'password' => Hash::make($request->password),
                'role' => 'karyawan',
                'is_active' => true
            ]);

            // 4. Buat Profil Karyawan (Otomatis terikat ke id_Toko si bos)
            $karyawan = \App\Models\Karyawan::create([
                'id_pengguna' => $userKaryawan->id,
                'id_Toko' => $toko->id,
                'nama_lengkap' => $request->nama,
                'posisi' => $request->posisi,
                'status_kerja' => 'bebas', // Default sesuai ERD
                'poin_performa' => 0,
                'tanggal_bergabung' => now()
            ]);

            DB::commit();

            return response()->json([
                'status' => 'success',
                'message' => 'Karyawan berhasil didaftarkan oleh Owner!',
                'data' => [
                    'account' => $userKaryawan,
                    'profile' => $karyawan
                ]
            ], 201);

        } catch (\Exception $e) {
            DB::rollBack();
            return response()->json([
                'status' => 'error',
                'message' => 'Gagal mendaftarkan karyawan: ' . $e->getMessage()
            ], 500);
        }
    }

    // --- 2. LOGIN (Sesuai Figma) ---
    public function login(Request $request)
    {
        // Di Figma tulisannya 'Username'. Kita buat fleksibel!
        // User bisa login pakai Email ATAU Nomor Telepon.
        $request->validate([
            'username' => 'required|string',
            'password' => 'required'
        ]);

        // Cari berdasarkan Email atau No. Telp
        $user = User::where('email', $request->username)
                    ->orWhere('nomor_telp', $request->username)
                    ->first();

        // Cek kecocokan
        if (!$user || !Hash::check($request->password, $user->password)) {
            return response()->json([
                'status' => 'error',
                'message' => 'Username atau Password salah.'
            ], 401);
        }

        // Cek apakah akun aktif
        if (!$user->is_active) {
            return response()->json([
                'status' => 'error',
                'message' => 'Akun Anda sedang dinonaktifkan.'
            ], 403);
        }

        // Buat Token
        $token = $user->createToken('mobile-token')->plainTextToken;

        // Tarik data profil spesifik berdasarkan Role agar Frontend mudah baca datanya
        $profil = null;
        if ($user->role === 'pemilik') {
            $profil = Pemilik::with('toko')->where('id_pengguna', $user->id)->first();
        } else if ($user->role === 'karyawan') {
            $profil = \App\Models\Karyawan::with('toko')->where('id_pengguna', $user->id)->first();
        }

        return response()->json([
            'status' => 'success',
            'message' => 'Login Berhasil',
            'role' => $user->role,
            'data_profil' => $profil,
            'access_token' => $token
        ], 200);
    }


    public function logout(Request $request)
    {
        // Menghapus token yang sedang digunakan saat ini
        $request->user()->currentAccessToken()->delete();

        return response()->json([
            'status' => 'success',
            'message' => 'Berhasil logout dan token telah dihapus.'
        ], 200);
    }
}