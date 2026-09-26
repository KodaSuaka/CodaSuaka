<?php

namespace App\Services;

use App\Models\Notification;
use App\Models\User;
use Illuminate\Contracts\Pagination\LengthAwarePaginator;
use Illuminate\Support\Collection;

class NotificationService
{
    /**
     * Buat notifikasi baru.
     */
    public function create(
        int $userId,
        string $type,
        string $title,
        string $body,
        string $icon = 'Bell',
        string $color = '#6366F1',
        ?int $relatedId = null,
        ?string $relatedType = null,
    ): Notification {
        return Notification::create([
            'user_id' => $userId,
            'type' => $type,
            'title' => $title,
            'body' => $body,
            'icon' => $icon,
            'color' => $color,
            'is_read' => false,
            'related_id' => $relatedId,
            'related_type' => $relatedType,
        ]);
    }

    /**
     * Kirim notifikasi ke banyak user sekaligus.
     */
    public function createForUsers(
        array $userIds,
        string $type,
        string $title,
        string $body,
        string $icon = 'Bell',
        string $color = '#6366F1',
        ?int $relatedId = null,
        ?string $relatedType = null,
    ): Collection {
        $notifications = collect();
        foreach ($userIds as $userId) {
            $notifications->push(
                $this->create($userId, $type, $title, $body, $icon, $color, $relatedId, $relatedType)
            );
        }

        return $notifications;
    }

    /**
     * Kirim notifikasi ke semua user dalam instansi.
     */
    public function createForInstansi(
        string $instansiId,
        string $type,
        string $title,
        string $body,
        string $icon = 'Bell',
        string $color = '#6366F1',
        ?int $relatedId = null,
        ?string $relatedType = null,
        ?array $excludeUserIds = null,
    ): Collection {
        $query = User::where('instansi_id', $instansiId);
        if ($excludeUserIds) {
            $query->whereNotIn('id', $excludeUserIds);
        }
        $userIds = $query->pluck('id')->toArray();

        return $this->createForUsers($userIds, $type, $title, $body, $icon, $color, $relatedId, $relatedType);
    }

    /**
     * Ambil notifikasi user dengan pagination.
     */
    public function getUserNotifications(int $userId, int $perPage = 20): LengthAwarePaginator
    {
        return Notification::forUser($userId)
            ->orderBy('created_at', 'desc')
            ->paginate($perPage);
    }

    /**
     * Ambil jumlah notifikasi belum dibaca.
     */
    public function getUnreadCount(int $userId): int
    {
        return Notification::forUser($userId)->unread()->count();
    }

    /**
     * Tandai satu notifikasi sudah dibaca.
     */
    public function markAsRead(int $notificationId, int $userId): ?Notification
    {
        $notification = Notification::where('id', $notificationId)
            ->where('user_id', $userId)
            ->first();

        if ($notification) {
            $notification->markAsRead();
        }

        return $notification;
    }

    /**
     * Tandai semua notifikasi user sudah dibaca.
     */
    public function markAllAsRead(int $userId): int
    {
        return Notification::forUser($userId)
            ->unread()
            ->update(['is_read' => true]);
    }

    /**
     * Hapus notifikasi user.
     */
    public function delete(int $notificationId, int $userId): bool
    {
        $notification = Notification::where('id', $notificationId)
            ->where('user_id', $userId)
            ->first();

        if ($notification) {
            $notification->delete();

            return true;
        }

        return false;
    }

    /**
     * Notifikasi otomatis saat pengajuan baru.
     */
    public function onPengajuanBaru(int $pengajuanId, int $userId, string $namaKaryawan, string $jenisPengajuan): void
    {
        // Kirim ke Manager/Owner di instansi yang sama
        $user = User::find($userId);
        if (! $user) {
            return;
        }

        $this->createForInstansi(
            $user->instansi_id,
            'pengajuan',
            'Pengajuan Baru',
            "{$namaKaryawan} mengajukan {$jenisPengajuan}",
            'Description',
            '#F59E0B',
            $pengajuanId,
            'App\\Models\\pengajuan',
            [$userId] // Exclude pengaju
        );
    }

    /**
     * Notifikasi otomatis saat pengajuan disetujui/ditolak.
     */
    public function onPengajuanStatusChanged(int $pengajuanId, int $userId, string $status, string $jenisPengajuan): void
    {
        $this->create(
            $userId,
            'pengajuan',
            'Status Pengajuan',
            "Pengajuan {$jenisPengajuan} anda telah {$status}",
            $status === 'disetujui' ? 'CheckCircle' : 'Cancel',
            $status === 'disetujui' ? '#10B981' : '#EF4444',
            $pengajuanId,
            'App\\Models\\pengajuan'
        );
    }

    /**
     * Notifikasi otomatis saat penugasan baru.
     */
    public function onPenugasanBaru(int $penugasanId, int $userId, string $judul): void
    {
        $this->create(
            $userId,
            'penugasan',
            'Penugasan Baru',
            "Anda mendapat penugasan baru: {$judul}",
            'Assignment',
            '#6366F1',
            $penugasanId,
            'App\\Models\\penugasan'
        );
    }

    /**
     * Notifikasi otomatis saat tugas diterima/dikerjakan oleh karyawan.
     */
    public function onPenugasanDikerjakan(int $penugasanId, int $ownerUserId, string $namaKaryawan, string $judul): void
    {
        $this->create(
            $ownerUserId,
            'penugasan',
            'Tugas Sedang Dikerjakan',
            "{$namaKaryawan} sedang mengerjakan tugas: {$judul}",
            'Assignment',
            '#3B82F6',
            $penugasanId,
            'App\\Models\\penugasan'
        );
    }

    /**
     * Notifikasi otomatis saat karyawan menandai tugas selesai — menunggu
     * validasi manual dari pemilik/manager sebelum benar-benar dianggap selesai.
     */
    public function onPenugasanSelesai(int $penugasanId, int $ownerUserId, string $namaKaryawan, string $judul): void
    {
        $this->create(
            $ownerUserId,
            'penugasan',
            'Menunggu Validasi Tugas',
            "{$namaKaryawan} menandai tugas selesai, menunggu validasi Anda: {$judul}",
            'CheckCircle',
            '#10B981',
            $penugasanId,
            'App\\Models\\penugasan'
        );
    }

    /**
     * Notifikasi otomatis saat presensi.
     */
    public function onPresensi(int $userId, string $jenis): void
    {
        $this->create(
            $userId,
            'presensi',
            'Presensi '.ucfirst($jenis),
            "Anda telah {$jenis} pada hari ini",
            'AccessTime',
            '#3B82F6',
            null,
            null
        );
    }

    /**
     * Notifikasi otomatis saat ada transaksi keuangan pending approval.
     */
    public function onTransaksiPendingApproval(int $transaksiId, int $pemeriksaId, string $keterangan): void
    {
        $this->create(
            $pemeriksaId,
            'keuangan',
            'Transaksi Menunggu Approval',
            "Transaksi baru menunggu approval: {$keterangan}",
            'AccountBalance',
            '#8B5CF6',
            $transaksiId,
            'App\\Models\\TransaksiKas'
        );
    }
}
