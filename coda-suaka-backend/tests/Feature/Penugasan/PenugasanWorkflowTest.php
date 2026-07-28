<?php

namespace Tests\Feature\Penugasan;

use App\Models\Divisi;
use App\Models\Instansi;
use App\Models\karyawan;
use App\Models\outlet;
use App\Models\penugasan;
use App\Models\role;
use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Support\Facades\DB;
use Tests\TestCase;

class PenugasanWorkflowTest extends TestCase
{
    use RefreshDatabase;

    private Instansi $instansi;

    private outlet $outlet;

    private Divisi $divisi;

    private role $ownerRole;

    private role $karyawanRole;

    private User $ownerUser;

    private User $karyawanUser;

    private karyawan $karyawan;

    protected function setUp(): void
    {
        parent::setUp();

        // ── Setup Instansi ──
        $this->instansi = Instansi::factory()->create();

        // ── Setup Outlet ──
        $this->outlet = outlet::factory()->create([
            'instansi_id' => $this->instansi->id,
        ]);

        // ── Setup Roles ──
        $this->ownerRole = role::firstOrCreate(
            ['nama_role' => 'Owner'],
            ['deskripsi' => 'Owner']
        );

        $this->karyawanRole = role::firstOrCreate(
            ['nama_role' => 'Karyawan'],
            ['deskripsi' => 'Karyawan']
        );

        // ── Setup Owner User ──
        $this->ownerUser = User::factory()->create([
            'instansi_id' => $this->instansi->id,
            'role_id' => $this->ownerRole->id,
            'outlet_id' => $this->outlet->id,
            'email_verified_at' => now(),
        ]);

        // ── Setup Karyawan User ──
        $this->karyawanUser = User::factory()->create([
            'instansi_id' => $this->instansi->id,
            'role_id' => $this->karyawanRole->id,
            'outlet_id' => $this->outlet->id,
            'email_verified_at' => now(),
        ]);

        // ── Setup Karyawan Profile ──
        $this->karyawan = karyawan::create([
            'user_id' => $this->karyawanUser->id,
            'nama_lengkap' => 'Test Karyawan',
            'alamat' => 'Alamat Test',
            'outlet_id' => $this->outlet->id,
            'sisa_cuti' => 12,
        ]);

        // ── Setup Divisi ──
        $this->divisi = Divisi::create([
            'nama_divisi' => 'Operasional',
            'outlet_id' => $this->outlet->id,
        ]);

        // ── Setup Permissions for Owner ──
        \DB::table('role_permissions')->insert([
            'role_id' => $this->ownerRole->id,
            'permission' => 'manage:penugasan',
        ]);
    }

    /**
     * Helper: buat penugasan dengan status tertentu.
     */
    private function createPenugasan(string $status = 'belum', ?int $createdBy = null): penugasan
    {
        return penugasan::create([
            'judul' => 'Tugas Test',
            'deskripsi' => 'Deskripsi tugas test',
            'penanggung_jawab_id' => $this->karyawan->id,
            'divisi_id' => $this->divisi->id,
            'tenggat' => now()->addDays(7),
            'status' => $status,
            'urgency' => 'sedang',
            'created_by' => $createdBy ?? $this->ownerUser->id,
            'instansi_id' => $this->instansi->id,
        ]);
    }

    // ════════════════════════════════════════════════════════════
    //  ACCEPT WORKFLOW TESTS
    // ════════════════════════════════════════════════════════════

    public function test_karyawan_dapat_menerima_tugas_dengan_status_belum()
    {
        // Arrange
        $penugasan = $this->createPenugasan('belum');

        // Act
        $response = $this->actingAs($this->karyawanUser)
            ->putJson("/api/penugasans/{$penugasan->id}/accept");

        // Assert
        $response->assertStatus(200)
            ->assertJson([
                'status' => 'success',
                'message' => 'Tugas berhasil diterima',
            ]);

        // Verifikasi status berubah
        $this->assertDatabaseHas('penugasans', [
            'id' => $penugasan->id,
            'status' => 'proses',
            'status_changed_by' => $this->karyawanUser->id,
        ]);

        // Verifikasi accepted_at terisi
        $this->assertNotNull(
            $penugasan->fresh()->accepted_at,
            'accepted_at harus terisi setelah menerima tugas'
        );
    }

    public function test_karyawan_tidak_dapat_menerima_tugas_dengan_status_proses()
    {
        // Arrange
        $penugasan = $this->createPenugasan('proses');

        // Act
        $response = $this->actingAs($this->karyawanUser)
            ->putJson("/api/penugasans/{$penugasan->id}/accept");

        // Assert
        $response->assertStatus(422)
            ->assertJson([
                'status' => 'error',
            ]);
    }

    public function test_karyawan_tidak_dapat_menerima_tugas_dengan_status_selesai()
    {
        // Arrange
        $penugasan = $this->createPenugasan('selesai');

        // Act
        $response = $this->actingAs($this->karyawanUser)
            ->putJson("/api/penugasans/{$penugasan->id}/accept");

        // Assert
        $response->assertStatus(422);
    }

    public function test_karyawan_tidak_dapat_menerima_tugas_orang_lain()
    {
        // Arrange: buat karyawan lain
        $otherUser = User::factory()->create([
            'instansi_id' => $this->instansi->id,
            'role_id' => $this->karyawanRole->id,
        ]);
        $otherKaryawan = karyawan::create([
            'user_id' => $otherUser->id,
            'nama_lengkap' => 'Karyawan Lain',
            'outlet_id' => $this->outlet->id,
        ]);

        // Buat tugas untuk karyawan lain
        $penugasan = penugasan::create([
            'judul' => 'Tugas Orang Lain',
            'penanggung_jawab_id' => $otherKaryawan->id,
            'status' => 'belum',
            'urgency' => 'sedang',
            'created_by' => $this->ownerUser->id,
            'instansi_id' => $this->instansi->id,
        ]);

        // Act: karyawan pertama coba menerima tugas orang lain
        $response = $this->actingAs($this->karyawanUser)
            ->putJson("/api/penugasans/{$penugasan->id}/accept");

        // Assert
        $response->assertStatus(403);
    }

    public function test_karyawan_dapat_menerima_template_tugas()
    {
        // Arrange: buat template tugas global
        $template = penugasan::create([
            'judul' => 'Template Bersihkan Etalase',
            'deskripsi' => 'Bersihkan etalase toko',
            'status' => 'belum',
            'urgency' => 'sedang',
            'created_by' => $this->ownerUser->id,
            'is_template' => true,
            'instansi_id' => null, // template global
        ]);

        // Act
        $response = $this->actingAs($this->karyawanUser)
            ->putJson("/api/penugasans/{$template->id}/accept");

        // Assert
        $response->assertStatus(200)
            ->assertJson([
                'status' => 'success',
                'message' => 'Tugas berhasil diterima',
            ]);

        // Template TIDAK berubah (masih status belum, is_template=true)
        $this->assertDatabaseHas('penugasans', [
            'id' => $template->id,
            'status' => 'belum',
            'is_template' => true,
        ]);

        // Task BARU dibuat dari template (is_template=false, template_penugasan_id terisi)
        $this->assertDatabaseHas('penugasans', [
            'judul' => 'Template Bersihkan Etalase',
            'is_template' => false,
            'status' => 'proses',
            'template_penugasan_id' => $template->id,
            'penanggung_jawab_id' => $this->karyawan->id,
        ]);
    }

    public function test_template_tetap_ada_setelah_diterima_banyak_karyawan()
    {
        // Arrange: buat template
        $template = penugasan::create([
            'judul' => 'Template Stock Opname',
            'deskripsi' => 'Lakukan stock opname bulanan',
            'status' => 'belum',
            'urgency' => 'urgent',
            'created_by' => $this->ownerUser->id,
            'is_template' => true,
        ]);

        // Buat karyawan kedua
        $otherUser = User::factory()->create([
            'instansi_id' => $this->instansi->id,
            'role_id' => $this->karyawanRole->id,
        ]);
        $otherKaryawan = karyawan::create([
            'user_id' => $otherUser->id,
            'nama_lengkap' => 'Karyawan Kedua',
            'outlet_id' => $this->outlet->id,
        ]);

        // Act: karyawan pertama terima template
        $this->actingAs($this->karyawanUser)
            ->putJson("/api/penugasans/{$template->id}/accept")
            ->assertStatus(200);

        // Karyawan kedua juga bisa terima template yang sama
        $response = $this->actingAs($otherUser)
            ->putJson("/api/penugasans/{$template->id}/accept");

        // Assert
        $response->assertStatus(200);

        // Template tetap status belum
        $this->assertDatabaseHas('penugasans', [
            'id' => $template->id,
            'status' => 'belum',
            'is_template' => true,
        ]);

        // Ada 2 task baru dari template
        $newTasksCount = DB::table('penugasans')
            ->where('template_penugasan_id', $template->id)
            ->where('is_template', false)
            ->count();
        $this->assertEquals(2, $newTasksCount);
    }

    // ════════════════════════════════════════════════════════════
    //  COMPLETE WORKFLOW TESTS
    // ════════════════════════════════════════════════════════════

    public function test_karyawan_dapat_menandai_tugas_selesai_menunggu_validasi()
    {
        // Arrange
        $penugasan = $this->createPenugasan('proses');

        // Act
        $response = $this->actingAs($this->karyawanUser)
            ->putJson("/api/penugasans/{$penugasan->id}/complete");

        // Assert: karyawan menandai selesai → menunggu_validasi, poin belum diberikan
        $response->assertStatus(200)
            ->assertJson([
                'status' => 'success',
                'message' => 'Tugas menunggu validasi pemilik/manager',
            ]);

        $this->assertDatabaseHas('penugasans', [
            'id' => $penugasan->id,
            'status' => 'menunggu_validasi',
            'poin' => 0,
            'status_changed_by' => $this->karyawanUser->id,
        ]);
        $this->assertNull(
            $penugasan->fresh()->completed_at,
            'completed_at belum boleh terisi sebelum divalidasi'
        );

        // Act: owner memvalidasi (setuju) → selesai, poin diberikan
        $validasiResponse = $this->actingAs($this->ownerUser)
            ->putJson("/api/penugasans/{$penugasan->id}/validasi", ['disetujui' => true]);

        $validasiResponse->assertStatus(200)
            ->assertJson([
                'status' => 'success',
                'message' => 'Tugas disetujui & selesai',
            ]);

        $this->assertDatabaseHas('penugasans', [
            'id' => $penugasan->id,
            'status' => 'selesai',
            'poin' => 20, // sedang = 20 poin
        ]);
        $this->assertNotNull(
            $penugasan->fresh()->completed_at,
            'completed_at harus terisi setelah divalidasi'
        );
    }

    public function test_karyawan_tidak_dapat_memvalidasi_tugas_sendiri()
    {
        $penugasan = $this->createPenugasan('proses');
        $this->actingAs($this->karyawanUser)
            ->putJson("/api/penugasans/{$penugasan->id}/complete");

        $response = $this->actingAs($this->karyawanUser)
            ->putJson("/api/penugasans/{$penugasan->id}/validasi", ['disetujui' => true]);

        $response->assertStatus(403);
    }

    public function test_owner_dapat_mengembalikan_tugas_yang_tidak_disetujui()
    {
        $penugasan = $this->createPenugasan('proses');
        $this->actingAs($this->karyawanUser)
            ->putJson("/api/penugasans/{$penugasan->id}/complete");

        $response = $this->actingAs($this->ownerUser)
            ->putJson("/api/penugasans/{$penugasan->id}/validasi", ['disetujui' => false]);

        $response->assertStatus(200)
            ->assertJson([
                'status' => 'success',
                'message' => 'Tugas dikembalikan ke karyawan',
            ]);

        $this->assertDatabaseHas('penugasans', [
            'id' => $penugasan->id,
            'status' => 'proses',
            'poin' => 0,
        ]);
    }

    public function test_karyawan_tidak_dapat_menyelesaikan_tugas_dengan_status_belum()
    {
        // Arrange
        $penugasan = $this->createPenugasan('belum');

        // Act
        $response = $this->actingAs($this->karyawanUser)
            ->putJson("/api/penugasans/{$penugasan->id}/complete");

        // Assert
        $response->assertStatus(422);
    }

    public function test_poin_dihitung_benar_berdasarkan_urgency()
    {
        // Test urgent = 30 — poin baru diberikan setelah owner memvalidasi
        $urgentTask = $this->createPenugasan('proses');
        $urgentTask->update(['urgency' => 'urgent']);

        $response = $this->actingAs($this->karyawanUser)
            ->putJson("/api/penugasans/{$urgentTask->id}/complete");
        $response->assertStatus(200);

        $this->actingAs($this->ownerUser)
            ->putJson("/api/penugasans/{$urgentTask->id}/validasi", ['disetujui' => true])
            ->assertStatus(200);

        $this->assertDatabaseHas('penugasans', [
            'id' => $urgentTask->id,
            'poin' => 30,
        ]);

        // Test rendah = 10
        $rendahTask = $this->createPenugasan('proses');
        $rendahTask->update(['urgency' => 'rendah']);

        $response2 = $this->actingAs($this->karyawanUser)
            ->putJson("/api/penugasans/{$rendahTask->id}/complete");
        $response2->assertStatus(200);

        $this->actingAs($this->ownerUser)
            ->putJson("/api/penugasans/{$rendahTask->id}/validasi", ['disetujui' => true])
            ->assertStatus(200);

        $this->assertDatabaseHas('penugasans', [
            'id' => $rendahTask->id,
            'poin' => 10,
        ]);
    }

    // ════════════════════════════════════════════════════════════
    //  NOTIFICATION TESTS
    // ════════════════════════════════════════════════════════════

    public function test_notifikasi_dikirim_ke_owner_ketika_tugas_diterima()
    {
        // Arrange
        $penugasan = $this->createPenugasan('belum');

        // Act
        $this->actingAs($this->karyawanUser)
            ->putJson("/api/penugasans/{$penugasan->id}/accept");

        // Assert: notifikasi terbuat untuk owner
        $this->assertDatabaseHas('notifications', [
            'user_id' => $this->ownerUser->id,
            'type' => 'penugasan',
            'title' => 'Tugas Sedang Dikerjakan',
        ]);
    }

    public function test_notifikasi_dikirim_ke_owner_ketika_tugas_selesai()
    {
        // Arrange
        $penugasan = $this->createPenugasan('proses');

        // Act: karyawan menandai selesai → menunggu validasi owner
        $this->actingAs($this->karyawanUser)
            ->putJson("/api/penugasans/{$penugasan->id}/complete");

        // Assert: notifikasi terbuat untuk owner
        $this->assertDatabaseHas('notifications', [
            'user_id' => $this->ownerUser->id,
            'type' => 'penugasan',
            'title' => 'Menunggu Validasi Tugas',
        ]);
    }

    // ════════════════════════════════════════════════════════════
    //  UNAUTHENTICATED TESTS
    // ════════════════════════════════════════════════════════════

    public function test_unauthenticated_tidak_dapat_accept_tugas()
    {
        $penugasan = $this->createPenugasan('belum');

        $response = $this->putJson("/api/penugasans/{$penugasan->id}/accept");

        $response->assertStatus(401);
    }

    public function test_unauthenticated_tidak_dapat_complete_tugas()
    {
        $penugasan = $this->createPenugasan('proses');

        $response = $this->putJson("/api/penugasans/{$penugasan->id}/complete");

        $response->assertStatus(401);
    }
}
