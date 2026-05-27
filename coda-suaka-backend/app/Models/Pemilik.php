<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Concerns\HasUuids;

class Pemilik extends Model
{
    use HasUuids;

    protected $table = 'pemilik';
    
    public $incrementing = false;
    protected $keyType = 'string';
    
    const UPDATED_AT = null;

    protected $fillable = [
        'id_pengguna', 
        'nama_lengkap', 
        'nik_ktp', 
        'akun_bank', 
        'nama_bank'
    ];

    // Relasi balik ke User
    public function user()
    {
        return $this->belongsTo(User::class, 'id_pengguna');
    }

    // Pemilik bisa punya banyak Toko
    public function toko()
    {
        return $this->hasMany(Toko::class, 'id_pemilik');
    }
}