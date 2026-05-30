<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Concerns\HasUuids;

class Toko extends Model
{
    use HasUuids;

    protected $table = 'Toko';
    
    public $incrementing = false;
    protected $keyType = 'string';
    
    const UPDATED_AT = null;

    protected $fillable = [
        'id_pemilik', 
        'nama', 
        'slug', 
        'provinsi', 
        'kota', 
        'kecamatan', 
        'desa', 
        'alamat_lengkap', 
        'latitude', 
        'longtitude', 
        'berlangganan'
    ];

    public function pemilik()
    {
        return $this->belongsTo(Pemilik::class, 'id_pemilik');
    }

    public function karyawan()
    {
        return $this->hasMany(Karyawan::class, 'id_Toko');
    }
}