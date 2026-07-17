<?php

namespace App\Http\Controllers;

use App\Http\Requests\StoreattandenceRequest;
use App\Models\attandence;
use App\Models\User;
use App\Traits\ApiResponse;
use Illuminate\Http\Request;

class AttandenceController extends Controller
{
    use ApiResponse;

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

        // Owner/Manajemen (view:presensi) bisa lihat semua, karyawan hanya lihat sendiri
        $canViewAll = app(\App\Services\PermissionService::class)->userHasPermission($user, 'view:presensi');
        if (!$canViewAll) {
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
        return $this->success($presensis);
    }

    /**
     * POST /api/presensis/checkin
     * Absen masuk
     */
    public function checkin(StoreattandenceRequest $request)
    {
        $user = $request->user();
        $today = now()->toDateString();

        // Cek apakah sudah checkin hari ini
        $existing = attandence::where('user_id', $user->id)
            ->where('tanggal', $today)
            ->first();

        if ($existing && $existing->jam_checkin) {
            return $this->error('Anda sudah melakukan checkin hari ini', 409);
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

        return $this->success($existing, 'Checkin berhasil');
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
            return $this->error('Anda belum melakukan checkin hari ini', 400);
        }

        if ($presensi->jam_checkout) {
            return $this->error('Anda sudah melakukan checkout', 409);
        }

        $presensi->update(['jam_checkout' => now()->toTimeString()]);

        return $this->success($presensi, 'Checkout berhasil');
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

        return $this->success([
            'sudah_checkin' => $presensi && $presensi->jam_checkin ? true : false,
            'sudah_checkout' => $presensi && $presensi->jam_checkout ? true : false,
            'presensi' => $presensi,
        ]);
    }

    /**
     * GET /api/rekap-kehadiran
     * Rekap kehadiran per karyawan dalam satu bulan
     */
    public function rekap(Request $request)
    {
        $user = $request->user();

        // Gunakan PermissionService — Manajemen punya 'view:presensi' yang mencakup akses rekap
        if (!app(\App\Services\PermissionService::class)->userHasPermission($user, 'view:presensi')) {
            return $this->error('Anda tidak memiliki akses ke rekap kehadiran', 403);
        }

        $bulan = $request->get('bulan', now()->month);
        $tahun = $request->get('tahun', now()->year);

        // Karyawan dalam instansi — eager load users to avoid N+1
        $userIds = User::where('instansi_id', $user->instansi_id)->pluck('id');

        // Load all users with their karyawan profile in one query
        $users = User::whereIn('id', $userIds)
            ->with('profilKaryawan')
            ->get()
            ->keyBy('id');

        $rekap = attandence::whereIn('user_id', $userIds)
            ->whereMonth('tanggal', $bulan)
            ->whereYear('tanggal', $tahun)
            ->get()
            ->groupBy('user_id')
            ->map(function ($items, $userId) use ($users) {
                $user = $users->get($userId);
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

        return $this->success($rekap);
    }
}
