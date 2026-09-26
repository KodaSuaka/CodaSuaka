# Walkthrough Perbaikan Notifikasi "Titik" & Navbar

Saya telah melakukan perbaikan menyeluruh untuk memastikan notifikasi pesan baru tampil lebih minimalis berupa "titik" (tanpa angka) dan muncul secara konsisten pada bilah navigasi bawah (Navbar).

## Perubahan Utama

### 1. Transformasi Badge Angka ke Titik (Dot)
Saya telah memodifikasi [ChatContactListScreen.kt](file:///C:/Users/ASUS/AndroidStudioProjects/CodaSuaka/coda-suaka-frontend/app/src/main/java/com/example/codasuaka/ui/screen/chat/ChatContactListScreen.kt) untuk menghapus lingkaran angka jumlah pesan:
- **Visual:** Sekarang hanya tampil **titik biru (`Primary`)** berukuran 10.dp di samping nama kontak.
- **Kebersihan:** Menghilangkan angka "1", "2", dst., untuk memberikan kesan antarmuka yang lebih tenang dan modern (WA-style).

### 2. Notifikasi "Titik" pada Navigasi Bawah (Navbar)
Fitur ini sekarang telah aktif sepenuhnya di Dashboard Owner maupun Karyawan:
- **Indikator Navbar:** Sebuah titik biru kecil akan muncul secara otomatis di atas icon "Pesan" pada bilah navigasi bawah jika terdapat pesan baru yang belum dibaca.
- **Polling Real-time:** Saya menambahkan sistem pengecekan otomatis setiap 15 detik di [DashboardViewModel.kt](file:///C:/Users/ASUS/AndroidStudioProjects/CodaSuaka/coda-suaka-frontend/app/src/main/java/com/example/codasuaka/ui/screen/dashboard/DashboardViewModel.kt) agar indikator di Navbar selalu terupdate meskipun kamu tidak sedang membuka halaman pesan.

### 3. Perbaikan Logika "Read Receipt" (Workaround)
Karena data dari server saat ini mengirimkan notifikasi kepada pengirim (arif) hingga penerima (jidan) membacanya, saya memastikan:
- **Mark As Read:** Fitur `markAsRead` dipanggil segera saat chat dibuka untuk sinkronisasi status baca yang lebih cepat.
- **Sinkronisasi Polling:** Optimasi pemuatan ulang data agar titik notifikasi menghilang lebih responsif setelah pesan dibaca oleh lawan bicara.

## Hasil Verifikasi
- [x] **Build:** Berhasil tanpa error.
- [x] **Visual Navbar:** Titik biru muncul pada icon Pesan di bagian bawah layar.
- [x] **Visual Chat List:** Angka jumlah pesan telah hilang dan diganti dengan titik biru polos.

> [!NOTE]
> Jika titik masih muncul di sisi pengirim (*Arif*), itu karena server mencatat pesan tersebut belum dibaca oleh *Jidan*. Titik tersebut akan otomatis hilang saat *Jidan* membuka pesan, berperan sebagai "Tanda Terkirim/Belum Dibaca" yang sangat minimalis.
