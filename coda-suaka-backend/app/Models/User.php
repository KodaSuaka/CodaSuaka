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

    /**
     * Get the attributes that should be cast.
     *
     * @return array<string, string>
     */
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
        return $this->belongsTo(Role::class);
    }

    /**
     * Profil karyawan yang terkait dengan user ini.
     */
    public function profilKaryawan()
    {
        return $this->hasOne(Karyawan::class, 'user_id');
    }

    /**
     * Pesan yang dikirim oleh user ini.
     */
    public function pesanDikirim()
    {
        return $this->hasMany(Chat::class, 'pengirim_id');
    }

    /**
     * Pesan yang diterima oleh user ini.
     */
    public function pesanDiterima()
    {
        return $this->hasMany(Chat::class, 'penerima_id');
    }
}
