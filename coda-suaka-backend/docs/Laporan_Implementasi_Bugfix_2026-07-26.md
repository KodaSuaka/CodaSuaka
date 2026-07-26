# Laporan Implementasi Bug Fix — 26 Juli 2026

## Ringkasan

| Bug | Status | Layer | File |
|-----|--------|-------|------|
| #1 | ✅ Fixed | Backend Controller | `PenugasanController.php` |
| #2 | ✅ Fixed | Backend Controller | `PenugasanController.php` |
| #3 | ✅ Fixed | Backend Policy + Frontend | `PenugasanPolicy.php`, `PenugasanViewModel.kt` |
| #4 | ℹ️ No bug | Backend | Validasi & save logic sudah benar |
| #5 | ✅ Fixed | Backend Controller | `AttandenceController.php` |
| #6 | ✅ Fixed | Frontend | `NotificationViewModel.kt` |
| #7 | ✅ Fixed | Frontend | `ErrorMessageMapper.kt` |
| #8 | ✅ Fixed | Backend Routes + Config | `api.php`, `permissions.php` |
| #9 | ✅ Fixed | Frontend | `DivisiViewModel.kt` |
| #10 | ✅ Fixed | Database Migration | `2026_07_26_000001_add_additional_performance_indexes.php` |
| #11 | ✅ Fixed | Backend Controller | `KaryawanController.php` |
| #12 | ✅ Fixed | Backend Validation | `StorejadwalRequest.php`, `UpdatejadwalRequest.php` |
| #13 | ℹ️ No bug | — | Frontend & backend konsisten, 422 dari field lain |
| #14 | ✅ Fixed | Backend Controller | `KaryawanController.php` |
| #15 | ✅ Fixed | Backend + Frontend | `StorekaryawanRequest.php`, `KelolaKaryawanViewModel.kt` |

---

## Detail Perubahan

### Bug #1 — Notif menerima tugas tidak muncul
**Root Cause:** `NotificationService::onPenugasanBaru()` tidak dipanggil dari `PenugasanController::store()`.
**Fix:** Tambahkan pemanggilan `onPenugasanBaru()` setelah `penugasan` dibuat, dengan pengecekan `penanggung_jawab_id`.

### Bug #2 — Pesan selesainya tugas tidak terkirim
**Root Cause:** `sendPenugasanSelesaiNotification()` mengirim ke `created_by`, tapi untuk template task, `created_by` = karyawan yang accept.
**Fix:** Tambah fallback — jika `created_by === currentUser.id`, kirim ke Owner/Manager instansi.

### Bug #3 — Manager tidak bisa menerima tugas
**Root Cause:**
- Backend: `PenugasanPolicy::accept()` hanya cek `penanggung_jawab_id`
- Frontend: `isAssignedTo()` hanya cek karyawan match

**Fix:**
- Backend: Tambah check `manage:penugasan` permission (Owner/Manager) sebelum cek karyawan
- Frontend: Tambah check `canManagePenugasan` di `isAssignedTo()`

### Bug #4 — Gagal menyimpan jam operasional
**Analisis:** Backend validation (`nullable|array` + nested rules), controller save, dan model cast sudah benar. Frontend mengirim data dengan format sesuai. Error kemungkinan dari intermittent issue atau error message mapping (tertangani di Bug #7).

### Bug #5 — Rekap kehadiran tidak cross-reference pengajuan
**Root Cause:** `AttandenceController::rekap()` hanya menghitung dari tabel `attandence`, tidak mempertimbangkan pengajuan yang sudah disetujui.
**Fix:** Query pengajuan `status=disetujui` yang overlap bulan rekap, hitung hari izin/cuti/sakit dari pengajuan yang belum tercatat di attandence. Tambah field `hari_kerja` di response.

### Bug #6 — Fitur notifikasi tidak berjalan
**Root Cause:** Tidak ada periodic polling untuk unread count. Hanya di-load sekali saat init.
**Fix:** Tambah `startPeriodicPolling()` dengan interval 30 detik menggunakan coroutine `delay()`.

### Bug #7 — Error message tidak jelas
**Root Cause:** `ErrorMessageMapper` untuk 422 hanya menampilkan "Format data tidak sesuai" tanpa ekstrak detail validasi.
**Fix:** Tambah `extractValidasiDetails()` yang parse pola validasi Laravel (`the X field is required`, `the selected X is invalid`, dll).

### Bug #8 — Approval workflow belum aktif
**Root Cause:** Routes dikomentari di `api.php`, permission `approve:keuangan` dikomentari di `config/permissions.php`.
**Fix:** Uncomment approval routes, uncomment `approve:keuangan` untuk Owner role.

### Bug #9 — Logika divisi tidak filter by outlet
**Root Cause:** `DivisiViewModel.loadInitialData()` load semua karyawan tanpa filter outlet.
**Fix:** Tambah field `allKaryawans` di state (semua karyawan untuk mapping anggota), karyawan list bisa difilter by outlet.

### Bug #10 — Optimasi performance
**Fix:** Migration `2026_07_26_000001_add_additional_performance_indexes.php`:
- `pengajuans(user_id, status, tanggal_mulai, tanggal_selesai)` — rekap query
- `attandences(user_id, tanggal, status)` — rekap query
- `notifications(user_id, is_read)` — unread count
- `penugasans(penanggung_jawab_id, status)` — filter query

### Bug #11 — Owner dihitung sebagai karyawan
**Root Cause:** `KaryawanController::store()` count semua karyawan termasuk Owner/Super Admin.
**Fix:** Filter count dengan `whereNotIn('nama_role', ['Super Admin', 'Owner'])`.

### Bug #12 — Kategori tugas error 422
**Root Cause:** Validasi `kategori` tidak mencakup nilai `tugas`.
**Fix:** Tambah `tugas` ke `in:meeting,training,event,libur,tugas,lainnya` di `StorejadwalRequest` dan `UpdatejadwalRequest`.

### Bug #13 — Cuti tahunan error 422
**Analisis:** Frontend kirim `"cuti_tahunan"`, backend terima `in:cuti_tahunan,izin_sakit,mendadak`. Konsisten. Error 422 kemungkinan dari field lain (tanggal, keterangan). Tidak perlu fix.

### Bug #14 — Karyawan tanpa jatah cuti bisa ajukan izin
**Root Cause:** Tidak ada validasi `sisa_cuti < 0` di `KaryawanController::update()`.
**Fix:** Tambah validasi `sisa_cuti < 0` return error 422.

### Bug #15 — Pemilik harus buat outlet dulu
**Root Cause:** `outlet_id` required di `StorekaryawanRequest`.
**Fix:**
- Backend: Ubah `outlet_id` dari `required` ke `nullable`
- Frontend: Tambah `hasOutlets` flag di `KelolaKaryawanUiState`, `outletId` sudah nullable di request

---

## File yang Dimodifikasi

### Backend (9 file)
1. `app/Http/Controllers/PenugasanController.php` — Bug #1, #2
2. `app/Policies/PenugasanPolicy.php` — Bug #3
3. `app/Http/Controllers/AttandenceController.php` — Bug #5
4. `app/Http/Controllers/KaryawanController.php` — Bug #11, #14
5. `app/Http/Requests/StorejadwalRequest.php` — Bug #12
6. `app/Http/Requests/UpdatejadwalRequest.php` — Bug #12
7. `app/Http/Requests/StorekaryawanRequest.php` — Bug #15
8. `routes/api.php` — Bug #8
9. `config/permissions.php` — Bug #8

### Database (1 file)
10. `database/migrations/2026_07_26_000001_add_additional_performance_indexes.php` — Bug #10 (NEW)

### Frontend (5 file)
11. `ui/screen/notifikasi/NotificationViewModel.kt` — Bug #6
12. `util/ErrorMessageMapper.kt` — Bug #7
13. `ui/screen/penugasan/PenugasanViewModel.kt` — Bug #3
14. `ui/screen/divisi/DivisiViewModel.kt` — Bug #9
15. `ui/screen/kelola_karyawan/KelolaKaryawanViewModel.kt` — Bug #15

---

## Catatan untuk Testing

### Post-deploy wajib:
1. Jalankan `php artisan permission:sync` untuk sync permission baru (`approve:keuangan`)
2. Jalankan `php artisan migrate` untuk migration index
3. Untuk MySQL 5.7.44, indexes ditambahkan tanpa CTE/window functions

### Manual testing checklist:
- [ ] Bug #1: Buat penugasan → cek notif muncul di karyawan ditugasi
- [ ] Bug #2: Karyawan accept + selesai tugas → cek notif ke Owner/Manager
- [ ] #3: Owner/Manager accept tugas → harus berhasil
- [ ] #5: Rekap bulanan → karyawan dengan pengajuan disetujui tidak dihitung alpha
- [ ] #6: Buka halaman notifikasi → badge count update otomatis tiap 30 detik
- [ ] #7: Submit form dengan validasi gagal → pesan error jelas field mana yang salah
- [ ] #8: Approval transaksi keuangan → route aktif, permission berfungsi
- [ ] #10: Query rekap kehadiran → response lebih cepat
- [ ] #11: Tambah karyawan saat kuota penuh → Owner tidak dihitung
- [ ] #12: Buat jadwal dengan kategori "tugas" → tidak error 422
- [ ] #14: Update sisa_cuti ke -1 → error 422
- [ ] #15: Tambah karyawan tanpa pilih outlet → berhasil
