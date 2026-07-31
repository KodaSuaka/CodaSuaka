<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class NotaItem extends Model
{
    use HasFactory;

    protected $fillable = [
        'nota_id',
        'barang_jasa_id',
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

    public function nota(): BelongsTo
    {
        return $this->belongsTo(Nota::class);
    }

    public function barangJasa(): BelongsTo
    {
        return $this->belongsTo(BarangJasa::class);
    }
}
