<?php

namespace App\Http\Controllers;

use App\Models\attandence;
use App\Models\User;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

class AttandenceController extends Controller
{
    public function __construct()
    {
        $this->authorizeResource(attandence::class, 'attandence');
    }

    /**
     * GET /api/presensis
     * Riwayat presensi, bisa filter by user_id, tanggal, bulan
     */
    public function index(Request $request)
    {
        $user = $request->user();
        $query = attandence::with('user.profilKaryawan');

        // Owner/Admin bisa lihat semua, karyawan hanya lihat sendiri
        if ($user->role?->nama_role !== 'Owner') {
            $query->where('user_id', $user->id);
        } elseif ($request->has('user_id')) {
            $query->where('user_id', $request->user_id);
        }

        if ($request->has('tanggal')) {
            $query->where('tanggal', $request->tanggal);
        }

        if ($request->has('bulan') && $request->has('tahun')) {
            $query->whereMonth('tanggal', $request->bulan)
                  ->whereYear('tanggal', $request->tahun);
        }

        $presensis = $query->orderBy('tanggal', 'desc')->get();
        return response()->json(['status' => 'success', 'data' => $presensis]);
    }

    /**
     * POST /api/presensis/checkin
     * Absen masuk
     */
    public function checkin(Request $request)
    {
        $user = $request->user();
        $today = now()->toDateString();

        // Cek apakah sudah checkin hari ini
        $existing = attandence::where('user_id', $user->id)
            ->where('tanggal', $today)
            ->first();

        if ($existing && $existing->jam_checkin) {
            return response()->json(['status' => 'error', 'message' => 'Anda sudah melakukan checkin hari ini'], 409);
        }

        if (!$existing) {
            $existing = attandence::create([
                'user_id' => $user->id,
                'tanggal' => $today,
                'jam_checkin' => now()->toTimeString(),
                'status' => 'hadir',
                'lokasi_checkin' => $request->lokasi ?? null,
            ]);
        } else {
            $existing->update([
                'jam_checkin' => now()->toTimeString(),
                'status' => 'hadir',
                'lokasi_checkin' => $request->lokasi ?? $existing->lokasi_checkin,
            ]);
        }

        return response()->json([
            'status' => 'success',
            'message' => 'Checkin berhasil',
            'data' => $existing
        ]);
    }

    /**
     * POST /api/presensis/checkout
     * Absen pulang
     */
    public function checkout(Request $request)
    {
        $user = $request->user();
        $today = now()->toDateString();

        $presensi = attandence::where('user_id', $user->id)
            ->where('tanggal', $today)
            ->first();

        if (!$presensi || !$presensi->jam_checkin) {
            return response()->json(['status' => 'error', 'message' => 'Anda belum melakukan checkin hari ini'], 400);
        }

        if ($presensi->jam_checkout) {
            return response()->json(['status' => 'error', 'message' => 'Anda sudah melakukan checkout'], 409);
        }

        $presensi->update(['jam_checkout' => now()->toTimeString()]);

        return response()->json([
            'status' => 'success',
            'message' => 'Checkout berhasil',
            'data' => $presensi
        ]);
    }

    /**
     * GET /api/presensis/today
     * Status presensi hari ini
     */
    public function today(Request $request)
    {
        $user = $request->user();
        $today = now()->toDateString();

        $presensi = attandence::where('user_id', $user->id)
            ->where('tanggal', $today)
            ->first();

        return response()->json([
            'status' => 'success',
            'data' => [
                'sudah_checkin' => $presensi && $presensi->jam_checkin ? true : false,
                'sudah_checkout' => $presensi && $presensi->jam_checkout ? true : false,
                'presensi' => $presensi,
            ]
        ]);
    }

    /**
     * GET /api/rekap-kehadiran
     * Rekap kehadiran per karyawan dalam satu bulan
     */
    public function rekap(Request $request)
    {
        $user = $request->user();

        // Hanya Owner yang bisa melihat rekap kehadiran
        if ($user->role?->nama_role !== 'Owner') {
            return response()->json(['status' => 'error', 'message' => 'Anda tidak memiliki akses ke rekap kehadiran'], 403);
        }

        $bulan = $request->get('bulan', now()->month);
        $tahun = $request->get('tahun', now()->year);

        // Karyawan dalam instansi
        $userIds = User::where('instansi_id', $user->instansi_id)->pluck('id');

        $rekap = attandence::whereIn('user_id', $userIds)
            ->whereMonth('tanggal', $bulan)
            ->whereYear('tanggal', $tahun)
            ->get()
            ->groupBy('user_id')
            ->map(function ($items, $userId) {
                $user = User::find($userId);
                return [
                    'user_id' => $userId,
                    'nama_lengkap' => $user?->profilKaryawan?->nama_lengkap ?? $user?->name,
                    'total_hadir' => $items->where('status', 'hadir')->count(),
                    'total_izin' => $items->where('status', 'izin')->count(),
                    'total_sakit' => $items->where('status', 'sakit')->count(),
                    'total_alpha' => $items->where('status', 'alpha')->count(),
                    'total_cuti' => $items->where('status', 'cuti')->count(),
                ];
            })->values();

        return response()->json([
            'status' => 'success',
            'data' => $rekap
        ]);
    }
}
