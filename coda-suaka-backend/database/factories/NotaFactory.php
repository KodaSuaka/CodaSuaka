<?php

namespace Database\Factories;

use App\Models\Instansi;
use App\Models\KategoriTransaksi;
use App\Models\Nota;
use App\Models\outlet;
use App\Models\User;
use Illuminate\Database\Eloquent\Factories\Factory;

/**
 * @extends Factory<Nota>
 */
class NotaFactory extends Factory
{
    protected $model = Nota::class;

    /**
     * Ikat semua relasi (outlet, kategori, created_by) ke instansi_id yang sama.
     * State ini dipanggil otomatis oleh configure() di bawah.
     */
    public function configure(): static
    {
        return $this->afterCreating(function (Nota $nota) {
            // Pastikan outlet_id dan kategori_transaksi_id merujuk ke
            // instansi_id yang sama dengan nota ini.
            $instansiId = $nota->instansi_id;

            if ($nota->outlet_id && $nota->outlet->instansi_id !== $instansiId) {
                $outlet = outlet::factory()->create(['instansi_id' => $instansiId]);
                $nota->outlet_id = $outlet->id;
                $nota->save();
            }

            if ($nota->kategori_transaksi_id && $nota->kategoriTransaksi->instansi_id !== $instansiId) {
                $kategori = KategoriTransaksi::factory()->create(['instansi_id' => $instansiId]);
                $nota->kategori_transaksi_id = $kategori->id;
                $nota->save();
            }
        });
    }

    public function definition(): array
    {
        return [
            'instansi_id' => Instansi::factory(),
            'outlet_id' => outlet::factory(),
            'kategori_transaksi_id' => KategoriTransaksi::factory(),
            'tipe' => fake()->randomElement(Nota::TIPE_VALID),
            'nomor_nota' => 'TEST-'.fake()->unique()->numerify('####'),
            'tanggal' => now()->format('Y-m-d'),
            'total' => 0,
            'created_by' => User::factory(),
        ];
    }

    /**
     * Indicate that the nota is penjualan.
     */
    public function penjualan(): static
    {
        return $this->state(fn (array $attributes) => [
            'tipe' => 'penjualan',
        ]);
    }

    /**
     * Indicate that the nota is pembelian.
     */
    public function pembelian(): static
    {
        return $this->state(fn (array $attributes) => [
            'tipe' => 'pembelian',
        ]);
    }
}
