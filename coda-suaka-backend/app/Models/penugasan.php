<?php

namespace App\Models;

use App\Models\Scopes\TenantScope;
use Illuminate\Database\Eloquent\Builder;
use Illuminate\Database\Eloquent\Model;

class penugasan extends Model
{
    protected $fillable = [
        'judul',
        'deskripsi',
        'penanggung_jawab_id',
        'divisi_id',
        'tenggat',
        'status',
        'urgency',
        'poin',
        'created_by',
        'is_template',
        'instansi_id',
        'template_penugasan_id',
        'accepted_at',
        'completed_at',
        'status_changed_by',
    ];

    protected function casts(): array
    {
        return [
            'tenggat' => 'date',
            'poin' => 'integer',
            'is_template' => 'boolean',
            'accepted_at' => 'datetime',
            'completed_at' => 'datetime',
        ];
    }

    /**
     * Mapping urgency ke poin yang diperoleh saat tugas selesai.
     * urgent = 30, sedang = 20, rendah = 10.
     */
    public static function getPoinForUrgency(string $urgency): int
    {
        return match ($urgency) {
            'urgent' => 30,
            'sedang' => 20,
            'rendah' => 10,
            default => 0,
        };
    }

    protected static function booted(): void
    {
        static::addGlobalScope(new TenantScope(function (Builder $builder, $user) {
            $builder->where(function ($q) use ($user) {
                // Template: scope via instansi_id langsung
                $q->where(function ($tq) use ($user) {
                    $tq->where('is_template', true)
                        ->where(function ($tq2) use ($user) {
                            $tq2->whereNull('instansi_id') // template global
                                ->orWhere('instansi_id', $user->instansi_id);
                        });
                });
                // Regular tugas dengan divisi: scope via divisi → outlet → instansi
                $q->orWhere(function ($tq) use ($user) {
                    $tq->where('is_template', false)
                        ->whereHas('divisi.outlet', function (Builder $tq2) use ($user) {
                            $tq2->where('instansi_id', $user->instansi_id);
                        });
                });
                // Regular tugas tanpa divisi: scope via pembuat (created_by → user.instansi_id)
                $q->orWhere(function ($subQ) use ($user) {
                    $subQ->where('is_template', false)
                        ->whereNull('divisi_id')
                        ->whereHas('pembuat', function (Builder $q) use ($user) {
                            $q->where('instansi_id', $user->instansi_id);
                        });
                });
            });
        }));
    }

    /**
     * Scope: hanya template (is_template = true).
     */
    public function scopeTemplates($query)
    {
        return $query->where('is_template', true);
    }

    /**
     * Scope: hanya tugas biasa (is_template = false).
     */
    public function scopeRegularTasks($query)
    {
        return $query->where('is_template', false);
    }

    public function penanggungJawab()
    {
        return $this->belongsTo(karyawan::class, 'penanggung_jawab_id');
    }

    public function divisi()
    {
        return $this->belongsTo(Divisi::class);
    }

    public function pembuat()
    {
        return $this->belongsTo(User::class, 'created_by');
    }

    public function statusChanger()
    {
        return $this->belongsTo(User::class, 'status_changed_by');
    }
}
