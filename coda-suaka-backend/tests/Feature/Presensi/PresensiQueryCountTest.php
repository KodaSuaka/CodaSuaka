<?php

namespace Tests\Feature\Presensi;

use App\Models\attandence;
use App\Models\Instansi;
use App\Models\karyawan;
use App\Models\role;
use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Support\Facades\DB;
use Tests\TestCase;

/**
 * Regresi N+1: GET /api/presensis dan GET /api/rekap-kehadiran harus
 * eager-load relasi user/karyawan sekali di awal — jumlah query TIDAK
 * BOLEH bertambah seiring bertambahnya jumlah karyawan/baris presensi (N).
 */
class PresensiQueryCountTest extends TestCase
{
    use RefreshDatabase;

    private Instansi $instansi;

    private role $ownerRole;

    private User $ownerUser;

    protected function setUp(): void
    {
        parent::setUp();

        $this->instansi = Instansi::factory()->create(['timezone' => 'Asia/Jakarta']);

        $this->ownerRole = role::firstOrCreate(
            ['nama_role' => 'Owner'],
            ['deskripsi' => 'Owner']
        );

        $this->ownerUser = User::factory()->create([
            'instansi_id' => $this->instansi->id,
            'role_id' => $this->ownerRole->id,
            'email_verified_at' => now(),
        ]);

        DB::table('role_permissions')->insert([
            'role_id' => $this->ownerRole->id,
            'permission' => 'view:presensi',
        ]);
    }

    /**
     * Buat $count karyawan (user berbeda) masing-masing dengan satu baris
     * presensi hari ini, supaya relasi 'user' tidak bisa kebetulan hemat
     * query gara-gara identity map.
     */
    private function seedPresensis(int $count): void
    {
        for ($i = 0; $i < $count; $i++) {
            $user = User::factory()->create([
                'instansi_id' => $this->instansi->id,
                'role_id' => $this->ownerRole->id,
            ]);
            karyawan::create([
                'user_id' => $user->id,
                'nama_lengkap' => "Karyawan $i",
            ]);
            attandence::create([
                'user_id' => $user->id,
                'tanggal' => now()->toDateString(),
                'jam_checkin' => '07:30:00',
                'status' => 'hadir',
            ]);
        }
    }

    private function queryCountForIndex(int $n): int
    {
        $this->seedPresensis($n);

        DB::enableQueryLog();
        $response = $this->actingAs($this->ownerUser)->getJson('/api/presensis');
        $count = count(DB::getQueryLog());
        DB::disableQueryLog();
        DB::flushQueryLog();

        $response->assertStatus(200);
        $this->assertCount($n, $response->json('data'));

        return $count;
    }

    public function test_presensi_index_query_count_konstan_untuk_n_kecil(): void
    {
        $count = $this->queryCountForIndex(5);

        $this->assertLessThanOrEqual(
            10,
            $count,
            "Query count untuk 5 baris ({$count}) melebihi batas wajar 10 — indikasi N+1."
        );
    }

    public function test_presensi_index_query_count_konstan_untuk_n_besar(): void
    {
        $count = $this->queryCountForIndex(20);

        $this->assertLessThanOrEqual(
            10,
            $count,
            "Query count untuk 20 baris ({$count}) melebihi batas wajar 10 — endpoint tidak O(1), N+1 terdeteksi."
        );
    }

    private function queryCountForRekap(int $n): int
    {
        $this->seedPresensis($n);

        DB::enableQueryLog();
        $response = $this->actingAs($this->ownerUser)->getJson('/api/rekap-kehadiran');
        $count = count(DB::getQueryLog());
        DB::disableQueryLog();
        DB::flushQueryLog();

        $response->assertStatus(200);
        // rekap() hanya menyertakan user yang punya minimal 1 baris attandence
        // di bulan berjalan — ownerUser sendiri tidak checkin, jadi tidak muncul.
        $this->assertCount($n, $response->json('data'));

        return $count;
    }

    public function test_rekap_kehadiran_query_count_konstan_untuk_n_kecil(): void
    {
        $count = $this->queryCountForRekap(5);

        $this->assertLessThanOrEqual(
            10,
            $count,
            "Query count rekap untuk 5 karyawan ({$count}) melebihi batas wajar 10 — indikasi N+1."
        );
    }

    public function test_rekap_kehadiran_query_count_konstan_untuk_n_besar(): void
    {
        $count = $this->queryCountForRekap(20);

        $this->assertLessThanOrEqual(
            10,
            $count,
            "Query count rekap untuk 20 karyawan ({$count}) melebihi batas wajar 10 — endpoint tidak O(1), N+1 terdeteksi."
        );
    }
}
