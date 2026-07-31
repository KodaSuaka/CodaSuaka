<?php

namespace Database\Factories;

use App\Models\BarangJasa;
use App\Models\Instansi;
use Illuminate\Database\Eloquent\Factories\Factory;

/**
 * @extends Factory<BarangJasa>
 */
class BarangJasaFactory extends Factory
{
    protected $model = BarangJasa::class;

    public function definition(): array
    {
        return [
            'instansi_id' => Instansi::factory(),
            'nama' => fake()->word(),
            'jenis' => fake()->randomElement(BarangJasa::JENIS_VALID),
            'satuan' => 'pcs',
            'harga_jual' => fake()->numberBetween(5000, 200000),
            'stok' => fake()->numberBetween(0, 100),
            'is_active' => true,
        ];
    }
}
