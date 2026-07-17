<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Factories\HasFactory;

class ApprovalLog extends Model
{
    use HasFactory;

    protected $table = 'approval_logs';

    protected $fillable = [
        'transaksi_kas_id',
        'diajukan_oleh',
        'disetujui_oleh',
        'status',
        'catatan',
        'tanggal_diajukan',
        'tanggal_diproses',
    ];

    protected function casts(): array
    {
        return [
            'tanggal_diajukan' => 'datetime',
            'tanggal_diproses' => 'datetime',
        ];
    }

    /**
     * Relasi ke transaksi kas yang diajukan.
     */
    public function transaksiKas()
    {
        return $this->belongsTo(TransaksiKas::class, 'transaksi_kas_id');
    }

    /**
     * User yang mengajukan transaksi untuk approval.
     */
    public function pengaju()
    {
        return $this->belongsTo(User::class, 'diajukan_oleh');
    }

    /**
     * User yang menyetujui/menolak transaksi.
     */
    public function pemeriksa()
    {
        return $this->belongsTo(User::class, 'disetujui_oleh');
    }
}
