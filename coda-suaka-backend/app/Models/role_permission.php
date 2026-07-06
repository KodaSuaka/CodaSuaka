<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class role_permission extends Model
{
    protected $fillable = [
        'role_id',
        'permission',
    ];

    public function role()
    {
        return $this->belongsTo(role::class);
    }
}
