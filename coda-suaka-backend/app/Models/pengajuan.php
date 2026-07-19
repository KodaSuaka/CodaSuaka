<?php

namespace App\Models;

use App\Models\Scopes\TenantScope;
use Illuminate\Database\Eloquent\Builder;
use Illuminate\Database\Eloquent\Model;

class pengajuan extends Model
{
    protected $fillable = [
        'user_id',
        'jenis',
        'tanggal_mulai',
        'tanggal_selesai',
        'keterangan',
        'status',
        'disetujui_oleh',
        'tanggal_disetujui',
        'alasan_penolakan',
    ];

    protected function casts(): array
    {
        return [
            'tanggal_mulai' => 'date',
            'tanggal_selesai' => 'date',
            'tanggal_disetujui' => 'datetime',
        ];
    }

    protected static function booted(): void
    {
        static::addGlobalScope(new TenantScope(function (Builder $builder, $user) {
            $builder->whereHas('user', function (Builder $q) use ($user) {
                $q->where('instansi_id', $user->instansi_id);
            });
        }));
    }

    public function user()
    {
        return $this->belongsTo(User::class);
    }

    public function penyetuju()
    {
        return $this->belongsTo(User::class, 'disetujui_oleh');
    }
}
