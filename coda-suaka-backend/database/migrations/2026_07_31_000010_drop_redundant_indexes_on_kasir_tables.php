<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

/**
 * Hapus index redundan pada tabel kasir, konsisten dengan pembersihan yang
 * sama di penugasans/attandences (2026_07_31_000001).
 *
 * Keduanya adalah leftmost-prefix dari index lain yang lebih panjang pada
 * tabel yang sama, jadi MySQL sudah bisa memakainya lewat index yang
 * dipertahankan. Tidak mempercepat SELECT apa pun, tapi tetap harus
 * di-update setiap INSERT/UPDATE — beban tulis di jalur panas kasir
 * (setiap nota penjualan/pembelian menulis ke notas + nota_items).
 *
 *   notas.notas_instansi_id_index (instansi_id)
 *     -> prefix dari notas_instansi_id_tipe_tanggal_index
 *        (instansi_id, tipe, tanggal) DAN dari
 *        notas_instansi_id_nomor_nota_unique (instansi_id, nomor_nota)
 *
 *   barang_jasas.barang_jasas_instansi_id_index (instansi_id)
 *     -> prefix dari barang_jasas_instansi_id_jenis_index
 *        (instansi_id, jenis)
 *
 * Tidak ada foreign key pada kolom instansi_id di kedua tabel yang
 * kehilangan index pendukung: instansi_id tetap jadi kolom terkiri pada
 * index yang dipertahankan.
 */
return new class extends Migration
{
    private const REDUNDANT = [
        'notas' => 'notas_instansi_id_index',
        'barang_jasas' => 'barang_jasas_instansi_id_index',
    ];

    public function up(): void
    {
        foreach (self::REDUNDANT as $table => $index) {
            if (Schema::hasTable($table) && Schema::hasIndex($table, $index)) {
                Schema::table($table, function (Blueprint $blueprint) use ($index) {
                    $blueprint->dropIndex($index);
                });
            }
        }
    }

    public function down(): void
    {
        foreach (self::REDUNDANT as $table => $index) {
            if (Schema::hasTable($table) && ! Schema::hasIndex($table, $index)) {
                Schema::table($table, function (Blueprint $blueprint) use ($index) {
                    $blueprint->index('instansi_id', $index);
                });
            }
        }
    }
};
