# Fix Plan: Dashboard Karyawan — Simplifikasi & Finalisasi

## Ringkasan

Dokumen ini mencatat perubahan yang dilakukan pada dashboard karyawan setelah melalui beberapa iterasi penyempurnaan. Perubahan mencakup file prototype HTML dan implementasi Android (Jetpack Compose).

---

## 1. Perbaikan Awal (Iterasi 1–3)

### Masalah
File [`prototype_view/dashboard_karyawan.html`](prototype_view/dashboard_karyawan.html) terpotong di baris 881 sehingga JSX `NewApp()` tidak memiliki tag penutup yang lengkap.

### Perbaikan
- File telah diperbaiki secara eksternal (isi lengkap 907+ baris).
- Link `<a>` di footer kartu presensi diganti dengan `<button>`.
- Tata letak perbandingan (SEBELUM/SESUDAH) dihapus, diganti pratinjau *single frame* dengan `App()` sebagai komponen utama.

---

## 2. Sistem Widget Dinamis (Iterasi 4 — DIBATALKAN)

### Rencana Awal
Menambahkan sistem widget yang berubah berdasarkan jenis usaha UMKM (bengkel, salon, toko, cafe, laundry).

### Status
**DIBATALKAN** atas permintaan pengguna. Dashboard dikembalikan ke struktur sederhana.

---

## 3. Struktur Final (Iterasi 5 — Disetujui)

Dashboard karyawan terdiri dari **5 bagian** dalam urutan berikut:

```
┌─────────────────────────────────────┐
│  Top Bar (Judul + Notifikasi)       │
├─────────────────────────────────────┤
│  1. Data Diri                       │
│     - Avatar, Nama, Jabatan         │
│     - Progress Bar Poin Performa    │
├─────────────────────────────────────┤
│  2. Presensi Hari Ini (1 Kartu)     │
│     - Tanggal                       │
│     - Info Shift (warna biru)       │
│     ─────────────────────────────── │
│     - Status Absensi + Tombol C/I   │
│     - Event Spesial (opsional)      │
│     ─────────────────────────────── │
│     - [Riwayat Presensi] [Jadwal]   │
│       (dua tombol di footer)        │
├─────────────────────────────────────┤
│  3. Menu Jabatan                    │
│     - Grid tombol sesuai role       │
│       (3 kolom: Laporan, Riwayat    │
│        Absensi, Tugas Tim)          │
├─────────────────────────────────────┤
│  4. Poin Kinerja                    │
│     - Card dengan skor poin         │
│     - Chevron ke detail             │
│     (TANPA daftar tugas)            │
├─────────────────────────────────────┤
│  5. Sisa Cuti                       │
│     - Card sisa cuti tahunan        │
│     - Chevron ke detail             │
│     (TANPA grid tambahan)           │
├─────────────────────────────────────┤
│  Bottom Nav (Dashboard/Pengajuan/   │
│             Pesan)                  │
└─────────────────────────────────────┘
```

### Bagian yang DIHAPUS
| Bagian | Alasan |
|--------|--------|
| Simulator pemilih jenis usaha | Tidak diperlukan |
| Widget dinamis per jenis UMKM | Tidak diperlukan |
| Daftar Tugas (`TaskItem`) | Disederhanakan ke Poin Kinerja saja |
| Grid tambahan di Cuti (`AdditionalCard`) | Dihapus |
| Ikon ekstra (Build, ContentCut, Coffee, dll) | Tidak lagi digunakan |

---

## 4. Perubahan pada File

### 4.1 `prototype_view/dashboard_karyawan.html`

**Struktur CSS**: Gaya ditulis dalam format *single-line* untuk penghematan ruang.

**Komponen React**:
- [`App()`](prototype_view/dashboard_karyawan.html:200) — Komponen utama dengan state `bottomNav`
- [`EmployeeInfo`](prototype_view/dashboard_karyawan.html:233) — Data diri karyawan
- [`PresensiCard`](prototype_view/dashboard_karyawan.html:257) — Kartu presensi terpadu
- [`RoleMenu`](prototype_view/dashboard_karyawan.html:313) — Menu jabatan (per div)
- [`PerformanceCard`](prototype_view/dashboard_karyawan.html:344) — Poin kinerja
- [`LeaveCard`](prototype_view/dashboard_karyawan.html:365) — Sisa cuti

**Data**:
- [`dummyData`](prototype_view/dashboard_karyawan.html:178) — Berisi `employee`, `shiftInfo`, `absensiStatus`, `absensiTime`, `specialEvent`, `showSpecialEvent`, `roleMenuItems`, `poinKinerja`, `sisaCuti`
- `roleMenuItems` — 3 item: Laporan, Riwayat Absensi, Tugas Tim
- `mapIcon` memetakan string ke komponen ikon Material

**Footer Presensi**: Dua tombol `button` dengan `className="presensi-btn"` untuk Riwayat Presensi dan Jadwal Shift. Gaya tombol:
- Primer (solid) untuk Riwayat Presensi
- Sekunder (outline) untuk Jadwal Shift

### 4.2 `DashboardKaryawanScreen.kt`

**Perubahan**:
| Area | Sebelum | Sesudah |
|------|---------|---------|
| `SectionPerformance` | 4 parameter: `poinKinerja`, `totalTugas`, `tugasSelesai`, `onDetailKinerjaClick` | 2 parameter: `poinKinerja`, `onDetailKinerjaClick` |
| `SectionTaskList` | Komponen utuh (daftar tugas dengan `Lihat Semua`) | **DIHAPUS** |
| `TaskItemCard` | Komponen kartu tugas per item | **DIHAPUS** |
| `SectionLeaveAndAdditional` | 4 parameter: `sisaCuti`, `additionalItems`, `onSisaCutiClick`, `onAdditionalItemClick` | 2 parameter: `sisaCuti`, `onSisaCutiClick` |
| `AdditionalCard` | Komponen grid tambahan | **DIHAPUS** |
| Panggilan di `DashboardKaryawanScreen` | `SectionPerformance` + `SectionTaskList` + `SectionLeaveAndAdditional` (with extras) | `SectionPerformance` + `SectionLeaveAndAdditional` (saja) |

**Fungsi yang tersisa**:
- [`DashboardKaryawanScreen`](coda-suaka-frontend/app/src/main/java/com/example/codasuaka/ui/screen/dashboard_karyawan/DashboardKaryawanScreen.kt:47) — Layar utama
- [`SectionEmployeeInfo`](coda-suaka-frontend/app/src/main/java/com/example/codasuaka/ui/screen/dashboard_karyawan/DashboardKaryawanScreen.kt:200) — Data diri
- [`SectionPresensiToday`](coda-suaka-frontend/app/src/main/java/com/example/codasuaka/ui/screen/dashboard_karyawan/DashboardKaryawanScreen.kt:300) — Presensi terpadu
- [`SectionRoleMenu`](coda-suaka-frontend/app/src/main/java/com/example/codasuaka/ui/screen/dashboard_karyawan/DashboardKaryawanScreen.kt:541) — Menu jabatan
- [`RoleMenuCard`](coda-suaka-frontend/app/src/main/java/com/example/codasuaka/ui/screen/dashboard_karyawan/DashboardKaryawanScreen.kt:572) — Kartu menu
- [`SectionPerformance`](coda-suaka-frontend/app/src/main/java/com/example/codasuaka/ui/screen/dashboard_karyawan/DashboardKaryawanScreen.kt:627) — Poin kinerja (disederhanakan)
- [`SectionLeaveAndAdditional`](coda-suaka-frontend/app/src/main/java/com/example/codasuaka/ui/screen/dashboard_karyawan/DashboardKaryawanScreen.kt:802) — Sisa cuti (disederhanakan)
- [`BottomNavigationBar`](coda-suaka-frontend/app/src/main/java/com/example/codasuaka/ui/screen/dashboard_karyawan/DashboardKaryawanScreen.kt:958) — Navigasi bawah
- [`mapIcon`](coda-suaka-frontend/app/src/main/java/com/example/codasuaka/ui/screen/dashboard_karyawan/DashboardKaryawanScreen.kt:1005) — Pemetaan ikon

### 4.3 `DashboardKaryawanViewModel.kt`

**Tidak ada perubahan** — semua state (`poinKinerja`, `totalTugas`, `tugasSelesai`, `daftarTugas`, `sisaCuti`, `additionalContent`) masih ada di `UiState`. Layar hanya menggunakan yang diperlukan.

---

## 5. Verifikasi

- [x] HTML prototype menampilkan 5 bagian dalam urutan yang benar
- [x] Semua JSX div tertutup dengan benar
- [x] Tombol footer presensi menggunakan `<button>` (bukan `<a>`)
- [x] Android menampilkan struktur yang sama dengan HTML
- [x] Tidak ada komponen `TaskItem`, `AdditionalCard`, atau widget yang tersisa di screen
- [x] File `plans/fix-dashboard-karyawan-html.md` diperbarui dengan dokumentasi final
