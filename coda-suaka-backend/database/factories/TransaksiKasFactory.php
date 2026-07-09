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
