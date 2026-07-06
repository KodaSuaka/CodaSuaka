<?php

namespace App\Models;

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

    public function user()
    {
        return $this->belongsTo(User::class);
    }

    public function penyetuju()
    {
        return $this->belongsTo(User::class, 'disetujui_oleh');
    }
}
