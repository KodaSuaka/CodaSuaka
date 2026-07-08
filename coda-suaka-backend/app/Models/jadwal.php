<?php

namespace App\Models;

use App\Models\Scopes\TenantScope;
use Illuminate\Database\Eloquent\Builder;
use Illuminate\Database\Eloquent\Model;

class jadwal extends Model
{
    protected $fillable = [
        'nama_event',
        'deskripsi',
        'tanggal',
        'kategori',
        'outlet_id',
        'created_by',
    ];

    protected function casts(): array
    {
        return [
            'tanggal' => 'date',
        ];
    }

    protected static function booted(): void
    {
        static::addGlobalScope(new TenantScope(function (Builder $builder, $user) {
            $builder->whereHas('outlet', function (Builder $q) use ($user) {
                $q->where('instansi_id', $user->instansi_id);
            });
        }));
    }

    public function outlet()
    {
        return $this->belongsTo(outlet::class);
    }

    public function pembuat()
    {
        return $this->belongsTo(User::class, 'created_by');
    }
}
