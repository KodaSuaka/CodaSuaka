<?php

namespace App\Http\Controllers;

use App\Models\Divisi;
use App\Models\karyawan;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;
use Illuminate\Validation\Rule;

class DivisiController extends Controller
{
    public function __construct()
    {
        $this->authorizeResource(Divisi::class, 'divisi');
    }

    /**
     * GET /api/divisis?outlet_id=xxx
     */
    public function index(Request $request)
    {
        $query = Divisi::with(['ketuaKaryawan', 'outlet', 'anggota']);

        if ($request->has('outlet_id')) {
            $query->where('outlet_id', $request->outlet_id);
        }

        $divisis = $query->orderBy('nama_divisi')->get();
        return response()->json(['status' => 'success', 'data' => $divisis]);
    }

    /**
     * POST /api/divisis
     */
    public function store(Request $request)
    {
        $user = $request->user();

        $validator = Validator::make($request->all(), [
            'nama_divisi' => 'required|string|max:100',
            'deskripsi' => 'nullable|string',
            'ketua_karyawan_id' => [
                'nullable',
                function ($attribute, $value, $fail) use ($user) {
                    if ($value && !karyawan::whereHas('user', function ($q) use ($user) {
                            $q->where('instansi_id', $user->instansi_id);
                        })->where('id', $value)->exists()) {
                        $fail('Karyawan tidak ditemukan di instansi Anda');
                    }
                },
            ],
            'outlet_id' => [
                'required',
                Rule::exists('outlets', 'id')->where('instansi_id', $user->instansi_id),
            ],
        ]);

        if ($validator->fails()) {
            return response()->json(['status' => 'error', 'message' => 'Validasi gagal', 'errors' => $validator->errors()], 422);
        }

        $divisi = Divisi::create($request->only(['nama_divisi', 'deskripsi', 'ketua_karyawan_id', 'outlet_id']));
        $divisi->load(['ketuaKaryawan', 'outlet']);

        return response()->json([
            'status' => 'success',
            'message' => 'Divisi berhasil ditambahkan',
            'data' => $divisi
        ], 201);
    }

    /**
     * GET /api/divisis/{divisi}
     */
    public function show(Divisi $divisi)
    {
        $divisi->load(['ketuaKaryawan', 'outlet', 'anggota.karyawan.user']);
        return response()->json(['status' => 'success', 'data' => $divisi]);
    }

    /**
     * PUT /api/divisis/{divisi}
     */
    public function update(Request $request, Divisi $divisi)
    {
        $user = $request->user();

        $validator = Validator::make($request->all(), [
            'nama_divisi' => 'sometimes|required|string|max:100',
            'deskripsi' => 'nullable|string',
            'ketua_karyawan_id' => [
                'nullable',
                function ($attribute, $value, $fail) use ($user) {
                    if ($value && !karyawan::whereHas('user', function ($q) use ($user) {
                            $q->where('instansi_id', $user->instansi_id);
                        })->where('id', $value)->exists()) {
                        $fail('Karyawan tidak ditemukan di instansi Anda');
                    }
                },
            ],
            'outlet_id' => [
                'sometimes',
                'required',
                Rule::exists('outlets', 'id')->where('instansi_id', $user->instansi_id),
            ],
        ]);

        if ($validator->fails()) {
            return response()->json(['status' => 'error', 'message' => 'Validasi gagal', 'errors' => $validator->errors()], 422);
        }

        $divisi->update($request->only(['nama_divisi', 'deskripsi', 'ketua_karyawan_id', 'outlet_id']));
        $divisi->load(['ketuaKaryawan', 'outlet']);

        return response()->json([
            'status' => 'success',
            'message' => 'Divisi berhasil diperbarui',
            'data' => $divisi
        ]);
    }

    /**
     * DELETE /api/divisis/{divisi}
     */
    public function destroy(Divisi $divisi)
    {
        $divisi->delete();
        return response()->json(['status' => 'success', 'message' => 'Divisi berhasil dihapus']);
    }
}
