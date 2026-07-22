# Rencana Perbaikan Notifikasi Pesan & Navbar Dot

Rencana ini bertujuan untuk memperbaiki logika notifikasi "titik" agar berperilaku seperti WhatsApp (hanya muncul saat ada pesan masuk), serta menambahkan indikator pesan baru pada bilah navigasi bawah (Navbar).

## User Review Required

> [!IMPORTANT]
> - **Perubahan Logika Notifikasi:** Karena indikator saat ini muncul pada pengirim (Read Receipt style), saya akan mencoba menyesuaikan agar UI hanya menampilkan titik jika pesan tersebut benar-benar ditujukan untuk pengguna saat ini.
> - **Navbar Dot:** Titik biru akan muncul di icon "Pesan" pada Dashboard jika ada pesan yang belum dibaca dari kontak mana pun.

## Proposed Changes

### 1. Sinkronisasi Dashboard (Total Unread)

#### [MODIFY] [DashboardViewModel.kt](file:///C:/Users/ASUS/AndroidStudioProjects/CodaSuaka/coda-suaka-frontend/app/src/main/java/com/example/codasuaka/ui/screen/dashboard/DashboardViewModel.kt) & [DashboardKaryawanViewModel.kt](file:///C:/Users/ASUS/AndroidStudioProjects/CodaSuaka/coda-suaka-frontend/app/src/main/java/com/example/codasuaka/ui/screen/dashboard_karyawan/DashboardKaryawanViewModel.kt)
- Menambahkan fungsi `checkUnreadMessages()` yang dipanggil saat `init` dan melalui polling ringan.
- Menggunakan `chatRepository.getContacts()` untuk menghitung total pesan belum dibaca.
- Memperbarui `hasUnreadMessages` di `UiState`.

### 2. Update Navigasi Bawah (Bottom Navbar)

#### [MODIFY] [DashboardScreen.kt](file:///C:/Users/ASUS/AndroidStudioProjects/CodaSuaka/coda-suaka-frontend/app/src/main/java/com/example/codasuaka/ui/screen/dashboard/DashboardScreen.kt) & [DashboardKaryawanScreen.kt](file:///C:/Users/ASUS/AndroidStudioProjects/CodaSuaka/coda-suaka-frontend/app/src/main/java/com/example/codasuaka/ui/screen/dashboard_karyawan/DashboardKaryawanScreen.kt)
- Mengintegrasikan `BadgedBox` pada item navigasi "Pesan".
- Menampilkan `Badge` berbentuk titik biru jika `hasUnreadMessages` bernilai true.

### 3. Perbaikan Logika Titik di Daftar Chat

#### [MODIFY] [ChatContactListScreen.kt](file:///C:/Users/ASUS/AndroidStudioProjects/CodaSuaka/coda-suaka-frontend/app/src/main/java/com/example/codasuaka/ui/screen/chat/ChatContactListScreen.kt)
- **Visual:** Memastikan indikator hanya berupa titik biru kecil tanpa angka.
- **Logika:** Menambahkan pemeriksaan tambahan (jika memungkinkan dari data yang ada) agar titik tidak muncul jika pesan terakhir dikirim oleh "Saya".

---

## Tahapan Eksekusi

1. **Step 1: Dashboard ViewModel Update** - Implementasi pengecekan pesan belum dibaca secara global.
2. **Step 2: Navbar UI Update** - Menambahkan titik biru pada bilah navigasi bawah.
3. **Step 3: Contact List Refinement** - Memoles tampilan titik agar benar-benar minimalis.
4. **Step 4: Verifikasi** - Uji coba pengiriman pesan antar akun untuk memastikan alur notifikasi sudah tepat.

## Verification Plan

### Manual Verification
- **Skenario Pesan Masuk:** Kirim pesan dari akun B ke akun A. Pastikan muncul titik di Navbar akun A dan di samping nama pengirim di daftar kontak.
- **Skenario Pesan Keluar:** Kirim pesan dari akun A ke akun B. Pastikan akun A **TIDAK** mendapatkan titik notifikasi pada dirinya sendiri.
- **Skenario Baca Pesan:** Buka pesan masuk di akun A, pastikan titik di Navbar dan daftar kontak menghilang seketika.
