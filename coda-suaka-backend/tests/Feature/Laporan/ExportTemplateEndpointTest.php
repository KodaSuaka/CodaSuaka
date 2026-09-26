<?php

namespace Tests\Feature\Laporan;

use App\Models\Instansi;
use App\Models\role;
use App\Models\role_permission;
use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

/**
 * Endpoint export template laporan keuangan. Gated izin 'export:laporan-keuangan'
 * (Owner + Keuangan). Role tanpa izin ditolak 403.
 */
class ExportTemplateEndpointTest extends TestCase
{
    use RefreshDatabase;

    private function user(string $namaRole, array $permissions): User
    {
        $instansi = Instansi::factory()->create();
        $role = role::firstOrCreate(['nama_role' => $namaRole], ['deskripsi' => $namaRole]);
        foreach ($permissions as $p) {
            role_permission::firstOrCreate(
                ['role_id' => $role->id, 'permission' => $p],
                ['created_at' => now(), 'updated_at' => now()]
            );
        }

        return User::factory()->create([
            'instansi_id' => $instansi->id,
            'role_id' => $role->id,
            'email_verified_at' => now(),
        ]);
    }

    private const URL = '/api/laporan-keuangan/template/export?jenis=laba_rugi&tipe_usaha=barang&bulan=7&tahun=2026';

    public function test_user_berizin_dapat_mengunduh_template()
    {
        $user = $this->user('Keuangan', ['export:laporan-keuangan']);

        $response = $this->actingAs($user)->get(self::URL);

        $response->assertStatus(200);
        $this->assertStringContainsString(
            'spreadsheetml',
            (string) $response->headers->get('content-type'),
            'Response harus berupa file .xlsx'
        );
    }

    public function test_user_tanpa_izin_ditolak()
    {
        $user = $this->user('Staff', []); // tanpa izin export:laporan-keuangan

        $response = $this->actingAs($user)->get(self::URL);

        $response->assertStatus(403);
    }

    public function test_parameter_tidak_valid_ditolak_422()
    {
        $user = $this->user('Keuangan', ['export:laporan-keuangan']);

        $response = $this->actingAs($user)
            ->getJson('/api/laporan-keuangan/template/export?jenis=ngawur&tipe_usaha=barang&bulan=7&tahun=2026');

        $response->assertStatus(422);
    }
}
