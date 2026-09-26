<?php

namespace Tests\Feature\Karyawan;

use App\Models\Instansi;
use App\Models\role;
use App\Models\role_permission;
use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

/**
 * Biodata karyawan opsional: tempat_lahir, tanggal_lahir (alamat sudah ada).
 * lama_bekerja BUKAN input — dihitung dari tanggal_mulai_kerja.
 */
class KaryawanBiodataTest extends TestCase
{
    use RefreshDatabase;

    private function owner(): User
    {
        $instansi = Instansi::factory()->create();
        $ownerRole = role::firstOrCreate(['nama_role' => 'Owner']);
        role_permission::firstOrCreate(['role_id' => $ownerRole->id, 'permission' => 'manage:karyawan']);

        return User::factory()->create([
            'instansi_id' => $instansi->id,
            'role_id' => $ownerRole->id,
            'email_verified_at' => now(),
        ]);
    }

    public function test_karyawan_dibuat_dengan_biodata_dan_lama_bekerja_dihitung()
    {
        $owner = $this->owner();
        $staffRole = role::firstOrCreate(['nama_role' => 'Staff']);

        $response = $this->actingAs($owner)->postJson('/api/karyawans', [
            'nama_lengkap' => 'Budi Santoso',
            'email' => 'budi@example.com',
            'password' => 'password123',
            'role_id' => $staffRole->id,
            'tempat_lahir' => 'Bandung',
            'tanggal_lahir' => '1995-05-20',
            'alamat' => 'Jl. Merdeka 1',
            'tanggal_mulai_kerja' => now()->subYears(2)->subMonths(3)->toDateString(),
        ]);

        $response->assertStatus(201);
        $this->assertDatabaseHas('karyawans', [
            'tempat_lahir' => 'Bandung',
            'tanggal_lahir' => '1995-05-20',
        ]);
        $this->assertSame('Bandung', $response->json('data.tempat_lahir'));
        // lama_bekerja diturunkan (~2 tahun 3 bulan), bukan input.
        $this->assertStringContainsString('2 tahun', (string) $response->json('data.lama_bekerja'));
    }

    public function test_biodata_opsional_boleh_kosong()
    {
        $owner = $this->owner();
        $staffRole = role::firstOrCreate(['nama_role' => 'Staff']);

        $response = $this->actingAs($owner)->postJson('/api/karyawans', [
            'nama_lengkap' => 'Tanpa Biodata',
            'email' => 'tanpa@example.com',
            'password' => 'password123',
            'role_id' => $staffRole->id,
        ]);

        $response->assertStatus(201);
        $this->assertNull($response->json('data.tempat_lahir'));
        $this->assertNull($response->json('data.tanggal_lahir'));
        // Tanpa tanggal_mulai_kerja, lama_bekerja tak terdefinisi.
        $this->assertNull($response->json('data.lama_bekerja'));
    }
}
