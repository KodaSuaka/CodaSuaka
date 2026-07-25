<?php

namespace App\Http\Controllers;

use App\Http\Requests\StoreattandenceRequest;
use App\Models\attandence;
use App\Models\User;
use App\Traits\ApiResponse;
use Carbon\Carbon;
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

        // Select kolom yang dibutuhkan + eager loading relasi
        $query = attandence::with([
            'user' => fn($q) => $q->select(['id', 'name', 'instansi_id']),
        ])->select([
            'id', 'user_id', 'tanggal', 'jam_checkin', 'jam_checkout',
            'status', 'keterangan', 'lokasi_checkin',
        ]);

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
        $jamSekarang = now();

        // Standar waktu checkin: 07:30 WIB
        $jamCheckinStandard = Carbon::today()->setTime(7, 30, 0);

        // Tentukan status_keterangan berdasarkan waktu checkin
        $statusKeterangan = $this->tentukanStatusCheckin($jamSekarang, $jamCheckinStandard);

        try {
            // Gunakan transaksi DB + lockForUpdate agar atomic (cegah race condition)
            return \Illuminate\Support\Facades\DB::transaction(function () use ($user, $today, $request, $jamSekarang, $statusKeterangan) {
                $existing = attandence::where('user_id', $user->id)
                    ->where('tanggal', $today)
                    ->lockForUpdate()
                    ->first();

                if ($existing && $existing->jam_checkin) {
                    return $this->error('Anda sudah melakukan checkin hari ini', 409);
                }

                $dataCheckin = [
                    'user_id' => $user->id,
                    'tanggal' => $today,
                    'jam_checkin' => $jamSekarang->toTimeString(),
                    'status' => 'hadir',
                    'lokasi_checkin' => $request->lokasi ?? null,
                    'status_keterangan' => $statusKeterangan,
                ];

                if (!$existing) {
                    $existing = attandence::create($dataCheckin);
                } else {
                    $existing->update([
                        'jam_checkin' => $jamSekarang->toTimeString(),
                        'status' => 'hadir',
                        'lokasi_checkin' => $request->lokasi ?? $existing->lokasi_checkin,
                        'status_keterangan' => $statusKeterangan,
                    ]);
                }

                return $this->success($existing, 'Checkin berhasil — ' . $this->labelStatusKeterangan($statusKeterangan));
            });
        } catch (\Illuminate\Database\QueryException $e) {
            // Tangkap race condition jika unique constraint violated
            return $this->error('Anda sudah melakukan checkin hari ini', 409);
        }
    }

    /**
     * POST /api/presensis/checkout
     * Absen pulang
     */
    public function checkout(Request $request)
    {
        $user = $request->user();
        $today = now()->toDateString();
        $jamSekarang = now();

        // Standar waktu checkout: 16:30 WIB
        $jamCheckoutStandard = Carbon::today()->setTime(16, 30, 0);

        // Tentukan status_keterangan berdasarkan waktu checkout
        $statusKeterangan = $this->tentukanStatusCheckout($jamSekarang, $jamCheckoutStandard);

        try {
            return \Illuminate\Support\Facades\DB::transaction(function () use ($user, $today, $jamSekarang, $statusKeterangan) {
                $presensi = attandence::where('user_id', $user->id)
                    ->where('tanggal', $today)
                    ->lockForUpdate()
                    ->first();

                if (!$presensi || !$presensi->jam_checkin) {
                    return $this->error('Anda belum melakukan checkin hari ini', 400);
                }

                if ($presensi->jam_checkout) {
                    return $this->error('Anda sudah melakukan checkout', 409);
                }

                $presensi->update([
                    'jam_checkout' => $jamSekarang->toTimeString(),
                    'status_keterangan' => $statusKeterangan,
                ]);

                return $this->success($presensi, 'Checkout berhasil — ' . $this->labelStatusKeterangan($statusKeterangan));
            });
        } catch (\Illuminate\Database\QueryException $e) {
            return $this->error('Terjadi kesalahan, silakan coba lagi', 500);
        }
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

    // ─── Helper: Deteksi Status Waktu Checkin ──────────────────────

    /**
     * Menentukan status_keterangan checkin berdasarkan jam berjalan vs jam standar.
     *
     * @param Carbon $jamSekarang   Waktu checkin aktual
     * @param Carbon $jamStandar    Waktu standar (07:30)
     * @return string               tepat_waktu | checkin_awal | checkin_terlambat
     */
    private function tentukanStatusCheckin(Carbon $jamSekarang, Carbon $jamStandar): string
    {
        if ($jamSekarang->lt($jamStandar)) {
            return 'checkin_awal';
        } elseif ($jamSekarang->gt($jamStandar)) {
            return 'checkin_terlambat';
        }
        return 'tepat_waktu';
    }

    // ─── Helper: Deteksi Status Waktu Checkout ─────────────────────

    /**
     * Menentukan status_keterangan checkout berdasarkan jam berjalan vs jam standar.
     *
     * @param Carbon $jamSekarang   Waktu checkout aktual
     * @param Carbon $jamStandar    Waktu standar (16:30)
     * @return string               tepat_waktu | checkout_awal | checkout_terlambat
     */
    private function tentukanStatusCheckout(Carbon $jamSekarang, Carbon $jamStandar): string
    {
        if ($jamSekarang->lt($jamStandar)) {
            return 'checkout_awal';
        } elseif ($jamSekarang->gt($jamStandar)) {
            return 'checkout_terlambat';
        }
        return 'tepat_waktu';
    }

    // ─── Helper: Label Status Keterangan ───────────────────────────

    /**
     * Mengubah status_keterangan menjadi label yang mudah dibaca.
     */
    private function labelStatusKeterangan(string $status): string
    {
        return match ($status) {
            'tepat_waktu' => 'Tepat Waktu',
            'checkin_awal' => 'Checkin Awal',
            'checkin_terlambat' => 'Checkin Terlambat',
            'checkout_awal' => 'Checkout Awal',
            'checkout_terlambat' => 'Checkout Terlambat',
            default => $status,
        };
    }
}
