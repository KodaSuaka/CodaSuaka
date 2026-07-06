<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Concerns\HasUuids;
use Illuminate\Database\Eloquent\Model;

class karyawan extends Model
{
    use HasUuids;

    protected $fillable = [
        'user_id',
        'nama_lengkap',
        'kontak',
        'foto_profil',
    ];

    /**
     * User yang memiliki profil karyawan ini.
     */
    public function user()
    {
        return $this->belongsTo(User::class);
    }
}
