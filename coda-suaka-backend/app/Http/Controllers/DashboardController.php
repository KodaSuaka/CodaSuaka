<?php

namespace App\Http\Controllers;

use App\Models\attandence;
use App\Models\Divisi;
use App\Models\karyawan;
use App\Models\outlet;
use App\Models\pengajuan;
use App\Models\penugasan;
use App\Models\User;
use Carbon\Carbon;
use Illuminate\Http\Request;

class DashboardController extends Controller
{
    public function __construct()
    {
    }

    /**
     * GET /api/dashboard
     * Dashboard untuk Owner (ringkasan data instansi)
     */
    public function index(Request $request)
    {
        $user = $request->user();
        $instansiId = $user->instansi_id;
        $userIds = User::where('instansi_id', $instansiId)->pluck('id');

        $totalKaryawan = karyawan::whereIn('user_id', $userIds)
            ->whereHas('user', fn($q) => $q->whereHas('role', fn($r) => $r->where('nama_role', 'Karyawan')))
            ->count();
        $totalOutlet = outlet::where('instansi_id', $instansiId)->count();
        $totalDivisi = Divisi::whereHas('outlet', fn($q) => $q->where('instansi_id', $instansiId))->count();

        // Presensi hari ini
        $today = now()->toDateString();
        $presensiHariIni = attandence::whereIn('user_id', $userIds)
            ->where('tanggal', $today)
            ->count();

        // Pengajuan pending
        $pengajuanPending = pengajuan::whereIn('user_id', $userIds)
            ->where('status', 'pending')
            ->count();

        // Tugas dengan status
        $tugasStats = [
            'belum' => penugasan::whereIn('created_by', $userIds)->where('status', 'belum')->count(),
            'proses' => penugasan::whereIn('created_by', $userIds)->where('status', 'proses')->count(),
            'selesai' => penugasan::whereIn('created_by', $userIds)->where('status', 'selesai')->count(),
        ];

        return response()->json([
            'status' => 'success',
            'data' => [
                'total_karyawan' => $totalKaryawan,
                'total_outlet' => $totalOutlet,
                'total_divisi' => $totalDivisi,
                'presensi_hari_ini' => $presensiHariIni,
                'pengajuan_pending' => $pengajuanPending,
                'tugas_stats' => $tugasStats,
            ]
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

        return response()->json([
            'status' => 'success',
            'data' => [
                'karyawan' => $karyawan,
                'presensi_hari_ini' => $presensi,
                'sudah_checkin' => $presensi && $presensi->jam_checkin ? true : false,
                'sudah_checkout' => $presensi && $presensi->jam_checkout ? true : false,
                'tugas_aktif' => $tugas,
                'pengajuan_pending_count' => $pengajuanPendingCount,
                'sisa_cuti' => $karyawan ? (int) $karyawan->sisa_cuti : 0,
            ]
        ]);
    }

    /**
     * GET /api/dashboard/omset
     * Omset (untuk kepentingan dashboard - placeholder)
     */
    public function omset(Request $request)
    {
        $startDate = $request->get('start_date', now()->startOfMonth()->toDateString());
        $endDate = $request->get('end_date', now()->toDateString());

        // Placeholder - nanti bisa diintegrasikan dengan modul transaksi
        return response()->json([
            'status' => 'success',
            'data' => [
                'start_date' => $startDate,
                'end_date' => $endDate,
                'total_omset' => 0,
                'message' => 'Fitur omset akan diintegrasikan dengan modul transaksi'
            ]
        ]);
    }
}
