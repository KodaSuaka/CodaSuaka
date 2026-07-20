# Walkthrough Perbaikan Daftar Kontak (WhatsApp Style)

Saya telah merombak halaman Daftar Kontak menjadi lebih bersih dan intuitif, mengikuti pola desain daftar yang minimalis.

## Perubahan Utama

### 1. Desain List yang Bersih (Cardless)
Saya menghapus desain kotak-kotak (*Card*) yang kaku dan menggantinya dengan daftar baris (*List*) yang rapat namun rapi:
- **Horizontal Divider:** Memberikan pemisah visual yang halus antar kontak.
- **Lega (Spacing):** Menghilangkan *elevation* dan *shadow* yang berat untuk memberikan kesan aplikasi yang ringan dan cepat.

### 2. Pembaruan Navigasi & Aksi
- **Search Icon:** Menambahkan icon pencarian di pojok kanan atas Top Bar untuk akses cepat mencari nama kontak.
- **Unread Badge:** Memperbaiki tampilan indikator pesan belum terbaca agar lebih menonjol dengan background biru `Primary`.

### 3. Perbaikan Layout Detail Kontak
- **Pesan Terakhir:** Memperbaiki teks pratinjau pesan agar terpotong rapi (*Ellipsis*) jika terlalu panjang.
- **Waktu Pesan:** Menyeleraskan posisi waktu pesan terakhir di ujung kanan agar mudah dipantau.

## Hasil Verifikasi
- [x] **Build:** Sukses.
- [x] **Visual Check:** Daftar kontak sekarang tampil lebih profesional dan mampu menampilkan lebih banyak informasi dalam satu layar.
- [x] **Navigasi:** Navigasi ke detail chat tetap berfungsi normal dengan tampilan item yang lebih cantik.

> [!NOTE]
> Sesuai permintaan terbaru, fitur "Grup Baru", "Kontak Baru", dan tombol melayang "Pesan Baru" telah dihapus untuk menjaga kesederhanaan antarmuka.
