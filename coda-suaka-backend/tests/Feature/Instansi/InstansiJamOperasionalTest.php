<?php

namespace Tests\Feature\Instansi;

use App\Models\Instansi;
use App\Models\role;
use App\Models\role_permission;
use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

class InstansiJamOperasionalTest extends TestCase
{
    use RefreshDatabase;

    private Instansi $instansi;

    private role $ownerRole;

    private User $ownerUser;

    protected function setUp(): void
    {
        parent::setUp();

        // ── Setup Instansi ──
        $this->instansi = Instansi::factory()->create();

        // ── Setup Role ──
        $this->ownerRole = role::firstOrCreate(
            ['nama_role' => 'Owner'],
            ['deskripsi' => 'Owner']
        );

        // ── Setup Permission ──
        // Buat permission manage:instansi untuk role Owner
        role_permission::firstOrCreate(
            ['role_id' => $this->ownerRole->id, 'permission' => 'manage:instansi'],
            ['created_at' => now(), 'updated_at' => now()]
        );

        // ── Setup Owner User ──
        $this->ownerUser = User::factory()->create([
            'instansi_id' => $this->instansi->id,
            'role_id' => $this->ownerRole->id,
            'email_verified_at' => now(),
        ]);
    }

    // ════════════════════════════════════════════════════════════
    //  GET INSTANSI TESTS
    // ════════════════════════════════════════════════════════════

    public function test_owner_dapat_melihat_instansi_dengan_jam_operasional()
    {
        // Arrange
        $this->instansi->update([
            'jam_operasional' => json_encode([
                'jam_buka' => '08:00',
                'jam_tutup' => '20:00',
                'hari_operasional' => [1, 2, 3, 4, 5, 6],
            ]),
        ]);

        // Act
        $response = $this->actingAs($this->ownerUser)
            ->getJson('/api/instansi');

        // Assert
        $response->assertStatus(200)
            ->assertJson([
                'status' => 'success',
            ]);

        $data = $response->json('data');
        $this->assertArrayHasKey('jam_operasional', $data);
        // jam_operasional bisa berupa array langsung atau JSON string
        $jamOp = is_string($data['jam_operasional'])
            ? json_decode($data['jam_operasional'], true)
            : $data['jam_operasional'];
        $this->assertEquals('08:00', $jamOp['jam_buka']);
        $this->assertEquals('20:00', $jamOp['jam_tutup']);
    }

    // ════════════════════════════════════════════════════════════
    //  UPDATE INSTANSI TESTS
    // ════════════════════════════════════════════════════════════

    public function test_owner_dapat_update_jam_operasional()
    {
        // Act
        $response = $this->actingAs($this->ownerUser)
            ->putJson('/api/instansi', [
                'jam_operasional' => [
                    'jam_buka' => '09:00',
                    'jam_tutup' => '21:00',
                    'hari_operasional' => [1, 2, 3, 4, 5],
                ],
            ]);

        // Assert
        $response->assertStatus(200)
            ->assertJson([
                'status' => 'success',
            ]);

        // Verifikasi di database
        $this->instansi->refresh();
        // Model cast 'array' otomatis decode JSON ke array
        $jamOperasional = $this->instansi->jam_operasional;
        if (is_string($jamOperasional)) {
            $jamOperasional = json_decode($jamOperasional, true);
        }
        $this->assertEquals('09:00', $jamOperasional['jam_buka']);
        $this->assertEquals('21:00', $jamOperasional['jam_tutup']);
        $this->assertEquals([1, 2, 3, 4, 5], $jamOperasional['hari_operasional']);
    }

    public function test_owner_dapat_update_nama_instansi_saja_tanpa_jam_operasional()
    {
        // Act
        $response = $this->actingAs($this->ownerUser)
            ->putJson('/api/instansi', [
                'nama_instansi' => 'Nama Baru Salon',
            ]);

        // Assert
        $response->assertStatus(200);

        $this->instansi->refresh();
        $this->assertEquals('Nama Baru Salon', $this->instansi->nama_instansi);
    }

    public function test_owner_dapat_update_jam_operasional_null()
    {
        // Arrange: set jam_operasional dulu
        $this->instansi->update([
            'jam_operasional' => json_encode([
                'jam_buka' => '08:00',
                'jam_tutup' => '20:00',
            ]),
        ]);

        // Act: update dengan null
        $response = $this->actingAs($this->ownerUser)
            ->putJson('/api/instansi', [
                'jam_operasional' => null,
            ]);

        // Assert
        $response->assertStatus(200);

        $this->instansi->refresh();
        $this->assertNull($this->instansi->jam_operasional);
    }

    // ════════════════════════════════════════════════════════════
    //  VALIDATION TESTS
    // ════════════════════════════════════════════════════════════

    public function test_jam_operasional_harus_berupa_array()
    {
        $response = $this->actingAs($this->ownerUser)
            ->putJson('/api/instansi', [
                'jam_operasional' => 'bukan array',
            ]);

        $response->assertStatus(422);
    }

    public function test_hari_operasional_harus_berupa_array()
    {
        $response = $this->actingAs($this->ownerUser)
            ->putJson('/api/instansi', [
                'jam_operasional' => [
                    'jam_buka' => '08:00',
                    'jam_tutup' => '20:00',
                    'hari_operasional' => 'bukan array',
                ],
            ]);

        $response->assertStatus(422);
    }

    public function test_unauthenticated_tidak_dapat_update_instansi()
    {
        $response = $this->putJson('/api/instansi', [
            'jam_operasional' => [
                'jam_buka' => '08:00',
                'jam_tutup' => '20:00',
            ],
        ]);

        $response->assertStatus(401);
    }
}
