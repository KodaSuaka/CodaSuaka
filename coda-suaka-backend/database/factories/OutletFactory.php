<?php

namespace Database\Factories;

use App\Models\outlet;
use Illuminate\Database\Eloquent\Factories\Factory;

/**
 * @extends Factory<outlet>
 */
class OutletFactory extends Factory
{
    /**
     * Define the model's default state.
     *
     * @return array<string, mixed>
     */
    public function definition(): array
    {
        return [
            'nama_outlet' => fake()->company(),
            'alamat_outlet' => fake()->address(),
            'is_active' => true,
        ];
    }
}
