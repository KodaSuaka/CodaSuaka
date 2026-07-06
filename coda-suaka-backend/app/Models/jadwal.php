<?php

namespace App\Models;

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

    public function outlet()
    {
        return $this->belongsTo(outlet::class);
    }

    public function pembuat()
    {
        return $this->belongsTo(User::class, 'created_by');
    }
}
