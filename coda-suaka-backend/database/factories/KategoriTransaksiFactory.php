<?php

namespace Database\Factories;

use App\Models\KategoriTransaksi;
use App\Models\instansi;
use Illuminate\Database\Eloquent\Factories\Factory;

/**
 * @extends Factory<KategoriTransaksi>
 */
class KategoriTransaksiFactory extends Factory
{
    protected $model = KategoriTransaksi::class;

    public function definition(): array
    {
        $tipe = fake()->randomElement(['masuk', 'keluar']);

        return [
            'instansi_id' => instansi::factory(),
            'nama_kategori' => fake()->unique()->word() . ' ' . $tipe,
            'tipe' => $tipe,
            'sifat' => fake()->randomElement(['operasional', 'non_operasional']),
            'termasuk_hpp' => fake()->boolean(30),
            'is_default' => false,
            'is_active' => true,
        ];
    }

    /**
     * Indicate that the kategori is for pemasukan.
     */
    public function pemasukan(): static
    {
        return $this->state(fn (array $attributes) => [
            'tipe' => 'masuk',
        ]);
    }

    /**
     * Indicate that the kategori is for pengeluaran.
     */
    public function pengeluaran(): static
    {
        return $this->state(fn (array $attributes) => [
            'tipe' => 'keluar',
        ]);
    }

    /**
     * Indicate that the kategori is inactive.
     */
    public function inactive(): static
    {
        return $this->state(fn (array $attributes) => [
            'is_active' => false,
        ]);
    }
}
