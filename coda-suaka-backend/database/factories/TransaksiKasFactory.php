<?php

namespace Database\Factories;

use App\Models\TransaksiKas;
use App\Models\User;
use App\Models\instansi;
use App\Models\KategoriTransaksi;
use App\Models\outlet;
use Illuminate\Database\Eloquent\Factories\Factory;

/**
 * @extends Factory<TransaksiKas>
 */
class TransaksiKasFactory extends Factory
{
    protected $model = TransaksiKas::class;

    /**
     * Ikat semua relasi (outlet, kategori, created_by) ke instansi_id yang sama.
     * State ini dipanggil otomatis oleh configure() di bawah.
     */
    public function configure(): static
    {
        return $this->afterMaking(function (TransaksiKas $transaksiKas) {
            // afterMaking masih belum punya id, jadi kita handle di afterCreating
        })->afterCreating(function (TransaksiKas $transaksiKas) {
            // Pastikan outlet_id, kategori_transaksi_id, dan created_by
            // semuanya merujuk ke instansi_id yang sama dengan transaksi ini.
            $instansiId = $transaksiKas->instansi_id;

            if ($transaksiKas->outlet_id && $transaksiKas->outlet->instansi_id !== $instansiId) {
                $outlet = outlet::factory()->create(['instansi_id' => $instansiId]);
                $transaksiKas->outlet_id = $outlet->id;
                $transaksiKas->save();
            }

            if ($transaksiKas->kategori_transaksi_id && $transaksiKas->kategoriTransaksi->instansi_id !== $instansiId) {
                $kategori = KategoriTransaksi::factory()->create(['instansi_id' => $instansiId]);
                $transaksiKas->kategori_transaksi_id = $kategori->id;
                $transaksiKas->save();
            }
        });
    }

    public function definition(): array
    {
        $tipe = fake()->randomElement(['masuk', 'keluar']);

        return [
            'instansi_id' => instansi::factory(),
            'outlet_id' => outlet::factory(),
            'kategori_transaksi_id' => KategoriTransaksi::factory(),
            'tanggal' => fake()->dateTimeBetween('-3 months', 'now')->format('Y-m-d'),
            'tipe' => $tipe,
            'nominal' => fake()->randomFloat(2, 1000, 5000000),
            'metode_pembayaran' => fake()->randomElement(['Tunai', 'Transfer', 'QRIS', null]),
            'keterangan' => fake()->optional(0.7)->sentence(),
            'created_by' => User::factory(),
        ];
    }

    /**
     * Indicate that the transaksi is pemasukan.
     */
    public function pemasukan(): static
    {
        return $this->state(fn (array $attributes) => [
            'tipe' => 'masuk',
        ]);
    }

    /**
     * Indicate that the transaksi is pengeluaran.
     */
    public function pengeluaran(): static
    {
        return $this->state(fn (array $attributes) => [
            'tipe' => 'keluar',
        ]);
    }
}
