# Laporan Bugfix — Penugasan & Jam Operasional
**Tanggal:** 2026-07-26  
**SOP:** `Helper/sop_master_end_to_end_ai_agent.md`

---

## 1. Ringkasan

| Bug | Severitas | Status |
|-----|-----------|--------|
| Karyawan tidak bisa menerima tugas (template) | HIGH | ✅ Fixed |
| Karyawan bisa edit data tugas tanpa hak | MEDIUM | ✅ Fixed |
| Gagal menyimpan jam operasional | HIGH | ✅ Fixed |

---

## 2. Root Cause & Perbaikan

### Bug 1a: Karyawan tidak bisa menerima tugas template

**File:** [`PenugasanDetailScreen.kt:262`](../coda-suaka-frontend/app/src/main/java/com/example/codasuaka/ui/screen/penugasan/PenugasanDetailScreen.kt:262)

**Root cause:** Tombol accept/complete hanya muncul jika `isAssigned && !canManage`. Untuk template task, `penanggungJawabId = null` sehingga `isAssigned` selalu `false`. Backend benar — `PenugasanPolicy::accept():92` mengizinkan semua karyawan accept template.

**Fix:**
```kotlin
// Sebelum:
if (isAssigned && !canManage) {

// Sesudah:
if (!canManage && (isAssigned || penugasan.isTemplate == true)) {
```

---

### Bug 1b: Karyawan bisa edit data tugas tanpa hak

**File:** [`PenugasanPolicy.php:47-65`](../coda-suaka-backend/app/Policies/PenugasanPolicy.php:47)

**Root cause:** `PenugasanPolicy::update()` mengembalikan `true` untuk karyawan yang ditugasi tanpa membatasi field apa yang bisa di-update. Komentar bilang "update status (accept/complete)" tapi kode mengizinkan update semua field (judul, deskripsi, dll). Karyawan seharusnya HANYA boleh pakai endpoint `accept` dan `complete`.

**Fix:** Hapus blok karyawan dari `update()`:
```php
// Dihapus:
$karyawan = $user->profilKaryawan;
if ($karyawan && $penugasan->penanggung_jawab_id === $karyawan->id) {
    return true;
}
```

Sekarang hanya role dengan permission `manage:penugasan` (Owner/Manager) yang boleh update field tugas.

---

### Bug 2: Gagal menyimpan jam operasional

**File:** [`UpdateInstansiRequest.php:32`](../coda-suaka-backend/app/Http/Requests/UpdateInstansiRequest.php:32)

**Root cause:** Validasi `hari_operasional.*` pakai `min:0|max:6`, tapi frontend pakai `Calendar.DAY_OF_WEEK` nilai 1-7 (1=Minggu, 7=Sabtu). Default `hariOperasional = setOf(2,3,4,5,6,7)` — nilai 7 (Sabtu) melebihi batas `max:6` sehingga validasi gagal.

**Fix:**
```php
// Sebelum:
'jam_operasional.hari_operasional.*' => 'integer|min:0|max:6',

// Sesudah:
'jam_operasional.hari_operasional.*' => 'integer|min:1|max:7',
```

---

## 3. QA+QC Results

| Check | Result |
|-------|--------|
| Backend tests (`php artisan test`) | ✅ 51/51 passed (138 assertions) |
| Frontend build (`gradlew assembleDebug`) | ✅ BUILD SUCCESSFUL |
| Regresi PenugasanWorkflowTest | ✅ 13/13 passed |
| Regresi InstansiJamOperasionalTest | ✅ 7/7 passed |
| Tidak ada perubahan database/migration | ✅ |
| Tidak ada perubahan routing | ✅ |

---

## 4. File yang Diubah

| # | File | Layer | Perubahan |
|---|------|-------|-----------|
| 1 | `coda-suaka-frontend/.../penugasan/PenugasanDetailScreen.kt` | Frontend | Tombol accept tampil untuk template task |
| 2 | `coda-suaka-backend/app/Policies/PenugasanPolicy.php` | Backend | Hapus hak update untuk karyawan |
| 3 | `coda-suaka-backend/app/Http/Requests/UpdateInstansiRequest.php` | Backend | Fix range validasi hari_operasional 1-7 |
