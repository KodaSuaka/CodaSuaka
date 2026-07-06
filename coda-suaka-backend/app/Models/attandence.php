<?php

namespace App\Models;

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

    public function user()
    {
        return $this->belongsTo(User::class);
    }
}
