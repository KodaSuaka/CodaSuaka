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

    /**
     * Regresi: status_approval hilang dari TransaksiKas::$fillable membuat
     * Eloquent mass-assignment (create()/update()) selalu membuang key ini
     * secara diam-diam, sehingga kolom status_approval selalu berakhir di
     * default DB ('disetujui') walau controller/ApprovalService bermaksud
     * mengubahnya menjadi 'pending'.
     */
    public function test_transaksi_keluar_di_atas_threshold_berstatus_pending()
    {
        $kategoriKeluar = KategoriTransaksi::factory()
            ->pengeluaran()
            ->create(['instansi_id' => $this->instansi->id]);

        $payload = [
            'tanggal' => '2026-07-01',
            'tipe' => 'keluar',
            'nominal' => 2000000, // di atas threshold_nominal default (1.000.000)
            'kategori_transaksi_id' => $kategoriKeluar->id,
            'metode_pembayaran' => 'Tunai',
            'keterangan' => 'Pembelian besar butuh approval',
        ];

        $response = $this->actingAs($this->user)
            ->postJson('/api/transaksi-kas', $payload);

        $response->assertStatus(201)
            ->assertJsonPath('data.status_approval', 'pending');

        $this->assertDatabaseHas('transaksi_kas', [
            'id' => $response->json('data.id'),
            'status_approval' => 'pending',
        ]);
    }

    /**
     * Karakterisasi filter rentang tanggal: batas start_date & end_date
     * harus inklusif di kedua ujung. Test ini mengunci perilaku sebelum
     * whereDate() (yang membungkus kolom dengan DATE() sehingga index
     * tanggal tidak terpakai) diganti perbandingan langsung yang sargable.
     */
    public function test_filter_rentang_tanggal_inklusif_di_kedua_ujung()
    {
        foreach (['2026-07-01', '2026-07-15', '2026-07-31'] as $tanggal) {
            TransaksiKas::factory()->pemasukan()->create([
                'instansi_id' => $this->instansi->id,
                'kategori_transaksi_id' => $this->kategori->id,
                'tanggal' => $tanggal,
            ]);
        }

        // Rentang penuh — ketiga baris masuk (batas awal & akhir inklusif).
        $this->actingAs($this->user)
            ->getJson('/api/transaksi-kas?start_date=2026-07-01&end_date=2026-07-31')
            ->assertStatus(200)
            ->assertJsonCount(3, 'data');

        // Rentang satu hari tepat di tengah — hanya satu baris.
        $response = $this->actingAs($this->user)
            ->getJson('/api/transaksi-kas?start_date=2026-07-15&end_date=2026-07-15');

        $response->assertStatus(200)->assertJsonCount(1, 'data');

        // Rentang yang mengecualikan kedua ujung.
        $this->actingAs($this->user)
            ->getJson('/api/transaksi-kas?start_date=2026-07-02&end_date=2026-07-30')
            ->assertStatus(200)
            ->assertJsonCount(1, 'data');
    }

    /**
     * Endpoint saldo memakai filter tanggal yang sama — pastikan batasnya
     * juga tetap inklusif setelah perubahan ke perbandingan sargable.
     */
    public function test_saldo_menghormati_batas_rentang_tanggal()
    {
        foreach ([['2026-07-01', 100000], ['2026-07-15', 200000], ['2026-07-31', 400000]] as [$tanggal, $nominal]) {
            TransaksiKas::factory()->pemasukan()->create([
                'instansi_id' => $this->instansi->id,
                'kategori_transaksi_id' => $this->kategori->id,
                'tanggal' => $tanggal,
                'nominal' => $nominal,
            ]);
        }

        $response = $this->actingAs($this->user)
            ->getJson('/api/transaksi-kas/saldo?start_date=2026-07-01&end_date=2026-07-15');

        $response->assertStatus(200);
        $this->assertEquals(300000, $response->json('data.total_masuk'));
    }

    /**
     * Regresi: config/app.php memakai timezone Asia/Jakarta (UTC+7), sedangkan
     * cast 'date' bawaan Laravel diserialisasi ke ISO-8601 UTC. Akibatnya
     * tanggal 2026-07-15 00:00 WIB dikirim sebagai 2026-07-14T17:00:00Z —
     * mundur satu hari di seluruh response JSON.
     *
     * LaporanExportController sempat menambal ini hanya di jalur PDF
     * (format('Y-m-d') manual); semua endpoint JSON masih salah. Perbaikan
     * ada di cast model supaya semua pemanggil ikut benar.
     */
    public function test_tanggal_tidak_bergeser_saat_diserialisasi_ke_json()
    {
        $transaksi = TransaksiKas::factory()->pemasukan()->create([
            'instansi_id' => $this->instansi->id,
            'kategori_transaksi_id' => $this->kategori->id,
            'tanggal' => '2026-07-15',
        ]);

        $this->actingAs($this->user)
            ->getJson('/api/transaksi-kas')
            ->assertStatus(200)
            ->assertJsonPath('data.0.tanggal', '2026-07-15');

        $this->actingAs($this->user)
            ->getJson("/api/transaksi-kas/{$transaksi->id}")
            ->assertStatus(200)
            ->assertJsonPath('data.tanggal', '2026-07-15');
    }
}
