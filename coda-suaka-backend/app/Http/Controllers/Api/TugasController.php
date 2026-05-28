<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use App\Models\Tugas;
use App\Models\PenugasanKaryawan;
use App\Models\Pemilik;
use App\Models\Karyawan;
use App\Models\Toko;
use Illuminate\Support\Facades\DB;
use Carbon\Carbon;

class TugasController extends Controller
{
    // --- 1. OWNER: BERIKAN TUGAS KE KARYAWAN ---
    public function assignTugas(Request $request)
    {
        $user = $request->user();
        if ($user->role !== 'pemilik') return response()->json(['message' => 'Akses ditolak.'], 403);

        $request->validate([
            'id_karyawan' => 'required|exists:karyawan,id',
            'nama_tugas' => 'required|string',
            'poin' => 'required|integer',
            'tanggal_tugas' => 'required|date',
            'catatan_tambahan' => 'nullable|string'
        ]);

        $pemilik = Pemilik::where('id_pengguna', $user->id)->first();
        $toko = Toko::where('id_pemilik', $pemilik->id)->first();

        DB::beginTransaction();
        try {
            // 1. Buat Data Master Tugas
            $masterTugas = Tugas::create([
                'id_Toko' => $toko->id,
                'nama_tugas' => $request->nama_tugas,
                'poin' => $request->poin
            ]);

            // 2. Langsung tugaskan ke Karyawan tersebut
            $penugasan = PenugasanKaryawan::create([
                'id_karyawan' => $request->id_karyawan,
                'id_master_tugas' => $masterTugas->id,
                'tanggal_tugas' => $request->tanggal_tugas,
                'status' => 'menunggu',
                'catatan_tambahan' => $request->catatan_tambahan
            ]);

            DB::commit();

            return response()->json([
                'status' => 'success',
                'message' => 'Tugas berhasil diberikan!',
                'data' => $penugasan->load('masterTugas')
            ], 201);
        } catch (\Exception $e) {
            DB::rollBack();
            return response()->json(['message' => $e->getMessage()], 500);
        }
    }

    // --- 2. KARYAWAN: LIHAT DAFTAR TUGAS SAYA ---
    public function myTugas(Request $request)
    {
        $user = $request->user();
        if ($user->role !== 'karyawan') return response()->json(['message' => 'Akses ditolak.'], 403);

        $karyawan = Karyawan::where('id_pengguna', $user->id)->first();

        // Ambil penugasan beserta info detail master tugasnya
        $daftarTugas = PenugasanKaryawan::with('masterTugas')
                        ->where('id_karyawan', $karyawan->id)
                        ->orderBy('tanggal_tugas', 'desc')
                        ->get();

        return response()->json([
            'status' => 'success',
            'data' => $daftarTugas
        ], 200);
    }

    // --- 3. KARYAWAN: UBAH STATUS TUGAS (Otomatis Dapat Poin!) ---
    public function updateStatusTugas(Request $request, $id)
    {
        $user = $request->user();
        if ($user->role !== 'karyawan') return response()->json(['message' => 'Akses ditolak.'], 403);

        $request->validate([
            'status' => 'required|in:dikerjakan,selesai'
        ]);

        $penugasan = PenugasanKaryawan::with('masterTugas')->find($id);

        if (!$penugasan) return response()->json(['message' => 'Tugas tidak ditemukan.'], 404);
        if ($penugasan->status === 'selesai') return response()->json(['message' => 'Tugas ini sudah selesai.'], 400);

        DB::beginTransaction();
        try {
            // Update status tugas
            $penugasan->status = $request->status;

            // Jika statusnya selesai, catat waktu dan berikan POIN ke Karyawan
            if ($request->status === 'selesai') {
                $penugasan->waktu_diselesaikan = Carbon::now();
                
                $karyawan = Karyawan::find($penugasan->id_karyawan);
                // Tambahkan poin performa!
                $karyawan->increment('poin_performa', $penugasan->masterTugas->poin);
            }

            $penugasan->save();
            DB::commit();

            return response()->json([
                'status' => 'success',
                'message' => 'Status tugas diperbarui.',
                'data' => $penugasan
            ], 200);

        } catch (\Exception $e) {
            DB::rollBack();
            return response()->json(['message' => $e->getMessage()], 500);
        }
    }
}