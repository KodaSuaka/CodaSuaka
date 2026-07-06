<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class transaksi_paket extends Model
{
    protected $fillable = [
        'instansi_id',
        'paket_id',
        'tanggal_mulai',
        'tanggal_berakhir',
        'total_harga',
        'status',
        'bukti_pembayaran',
    ];

    protected function casts(): array
    {
        return [
            'tanggal_mulai' => 'date',
            'tanggal_berakhir' => 'date',
            'total_harga' => 'decimal:2',
        ];
    }

    public function instansi()
    {
        return $this->belongsTo(instansi::class);
    }

    public function paket()
    {
        return $this->belongsTo(paket::class);
    }
}
