<?php

namespace App\Models;

use App\Models\Scopes\TenantScope;
use Illuminate\Database\Eloquent\Builder;
use Illuminate\Database\Eloquent\Model;

class Divisi extends Model
{
    protected $fillable = [
        'nama_divisi',
        'deskripsi',
        'ketua_karyawan_id',
        'outlet_id',
    ];

    protected static function booted(): void
    {
        static::addGlobalScope(new TenantScope(function (Builder $builder, $user) {
            $builder->whereHas('outlet', function (Builder $q) use ($user) {
                $q->where('instansi_id', $user->instansi_id);
            });
        }));
    }

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
