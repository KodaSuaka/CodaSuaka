<?php

namespace App\Http\Controllers;

use App\Models\penugasan;
use App\Models\Divisi;
use App\Models\karyawan;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

class PenugasanController extends Controller
{
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
        return response()->json(['status' => 'success', 'data' => $penugasans]);
    }

    /**
     * POST /api/penugasans
     */
    public function store(Request $request)
    {
        $user = $request->user();

        $validator = Validator::make($request->all(), [
            'judul' => 'required|string|max:200',
            'deskripsi' => 'nullable|string',
            'penanggung_jawab_id' => [
                'required',
                function ($attribute, $value, $fail) use ($user) {
                    if (!karyawan::where('id', $value)->exists()) {
                        $fail('Karyawan tidak ditemukan di instansi Anda');
                    }
                },
            ],
            'divisi_id' => [
                'nullable',
                function ($attribute, $value, $fail) use ($user) {
                    if ($value && !Divisi::where('id', $value)->exists()) {
                        $fail('Divisi tidak ditemukan di instansi Anda');
                    }
                },
            ],
            'tenggat' => 'nullable|date',
            'status' => 'sometimes|in:belum,proses,selesai,batal',
        ]);

        if ($validator->fails()) {
            return response()->json(['status' => 'error', 'message' => 'Validasi gagal', 'errors' => $validator->errors()], 422);
        }

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

        return response()->json([
            'status' => 'success',
            'message' => 'Tugas berhasil ditambahkan',
            'data' => $penugasan
        ], 201);
    }

    /**
     * GET /api/penugasans/{penugasan}
     */
    public function show(penugasan $penugasan)
    {
        $penugasan->load(['penanggungJawab.user', 'divisi', 'pembuat']);
        return response()->json(['status' => 'success', 'data' => $penugasan]);
    }

    /**
     * PUT /api/penugasans/{penugasan}
     */
    public function update(Request $request, penugasan $penugasan)
    {
        $user = $request->user();

        $validator = Validator::make($request->all(), [
            'judul' => 'sometimes|required|string|max:200',
            'deskripsi' => 'nullable|string',
            'penanggung_jawab_id' => [
                'sometimes',
                'required',
                function ($attribute, $value, $fail) use ($user) {
                    if (!karyawan::where('id', $value)->exists()) {
                        $fail('Karyawan tidak ditemukan di instansi Anda');
                    }
                },
            ],
            'divisi_id' => [
                'nullable',
                function ($attribute, $value, $fail) use ($user) {
                    if ($value && !Divisi::where('id', $value)->exists()) {
                        $fail('Divisi tidak ditemukan di instansi Anda');
                    }
                },
            ],
            'tenggat' => 'nullable|date',
            'status' => 'sometimes|in:belum,proses,selesai,batal',
        ]);

        if ($validator->fails()) {
            return response()->json(['status' => 'error', 'message' => 'Validasi gagal', 'errors' => $validator->errors()], 422);
        }

        $penugasan->update($request->only(['judul', 'deskripsi', 'penanggung_jawab_id', 'divisi_id', 'tenggat', 'status']));
        $penugasan->load(['penanggungJawab.user', 'divisi', 'pembuat']);

        return response()->json([
            'status' => 'success',
            'message' => 'Tugas berhasil diperbarui',
            'data' => $penugasan
        ]);
    }

    /**
     * DELETE /api/penugasans/{penugasan}
     */
    public function destroy(penugasan $penugasan)
    {
        $penugasan->delete();
        return response()->json(['status' => 'success', 'message' => 'Tugas berhasil dihapus']);
    }
}
