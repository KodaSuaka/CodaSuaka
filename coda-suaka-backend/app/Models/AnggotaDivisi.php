<?php

namespace App\Models;

use App\Models\Scopes\TenantScope;
use Illuminate\Database\Eloquent\Builder;
use Illuminate\Database\Eloquent\Model;

class AnggotaDivisi extends Model
{
    protected $fillable = [
        'divisi_id',
        'karyawan_id',
    ];

    protected static function booted(): void
    {
        static::addGlobalScope(new TenantScope(function (Builder $builder, $user) {
            $builder->whereHas('divisi.outlet', function (Builder $q) use ($user) {
                $q->where('instansi_id', $user->instansi_id);
            });
        }));
    }

    public function divisi()
    {
        return $this->belongsTo(Divisi::class);
    }

    public function karyawan()
    {
        return $this->belongsTo(karyawan::class);
    }
}
