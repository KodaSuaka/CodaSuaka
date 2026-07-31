<?php

namespace Tests\Feature\Karyawan;

use App\Models\Instansi;
use App\Models\role;
use App\Models\role_permission;
use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

class KaryawanStoreRoleEscalationTest extends TestCase
{
    use RefreshDatabase;

    /**
     * Bug: StorekaryawanRequest only checks `exists:roles,id` for role_id.
     * A Manager (who has manage:karyawan permission) can create a new
     * karyawan/user with role_id = Owner's role, granting itself full
     * tenant-owner privileges (manage:instansi, approve:keuangan, etc.)
     * — a privilege escalation. Owner/Super Admin are supposed to be
     * platform-level roles excluded from the Karyawan CRUD entirely
     * (see KaryawanController::index()'s $excludedRoleNames).
     */
    public function test_manager_tidak_bisa_membuat_karyawan_dengan_role_owner()
    {
        $instansi = Instansi::factory()->create();

        $ownerRole = role::firstOrCreate(['nama_role' => 'Owner']);
        $managerRole = role::firstOrCreate(['nama_role' => 'Manager']);
        role_permission::firstOrCreate([
            'role_id' => $managerRole->id,
            'permission' => 'manage:karyawan',
        ]);

        $manager = User::factory()->create([
            'instansi_id' => $instansi->id,
            'role_id' => $managerRole->id,
        ]);

        $response = $this->actingAs($manager)->postJson('/api/karyawans', [
            'nama_lengkap' => 'Penyusup',
            'email' => 'penyusup@example.com',
            'password' => 'password123',
            'role_id' => $ownerRole->id,
        ]);

        $response->assertStatus(422);
        $response->assertJsonValidationErrors(['role_id']);

        $this->assertDatabaseMissing('users', [
            'email' => 'penyusup@example.com',
        ]);
    }
}
