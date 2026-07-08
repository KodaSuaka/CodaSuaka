<?php

namespace App\Models;

use App\Models\Scopes\TenantScope;
use Illuminate\Database\Eloquent\Builder;
use Illuminate\Database\Eloquent\Model;

class attandence extends Model
{
    protected $fillable = [
        'user_id',
        'tanggal',
        'jam_checkin',
        'jam_checkout',
        'status',
        'keterangan',
        'lokasi_checkin',
    ];

    protected function casts(): array
    {
        return [
            'tanggal' => 'date',
            'jam_checkin' => 'datetime:H:i:s',
            'jam_checkout' => 'datetime:H:i:s',
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
}
