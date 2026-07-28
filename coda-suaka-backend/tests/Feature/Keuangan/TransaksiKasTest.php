<?php

namespace Tests\Feature\Keuangan;

use App\Models\Instansi;
use App\Models\KategoriTransaksi;
use App\Models\role;
use App\Models\role_permission;
use App\Models\TransaksiKas;
use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

class TransaksiKasTest extends TestCase
{
    use RefreshDatabase;

    private User $user;

    private Instansi $instansi;

    private KategoriTransaksi $kategori;

    protected function setUp(): void
    {
        parent::setUp();

        $this->instansi = Instansi::factory()->create();
        $role = role::firstOrCreate(
            ['nama_role' => 'Owner'],
            ['deskripsi' => 'Owner']
        );

        // Setup permission untuk Owner agar bisa akses keuangan
        role_permission::firstOrCreate(
            ['role_id' => $role->id, 'permission' => 'view:keuangan'],
            ['created_at' => now(), 'updated_at' => now()]
        );
        role_permission::firstOrCreate(
            ['role_id' => $role->id, 'permission' => 'manage:keuangan'],
            ['created_at' => now(), 'updated_at' => now()]
        );
        role_permission::firstOrCreate(
            ['role_id' => $role->id, 'permission' => 'delete:keuangan'],
            ['created_at' => now(), 'updated_at' => now()]
        );

        $this->user = User::factory()->create([
            'instansi_id' => $this->instansi->id,
            'role_id' => $role->id,
            'email_verified_at' => now(),
        ]);
        $this->kategori = KategoriTransaksi::factory()
            ->pemasukan()
            ->create(['instansi_id' => $this->instansi->id]);
    }

    public function test_user_dapat_melihat_daftar_transaksi_kas()
    {
        // Arrange
        TransaksiKas::factory()
            ->pemasukan()
            ->count(3)
            ->create(['instansi_id' => $this->instansi->id]);

        // Act
        $response = $this->actingAs($this->user)
            ->getJson('/api/transaksi-kas');

        // Assert
        $response->assertStatus(200)
            ->assertJsonStructure([
                'status',
                'data',
                'meta' => ['current_page', 'last_page', 'per_page', 'total'],
            ]);
    }

    public function test_user_dapat_membuat_entri_kas_baru()
    {
        // Arrange
        $payload = [
            'tanggal' => '2026-07-01',
            'tipe' => 'masuk',
            'nominal' => 500000,
            'kategori_transaksi_id' => $this->kategori->id,
            'metode_pembayaran' => 'Tunai',
            'keterangan' => 'Penjualan harian',
        ];

        // Act
        $response = $this->actingAs($this->user)
            ->postJson('/api/transaksi-kas', $payload);

        // Assert
        $response->assertStatus(201)
            ->assertJson([
                'status' => 'success',
                'data' => [
                    'tipe' => 'masuk',
                    'nominal' => 500000,
                ],
            ]);
    }

    /**
     * Regresi: before_or_equal:today pakai Carbon::parse('today') = tengah
     * malam, tapi frontend mengirim tanggal hari ini lengkap dengan jam
     * (mis. dari date-time picker) — sebelumnya selalu gagal validasi
     * kecuali persis jam 00:00.
     */
    public function test_user_dapat_membuat_entri_kas_dengan_tanggal_hari_ini_dan_jam()
    {
        $payload = [
            'tanggal' => now()->format('Y-m-d\TH:i:sP'),
            'tipe' => 'masuk',
            'nominal' => 250000,
            'kategori_transaksi_id' => $this->kategori->id,
            'metode_pembayaran' => 'Tunai',
            'keterangan' => 'Transaksi hari ini',
        ];

        $response = $this->actingAs($this->user)
            ->postJson('/api/transaksi-kas', $payload);

        $response->assertStatus(201)
            ->assertJson(['status' => 'success']);
    }

    public function test_validasi_gagal_saat_nominal_negatif()
    {
        // Arrange
        $payload = [
            'tanggal' => '2026-07-01',
            'tipe' => 'masuk',
            'nominal' => -10000,
            'kategori_transaksi_id' => $this->kategori->id,
        ];

        // Act
        $response = $this->actingAs($this->user)
            ->postJson('/api/transaksi-kas', $payload);

        // Assert
        $response->assertStatus(422);
    }

    public function test_user_tidak_bisa_melihat_transaksi_instansi_lain()
    {
        // Arrange
        $instansiLain = Instansi::factory()->create();
        TransaksiKas::factory()->create([
            'instansi_id' => $instansiLain->id,
        ]);

        // Act
        $response = $this->actingAs($this->user)
            ->getJson('/api/transaksi-kas');

        // Assert — TenantScope memastikan data instansi lain tidak muncul
        $response->assertStatus(200);
        $this->assertCount(0, $response->json('data'));
    }
}
