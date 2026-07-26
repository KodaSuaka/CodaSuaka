# Perbaikan ErrorMessageMapper

Perbaikan bug referensi variabel yang tidak ditemukan dan pembersihan kode pada `ErrorMessageMapper.kt`.

## User Review Required

> [!IMPORTANT]
> Saya akan menghapus beberapa fungsi utilitas yang tidak digunakan (`success`, `warning`, `info`, dan overload `error`) untuk menjaga kebersihan kode. Jika fungsi-fungsi ini direncanakan untuk digunakan di masa depan, harap beri tahu saya.

## Proposed Changes

### Utility

#### [MODIFY] [ErrorMessageMapper.kt](file:///C:/Users/ASUS/AndroidStudioProjects/CodaSuaka/coda-suaka-frontend/app/src/main/java/com/example/codasuaka/util/ErrorMessageMapper.kt)

1.  **Perbaikan Bug:** Menambahkan parameter `msg: String` pada fungsi `mapHttpCode` agar variabel `msg` dapat diakses saat mengekstrak detail validasi (Bug #7).
2.  **Pembersihan Regex:** Menghapus escape karakter yang redundan pada `exceptionPattern`.
3.  **Optimalisasi:** Menghapus variabel `lower` yang tidak digunakan di `extractValidationDetails`.
4.  **Refactoring:** Menghapus fungsi-fungsi pembantu yang tidak dipanggil di mana pun dalam proyek (`success`, `warning`, `info`, dan overload `error`).

## Verification Plan

### Automated Tests
- Menjalankan `analyze_file` kembali untuk memastikan tidak ada error "Unresolved reference".
- Memastikan proyek dapat di-build dengan sukses menggunakan `gradle_build`.

### Manual Verification
- Memastikan tidak ada regresi pada pemetaan pesan error yang sudah ada.
