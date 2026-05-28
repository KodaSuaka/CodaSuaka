<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Concerns\HasUuids;

class PenugasanKaryawan extends Model
{
    use HasUuids;

    protected $table = 'penugasan_karyawan';
    public $incrementing = false;
    protected $keyType = 'string';
    public $timestamps = false; // Matikan karena ERD pakai custom timestamp

    protected $fillable = [
        'id_karyawan', 
        'id_master_tugas', 
        'tanggal_tugas', 
        'status', 
        'catatan_tambahan', 
        'waktu_diselesaikan'
    ];

    // Relasi penting untuk menarik nama tugas dan poin
    public function masterTugas()
    {
        return $this->belongsTo(Tugas::class, 'id_master_tugas');
    }
}