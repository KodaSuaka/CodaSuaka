<?php

namespace Tests\Feature\Keuangan;

use App\Models\ApprovalLog;
use App\Models\Instansi;
use App\Models\KategoriTransaksi;
use App\Models\outlet;
use App\Models\role;
use App\Models\role_permission;
use App\Models\TransaksiKas;
use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Support\Facades\DB;
use Tests\TestCase;

class ApprovalTest extends TestCase
{
    use RefreshDatabase;

    private User $pemeriksa;

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

        role_permission::firstOrCreate(
            ['role_id' => $role->id, 'permission' => 'approve:keuangan'],
            ['created_at' => now(), 'updated_at' => now()]
        );

        $this->pemeriksa = User::factory()->create([
            'instansi_id' => $this->instansi->id,
            'role_id' => $role->id,
            'email_verified_at' => now(),
        ]);
        $this->kategori = KategoriTransaksi::factory()
            ->pengeluaran()
            ->create(['instansi_id' => $this->instansi->id]);
    }

    /**
     * Buat $count TransaksiKas + ApprovalLog pending di outlet tertentu.
     * Semua seeding harus terjadi SEBELUM actingAs() — TenantScope pada
     * model outlet difilter oleh Auth::user() aktif, sehingga
     * TransaksiKasFactory::afterCreating() (yang mengakses $transaksi->outlet)
     * gagal resolve relasi kalau ada user lain sedang "acting as" saat
     * factory jalan.
     */
    private function seedPendingApprovals(int $count, outlet $outlet): void
    {
        TransaksiKas::factory()
            ->pengeluaran()
            ->count($count)
            ->create([
                'instansi_id' => $this->instansi->id,
                'outlet_id' => $outlet->id,
                'kategori_transaksi_id' => $this->kategori->id,
                'status_approval' => 'pending',
            ])
            ->each(function (TransaksiKas $transaksi) {
                ApprovalLog::create([
                    'transaksi_kas_id' => $transaksi->id,
                    'instansi_id' => $this->instansi->id,
                    'diajukan_oleh' => $transaksi->created_by,
                    'status' => 'pending',
                    'tanggal_diajukan' => now(),
                ]);
            });
    }

    /**
     * actingAs() dengan model User baru (bukan reuse objek PHP yang sama),
     * supaya relasi `role` tidak ikut ke-cache di memori antar request dalam
     * satu test — meniru kondisi request sungguhan yang selalu resolve user
     * dari awal.
     */
    private function actingAsPemeriksa(): static
    {
        return $this->actingAs(User::find($this->pemeriksa->id));
    }

    /**
     * Regresi N+1: jumlah query pada GET /api/approval/pending harus tetap
     * konstan berapa pun banyaknya baris approval (eager loading transaksiKas,
     * kategoriTransaksi, outlet, createdByUser, pengaju). Seed semua data
     * lebih dulu (3 di outlet kecil, 15 tambahan di outlet besar), baru
     * acting-as + request supaya urutan tidak memicu bug TenantScope di atas.
     */
    public function test_pending_approval_query_count_konstan_terlepas_dari_jumlah_baris()
    {
        $outletKecil = outlet::factory()->create(['instansi_id' => $this->instansi->id]);
        $outletBesar = outlet::factory()->create(['instansi_id' => $this->instansi->id]);

        $this->seedPendingApprovals(3, $outletKecil);
        $this->seedPendingApprovals(15, $outletBesar);

        DB::enableQueryLog();
        $this->actingAsPemeriksa()
            ->getJson('/api/approval/pending?outlet_id='.$outletKecil->id)
            ->assertStatus(200)
            ->assertJsonCount(3, 'data');
        $queryCountKecil = count(DB::getQueryLog());
        DB::flushQueryLog();

        $this->actingAsPemeriksa()
            ->getJson('/api/approval/pending?outlet_id='.$outletBesar->id)
            ->assertStatus(200)
            ->assertJsonCount(15, 'data');
        $queryCountBesar = count(DB::getQueryLog());
        DB::disableQueryLog();

        $this->assertSame(
            $queryCountKecil,
            $queryCountBesar,
            "Jumlah query berubah dari {$queryCountKecil} (3 baris) ke {$queryCountBesar} (15 baris) — indikasi N+1."
        );
    }

    /**
     * Regresi N+1 yang sama untuk GET /api/approval/riwayat.
     */
    public function test_riwayat_approval_query_count_konstan_terlepas_dari_jumlah_baris()
    {
        $outletKecil = outlet::factory()->create(['instansi_id' => $this->instansi->id]);
        $outletBesar = outlet::factory()->create(['instansi_id' => $this->instansi->id]);

        $this->seedPendingApprovals(3, $outletKecil);
        $this->seedPendingApprovals(15, $outletBesar);

        DB::enableQueryLog();
        $this->actingAsPemeriksa()
            ->getJson('/api/approval/riwayat?outlet_id='.$outletKecil->id)
            ->assertStatus(200)
            ->assertJsonCount(3, 'data');
        $queryCountKecil = count(DB::getQueryLog());
        DB::flushQueryLog();

        $this->actingAsPemeriksa()
            ->getJson('/api/approval/riwayat?outlet_id='.$outletBesar->id)
            ->assertStatus(200)
            ->assertJsonCount(15, 'data');
        $queryCountBesar = count(DB::getQueryLog());
        DB::disableQueryLog();

        $this->assertSame(
            $queryCountKecil,
            $queryCountBesar,
            "Jumlah query berubah dari {$queryCountKecil} (3 baris) ke {$queryCountBesar} (15 baris) — indikasi N+1."
        );
    }

    /**
     * Regresi: ApprovalService::getPendingApprovals()/getRiwayatApproval()
     * eager-load `transaksiKas.kategoriTransaksi`, tapi select() untuk
     * relasi `transaksiKas` tidak menyertakan kolom kategori_transaksi_id
     * (foreign key). Tanpa FK itu, Eloquent tidak bisa mencocokkan baris
     * kategori ke transaksi induknya, sehingga kategoriTransaksi selalu
     * null di response walau datanya ada — tanpa error dan tanpa query
     * N+1 tambahan (silently broken, bukan lambat).
     */
    public function test_pending_approval_menyertakan_kategori_transaksi()
    {
        $outlet = outlet::factory()->create(['instansi_id' => $this->instansi->id]);
        $this->seedPendingApprovals(1, $outlet);

        $response = $this->actingAsPemeriksa()->getJson('/api/approval/pending');

        $response->assertStatus(200);
        $this->assertNotNull(
            $response->json('data.0.transaksi_kas.kategori_transaksi'),
            'kategori_transaksi hilang dari response approval/pending — kategori_transaksi_id tidak ikut di-select pada eager load transaksiKas.'
        );
        $this->assertSame(
            $this->kategori->nama_kategori,
            $response->json('data.0.transaksi_kas.kategori_transaksi.nama_kategori')
        );
    }

    /**
     * Regresi yang sama untuk GET /api/approval/riwayat.
     */
    public function test_riwayat_approval_menyertakan_kategori_transaksi()
    {
        $outlet = outlet::factory()->create(['instansi_id' => $this->instansi->id]);
        $this->seedPendingApprovals(1, $outlet);

        $response = $this->actingAsPemeriksa()->getJson('/api/approval/riwayat');

        $response->assertStatus(200);
        $this->assertSame(
            $this->kategori->nama_kategori,
            $response->json('data.0.transaksi_kas.kategori_transaksi.nama_kategori')
        );
    }
}
