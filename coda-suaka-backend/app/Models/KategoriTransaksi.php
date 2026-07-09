<?php

namespace App\Models;

use App\Models\Scopes\TenantScope;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Factories\HasFactory;

class KategoriTransaksi extends Model
{
    use HasFactory;

    protected $table = 'kategori_transaksis';

    protected $fillable = [
        'instansi_id',
        'nama_kategori',
        'tipe',
        'sifat',
        'termasuk_hpp',
        'is_default',
        'is_active',
    ];

    protected function casts(): array
    {
        return [
            'termasuk_hpp' => 'boolean',
            'is_default' => 'boolean',
            'is_active' => 'boolean',
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

    public function transaksiKas()
    {
        return $this->hasMany(TransaksiKas::class, 'kategori_transaksi_id');
    }
}
