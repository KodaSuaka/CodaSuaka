# Rencana Perbaikan Bug: Dialog Scroll & Fitur Stok

Tujuannya adalah memperbaiki masalah teknis pada form input karyawan yang tidak bisa di-scroll dan mendiagnosa/memperbaiki kegagalan pada fitur Stok.

## User Review Required

> [!IMPORTANT]
> - **Kelola Karyawan:** Saya akan menambahkan fitur scroll pada pop-up Tambah/Edit Karyawan. Ini sangat krusial jika input form melebihi tinggi layar HP Anda.
> - **Fitur Stok:** Masalah "Kesalahan tidak terduga" biasanya terjadi karena adanya ketidakcocokan data antara aplikasi dan server, atau pesan error server yang tersembunyi. Saya akan meningkatkan sistem pelaporan error agar kita bisa melihat alasan sebenarnya dari kegagalan tersebut (misal: "Nama stok sudah ada" atau "Format salah").

## Proposed Changes

### [Component] Kelola Karyawan - UI Fix

#### [MODIFY] [KelolaKaryawanScreen.kt](file:///C:/Users/ASUS/AndroidStudioProjects/CodaSuaka/coda-suaka-frontend/app/src/main/java/com/example/codasuaka/ui/screen/kelola_karyawan/KelolaKaryawanScreen.kt)
- Menambahkan `Modifier.verticalScroll(rememberScrollState())` pada `Column` utama di dalam `DialogTambahKaryawan` dan `DialogEditKaryawan`.
- Memberikan batasan tinggi maksimal (`heightIn(max = ...)`) agar dialog tidak "terpotong" di layar kecil.

---

### [Component] Stok & Error Handling - Diagnosis & Fix

#### [MODIFY] [ErrorMessageMapper.kt](file:///C:/Users/ASUS/AndroidStudioProjects/CodaSuaka/coda-suaka-frontend/app/src/main/java/com/example/codasuaka/util/ErrorMessageMapper.kt)
- Memperbaiki logika fallback agar tidak langsung menampilkan "Kesalahan tidak terduga" jika pesan error aslinya mengandung informasi berguna dari server.

#### [MODIFY] [StokRepositoryImpl.kt](file:///C:/Users/ASUS/AndroidStudioProjects/CodaSuaka/coda-suaka-frontend/app/src/main/java/com/example/codasuaka/data/repository/StokRepositoryImpl.kt)
- Menambahkan log atau meningkatkan parsing error body untuk memastikan kita menangkap alasan kegagalan dari API Laravel.

---

## Verification Plan

### Manual Verification
- **Karyawan:** Buka Tambah Karyawan, pastikan Anda bisa melakukan scroll dari Nama hingga tombol Simpan.
- **Stok:** Coba muat ulang halaman stok. Jika masih gagal, pesan error sekarang seharusnya lebih spesifik (bukan lagi "Kesalahan tidak terduga").
- **Tambah Stok:** Coba tambah stok lagi, dan lihat pesan error barunya untuk mengetahui apakah masalahnya ada di isian data atau server.
