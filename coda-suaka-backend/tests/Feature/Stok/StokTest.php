<?php

namespace Tests\Feature\Stok;

use App\Models\Instansi;
use App\Models\Notification;
use App\Models\role;
use App\Models\role_permission;
use App\Models\Stok;
use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

class StokTest extends TestCase
{
    use RefreshDatabase;

    private User $user;

    private Instansi $instansi;

    protected function setUp(): void
    {
        parent::setUp();

        $this->instansi = Instansi::factory()->create();
        $role = role::firstOrCreate(
            ['nama_role' => 'Owner'],
            ['deskripsi' => 'Owner']
        );

        foreach (['view:stok', 'manage:stok'] as $permission) {
            role_permission::firstOrCreate(
                ['role_id' => $role->id, 'permission' => $permission],
                ['created_at' => now(), 'updated_at' => now()]
            );
        }

        $this->user = User::factory()->create([
            'instansi_id' => $this->instansi->id,
            'role_id' => $role->id,
            'email_verified_at' => now(),
        ]);
    }

    public function test_crud_dasar_stok(): void
    {
        // Create
        $payload = [
            'nama' => 'Minyak Kelapa',
            'kategori' => 'Bahan Baku',
            'satuan' => 'liter',
            'stok' => 20,
            'stok_minimum' => 5,
            'harga_beli' => 25000,
            'is_active' => true,
            'keterangan' => 'Untuk perawatan rambut',
        ];

        $createResponse = $this->actingAs($this->user)
            ->postJson('/api/stoks', $payload);

        $createResponse->assertStatus(201)
            ->assertJson([
                'status' => 'success',
                'data' => [
                    'nama' => 'Minyak Kelapa',
                    'kategori' => 'Bahan Baku',
                ],
            ]);

        $stokId = $createResponse->json('data.id');

        // Index — muncul
        $indexResponse = $this->actingAs($this->user)
            ->getJson('/api/stoks');

        $indexResponse->assertStatus(200)
            ->assertJsonStructure([
                'status',
                'data',
                'meta' => ['current_page', 'last_page', 'per_page', 'total'],
            ]);
        $this->assertCount(1, $indexResponse->json('data'));

        // Update — kolom stok tidak bisa diubah lewat update (audit trail via mutasi)
        $updateResponse = $this->actingAs($this->user)
            ->putJson("/api/stoks/{$stokId}", [
                'nama' => 'Minyak Kelapa Murni',
                'stok' => 999,
            ]);

        $updateResponse->assertStatus(200)
            ->assertJsonPath('data.nama', 'Minyak Kelapa Murni')
            ->assertJsonPath('data.stok', '20.00');

        // Delete — tanpa riwayat mutasi, bisa
        $deleteResponse = $this->actingAs($this->user)
            ->deleteJson("/api/stoks/{$stokId}");

        $deleteResponse->assertStatus(200);
        $this->assertDatabaseMissing('stoks', ['id' => $stokId]);
    }

    public function test_mutasi_masuk_dan_keluar(): void
    {
        $stok = Stok::factory()->create([
            'instansi_id' => $this->instansi->id,
            'stok' => 10,
        ]);

        // Masuk +5 → 15
        $masukResponse = $this->actingAs($this->user)
            ->postJson("/api/stoks/{$stok->id}/mutasi", [
                'jenis' => 'masuk',
                'jumlah' => 5,
                'keterangan' => 'Beli dari supplier',
            ]);

        $masukResponse->assertStatus(200)
            ->assertJsonPath('data.stok_sebelum', '10.00')
            ->assertJsonPath('data.stok_sesudah', '15.00')
            ->assertJsonPath('data.jenis', 'masuk');

        // Keluar -7 → 8
        $keluarResponse = $this->actingAs($this->user)
            ->postJson("/api/stoks/{$stok->id}/mutasi", [
                'jenis' => 'keluar',
                'jumlah' => 7,
            ]);

        $keluarResponse->assertStatus(200)
            ->assertJsonPath('data.stok_sebelum', '15.00')
            ->assertJsonPath('data.stok_sesudah', '8.00');

        $this->assertDatabaseHas('stoks', ['id' => $stok->id, 'stok' => 8]);
        $this->assertDatabaseCount('stok_mutations', 2);
    }

    public function test_mutasi_penyesuaian_set_stok(): void
    {
        $stok = Stok::factory()->create([
            'instansi_id' => $this->instansi->id,
            'stok' => 50,
        ]);

        $response = $this->actingAs($this->user)
            ->postJson("/api/stoks/{$stok->id}/mutasi", [
                'jenis' => 'penyesuaian',
                'jumlah' => 30,
                'keterangan' => 'Stock opname',
            ]);

        $response->assertStatus(200)
            ->assertJsonPath('data.stok_sesudah', '30.00');
    }

    public function test_mutasi_keluar_melebihi_stok_ditolak_422(): void
    {
        $stok = Stok::factory()->create([
            'instansi_id' => $this->instansi->id,
            'stok' => 3,
        ]);

        $response = $this->actingAs($this->user)
            ->postJson("/api/stoks/{$stok->id}/mutasi", [
                'jenis' => 'keluar',
                'jumlah' => 10,
            ]);

        $response->assertStatus(422)
            ->assertJsonPath('status', 'error');

        // Stok tidak berubah, tidak ada mutasi tercatat
        $this->assertDatabaseHas('stoks', ['id' => $stok->id, 'stok' => 3]);
        $this->assertDatabaseCount('stok_mutations', 0);
    }

    public function test_riwayat_mutasi_hanya_milik_stok_itu(): void
    {
        $stok = Stok::factory()->create([
            'instansi_id' => $this->instansi->id,
            'stok' => 0,
        ]);
        $stokLain = Stok::factory()->create([
            'instansi_id' => $this->instansi->id,
            'stok' => 0,
        ]);

        $this->actingAs($this->user)
            ->postJson("/api/stoks/{$stok->id}/mutasi", [
                'jenis' => 'masuk',
                'jumlah' => 10,
            ]);
        $this->actingAs($this->user)
            ->postJson("/api/stoks/{$stokLain->id}/mutasi", [
                'jenis' => 'masuk',
                'jumlah' => 99,
            ]);

        $response = $this->actingAs($this->user)
            ->getJson("/api/stoks/{$stok->id}/riwayat");

        $response->assertStatus(200);
        $this->assertCount(1, $response->json('data'));
        $this->assertEquals('10.00', $response->json('data.0.jumlah'));
    }

    public function test_destroy_diblok_jika_ada_riwayat_mutasi(): void
    {
        $stok = Stok::factory()->create([
            'instansi_id' => $this->instansi->id,
            'stok' => 0,
        ]);

        $this->actingAs($this->user)
            ->postJson("/api/stoks/{$stok->id}/mutasi", [
                'jenis' => 'masuk',
                'jumlah' => 5,
            ]);

        $deleteResponse = $this->actingAs($this->user)
            ->deleteJson("/api/stoks/{$stok->id}");

        $deleteResponse->assertStatus(422);
        $this->assertDatabaseHas('stoks', ['id' => $stok->id]);
    }

    public function test_tenant_isolation_stok(): void
    {
        $instansiLain = Instansi::factory()->create();
        $stokLain = Stok::factory()->create([
            'instansi_id' => $instansiLain->id,
        ]);

        // Tidak muncul di index user ini
        $indexResponse = $this->actingAs($this->user)
            ->getJson('/api/stoks');

        $indexResponse->assertStatus(200);
        $this->assertCount(0, $indexResponse->json('data'));

        // show() 403/404
        $showResponse = $this->actingAs($this->user)
            ->getJson("/api/stoks/{$stokLain->id}");

        $this->assertContains($showResponse->status(), [403, 404]);

        // mutasi() diblok juga
        $mutasiResponse = $this->actingAs($this->user)
            ->postJson("/api/stoks/{$stokLain->id}/mutasi", [
                'jenis' => 'masuk',
                'jumlah' => 5,
            ]);

        $this->assertContains($mutasiResponse->status(), [403, 404]);
    }

    public function test_notifikasi_stok_minimum_ke_owner(): void
    {
        $stok = Stok::factory()->create([
            'instansi_id' => $this->instansi->id,
            'stok' => 0,
            'stok_minimum' => 10,
        ]);

        // Masuk 5 → stok 5 ≤ minimum 10 → notif
        $this->actingAs($this->user)
            ->postJson("/api/stoks/{$stok->id}/mutasi", [
                'jenis' => 'masuk',
                'jumlah' => 5,
            ]);

        $this->assertDatabaseHas('notifications', [
            'user_id' => $this->user->id,
            'type' => 'stok_minimum',
            'related_id' => $stok->id,
        ]);
    }

    public function test_tanpa_notifikasi_jika_stok_di_atas_minimum(): void
    {
        $stok = Stok::factory()->create([
            'instansi_id' => $this->instansi->id,
            'stok' => 50,
            'stok_minimum' => 10,
        ]);

        $this->actingAs($this->user)
            ->postJson("/api/stoks/{$stok->id}/mutasi", [
                'jenis' => 'keluar',
                'jumlah' => 5,
            ]);

        $this->assertDatabaseMissing('notifications', [
            'user_id' => $this->user->id,
            'type' => 'stok_minimum',
        ]);
    }
}
