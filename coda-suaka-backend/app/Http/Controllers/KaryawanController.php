<?php

namespace App\Http\Controllers;

use App\Models\karyawan;
use App\Models\User;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;
use Illuminate\Support\Str;

class KaryawanController extends Controller
{
    public function __construct()
    {
    }

    /**
     * GET /api/karyawans?outlet_id=xxx
     * Daftar karyawan dalam instansi user
     */
    public function index(Request $request)
    {
        $user = $request->user();
        $instansiId = $user->instansi_id;

        // Ambil semua user_id dalam instansi ini
        $userIds = User::where('instansi_id', $instansiId)->pluck('id');

        $query = karyawan::whereIn('user_id', $userIds)
            ->with(['user.role', 'outlet']);

        if ($request->has('outlet_id')) {
            $query->where('outlet_id', $request->outlet_id);
        }

        $karyawans = $query->orderBy('nama_lengkap')->get();

        return response()->json(['status' => 'success', 'data' => $karyawans]);
    }

    /**
     * GET /api/karyawans/me
     * Profil karyawan yang sedang login
     */
    public function me(Request $request)
    {
        $karyawan = karyawan::where('user_id', $request->user()->id)
            ->with(['user.role', 'outlet'])
            ->first();

        if (!$karyawan) {
            return response()->json(['status' => 'error', 'message' => 'Profil karyawan tidak ditemukan'], 404);
        }

        return response()->json(['status' => 'success', 'data' => $karyawan]);
    }

    /**
     * POST /api/karyawans
     * Tambah karyawan baru (dengan user account)
     */
    public function store(Request $request)
    {
        $user = $request->user();
        $instansiId = $user->instansi_id;

        // Hanya Owner yang boleh menambahkan karyawan baru
        if (!$user->role || $user->role->nama_role !== 'Owner') {
            return response()->json(['status' => 'error', 'message' => 'Hanya Owner yang dapat menambahkan karyawan'], 403);
        }

        $validator = Validator::make($request->all(), [
            'nama_lengkap' => 'required|string|max:255',
            'email' => 'required|email|unique:users,email',
            'password' => 'required|string|min:6',
            'kontak' => 'nullable|string|max:20',
            'alamat' => 'nullable|string',
            'role_id' => 'required|exists:roles,id',
            'outlet_id' => 'nullable|exists:outlets,id',
            'sisa_cuti' => 'nullable|integer|min:0',
        ]);

        if ($validator->fails()) {
            return response()->json(['status' => 'error', 'message' => 'Validasi gagal', 'errors' => $validator->errors()], 422);
        }

        // Buat user account
        $newUser = User::create([
            'name' => $request->nama_lengkap,
            'email' => $request->email,
            'password' => bcrypt($request->password),
            'role_id' => $request->role_id,
            'instansi_id' => $instansiId,
            'outlet_id' => $request->outlet_id,
        ]);

        // Buat profil karyawan
        $karyawan = karyawan::create([
            'user_id' => $newUser->id,
            'nama_lengkap' => $request->nama_lengkap,
            'kontak' => $request->kontak,
            'alamat' => $request->alamat,
            'foto_profil' => null,
            'outlet_id' => $request->outlet_id,
            'sisa_cuti' => $request->sisa_cuti ?? 0,
        ]);

        $karyawan->load(['user.role', 'outlet']);

        return response()->json([
            'status' => 'success',
            'message' => 'Karyawan berhasil ditambahkan',
            'data' => $karyawan
        ], 201);
    }

    /**
     * GET /api/karyawans/{karyawan}
     */
    public function show(karyawan $karyawan)
    {
        $karyawan->load(['user.role', 'outlet', 'divisi', 'anggotaDivisis']);
        return response()->json(['status' => 'success', 'data' => $karyawan]);
    }

    /**
     * PUT /api/karyawans/{karyawan}
     */
    public function update(Request $request, karyawan $karyawan)
    {
        $validator = Validator::make($request->all(), [
            'nama_lengkap' => 'sometimes|required|string|max:255',
            'kontak' => 'nullable|string|max:20',
            'alamat' => 'nullable|string',
            'outlet_id' => 'nullable|exists:outlets,id',
            'sisa_cuti' => 'nullable|integer|min:0',
            'foto_profil' => 'nullable|string',
        ]);

        if ($validator->fails()) {
            return response()->json(['status' => 'error', 'message' => 'Validasi gagal', 'errors' => $validator->errors()], 422);
        }

        $karyawan->update($request->only([
            'nama_lengkap', 'kontak', 'alamat', 'outlet_id', 'sisa_cuti', 'foto_profil'
        ]));

        $karyawan->load(['user.role', 'outlet']);

        return response()->json([
            'status' => 'success',
            'message' => 'Karyawan berhasil diperbarui',
            'data' => $karyawan
        ]);
    }

    /**
     * DELETE /api/karyawans/{karyawan}
     */
    public function destroy(karyawan $karyawan)
    {
        // Hapus profil karyawan terlebih dahulu, baru user (karena cascade)
        $karyawan->delete();
        $karyawan->user()->delete();
        return response()->json(['status' => 'success', 'message' => 'Karyawan berhasil dihapus']);
    }
}
