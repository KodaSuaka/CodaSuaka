<?php

namespace App\Models;

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
