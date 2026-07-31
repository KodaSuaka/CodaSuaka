<?php

namespace Tests\Feature\Karyawan;

use App\Models\Instansi;
use App\Models\karyawan;
use App\Models\outlet;
use App\Models\role;
use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Support\Facades\DB;
use Tests\TestCase;

class KaryawanIndexQueryCountTest extends TestCase
{
    use RefreshDatabase;

    /**
     * GET /api/karyawans eager-loads user.role + outlet. Assert the query
     * count stays flat regardless of how many karyawan rows exist (no N+1
     * from lazy-loading user/role/outlet per row).
     */
    public function test_query_count_tetap_flat_saat_jumlah_karyawan_bertambah()
    {
        $instansi = Instansi::factory()->create();
        $staffRole = role::firstOrCreate(['nama_role' => 'Karyawan']);
        $ownerRole = role::firstOrCreate(['nama_role' => 'Owner']);
        $outlet = outlet::factory()->create(['instansi_id' => $instansi->id]);

        $owner = User::factory()->create([
            'instansi_id' => $instansi->id,
            'role_id' => $ownerRole->id,
        ]);

        $countFor = function (int $n) use ($instansi, $staffRole, $outlet, $owner) {
            karyawan::query()->delete();
            User::where('role_id', $staffRole->id)->delete();

            foreach (range(1, $n) as $i) {
                $user = User::factory()->create([
                    'instansi_id' => $instansi->id,
                    'role_id' => $staffRole->id,
                    'outlet_id' => $outlet->id,
                ]);
                karyawan::create([
                    'user_id' => $user->id,
                    'nama_lengkap' => "Karyawan $i",
                    'outlet_id' => $outlet->id,
                ]);
            }

            DB::enableQueryLog();
            $response = $this->actingAs($owner)->getJson('/api/karyawans');
            $response->assertStatus(200);
            $queryCount = count(DB::getQueryLog());
            DB::disableQueryLog();
            DB::flushQueryLog();

            return $queryCount;
        };

        $small = $countFor(5);
        $large = $countFor(20);

        $this->assertSame($small, $large, "Query count grew from {$small} to {$large} as karyawan count grew — N+1 suspected.");
    }
}
