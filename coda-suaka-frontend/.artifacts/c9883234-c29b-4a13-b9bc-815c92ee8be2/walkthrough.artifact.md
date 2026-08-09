# Walkthrough: Perbaikan Dialog Scroll & Diagnosa Fitur Stok

Saya telah memperbaiki masalah teknis pada pop-up kelola karyawan serta meningkatkan sistem pelaporan error pada fitur Stok agar masalah utamanya dapat teridentifikasi.

## Perubahan yang Dilakukan

### 1. Perbaikan Scroll (Kelola Karyawan)
- **Aksesibilitas Form:** Menambahkan fitur **vertical scroll** pada pop-up Tambah dan Edit Karyawan.
- **Adaptasi Layar:** Memberikan batasan tinggi maksimal pada dialog agar tetap nyaman digunakan di HP dengan layar kecil tanpa ada tombol yang terpotong di bagian bawah.

### 2. Diagnosa & Perbaikan Error (Fitur Stok)
- **Pesan Error Spesifik:** Memperbarui sistem [ErrorMessageMapper.kt](file:///C:/Users/ASUS/AndroidStudioProjects/CodaSuaka/coda-suaka-frontend/app/src/main/java/com/example/codasuaka/util/ErrorMessageMapper.kt) agar tidak langsung menyembunyikan error di balik pesan "Kesalahan tidak terduga". Jika server mengirimkan alasan kegagalan, aplikasi sekarang akan menampilkannya.
- **Parsing Error Robust:** Meningkatkan kemampuan [StokRepositoryImpl.kt](file:///C:/Users/ASUS/AndroidStudioProjects/CodaSuaka/coda-suaka-frontend/app/src/main/java/com/example/codasuaka/data/repository/StokRepositoryImpl.kt) dalam membaca pesan error mentah dari server, baik dalam format JSON maupun teks biasa.
- **Transparansi Dialog:** Menerapkan sistem pemetaan error yang sama ke dalam dialog Tambah/Mutasi Stok agar user tahu persis kolom mana yang bermasalah.

## Hasil Verifikasi
- [x] Pop-up Karyawan sekarang bisa digulir hingga ke tombol paling bawah.
- [x] Fitur Stok akan menampilkan pesan error yang lebih informatif jika terjadi kegagalan (membantu proses debugging lebih cepat).

> [!TIP]
> Dengan perbaikan ini, jika fitur Stok masih gagal, Anda akan melihat alasan teknisnya di layar (misal: "API tidak ditemukan" atau "Data wajib belum diisi"). Mohon infokan pesan tersebut jika masalah masih berlanjut.
