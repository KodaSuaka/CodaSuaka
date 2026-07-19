<?php

namespace App\Http\Controllers;

use App\Http\Requests\StorejadwalRequest;
use App\Http\Requests\UpdatejadwalRequest;
use App\Models\jadwal;
use App\Traits\ApiResponse;
use Illuminate\Http\Request;

class JadwalController extends Controller
{
    use ApiResponse;

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
        return $this->success($jadwals);
    }

    /**
     * POST /api/jadwals
     */
    public function store(StorejadwalRequest $request)
    {
        $jadwal = jadwal::create([
            'nama_event' => $request->nama_event,
            'deskripsi' => $request->deskripsi,
            'tanggal' => $request->tanggal,
            'kategori' => $request->kategori,
            'outlet_id' => $request->outlet_id,
            'created_by' => $request->user()->id,
        ]);

        $jadwal->load(['outlet', 'pembuat']);

        return $this->success($jadwal, 'Event berhasil ditambahkan', 201);
    }

    /**
     * GET /api/jadwals/{jadwal}
     */
    public function show(jadwal $jadwal)
    {
        $jadwal->load(['outlet', 'pembuat']);
        return $this->success($jadwal);
    }

    /**
     * PUT /api/jadwals/{jadwal}
     */
    public function update(UpdatejadwalRequest $request, jadwal $jadwal)
    {
        $jadwal->update($request->only(['nama_event', 'deskripsi', 'tanggal', 'kategori', 'outlet_id']));
        $jadwal->load(['outlet', 'pembuat']);

        return $this->success($jadwal, 'Event berhasil diperbarui');
    }

    /**
     * DELETE /api/jadwals/{jadwal}
     */
    public function destroy(jadwal $jadwal)
    {
        $jadwal->delete();
        return $this->success(null, 'Event berhasil dihapus');
    }
}
