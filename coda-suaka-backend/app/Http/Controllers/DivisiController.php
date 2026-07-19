<?php

namespace App\Http\Controllers;

use App\Http\Requests\StoreDivisiRequest;
use App\Http\Requests\UpdateDivisiRequest;
use App\Models\Divisi;
use App\Traits\ApiResponse;
use Illuminate\Http\Request;

class DivisiController extends Controller
{
    use ApiResponse;

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
        return $this->success($divisis);
    }

    /**
     * POST /api/divisis
     */
    public function store(StoreDivisiRequest $request)
    {
        $divisi = Divisi::create($request->only(['nama_divisi', 'deskripsi', 'ketua_karyawan_id', 'outlet_id']));
        $divisi->load(['ketuaKaryawan', 'outlet']);

        return $this->success($divisi, 'Divisi berhasil ditambahkan', 201);
    }

    /**
     * GET /api/divisis/{divisi}
     */
    public function show(Divisi $divisi)
    {
        $divisi->load(['ketuaKaryawan', 'outlet', 'anggota.karyawan.user']);
        return $this->success($divisi);
    }

    /**
     * PUT /api/divisis/{divisi}
     */
    public function update(UpdateDivisiRequest $request, Divisi $divisi)
    {
        $divisi->update($request->only(['nama_divisi', 'deskripsi', 'ketua_karyawan_id', 'outlet_id']));
        $divisi->load(['ketuaKaryawan', 'outlet']);

        return $this->success($divisi, 'Divisi berhasil diperbarui');
    }

    /**
     * DELETE /api/divisis/{divisi}
     */
    public function destroy(Divisi $divisi)
    {
        $divisi->delete();
        return $this->success(null, 'Divisi berhasil dihapus');
    }
}
