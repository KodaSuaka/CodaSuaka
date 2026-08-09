# Tiket CodaSuaka

Daftar bug, saran pengembangan, dan fitur yang dilaporkan. Sebelum menambah
tiket baru: cek dulu apakah sudah ada entri dengan topik sama di bawah —
tambahkan komentar/tanggal baru ke entri yang sudah ada, jangan buat ID baru
untuk laporan yang sama. Status: `Open` / `In Progress` / `Done`.

## Bug

| ID | Judul | Deskripsi | Dilaporkan | Status |
|----|-------|-----------|------------|--------|
| BUG-1 | Kategori cuti tahunan → error 422 | Membuat/memilih kategori cuti tahunan menyebabkan error 422 (field `name` invalid). | 2026-07-07 | Done — verified no code mismatch (`jenis` value/field match backend & frontend exactly); likely symptom was the pre-fix unclear 422 message, already resolved by `ErrorMessageMapper` detail extraction. Client enforces `keterangan` ≥10 chars with a clear message. |
| BUG-2 | Permission Owner bocor ke Karyawan setelah app ditutup tanpa logout | Menutup aplikasi (bukan logout) menyisakan permission Owner aktif pada sesi Karyawan berikutnya. | 2026-07-07 | Done — root cause: `AppNavigation`'s AUTH gatekeeper (`onAuthenticated`) selalu navigasi ke `Routes.DASHBOARD` (Owner) tanpa cek role, sedangkan alur Login sudah benar cek role. Jadi resume tanpa logout selalu masuk Dashboard Owner, apapun role user. Fix: `Routes.dashboardForRole(role)` dipakai konsisten di kedua alur (`AuthViewModel`/`AuthScreen` kini membawa role saat Authenticated). |
| BUG-3 | UI state Dashboard error saat tombol back ditekan cepat berulang | Spam-tap tombol back membuat state Dashboard rusak/tidak konsisten. | 2026-07-07 | Done — root cause: tombol back fisik/gestur tidak melewati `safePopBackStack`'s debounce sama sekali (tidak ada `BackHandler` di manapun), jadi spam-tap memicu banyak pop NavHost lebih cepat dari state sempat settle. Fix: `BackHandler` global di `AppNavigation` yang reuse debounce yang sama, `enabled` mengikuti apakah masih ada back-stack entry (supaya keluar app tetap normal di layar akar). |
| BUG-4 | Beberapa button & warna popup kurang responsif | Sejumlah tombol dan warna popup terasa lambat/kurang responsif terhadap input. | 2026-07-07 | Open — investigated: `ClickHelper` global-debounce (fixed sebelumnya) hanya dipakai untuk navigasi, bukan tombol biasa; sample dialog (`KelolaOutletScreen`) sudah benar disable+spinner saat `isSaving`; tidak ditemukan pola sistemik yang cocok. Terlalu vague untuk di-fix tanpa detail: **butuh nama layar spesifik + screenshot** dari tombol/popup yang dimaksud. |
| BUG-5 | Data karyawan tidak tersimpan ke outlet saat diinput dari form outlet | Menambahkan data karyawan lewat form data outlet gagal tersimpan/terhubung ke outlet tsb. | 2026-07-07 | Done — root cause: `OutletDto.toOutlet()` hardcoded `jumlahKaryawan = 0`, backend never returned an employee count. Fixed via `withCount('karyawans')` in `OutletController::index()` + `karyawans_count` field end-to-end. Test: `tests/Feature/Outlet/OutletKaryawanCountTest.php`. |
| BUG-6 | Jam operasional tidak ter-update meski sudah diubah & disubmit | Perubahan jam operasional tidak tersimpan di server walau form berhasil disubmit. | 2026-07-07 | Done — already fixed in commit `ed6df8d` (`UpdateInstansiRequest.php`: `hari_operasional.*` range corrected from `min:0\|max:6` to `min:1\|max:7` to match frontend's `Calendar.DAY_OF_WEEK` convention). Verified current code is consistent both sides. |

## Saran Pengembangan

| ID | Judul | Deskripsi | Dilaporkan | Status |
|----|-------|-----------|------------|--------|
| SUG-1 | Template tugas lebih dinamis | Template tugas dibuat lebih fleksibel sebelum pemilik membuat tugas khusus. | 2026-07-07 | Done — form "Buat Tugas Baru" kini punya chip "Mulai dari template (opsional)" yang mengisi judul/deskripsi/urgency dari template terpilih; pemilik tetap bebas ubah semua field sebelum simpan. Tidak perlu API baru (reuse data `is_template` yang sudah termuat). |
| SUG-2 | Data presensi/pengajuan/divisi/karyawan wajib terikat outlet | Tanpa outlet terdaftar, pembuatan data karyawan/presensi/pengajuan dianggap invalid. | 2026-07-07 | Won't-Do — **bertentangan langsung** dengan fix "Bug #15" yang sudah di-commit (`outlet_id` sengaja dibuat nullable supaya pemilik BISA tambah karyawan sebelum buat outlet). Menerapkan SUG-2 apa adanya akan membalikkan fix tsb dan memunculkan lagi blocker lama. Dikonfirmasi ke pelapor: pertahankan perilaku saat ini (outlet opsional). |
| SUG-3 | Validasi penyelesaian tugas karyawan belum jelas | Perlu notifikasi ke pemilik + validasi manual pada fitur penugasan saat karyawan menandai tugas selesai. | 2026-07-07 | Open |
| SUG-4 | Tambah detail karyawan: masa kerja & tanggal mulai kerja | Data karyawan perlu field masa kerja dan tanggal mulai bekerja. | 2026-07-07 | Open |
| SUG-5 | Laporan keuangan ekspor kurang detail | Hasil ekspor laporan keuangan (PDF/Excel) perlu rincian yang lebih jelas. | 2026-07-07 | Done — Laba Rugi & Arus Kas PDF sudah detail (breakdown per kategori); gap ada di Buku Kas PDF (hanya tabel transaksi flat, tanpa ringkasan). Ditambahkan ringkasan Total Pemasukan/Pengeluaran/Saldo Bersih di atas tabel (`buku_kas_pdf.blade.php` + `LaporanExportService`/`LaporanExportController`). Diverifikasi render via Blade langsung (tanpa DB). |
| SUG-6 | Optimalisasi aplikasi | Permintaan umum untuk optimasi performa aplikasi. | 2026-07-07 | Done (sejauh bisa tanpa data profiling) — sudah ada index DB dari sesi sebelumnya (migration `2026_07_26_000001`); `DashboardController` sudah dioptimasi (4 query agregat, bukan 8, subquery bukan `pluck()`); dashboard karyawan (frontend) sudah diparalelkan via `async`/`await` (lihat sesi debugging sebelumnya). Tidak ditemukan endpoint lambat konkret tanpa data profiling nyata — butuh laporan spesifik (layar mana, berapa lama) untuk optimasi lebih lanjut. |
| SUG-7 | Data transaksi masuk/keluar kurang jelas | Tampilan/detail transaksi kas masuk & keluar perlu diperjelas. | 2026-07-07 | Done — tampilan in-app (`TransaksiCard`) sudah jelas (ikon, warna, +/- per tipe). Gap ada di laporan Buku Kas PDF: kolom "Tipe" + "Nominal" gabungan diganti jadi kolom terpisah **Masuk** / **Keluar** / **Saldo (berjalan)**, format buku kas tradisional. Diverifikasi render via Blade langsung. |

## Fitur Baru

| ID | Judul | Deskripsi | Dilaporkan | Status |
|----|-------|-----------|------------|--------|
| FEAT-1 | Kasir (UMKM) | Fitur keuangan fokus transaksi UMKM: detail barang/jasa keluar-masuk, cetak nota saat penjualan, import nota saat pembelian. | 2026-07-07 | Open |
| FEAT-2 | Cetak nota via printer Bluetooth/jaringan | Interkoneksi ke printer thermal via Bluetooth atau jaringan yang sama untuk cetak nota (tergantung FEAT-1). | 2026-07-07 | Open |
