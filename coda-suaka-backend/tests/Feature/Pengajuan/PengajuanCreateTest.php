<?php

namespace Tests\Feature\Pengajuan;

use App\Models\Instansi;
use App\Models\karyawan;
use App\Models\role;
use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

class PengajuanCreateTest extends TestCase
{
    use RefreshDatabase;

    /**
     * Regresi: NotificationService::createForInstansi() dulu bertipe
     * int $instansiId, padahal instansis.id adalah UUID — TypeError 500
     * di setiap pembuatan pengajuan (cuti_tahunan, izin_sakit, mendadak).
     */
    public function test_karyawan_dapat_mengajukan_cuti_tahunan_tanpa_error_500()
    {
        $instansi = Instansi::factory()->create();
        $karyawanRole = role::firstOrCreate(['nama_role' => 'Karyawan'], ['deskripsi' => 'Karyawan']);
        $user = User::factory()->create([
            'instansi_id' => $instansi->id,
            'role_id' => $karyawanRole->id,
            'email_verified_at' => now(),
        ]);
        karyawan::create([
            'user_id' => $user->id,
            'nama_lengkap' => 'Karyawan Test',
            'sisa_cuti' => 12,
        ]);

        $response = $this->actingAs($user)
            ->postJson('/api/pengajuans', [
                'jenis' => 'cuti_tahunan',
                'tanggal_mulai' => now()->addDays(3)->toDateString(),
                'tanggal_selesai' => now()->addDays(4)->toDateString(),
                'keterangan' => 'Cuti tahunan test',
            ]);

        $response->assertStatus(201)
            ->assertJson(['status' => 'success']);

        $this->assertDatabaseHas('pengajuans', [
            'user_id' => $user->id,
            'jenis' => 'cuti_tahunan',
            'status' => 'pending',
        ]);
    }
}
