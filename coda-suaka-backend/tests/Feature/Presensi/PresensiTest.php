<?php

namespace Tests\Feature\Presensi;

use App\Models\Instansi;
use App\Models\karyawan;
use App\Models\role;
use App\Models\User;
use Carbon\Carbon;
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

    protected function tearDown(): void
    {
        // Pastikan test time di-reset setelah setiap test
        Carbon::setTestNow();
        parent::tearDown();
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
        // Arrange — checkin dulu (di waktu real)
        $this->actingAs($this->user)
            ->postJson('/api/presensis/checkin', [
                'lokasi' => '-6.2088,106.8456',
            ]);

        // Act — freeze waktu ke jam 17:00 Asia/Jakarta (setelah jam standar 16:30)
        Carbon::setTestNow(Carbon::now('Asia/Jakarta')->setTime(17, 0, 0));
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
        // Act — freeze waktu ke17:00 agar lolos time check, tapi belum checkin
        Carbon::setTestNow(Carbon::now('Asia/Jakarta')->setTime(17, 0, 0));
        $response = $this->actingAs($this->user)
            ->postJson('/api/presensis/checkout');

        // Assert — harus ditolak karena belum checkin
        $response->assertStatus(400)
            ->assertJson([
                'status' => 'error',
            ]);
    }

    public function test_checkout_gagal_sebelum_jam_16_30()
    {
        // Arrange — checkin dulu
        $this->actingAs($this->user)
            ->postJson('/api/presensis/checkin', [
                'lokasi' => '-6.2088,106.8456',
            ]);

        // Act — freeze waktu ke jam 15:00 Asia/Jakarta (sebelum jam standar 16:30)
        Carbon::setTestNow(Carbon::now('Asia/Jakarta')->setTime(15, 0, 0));
        $response = $this->actingAs($this->user)
            ->postJson('/api/presensis/checkout');

        // Assert — harus ditolak karena belum waktunya
        $response->assertStatus(422)
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

        // Assert — response harus berisi field standar waktu
        $response->assertStatus(200)
            ->assertJson([
                'status' => 'success',
            ])
            ->assertJsonStructure([
                'data' => [
                    'sudah_checkin',
                    'sudah_checkout',
                    'jam_checkin',
                    'jam_checkout',
                    'jam_checkin_standar',
                    'jam_checkout_standar',
                    'presensi',
                ],
            ]);
    }

    public function test_today_mengembalikan_waktu_standar_jika_belum_checkin()
    {
        // Act — tanpa checkin
        $response = $this->actingAs($this->user)
            ->getJson('/api/presensis/today');

        // Assert — jam_checkin harus default 07:30, jam_checkout null (belum checkin)
        $response->assertStatus(200)
            ->assertJson([
                'status' => 'success',
                'data' => [
                    'sudah_checkin' => false,
                    'sudah_checkout' => false,
                    'jam_checkin_standar' => '07:30:00',
                    'jam_checkout_standar' => '16:30:00',
                ],
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
