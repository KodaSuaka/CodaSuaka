# Rencana Implementasi Sinkronisasi UI Dashboard Owner

Sinkronisasi desain UI pada `DashboardScreen` (Owner) agar memiliki tema warna, tombol, dan ikon yang konsisten dengan `DashboardKaryawanScreen`.

## Perubahan yang Diusulkan

### [Component] UI Screens

#### [MODIFY] [DashboardScreen.kt](file:///C:/Users/ASUS/AndroidStudioProjects/CodaSuaka/coda-suaka-frontend/app/src/main/java/com/example/codasuaka/ui/screen/dashboard/DashboardScreen.kt)

1.  **Penambahan Palette Warna:** Menambahkan konstanta warna yang sama dengan Dashboard Karyawan (`Teal`, `OceanBlue`, `Mint`, `Amber`, `Coral`, `ScoreGreen`).
2.  **Pembaruan `SectionOmset`:**
    *   Mengganti warna tren (`Success`) menjadi `ScoreGreen`.
    *   Mengganti warna tombol "Filter Data" agar lebih selaras (mungkin menggunakan `Primary` atau `Secondary` dengan style yang lebih modern).
3.  **Pembaruan `SectionMenuGrid` & `MenuCard`:**
    *   Memberikan variasi warna ikon pada `MenuItem` (seperti `Teal` untuk Outlet, `OceanBlue` untuk Jadwal, `Mint` untuk Absensi, dll) daripada hanya `Primary`/`Secondary`.
    *   Menyelaraskan opasitas background ikon (`0.1f`).
4.  **Pembaruan `DrawerContent`:**
    *   Mengganti warna `Error` pada menu Logout menjadi `Coral`.
    *   Menyelaraskan desain header drawer (avatar background, badge role).

## Rincian Mapping Warna Menu Owner:
*   **Kelola Outlet:** `Teal`
*   **Jadwal:** `OceanBlue`
*   **Log Absensi:** `Mint`
*   **Laporan Keuangan:** `Amber`
*   **Status Karyawan:** `Primary` (atau `Teal`)

## Verifikasi Plan

### Manual Verification
*   Membuka Dashboard Owner dan Dashboard Karyawan untuk memastikan konsistensi visual.
*   Memeriksa apakah warna tombol "Logout" di Drawer Owner sudah sama dengan Dashboard Karyawan (menggunakan `Coral`).
*   Memastikan variasi warna ikon pada menu grid Owner terlihat "setema" dengan Karyawan.
