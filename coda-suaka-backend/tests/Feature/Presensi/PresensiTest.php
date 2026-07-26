<?php

namespace Tests\Feature\Presensi;

use App\Models\Instansi;
use App\Models\karyawan;
use App\Models\role;
use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

class PresensiTest extends TestCase
{
    use RefreshDatabase;

    private User $user;

    private Instansi $instansi;

    protected function setUp(): void
    {
        parent::setUp();

        $this->instansi = Instansi::factory()->create([
            'timezone' => 'Asia/Jakarta',
        ]);

        $role = role::firstOrCreate(
            ['nama_role' => 'Staff'],
            ['deskripsi' => 'Staff']
        );

        $this->user = User::factory()->create([
            'instansi_id' => $this->instansi->id,
            'role_id' => $role->id,
            'email_verified_at' => now(),
        ]);

        karyawan::firstOrCreate(
            ['user_id' => $this->user->id],
            [
                'nama_lengkap' => $this->user->name,
                'kontak' => '081234567890',
                'alamat' => 'Jakarta',
            ]
        );
    }

    public function test_user_dapat_checkin()
    {
        // Act
        $response = $this->actingAs($this->user)
            ->postJson('/api/presensis/checkin', [
                'lokasi' => '-6.2088,106.8456',
            ]);

        // Assert
        $response->assertStatus(200)
            ->assertJson([
                'status' => 'success',
            ]);

        $this->assertDatabaseHas('attandences', [
            'user_id' => $this->user->id,
            'status' => 'hadir',
        ]);
    }

    public function test_user_tidak_bisa_checkin_dua_kali()
    {
        // Arrange — checkin pertama
        $this->actingAs($this->user)
            ->postJson('/api/presensis/checkin', [
                'lokasi' => '-6.2088,106.8456',
            ]);

        // Act — checkin kedua
        $response = $this->actingAs($this->user)
            ->postJson('/api/presensis/checkin', [
                'lokasi' => '-6.2088,106.8456',
            ]);

        // Assert
        $response->assertStatus(409)
            ->assertJson([
                'status' => 'error',
            ]);
    }

    public function test_user_dapat_checkout_setelah_checkin()
    {
        // Arrange — checkin dulu
        $this->actingAs($this->user)
            ->postJson('/api/presensis/checkin', [
                'lokasi' => '-6.2088,106.8456',
            ]);

        // Act — checkout
        $response = $this->actingAs($this->user)
            ->postJson('/api/presensis/checkout');

        // Assert
        $response->assertStatus(200)
            ->assertJson([
                'status' => 'success',
            ]);
    }

    public function test_checkout_gagal_jika_belum_checkin()
    {
        // Act
        $response = $this->actingAs($this->user)
            ->postJson('/api/presensis/checkout');

        // Assert
        $response->assertStatus(400)
            ->assertJson([
                'status' => 'error',
            ]);
    }

    public function test_user_dapat_melihat_presensi_hari_ini()
    {
        // Arrange — checkin
        $this->actingAs($this->user)
            ->postJson('/api/presensis/checkin', [
                'lokasi' => '-6.2088,106.8456',
            ]);

        // Act
        $response = $this->actingAs($this->user)
            ->getJson('/api/presensis/today');

        // Assert
        $response->assertStatus(200)
            ->assertJson([
                'status' => 'success',
            ]);
    }

    public function test_lokasi_checkin_validasi_format_gps()
    {
        // Act — format GPS tidak valid
        $response = $this->actingAs($this->user)
            ->postJson('/api/presensis/checkin', [
                'lokasi' => 'format_tidak_valid',
            ]);

        // Assert
        $response->assertStatus(422)
            ->assertJsonValidationErrors(['lokasi']);
    }

    public function test_lokasi_checkin_null_diperbolehkan()
    {
        // Act — lokasi null
        $response = $this->actingAs($this->user)
            ->postJson('/api/presensis/checkin', []);

        // Assert — tetap berhasil (nullable)
        $response->assertStatus(200);
    }
}
