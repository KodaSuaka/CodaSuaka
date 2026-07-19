<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class paket extends Model
{
    protected $fillable = [
        'nama_paket',
        'harga',
        'deskripsi',
        'fitur',
        'durasi_hari',
        'max_outlet',
        'max_karyawan_per_outlet',
        'is_active',
    ];

    protected function casts(): array
    {
        return [
            'harga' => 'decimal:2',
            'is_active' => 'boolean',
        ];
    }

    public function transaksiPakets()
    {
        return $this->hasMany(transaksi_paket::class);
    }

    public function instansis()
    {
        return $this->hasMany(instansi::class, 'paket_id');
    }
}
