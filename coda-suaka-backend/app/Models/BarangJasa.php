<?php

namespace App\Models;

use App\Models\Scopes\TenantScope;
use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\HasMany;

class BarangJasa extends Model
{
    use HasFactory;

    /**
     * Daftar jenis yang valid.
     */
    public const JENIS_VALID = ['barang', 'jasa'];

    protected $fillable = [
        'instansi_id',
        'nama',
        'jenis',
        'kategori',
        'satuan',
        'harga_jual',
        'harga_beli',
        'stok',
        'is_active',
        'keterangan',
    ];

    protected function casts(): array
    {
        return [
            'harga_jual' => 'decimal:2',
            'harga_beli' => 'decimal:2',
            'is_active' => 'boolean',
        ];
    }

    protected static function booted(): void
    {
        static::addGlobalScope(new TenantScope('instansi_id'));
    }

    public function notaItems(): HasMany
    {
        return $this->hasMany(NotaItem::class);
    }
}
