<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Foundation\Auth\User as Authenticatable;
use Illuminate\Notifications\Notifiable;
use Laravel\Sanctum\HasApiTokens;
use Illuminate\Database\Eloquent\Concerns\HasUuids;

class User extends Authenticatable
{
    use HasApiTokens, HasFactory, Notifiable, HasUuids;

    // UUID Setup
    public $incrementing = false;
    protected $keyType = 'string';

    // Matikan updated_at karena di desainmu hanya ada created_at
    const UPDATED_AT = null;

    protected $fillable = [
        'nomor_telp', 
        'email',
        'password', 
        'role', 
        'is_active'
    ];

    protected $hidden = [
        'password',
    ];

    // Relasi ke tabel profil (perhatikan foreign key 'id_pengguna' disebutkan eksplisit)
    public function pemilik()
    {
        return $this->hasOne(Pemilik::class, 'id_pengguna');
    }

    public function karyawan()
    {
        return $this->hasOne(Karyawan::class, 'id_pengguna');
    }

    public function pembeli()
    {
        return $this->hasOne(Pembeli::class, 'id_pengguna');
    }
}