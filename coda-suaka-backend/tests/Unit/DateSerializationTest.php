<?php

namespace Tests\Unit;

use App\Models\attandence;
use App\Models\jadwal;
use App\Models\Nota;
use App\Models\pengajuan;
use App\Models\penugasan;
use App\Models\transaksi_paket;
use App\Models\TransaksiKas;
use PHPUnit\Framework\Attributes\DataProvider;
use Tests\TestCase;

/**
 * Kolom bertipe tanggal (bukan datetime) harus terserialisasi apa adanya.
 *
 * Cast 'date' polos diserialisasi ke ISO-8601 UTC. Karena app.timezone
 * Asia/Jakarta (UTC+7), 2026-07-15 berubah jadi "2026-07-14T17:00:00Z" —
 * mundur satu hari. Client Kotlin yang memakai
 * DateTimeUtil.formatDateDisplay() melakukan LocalDate.parse(take(10)),
 * jadi menampilkan tanggal 14. Itu sebabnya tanggal salah di sebagian
 * layar saja: helper lain (formatIsoToLocal) mem-parse sebagai Instant
 * lalu mengonversi zona, sehingga kebetulan benar.
 *
 * Perbaikannya cast 'date:Y-m-d' supaya tidak ada konversi zona sama
 * sekali untuk kolom yang memang hanya menyimpan tanggal.
 */
class DateSerializationTest extends TestCase
{
    public static function kolomTanggalProvider(): array
    {
        return [
            'attandence.tanggal' => [attandence::class, 'tanggal'],
            'jadwal.tanggal' => [jadwal::class, 'tanggal'],
            'pengajuan.tanggal_mulai' => [pengajuan::class, 'tanggal_mulai'],
            'pengajuan.tanggal_selesai' => [pengajuan::class, 'tanggal_selesai'],
            'penugasan.tenggat' => [penugasan::class, 'tenggat'],
            'transaksi_paket.tanggal_mulai' => [transaksi_paket::class, 'tanggal_mulai'],
            'transaksi_paket.tanggal_berakhir' => [transaksi_paket::class, 'tanggal_berakhir'],
            'TransaksiKas.tanggal' => [TransaksiKas::class, 'tanggal'],
            'Nota.tanggal' => [Nota::class, 'tanggal'],
        ];
    }

    #[DataProvider('kolomTanggalProvider')]
    public function test_kolom_tanggal_tidak_bergeser_saat_diserialisasi(string $model, string $kolom)
    {
        // Timezone app harus +UTC supaya pergeseran benar-benar teruji.
        $this->assertSame('Asia/Jakarta', config('app.timezone'));

        $instance = new $model;
        $instance->forceFill([$kolom => '2026-07-15']);

        $this->assertSame(
            '2026-07-15',
            $instance->toArray()[$kolom],
            "$model::\$casts['$kolom'] menggeser tanggal — pakai 'date:Y-m-d'."
        );
    }
}
