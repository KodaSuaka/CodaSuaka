<?php

namespace Database\Factories;

use App\Models\Instansi;
use App\Models\Stok;
use Illuminate\Database\Eloquent\Factories\Factory;

/**
 * @extends Factory<Stok>
 */
class StokFactory extends Factory
{
    protected $model = Stok::class;

    public function definition(): array
    {
        return [
            'instansi_id' => Instansi::factory(),
            'nama' => fake()->unique()->words(2, true),
            'kategori' => fake()->randomElement(['Bahan Baku', 'Barang Produksi', 'Peralatan']),
            'satuan' => fake()->randomElement(['pcs', 'kg', 'liter', 'botol']),
            'stok' => fake()->numberBetween(0, 500),
            'stok_minimum' => fake()->numberBetween(1, 20),
            'harga_beli' => fake()->numberBetween(1000, 500000),
            'is_active' => true,
            'keterangan' => null,
        ];
    }
}
