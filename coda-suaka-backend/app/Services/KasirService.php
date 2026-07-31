<?php

namespace App\Services;

use App\Models\BarangJasa;
use App\Models\KategoriTransaksi;
use App\Models\Nota;
use App\Models\NotaItem;
use App\Models\TransaksiKas;
use App\Models\User;
use Illuminate\Support\Facades\DB;
use Illuminate\Validation\ValidationException;

class KasirService
{
    /**
     * Buat Nota Penjualan: validasi stok, buat Nota + NotaItem, kurangi
     * stok, dan buat TransaksiKas masuk yang terhubung ke nota.
     */
    public function buatNotaPenjualan(array $data, User $user): Nota
    {
        return DB::transaction(function () use ($data, $user) {
            $items = $data['items'];

            // 1. Validasi stok cukup untuk semua item jenis=barang yang menunjuk katalog.
            // 'jenis' boleh tidak dikirim caller saat barang_jasa_id ada (lihat
            // StoreNotaRequest: items.*.jenis required_without barang_jasa_id) — jadi
            // fallback ke katalog di sini juga, sama seperti langkah 2, supaya
            // pengecekan stok tidak ke-skip untuk request yang valid.
            foreach ($items as $item) {
                if ($item['barang_jasa_id'] ?? null) {
                    $barangJasa = BarangJasa::findOrFail($item['barang_jasa_id']);
                    $jenis = $item['jenis'] ?? $barangJasa->jenis;
                    if ($jenis === 'barang' && $barangJasa->stok !== null && $barangJasa->stok < $item['kuantitas']) {
                        throw ValidationException::withMessages([
                            'items' => "Stok {$barangJasa->nama} tidak cukup (tersisa {$barangJasa->stok}).",
                        ]);
                    }
                }
            }

            // 2. Hitung total & buat NotaItem snapshot
            $total = 0;
            $itemRows = [];
            foreach ($items as $item) {
                $barangJasa = ($item['barang_jasa_id'] ?? null) ? BarangJasa::find($item['barang_jasa_id']) : null;
                $namaItem = $item['nama_item'] ?? $barangJasa?->nama;
                $jenis = $item['jenis'] ?? $barangJasa?->jenis;
                $satuan = $item['satuan'] ?? $barangJasa?->satuan ?? 'pcs';
                $hargaSatuan = $item['harga_satuan'];
                $subtotal = $item['kuantitas'] * $hargaSatuan;
                $total += $subtotal;
                $itemRows[] = compact('barangJasa', 'namaItem', 'jenis', 'satuan', 'hargaSatuan', 'subtotal', 'item');
            }

            // 3. Nomor nota: sequence harian per instansi
            $tanggal = $data['tanggal'];
            $countHariIni = Nota::where('instansi_id', $user->instansi_id)
                ->where('tipe', 'penjualan')
                ->whereDate('created_at', now())
                ->count();
            $nomorNota = sprintf('PJL-%s-%04d', now()->format('Ymd'), $countHariIni + 1);

            // 4. Kategori default: "Penjualan Barang" kecuali caller override
            $kategoriId = $data['kategori_transaksi_id']
                ?? KategoriTransaksi::whereNull('instansi_id')->where('nama_kategori', 'Penjualan Barang')->value('id');
            if (! $kategoriId) {
                throw new \RuntimeException('Kategori default "Penjualan Barang" tidak ditemukan — cek KategoriTransaksiSeeder.');
            }

            // 5. Buat Nota
            $nota = Nota::create([
                'instansi_id' => $user->instansi_id,
                'outlet_id' => $data['outlet_id'] ?? null,
                'kategori_transaksi_id' => $kategoriId,
                'tipe' => 'penjualan',
                'nomor_nota' => $nomorNota,
                'tanggal' => $tanggal,
                'pihak_terkait' => $data['pihak_terkait'] ?? null,
                'metode_pembayaran' => $data['metode_pembayaran'] ?? null,
                'total' => $total,
                'status' => 'selesai',
                'catatan' => $data['catatan'] ?? null,
                'created_by' => $user->id,
            ]);

            // 6. Buat NotaItem + kurangi stok
            foreach ($itemRows as $row) {
                NotaItem::create([
                    'nota_id' => $nota->id,
                    'barang_jasa_id' => $row['barangJasa']?->id,
                    'nama_item' => $row['namaItem'],
                    'jenis' => $row['jenis'],
                    'kuantitas' => $row['item']['kuantitas'],
                    'satuan' => $row['satuan'],
                    'harga_satuan' => $row['hargaSatuan'],
                    'subtotal' => $row['subtotal'],
                ]);
                if ($row['barangJasa'] && $row['jenis'] === 'barang' && $row['barangJasa']->stok !== null) {
                    $row['barangJasa']->decrement('stok', $row['item']['kuantitas']);
                }
            }

            // 7. Buat TransaksiKas masuk (income tidak pernah butuh approval — lihat Global Constraint #2)
            $transaksiKas = TransaksiKas::create([
                'instansi_id' => $user->instansi_id,
                'outlet_id' => $data['outlet_id'] ?? null,
                'kategori_transaksi_id' => $kategoriId,
                'tanggal' => $tanggal,
                'tipe' => 'masuk',
                'nominal' => $total,
                'metode_pembayaran' => $data['metode_pembayaran'] ?? null,
                'keterangan' => "Penjualan {$nomorNota}",
                'dokumen_transaksi_id' => $nota->id,
                'created_by' => $user->id,
            ]);

            $nota->update(['transaksi_kas_id' => $transaksiKas->id]);

            app(AuditService::class)->created($nota, $user);

            return $nota->load('items');
        });
    }
}
