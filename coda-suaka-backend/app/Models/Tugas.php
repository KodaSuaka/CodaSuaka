<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Concerns\HasUuids;

class Tugas extends Model
{
    use HasUuids;

    protected $table = 'tugas';
    public $incrementing = false;
    protected $keyType = 'string';
    const UPDATED_AT = null; 

    protected $fillable = ['id_Toko', 'nama_tugas', 'poin'];
}