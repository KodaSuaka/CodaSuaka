<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class Divisi extends Model
{
    protected $fillable = [
        'nama_divisi',
        'deskripsi',
        'ketua_karyawan_id',
        'outlet_id',
    ];

    public function ketuaKaryawan()
    {
        return $this->belongsTo(karyawan::class, 'ketua_karyawan_id');
    }

    public function outlet()
    {
        return $this->belongsTo(outlet::class);
    }

    public function anggota()
    {
        return $this->hasMany(AnggotaDivisi::class, 'divisi_id');
    }

    public function karyawans()
    {
        return $this->belongsToMany(karyawan::class, 'anggota_divisis', 'divisi_id', 'karyawan_id');
    }

    public function penugasans()
    {
        return $this->hasMany(penugasan::class);
    }
}
