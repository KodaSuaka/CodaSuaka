<?php

namespace App\Http\Controllers;

use App\Http\Requests\StorePengajuanRequest;
use App\Http\Requests\RejectPengajuanRequest;
use App\Models\pengajuan;
use App\Traits\ApiResponse;
use Illuminate\Http\Request;

class PengajuanController extends Controller
{
    use ApiResponse;

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
        return $this->success($pengajuans);
    }

    /**
     * POST /api/pengajuans
     */
    public function store(StorePengajuanRequest $request)
    {
        $user = $request->user();

        // Jika cuti tahunan, cek sisa cuti
        if ($request->jenis === 'cuti_tahunan') {
            $karyawan = $user->profilKaryawan;
            if ($karyawan && $karyawan->sisa_cuti <= 0) {
                return $this->error('Sisa cuti Anda habis', 400);
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

        return $this->success($pengajuan, 'Pengajuan berhasil dikirim', 201);
    }

    /**
     * GET /api/pengajuans/{pengajuan}
     */
    public function show(pengajuan $pengajuan)
    {
        $pengajuan->load(['user.profilKaryawan', 'penyetuju.profilKaryawan']);
        return $this->success($pengajuan);
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
            return $this->error('Anda tidak dapat menyetujui pengajuan sendiri', 403);
        }

        // Tenant isolation: pengajuan must belong to same instansi
        if ($pengajuan->user->instansi_id !== $user->instansi_id) {
            return $this->error('Forbidden', 403);
        }

        if ($pengajuan->status !== 'pending') {
            return $this->error('Pengajuan sudah diproses', 400);
        }

        // Check remaining leave balance before approving cuti
        if ($pengajuan->jenis === 'cuti_tahunan') {
            $karyawan = $pengajuan->user->profilKaryawan;
            if (!$karyawan || $karyawan->sisa_cuti <= 0) {
                return $this->error('Sisa cuti karyawan habis, tidak dapat menyetujui', 400);
            }
        }

        $pengajuan->update([
            'status' => 'disetujui',
            'disetujui_oleh' => $user->id,
            'tanggal_disetujui' => now(),
        ]);

        // Jika cuti, kurangi sisa cuti (hitung hari kerja, exclude weekend)
        if ($pengajuan->jenis === 'cuti_tahunan') {
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

        return $this->success($pengajuan, 'Pengajuan disetujui');
    }

    /**
     * PUT /api/pengajuans/{pengajuan}/reject
     * Tolak pengajuan (hanya Owner yang bisa)
     */
    public function reject(RejectPengajuanRequest $request, pengajuan $pengajuan)
    {
        $user = $request->user();

        // Gunakan Policy untuk cek permission (manage:pengajuan)
        $this->authorize('reject', $pengajuan);

        // Prevent self-rejection
        if ($pengajuan->user_id === $user->id) {
            return $this->error('Anda tidak dapat menolak pengajuan sendiri', 403);
        }

        // Tenant isolation
        if ($pengajuan->user->instansi_id !== $user->instansi_id) {
            return $this->error('Forbidden', 403);
        }

        if ($pengajuan->status !== 'pending') {
            return $this->error('Pengajuan sudah diproses', 400);
        }

        $pengajuan->update([
            'status' => 'ditolak',
            'disetujui_oleh' => $user->id,
            'tanggal_disetujui' => now(),
            'alasan_penolakan' => $request->alasan_penolakan,
        ]);

        return $this->success($pengajuan, 'Pengajuan ditolak');
    }
}
