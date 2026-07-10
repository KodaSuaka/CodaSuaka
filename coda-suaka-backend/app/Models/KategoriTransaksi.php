<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Builder;
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

    // ─── Scopes ────────────────────────────────────────────────

    /**
     * Scope untuk menampilkan kategori template global (instansi_id = null)
     * digabung dengan kategori custom milik instansi tertentu.
     */
    public function scopeForInstansi(Builder $query, string $instansiId): Builder
    {
        return $query->whereNull('instansi_id')
            ->orWhere('instansi_id', $instansiId);
    }

    /**
     * Scope hanya kategori non-global (milik instansi tertentu).
     */
    public function scopeCustomOnly(Builder $query): Builder
    {
        return $query->whereNotNull('instansi_id');
    }

    /**
     * Scope hanya kategori template global.
     */
    public function scopeGlobalOnly(Builder $query): Builder
    {
        return $query->whereNull('instansi_id');
    }

    // ─── Relasi ────────────────────────────────────────────────

    public function instansi()
    {
        return $this->belongsTo(instansi::class);
    }

    public function transaksiKas()
    {
        return $this->hasMany(TransaksiKas::class, 'kategori_transaksi_id');
    }

    // ─── Helper ────────────────────────────────────────────────

    /**
     * Cek apakah kategori ini adalah template global.
     */
    public function isGlobal(): bool
    {
        return $this->instansi_id === null;
    }

    /**
     * Cek apakah kategori ini milik instansi tertentu.
     */
    public function isCustom(): bool
    {
        return $this->instansi_id !== null;
    }
}
