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

        // Select kolom yang dibutuhkan + eager loading relasi
        $query = penugasan::with([
            'penanggungJawab.user' => fn($q) => $q->select(['id', 'name', 'role_id']),
            'divisi' => fn($q) => $q->select(['id', 'nama_divisi']),
        ])->select([
            'id', 'judul', 'deskripsi', 'penanggung_jawab_id',
            'divisi_id', 'tenggat', 'status', 'urgency', 'poin',
            'created_by', 'created_at', 'updated_at',
        ]);

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
        $urgency = $request->urgency ?? 'sedang';

        $penugasan = penugasan::create([
            'judul' => $request->judul,
            'deskripsi' => $request->deskripsi,
            'penanggung_jawab_id' => $request->penanggung_jawab_id,
            'divisi_id' => $request->divisi_id,
            'tenggat' => $request->tenggat,
            'status' => $request->status ?? 'belum',
            'urgency' => $urgency,
            'poin' => penugasan::getPoinForUrgency($urgency),
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
        $data = $request->only(['judul', 'deskripsi', 'penanggung_jawab_id', 'divisi_id', 'tenggat', 'status', 'urgency']);

        // Jika status berubah menjadi 'selesai', hitung poin berdasarkan urgency
        if (isset($data['status']) && $data['status'] === 'selesai' && $penugasan->status !== 'selesai') {
            $urgency = $data['urgency'] ?? $penugasan->urgency;
            $data['poin'] = penugasan::getPoinForUrgency($urgency);
        }

        // Jika urgency berubah dan tugas belum selesai, update poin
        if (isset($data['urgency']) && $penugasan->status !== 'selesai') {
            $data['poin'] = penugasan::getPoinForUrgency($data['urgency']);
        }

        $penugasan->update($data);
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
