<?php

namespace App\Models;

use App\Models\Scopes\TenantScope;
use Illuminate\Database\Eloquent\Builder;
use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class NotaItem extends Model
{
    use HasFactory;

    protected $fillable = [
        'nota_id',
        'barang_jasa_id',
        'stok_id',
        'nama_item',
        'jenis',
        'kuantitas',
        'satuan',
        'harga_satuan',
        'subtotal',
    ];

    protected function casts(): array
    {
        return [
            'kuantitas' => 'decimal:2',
            'harga_satuan' => 'decimal:2',
            'subtotal' => 'decimal:2',
        ];
    }

    /**
     * NotaItem tidak menyimpan instansi_id sendiri — tenant-nya ditentukan
     * lewat nota induknya. Tanpa scope ini, query langsung NotaItem::where()
     * akan menembus batas instansi.
     */
    protected static function booted(): void
    {
        static::addGlobalScope(new TenantScope(function (Builder $builder, $user) {
            $builder->whereHas('nota', function (Builder $q) use ($user) {
                $q->where('instansi_id', $user->instansi_id);
            });
        }));
    }

    public function nota(): BelongsTo
    {
        return $this->belongsTo(Nota::class);
    }

    public function stok(): BelongsTo
    {
        return $this->belongsTo(Stok::class);
    }

    public function barangJasa(): BelongsTo
    {
        return $this->belongsTo(BarangJasa::class);
    }
}
