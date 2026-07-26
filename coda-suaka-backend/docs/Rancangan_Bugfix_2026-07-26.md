# Rancangan Bugfix — CodaSuaka
**Tanggal**: 2026-07-26  
**SOP Ref**: Fase 2 — Rancang Solusi per-Layer  
**Status**: MENUNGGU APPROVAL USER

---

## Ringkasan Root Cause Analysis

| # | Bug | Root Cause | Layer |
|---|-----|-----------|-------|
| 1 | Notif menerima tugas tidak muncul | [`PenugasanController::store()`](coda-suaka-backend/app/Http/Controllers/PenugasanController.php:104) tidak panggil `NotificationService::onPenugasanBaru()` | Backend Logic |
| 2 | Pesan selesaikan tugas tidak terkirim | [`sendPenugasanSelesaiNotification()`](coda-suaka-backend/app/Http/Controllers/PenugasanController.php:283) skip notifikasi jika `created_by === currentUser.id` (kasus template task) | Backend Logic |
| 3 | Manager tidak bisa menerima tugas | [`PenugasanPolicy::accept()`](coda-suaka-backend/app/Policies/PenugasanPolicy.php:70) — policy oke, tapi frontend render tombol accept hanya untuk karyawan biasa, bukan Manager | Frontend Logic |
| 4 | Gagal simpan data operasional | [`InstansiController::update()`](coda-suaka-backend/app/Http/Controllers/InstansiController.php:44) hanya save `jam_operasional` sebagai JSON array tanpa validasi nested structure; `UpdateInstansiRequest` validasi oke tapi tidak update `jam_operasional` dengan benar karena `$request->only()` | Backend Logic |
| 5 | Rekap kehadiran masih dianggap hadir meski izin | [`AttandenceController::rekap()`](coda-suaka-backend/app/Http/Controllers/AttandenceController.php:211) hanya count dari tabel `attandence`, tidak cross-reference tabel `pengajuan` untuk approved leave | Backend Logic |
| 6 | Fitur notifikasi tidak berjalan | Bug #1 + tidak ada periodic refresh di frontend; notifikasi hanya load saat sidebar dibuka | Backend + Frontend |
| 7 | Pesan error/progres tidak jelas | Frontend error handling generic, tidak map HTTP status ke pesan spesifik | Frontend DTO |
| 8 | Data persetujuan tidak ada fallback validasi | Route approval workflow [dikomentari](coda-suaka-backend/routes/api.php:153) di `api.php` | Route Config |
| 9 | Logika divisi ambigu | Tidak ada enforce outlet scoping di frontend; karyawan bisa dipilih dari semua outlet | Frontend + Backend |
| 10 | Optimasi caching & indexing | Sudah ada [migration index](coda-suaka-backend/database/migrations/2026_07_10_000001_add_performance_indexes.php), tapi perlu tambah index pada beberapa kolom | Database |
| 11 | Role pemilik dianggap karyawan | [`KaryawanController::store()`](coda-suaka-backend/app/Http/Controllers/KaryawanController.php:80) count ALL karyawan via `user.instansi_id` — termasuk profilKaryawan Owner jika ada | Backend Logic |
| 12 | Kategori tugas jadwal error 422 | Frontend kirim `"tugas"` via [`EventCategory.TUGAS.name.lowercase()`](coda-suaka-frontend/app/src/main/java/com/example/codasuaka/ui/screen/kalender/KalenderViewModel.kt:208), tapi backend validasi [hanya terima `meeting,training,event,libur,lainnya`](coda-suaka-backend/app/Http/Requests/StorejadwalRequest.php:32) | Backend Validation |
| 13 | Kategori cuti tahunan error 422 | Frontend enum `JenisPengajuan.name.lowercase()` → `"cuti_tahunan"` SAMA dengan backend `in:cuti_tahunan,...` — **tidak ada mismatch**. Kemungkinan 422 datang dari field `keterangan` kosong atau < 10 char (backend `nullable`, frontend enforce min 10) | Tidak ada bug backend |
| 14 | Karyawan tanpa jatah cuti bisa ajukan | `sisa_cuti` default `0` di [migration](coda-suaka-backend/database/migrations/2026_06_12_120917_create_karyawans_table.php:19), tapi check di [`store()`](coda-suaka-backend/app/Http/Controllers/PengajuanController.php:65) pakai `<= 0` yang benar. Issue: `sisa_cuti` bisa di-set manual via edit karyawan tanpa validasi apakah karyawan punya jatah cuti | Backend Logic |
| 15 | Pemilik harus daftar outlet dulu | [`StorekaryawanRequest`](coda-suaka-backend/app/Http/Requests/StorekaryawanRequest.php) requires `outlet_id`. Ini BENAR. Tapi frontend tidak enforce order (harus buat outlet dulu) | Frontend UX |

---

## Rancangan Fix per Bug — per Layer

---

### BUG #1: Notif menerima tugas tidak muncul

**Root Cause**: [`PenugasanController::store()`](coda-suaka-backend/app/Http/Controllers/PenugasanController.php:104) membuat tugas baru tapi TIDAK panggil `NotificationService::onPenugasanBaru()`. Method `onPenugasanBaru` sudah ada di [`NotificationService`](coda-suaka-backend/app/Services/NotificationService.php:190) tapi tidak pernah dipanggil.

**Fix — Backend Controller**:
- Di [`PenugasanController::store()`](coda-suaka-backend/app/Http/Controllers/PenugasanController.php:122), setelah `$penugasan->load(...)`, tambahkan:
  ```php
  // Kirim notifikasi ke karyawan yang ditugasi
  if ($penugasan->penanggung_jawab_id) {
      $this->notificationService->onPenugasanBaru(
          $penugasan->id,
          $penugasan->penanggung_jawab_id,
          $penugasan->judul
      );
  }
  ```

**File**: [`PenugasanController.php`](coda-suaka-backend/app/Http/Controllers/PenugasanController.php:122)

---

### BUG #2: Pesan selesaikan tugas tidak terkirim

**Root Cause**: [`sendPenugasanSelesaiNotification()`](coda-suaka-backend/app/Http/Controllers/PenugasanController.php:283) kirim notifikasi ke `created_by`. Untuk task dari template, `created_by` = karyawan itu sendiri, jadi `$penugasan->created_by !== $currentUser->id` = false → notifikasi di-skip.

**Fix — Backend Controller**:
- Ubah logika di [`sendPenugasanSelesaiNotification()`](coda-suaka-backend/app/Http/Controllers/PenugasanController.php:283):
  ```php
  private function sendPenugasanSelesaiNotification(penugasan $penugasan, User $currentUser): void
  {
      $karyawan = $currentUser->profilKaryawan;
      $namaKaryawan = $karyawan->nama_lengkap ?? $currentUser->name;

      // Kirim ke pembuat tugas (jika berbeda dari pelaku)
      if ($penugasan->created_by && $penugasan->created_by !== $currentUser->id) {
          $this->notificationService->onPenugasanSelesai(
              $penugasan->id, $penugasan->created_by, $namaKaryawan, $penugasan->judul
          );
          return;
      }

      // Fallback: untuk task dari template, kirim ke owner/manager instansi
      // Cari manager/owner yang punya manage:penugasan permission
      $recipientIds = User::where('instansi_id', $currentUser->instansi_id)
          ->whereHas('role', fn ($q) => $q->whereIn('nama_role', ['Owner', 'Manager']))
          ->where('id', '!=', $currentUser->id)
          ->pluck('id');

      foreach ($recipientIds as $recipientId) {
          $this->notificationService->onPenugasanSelesai(
              $penugasan->id, $recipientId, $namaKaryawan, $penugasan->judul
          );
      }
  }
  ```

- Terapkan logika serupa di [`sendPenugasanDikerjakanNotification()`](coda-suaka-backend/app/Http/Controllers/PenugasanController.php:264).

**File**: [`PenugasanController.php`](coda-suaka-backend/app/Http/Controllers/PenugasanController.php:264)

---

### BUG #3: Manager tidak bisa menerima tugas

**Root Cause**: [`PenugasanPolicy::accept()`](coda-suaka-backend/app/Policies/PenugasanPolicy.php:70) sebenarnya oke — Manager yang ditugasi BISA accept. Tapi di frontend, [`PenugasanViewModel::isAssignedTo()`](coda-suaka-frontend/app/src/main/java/com/example/codasuaka/ui/screen/penugasan/PenugasanViewModel.kt:229) check `currentKaryawanId != null && penugasan.penanggungJawabId == currentKaryawanId`. Manager mungkin tidak punya `profilKaryawan` record jika dibuat langsung via User register (bukan via KaryawanController).

**Fix — Backend**:
- Pastikan semua user dengan role Manager/Owner punya profilKaryawan. Tambahkan validasi di [`PenugasanPolicy::accept()`](coda-suaka-backend/app/Policies/PenugasanPolicy.php:70): jika user punya permission `manage:penugasan`, izinkan accept (sebagai override).

```php
public function accept(User $user, penugasan $penugasan): bool
{
    if (! $this->isSameTenant($user, $penugasan)) {
        return false;
    }

    // User dengan manage:penugasan bisa accept semua tugas di instansinya
    if (app(PermissionService::class)->userHasPermission($user, 'manage:penugasan')) {
        return true;
    }

    $karyawan = $user->profilKaryawan;
    if (! $karyawan) {
        return false;
    }

    if ($penugasan->is_template) {
        return true;
    }

    return $penugasan->penanggung_jawab_id === $karyawan->id;
}
```

**Fix — Frontend**:
- Di [`PenugasanViewModel`](coda-suaka-frontend/app/src/main/java/com/example/codasuaka/ui/screen/penugasan/PenugasanViewModel.kt:229), update `isAssignedTo()`:
  ```kotlin
  fun isAssignedTo(penugasan: PenugasanDto): Boolean {
      val state = _uiState.value
      // Manager/Owner bisa accept semua tugas
      if (state.canManagePenugasan) return true
      return state.currentKaryawanId != null && penugasan.penanggungJawabId == state.currentKaryawanId
  }
  ```

**Files**: [`PenugasanPolicy.php`](coda-suaka-backend/app/Policies/PenugasanPolicy.php:70), [`PenugasanViewModel.kt`](coda-suaka-frontend/app/src/main/java/com/example/codasuaka/ui/screen/penugasan/PenugasanViewModel.kt:229)

---

### BUG #4: Gagal simpan data operasional

**Root Cause**: [`InstansiController::update()`](coda-suaka-backend/app/Http/Controllers/InstansiController.php:44) pakai `$request->only(['nama_instansi', 'jam_operasional'])`. Field `jam_operasional` dikirim sebagai nested JSON object dari frontend [`JamOperasionalViewModel`](coda-suaka-frontend/app/src/main/java/com/example/codasuaka/ui/screen/jam_operasional/JamOperasionalViewModel.kt:105). Model [`Instansi`](coda-suaka-backend/app/Models/Instansi.php:24) sudah cast `jam_operasional` sebagai `array`. **Sebenarnya ini seharusnya work**. 

Kemungkinan issue: response frontend [`response.body()?.data`](coda-suaka-frontend/app/src/main/java/com/example/codasuaka/ui/screen/jam_operasional/JamOperasionalViewModel.kt:49) tidak parse nested JSON dengan benar karena field mapping (snake_case vs camelCase).

**Fix — Frontend DTO**:
- Pastikan `InstansiResponse` DTO properly deserialize `jam_operasional` dari JSON.
- Cek `UpdateInstansiRequest` DTO di frontend kirim field dengan nama yang benar.

**Fix — Backend**:
- Di [`InstansiController::update()`](coda-suaka-backend/app/Http/Controllers/InstansiController.php:50), pastikan `jam_operasional` di-save dengan cast yang benar:
  ```php
  $data = $request->only(['nama_instansi', 'jam_operasional']);
  // jam_operasional harus berupa JSON object, bukan array of mixed types
  if (isset($data['jam_operasional'])) {
      $data['jam_operasional'] = json_encode($data['jam_operasional']);
  }
  $instansi->update($data);
  ```

**Files**: [`InstansiController.php`](coda-suaka-backend/app/Http/Controllers/InstansiController.php:44), frontend `JamOperasionalViewModel.kt`

---

### BUG #5: Rekap kehadiran masih dianggap hadir meski izin

**Root Cause**: [`AttandenceController::rekap()`](coda-suaka-backend/app/Http/Controllers/AttandenceController.php:211) hanya count dari tabel `attandence` berdasarkan `status`. Tidak cross-reference dengan tabel `pengajuan` untuk approved leave. Jika karyawan submit izin/cuti yang sudah di-approve, tapi tidak ada record di `attandence` untuk hari itu, maka hari tersebut dianggap tidak hadir (alpha). Atau jika ada record `attandence` dengan status `hadir` tapi karyawan juga punya approved pengajuan untuk hari yang sama, status tetap `hadir`.

**Fix — Backend Controller**:
- Update [`AttandenceController::rekap()`](coda-suaka-backend/app/Http/Controllers/AttandenceController.php:211):
  ```php
  public function rekap(Request $request)
  {
      // ... permission check, timezone, bulan/tahun ...

      $userIds = User::where('instansi_id', $user->instansi_id)->pluck('id');

      // Load attendance data
      $attendanceRecords = attandence::whereIn('user_id', $userIds)
          ->whereMonth('tanggal', $bulan)
          ->whereYear('tanggal', $tahun)
          ->get()
          ->groupBy('user_id');

      // Load approved pengajuan for the same period
      $approvedPengajuans = pengajuan::whereIn('user_id', $userIds)
          ->where('status', 'disetujui')
          ->where('jenis', 'in', ['cuti_tahunan', 'izin_sakit', 'mendadak'])
          ->where(function ($q) use ($startDate, $endDate) {
              $q->whereBetween('tanggal_mulai', [$startDate, $endDate])
                ->orWhereBetween('tanggal_selesai', [$startDate, $endDate])
                ->orWhere(function ($q2) use ($startDate, $endDate) {
                    $q2->where('tanggal_mulai', '<=', $startDate)
                       ->where('tanggal_selesai', '>=', $endDate);
                });
          })
          ->get()
          ->groupBy('user_id');

      // Build rekap with cross-reference
      $rekap = $userIds->map(function ($userId) use ($attendanceRecords, $approvedPengajuans, $users, $startDate, $endDate) {
          $items = $attendanceRecords->get($userId, collect());
          $user = $users->get($userId);

          // Calculate izin/sakit days from approved pengajuan
          $pengajuans = $approvedPengajuans->get($userId, collect());
          $izinDays = 0;
          $sakitDays = 0;
          $cutiDays = 0;
          // ... hitung hari izin/sakit/cuti dari pengajuan yang overlap dengan bulan ini

          return [
              'user_id' => $userId,
              'nama_lengkap' => $user?->profilKaryawan?->nama_lengkap ?? $user?->name,
              'total_hadir' => $items->where('status', 'hadir')->count(),
              'total_izin' => $izinDays,
              'total_sakit' => $sakitDays,
              'total_alpha' => $items->where('status', 'alpha')->count(),
              'total_cuti' => $cutiDays,
          ];
      })->values();

      return $this->success($rekap);
  }
  ```

**File**: [`AttandenceController.php`](coda-suaka-backend/app/Http/Controllers/AttandenceController.php:211)

---

### BUG #6: Fitur notifikasi tidak berjalan

**Root Cause**: Gabungan dari Bug #1 (notif tidak dibuat saat assign tugas) + frontend hanya load notifikasi saat sidebar dibuka (tidak ada auto-refresh/polling).

**Fix — Backend**: Sudah covered di Bug #1 & #2.

**Fix — Frontend**:
- Tambahkan periodic polling di [`NotificationViewModel`](coda-suaka-frontend/app/src/main/java/com/example/codasuaka/ui/screen/notifikasi/NotificationViewModel.kt:24) untuk refresh unread count setiap 30 detik:
  ```kotlin
  init {
      loadNotifications()
      loadUnreadCount()
      startPolling()
  }

  private fun startPolling() {
      viewModelScope.launch {
          while (true) {
              delay(30_000)
              loadUnreadCount()
          }
      }
  }
  ```
- Pastikan `viewModelScope` handle lifecycle dengan benar.

**File**: [`NotificationViewModel.kt`](coda-suaka-frontend/app/src/main/java/com/example/codasuaka/ui/screen/notifikasi/NotificationViewModel.kt)

---

### BUG #7: Pesan error/progres tidak jelas

**Root Cause**: Frontend error handling generic. Tidak map HTTP status code ke pesan spesifik yang user-friendly.

**Fix — Frontend DTO/Util**:
- Update [`ErrorMessageMapper.kt`](coda-suaka-frontend/app/src/main/java/com/example/codasuaka/util/ErrorMessageMapper.kt) untuk map HTTP status ke pesan:
  ```kotlin
  fun mapErrorMessage(code: Int, message: String?): String {
      return when (code) {
          400 -> message ?: "Data tidak valid"
          401 -> "Sesi berakhir, silakan login kembali"
          403 -> "Anda tidak memiliki akses"
          404 -> "Data tidak ditemukan"
          422 -> message ?: "Validasi gagal, periksa input Anda"
          429 -> "Terlalu banyak request, coba lagi nanti"
          500 -> "Terjadi kesalahan server"
          else -> message ?: "Terjadi kesalahan tidak diketahui"
      }
  }
  ```
- Terapkan di setiap repository layer saat handle error response.

**File**: [`ErrorMessageMapper.kt`](coda-suaka-frontend/app/src/main/java/com/example/codasuaka/util/ErrorMessageMapper.kt), semua `*RepositoryImpl.kt`

---

### BUG #8: Data persetujuan tidak ada fallback validasi

**Root Cause**: Route approval workflow [dikomentari](coda-suaka-backend/routes/api.php:153) di `api.php`. Approval untuk pengajuan (cuti/izin) sudah ada di route `/pengajuans/{id}/approve` dan `/pengajuans/{id}/reject` — ini WORK. Tapi approval untuk keuangan (`approve:keuangan`) belum diaktifkan.

**Fix — Backend Route**:
- Aktifkan route approval keuangan di [`api.php`](coda-suaka-backend/routes/api.php:153):
  ```php
  Route::prefix('approval')->middleware('permission:approve:keuangan')->group(function () {
      Route::get('/pending', [ApprovalController::class, 'pending']);
      Route::get('/riwayat', [ApprovalController::class, 'riwayat']);
      Route::post('/{transaksi_kas}/ajukan', [ApprovalController::class, 'ajukan']);
      Route::post('/{approval_log}/setujui', [ApprovalController::class, 'setujui']);
      Route::post('/{approval_log}/tolak', [ApprovalController::class, 'tolak']);
  });
  ```
- Aktifkan permission `approve:keuangan` di [`config/permissions.php`](coda-suaka-backend/config/permissions.php:112) untuk role Owner dan Keuangan.

**Files**: [`api.php`](coda-suaka-backend/routes/api.php:153), [`config/permissions.php`](coda-suaka-backend/config/permissions.php)

---

### BUG #9: Logika divisi tidak jelas

**Root Cause**: Frontend [`DivisiViewModel`](coda-suaka-frontend/app/src/main/java/com/example/codasuaka/ui/screen/divisi/DivisiViewModel.kt:101) load semua karyawan (`getKaryawans()`) tanpa filter outlet. Jadi saat membuat divisi, user bisa pilih karyawan dari outlet lain. Backend [`StoreDivisiRequest`](coda-suaka-backend/app/Http/Requests/StoreDivisiRequest.php) tidak enforce karyawan harus dari outlet yang sama.

**Fix — Backend**:
- Di [`StoreDivisiRequest`](coda-suaka-backend/app/Http/Requests/StoreDivisiRequest.php), tambahkan validasi `ketua_karyawan_id` harus dari outlet yang sama.

**Fix — Frontend**:
- Di [`DivisiViewModel`](coda-suaka-frontend/app/src/main/java/com/example/codasuaka/ui/screen/divisi/DivisiViewModel.kt:84), filter karyawan berdasarkan outlet yang dipilih:
  ```kotlin
  fun onFormOutletChange(outletId: Int) {
      _uiState.update { state ->
          val filteredKaryawan = state.karyawanList.filter { it.outlet?.id == outletId }
          state.copy(
              formOutletId = outletId,
              formAvailableKaryawan = filteredKaryawan
          )
      }
  }
  ```

**Files**: [`DivisiViewModel.kt`](coda-suaka-frontend/app/src/main/java/com/example/codasuaka/ui/screen/divisi/DivisiViewModel.kt), [`StoreDivisiRequest.php`](coda-suaka-backend/app/Http/Requests/StoreDivisiRequest.php)

---

### BUG #10: Optimasi caching & indexing

**Root Cause**: Index sudah ada di [migration](coda-suaka-backend/database/migrations/2026_07_10_000001_add_performance_indexes.php), tapi perlu tambah beberapa index lagi.

**Fix — Database/Migration**:
- Buat migration baru untuk tambah index:
  ```php
  // pengajuans: index untuk rekap query
  Schema::table('pengajuans', function (Blueprint $table) {
      $table->index(['status', 'jenis', 'tanggal_mulai', 'tanggal_selesai']);
      $table->index(['user_id', 'status']);
  });

  // attandences: index untuk rekap query
  Schema::table('attandences', function (Blueprint $table) {
      $table->index(['tanggal', 'status']);
      $table->index(['user_id', 'tanggal']);
  });

  // notifications: index untuk user unread count
  Schema::table('notifications', function (Blueprint $table) {
      $table->index(['user_id', 'is_read']);
  });

  // penugasans: index untuk filter query
  Schema::table('penugasans', function (Blueprint $table) {
      $table->index(['status', 'divisi_id']);
      $table->index(['penanggung_jawab_id', 'status']);
  });
  ```

**File**: New migration file

---

### BUG #11: Role pemilik dianggap karyawan

**Root Cause**: [`KaryawanController::store()`](coda-suaka-backend/app/Http/Controllers/KaryawanController.php:80) count karyawan via `karyawan::whereHas('user', fn($q) => $q->where('instansi_id', $instansiId))->count()`. Ini menghitung SEMUA profilKaryawan yang user-nya punya `instansi_id` yang sama. Jika Owner juga punya `profilKaryawan` record (dari registrasi), maka count termasuk Owner.

**Fix — Backend**:
- Di [`KaryawanController::store()`](coda-suaka-backend/app/Http/Controllers/KaryawanController.php:80), exclude karyawan yang role-nya Owner/Super Admin:
  ```php
  $currentCount = karyawan::whereHas('user', function ($q) use ($instansiId) {
      $q->where('instansi_id', $instansiId)
        ->whereDoesntHave('role', fn ($q2) => $q2->whereIn('nama_role', ['Owner', 'Super Admin']));
  })->count();
  ```

**File**: [`KaryawanController.php`](coda-suaka-backend/app/Http/Controllers/KaryawanController.php:80)

---

### BUG #12: Kategori tugas jadwal error 422

**Root Cause**: Frontend [`KalenderViewModel`](coda-suaka-frontend/app/src/main/java/com/example/codasuaka/ui/screen/kalender/KalenderViewModel.kt:208) kirim `state.formKategori.name.lowercase()` → `"tugas"` untuk `EventCategory.TUGAS`. Backend [`StorejadwalRequest`](coda-suaka-backend/app/Http/Requests/StorejadwalRequest.php:32) validasi `in:meeting,training,event,libur,lainnya` — **`"tugas"` tidak ada di list**.

**Fix — Backend (pilih salah satu)**:

**Opsi A**: Tambah `"tugas"` ke validasi backend (recommended):
```php
'kategori' => 'required|in:meeting,training,event,libur,tugas,lainnya',
```

**Opsi B**: Hapus `EventCategory.TUGAS` dari frontend dan ganti dengan `training`.

**Rekomendasi**: Opsi A — tambah `"tugas"` ke validasi karena frontend sudah punya UI untuk kategori ini.

**File**: [`StorejadwalRequest.php`](coda-suaka-backend/app/Http/Requests/StorejadwalRequest.php:32)

---

### BUG #13: Kategori cuti tahunan error 422

**Root Cause**: Setelah analisis, frontend mengirim `state.selectedJenis.name.lowercase()` → `"cuti_tahunan"` yang SAMA dengan validasi backend `in:cuti_tahunan,izin_sakit,mendadak`. **Tidak ada mismatch**.

Kemungkinan 422 datang dari:
- Field `keterangan` kosong atau < 10 karakter (backend `nullable`, tapi frontend enforce min 10)
- Atau field tidak terkirim karena serialization issue

**Fix**: 
- Backend sudah benar (`keterangan` nullable).
- Jika user melaporkan 422, kemungkinan body request tidak lengkap. Tambahkan logging di [`PengajuanController::store()`](coda-suaka-backend/app/Http/Controllers/PengajuanController.php:58) untuk debug.
- **Tidak perlu fix code** — ini bukan bug, tapi perlu verifikasi di environment testing.

---

### BUG #14: Karyawan tanpa jatah cuti bisa ajukan izin cuti

**Root Cause**: [`PengajuanController::store()`](coda-suaka-backend/app/Http/Controllers/PengajuanController.php:65) check `$karyawan->sisa_cuti <= 0`. Default `sisa_cuti = 0` di migration. Jadi seharusnya blocked. Tapi masalahnya: admin bisa edit `sisa_cuti` via [`KaryawanController::update()`](coda-suaka-backend/app/Http/Controllers/KaryawanController.php:138) menjadi value positif tanpa validasi apakah karyawan seharusnya punya jatah cuti.

**Fix — Backend**:
- Di [`KaryawanController::update()`](coda-suaka-backend/app/Http/Controllers/KaryawanController.php:138), pastikan `sisa_cuti` hanya bisa di-update oleh user dengan permission `manage:karyawan`:
  ```php
  // Sudah ada via policy, tapi tambahkan validasi:
  // sisa_cuti tidak boleh negatif
  if (isset($validated['sisa_cuti']) && $validated['sisa_cuti'] < 0) {
      return $this->error('Sisa cuti tidak boleh negatif', 422);
  }
  ```
- **Tidak perlu fix tambahan** — logika sudah benar: `sisa_cuti <= 0` → blocked. Yang perlu dipastikan adalah admin tidak sembarangan set `sisa_cuti` tinggi untuk karyawan yang tidak seharusnya punya cuti.

**File**: [`PengajuanController.php`](coda-suaka-backend/app/Http/Controllers/PengajuanController.php:65)

---

### BUG #15: Pemilik harus daftar outlet dulu

**Root Cause**: [`StorekaryawanRequest`](coda-suaka-backend/app/Http/Requests/StorekaryawanRequest.php) requires `outlet_id`. Ini BENAR dari sisi backend. Tapi UX-nya: Owner harus buat outlet dulu sebelum bisa tambah karyawan. Jika Owner belum punya outlet, mereka tidak bisa tambah karyawan.

**Fix — Backend (Opsi A — recommended)**:
- Buat default outlet otomatis saat Owner pertama kali buat instansi:
  - Di [`AuthController::register()`](coda-suaka-backend/app/Http/Controllers/AuthController.php) atau [`SuperAdminController::storeInstansi()`](coda-suaka-backend/app/Http/Controllers/SuperAdminController.php), setelah buat instansi, otomatis buat outlet "Outlet Utama" dengan `instansi_id` yang sesuai.

**Fix — Backend (Opsi B)**:
- Buat `outlet_id` nullable di [`StorekaryawanRequest`](coda-suaka-backend/app/Http/Requests/StorekaryawanRequest.php):
  ```php
  'outlet_id' => [
      'nullable', // Changed from required
      Rule::exists('outlets', 'id')->where('instansi_id', $user->instansi_id),
  ],
  ```

**Fix — Frontend**:
- Di [`KelolaKaryawanViewModel`](coda-suaka-frontend/app/src/main/java/com/example/codasuaka/ui/screen/kelola_karyawan/KelolaKaryawanViewModel.kt:269), jika `outlets` kosong, tampilkan pesan: "Buat outlet terlebih dahulu sebelum menambah karyawan."
- Atau tampilkan dialog引导 user ke halaman outlet.

**Rekomendasi**: Opsi A — auto-create default outlet. Lebih user-friendly.

**Files**: [`StorekaryawanRequest.php`](coda-suaka-backend/app/Http/Requests/StorekaryawanRequest.php), [`AuthController.php`](coda-suaka-backend/app/Http/Controllers/AuthController.php), [`KelolaKaryawanViewModel.kt`](coda-suaka-frontend/app/src/main/java/com/example/codasuaka/ui/screen/kelola_karyawan/KelolaKaryawanViewModel.kt)

---

## Urutan Implementasi (Layer-wise, sesuai SOP)

| Urutan | Layer | Bug | File |
|--------|-------|-----|------|
| 1 | Database/Migration | #10 | New migration for indexes |
| 2 | Backend Validation | #12 | [`StorejadwalRequest.php`](coda-suaka-backend/app/Http/Requests/StorejadwalRequest.php) |
| 3 | Backend Policy | #3 | [`PenugasanPolicy.php`](coda-suaka-backend/app/Policies/PenugasanPolicy.php) |
| 4 | Backend Service | — | [`NotificationService.php`](coda-suaka-backend/app/Services/NotificationService.php) (no change needed) |
| 5 | Backend Controller | #1, #2, #5, #11, #14 | [`PenugasanController.php`](coda-suaka-backend/app/Http/Controllers/PenugasanController.php), [`AttandenceController.php`](coda-suaka-backend/app/Http/Controllers/AttandenceController.php), [`KaryawanController.php`](coda-suaka-backend/app/Http/Controllers/KaryawanController.php) |
| 6 | Backend Route | #8 | [`api.php`](coda-suaka-backend/routes/api.php) |
| 7 | Frontend ViewModel | #3, #6, #9, #15 | [`PenugasanViewModel.kt`](coda-suaka-frontend/app/src/main/java/com/example/codasuaka/ui/screen/penugasan/PenugasanViewModel.kt), [`NotificationViewModel.kt`](coda-suaka-frontend/app/src/main/java/com/example/codasuaka/ui/screen/notifikasi/NotificationViewModel.kt), [`DivisiViewModel.kt`](coda-suaka-frontend/app/src/main/java/com/example/codasuaka/ui/screen/divisi/DivisiViewModel.kt), [`KelolaKaryawanViewModel.kt`](coda-suaka-frontend/app/src/main/java/com/example/codasuaka/ui/screen/kelola_karyawan/KelolaKaryawanViewModel.kt) |
| 8 | Frontend Error Handling | #7 | [`ErrorMessageMapper.kt`](coda-suaka-frontend/app/src/main/java/com/example/codasuaka/util/ErrorMessageMapper.kt) |
| 9 | Testing | All | Backend tests + manual testing checklist |

---

## Tech Stack Compliance (🔒 SOP Bagian 1)

| Item | Current | Required | Status |
|------|---------|----------|--------|
| Laravel | 12.x | 12.x | ✅ |
| PHP | 8.4 | 8.4 | ✅ |
| MySQL | 5.7.44 | 5.7.44 | ✅ (no CTE/window func) |
| Kotlin | 2.0.21 | 2.0.21 | ✅ |
| Compose BOM | 2024.09 | 2024.09 | ✅ |
| Koin | 3.5.6 | 3.5.6 | ✅ |
| Retrofit | 3.0.0 | 3.0.0 | ✅ |
| Min SDK | 26 | 26 | ✅ |
| Target SDK | 36 | 36 | ✅ |
| Package | com.example.codasuaka | com.example.codasuaka | ✅ |

---

**⚠️ MENUNGGU APPROVAL USER SEBELUM IMPLEMENTASI**
