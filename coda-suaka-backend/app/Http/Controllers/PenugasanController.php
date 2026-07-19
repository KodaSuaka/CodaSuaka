<?php

namespace App\Http\Controllers;

use App\Http\Requests\StorepenugasanRequest;
use App\Http\Requests\UpdatepenugasanRequest;
use App\Models\penugasan;
use App\Traits\ApiResponse;
use Illuminate\Http\Request;

class PenugasanController extends Controller
{
    use ApiResponse;

    public function __construct()
    {
        $this->authorizeResource(penugasan::class, 'penugasan');
    }

    /**
     * GET /api/penugasans?divisi_id=xxx&status=xxx&penanggung_jawab_id=xxx
     */
    public function index(Request $request)
    {
        $user = $request->user();
        $query = penugasan::with(['penanggungJawab.user', 'divisi', 'pembuat']);

        // Owner/Admin lihat semua di instansi, karyawan lihat tugas sendiri
        if ($user->role?->nama_role !== 'Owner') {
            $karyawan = $user->profilKaryawan;
            if ($karyawan) {
                $query->where('penanggung_jawab_id', $karyawan->id);
            }
        }

        if ($request->has('divisi_id')) {
            $query->where('divisi_id', $request->divisi_id);
        }

        if ($request->has('status')) {
            $query->where('status', $request->status);
        }

        if ($request->has('penanggung_jawab_id')) {
            $query->where('penanggung_jawab_id', $request->penanggung_jawab_id);
        }

        $penugasans = $query->orderBy('created_at', 'desc')->get();
        return $this->success($penugasans);
    }

    /**
     * POST /api/penugasans
     */
    public function store(StorepenugasanRequest $request)
    {
        $penugasan = penugasan::create([
            'judul' => $request->judul,
            'deskripsi' => $request->deskripsi,
            'penanggung_jawab_id' => $request->penanggung_jawab_id,
            'divisi_id' => $request->divisi_id,
            'tenggat' => $request->tenggat,
            'status' => $request->status ?? 'belum',
            'created_by' => $request->user()->id,
        ]);

        $penugasan->load(['penanggungJawab.user', 'divisi', 'pembuat']);

        return $this->success($penugasan, 'Tugas berhasil ditambahkan', 201);
    }

    /**
     * GET /api/penugasans/{penugasan}
     */
    public function show(penugasan $penugasan)
    {
        $penugasan->load(['penanggungJawab.user', 'divisi', 'pembuat']);
        return $this->success($penugasan);
    }

    /**
     * PUT /api/penugasans/{penugasan}
     */
    public function update(UpdatepenugasanRequest $request, penugasan $penugasan)
    {
        $penugasan->update($request->only(['judul', 'deskripsi', 'penanggung_jawab_id', 'divisi_id', 'tenggat', 'status']));
        $penugasan->load(['penanggungJawab.user', 'divisi', 'pembuat']);

        return $this->success($penugasan, 'Tugas berhasil diperbarui');
    }

    /**
     * DELETE /api/penugasans/{penugasan}
     */
    public function destroy(penugasan $penugasan)
    {
        $penugasan->delete();
        return $this->success(null, 'Tugas berhasil dihapus');
    }
}
