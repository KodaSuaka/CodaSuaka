<?php

namespace App\Http\Controllers;

use App\Models\paket;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

class PaketController extends Controller
{
    public function __construct()
    {
        $this->authorizeResource(paket::class, 'paket');
    }

    /**
     * GET /api/pakets
     * Semua paket aktif
     */
    public function index()
    {
        $pakets = paket::where('is_active', true)->orderBy('harga', 'asc')->get();
        return response()->json(['status' => 'success', 'data' => $pakets]);
    }

    /**
     * POST /api/pakets
     */
    public function store(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'nama_paket' => 'required|string|max:255',
            'harga' => 'required|numeric|min:0',
            'deskripsi' => 'nullable|string',
            'fitur' => 'nullable|string',
            'durasi_hari' => 'required|integer|min:1',
            'max_outlet' => 'nullable|integer|min:1',
            'max_karyawan_per_outlet' => 'nullable|integer|min:1',
            'is_active' => 'boolean',
        ]);

        if ($validator->fails()) {
            return response()->json(['status' => 'error', 'message' => 'Validasi gagal', 'errors' => $validator->errors()], 422);
        }

        $paket = paket::create($request->all());
        return response()->json(['status' => 'success', 'message' => 'Paket berhasil ditambahkan', 'data' => $paket], 201);
    }

    /**
     * GET /api/pakets/{paket}
     */
    public function show(paket $paket)
    {
        return response()->json(['status' => 'success', 'data' => $paket]);
    }

    /**
     * PUT /api/pakets/{paket}
     */
    public function update(Request $request, paket $paket)
    {
        $validator = Validator::make($request->all(), [
            'nama_paket' => 'sometimes|required|string|max:255',
            'harga' => 'sometimes|required|numeric|min:0',
            'deskripsi' => 'nullable|string',
            'fitur' => 'nullable|string',
            'durasi_hari' => 'sometimes|required|integer|min:1',
            'max_outlet' => 'nullable|integer|min:1',
            'max_karyawan_per_outlet' => 'nullable|integer|min:1',
            'is_active' => 'boolean',
        ]);

        if ($validator->fails()) {
            return response()->json(['status' => 'error', 'message' => 'Validasi gagal', 'errors' => $validator->errors()], 422);
        }

        $paket->update($request->all());
        return response()->json(['status' => 'success', 'message' => 'Paket berhasil diperbarui', 'data' => $paket]);
    }

    /**
     * DELETE /api/pakets/{paket}
     */
    public function destroy(paket $paket)
    {
        $paket->update(['is_active' => false]);
        return response()->json(['status' => 'success', 'message' => 'Paket berhasil dinonaktifkan']);
    }
}
