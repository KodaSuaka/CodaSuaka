<?php

namespace App\Services;

use App\Models\BarangJasa;
use App\Models\KategoriTransaksi;
use App\Models\Nota;
use App\Models\NotaItem;
use App\Models\TransaksiKas;
use App\Models\User;
use Illuminate\Database\QueryException;
use Illuminate\Http\UploadedFile;
use Illuminate\Support\Facades\DB;
use Illuminate\Validation\ValidationException;
use OpenSpout\Reader\XLSX\Reader;

class KasirService
{
    /**
     * Jumlah percobaan maksimum saat nomor_nota bentrok (unique constraint)
     * akibat dua checkout bersamaan membaca count yang sama.
     */
    private const MAX_NOMOR_NOTA_ATTEMPTS = 3;

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

            // 3. Kategori default: "Penjualan Barang" kecuali caller override
            $tanggal = $data['tanggal'];
            $kategoriId = $data['kategori_transaksi_id']
                ?? KategoriTransaksi::whereNull('instansi_id')->where('nama_kategori', 'Penjualan Barang')->value('id');
            if (! $kategoriId) {
                throw new \RuntimeException('Kategori default "Penjualan Barang" tidak ditemukan — cek KategoriTransaksiSeeder.');
            }

            // 4. Buat Nota. Nomor nota (sequence harian per instansi) di-generate
            // & di-retry di dalam buatNotaDenganNomorUnik() karena dua checkout
            // bersamaan bisa membaca count yang sama sebelum salah satu commit.
            $nota = $this->buatNotaDenganNomorUnik('PJL', [
                'instansi_id' => $user->instansi_id,
                'outlet_id' => $data['outlet_id'] ?? null,
                'kategori_transaksi_id' => $kategoriId,
                'tipe' => 'penjualan',
                'tanggal' => $tanggal,
                'pihak_terkait' => $data['pihak_terkait'] ?? null,
                'metode_pembayaran' => $data['metode_pembayaran'] ?? null,
                'total' => $total,
                'status' => 'selesai',
                'catatan' => $data['catatan'] ?? null,
                'created_by' => $user->id,
            ]);

            // 5. Buat NotaItem + kurangi stok
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

            // 6. Buat TransaksiKas masuk (income tidak pernah butuh approval — lihat Global Constraint #2)
            $transaksiKas = TransaksiKas::create([
                'instansi_id' => $user->instansi_id,
                'outlet_id' => $data['outlet_id'] ?? null,
                'kategori_transaksi_id' => $kategoriId,
                'tanggal' => $tanggal,
                'tipe' => 'masuk',
                'nominal' => $total,
                'metode_pembayaran' => $data['metode_pembayaran'] ?? null,
                'keterangan' => "Penjualan {$nota->nomor_nota}",
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

            // 2. Kategori default: "Pembelian Bahan/Stok" kecuali caller override
            $tanggal = $data['tanggal'];
            $kategoriId = $data['kategori_transaksi_id']
                ?? KategoriTransaksi::whereNull('instansi_id')->where('nama_kategori', 'Pembelian Bahan/Stok')->value('id');
            if (! $kategoriId) {
                throw new \RuntimeException('Kategori default "Pembelian Bahan/Stok" tidak ditemukan — cek KategoriTransaksiSeeder.');
            }

            // 3. Simpan lampiran (opsional — entri manual tidak punya file)
            $lampiranUrl = null;
            if ($sourceFile) {
                $lampiranUrl = $sourceFile->store('nota-imports/'.$user->instansi_id, 'public');
            }

            // 4. Buat Nota. Nomor nota (sequence harian per instansi) di-generate
            // & di-retry di dalam buatNotaDenganNomorUnik() karena dua checkout
            // bersamaan bisa membaca count yang sama sebelum salah satu commit.
            $nota = $this->buatNotaDenganNomorUnik('PBL', [
                'instansi_id' => $user->instansi_id,
                'outlet_id' => $data['outlet_id'] ?? null,
                'kategori_transaksi_id' => $kategoriId,
                'tipe' => 'pembelian',
                'tanggal' => $tanggal,
                'pihak_terkait' => $data['pihak_terkait'] ?? null,
                'metode_pembayaran' => $data['metode_pembayaran'] ?? null,
                'total' => $total,
                'status' => 'selesai',
                'lampiran_url' => $lampiranUrl,
                'catatan' => $data['catatan'] ?? null,
                'created_by' => $user->id,
            ]);

            // 5. Buat NotaItem + tambah stok
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

            // 6. Buat TransaksiKas keluar — reuse alur approval existing (lihat
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
                'keterangan' => "Pembelian {$nota->nomor_nota}",
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
     * Generate nomor_nota (sequence harian per instansi+tipe, mis. PJL-20260731-0001)
     * lalu panggil Nota::create() dengan retry saat terjadi race condition pada
     * unique(['instansi_id','nomor_nota']) — dua checkout bersamaan bisa membaca
     * count harian yang sama sebelum salah satunya commit. Retry membungkus
     * generate-nomor + create() sekaligus (bukan cuma generate nomor) karena
     * celah race-nya ada di antara baca-count dan insert, bukan hanya di
     * pembacaan count itu sendiri.
     *
     * $attributes wajib berisi 'instansi_id' dan 'tipe'; 'nomor_nota' akan
     * ditimpa oleh method ini.
     */
    private function buatNotaDenganNomorUnik(string $prefix, array $attributes): Nota
    {
        for ($attempt = 1; $attempt <= self::MAX_NOMOR_NOTA_ATTEMPTS; $attempt++) {
            $countHariIni = Nota::where('instansi_id', $attributes['instansi_id'])
                ->where('tipe', $attributes['tipe'])
                ->whereDate('created_at', now())
                ->count();
            // +$attempt (bukan cuma +1): di isolation level REPEATABLE READ (default
            // MySQL), SELECT count() pada percobaan retry bisa saja masih memakai
            // snapshot yang sama dengan percobaan pertama dan belum "melihat" commit
            // dari transaksi lain yang barusan menang race — kalau cuma +1, retry
            // bisa menghasilkan nomor yang sama persis dan bentrok lagi. +$attempt
            // menjamin tiap percobaan mencoba slot berikutnya walau count() belum berubah.
            $nomorNota = sprintf('%s-%s-%04d', $prefix, now()->format('Ymd'), $countHariIni + $attempt);

            try {
                return Nota::create(array_merge($attributes, ['nomor_nota' => $nomorNota]));
            } catch (QueryException $e) {
                $isDuplicateKey = $e->getCode() === '23000';
                if (! $isDuplicateKey || $attempt >= self::MAX_NOMOR_NOTA_ATTEMPTS) {
                    throw $e;
                }
                // nomor_nota bentrok (race condition antar checkout) — ulangi dengan nomor baru
            }
        }

        // Tidak pernah tercapai: loop di atas selalu return atau throw pada percobaan terakhir.
        throw new \RuntimeException('Gagal membuat nomor nota unik setelah '.self::MAX_NOMOR_NOTA_ATTEMPTS.' percobaan.');
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

    /**
     * Hapus Nota: balikkan stok, hapus TransaksiKas terkait, lalu hapus Nota.
     *
     * Guard TIDAK boleh meniru TransaksiKasController::destroy() secara
     * harfiah (blok jika status_approval === 'disetujui') — transaksi masuk
     * (penjualan) selalu default 'disetujui' tanpa pernah lewat approval,
     * jadi guard itu akan membuat semua nota penjualan permanen tidak bisa
     * dihapus. Aturan yang benar: blok hanya jika ada ApprovalLog dengan
     * status 'disetujui' yang sungguhan dibuat lewat alur approval manusia.
     */
    public function hapusNota(Nota $nota): void
    {
        $sudahDisetujuiManusia = $nota->transaksiKas
            ?->approvalLogs()
            ->where('status', 'disetujui')
            ->exists() ?? false;

        if ($sudahDisetujuiManusia) {
            throw ValidationException::withMessages([
                'nota' => 'Nota ini sudah disetujui melalui alur approval dan tidak bisa dihapus langsung.',
            ]);
        }

        DB::transaction(function () use ($nota) {
            foreach ($nota->items as $item) {
                if ($item->barangJasa && $item->jenis === 'barang' && $item->barangJasa->stok !== null) {
                    $delta = $nota->tipe === 'penjualan' ? $item->kuantitas : -$item->kuantitas;
                    $item->barangJasa->increment('stok', $delta);
                }
            }
            $nota->transaksiKas?->delete();
            $user = auth()->user();
            app(AuditService::class)->deleted($nota, $user);
            $nota->delete(); // cascade ke nota_items via FK
        });
    }
}
