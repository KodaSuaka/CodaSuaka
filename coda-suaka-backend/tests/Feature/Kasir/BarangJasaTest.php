<?php

namespace Tests\Feature\Kasir;

use App\Models\BarangJasa;
use App\Models\Instansi;
use App\Models\Nota;
use App\Models\NotaItem;
use App\Models\role;
use App\Models\role_permission;
use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

class BarangJasaTest extends TestCase
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

        role_permission::firstOrCreate(
            ['role_id' => $role->id, 'permission' => 'view:kasir'],
            ['created_at' => now(), 'updated_at' => now()]
        );
        role_permission::firstOrCreate(
            ['role_id' => $role->id, 'permission' => 'manage:kasir'],
            ['created_at' => now(), 'updated_at' => now()]
        );
        role_permission::firstOrCreate(
            ['role_id' => $role->id, 'permission' => 'delete:kasir'],
            ['created_at' => now(), 'updated_at' => now()]
        );

        $this->user = User::factory()->create([
            'instansi_id' => $this->instansi->id,
            'role_id' => $role->id,
            'email_verified_at' => now(),
        ]);
    }

    public function test_crud_dasar_barang_jasa()
    {
        // Create
        $payload = [
            'nama' => 'Kopi Susu',
            'jenis' => 'barang',
            'satuan' => 'gelas',
            'harga_jual' => 18000,
            'harga_beli' => 10000,
            'stok' => 50,
            'is_active' => true,
        ];

        $createResponse = $this->actingAs($this->user)
            ->postJson('/api/barang-jasas', $payload);

        $createResponse->assertStatus(201)
            ->assertJson([
                'status' => 'success',
                'data' => [
                    'nama' => 'Kopi Susu',
                    'jenis' => 'barang',
                ],
            ]);

        $barangJasaId = $createResponse->json('data.id');

        // Index — muncul
        $indexResponse = $this->actingAs($this->user)
            ->getJson('/api/barang-jasas');

        $indexResponse->assertStatus(200)
            ->assertJsonStructure([
                'status',
                'data',
                'meta' => ['current_page', 'last_page', 'per_page', 'total'],
            ]);
        $this->assertCount(1, $indexResponse->json('data'));

        // Update — berubah
        $updateResponse = $this->actingAs($this->user)
            ->putJson("/api/barang-jasas/{$barangJasaId}", [
                'harga_jual' => 20000,
            ]);

        $updateResponse->assertStatus(200)
            ->assertJsonPath('data.harga_jual', '20000.00');

        // Delete
        $deleteResponse = $this->actingAs($this->user)
            ->deleteJson("/api/barang-jasas/{$barangJasaId}");

        $deleteResponse->assertStatus(200);
        $this->assertDatabaseMissing('barang_jasas', ['id' => $barangJasaId]);
    }

    public function test_tenant_isolation_barang_jasa()
    {
        $instansiLain = Instansi::factory()->create();
        $barangJasaLain = BarangJasa::factory()->create([
            'instansi_id' => $instansiLain->id,
        ]);

        // Tidak muncul di index user ini
        $indexResponse = $this->actingAs($this->user)
            ->getJson('/api/barang-jasas');

        $indexResponse->assertStatus(200);
        $this->assertCount(0, $indexResponse->json('data'));

        // show() 404/403
        $showResponse = $this->actingAs($this->user)
            ->getJson("/api/barang-jasas/{$barangJasaLain->id}");

        $this->assertContains($showResponse->status(), [403, 404]);
    }

    public function test_destroy_diblok_jika_barang_jasa_dipakai_di_nota()
    {
        $barangJasa = BarangJasa::factory()->create([
            'instansi_id' => $this->instansi->id,
        ]);

        $nota = Nota::factory()->create(['instansi_id' => $this->instansi->id]);
        NotaItem::factory()->create([
            'barang_jasa_id' => $barangJasa->id,
            'nota_id' => $nota->id,
        ]);

        $deleteResponse = $this->actingAs($this->user)
            ->deleteJson("/api/barang-jasas/{$barangJasa->id}");

        $deleteResponse->assertStatus(422);
        $this->assertDatabaseHas('barang_jasas', ['id' => $barangJasa->id]);
    }
}
