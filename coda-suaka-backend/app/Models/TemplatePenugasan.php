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
            $builder->where('instansi_id', $user->instansi_id);
        }));
    }

    public function pembuat()
    {
        return $this->belongsTo(User::class, 'created_by');
    }
}
