<?php

namespace App\Models;

use App\Models\Scopes\TenantScope;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Factories\HasFactory;

class TransaksiKas extends Model
{
    use HasFactory;

    /**
     * Daftar metode pembayaran yang valid.
     */
    public const METODE_PEMBAYARAN_VALID = [
        'Tunai', 'Transfer', 'QRIS', 'Kartu Kredit', 'Kartu Debit', 'Lainnya',
    ];

    /**
     * Nominal maksimum yang diizinkan.
     */
    public const NOMINAL_MAX = 999999999999.99;

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
            'status_approval' => 'string',
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

    /**
     * Log approval untuk transaksi ini.
     */
    public function approvalLogs()
    {
        return $this->hasMany(\App\Models\ApprovalLog::class, 'transaksi_kas_id');
    }

    /**
     * Cek apakah transaksi perlu approval.
     */
    public function needsApproval(): bool
    {
        $config = config('keuangan.approval');
        if (!$config['enabled']) {
            return false;
        }

        // Cek tipe
        $tipePerluApproval = $config['tipe_perlu_approval'];
        if (!in_array($this->tipe, (array) $tipePerluApproval)) {
            return false;
        }

        // Cek threshold nominal
        if ((float) $this->nominal < (float) $config['threshold_nominal']) {
            return false;
        }

        return true;
    }
}
