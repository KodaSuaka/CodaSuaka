<?php

namespace Tests\Feature\Auth;

use App\Models\Instansi;
use App\Models\role;
use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Support\Facades\Hash;
use Tests\TestCase;

class LoginTest extends TestCase
{
    use RefreshDatabase;

    private Instansi $instansi;

    private role $role;

    protected function setUp(): void
    {
        parent::setUp();

        // Seed roles
        $this->role = role::firstOrCreate(
            ['nama_role' => 'Owner'],
            ['deskripsi' => 'Owner']
        );

        $this->instansi = Instansi::factory()->create();
    }

    public function test_user_dapat_login_dengan_email_dan_password_benar()
    {
        // Arrange
        $password = 'password123';
        $user = User::factory()->create([
            'email' => 'test@example.com',
            'password' => Hash::make($password),
            'instansi_id' => $this->instansi->id,
            'role_id' => $this->role->id,
            'email_verified_at' => now(),
        ]);

        // Act
        $response = $this->postJson('/api/login', [
            'email' => 'test@example.com',
            'password' => $password,
        ]);

        // Assert
        $response->assertStatus(200)
            ->assertJson([
                'status' => 'success',
            ])
            ->assertJsonStructure([
                'status',
                'data' => ['access_token', 'user' => ['id', 'email', 'role']],
            ]);

        // Pastikan token di-generate
        $this->assertNotEmpty($response->json('data.access_token'));
    }

    public function test_login_gagal_dengan_password_salah()
    {
        // Arrange
        User::factory()->create([
            'email' => 'test@example.com',
            'password' => Hash::make('correct_password'),
            'instansi_id' => $this->instansi->id,
            'role_id' => $this->role->id,
            'email_verified_at' => now(),
        ]);

        // Act
        $response = $this->postJson('/api/login', [
            'email' => 'test@example.com',
            'password' => 'wrong_password',
        ]);

        // Assert
        $response->assertStatus(401)
            ->assertJson([
                'status' => 'error',
            ]);
    }

    public function test_login_gagal_dengan_email_tidak_terdaftar()
    {
        // Act
        $response = $this->postJson('/api/login', [
            'email' => 'nonexistent@example.com',
            'password' => 'password',
        ]);

        // Assert
        $response->assertStatus(401);
    }

    public function test_login_gagal_dengan_validasi_gagal()
    {
        // Act — email kosong
        $response = $this->postJson('/api/login', [
            'email' => '',
            'password' => 'password',
        ]);

        // Assert
        $response->assertStatus(422)
            ->assertJsonValidationErrors(['email']);
    }

    public function test_user_dapat_logout()
    {
        // Arrange
        $user = User::factory()->create([
            'instansi_id' => $this->instansi->id,
            'role_id' => $this->role->id,
        ]);

        // Act
        $response = $this->actingAs($user)
            ->postJson('/api/logout');

        // Assert
        $response->assertStatus(200);
    }

    public function test_register_user_baru_berhasil()
    {
        // Act
        $response = $this->postJson('/api/register', [
            'nama_instansi' => 'Toko Test',
            'nama_pemilik' => 'Pemilik Test',
            'email' => 'newuser@example.com',
            'password' => 'password123',
        ]);

        // Assert
        $response->assertStatus(201)
            ->assertJson([
                'status' => 'success',
            ]);

        $this->assertDatabaseHas('users', [
            'email' => 'newuser@example.com',
            'name' => 'Pemilik Test',
        ]);
    }

    public function test_register_gagal_dengan_password_tidak_sama()
    {
        // Act — register dengan field yang valid tapi password kurang dari 8 karakter
        $response = $this->postJson('/api/register', [
            'nama_instansi' => 'Toko Test 2',
            'nama_pemilik' => 'Pemilik Test 2',
            'email' => 'newuser2@example.com',
            'password' => 'short',
        ]);

        // Assert — password minimal 8 karakter
        $response->assertStatus(422)
            ->assertJsonValidationErrors(['password']);
    }

    public function test_endpoint_user_mengembalikan_data_user_yang_login()
    {
        // Arrange
        $user = User::factory()->create([
            'instansi_id' => $this->instansi->id,
            'role_id' => $this->role->id,
        ]);

        // Act
        $response = $this->actingAs($user)
            ->getJson('/api/user');

        // Assert
        $response->assertStatus(200)
            ->assertJson([
                'status' => 'success',
                'data' => [
                    'email' => $user->email,
                ],
            ]);
    }
}
