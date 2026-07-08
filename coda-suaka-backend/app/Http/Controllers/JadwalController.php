<?php

namespace App\Http\Controllers;

use App\Models\jadwal;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

class JadwalController extends Controller
{
    public function __construct()
    {
        $this->authorizeResource(jadwal::class, 'jadwal');
    }

    /**
     * GET /api/jadwals?outlet_id=xxx&bulan=x&tahun=xxxx
     */
    public function index(Request $request)
    {
        $query = jadwal::with(['outlet', 'pembuat']);

        if ($request->has('outlet_id')) {
            $query->where('outlet_id', $request->outlet_id);
        }

        if ($request->has('bulan') && $request->has('tahun')) {
            $query->whereMonth('tanggal', $request->bulan)
                  ->whereYear('tanggal', $request->tahun);
        }

        if ($request->has('tanggal')) {
            $query->where('tanggal', $request->tanggal);
        }

        $jadwals = $query->orderBy('tanggal', 'asc')->get();
        return response()->json(['status' => 'success', 'data' => $jadwals]);
    }

    /**
     * POST /api/jadwals
     */
    public function store(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'nama_event' => 'required|string|max:200',
            'deskripsi' => 'nullable|string',
            'tanggal' => 'required|date',
            'kategori' => 'required|in:meeting,training,event,libur,lainnya',
            'outlet_id' => 'nullable|exists:outlets,id',
        ]);

        if ($validator->fails()) {
            return response()->json(['status' => 'error', 'message' => 'Validasi gagal', 'errors' => $validator->errors()], 422);
        }

        $jadwal = jadwal::create([
            'nama_event' => $request->nama_event,
            'deskripsi' => $request->deskripsi,
            'tanggal' => $request->tanggal,
            'kategori' => $request->kategori,
            'outlet_id' => $request->outlet_id,
            'created_by' => $request->user()->id,
        ]);

        $jadwal->load(['outlet', 'pembuat']);

        return response()->json([
            'status' => 'success',
            'message' => 'Event berhasil ditambahkan',
            'data' => $jadwal
        ], 201);
    }

    /**
     * GET /api/jadwals/{jadwal}
     */
    public function show(jadwal $jadwal)
    {
        $jadwal->load(['outlet', 'pembuat']);
        return response()->json(['status' => 'success', 'data' => $jadwal]);
    }

    /**
     * PUT /api/jadwals/{jadwal}
     */
    public function update(Request $request, jadwal $jadwal)
    {
        $validator = Validator::make($request->all(), [
            'nama_event' => 'sometimes|required|string|max:200',
            'deskripsi' => 'nullable|string',
            'tanggal' => 'sometimes|required|date',
            'kategori' => 'sometimes|required|in:meeting,training,event,libur,lainnya',
            'outlet_id' => 'nullable|exists:outlets,id',
        ]);

        if ($validator->fails()) {
            return response()->json(['status' => 'error', 'message' => 'Validasi gagal', 'errors' => $validator->errors()], 422);
        }

        $jadwal->update($request->only(['nama_event', 'deskripsi', 'tanggal', 'kategori', 'outlet_id']));
        $jadwal->load(['outlet', 'pembuat']);

        return response()->json([
            'status' => 'success',
            'message' => 'Event berhasil diperbarui',
            'data' => $jadwal
        ]);
    }

    /**
     * DELETE /api/jadwals/{jadwal}
     */
    public function destroy(jadwal $jadwal)
    {
        $jadwal->delete();
        return response()->json(['status' => 'success', 'message' => 'Event berhasil dihapus']);
    }
}
