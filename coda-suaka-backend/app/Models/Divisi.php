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
            // outlet_id di tabel divisis adalah NOT NULL (constrained),
            // jadi whereHas outlet sudah cukup aman.
            // Tapi untuk jaga-jaga jika ada data lama, gunakan fallback via ketuaKaryawan
            $builder->where(function ($q) use ($user) {
                $q->whereHas('outlet', function (Builder $q) use ($user) {
                    $q->where('instansi_id', $user->instansi_id);
                });
                // Fallback: jika outlet_id NULL (data legacy), scope via user pembuat
                // Karena tidak ada created_by di divisi, fallback via ketua_karyawan_id → user
                $q->orWhere(function ($subQ) use ($user) {
                    $subQ->whereNull('outlet_id')
                         ->whereHas('ketuaKaryawan.user', function (Builder $q) use ($user) {
                             $q->where('instansi_id', $user->instansi_id);
                         });
                });
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
