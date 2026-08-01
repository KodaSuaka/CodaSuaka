# Rencana: Export + Import Excel Laporan Keuangan (Laba Rugi & Arus Kas)

> ## ✅ STATUS: EXPORT SELESAI (2026-08-02)
> Fase 2, 3, 5-export, 6-export **selesai & hijau** (9 test, bagian dari suite 119 passed). Yang dibangun:
> - `app/Support/LaporanTemplateMap.php` — peta 4 sheet (laba rugi barang/jasa, arus kas barang/jasa), formula & koordinat diambil dari file template.
> - `app/Services/LaporanTemplateExportService.php` — render OpenSpout: prefill input dari total transaksi per kategori, sel formula asli (FormulaCell), style kuning/biru.
> - `app/Http/Controllers/LaporanKeuanganController.php` + `ExportTemplateLaporanRequest` + route `GET /api/laporan-keuangan/template/export`.
> - Izin baru `export:laporan-keuangan` (Owner + Keuangan) di `config/permissions.php`.
> - Test: `tests/Feature/Laporan/*` (struktur, prefill, formula lintas-sel di baris absolut, endpoint 200/403/422).
>
> **WAJIB saat deploy:** `php artisan permissions:sync` (mendorong izin baru dari config ke DB). Tanpa ini, Owner/Keuangan belum punya `export:laporan-keuangan` di produksi → 403.
>
> **Catatan teknis penting:** sheet Arus Kas punya BARIS 28 KOSONG (sengaja) supaya RINGKASAN mulai baris 29 & formula `C34=C30+C31+C32+C33` menunjuk sel benar. Jangan hapus baris kosong itu di peta.
>
> **Ditunda (backlog):** re-import, model penyimpanan, computedValue pada formula (Excel menghitung sendiri saat dibuka). Lihat fase-fase ⏸️ di bawah.

---



**Scope AKTIF (dikunci bersama user, 2026-08-02):** Export saja.
- **Export** template ter-isi sebagian dari data transaksi sistem, mengikuti layout persis template divisi keuangan. Keuangan lalu menyesuaikan angka manual (penyusutan, persediaan, dll) di luar app.

**DITUNDA ke backlog** (task "[BACKLOG] Re-import template laporan keuangan"): re-import file final untuk disimpan sebagai laporan resmi. Belum diperlukan sekarang. Karena tanpa import, **tidak perlu tabel penyimpanan** — export dihitung on-the-fly & langsung diunduh.

**Fase aktif untuk dikerjakan sekarang:** 2 (peta) → 3 (service export) → 5-export (endpoint) → 6-export (test).
**Fase ditunda (backlog import):** 1 (storage), 4 (import service), bagian import di 5 & 6.

**Fokus:** backend (`coda-suaka-backend`). Frontend menyusul di sesi lain.

---

## Phase 0 — Temuan Discovery (sudah dikumpulkan, jangan diasumsikan ulang)

**Library:** hanya `openspout/openspout v4.32.0` terpasang (TIDAK ada PhpSpreadsheet). Cukup — v4.32 mendukung:
- `OpenSpout\Common\Entity\Cell\FormulaCell(string $formula, ?Style $style, $computedValue)` → tulis formula asli (mis. `"=C6-C7"`) + nilai cache. Verifikasi: `vendor/openspout/openspout/src/Common/Entity/Cell/FormulaCell.php`.
- `OpenSpout\Common\Entity\Style\Style::setBackgroundColor(string $hex)` (baris 427) & `setFontColor()` (baris 265) → sel kuning (input) & biru (formula).
- `OpenSpout\Reader\XLSX\Reader` (streaming) untuk baca — sudah dipakai di `KasirService::importNotaPembelian()`.
- **JANGAN tambah dependency baru.** OpenSpout menutup semua kebutuhan template.

**Sumber nilai pre-fill (sudah ada, jangan hitung ulang dari nol):**
- Laba Rugi: `app/Http/Controllers/TransaksiKasController.php::labaRugi()` (baris ~286). Menghitung `pendapatan` (tipe=masuk), `hpp` (keluar + `kategori.termasuk_hpp=true`), `beban_operasional` (keluar + `termasuk_hpp=false`), plus breakdown `pendapatan_per_kategori`, `hpp_per_kategori`, `beban_per_kategori`.
- Arus Kas: `app/Http/Controllers/LaporanController.php::arusKas()` (baris ~19) + `tentukanAktivitas()` (baris ~165) — bucket operasi/investasi/pendanaan via keyword nama kategori.

**Pola import untuk DICOPY (bukan bikin baru):** `app/Services/KasirService.php::importNotaPembelian()` (baris ~303): `new Reader`, `getSheetIterator`/`getRowIterator`/`getCells`, `DB::transaction`, `ValidationException::withMessages(['file' => ...])`. Validasi upload: `app/Http/Requests/ImportNotaPembelianRequest.php` → `'file' => 'required|file|mimes:xlsx|max:5120'`.

**Kategori global sistem** (`database/seeders/KategoriTransaksiSeeder.php`): Penjualan Barang, Pendapatan Jasa, Pembelian Bahan/Stok, Gaji & Upah, Sewa Tempat, Listrik/Air/Internet, Operasional Lain-lain, Setoran Modal, Prive, Pinjaman Masuk, Bayar Cicilan Pinjaman. Tiap kategori punya `tipe`, `sifat`, `termasuk_hpp`.

**Storage:** BELUM ada tabel laporan tersimpan (`grep` migrations kosong) → Phase 1 buat baru.

**Template** (`D:\Download\TEMPLATE LAPORAN KEUANGAN.xlsx`, 4 sheet):
| Sheet | Kolom | Baris kunci |
|---|---|---|
| Laba Rugi - Barang | No, Keterangan, Jumlah(C), SubTotal(D), Total(E) | Pendapatan (C6,C7), HPP (C10-C15), Beban Ops (C19-C26); formula: D8,D14,D16,E17,D27,E28 |
| Laba Rugi - Jasa | idem | Pendapatan (C6-C8), Beban Langsung (C11-C14), Beban Ops (C18-C25); formula: D9,D15,E16,D26,E27 |
| Arus Kas - Barang | No, Keterangan, Masuk(C), Keluar(D) | Operasi (C6-C8/D9-D14), Investasi, Pendanaan; formula: C15,D15,D16,D21,D27,C30-33 |
| Arus Kas - Jasa | idem | idem struktur |
Sel KUNING = input nominal, sel BIRU = formula. **Ambil posisi & warna PERSIS dari file template, jangan hardcode dari ringkasan ini.**

**Ketidakcocokan kunci (sudah diketahui):** line item template seperti Persediaan Awal/Akhir, Penyusutan, Retur, Beban Angkut TIDAK punya sumber transaksi. → pre-fill PARSIAL: isi yang bisa dipetakan, sisakan kuning kosong untuk keuangan.

---

## Phase 1 — Model penyimpanan laporan + fixture template  ⏸️ DITUNDA (backlog import)

> Hanya diperlukan untuk re-import. Untuk export-only, LEWATI seluruh fase ini KECUALI satu tugas: salin template ke `coda-suaka-backend/tests/fixtures/template-laporan-keuangan.xlsx` sebagai fixture test Fase 6. Model/migration/factory `LaporanKeuangan` ditunda sampai fitur import diambil.

**Yang diimplementasi:**
1. Migration `create_laporan_keuangans_table`:
   - `id`, `instansi_id` (char36, FK, index), `outlet_id` (nullable FK), `jenis` enum(`laba_rugi`,`arus_kas`), `tipe_usaha` enum(`barang`,`jasa`), `periode_bulan` (unsignedTinyInt nullable), `periode_tahun` (unsignedSmallInt), `data` (json — map line-item → nominal final), `file_path` (string nullable — xlsx terunggah di disk `local`/`public`), `created_by` (FK users), timestamps.
   - Unique `(instansi_id, jenis, tipe_usaha, periode_tahun, periode_bulan)` — satu laporan resmi per periode+jenis+tipe.
   - Index `(instansi_id, jenis, periode_tahun)`.
2. Model `app/Models/LaporanKeuangan.php`: `$fillable`, `casts` (`data` => array), `TenantScope('instansi_id')` (COPY pola dari `app/Models/Nota.php`), relasi `belongsTo` instansi/outlet/pembuat.
3. Factory `database/factories/LaporanKeuanganFactory.php` (untuk test).
4. Salin template ke repo sebagai sumber kebenaran + fixture test: `coda-suaka-backend/tests/fixtures/template-laporan-keuangan.xlsx` (dari `D:\Download\TEMPLATE LAPORAN KEUANGAN.xlsx`).

**Referensi pola:** `app/Models/Nota.php` (TenantScope kolom-string), `database/migrations/2026_07_31_000002_create_notas_table.php` (struktur migration + index + FK).

**Checklist verifikasi:**
- `php artisan migrate` sukses; `php artisan migrate:rollback` mengembalikan bersih.
- `LaporanKeuangan::factory()->create()` jalan di tinker.
- Tenant scope aktif: query dua instansi tidak bocor (tulis 1 test kecil).

**Anti-pattern:** jangan simpan nilai sebagai kolom terpisah per line-item (puluhan kolom) — pakai `data` JSON. Jangan lupa `TenantScope`.

---

## Phase 2 — Definisi peta template (LINCHPIN, satu sumber kebenaran)

**Yang diimplementasi:** satu file config PHP `config/laporan_template.php` (atau class `App\Support\LaporanTemplateMap`) mendeskripsikan tiap sheet sebagai data, dipakai BERSAMA oleh export & import:
- Nama sheet, judul, sub-judul.
- Untuk tiap baris: `no`, `label`, `cell_input` (mis. `C6`) atau `cell_formula` (mis. `D8` + string formula `=C6-C7`), warna (`kuning`/`biru`), dan `sumber` opsional (nama kategori sistem / rumus agregasi untuk pre-fill).
- Untuk line item tanpa sumber transaksi: `sumber => null` (tetap kuning kosong).

**Cara membangun (WAJIB dari file, bukan dari ingatan):** tulis skrip sekali-pakai yang membaca `tests/fixtures/template-laporan-keuangan.xlsx` via OpenSpout Reader + baca `xl/styles.xml`/`xl/worksheets/sheetN.xml` untuk memastikan koordinat sel, string formula, dan warna fill yang PERSIS. Cocokkan dengan tabel di Phase 0.

**Peta sumber pre-fill (draf, finalisasi saat implementasi):**
- Laba Rugi Barang C6 "Penjualan Kotor" ← Σ transaksi kategori `Penjualan Barang` (masuk).
- C11 "Pembelian Bersih" ← Σ `Pembelian Bahan/Stok` (keluar, termasuk_hpp).
- C19 "Beban Gaji & Upah" ← Σ `Gaji & Upah`. C20 "Beban Sewa" ← `Sewa Tempat`. C21 "Beban Listrik & Air" ← `Listrik, Air, Internet`. C26 "Beban Lain-lain" ← `Operasional Lain-lain`.
- Persediaan Awal/Akhir (C10,C15), Retur (C7,C13), Beban Angkut (C12), Penyusutan (C24) → `sumber=null` (kuning kosong).
- Arus Kas: petakan penerimaan/pembayaran ke `tentukanAktivitas()` yang sudah ada.

**Checklist verifikasi:**
- Setiap `cell_formula` di peta === string formula di file template (bandingkan otomatis dalam test).
- Setiap `cell_input` yang bertanda kuning benar-benar kuning di template.
- Tidak ada koordinat yang tumpang tindih.

**Anti-pattern:** jangan hardcode koordinat dari ingatan/ringkasan ini; ambil dari file. Jangan gandakan formula di export & import — keduanya baca peta yang sama.

---

## Phase 3 — Service EXPORT (template ter-prefill)

**Yang diimplementasi:** `app/Services/LaporanTemplateExportService.php` dengan method per jenis+tipe (atau satu method generik didorong peta Phase 2). Untuk tiap sheet:
- Tulis header (judul, sub-judul, "Periode: <bulan> <tahun>").
- Tulis label line item (kolom Keterangan) dari peta.
- Sel input: jika `sumber != null`, isi nilai terhitung (dari logika `labaRugi()`/`arusKas()` yang sudah ada — panggil ulang / ekstrak jadi service bila perlu, JANGAN duplikasi rumus); beri `Style` background KUNING. Jika `sumber == null`, sel kuning kosong.
- Sel formula: `new FormulaCell($formulaString, $styleBiru, $computedValue)` — `$computedValue` = hasil hitung sistem agar file tetap menampilkan angka sebelum dibuka Excel.
- Style biru untuk sel formula; border/bold untuk baris subtotal sesuai template.
- Kembalikan file via pola `LaporanExportService` yang ada (download response).

**Referensi pola:** `app/Services/LaporanExportService.php` (cara pakai OpenSpout Writer, `Row::fromValues`, style, download). Untuk formula & style: `FormulaCell` + `Style::setBackgroundColor` (lihat Phase 0).

**Checklist verifikasi:**
- Generate untuk instansi ber-transaksi → buka hasil via OpenSpout Reader di test: sel input terisi nilai benar, sel formula berisi string formula yang benar + computedValue, warna sesuai.
- Line item tanpa sumber → sel kosong (bukan 0 keliru).
- Total (computedValue) === hasil `labaRugi()`/`arusKas()` untuk data yang sama.

**Anti-pattern:** jangan tulis nilai kalkulasi sebagai angka biasa di sel yang seharusnya formula (nanti tidak ter-recompute saat keuangan ubah input). Jangan hitung ulang laba/HPP dengan rumus baru — pakai sumber logika yang sudah ada.

---

## Phase 4 — Service IMPORT (parse + simpan laporan resmi)  ⏸️ DITUNDA (backlog import)

> Seluruh fase ini ditunda. Diambil bersama Fase 1 saat fitur re-import dijadwalkan. Peta template (Fase 2) sudah dirancang dipakai bersama, jadi import tinggal membaca peta yang sama.

**Yang diimplementasi:** `app/Services/LaporanTemplateImportService.php::import(UploadedFile $file, array $meta, User $user): LaporanKeuangan`:
- `new Reader`, iterasi sheet sesuai peta; untuk tiap `cell_input` (kuning), baca nilai pada koordinat itu (lacak index baris/kolom seperti `importNotaPembelian`).
- Validasi: nilai numerik ≥ 0 (atau sesuai aturan); sheet/urutan sesuai template (tolak file yang bukan template — cek judul sel A1 == "LAPORAN LABA RUGI"/"LAPORAN ARUS KAS").
- Bangun array `data` (line-item key → nominal), simpan `LaporanKeuangan` (upsert per unique periode+jenis+tipe), simpan file ke disk (`->store('laporan-keuangan/'.$instansiId, 'public')` — pola dari `KasirService`).
- Bungkus `DB::transaction`.

**Referensi pola:** `app/Services/KasirService.php::importNotaPembelian()` (Reader + transaction + ValidationException + `$file->store(...,'public')`). Validasi upload: COPY `ImportNotaPembelianRequest` → `LaporanKeuanganImportRequest` (`mimes:xlsx|max:5120` + `jenis`, `tipe_usaha`, `periode_bulan`, `periode_tahun`).

**Checklist verifikasi:**
- Import file template terisi → row `laporan_keuangans` tersimpan dengan `data` benar + `file_path` ada.
- File bukan template (judul A1 salah) → 422 `ValidationException`, tidak ada row tersimpan (transaction rollback).
- Import ulang periode sama → update (bukan duplikat), unique constraint terjaga.
- Tenant isolation: user instansi lain tidak bisa baca laporan ini.

**Anti-pattern:** jangan percaya sel formula dari file (bisa dimanipulasi) — hanya baca sel INPUT (kuning), hitung/verifikasi total di server. Jangan skip validasi "ini file template kami".

---

## Phase 5 — Controller, route, FormRequest, izin

**Yang diimplementasi (AKTIF — export saja):**
- `LaporanKeuanganController::exportTemplate(Request)` (GET, params jenis/tipe_usaha/periode → panggil export service, kembalikan file download).
- Route: `GET /api/laporan-keuangan/template/export`.

**⏸️ DITUNDA (backlog import):** `import(LaporanKeuanganImportRequest)` (POST), `index`/`show`/`download($id)` (baca laporan tersimpan) — semua bergantung pada storage Fase 1.
- Policy `LaporanKeuanganPolicy` + izin baru mis. `manage:laporan-keuangan` / `import:laporan-keuangan`.

**PENTING — selaras dengan feedback penguji:** penguji minta permission ekspor/impor Excel DIHAPUS dari role **Keuangan**. Jadi:
- Definisikan izin fitur ini eksplisit; JANGAN otomatis berikan ke role Keuangan.
- Cek `config/permissions.php` / `RolePermissionSeeder`: tentukan role mana yang boleh (kemungkinan Owner/Manajemen saja). Konfirmasi ke user sebelum menempelkan ke role.
- Ini juga menyentuh bug terpisah "hapus permission ekspor impor Excel dari role keuangan" — tangani izin Kasir Excel yang sudah ada sekalian di sini atau di tiket terpisah.

**Referensi pola:** `LaporanExportController` (export response), `NotaController` (import + policy `authorizeResource`), `app/Policies/NotaPolicy.php`.

**Checklist verifikasi:**
- `php artisan route:list --path=laporan-keuangan` menampilkan semua route.
- Role tanpa izin → 403; role berizin → 200.
- Round-trip via HTTP: export → (isi) → import → tersimpan.

**Anti-pattern:** jangan pakai nama route singular tak konsisten. Jangan beri izin ke Keuangan tanpa konfirmasi (langsung bertentangan dengan feedback penguji).

---

## Phase 6 — Test & verifikasi akhir

**Yang diimplementasi (AKTIF — export saja; TDD, ikuti `superpowers:test-driven-development`):**
- `tests/Feature/Laporan/ExportTemplateLabaRugiTest.php` — struktur sheet, sel input terisi benar, formula benar, warna, line-item tanpa sumber kosong.
- `tests/Feature/Laporan/ExportTemplateArusKasTest.php` — idem untuk arus kas.
- Izin: role tanpa izin → 403; role Keuangan TIDAK punya izin export (selaras feedback penguji).

**⏸️ DITUNDA (backlog import):** `ImportLaporanKeuanganTest`, `RoundTripLaporanTest`.

**Checklist verifikasi akhir (ikuti `superpowers:verification-before-completion`):**
- `php artisan test` — seluruh suite hijau (termasuk 107 tes existing).
- `grep` anti-pattern: tidak ada dependency spreadsheet baru di `composer.json`; tidak ada koordinat hardcoded di luar peta Phase 2.
- Konfirmasi izin: role Keuangan TIDAK memiliki izin export/import Excel.

**Anti-pattern:** jangan tandai selesai tanpa round-trip test hijau. Jangan lewati verifikasi izin (feedback penguji eksplisit).

---

## Catatan lintas-fase / risiko
- **Formula OpenSpout:** verifikasi dini di Phase 3 dengan satu sel — tulis `FormulaCell`, buka lagi via Reader, pastikan string formula + computedValue kebaca. Kalau ada kejutan, itu titik keputusan paling awal.
- **Pemetaan parsial:** banyak line item template = input manual keuangan (penyusutan, persediaan). Export hanya pre-fill yang punya sumber; sisanya kuning kosong. Jangan paksakan pemetaan yang salah.
- **Barang vs Jasa:** instansi bisa dagang, jasa, atau campuran. Tentukan sheet mana yang relevan per instansi (mungkin export semua 4, atau pilih by parameter). Konfirmasi saat Phase 3.
- **Frontend:** di luar scope rencana ini (dikerjakan sesi lain). Sediakan kontrak API yang jelas (bentuk request/response) agar FE bisa menyusul.
