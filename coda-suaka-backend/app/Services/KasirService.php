<?php

namespace App\Services;

use App\Models\BarangJasa;
use App\Models\KategoriTransaksi;
use App\Models\Nota;
use App\Models\NotaItem;
use App\Models\TransaksiKas;
use App\Models\User;
use Illuminate\Http\UploadedFile;
use Illuminate\Support\Facades\DB;
use Illuminate\Validation\ValidationException;
use OpenSpout\Reader\XLSX\Reader;

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

    /**
     * Buat Nota Pembelian: buat Nota + NotaItem, tambah stok, simpan
     * lampiran (jika ada), dan buat TransaksiKas keluar yang mengikuti
     * alur approval existing (lihat TransaksiKasController::store()).
     */
    public function buatNotaPembelian(array $data, User $user, ?UploadedFile $sourceFile = null): Nota
    {
        return DB::transaction(function () use ($data, $user, $sourceFile) {
            $items = $data['items'];

            // 1. Hitung total & buat NotaItem snapshot. Tidak ada
            // validasi stok-cukup untuk pembelian — menambah stok apa pun kondisinya.
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

            // 2. Nomor nota: sequence harian per instansi
            $tanggal = $data['tanggal'];
            $countHariIni = Nota::where('instansi_id', $user->instansi_id)
                ->where('tipe', 'pembelian')
                ->whereDate('created_at', now())
                ->count();
            $nomorNota = sprintf('PBL-%s-%04d', now()->format('Ymd'), $countHariIni + 1);

            // 3. Kategori default: "Pembelian Bahan/Stok" kecuali caller override
            $kategoriId = $data['kategori_transaksi_id']
                ?? KategoriTransaksi::whereNull('instansi_id')->where('nama_kategori', 'Pembelian Bahan/Stok')->value('id');
            if (! $kategoriId) {
                throw new \RuntimeException('Kategori default "Pembelian Bahan/Stok" tidak ditemukan — cek KategoriTransaksiSeeder.');
            }

            // 4. Simpan lampiran (opsional — entri manual tidak punya file)
            $lampiranUrl = null;
            if ($sourceFile) {
                $lampiranUrl = $sourceFile->store('nota-imports/'.$user->instansi_id, 'public');
            }

            // 5. Buat Nota
            $nota = Nota::create([
                'instansi_id' => $user->instansi_id,
                'outlet_id' => $data['outlet_id'] ?? null,
                'kategori_transaksi_id' => $kategoriId,
                'tipe' => 'pembelian',
                'nomor_nota' => $nomorNota,
                'tanggal' => $tanggal,
                'pihak_terkait' => $data['pihak_terkait'] ?? null,
                'metode_pembayaran' => $data['metode_pembayaran'] ?? null,
                'total' => $total,
                'status' => 'selesai',
                'lampiran_url' => $lampiranUrl,
                'catatan' => $data['catatan'] ?? null,
                'created_by' => $user->id,
            ]);

            // 6. Buat NotaItem + tambah stok
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
                    $row['barangJasa']->increment('stok', $row['item']['kuantitas']);
                }
            }

            // 7. Buat TransaksiKas keluar — reuse alur approval existing (lihat
            // TransaksiKasController::store()). JANGAN kirim 'status_approval' di
            // sini: kolom itu tidak ada di TransaksiKas::$fillable.
            $transaksiKas = TransaksiKas::create([
                'instansi_id' => $user->instansi_id,
                'outlet_id' => $data['outlet_id'] ?? null,
                'kategori_transaksi_id' => $kategoriId,
                'tanggal' => $tanggal,
                'tipe' => 'keluar',
                'nominal' => $total,
                'metode_pembayaran' => $data['metode_pembayaran'] ?? null,
                'keterangan' => "Pembelian {$nomorNota}",
                'dokumen_transaksi_id' => $nota->id,
                'created_by' => $user->id,
            ]);

            if (app(ApprovalService::class)->perluApproval($transaksiKas)) {
                app(ApprovalService::class)->ajukanApproval($transaksiKas, $user);

                $pemeriksaIds = User::where('instansi_id', $user->instansi_id)
                    ->whereHas('role.permissions', fn ($q) => $q->where('permission', 'approve:keuangan'))
                    ->pluck('id');

                foreach ($pemeriksaIds as $pemeriksaId) {
                    app(NotificationService::class)->onTransaksiPendingApproval(
                        $transaksiKas->id,
                        $pemeriksaId,
                        $transaksiKas->keterangan
                    );
                }
            }

            $nota->update(['transaksi_kas_id' => $transaksiKas->id]);

            app(AuditService::class)->created($nota, $user);

            return $nota->load('items');
        });
    }

    /**
     * Impor Nota Pembelian dari file .xlsx. Template kolom yang diharapkan:
     * nama_item | jenis | kuantitas | satuan | harga_satuan
     * Baris pertama = header, dilewati.
     */
    public function importNotaPembelian(UploadedFile $file, array $header, User $user): Nota
    {
        $reader = new Reader;
        $reader->open($file->getRealPath());

        $items = [];
        foreach ($reader->getSheetIterator() as $sheet) {
            foreach ($sheet->getRowIterator() as $i => $row) {
                if ($i === 1) {
                    continue; // baris 1 = header kolom
                }
                $cells = $row->toArray();
                if (empty($cells[0])) {
                    continue; // baris kosong di akhir file
                }
                $items[] = [
                    'nama_item' => (string) $cells[0],
                    'jenis' => (string) $cells[1],
                    'kuantitas' => (float) $cells[2],
                    'satuan' => (string) ($cells[3] ?? 'pcs'),
                    'harga_satuan' => (float) $cells[4],
                ];
            }
        }
        $reader->close();

        if (empty($items)) {
            throw ValidationException::withMessages(['file' => 'File tidak berisi baris item yang valid.']);
        }
        foreach ($items as $i => $item) {
            if (! in_array($item['jenis'], ['barang', 'jasa'], true)) {
                throw ValidationException::withMessages(['file' => 'Baris '.($i + 2).": jenis harus 'barang' atau 'jasa'."]);
            }
            if ($item['kuantitas'] <= 0 || $item['harga_satuan'] < 0) {
                throw ValidationException::withMessages(['file' => 'Baris '.($i + 2).': kuantitas/harga tidak valid.']);
            }
        }

        return $this->buatNotaPembelian(array_merge($header, ['items' => $items]), $user, $file);
    }
}
