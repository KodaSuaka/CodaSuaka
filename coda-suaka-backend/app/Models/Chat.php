<?php

namespace App\Models;

use App\Models\Scopes\TenantScope;
use Illuminate\Database\Eloquent\Builder;
use Illuminate\Database\Eloquent\Model;

class Chat extends Model
{
    protected $fillable = [
        'pengirim_id',
        'penerima_id',
        'pesan',
        'is_read',
    ];

    protected $casts = [
        'is_read' => 'boolean',
    ];

    protected static function booted(): void
    {
        static::addGlobalScope(new TenantScope(function (Builder $builder, $user) {
            $builder->where(function ($q) use ($user) {
                // Chat terlihat jika user adalah pengirim atau penerima
                // dibatasi dalam instansi yang sama
                $q->whereHas('pengirim', function (Builder $q) use ($user) {
                    $q->where('instansi_id', $user->instansi_id);
                })->whereHas('penerima', function (Builder $q) use ($user) {
                    $q->where('instansi_id', $user->instansi_id);
                });
            });
        }));
    }

    /**
     * Pengirim pesan.
     */
    public function pengirim()
    {
        return $this->belongsTo(User::class, 'pengirim_id');
    }

    /**
     * Penerima pesan.
     */
    public function penerima()
    {
        return $this->belongsTo(User::class, 'penerima_id');
    }
}
