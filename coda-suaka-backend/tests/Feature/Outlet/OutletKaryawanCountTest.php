<?php

namespace Tests\Feature\Outlet;

use App\Models\Instansi;
use App\Models\karyawan;
use App\Models\outlet;
use App\Models\role;
use App\Models\role_permission;
use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

class OutletKaryawanCountTest extends TestCase
{
    use RefreshDatabase;

    public function test_outlet_list_menyertakan_jumlah_karyawan()
    {
        $instansi = Instansi::factory()->create();
        $ownerRole = role::firstOrCreate(['nama_role' => 'Owner'], ['deskripsi' => 'Owner']);
        role_permission::firstOrCreate(
            ['role_id' => $ownerRole->id, 'permission' => 'view:outlets'],
            ['created_at' => now(), 'updated_at' => now()]
        );
        $owner = User::factory()->create([
            'instansi_id' => $instansi->id,
            'role_id' => $ownerRole->id,
            'email_verified_at' => now(),
        ]);

        $outletA = outlet::factory()->create(['instansi_id' => $instansi->id]);
        $outletB = outlet::factory()->create(['instansi_id' => $instansi->id]);

        // 2 karyawan di outlet A, 0 di outlet B
        foreach (range(1, 2) as $i) {
            $user = User::factory()->create(['instansi_id' => $instansi->id, 'outlet_id' => $outletA->id]);
            karyawan::create([
                'user_id' => $user->id,
                'nama_lengkap' => "Karyawan $i",
                'outlet_id' => $outletA->id,
            ]);
        }

        $response = $this->actingAs($owner)->getJson('/api/outlets');

        $response->assertStatus(200);
        $data = collect($response->json('data'));

        $this->assertEquals(2, $data->firstWhere('id', $outletA->id)['karyawans_count']);
        $this->assertEquals(0, $data->firstWhere('id', $outletB->id)['karyawans_count']);
    }
}
