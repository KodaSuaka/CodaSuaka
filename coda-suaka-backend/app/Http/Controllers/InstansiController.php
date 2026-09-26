<?php

namespace App\Http\Controllers;

use App\Http\Requests\UpdateInstansiRequest;
use App\Models\Instansi;
use App\Traits\ApiResponse;
use Illuminate\Http\Request;

class InstansiController extends Controller
{
    use ApiResponse;

    public function __construct()
    {
        // authorizeResource tidak digunakan karena route /api/instansi
        // tidak memiliki parameter {instansi} untuk model resolution.
        // Authorization dilakukan manual di setiap method.
    }

    /**
     * GET /api/instansi
     * Profil instansi user yang sedang login
     */
    public function show(Request $request)
    {
        $instansi = Instansi::with(['paket'])
            ->where('id', $request->user()->instansi_id)
            ->first();

        if (! $instansi) {
            return $this->error('Instansi tidak ditemukan', 404);
        }

        $this->authorize('view', $instansi);

        return $this->success($instansi);
    }

    /**
     * PUT /api/instansi
     * Update data instansi
     */
    public function update(UpdateInstansiRequest $request)
    {
        $instansi = Instansi::findOrFail($request->user()->instansi_id);

        $this->authorize('update', $instansi);

        $data = $request->only(['nama_instansi', 'jam_operasional']);
        $instansi->update($data);

        return $this->success($instansi, 'Instansi berhasil diperbarui');
    }
}
