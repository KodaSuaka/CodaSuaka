# Walkthrough: Penyelarasan UI Filter Riwayat Nota

Saya telah memperbarui desain area filter pada layar **Riwayat Nota** agar identik dengan gaya visual yang ada di **Laporan Keuangan**, sesuai dengan instruksi dan referensi foto yang Anda berikan.

## Perubahan yang Dilakukan

### 1. Standarisasi Filter Chips
- **Background:** Chip yang terpilih sekarang menggunakan warna biru sangat muda (`Primary.copy(alpha = 0.08f)`) dan chip yang tidak terpilih berwarna putih bersih.
- **Border & Shape:** Menambahkan border tipis (`1.dp`) dengan warna `NeutralBorder` (unselected) dan `Primary.copy(alpha = 0.2f)` (selected). Bentuk chip sekarang lebih membulat dengan `RoundedCornerShape(12.dp)`.
- **Warna Teks:** Teks chip terpilih sekarang berwarna **Biru Sky (`Primary`)** dan teks tidak terpilih berwarna **Biru Navy (`Secondary`)** yang tegas.

### 2. Tombol Tanggal (Style Pill)
- Mengubah tombol "Tanggal" dari gaya kotak biru pekat menjadi gaya **Pill** yang elegan, mengikuti desain periode di Laporan Keuangan.
- Menggunakan latar belakang `Primary.copy(alpha = 0.08f)` dan border tipis, memberikan kesan yang lebih ringan dan modern.

### 3. Pembersihan Header
- Menghilangkan bayangan berat pada area filter dan menggantinya dengan `HorizontalDivider` tipis di bagian bawah untuk pemisah yang lebih halus.

## Hasil Verifikasi
- [x] Tampilan filter chips di Riwayat Nota kini selaras dengan Laporan Keuangan.
- [x] Kontras teks pada tombol dan chip sudah optimal dan tidak bentrok dengan latar belakang.
- [x] Layout area filter terasa lebih lega dan profesional.

> [!TIP]
> Dengan menyamakan komponen antar layar seperti ini, aplikasi Anda akan terasa jauh lebih konsisten dan mudah dipelajari oleh pengguna baru.
