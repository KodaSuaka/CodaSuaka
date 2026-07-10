<?php

namespace App\Http\Controllers;

use App\Models\attandence;
use App\Models\Divisi;
use App\Models\karyawan;
use App\Models\outlet;
use App\Models\pengajuan;
use App\Models\penugasan;
use App\Models\User;
use App\Services\PermissionService;
use App\Traits\ApiResponse;
use Carbon\Carbon;
use Illuminate\Http\Request;

class DashboardController extends Controller
{
    use ApiResponse;

    public function __construct()
    {
        // Middleware closure — berjalan setelah auth:sanctum
        $this->middleware(function (Request $request, $next) {
            // Pastikan user terautentikasi
            if (!$request->user()) {
                return $this->error('Unauthenticated', 401);
            }
            return $next($request);
        });
    }

    /**
     * GET /api/dashboard
     * Dashboard untuk Owner (ringkasan data instansi)
     */
    public function index(Request $request)
    {
        $user = $request->user();
        $instansiId = $user->instansi_id;

        // Hanya Owner yang bisa akses dashboard utama
        if ($user->role?->nama_role !== 'Owner') {
            return $this->error('Forbidden: Hanya Owner yang dapat mengakses dashboard ini', 403);
        }

        $today = now()->toDateString();

        // ─── Approach: 4 queries instead of 8 ───
        // Gunakan whereIn subquery (closure) untuk hindari pluck() array besar

        // 1. Outlet + Divisi (filter langsung via instansi_id)
        $totalOutlet = outlet::where('instansi_id', $instansiId)->count();
        $totalDivisi = Divisi::whereHas('outlet', fn($q) => $q->where('instansi_id', $instansiId))->count();

        // 2. Karyawan non-Owner + Presensi hari ini (query paralel dalam satu panggilan)
        $karyawanCount = karyawan::whereHas('user', function ($q) use ($instansiId) {
            $q->where('instansi_id', $instansiId)
              ->whereHas('role', fn($r) => $r->where('nama_role', '!=', 'Owner'));
        })->count();

        // 3. Presensi + Pengajuan pending (pakai subquery dari users)
        $userIdsSub = User::where('instansi_id', $instansiId)->select('id');
        $presensiHariIni = attandence::whereIn('user_id', $userIdsSub)
            ->where('tanggal', $today)
            ->count();
        $pengajuanPending = pengajuan::whereIn('user_id', $userIdsSub)
            ->where('status', 'pending')
            ->count();

        // 4. Tugas stats — single query dengan CASE + subquery
        $tugasStatsQuery = penugasan::whereIn('created_by', function ($q) use ($instansiId) {
            $q->select('id')->from('users')->where('instansi_id', $instansiId);
        })
        ->selectRaw("
            SUM(CASE WHEN status = 'belum' THEN 1 ELSE 0 END) as belum,
            SUM(CASE WHEN status = 'proses' THEN 1 ELSE 0 END) as proses,
            SUM(CASE WHEN status = 'selesai' THEN 1 ELSE 0 END) as selesai
        ")
        ->first();

        return $this->success([
            'total_karyawan' => $karyawanCount,
            'total_outlet' => $totalOutlet,
            'total_divisi' => $totalDivisi,
            'presensi_hari_ini' => $presensiHariIni,
            'pengajuan_pending' => $pengajuanPending,
            'tugas_stats' => [
                'belum' => (int) ($tugasStatsQuery->belum ?? 0),
                'proses' => (int) ($tugasStatsQuery->proses ?? 0),
                'selesai' => (int) ($tugasStatsQuery->selesai ?? 0),
            ],
        ]);
    }

    /**
     * GET /api/karyawan/dashboard
     * Dashboard untuk karyawan biasa
     */
    public function karyawanDashboard(Request $request)
    {
        $user = $request->user();
        $karyawan = $user->profilKaryawan;

        // Status presensi hari ini
        $today = now()->toDateString();
        $presensi = attandence::where('user_id', $user->id)
            ->where('tanggal', $today)
            ->first();

        // Tugas milik karyawan ini
        $tugas = [];
        if ($karyawan) {
            $tugas = penugasan::where('penanggung_jawab_id', $karyawan->id)
                ->where('status', '!=', 'selesai')
                ->orderBy('tenggat', 'asc')
                ->limit(5)
                ->get();
        }

        // Pengajuan pending milik user
        $pengajuanPendingCount = pengajuan::where('user_id', $user->id)
            ->where('status', 'pending')
            ->count();

        // Role-based dashboard menu from PermissionService
        $permissionService = app(PermissionService::class);
        $roleMenuItems = $permissionService->getKaryawanDashboardMenu($user);
        $additionalContent = $permissionService->getKaryawanAdditionalContent($user);

        return $this->success([
            'karyawan' => $karyawan,
            'presensi_hari_ini' => $presensi,
            'sudah_checkin' => $presensi && $presensi->jam_checkin ? true : false,
            'sudah_checkout' => $presensi && $presensi->jam_checkout ? true : false,
            'tugas_aktif' => $tugas,
            'pengajuan_pending_count' => $pengajuanPendingCount,
            'sisa_cuti' => $karyawan ? (int) $karyawan->sisa_cuti : 0,
            'role_menu_items' => $roleMenuItems,
            'additional_content' => $additionalContent,
        ]);
    }

    /**
     * GET /api/dashboard/omset
     * Omset — dihitung dari total transaksi kas masuk (operasional).
     */
    public function omset(Request $request)
    {
        $user = $request->user();
        $startDate = $request->get('start_date', now()->startOfMonth()->toDateString());
        $endDate = $request->get('end_date', now()->toDateString());

        // Total pemasukan operasional = omset
        $totalOmset = \App\Models\TransaksiKas::where('instansi_id', $user->instansi_id)
            ->where('tipe', 'masuk')
            ->whereDate('tanggal', '>=', $startDate)
            ->whereDate('tanggal', '<=', $endDate)
            ->whereHas('kategoriTransaksi', function ($q) {
                $q->where('sifat', 'operasional');
            })
            ->sum('nominal');

        return $this->success([
            'start_date' => $startDate,
            'end_date' => $endDate,
            'total_omset' => (float) $totalOmset,
            'message' => null,
        ]);
    }
}
