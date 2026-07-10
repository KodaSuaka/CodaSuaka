<?php

namespace App\Http\Controllers;

use App\Models\instansi;
use App\Traits\ApiResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

class InstansiController extends Controller
{
    use ApiResponse;

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
            return $this->error('Instansi tidak ditemukan', 404);
        }

        return $this->success($instansi);
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
            return $this->error('Validasi gagal', 422, $validator->errors());
        }

        $instansi->update($request->only(['nama_instansi']));
        return $this->success($instansi, 'Instansi berhasil diperbarui');
    }
}
