# Walkthrough Sinkronisasi UI Dashboard Owner

Saya telah memperbarui desain Dashboard Owner agar selaras dengan tema "Fresh & Soft" yang ada di Dashboard Karyawan.

## Perubahan yang Dilakukan

### 1. Sinkronisasi Palette Warna
Saya menambahkan palette warna khusus ke `DashboardScreen.kt` untuk memberikan variasi visual yang konsisten:
- `Teal`, `OceanBlue`, `Mint`, `Amber`, `Coral`, `ScoreGreen`.

### 2. Pembaruan Menu Utama & Ikon
Ikon pada menu grid Owner kini memiliki warna yang bervariasi namun tetap satu tema, menggantikan warna `Primary` yang monoton:
- **Kelola Outlet:** Menggunakan `Teal`.
- **Jadwal:** Menggunakan `OceanBlue`.
- **Log Absensi:** Menggunakan `Mint`.
- **Laporan Keuangan:** Menggunakan `Amber`.
- **Status Karyawan:** Menggunakan `Teal` (sebagai aksen).

### 3. Visual Omset & Tren
- Indikator tren kenaikan omset kini menggunakan warna `ScoreGreen` (hijau yang lebih segar) daripada hijau standar.
- Background ikon tren juga disesuaikan dengan opasitas soft (`0.1f`).

### 4. Perbaikan Drawer & Logout
- Tombol **Logout** di Drawer kini menggunakan warna `Coral`, memberikan kesan peringatan yang lembut namun jelas, konsisten dengan Dashboard Karyawan.
- **Header Drawer:** Dioptimalkan untuk menggunakan `uiState` secara langsung, memperbaiki peringatan performa Compose terkait akses `StateFlow.value` di dalam komposisi.

## Verifikasi
- ✅ Kode telah dianalisis dan tidak ditemukan error sintaks.
- ✅ Perubahan warna diterapkan pada komponen: `SectionOmset`, `MenuCard`, dan `DrawerItem`.
- ✅ Perbaikan akses state di `DrawerContent` telah diverifikasi.

> [!TIP]
> Sekarang Dashboard Owner terlihat lebih modern dan memiliki identitas visual yang sama dengan bagian aplikasi lainnya.
