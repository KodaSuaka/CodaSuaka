<?php

namespace App\Http\Controllers;

use App\Models\instansi;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

class InstansiController extends Controller
{
    public function __construct()
    {
        $this->authorizeResource(instansi::class, 'instansi');
    }

    /**
     * GET /api/instansi
     * Profil instansi user yang sedang login
     */
    public function show(Request $request)
    {
        $instansi = instansi::with(['paket'])
            ->where('id', $request->user()->instansi_id)
            ->first();

        if (!$instansi) {
            return response()->json(['status' => 'error', 'message' => 'Instansi tidak ditemukan'], 404);
        }

        return response()->json(['status' => 'success', 'data' => $instansi]);
    }

    /**
     * PUT /api/instansi
     * Update data instansi
     */
    public function update(Request $request)
    {
        $instansi = instansi::findOrFail($request->user()->instansi_id);

        $validator = Validator::make($request->all(), [
            'nama_instansi' => 'sometimes|required|string|max:255',
        ]);

        if ($validator->fails()) {
            return response()->json(['status' => 'error', 'message' => 'Validasi gagal', 'errors' => $validator->errors()], 422);
        }

        $instansi->update($request->only(['nama_instansi']));
        return response()->json(['status' => 'success', 'message' => 'Instansi berhasil diperbarui', 'data' => $instansi]);
    }
}
