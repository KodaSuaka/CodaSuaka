<?php

namespace App\Models;

use App\Models\Scopes\TenantScope;
use Illuminate\Database\Eloquent\Model;

class outlet extends Model
{
    protected $fillable = [
        'nama_outlet',
        'alamat_outlet',
        'instansi_id',
        'is_active',
    ];

    protected function casts(): array
    {
        return [
            'is_active' => 'boolean',
        ];
    }

    protected static function booted(): void
    {
        static::addGlobalScope(new TenantScope('instansi_id'));
    }

    public function instansi()
    {
        return $this->belongsTo(instansi::class);
    }

    public function users()
    {
        return $this->hasMany(User::class);
    }

    public function karyawans()
    {
        return $this->hasMany(karyawan::class);
    }

    public function divisis()
    {
        return $this->hasMany(Divisi::class);
    }

    public function jadwals()
    {
        return $this->hasMany(jadwal::class);
    }
}
