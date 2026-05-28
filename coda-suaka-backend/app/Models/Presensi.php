<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Concerns\HasUuids;

class Presensi extends Model
{
    use HasUuids;

    protected $table = 'presensi';
    
    public $incrementing = false;
    protected $keyType = 'string';
    
    const UPDATED_AT = null; // Hanya ada created_at di ERD

    protected $fillable = [
        'id_karyawan',
        'tanggal',
        'hadir',
        'kembali',
        'status',
        'koordinat'
    ];

    public function karyawan()
    {
        return $this->belongsTo(Karyawan::class, 'id_karyawan');
    }

    
}