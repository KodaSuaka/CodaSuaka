<?php

namespace App\Models;

use App\Models\Scopes\TenantScope;
use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;
use Illuminate\Database\Eloquent\Relations\HasMany;

class Nota extends Model
{
    use HasFactory;

    /**
     * Daftar tipe yang valid.
     */
    public const TIPE_VALID = ['penjualan', 'pembelian'];

    /**
     * Daftar status yang valid.
     */
    public const STATUS_VALID = ['selesai', 'dibatalkan'];

    protected $fillable = [
        'instansi_id',
        'outlet_id',
        'kategori_transaksi_id',
        'transaksi_kas_id',
        'tipe',
        'nomor_nota',
        'tanggal',
        'pihak_terkait',
        'metode_pembayaran',
        'total',
        'status',
        'lampiran_url',
        'catatan',
        'created_by',
    ];

    /**
     * jumlah_item (ringkasan) disertakan otomatis di response JSON.
     */
    protected $appends = ['jumlah_item'];

    protected function casts(): array
    {
        return [
            'tanggal' => 'date:Y-m-d',
            'total' => 'decimal:2',
        ];
    }

    protected static function booted(): void
    {
        static::addGlobalScope(new TenantScope('instansi_id'));
    }

    /**
     * Jumlah baris item pada nota. Aman dari N+1: pakai hasil withCount
     * (items_count) atau relasi yang sudah di-load; query hanya sebagai
     * fallback terakhir.
     */
    public function getJumlahItemAttribute(): int
    {
        if (array_key_exists('items_count', $this->attributes)) {
            return (int) $this->attributes['items_count'];
        }
        if ($this->relationLoaded('items')) {
            return $this->items->count();
        }

        return $this->items()->count();
    }

    public function instansi(): BelongsTo
    {
        return $this->belongsTo(Instansi::class);
    }

    public function outlet(): BelongsTo
    {
        return $this->belongsTo(outlet::class);
    }

    public function kategoriTransaksi(): BelongsTo
    {
        return $this->belongsTo(KategoriTransaksi::class);
    }

    public function transaksiKas(): BelongsTo
    {
        return $this->belongsTo(TransaksiKas::class);
    }

    public function items(): HasMany
    {
        return $this->hasMany(NotaItem::class);
    }

    public function createdByUser(): BelongsTo
    {
        return $this->belongsTo(User::class, 'created_by');
    }
}
