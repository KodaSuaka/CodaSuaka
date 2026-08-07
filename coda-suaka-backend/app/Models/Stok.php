<?php

namespace App\Models;

use App\Models\Scopes\TenantScope;
use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\HasMany;

class Stok extends Model
{
    use HasFactory;

    /**
     * Jenis mutasi stok yang valid.
     */
    public const JENIS_MUTASI_VALID = ['masuk', 'keluar', 'penyesuaian'];

    protected $fillable = [
        'instansi_id',
        'nama',
        'kategori',
        'satuan',
        'stok',
        'stok_minimum',
        'harga_beli',
        'is_active',
        'keterangan',
    ];

    protected function casts(): array
    {
        return [
            'stok' => 'decimal:2',
            'stok_minimum' => 'decimal:2',
            'harga_beli' => 'decimal:2',
            'is_active' => 'boolean',
        ];
    }

    protected static function booted(): void
    {
        static::addGlobalScope(new TenantScope('instansi_id'));
    }

    public function mutations(): HasMany
    {
        return $this->hasMany(StokMutation::class, 'stok_id');
    }
}
