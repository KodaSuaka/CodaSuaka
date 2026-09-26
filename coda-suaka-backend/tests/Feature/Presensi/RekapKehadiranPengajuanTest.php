<?php

namespace Tests\Feature\Presensi;

use App\Models\attandence;
use App\Models\Instansi;
use App\Models\pengajuan;
use App\Models\role;
use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Support\Facades\DB;
use Tests\TestCase;

/**
 * Rekap kehadiran harus ikut menghitung pengajuan yang MELINGKUPI seluruh
 * bulan rekap (mulai sebelum tanggal 1, selesai setelah akhir bulan).
 *
 * Cabang query untuk kasus ini sebelumnya membandingkan tanggal_selesai
 * dengan string "$tahun-$bulan-31" (hari ke-31 di-hardcode). Di MySQL itu
 * TETAP JALAN — DATE hanya divalidasi rentangnya, bukan kalendernya, jadi
 * '2026-02-31' sah sebagai nilai pembanding. Jadi bukan itu sumber bug-nya.
 *
 * Yang justru ketahuan lewat test ini: hari_kerja terkirim sebagai pecahan
 * (24.999...) karena diffInDays() mengembalikan float sejak Carbon 3,
 * sehingga hari_kerja dan total_alpha selalu kelebihan satu hari.
 *
 * Test ini mengunci kedua hal itu: pengajuan yang melingkupi seluruh bulan
 * tetap terhitung, dan hari_kerja tetap bilangan bulat yang benar.
 */
class RekapKehadiranPengajuanTest extends TestCase
{
    use RefreshDatabase;

    private Instansi $instansi;

    private User $ownerUser;

    private User $karyawanUser;

    protected function setUp(): void
    {
        parent::setUp();

        $this->instansi = Instansi::factory()->create(['timezone' => 'Asia/Jakarta']);

        $ownerRole = role::firstOrCreate(
            ['nama_role' => 'Owner'],
            ['deskripsi' => 'Owner']
        );

        DB::table('role_permissions')->insertOrIgnore([
            'role_id' => $ownerRole->id,
            'permission' => 'view:presensi',
        ]);

        $this->ownerUser = User::factory()->create([
            'instansi_id' => $this->instansi->id,
            'role_id' => $ownerRole->id,
            'email_verified_at' => now(),
        ]);

        $this->karyawanUser = User::factory()->create([
            'instansi_id' => $this->instansi->id,
            'role_id' => $ownerRole->id,
            'email_verified_at' => now(),
        ]);
    }

    /**
     * Februari 2026: 28 hari, tanggal 1/8/15/22 jatuh Minggu -> 24 hari kerja.
     * Pengajuan 15 Jan -> 15 Mar melingkupi seluruh Februari, jadi hanya
     * cabang "mencakup seluruh bulan" yang bisa menangkapnya (tanggal_mulai
     * ada di Januari, tanggal_selesai ada di Maret).
     */
    public function test_pengajuan_yang_melingkupi_seluruh_bulan_pendek_tetap_dihitung()
    {
        pengajuan::create([
            'user_id' => $this->karyawanUser->id,
            'jenis' => 'mendadak',
            'tanggal_mulai' => '2026-01-15',
            'tanggal_selesai' => '2026-03-15',
            'keterangan' => 'Izin panjang lintas bulan',
            'status' => 'disetujui',
        ]);

        // Rekap hanya memuat user yang punya minimal satu baris attandence.
        attandence::create([
            'user_id' => $this->karyawanUser->id,
            'tanggal' => '2026-02-02',
            'status' => 'hadir',
        ]);

        $response = $this->actingAs($this->ownerUser)
            ->getJson('/api/rekap-kehadiran?bulan=2&tahun=2026');

        $response->assertStatus(200);

        $row = collect($response->json('data'))
            ->firstWhere('user_id', $this->karyawanUser->id);

        $this->assertNotNull($row, 'Karyawan tidak muncul di rekap.');
        $this->assertSame(24, $row['hari_kerja']);
        $this->assertSame(1, $row['total_hadir']);
        // 24 hari kerja - 1 hari sudah hadir = 23 hari tercatat sebagai izin.
        $this->assertSame(23, $row['total_izin']);
        $this->assertSame(0, $row['total_alpha']);
    }

    /**
     * Kontrol untuk bulan yang PUNYA tanggal 31, supaya perbaikan batas
     * bulan tidak merusak jalur ini. (hari_kerja di bulan ini pun ikut
     * salah sebelum perbaikan Carbon: 27.999... alih-alih 27.)
     *
     * Januari 2026: 31 hari, tanggal 4/11/18/25 Minggu -> 27 hari kerja.
     */
    public function test_pengajuan_yang_melingkupi_seluruh_bulan_panjang_tetap_dihitung()
    {
        pengajuan::create([
            'user_id' => $this->karyawanUser->id,
            'jenis' => 'mendadak',
            'tanggal_mulai' => '2025-12-15',
            'tanggal_selesai' => '2026-02-15',
            'keterangan' => 'Izin panjang lintas bulan',
            'status' => 'disetujui',
        ]);

        attandence::create([
            'user_id' => $this->karyawanUser->id,
            'tanggal' => '2026-01-05',
            'status' => 'hadir',
        ]);

        $response = $this->actingAs($this->ownerUser)
            ->getJson('/api/rekap-kehadiran?bulan=1&tahun=2026');

        $response->assertStatus(200);

        $row = collect($response->json('data'))
            ->firstWhere('user_id', $this->karyawanUser->id);

        $this->assertNotNull($row, 'Karyawan tidak muncul di rekap.');
        $this->assertSame(27, $row['hari_kerja']);
        $this->assertSame(1, $row['total_hadir']);
        $this->assertSame(26, $row['total_izin']);
        $this->assertSame(0, $row['total_alpha']);
    }
}
