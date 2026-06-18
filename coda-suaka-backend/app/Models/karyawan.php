<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class karyawan extends Model
{
    protected $fillable = [
        'user_id',
        'nama_lengkap',
        'kontak',
        'foto_profil',
    ];
}
