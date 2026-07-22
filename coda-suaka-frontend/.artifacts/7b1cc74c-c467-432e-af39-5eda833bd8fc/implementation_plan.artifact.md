# Rencana Perbaikan Daftar Kontak (WhatsApp Style)

Rencana ini bertujuan untuk mengubah tampilan daftar kontak pada fitur pesan agar memiliki pengalaman pengguna yang serupa dengan WhatsApp, yaitu bersih, cepat, dan intuitif.

## User Review Required

> [!IMPORTANT]
> - **Penghapusan Card:** Saya akan menghapus desain "Card" pada tiap item kontak dan menggantinya dengan daftar baris (List) yang bersih dengan garis pemisah (Divider).
> - **Fitur Pencarian:** Saya akan menambahkan bar pencarian di bagian atas untuk memudahkan mencari teman atau rekan kerja.

## Proposed Changes

### 1. Refactor Daftar Kontak (Pesan)

#### [MODIFY] [ChatContactListScreen.kt](file:///C:/Users/ASUS/AndroidStudioProjects/CodaSuaka/coda-suaka-frontend/app/src/main/java/com/example/codasuaka/ui/screen/chat/ChatContactListScreen.kt)
- **TopAppBar**:
    - Menambahkan icon pencarian (Search) di pojok kanan atas.
    - Menghaluskan transisi antara judul dan konten.
- **Contact Item**:
    - Menghilangkan `Card` dan `elevation`. Menggunakan `Row` sederhana dengan background putih.
    - Menambahkan `HorizontalDivider` di antara setiap kontak.
    - Memperbaiki tata letak nama, pesan terakhir, dan indikator pesan belum terbaca agar lebih rapi.

### 2. Standarisasi Visual

#### [MODIFY] [ChatContactListScreen.kt](file:///C:/Users/ASUS/AndroidStudioProjects/CodaSuaka/coda-suaka-frontend/app/src/main/java/com/example/codasuaka/ui/screen/chat/ChatContactListScreen.kt)
- Menggunakan skema warna yang konsisten dengan desain baru kita (Putih Bersih + Biru Cerah).
- Mengatur padding agar area klik kontak terasa lebih lega dan nyaman bagi jempol.

---

## Tahapan Eksekusi

1. **Step 1: Refactor UI List** - Mengubah Card menjadi List datar dengan Divider.
2. **Step 2: Fitur Pencarian** - Menambahkan bar pencarian interaktif.
3. **Step 3: Verifikasi** - Memastikan navigasi ke detail chat tetap lancar dan tampilan terlihat "Segar" seperti WhatsApp.

## Verification Plan

### Manual Verification
- **Cek Visual:** Memastikan daftar kontak tampil rapat namun rapi dengan garis pemisah yang halus.
- **Uji Interaksi:** Mengetuk kontak dan memastikan masuk ke halaman percakapan dengan benar.
- **Cek Pencarian:** Mengetik nama di bar pencarian dan memastikan daftar tersaring (jika logic ViewModel sudah ada, atau menyiapkan UI-nya).
