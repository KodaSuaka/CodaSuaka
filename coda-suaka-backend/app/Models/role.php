<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class role extends Model
{
    protected $fillable = [
        'nama_role',
    ];

    public function users()
    {
        return $this->hasMany(User::class);
    }

    public function permissions()
    {
        return $this->hasMany(role_permission::class, 'role_id');
    }
}
