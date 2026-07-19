<?php

namespace App\Http\Controllers;

use App\Http\Requests\StoreAnggotaDivisiRequest;
use App\Models\AnggotaDivisi;
use App\Traits\ApiResponse;
use Illuminate\Http\Request;

class AnggotaDivisiController extends Controller
{
    use ApiResponse;

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
        return $this->success($anggota);
    }

    /**
     * POST /api/anggota-divisis
     * Tambah anggota ke divisi
     */
    public function store(StoreAnggotaDivisiRequest $request)
    {
        // Cek duplicate
        $exists = AnggotaDivisi::where('divisi_id', $request->divisi_id)
            ->where('karyawan_id', $request->karyawan_id)
            ->exists();

        if ($exists) {
            return $this->error('Karyawan sudah terdaftar di divisi ini', 409);
        }

        $anggota = AnggotaDivisi::create([
            'divisi_id' => $request->divisi_id,
            'karyawan_id' => $request->karyawan_id,
        ]);

        $anggota->load(['divisi', 'karyawan.user']);

        return $this->success($anggota, 'Anggota berhasil ditambahkan ke divisi', 201);
    }

    /**
     * DELETE /api/anggota-divisis/{anggotaDivisi}
     */
    public function destroy(AnggotaDivisi $anggotaDivisi)
    {
        $anggotaDivisi->delete();
        return $this->success(null, 'Anggota berhasil dihapus dari divisi');
    }
}
