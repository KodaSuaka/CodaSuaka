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
        'tempat_lahir',
        'tanggal_lahir',
        'foto_profil',
        'outlet_id',
        'sisa_cuti',
        'tanggal_mulai_kerja',
    ];

    /**
     * lama_bekerja adalah nilai turunan (dihitung dari tanggal_mulai_kerja),
     * disertakan otomatis di response JSON.
     */
    protected $appends = ['lama_bekerja'];

    protected function casts(): array
    {
        return [
            'tanggal_mulai_kerja' => 'date:Y-m-d',
            'tanggal_lahir' => 'date:Y-m-d',
        ];
    }

    /**
     * Lama bekerja dalam format "X tahun Y bulan" dihitung dari
     * tanggal_mulai_kerja hingga sekarang. null bila tanggal mulai kerja kosong.
     */
    public function getLamaBekerjaAttribute(): ?string
    {
        if ($this->tanggal_mulai_kerja === null) {
            return null;
        }

        $mulai = \Carbon\Carbon::parse($this->tanggal_mulai_kerja);
        if ($mulai->isFuture()) {
            return '0 bulan';
        }

        $tahun = $mulai->diffInYears(now());
        $bulan = $mulai->copy()->addYears($tahun)->diffInMonths(now());

        $bagian = [];
        if ($tahun > 0) {
            $bagian[] = $tahun.' tahun';
        }
        $bagian[] = $bulan.' bulan';

        return implode(' ', $bagian);
    }

    protected static function booted(): void
    {
        static::addGlobalScope(new TenantScope(function (Builder $builder, $user) {
            $builder->where(function ($q) use ($user) {
                // Jika karyawan memiliki outlet, scope via outlet → instansi
                $q->whereHas('outlet', function (Builder $q) use ($user) {
                    $q->where('instansi_id', $user->instansi_id);
                });
                // Fallback: jika outlet_id NULL, scope via user (user_id → user.instansi_id)
                // karena user_id adalah foreign key yang NOT NULL di migration
                $q->orWhere(function ($subQ) use ($user) {
                    $subQ->whereNull('outlet_id')
                        ->whereHas('user', function (Builder $q) use ($user) {
                            $q->where('instansi_id', $user->instansi_id);
                        });
                });
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
