<?php

namespace App\Http\Controllers;

use App\Http\Requests\UpdateInstansiRequest;
use App\Models\instansi;
use App\Traits\ApiResponse;
use Illuminate\Http\Request;

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
    public function update(UpdateInstansiRequest $request)
    {
        $instansi = instansi::findOrFail($request->user()->instansi_id);

        $instansi->update($request->only(['nama_instansi']));
        return $this->success($instansi, 'Instansi berhasil diperbarui');
    }
}
