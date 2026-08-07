<?php

namespace Database\Factories;

use App\Models\RequestLog;
use Illuminate\Database\Eloquent\Factories\Factory;

/**
 * @extends Factory<RequestLog>
 */
class RequestLogFactory extends Factory
{
    protected $model = RequestLog::class;

    public function definition(): array
    {
        return [
            'method' => fake()->randomElement(['GET', 'POST', 'PUT', 'DELETE']),
            'path' => 'api/'.fake()->randomElement(['dashboard/omset', 'transaksi-kas', 'karyawans', 'stoks', 'notas']),
            'full_url' => fake()->url(),
            'query_params' => null,
            'request_body' => null,
            'ip_address' => fake()->ipv4(),
            'user_agent' => fake()->userAgent(),
            'status_code' => fake()->randomElement([200, 201, 422, 403, 500]),
            'duration_ms' => fake()->numberBetween(5, 2000),
            'created_at' => now()->subMinutes(fake()->numberBetween(0, 1000)),
            'updated_at' => now()->subMinutes(fake()->numberBetween(0, 1000)),
        ];
    }
}
