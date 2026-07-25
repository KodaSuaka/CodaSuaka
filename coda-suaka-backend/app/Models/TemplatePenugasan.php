<?php

namespace App\Models;

use App\Models\Scopes\TenantScope;
use Illuminate\Database\Eloquent\Builder;
use Illuminate\Database\Eloquent\Model;

class TemplatePenugasan extends Model
{
    protected $table = 'template_penugasans';

    protected $fillable = [
        'nama_template',
        'deskripsi_template',
        'urgency_default',
        'poin_default',
        'instansi_id',
        'created_by',
    ];

    protected function casts(): array
    {
        return [
            'poin_default' => 'integer',
        ];
    }

    protected static function booted(): void
    {
        static::addGlobalScope(new TenantScope(function (Builder $builder, $user) {
            // Tampilkan template global (instansi_id = NULL) + template milik instansi user
            $builder->where(function ($q) use ($user) {
                $q->whereNull('instansi_id')
                    ->orWhere('instansi_id', $user->instansi_id);
            });
        }));
    }

    public function pembuat()
    {
        return $this->belongsTo(User::class, 'created_by');
    }
}
