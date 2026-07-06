<?php

namespace App\Models;

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
        'created_by',
    ];

    protected function casts(): array
    {
        return [
            'tenggat' => 'date',
        ];
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
}
