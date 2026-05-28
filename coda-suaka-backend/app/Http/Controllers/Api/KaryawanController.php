<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use App\Models\Karyawan;
use App\Models\Pemilik;
use App\Models\Toko;

class KaryawanController extends Controller
{
    // --- 1. TAMPILKAN DAFTAR KARYAWAN ---
    public function index(Request $request)
    {
        $user = $request->user();

        // Pastikan hanya Owner yang bisa melihat daftar semua karyawan
        if ($user->role !== 'pemilik') {
            return response()->json([
                'status' => 'error',
                'message' => 'Akses ditolak. Hanya pemilik yang dapat melihat daftar karyawan.'
            ], 403);
        }

        // Cari toko milik owner ini
        $pemilik = Pemilik::where('id_pengguna', $user->id)->first();
        $toko = Toko::where('id_pemilik', $pemilik->id)->first();

        // Tarik semua karyawan yang id_Toko-nya sama dengan toko owner
        // Kita juga gunakan with('user') agar email dan no HP-nya ikut terbawa!
        $karyawan = Karyawan::with('user:id,email,nomor_telp,is_active')
                            ->where('id_Toko', $toko->id)
                            ->get();

        return response()->json([
            'status' => 'success',
            'message' => 'Berhasil mengambil daftar karyawan.',
            'data' => $karyawan
        ], 200);
    }

    // --- 2. UPDATE STATUS/POSISI KARYAWAN ---
    public function update(Request $request, $id)
    {
        $user = $request->user();

        if ($user->role !== 'pemilik') {
            return response()->json([
                'status' => 'error',
                'message' => 'Akses ditolak.'
            ], 403);
        }

        $request->validate([
            'posisi' => 'sometimes|in:kasir,pramuniaga',
            'status_kerja' => 'sometimes|in:bebas,bekerja,libur'
        ]);

        // Cari karyawan berdasarkan ID yang dikirim
        $karyawan = Karyawan::find($id);

        if (!$karyawan) {
            return response()->json([
                'status' => 'error',
                'message' => 'Data karyawan tidak ditemukan.'
            ], 404);
        }

        // Update data
        $karyawan->update($request->only(['posisi', 'status_kerja']));

        return response()->json([
            'status' => 'success',
            'message' => 'Data karyawan berhasil diperbarui.',
            'data' => $karyawan
        ], 200);
    }
}