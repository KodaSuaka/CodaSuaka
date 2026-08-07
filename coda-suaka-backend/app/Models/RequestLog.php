<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class RequestLog extends Model
{
    protected $fillable = [
        'instansi_id',
        'user_id',
        'method',
        'path',
        'full_url',
        'query_params',
        'request_body',
        'ip_address',
        'user_agent',
        'status_code',
        'duration_ms',
    ];

    protected function casts(): array
    {
        return [
            'query_params' => 'array',
            'request_body' => 'array',
            'status_code' => 'integer',
            'duration_ms' => 'integer',
        ];
    }

    public function user()
    {
        return $this->belongsTo(User::class, 'user_id');
    }
}
