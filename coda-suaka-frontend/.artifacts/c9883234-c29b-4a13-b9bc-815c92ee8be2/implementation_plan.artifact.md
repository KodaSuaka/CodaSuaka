# Rencana Penyempurnaan Akhir & Perbaikan Bug (Force Close)

Tujuannya adalah memperbaiki crash pada Laporan Keuangan, menyelaraskan warna visual Kasir, merampingkan Splash Screen, serta menstandarisasi logika kalender di seluruh aplikasi.

## User Review Required

> [!IMPORTANT]
> - **Fix Force Close:** Crash saat membuka detail Laporan Keuangan disebabkan oleh data pembuat transaksi yang kosong/null. Saya akan memperbaiki DTO dan menambahkan proteksi null-safety.
> - **Audit Pelacakan:** Menampilkan informasi pembuat transaksi pada Laporan Keuangan. Untuk Nota, nama Kasir akan disertakan pada struk cetak.
> - **Logika Kalender:** Menyamakan logika perpindahan bulan menggunakan `java.time` agar sinkron dengan fitur Jadwal.
> - **Notifikasi Dinamis:** Notifikasi yang sudah dibaca akan otomatis hilang setelah 5 menit.

## Proposed Changes

### 1. Bug Fix & Audit (Integritas Data)

#### [MODIFY] [GenericResponses.kt](file:///C:/Users/ASUS/AndroidStudioProjects/CodaSuaka/coda-suaka-frontend/app/src/main/java/com/example/codasuaka/data/remote/dto/GenericResponses.kt) & [LoginResponse.kt](file:///C:/Users/ASUS/AndroidStudioProjects/CodaSuaka/coda-suaka-frontend/app/src/main/java/com/example/codasuaka/data/remote/dto/LoginResponse.kt)
- Mengubah `namaLengkap` pada `UserData` menjadi nullable (`String?`) untuk mencegah crash jika data dari server kosong.

#### [MODIFY] [LaporanKeuanganScreen.kt](file:///C:/Users/ASUS/AndroidStudioProjects/CodaSuaka/coda-suaka-frontend/app/src/main/java/com/example/codasuaka/ui/screen/laporan_keuangan/LaporanKeuanganScreen.kt)
- Memperbaiki pemanggilan `user.namaLengkap` dengan null-safety (`?: "System"`) untuk mencegah force close.

---

### 2. Visual & UX Polish

#### [MODIFY] [KasirScreen.kt](file:///C:/Users/ASUS/AndroidStudioProjects/CodaSuaka/coda-suaka-frontend/app/src/main/java/com/example/codasuaka/ui/screen/kasir/KasirScreen.kt)
- Memastikan semua teks nominal uang menggunakan warna **Hijau (`Success`)** agar lebih kontras dan tidak monoton.

#### [MODIFY] [AuthScreen.kt](file:///C:/Users/ASUS/AndroidStudioProjects/CodaSuaka/coda-suaka-frontend/app/src/main/java/com/example/codasuaka/ui/screen/auth/AuthScreen.kt)
- Mengatur `Spacer` antara logo dan teks menjadi `4.dp` agar terlihat lebih padat dan profesional.

---

### 3. Logika Kalender & Notifikasi

#### [MODIFY] [DashboardScreen.kt](file:///C:/Users/ASUS/AndroidStudioProjects/CodaSuaka/coda-suaka-frontend/app/src/main/java/com/example/codasuaka/ui/screen/dashboard/DashboardScreen.kt) & [RiwayatKehadiranScreen.kt](file:///C:/Users/ASUS/AndroidStudioProjects/CodaSuaka/coda-suaka-frontend/app/src/main/java/com/example/codasuaka/ui/screen/riwayat_kehadiran/RiwayatKehadiranScreen.kt)
- Menerapkan logika perpindahan bulan menggunakan `YearMonth` (Java Time) agar seragam dengan fitur Kalender Jadwal.

#### [MODIFY] [NotificationViewModel.kt](file:///C:/Users/ASUS/AndroidStudioProjects/CodaSuaka/coda-suaka-frontend/app/src/main/java/com/example/codasuaka/ui/screen/notifikasi/NotificationViewModel.kt)
- Implementasi penghapusan otomatis (*auto-delete*) notifikasi lokal setelah 5 menit dibaca.

---

## Verification Plan

### Manual Verification
- Klik salah satu transaksi di Laporan Keuangan, pastikan dialog muncul lancar (tidak crash) dan menampilkan "Dibuat Oleh".
- Cek layar Kasir, pastikan warna harga sudah hijau.
- Cek Splash Screen, pastikan jarak logo dan teks sudah nyaman dilihat.
- Coba pindah bulan pada filter Omset/Kehadiran, pastikan logikanya lancar.
