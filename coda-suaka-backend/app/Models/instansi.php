<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Concerns\HasUuids;
use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class instansi extends Model
{
    use HasFactory, HasUuids;

    protected $fillable = [
        'nama_instansi',
        'paket_id',
    ];

    public function users()
    {
        return $this->hasMany(User::class);
    }

    public function outlets()
    {
        return $this->hasMany(outlet::class);
    }

    public function transaksiPakets()
    {
        return $this->hasMany(transaksi_paket::class);
    }

    public function paket()
    {
        return $this->belongsTo(paket::class, 'paket_id');
    }
}
