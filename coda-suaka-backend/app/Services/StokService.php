<?php

namespace App\Services;

use App\Models\Stok;
use App\Models\StokMutation;
use App\Models\User;
use Illuminate\Support\Facades\DB;

class StokService
{
    public function __construct(
        private NotificationService $notificationService,
    ) {
    }

    /**
     * Mutasi stok atomik (masuk / keluar / penyesuaian).
     * Mencatat snapshot sebelum & sesudah, lalu mengecek ambang stok minimum.
     *
     * @throws \InvalidArgumentException jika stok keluar melebihi stok tersedia
     */
    public function mutasi(
        Stok $stok,
        string $jenis,
        float|int|string $jumlah,
        ?User $user = null,
        ?string $keterangan = null,
    ): StokMutation {
        if (! in_array($jenis, Stok::JENIS_MUTASI_VALID, true)) {
            throw new \InvalidArgumentException("Jenis mutasi tidak valid: {$jenis}");
        }

        $jumlah = (float) $jumlah;

        if ($jumlah <= 0) {
            throw new \InvalidArgumentException('Jumlah mutasi harus lebih dari 0.');
        }

        return DB::transaction(function () use ($stok, $jenis, $jumlah, $user, $keterangan) {
            // Lock baris agar aman dari race condition.
            $stok = Stok::query()->lockForUpdate()->findOrFail($stok->id);

            $stokSebelum = (float) $stok->stok;
            $stokSesudah = $stokSebelum;

            if ($jenis === 'masuk') {
                $stokSesudah = $stokSebelum + $jumlah;
            } elseif ($jenis === 'keluar') {
                if ($stokSebelum < $jumlah) {
                    throw new \InvalidArgumentException(
                        "Stok tidak mencukupi. Tersedia: {$stokSebelum}, diminta: {$jumlah}."
                    );
                }
                $stokSesudah = $stokSebelum - $jumlah;
            } else { // penyesuaian: set stok ke nilai jumlah
                if ($jumlah < 0) {
                    throw new \InvalidArgumentException('Stok penyesuaian tidak boleh negatif.');
                }
                $stokSesudah = $jumlah;
            }

            $stok->update(['stok' => $stokSesudah]);

            $mutation = StokMutation::create([
                'instansi_id' => $stok->instansi_id,
                'stok_id' => $stok->id,
                'jenis' => $jenis,
                'jumlah' => $jumlah,
                'stok_sebelum' => $stokSebelum,
                'stok_sesudah' => $stokSesudah,
                'keterangan' => $keterangan,
                'user_id' => $user?->id,
            ]);

            $this->notifyJikaStokMinimum($stok, $user);

            return $mutation;
        });
    }

    /**
     * Kirim notifikasi ke owner/manager bila stok ≤ stok_minimum.
     */
    private function notifyJikaStokMinimum(Stok $stok, ?User $user): void
    {
        if ($stok->stok_minimum === null || $stok->stok_minimum === '') {
            return;
        }

        if ((float) $stok->stok > (float) $stok->stok_minimum) {
            return;
        }

        $instansiId = $stok->instansi_id;
        $roleNames = ['Owner', 'Manager'];

        $userIds = User::query()
            ->where('instansi_id', $instansiId)
            ->whereHas('role', fn ($q) => $q->whereIn('nama_role', $roleNames))
            ->pluck('id')
            ->all();

        if (empty($userIds)) {
            return;
        }

        $this->notificationService->createForUsers(
            $userIds,
            'stok_minimum',
            'Stok Menipis',
            "Stok \"{$stok->nama}\" tersisa {$stok->stok} {$stok->satuan} (minimum {$stok->stok_minimum}).",
            icon: 'Package',
            color: '#F59E0B',
            relatedId: $stok->id,
            relatedType: Stok::class,
        );
    }
}
