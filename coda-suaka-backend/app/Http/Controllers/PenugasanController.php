<?php

namespace App\Http\Controllers;

use App\Http\Requests\StorepenugasanRequest;
use App\Http\Requests\UpdatepenugasanRequest;
use App\Models\penugasan;
use App\Models\User;
use App\Services\NotificationService;
use App\Traits\ApiResponse;
use Illuminate\Http\Request;

class PenugasanController extends Controller
{
    use ApiResponse;

    public function __construct(
        private NotificationService $notificationService,
    ) {
        $this->authorizeResource(penugasan::class, 'penugasan');
    }

    /**
     * GET /api/penugasans?divisi_id=xxx&status=xxx&penanggung_jawab_id=xxx&is_template=xxx
     *
     * Semua role (Owner, Manager, Keuangan, Staff) bisa melihat daftar tugas.
     * Template penugasan selalu terlihat oleh semua user di instansi yang sama.
     * Tugas biasa: Owner/Admin lihat semua di instansi, karyawan lain lihat tugas sendiri.
     */
    public function index(Request $request)
    {
        $user = $request->user();

        // Select kolom yang dibutuhkan + eager loading relasi
        $query = penugasan::with([
            'penanggungJawab.user' => fn ($q) => $q->select(['id', 'name', 'role_id']),
            'divisi' => fn ($q) => $q->select(['id', 'nama_divisi']),
            'pembuat' => fn ($q) => $q->select(['id', 'name']),
        ])->select([
            'id', 'judul', 'deskripsi', 'penanggung_jawab_id',
            'divisi_id', 'tenggat', 'status', 'urgency', 'poin',
            'is_template', 'instansi_id', 'created_by', 'created_at', 'updated_at',
        ]);

        // Filter berdasarkan parameter is_template (opsional)
        // Default: tampilkan SEMUA (template + tugas biasa)
        // is_template=true: hanya template
        // is_template=false: hanya tugas biasa
        // NOTE: Jangan pakai $request->boolean() langsung karena filter_var(null)
        // mengembalikan false (bukan null), sehingga regularTasks() akan dipanggil
        // saat parameter tidak dikirim. Gunakan $request->has() dulu untuk mengecek.
        if ($request->has('is_template')) {
            if ($request->boolean('is_template')) {
                $query->templates();
            } else {
                $query->regularTasks();
            }
        }
        // else: tidak ada parameter is_template → tampilkan semua (template + regular)

        // Filter berdasarkan role
        // Owner/Super Admin: lihat semua tugas di instansi (termasuk template + tugas semua karyawan)
        // Manager/Keuangan/Staff: lihat template (tanpa filter) + tugas biasa yang ditugaskan kepada mereka
        $roleName = $user->role?->nama_role;
        $isOwnerOrAdmin = in_array($roleName, ['Owner', 'Super Admin']);

        if (! $isOwnerOrAdmin) {
            $karyawan = $user->profilKaryawan;
            if ($karyawan) {
                // Non-Owner: lihat semua template + tugas biasa yang ditugaskan ke mereka
                $query->where(function ($q) use ($karyawan) {
                    $q->where('is_template', true)
                        ->orWhere(function ($tq) use ($karyawan) {
                            $tq->where('is_template', false)
                                ->where('penanggung_jawab_id', $karyawan->id);
                        });
                });
            } else {
                // Jika user tidak punya profil karyawan, hanya lihat template
                $query->where('is_template', true);
            }
        }

        if ($request->has('divisi_id')) {
            $query->where('divisi_id', $request->divisi_id);
        }

        if ($request->has('status')) {
            $query->whereIn('status', explode(',', $request->status));
        }

        if ($request->has('penanggung_jawab_id')) {
            $query->where('penanggung_jawab_id', $request->penanggung_jawab_id);
        }

        $penugasans = $query->orderBy('created_at', 'desc')->get();

        return $this->success($penugasans);
    }

    /**
     * POST /api/penugasans
     */
    public function store(StorepenugasanRequest $request)
    {
        $urgency = $request->urgency ?? 'sedang';

        $penugasan = penugasan::create([
            'judul' => $request->judul,
            'deskripsi' => $request->deskripsi,
            'penanggung_jawab_id' => $request->penanggung_jawab_id,
            'divisi_id' => $request->divisi_id,
            'tenggat' => $request->tenggat,
            'status' => $request->status ?? 'belum',
            'urgency' => $urgency,
            'poin' => penugasan::getPoinForUrgency($urgency),
            'is_template' => false,
            'instansi_id' => $request->user()->instansi_id,
            'created_by' => $request->user()->id,
        ]);

        $penugasan->load(['penanggungJawab.user', 'divisi', 'pembuat']);

        // Kirim notifikasi ke karyawan yang ditugasi.
        // penanggung_jawab_id adalah karyawans.id (UUID) — NotificationService
        // butuh users.id (int) untuk kolom notifications.user_id, jadi pakai
        // user_id dari relasi penanggungJawab, bukan id karyawan itu sendiri.
        $penanggungJawabUserId = $penugasan->penanggungJawab?->user_id;
        if ($penanggungJawabUserId) {
            $this->notificationService->onPenugasanBaru(
                $penugasan->id,
                $penanggungJawabUserId,
                $penugasan->judul
            );
        }

        return $this->success($penugasan, 'Tugas berhasil ditambahkan', 201);
    }

    /**
     * GET /api/penugasans/{penugasan}
     */
    public function show(penugasan $penugasan)
    {
        $penugasan->load(['penanggungJawab.user', 'divisi', 'pembuat']);

        return $this->success($penugasan);
    }

    /**
     * PUT /api/penugasans/{penugasan}
     */
    public function update(UpdatepenugasanRequest $request, penugasan $penugasan)
    {
        $data = $request->only(['judul', 'deskripsi', 'penanggung_jawab_id', 'divisi_id', 'tenggat', 'status', 'urgency']);

        // Jika status berubah menjadi 'selesai', hitung poin berdasarkan urgency
        if (isset($data['status']) && $data['status'] === 'selesai' && $penugasan->status !== 'selesai') {
            $urgency = $data['urgency'] ?? $penugasan->urgency;
            $data['poin'] = penugasan::getPoinForUrgency($urgency);
            $data['completed_at'] = now();

            // Kirim notifikasi ke owner/pembuat tugas saat karyawan selesai
            $this->sendPenugasanSelesaiNotification($penugasan, $request->user());
        }

        // Jika urgency berubah dan tugas belum selesai, update poin
        if (isset($data['urgency']) && $penugasan->status !== 'selesai') {
            $data['poin'] = penugasan::getPoinForUrgency($data['urgency']);
        }

        $data['status_changed_by'] = $request->user()->id;
        $penugasan->update($data);
        $penugasan->load(['penanggungJawab.user', 'divisi', 'pembuat']);

        return $this->success($penugasan, 'Tugas berhasil diperbarui');
    }

    /**
     * PUT /api/penugasans/{penugasan}/accept
     * Karyawan menerima/mulai mengerjakan tugas (belum → proses)
     *
     * Jika tugas adalah template (is_template=true), maka akan dibuat
     * task BARU dari template agar template tetap tersedia untuk karyawan lain.
     */
    public function accept(Request $request, penugasan $penugasan)
    {
        $this->authorize('accept', $penugasan);

        if ($penugasan->status !== 'belum') {
            return $this->error('Tugas hanya bisa diterima jika status masih "belum"', 422);
        }

        // Jika template: buat task baru dari template
        if ($penugasan->is_template) {
            $karyawan = $request->user()->profilKaryawan;
            $newTask = penugasan::create([
                'judul' => $penugasan->judul,
                'deskripsi' => $penugasan->deskripsi,
                'divisi_id' => $penugasan->divisi_id,
                'tenggat' => $penugasan->tenggat,
                'urgency' => $penugasan->urgency,
                'poin' => $penugasan->poin,
                'status' => 'proses',
                'penanggung_jawab_id' => $karyawan?->id,
                'created_by' => $request->user()->id,
                'is_template' => false,
                'instansi_id' => $request->user()->instansi_id,
                'template_penugasan_id' => $penugasan->id,
                'accepted_at' => now(),
                'status_changed_by' => $request->user()->id,
            ]);

            $this->sendPenugasanDikerjakanNotification($newTask, $request->user());

            $newTask->load(['penanggungJawab.user', 'divisi', 'pembuat']);

            return $this->success($newTask, 'Tugas berhasil diterima');
        }

        // Tugas biasa: langsung update
        $penugasan->update([
            'status' => 'proses',
            'accepted_at' => now(),
            'status_changed_by' => $request->user()->id,
        ]);

        // Kirim notifikasi ke owner/pembuat tugas
        $this->sendPenugasanDikerjakanNotification($penugasan, $request->user());

        $penugasan->load(['penanggungJawab.user', 'divisi', 'pembuat']);

        return $this->success($penugasan, 'Tugas berhasil diterima');
    }

    /**
     * PUT /api/penugasans/{penugasan}/complete
     * Karyawan menandai tugas selesai (proses → menunggu_validasi).
     * Poin baru diberikan setelah pemilik/manager memvalidasi (lihat validasi()).
     */
    public function complete(Request $request, penugasan $penugasan)
    {
        $this->authorize('complete', $penugasan);

        if ($penugasan->status !== 'proses') {
            return $this->error('Tugas hanya bisa diselesaikan jika status "proses"', 422);
        }

        $penugasan->update([
            'status' => 'menunggu_validasi',
            'status_changed_by' => $request->user()->id,
        ]);

        // Kirim notifikasi ke owner/pembuat tugas untuk validasi manual
        $this->sendPenugasanSelesaiNotification($penugasan, $request->user());

        $penugasan->load(['penanggungJawab.user', 'divisi', 'pembuat']);

        return $this->success($penugasan, 'Tugas menunggu validasi pemilik/manager');
    }

    /**
     * PUT /api/penugasans/{penugasan}/validasi
     * Pemilik/manager memvalidasi tugas yang menunggu_validasi.
     * disetujui=true → selesai (poin diberikan). disetujui=false → kembali ke proses.
     */
    public function validasi(Request $request, penugasan $penugasan)
    {
        $this->authorize('validasi', $penugasan);

        if ($penugasan->status !== 'menunggu_validasi') {
            return $this->error('Tugas belum menunggu validasi', 422);
        }

        $disetujui = $request->boolean('disetujui', true);

        if ($disetujui) {
            $penugasan->update([
                'status' => 'selesai',
                'completed_at' => now(),
                'poin' => penugasan::getPoinForUrgency($penugasan->urgency),
                'status_changed_by' => $request->user()->id,
            ]);
        } else {
            $penugasan->update([
                'status' => 'proses',
                'status_changed_by' => $request->user()->id,
            ]);
        }

        $penugasan->load(['penanggungJawab.user', 'divisi', 'pembuat']);

        return $this->success(
            $penugasan,
            $disetujui ? 'Tugas disetujui & selesai' : 'Tugas dikembalikan ke karyawan'
        );
    }

    /**
     * DELETE /api/penugasans/{penugasan}
     */
    public function destroy(penugasan $penugasan)
    {
        $penugasan->delete();

        return $this->success(null, 'Tugas berhasil dihapus');
    }

    /**
     * Kirim notifikasi ke owner/pembuat tugas saat tugas dikerjakan.
     */
    private function sendPenugasanDikerjakanNotification(penugasan $penugasan, User $currentUser): void
    {
        $karyawan = $currentUser->profilKaryawan;
        $namaKaryawan = $karyawan->nama_lengkap ?? $currentUser->name;

        // Kirim ke pembuat tugas (owner/manager) jika berbeda dari pelaku
        if ($penugasan->created_by && $penugasan->created_by !== $currentUser->id) {
            $this->notificationService->onPenugasanDikerjakan(
                $penugasan->id,
                $penugasan->created_by,
                $namaKaryawan,
                $penugasan->judul
            );

            return;
        }

        // Bug #2: Fallback — untuk task dari template, kirim ke owner/manager instansi
        $recipientIds = User::where('instansi_id', $currentUser->instansi_id)
            ->whereHas('role', fn ($q) => $q->whereIn('nama_role', ['Owner', 'Manager']))
            ->where('id', '!=', $currentUser->id)
            ->pluck('id');

        foreach ($recipientIds as $recipientId) {
            $this->notificationService->onPenugasanDikerjakan(
                $penugasan->id,
                $recipientId,
                $namaKaryawan,
                $penugasan->judul
            );
        }
    }

    /**
     * Kirim notifikasi ke owner/pembuat tugas saat tugas selesai.
     */
    private function sendPenugasanSelesaiNotification(penugasan $penugasan, User $currentUser): void
    {
        $karyawan = $currentUser->profilKaryawan;
        $namaKaryawan = $karyawan->nama_lengkap ?? $currentUser->name;

        // Kirim ke pembuat tugas (owner/manager) jika berbeda dari pelaku
        if ($penugasan->created_by && $penugasan->created_by !== $currentUser->id) {
            $this->notificationService->onPenugasanSelesai(
                $penugasan->id,
                $penugasan->created_by,
                $namaKaryawan,
                $penugasan->judul
            );

            return;
        }

        // Bug #2: Fallback — untuk task dari template, kirim ke owner/manager instansi
        $recipientIds = User::where('instansi_id', $currentUser->instansi_id)
            ->whereHas('role', fn ($q) => $q->whereIn('nama_role', ['Owner', 'Manager']))
            ->where('id', '!=', $currentUser->id)
            ->pluck('id');

        foreach ($recipientIds as $recipientId) {
            $this->notificationService->onPenugasanSelesai(
                $penugasan->id,
                $recipientId,
                $namaKaryawan,
                $penugasan->judul
            );
        }
    }
}
