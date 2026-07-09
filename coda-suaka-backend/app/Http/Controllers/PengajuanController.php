<?php

namespace App\Http\Controllers;

use App\Models\pengajuan;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

class PengajuanController extends Controller
{
    public function __construct()
    {
        $this->authorizeResource(pengajuan::class, 'pengajuan');
    }

    /**
     * GET /api/pengajuans
     */
    public function index(Request $request)
    {
        $user = $request->user();
        $query = pengajuan::with(['user.profilKaryawan', 'penyetuju.profilKaryawan']);

        // Owner bisa lihat semua pengajuan di instansinya, karyawan hanya punya sendiri
        if ($user->role?->nama_role !== 'Owner') {
            $query->where('user_id', $user->id);
        }

        if ($request->has('status')) {
            $query->where('status', $request->status);
        }

        $pengajuans = $query->orderBy('created_at', 'desc')->get();
        return response()->json(['status' => 'success', 'data' => $pengajuans]);
    }

    /**
     * POST /api/pengajuans
     */
    public function store(Request $request)
    {
        $user = $request->user();

        $validator = Validator::make($request->all(), [
            'jenis' => 'required|in:cuti,izin,sakit',
            'tanggal_mulai' => 'required|date',
            'tanggal_selesai' => 'required|date|after_or_equal:tanggal_mulai',
            'keterangan' => 'nullable|string',
        ]);

        if ($validator->fails()) {
            return response()->json(['status' => 'error', 'message' => 'Validasi gagal', 'errors' => $validator->errors()], 422);
        }

        // Jika cuti, cek sisa cuti
        if ($request->jenis === 'cuti') {
            $karyawan = $user->profilKaryawan;
            if ($karyawan && $karyawan->sisa_cuti <= 0) {
                return response()->json(['status' => 'error', 'message' => 'Sisa cuti Anda habis'], 400);
            }
        }

        $pengajuan = pengajuan::create([
            'user_id' => $user->id,
            'jenis' => $request->jenis,
            'tanggal_mulai' => $request->tanggal_mulai,
            'tanggal_selesai' => $request->tanggal_selesai,
            'keterangan' => $request->keterangan,
            'status' => 'pending',
        ]);

        $pengajuan->load(['user.profilKaryawan']);

        return response()->json([
            'status' => 'success',
            'message' => 'Pengajuan berhasil dikirim',
            'data' => $pengajuan
        ], 201);
    }

    /**
     * GET /api/pengajuans/{pengajuan}
     */
    public function show(pengajuan $pengajuan)
    {
        $pengajuan->load(['user.profilKaryawan', 'penyetuju.profilKaryawan']);
        return response()->json(['status' => 'success', 'data' => $pengajuan]);
    }

    /**
     * PUT /api/pengajuans/{pengajuan}/approve
     * Setujui pengajuan (hanya Owner yang bisa)
     */
    public function approve(Request $request, pengajuan $pengajuan)
    {
        $user = $request->user();

        // Gunakan Policy untuk cek permission (manage:pengajuan)
        $this->authorize('approve', $pengajuan);

        // Prevent self-approval
        if ($pengajuan->user_id === $user->id) {
            return response()->json(['status' => 'error', 'message' => 'Anda tidak dapat menyetujui pengajuan sendiri'], 403);
        }

        // Tenant isolation: pengajuan must belong to same instansi
        if ($pengajuan->user->instansi_id !== $user->instansi_id) {
            return response()->json(['status' => 'error', 'message' => 'Forbidden'], 403);
        }

        if ($pengajuan->status !== 'pending') {
            return response()->json(['status' => 'error', 'message' => 'Pengajuan sudah diproses'], 400);
        }

        // Check remaining leave balance before approving cuti
        if ($pengajuan->jenis === 'cuti') {
            $karyawan = $pengajuan->user->profilKaryawan;
            if (!$karyawan || $karyawan->sisa_cuti <= 0) {
                return response()->json(['status' => 'error', 'message' => 'Sisa cuti karyawan habis, tidak dapat menyetujui'], 400);
            }
        }

        $pengajuan->update([
            'status' => 'disetujui',
            'disetujui_oleh' => $user->id,
            'tanggal_disetujui' => now(),
        ]);

        // Jika cuti, kurangi sisa cuti (hitung hari kerja, exclude weekend)
        if ($pengajuan->jenis === 'cuti') {
            $karyawan = $pengajuan->user->profilKaryawan;
            if ($karyawan) {
                // Ensure dates are Carbon instances
                $mulai = \Carbon\Carbon::parse($pengajuan->tanggal_mulai);
                $selesai = \Carbon\Carbon::parse($pengajuan->tanggal_selesai);

                $hariCuti = $mulai->diffInDaysFiltered(function ($date) {
                    return !$date->isSaturday() && !$date->isSunday();
                }, $selesai) + 1;

                // Cap decrement to prevent negative balance
                $hariCuti = min($hariCuti, $karyawan->sisa_cuti);
                $karyawan->decrement('sisa_cuti', $hariCuti);
            }
        }

        return response()->json([
            'status' => 'success',
            'message' => 'Pengajuan disetujui',
            'data' => $pengajuan
        ]);
    }

    /**
     * PUT /api/pengajuans/{pengajuan}/reject
     * Tolak pengajuan (hanya Owner yang bisa)
     */
    public function reject(Request $request, pengajuan $pengajuan)
    {
        $user = $request->user();

        // Gunakan Policy untuk cek permission (manage:pengajuan)
        $this->authorize('reject', $pengajuan);

        // Prevent self-rejection
        if ($pengajuan->user_id === $user->id) {
            return response()->json(['status' => 'error', 'message' => 'Anda tidak dapat menolak pengajuan sendiri'], 403);
        }

        // Tenant isolation
        if ($pengajuan->user->instansi_id !== $user->instansi_id) {
            return response()->json(['status' => 'error', 'message' => 'Forbidden'], 403);
        }

        $validator = Validator::make($request->all(), [
            'alasan_penolakan' => 'required|string',
        ]);

        if ($validator->fails()) {
            return response()->json(['status' => 'error', 'message' => 'Alasan penolakan wajib diisi'], 422);
        }

        if ($pengajuan->status !== 'pending') {
            return response()->json(['status' => 'error', 'message' => 'Pengajuan sudah diproses'], 400);
        }

        $pengajuan->update([
            'status' => 'ditolak',
            'disetujui_oleh' => $user->id,
            'tanggal_disetujui' => now(),
            'alasan_penolakan' => $request->alasan_penolakan,
        ]);

        return response()->json([
            'status' => 'success',
            'message' => 'Pengajuan ditolak',
            'data' => $pengajuan
        ]);
    }
}
