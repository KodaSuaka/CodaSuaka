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
use Illuminate\Support\Facades\Gate;

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

        // Owner dashboard — role dengan akses penuh data bisnis
        // Gunakan Gate 'owner' yang sudah terdefinisi di AppServiceProvider
        if (!Gate::allows('owner')) {
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

        // 4. Tugas stats — single query dengan CASE + subquery (exclude template)
        $tugasStatsQuery = penugasan::where('is_template', false)
        ->whereIn('created_by', function ($q) use ($instansiId) {
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

        // Tugas milik karyawan ini — select kolom yang dibutuhkan
        $tugas = [];
        if ($karyawan) {
            $tugas = penugasan::where('is_template', false)
                ->where('penanggung_jawab_id', $karyawan->id)
                ->where('status', '!=', 'selesai')
                ->select(['id', 'judul', 'tenggat', 'status', 'urgency'])
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

        // Total pemasukan = omset (selaras dengan buku kas — semua tipe masuk)
        $totalOmset = \App\Models\TransaksiKas::where('instansi_id', $user->instansi_id)
            ->where('tipe', 'masuk')
            ->whereDate('tanggal', '>=', $startDate)
            ->whereDate('tanggal', '<=', $endDate)
            ->sum('nominal');

        return $this->success([
            'start_date' => $startDate,
            'end_date' => $endDate,
            'total_omset' => (float) $totalOmset,
            'message' => null,
        ]);
    }

    /**
     * GET /api/karyawan/poin-kinerja
     * Total poin kinerja karyawan dari tugas yang sudah selesai.
     * Poin dihitung berdasarkan urgency: urgent=30, sedang=20, rendah=10.
     */
    public function poinKinerja(Request $request)
    {
        $user = $request->user();
        $karyawan = $user->profilKaryawan;

        if (!$karyawan) {
            return $this->success([
                'total_poin' => 0,
                'total_tugas_selesai' => 0,
                'rata_rata_poin' => 0.0,
                'detail_urgency' => [],
            ]);
        }

        // Ambil semua tugas selesai untuk karyawan ini
        // Hanya select kolom yang dibutuhkan untuk performa optimal
        $tugasSelesai = \App\Models\penugasan::where('is_template', false)
            ->where('penanggung_jawab_id', $karyawan->id)
            ->where('status', 'selesai')
            ->select(['urgency', 'poin'])
            ->get();

        $totalPoin = $tugasSelesai->sum('poin');
        $totalSelesai = $tugasSelesai->count();
        $rataRata = $totalSelesai > 0 ? round($totalPoin / $totalSelesai, 1) : 0.0;

        // Detail per urgency — kembalikan sebagai array of objects
        // sesuai format yang diharapkan frontend: List<DetailUrgency>
        // field: urgency, jumlah, total_poin
        $detailByUrgency = $tugasSelesai->groupBy('urgency');

        $detailUrgency = collect([
            'urgent' => 'urgent',
            'sedang' => 'sedang',
            'rendah' => 'rendah',
        ])->map(function ($label) use ($detailByUrgency) {
            $items = $detailByUrgency[$label] ?? collect();
            return [
                'urgency' => $label,
                'jumlah' => $items->count(),
                'total_poin' => (int) $items->sum('poin'),
            ];
        })->filter(fn ($item) => $item['jumlah'] > 0)
          ->values()
          ->all();

        return $this->success([
            'total_poin' => (int) $totalPoin,
            'total_tugas_selesai' => (int) $totalSelesai,
            'rata_rata_poin' => (float) $rataRata,
            'detail_urgency' => $detailUrgency,
        ]);
    }
}
