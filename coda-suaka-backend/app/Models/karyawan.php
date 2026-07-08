<?php

namespace App\Models;

use App\Models\Scopes\TenantScope;
use Illuminate\Database\Eloquent\Builder;
use Illuminate\Database\Eloquent\Concerns\HasUuids;
use Illuminate\Database\Eloquent\Model;

class karyawan extends Model
{
    use HasUuids;

    protected $fillable = [
        'user_id',
        'nama_lengkap',
        'kontak',
        'alamat',
        'foto_profil',
        'outlet_id',
        'sisa_cuti',
    ];

    protected static function booted(): void
    {
        static::addGlobalScope(new TenantScope(function (Builder $builder, $user) {
            $builder->whereHas('outlet', function (Builder $q) use ($user) {
                $q->where('instansi_id', $user->instansi_id);
            });
        }));
    }

    public function user()
    {
        return $this->belongsTo(User::class);
    }

    public function outlet()
    {
        return $this->belongsTo(outlet::class);
    }

    public function divisi()
    {
        return $this->belongsToMany(Divisi::class, 'anggota_divisis', 'karyawan_id', 'divisi_id');
    }

    public function anggotaDivisis()
    {
        return $this->hasMany(AnggotaDivisi::class, 'karyawan_id');
    }

    public function penugasans()
    {
        return $this->hasMany(penugasan::class, 'penanggung_jawab_id');
    }

    public function divisiKetua()
    {
        return $this->hasMany(Divisi::class, 'ketua_karyawan_id');
    }
}
