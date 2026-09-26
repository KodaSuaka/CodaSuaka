<?php

namespace Tests\Feature\Penugasan;

use App\Models\Divisi;
use App\Models\Instansi;
use App\Models\karyawan;
use App\Models\outlet;
use App\Models\penugasan;
use App\Models\role;
use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Support\Facades\DB;
use Tests\TestCase;

/**
 * Regresi N+1: GET /api/penugasans harus eager-load penanggungJawab.user,
 * divisi, dan pembuat sekali di awal — jumlah query TIDAK BOLEH bertambah
 * seiring bertambahnya jumlah baris (N).
 */
class PenugasanIndexQueryCountTest extends TestCase
{
    use RefreshDatabase;

    private Instansi $instansi;

    private outlet $outlet;

    private role $ownerRole;

    private User $ownerUser;

    protected function setUp(): void
    {
        parent::setUp();

        $this->instansi = Instansi::factory()->create();
        $this->outlet = outlet::factory()->create(['instansi_id' => $this->instansi->id]);

        $this->ownerRole = role::firstOrCreate(
            ['nama_role' => 'Owner'],
            ['deskripsi' => 'Owner']
        );

        $this->ownerUser = User::factory()->create([
            'instansi_id' => $this->instansi->id,
            'role_id' => $this->ownerRole->id,
            'outlet_id' => $this->outlet->id,
            'email_verified_at' => now(),
        ]);

        DB::table('role_permissions')->insert([
            'role_id' => $this->ownerRole->id,
            'permission' => 'manage:penugasan',
        ]);
    }

    /**
     * Buat $count penugasan, masing-masing dengan divisi + penanggung jawab
     * + pembuat YANG BERBEDA, supaya relasi tidak bisa "kebetulan" hemat
     * query gara-gara identity map Eloquent.
     */
    private function seedPenugasans(int $count): void
    {
        for ($i = 0; $i < $count; $i++) {
            $divisi = Divisi::create([
                'nama_divisi' => "Divisi $i",
                'outlet_id' => $this->outlet->id,
            ]);

            $karyawanUser = User::factory()->create([
                'instansi_id' => $this->instansi->id,
                'role_id' => $this->ownerRole->id,
                'outlet_id' => $this->outlet->id,
            ]);
            $karyawan = karyawan::create([
                'user_id' => $karyawanUser->id,
                'nama_lengkap' => "Karyawan $i",
                'outlet_id' => $this->outlet->id,
            ]);

            $creator = User::factory()->create([
                'instansi_id' => $this->instansi->id,
                'role_id' => $this->ownerRole->id,
                'outlet_id' => $this->outlet->id,
            ]);

            penugasan::create([
                'judul' => "Tugas $i",
                'penanggung_jawab_id' => $karyawan->id,
                'divisi_id' => $divisi->id,
                'status' => 'belum',
                'urgency' => 'sedang',
                'created_by' => $creator->id,
                'instansi_id' => $this->instansi->id,
            ]);
        }
    }

    private function queryCountFor(int $n): int
    {
        $this->seedPenugasans($n);

        DB::enableQueryLog();
        $response = $this->actingAs($this->ownerUser)->getJson('/api/penugasans');
        $count = count(DB::getQueryLog());
        DB::disableQueryLog();
        DB::flushQueryLog();

        $response->assertStatus(200);
        $this->assertCount($n, $response->json('data'));

        return $count;
    }

    /**
     * Query count harus tetap konstan (bounded) baik untuk N=5 maupun N=20.
     * Jika endpoint N+1, query count untuk N=20 akan jauh lebih besar dari N=5.
     */
    public function test_index_query_count_konstan_untuk_n_kecil(): void
    {
        $count = $this->queryCountFor(5);

        $this->assertLessThanOrEqual(
            10,
            $count,
            "Query count untuk 5 baris ({$count}) melebihi batas wajar 10 — indikasi N+1."
        );
    }

    public function test_index_query_count_konstan_untuk_n_besar(): void
    {
        $count = $this->queryCountFor(20);

        $this->assertLessThanOrEqual(
            10,
            $count,
            "Query count untuk 20 baris ({$count}) melebihi batas wajar 10 — endpoint tidak O(1), N+1 terdeteksi."
        );
    }
}
