<?php

namespace App\Http\Controllers;

use App\Models\AnggotaDivisi;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

class AnggotaDivisiController extends Controller
{
    public function __construct()
    {
        $this->authorizeResource(AnggotaDivisi::class, 'anggotaDivisi');
    }

    /**
     * GET /api/anggota-divisis?divisi_id=xxx
     */
    public function index(Request $request)
    {
        $query = AnggotaDivisi::with(['divisi', 'karyawan.user']);

        if ($request->has('divisi_id')) {
            $query->where('divisi_id', $request->divisi_id);
        }

        $anggota = $query->orderBy('created_at')->get();
        return response()->json(['status' => 'success', 'data' => $anggota]);
    }

    /**
     * POST /api/anggota-divisis
     * Tambah anggota ke divisi
     */
    public function store(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'divisi_id' => 'required|exists:divisis,id',
            'karyawan_id' => 'required|exists:karyawans,id',
        ]);

        if ($validator->fails()) {
            return response()->json(['status' => 'error', 'message' => 'Validasi gagal', 'errors' => $validator->errors()], 422);
        }

        // Cek duplicate
        $exists = AnggotaDivisi::where('divisi_id', $request->divisi_id)
            ->where('karyawan_id', $request->karyawan_id)
            ->exists();

        if ($exists) {
            return response()->json(['status' => 'error', 'message' => 'Karyawan sudah terdaftar di divisi ini'], 409);
        }

        $anggota = AnggotaDivisi::create([
            'divisi_id' => $request->divisi_id,
            'karyawan_id' => $request->karyawan_id,
        ]);

        $anggota->load(['divisi', 'karyawan.user']);

        return response()->json([
            'status' => 'success',
            'message' => 'Anggota berhasil ditambahkan ke divisi',
            'data' => $anggota
        ], 201);
    }

    /**
     * DELETE /api/anggota-divisis/{anggotaDivisi}
     */
    public function destroy(AnggotaDivisi $anggotaDivisi)
    {
        $anggotaDivisi->delete();
        return response()->json(['status' => 'success', 'message' => 'Anggota berhasil dihapus dari divisi']);
    }
}
