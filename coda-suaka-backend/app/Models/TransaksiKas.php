<?php

namespace App\Models;

use App\Models\Scopes\TenantScope;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Factories\HasFactory;

class TransaksiKas extends Model
{
    use HasFactory;

    protected $table = 'transaksi_kas';

    protected $fillable = [
        'instansi_id',
        'outlet_id',
        'kategori_transaksi_id',
        'tanggal',
        'tipe',
        'nominal',
        'metode_pembayaran',
        'keterangan',
        'lampiran_url',
        'dokumen_transaksi_id',
        'created_by',
    ];

    protected function casts(): array
    {
        return [
            'tanggal' => 'date',
            'nominal' => 'decimal:2',
        ];
    }

    protected static function booted(): void
    {
        static::addGlobalScope(new TenantScope('instansi_id'));
    }

    public function instansi()
    {
        return $this->belongsTo(instansi::class);
    }

    public function outlet()
    {
        return $this->belongsTo(outlet::class);
    }

    public function kategoriTransaksi()
    {
        return $this->belongsTo(KategoriTransaksi::class, 'kategori_transaksi_id');
    }

    public function createdByUser()
    {
        return $this->belongsTo(User::class, 'created_by');
    }
}
