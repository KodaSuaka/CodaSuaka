<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Concerns\HasUuids;
use Illuminate\Database\Eloquent\Model;

class instansi extends Model
{
    use HasUuids;

    protected $fillable = [
        'nama_instansi',
        'paket_id',
    ];
}
