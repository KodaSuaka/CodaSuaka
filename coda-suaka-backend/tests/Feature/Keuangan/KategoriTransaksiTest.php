<?php

namespace Tests\Feature\Keuangan;

use App\Models\User;
use App\Models\instansi;
use App\Models\KategoriTransaksi;
use App\Models\role;
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
        $role = role::factory()->create(['nama_role' => 'Owner']);
        $this->user = User::factory()->create([
            'instansi_id' => $this->instansi->id,
            'role_id' => $role->id,
        ]);
    }

    public function test_user_dapat_melihat_daftar_kategori_transaksi()
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

    public function test_user_dapat_melihat_kategori_global()
    {
        // Arrange — buat kategori template global (instansi_id = null)
        $globalKategori = KategoriTransaksi::factory()
            ->global()
            ->pemasukan()
            ->create(['nama_kategori' => 'Global Kategori Test']);

        // Act
        $response = $this->actingAs($this->user)
            ->getJson('/api/kategori-transaksis');

        // Assert — kategori global harus muncul
        $response->assertStatus(200);
        $data = $response->json('data');
        $this->assertNotEmpty($data);
        $this->assertTrue(collect($data)->contains('nama_kategori', 'Global Kategori Test'));
    }

    public function test_user_dapat_membuat_kategori_transaksi_baru()
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

    public function test_user_tidak_bisa_melihat_kategori_instansi_lain()
    {
        // Arrange
        $instansiLain = instansi::factory()->create();
        $kategoriLain = KategoriTransaksi::factory()->create([
            'instansi_id' => $instansiLain->id,
        ]);

        // Act
        $response = $this->actingAs($this->user)
            ->getJson('/api/kategori-transaksis');

        // Assert — forInstansi scope memastikan data instansi lain tidak muncul
        $response->assertStatus(200);
        $data = $response->json('data');
        $this->assertCount(0, $data);
    }

    public function test_user_tidak_bisa_mengedit_kategori_global()
    {
        // Arrange
        $globalKategori = KategoriTransaksi::factory()
            ->global()
            ->create(['nama_kategori' => 'Global Kategori']);

        // Act
        $response = $this->actingAs($this->user)
            ->putJson("/api/kategori-transaksis/{$globalKategori->id}", [
                'nama_kategori' => 'Ganti Nama',
            ]);

        // Assert — kategori global tidak bisa diedit oleh user biasa
        $response->assertStatus(422)
            ->assertJson([
                'status' => 'error',
                'message' => 'Kategori template global tidak dapat diedit',
            ]);
    }

    public function test_user_tidak_bisa_menghapus_kategori_global()
    {
        // Arrange
        $globalKategori = KategoriTransaksi::factory()
            ->global()
            ->create(['nama_kategori' => 'Global Kategori']);

        // Act
        $response = $this->actingAs($this->user)
            ->deleteJson("/api/kategori-transaksis/{$globalKategori->id}");

        // Assert — kategori global tidak bisa dihapus oleh user biasa
        $response->assertStatus(422)
            ->assertJson([
                'status' => 'error',
                'message' => 'Kategori template global tidak dapat dihapus',
            ]);
    }

    public function test_user_bisa_mengedit_kategori_custom_sendiri()
    {
        // Arrange
        $kategori = KategoriTransaksi::factory()
            ->create(['instansi_id' => $this->instansi->id]);

        // Act
        $response = $this->actingAs($this->user)
            ->putJson("/api/kategori-transaksis/{$kategori->id}", [
                'nama_kategori' => 'Nama Baru',
            ]);

        // Assert
        $response->assertStatus(200);
        $this->assertEquals('Nama Baru', $response->json('data.nama_kategori'));
    }

    public function test_validasi_gagal_saat_tipe_tidak_valid()
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
