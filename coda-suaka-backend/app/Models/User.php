<?php

namespace App\Models;

// use Illuminate\Contracts\Auth\MustVerifyEmail;
use Database\Factories\UserFactory;
use Illuminate\Database\Eloquent\Attributes\Hidden;
use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Foundation\Auth\User as Authenticatable;
use Illuminate\Notifications\Notifiable;
use Laravel\Sanctum\HasApiTokens;

#[Hidden(['password', 'remember_token'])]
class User extends Authenticatable
{
    /** @use HasFactory<UserFactory> */
    use HasApiTokens, HasFactory, Notifiable;

    protected function casts(): array
    {
        return [
            'email_verified_at' => 'datetime',
            'password' => 'hashed',
        ];
    }

    protected $fillable = [
        'name',
        'email',
        'password',
        'role_id',
        'instansi_id',
        'outlet_id',
    ];

    public function role()
    {
        return $this->belongsTo(role::class);
    }

    public function instansi()
    {
        return $this->belongsTo(instansi::class, 'instansi_id');
    }

    public function outlet()
    {
        return $this->belongsTo(outlet::class, 'outlet_id');
    }

    public function profilKaryawan()
    {
        return $this->hasOne(karyawan::class, 'user_id');
    }

    public function pesanDikirim()
    {
        return $this->hasMany(Chat::class, 'pengirim_id');
    }

    public function pesanDiterima()
    {
        return $this->hasMany(Chat::class, 'penerima_id');
    }

    public function attandences()
    {
        return $this->hasMany(attandence::class);
    }

    public function pengajuans()
    {
        return $this->hasMany(pengajuan::class);
    }

    public function penugasanDibuat()
    {
        return $this->hasMany(penugasan::class, 'created_by');
    }

    public function jadwalDibuat()
    {
        return $this->hasMany(jadwal::class, 'created_by');
    }
}
