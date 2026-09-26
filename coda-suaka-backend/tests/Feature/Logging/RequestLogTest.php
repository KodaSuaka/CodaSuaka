<?php

namespace Tests\Feature\Logging;

use App\Models\Instansi;
use App\Models\RequestLog;
use App\Models\role;
use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

class RequestLogTest extends TestCase
{
    use RefreshDatabase;

    private User $superAdmin;

    private Instansi $instansi;

    private User $owner;

    protected function setUp(): void
    {
        parent::setUp();

        $this->instansi = Instansi::factory()->create();

        $superAdminRole = role::firstOrCreate(
            ['nama_role' => 'Super Admin'],
            ['deskripsi' => 'Super Admin']
        );

        $ownerRole = role::firstOrCreate(
            ['nama_role' => 'Owner'],
            ['deskripsi' => 'Owner']
        );

        $this->superAdmin = User::factory()->create([
            'role_id' => $superAdminRole->id,
            'email_verified_at' => now(),
        ]);

        $this->owner = User::factory()->create([
            'instansi_id' => $this->instansi->id,
            'role_id' => $ownerRole->id,
            'email_verified_at' => now(),
        ]);
    }

    public function test_middleware_mencatat_request_api(): void
    {
        // Request login biasa — harus tercatat di request_logs
        $this->actingAs($this->owner)
            ->getJson('/api/dashboard/omset')
            ->assertStatus(200);

        $this->assertDatabaseHas('request_logs', [
            'user_id' => $this->owner->id,
            'instansi_id' => $this->instansi->id,
            'method' => 'GET',
            'path' => 'api/dashboard/omset',
            'status_code' => 200,
        ]);
    }

    public function test_middleware_menyensor_password_di_request_body(): void
    {
        $this->actingAs($this->owner)
            ->postJson('/api/transaksi-kas', [
                'kategori_id' => 1,
                'tipe' => 'masuk',
                'nominal' => 50000,
                'password' => 'rahasia123',
            ]);

        $log = RequestLog::where('user_id', $this->owner->id)
            ->where('method', 'POST')
            ->latest()
            ->first();

        $this->assertNotNull($log);
        $this->assertSame('***', $log->request_body['password'] ?? null);
    }

    public function test_super_admin_melihat_daftar_request_logs(): void
    {
        RequestLog::factory()->count(3)->create([
            'instansi_id' => $this->instansi->id,
            'user_id' => $this->owner->id,
        ]);

        $response = $this->actingAs($this->superAdmin)
            ->getJson('/api/super-admin/request-logs');

        $response->assertStatus(200)
            ->assertJsonStructure([
                'status',
                'data',
                'meta' => ['current_page', 'last_page', 'per_page', 'total'],
            ]);

        $this->assertCount(3, $response->json('data'));
    }

    public function test_super_admin_filter_request_logs(): void
    {
        RequestLog::factory()->create([
            'instansi_id' => $this->instansi->id,
            'user_id' => $this->owner->id,
            'method' => 'GET',
            'path' => 'api/dashboard/omset',
            'status_code' => 200,
        ]);

        RequestLog::factory()->create([
            'instansi_id' => $this->instansi->id,
            'user_id' => $this->owner->id,
            'method' => 'POST',
            'path' => 'api/transaksi-kas',
            'status_code' => 422,
        ]);

        $response = $this->actingAs($this->superAdmin)
            ->getJson('/api/super-admin/request-logs?method=GET&status_code=200');

        $response->assertStatus(200)
            ->assertJsonCount(1, 'data');
        $this->assertSame('api/dashboard/omset', $response->json('data.0.path'));
    }

    public function test_super_admin_melihat_detail_dan_menghapus_log(): void
    {
        $log = RequestLog::factory()->create([
            'instansi_id' => $this->instansi->id,
            'user_id' => $this->owner->id,
            'method' => 'POST',
            'path' => 'api/transaksi-kas',
            'request_body' => ['nominal' => 50000],
        ]);

        // Detail
        $this->actingAs($this->superAdmin)
            ->getJson("/api/super-admin/request-logs/{$log->id}")
            ->assertStatus(200)
            ->assertJsonPath('data.path', 'api/transaksi-kas');

        // Hapus
        $this->actingAs($this->superAdmin)
            ->deleteJson("/api/super-admin/request-logs/{$log->id}")
            ->assertStatus(200);

        $this->assertDatabaseMissing('request_logs', ['id' => $log->id]);
    }

    public function test_non_super_admin_ditolak_akses_request_logs(): void
    {
        $this->actingAs($this->owner)
            ->getJson('/api/super-admin/request-logs')
            ->assertStatus(403);
    }

    public function test_path_request_logs_tidak_dicatat_oleh_middleware(): void
    {
        // Akses endpoint log — path request-logs harus di-skip dari logging
        $this->actingAs($this->superAdmin)
            ->getJson('/api/super-admin/request-logs')
            ->assertStatus(200);

        $this->assertDatabaseMissing('request_logs', [
            'path' => 'api/super-admin/request-logs',
        ]);
    }
}
