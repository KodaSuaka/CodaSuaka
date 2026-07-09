<?php

namespace Tests\Feature\Keuangan;

use App\Models\User;
use App\Models\instansi;
use App\Models\KategoriTransaksi;
use App\Models\Role;
use Database\Factories\KategoriTransaksiFactory;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

class KategoriTransaksiTest extends TestCase
{
    use RefreshDatabase;

    private User $user;
    private instansi $instansi;

    protected function setUp(): void
    {
        parent::setUp();

        // Buat data test minimal
        $this->instansi = instansi::factory()->create();
        $role = Role::factory()->create(['name' => 'Owner']);
        $this->user = User::factory()->create([
            'instansi_id' => $this->instansi->id,
            'role_id' => $role->id,
        ]);
    }

    /** @test */
    public function user_dapat_melihat_daftar_kategori_transaksi()
    {
        // Arrange
        $kategori = KategoriTransaksi::factory()
            ->pemasukan()
            ->create(['instansi_id' => $this->instansi->id]);

        // Act
        $response = $this->actingAs($this->user)
            ->getJson('/api/kategori-transaksis');

        // Assert
        $response->assertStatus(200)
            ->assertJsonStructure([
                'status',
                'data' => [
                    '*' => ['id', 'nama_kategori', 'tipe', 'sifat', 'termasuk_hpp', 'is_active'],
                ],
            ]);
    }

    /** @test */
    public function user_dapat_membuat_kategori_transaksi_baru()
    {
        // Arrange
        $payload = [
            'nama_kategori' => 'Penjualan Produk',
            'tipe' => 'masuk',
            'sifat' => 'operasional',
            'termasuk_hpp' => false,
        ];

        // Act
        $response = $this->actingAs($this->user)
            ->postJson('/api/kategori-transaksis', $payload);

        // Assert
        $response->assertStatus(201)
            ->assertJson([
                'status' => 'success',
                'data' => [
                    'nama_kategori' => 'Penjualan Produk',
                    'tipe' => 'masuk',
                    'instansi_id' => $this->instansi->id,
                ],
            ]);
    }

    /** @test */
    public function user_tidak_bisa_melihat_kategori_instansi_lain()
    {
        // Arrange
        $instansiLain = instansi::factory()->create();
        $kategoriLain = KategoriTransaksi::factory()->create([
            'instansi_id' => $instansiLain->id,
        ]);

        // Act
        $response = $this->actingAs($this->user)
            ->getJson('/api/kategori-transaksis');

        // Assert — TenantScope memastikan data instansi lain tidak muncul
        $response->assertStatus(200);
        $data = $response->json('data');
        $this->assertCount(0, $data);
    }

    /** @test */
    public function validasi_gagal_saat_tipe_tidak_valid()
    {
        // Arrange
        $payload = [
            'nama_kategori' => 'Test',
            'tipe' => 'invalid_type',
            'sifat' => 'operasional',
        ];

        // Act
        $response = $this->actingAs($this->user)
            ->postJson('/api/kategori-transaksis', $payload);

        // Assert
        $response->assertStatus(422)
            ->assertJson([
                'status' => 'error',
                'message' => 'Validasi gagal',
            ]);
    }
}
