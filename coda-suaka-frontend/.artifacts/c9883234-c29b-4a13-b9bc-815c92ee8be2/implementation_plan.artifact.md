# Rencana Standardisasi Fitur Kalender (Benchmarking)

Rencana ini bertujuan untuk menyatukan logika dan tampilan kalender di seluruh aplikasi Coda Suaka, dimulai dari fitur **Nota Pembelian** sebagai standar acuan (*benchmark*). Komponen ini akan dibuat responsif dan menggunakan logika `java.time` yang lebih akurat.

## User Review Required

> [!IMPORTANT]
> - **Unified Component:** Saya akan membuat satu komponen `CodaSuakaDatePicker` yang menggabungkan navigasi bulan custom, pemilihan tahun, dan grid tanggal Material 3.
> - **Logika Modern:** Mengganti penggunaan `java.util.Calendar` yang usang dengan `java.time` untuk navigasi bulan yang lebih stabil dan akurat.

## Proposed Changes

### [Component] UI/UX Calendar Standardization

#### [NEW] [CodaSuakaDatePicker.kt](file:///C:/Users/ASUS/AndroidStudioProjects/CodaSuaka/coda-suaka-frontend/app/src/main/java/com/example/codasuaka/ui/components/CodaSuakaDatePicker.kt)
- Membuat komponen `@Composable fun CodaSuakaDatePickerDialog` yang bersifat mandiri (*self-contained*).
- **Header:** Mengintegrasikan `CustomCalendarNavigation` agar user bisa berpindah bulan dengan chevron atau klik judul untuk buka `YearPickerDialog`.
- **Logic:** Navigasi bulan (Prev/Next) ditangani secara internal menggunakan `LocalDate` dan `DatePickerState.displayMonthMillis`.
- **Responsive:** Memastikan layout dialog tetap rapi di berbagai ukuran layar dengan batasan tinggi (*height constraints*) yang tepat.

#### [MODIFY] [NotaPembelianScreen.kt](file:///C:/Users/ASUS/AndroidStudioProjects/CodaSuaka/coda-suaka-frontend/app/src/main/java/com/example/codasuaka/ui/screen/nota_pembelian/NotaPembelianScreen.kt)
- Menghapus implementasi `DatePickerDialog` standar.
- Menggunakan `CodaSuakaDatePickerDialog` baru sebagai benchmark implementasi kalender global.

---

## Verification Plan

### Manual Verification
- Buka pemilihan tanggal pada Nota Pembelian.
- Coba navigasi bulan (Prev/Next) dan pastikan grid tanggal terupdate dengan benar.
- Klik judul bulan untuk membuka pilihan tahun, pilih tahun, dan pastikan kalender melompat ke tahun tersebut.
- Pilih sebuah tanggal dan pastikan dialog tertutup serta tanggal pada form terupdate.
