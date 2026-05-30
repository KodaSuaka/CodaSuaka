<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Concerns\HasUuids;

class Karyawan extends Model
{
    use HasUuids;

    protected $table = 'karyawan';
    
    public $incrementing = false;
    protected $keyType = 'string';

    // Matikan timestamps bawaan karena pakai 'tanggal_bergabung'
    public $timestamps = false;

    protected $fillable = [
        'id_pengguna',
        'id_Toko',
        'nama_lengkap',
        'posisi',
        'status_kerja',
        'poin_performa',
        'tanggal_bergabung'
    ];

    // Relasi balik ke User
    public function user()
    {
        return $this->belongsTo(User::class, 'id_pengguna');
    }

    public function toko()
    {
        return $this->belongsTo(Toko::class, 'id_Toko');
    }
}