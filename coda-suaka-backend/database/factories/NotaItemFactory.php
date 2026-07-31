<?php

namespace Database\Factories;

use App\Models\Nota;
use App\Models\NotaItem;
use Illuminate\Database\Eloquent\Factories\Factory;

/**
 * @extends Factory<NotaItem>
 */
class NotaItemFactory extends Factory
{
    protected $model = NotaItem::class;

    public function definition(): array
    {
        $kuantitas = fake()->numberBetween(1, 5);
        $hargaSatuan = fake()->numberBetween(5000, 200000);

        return [
            'nota_id' => Nota::factory(),
            'nama_item' => fake()->word(),
            'jenis' => 'barang',
            'kuantitas' => $kuantitas,
            'satuan' => 'pcs',
            'harga_satuan' => $hargaSatuan,
            'subtotal' => $kuantitas * $hargaSatuan,
        ];
    }
}
