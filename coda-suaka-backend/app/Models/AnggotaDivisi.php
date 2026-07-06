<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class AnggotaDivisi extends Model
{
    protected $fillable = [
        'divisi_id',
        'karyawan_id',
    ];

    public function divisi()
    {
        return $this->belongsTo(Divisi::class);
    }

    public function karyawan()
    {
        return $this->belongsTo(karyawan::class);
    }
}
