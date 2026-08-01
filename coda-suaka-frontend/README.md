# 📱 CodaSuaka — Frontend (Android)

> Aplikasi Android native untuk manajemen bisnis (salon/spa) menggunakan Kotlin & Jetpack Compose. Mengelola karyawan, kehadiran, keuangan, penugasan, komunikasi internal, dan laporan.

---

## 📋 Daftar Isi

- [Arsitektur](#arsitektur)
- [Tech Stack](#tech-stack)
- [Struktur Direktori](#struktur-direktori)
- [Fitur](#fitur)
- [Layar Aplikasi](#layar-aplikasi)
- [Dependency Injection](#dependency-injection)
- [Navigasi](#navigasi)
- [Setup & Instalasi](#setup--instalasi)
- [Konfigurasi](#konfigurasi)

---

## Arsitektur

Aplikasi ini mengikuti prinsip **Clean Architecture** dengan pemisahan yang jelas antara layer **Data**, **Domain**, dan **UI**.

```
┌──────────────────────────────────────────────────────────────┐
│                         UI LAYER                             │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐   │
│  │   Screens    │  │  ViewModels  │  │   Components     │   │
│  │  (Compose)   │  │   (State)    │  │  (Reusable UI)   │   │
│  └──────┬───────┘  └──────┬───────┘  └──────────────────┘   │
│         │                 │                                  │
├─────────▼─────────────────▼──────────────────────────────────┤
│                       DOMAIN LAYER                           │
│  ┌──────────────────┐  ┌──────────────────────────────────┐  │
│  │   Repositories   │  │         Use Cases                │  │
│  │   (Interfaces)   │  │    (LoginUseCase, etc.)          │  │
│  └────────┬─────────┘  └──────────────────────────────────┘  │
│           │                                                  │
├───────────▼──────────────────────────────────────────────────┤
│                        DATA LAYER                            │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐   │
│  │ Repositories  │  │  API Service │  │  Token Manager   │   │
│  │   (Impl)      │  │  (Retrofit)  │  │  (DataStore)     │   │
│  └──────┬───────┘  └──────┬───────┘  └──────────────────┘   │
│         │                 │                                  │
└─────────▼─────────────────▼──────────────────────────────────┘
                           │
                    HTTP (JSON)
                           │
              ┌────────────▼────────────┐
              │   CodaSuaka Backend API  │
              │      (Laravel 13)        │
              └─────────────────────────┘
```

### Prinsip Arsitektur

| Prinsip | Penerapan |
|---------|-----------|
| **Clean Architecture** | Pemisahan `data/`, `domain/`, `ui/` — dependensi mengarah ke dalam |
| **MVVM** | Setiap screen memiliki ViewModel yang mengelola UI State |
| **Repository Pattern** | Interface di `domain/repository/`, implementasi di `data/repository/` |
| **Dependency Injection** | Koin — modul terpisah: [`AppModule`](app/src/main/java/com/example/codasuaka/di/AppModule.kt), [`DataModule`](app/src/main/java/com/example/codasuaka/di/DataModule.kt), [`DomainModule`](app/src/main/java/com/example/codasuaka/di/DomainModule.kt), [`ViewModelModule`](app/src/main/java/com/example/codasuaka/di/ViewModelModule.kt) |
| **Single Source of Truth** | [`ApiService`](app/src/main/java/com/example/codasuaka/data/remote/ApiService.kt) — satu interface untuk semua endpoint backend |
| **Encrypted Storage** | Token disimpan di DataStore + Security Crypto |

---

## Tech Stack

| Komponen | Teknologi | Versi |
|----------|-----------|-------|
| **Bahasa** | Kotlin | 2.0.21 |
| **UI Framework** | Jetpack Compose + Material 3 | BOM 2024.09 |
| **Min SDK** | Android API 29 (Android 10) | — |
| **Target SDK** | Android API 36 | — |
| **Build System** | Gradle (Kotlin DSL) | AGP 8.13.2 |
| **Networking** | Retrofit + Gson Converter | 3.0.0 |
| **HTTP Client** | OkHttp + Logging Interceptor | 4.12.0 |
| **Dependency Injection** | Koin | 3.5.6 |
| **Navigation** | Navigation Compose | 2.7.7 |
| **Local Storage** | DataStore Preferences | 1.1.1 |
| **Encryption** | AndroidX Security Crypto | 1.1.0-alpha06 |
| **Async** | Kotlin Coroutines | 1.7.3 |
| **Lifecycle** | ViewModel + Runtime KTX | 2.7.0 / 2.6.1 |
| **Testing** | JUnit + MockK + Coroutines Test | — |

---

## Struktur Direktori

```
coda-suaka-frontend/
├── app/src/main/java/com/example/codasuaka/
│   ├── BaseApplication.kt                 # Application class (Koin init)
│   ├── MainActivity.kt                    # Entry point activity
│   │
│   ├── data/                              # ══ DATA LAYER ══
│   │   ├── local/
│   │   │   ├── AppDatabase.kt             # Room database (local cache)
│   │   │   └── TokenManager.kt            # Encrypted token storage
│   │   ├── remote/
│   │   │   ├── ApiService.kt              # Retrofit API interface (459 baris)
│   │   │   ├── dto/                       # Data Transfer Objects
│   │   │   │   ├── ApiResponse.kt         # Base response wrapper
│   │   │   │   ├── LoginRequest.kt        # Login request body
│   │   │   │   ├── LoginResponse.kt       # Login response
│   │   │   │   ├── GenericResponses.kt    # Semua DTO response
│   │   │   │   ├── ChatContactResponse.kt
│   │   │   │   ├── ChatMessageResponse.kt
│   │   │   │   ├── RegisterRequest.kt
│   │   │   │   └── RegisterResponse.kt
│   │   │   └── interceptor/
│   │   │       └── AuthInterceptor.kt     # Attach token ke setiap request
│   │   └── repository/                    # Repository implementations
│   │       ├── AuthRepositoryImpl.kt
│   │       ├── ChatRepositoryImpl.kt
│   │       ├── DashboardRepositoryImpl.kt
│   │       ├── DivisiRepositoryImpl.kt
│   │       ├── JadwalRepositoryImpl.kt
│   │       ├── KaryawanRepositoryImpl.kt
│   │       ├── KeuanganRepositoryImpl.kt
│   │       ├── NotificationRepositoryImpl.kt
│   │       ├── OutletRepositoryImpl.kt
│   │       ├── PengajuanRepositoryImpl.kt
│   │       ├── PenugasanRepositoryImpl.kt
│   │       └── PresensiRepositoryImpl.kt
│   │
│   ├── domain/                            # ══ DOMAIN LAYER ══
│   │   ├── model/
│   │   │   └── User.kt                   # Domain model
│   │   ├── repository/                    # Repository interfaces
│   │   │   ├── AuthRepository.kt
│   │   │   ├── ChatRepository.kt
│   │   │   ├── DashboardRepository.kt
│   │   │   ├── DivisiRepository.kt
│   │   │   ├── JadwalRepository.kt
│   │   │   ├── KaryawanRepository.kt
│   │   │   ├── KeuanganRepository.kt
│   │   │   ├── NotificationRepository.kt
│   │   │   ├── OutletRepository.kt
│   │   │   ├── PengajuanRepository.kt
│   │   │   ├── PenugasanRepository.kt
│   │   │   └── PresensiRepository.kt
│   │   └── usecase/
│   │       ├── LoginUseCase.kt
│   │       └── RegisterUseCase.kt
│   │
│   ├── ui/                                # ══ UI LAYER ══
│   │   ├── components/                    # Reusable compose components
│   │   │   ├── CalendarNavigation.kt
│   │   │   ├── MonthYearPickerDialog.kt
│   │   │   ├── NotificationBanner.kt
│   │   │   └── SectionHeaderNavigation.kt
│   │   ├── screen/                        # Semua layar aplikasi
│   │   │   ├── auth/                      # Auth gatekeeper
│   │   │   ├── login/                     # Login screen
│   │   │   ├── register/                  # Register screen
│   │   │   ├── dashboard/                 # Dashboard Owner
│   │   │   ├── dashboard_karyawan/        # Dashboard Karyawan
│   │   │   ├── kelola_outlet/             # Kelola Outlet
│   │   │   ├── kelola_karyawan/           # Kelola Karyawan
│   │   │   ├── divisi/                    # Kelola Divisi
│   │   │   ├── riwayat_kehadiran/         # Riwayat Kehadiran
│   │   │   ├── kalender/                  # Kalender Jadwal
│   │   │   ├── pengajuan/                 # Pengajuan Cuti/Izin/Sakit
│   │   │   ├── penugasan/                 # Penugasan / Tugas
│   │   │   ├── laporan_keuangan/          # Laporan Keuangan
│   │   │   ├── approval_keuangan/         # Approval Transaksi
│   │   │   ├── chat/                      # Chat (Kontak + Detail)
│   │   │   ├── notifikasi/                # Notifikasi
│   │   │   ├── poin_kinerja/              # Poin Kinerja
│   │   │   └── components/                # Shared screen components
│   │   └── theme/                         # Tema & styling
│   │       ├── Color.kt
│   │       ├── Theme.kt
│   │       └── Type.kt
│   │
│   ├── navigation/
│   │   └── AppNavigation.kt              # Route definitions & NavHost
│   │
│   ├── di/                                # ══ DEPENDENCY INJECTION ══
│   │   ├── AppModule.kt                   # TokenManager, global deps
│   │   ├── DataModule.kt                  # ApiService, Repositories
│   │   ├── DomainModule.kt               # Use Cases
│   │   └── ViewModelModule.kt            # All ViewModels
│   │
│   └── util/
│       └── ErrorMessageMapper.kt          # Error message localization
│
├── build.gradle.kts                       # Root build config
├── app/build.gradle.kts                   # App module build config
└── gradle/
    └── libs.versions.toml                 # Version catalog
```

---

## Fitur

### 🔐 Autentikasi
- **Login** — email & password, token disimpan terenkripsi (DataStore + Security Crypto)
- **Register** — daftar akun baru sebagai Owner
- **Auto-Login** — cek token saat aplikasi dibuka, redirect ke dashboard jika valid
- **Logout** — hapus token & navigasi ke login

### 👑 Dashboard Owner
- **Ringkasan Bisnis** — statistik karyawan, outlet, penugasan
- **Omset** — data omset dengan filter rentang tanggal
- **Akses Cepat** — shortcut ke semua modul utama

### 👷 Dashboard Karyawan
- **Presensi Hari Ini** — status check-in/check-out
- **Penugasan Aktif** — daftar tugas yang perlu dikerjakan
- **Poin Kinerja** — skor kinerja kumulatif

### 🏢 Kelola Outlet
- **CRUD Outlet** — tambah, edit, hapus data outlet/lokasi
- **Detail Outlet** — alamat, kontak, informasi lokasi

### 👥 Kelola Karyawan (HRD)
- **CRUD Karyawan** — data karyawan lengkap (nama, jabatan, kontak)
- **Filter per Outlet** — filter karyawan berdasarkan lokasi

### 🏗️ Kelola Divisi
- **CRUD Divisi** — organisasi divisi per outlet
- **Kelola Anggota** — assign karyawan ke divisi

### ⏰ Presensi & Kehadiran
- **Check-in / Check-out** — presensi satu klik
- **Riwayat Kehadiran** — rekap kehadiran per bulan dengan navigasi kalender

### 📋 Penugasan
- **Daftar Penugasan** — filter berdasarkan divisi, status, penanggung jawab
- **Detail Penugasan** — urgensi, poin kinerja, deadline
- **Template Penugasan** — template tugas berulang

### 📅 Kalender / Jadwal
- **Kalender Interaktif** — navigasi bulanan, pilih tanggal
- **Jadwal Kerja** — jadwal harian per outlet

### 📝 Pengajuan
- **Buat Pengajuan** — cuti, izin, sakit dengan pilihan tanggal
- **Setujui / Tolak** — approve/reject pengajuan (untuk Manager/Owner)

### 💰 Laporan Keuangan
- **Transaksi Kas** — daftar transaksi masuk/keluar
- **Saldo** — informasi saldo saat ini
- **Laba Rugi** — laporan laba/rugi
- **Arus Kas** — laporan arus kas per periode
- **Ringkasan Keuangan** — ringkasan tahunan
- **Export PDF/Excel** — unduh laporan dalam format PDF atau Excel

### ✅ Approval Keuangan
- **Approval Pending** — daftar transaksi menunggu persetujuan
- **Riwayat Approval** — histori approve/reject

### 💬 Chat
- **Daftar Kontak** — kontak sesama karyawan dalam instansi
- **Pesan Real-time** — kirim & terima pesan
- **Read Receipt** — tanda pesan sudah dibaca

### 🔔 Notifikasi
- **Daftar Notifikasi** — notifikasi aktivitas sistem
- **Unread Badge** — badge jumlah notifikasi belum dibaca
- **Mark as Read** — tandai sudah dibaca (per item / semua)

### 📊 Poin Kinerja
- **Skor Kinerja** — poin dari penugasan yang diselesaikan

### 🛒 Kasir (POS)
- **Kasir** — layar POS: pilih produk (grid barang/jasa), keranjang, checkout jadi nota penjualan
- **Kelola Barang/Jasa** — CRUD katalog item + stok
- **Nota Pembelian** — buat nota pembelian (item dari katalog / lepas)
- **Riwayat Nota** — daftar nota penjualan & pembelian
- **Detail Nota** — rincian item nota + cetak PDF
- Terhubung ke API `/api/notas` & `/api/barang-jasas` ([README backend](../coda-suaka-backend/README.md))

---

## Layar Aplikasi


| Screen | ViewModel | Deskripsi |
|--------|-----------|-----------|
| [`AuthScreen`](app/src/main/java/com/example/codasuaka/ui/screen/auth/AuthScreen.kt) | `AuthViewModel` | Gatekeeper — cek token & navigasi |
| [`LoginScreen`](app/src/main/java/com/example/codasuaka/ui/screen/login/LoginScreen.kt) | `LoginViewModel` | Form login |
| [`RegisterScreen`](app/src/main/java/com/example/codasuaka/ui/screen/register/RegisterScreen.kt) | `RegisterViewModel` | Form register |
| [`DashboardScreen`](app/src/main/java/com/example/codasuaka/ui/screen/dashboard/DashboardScreen.kt) | `DashboardViewModel` | Dashboard Owner |
| [`DashboardKaryawanScreen`](app/src/main/java/com/example/codasuaka/ui/screen/dashboard_karyawan/DashboardKaryawanScreen.kt) | `DashboardKaryawanViewModel` | Dashboard Karyawan |
| [`KelolaOutletScreen`](app/src/main/java/com/example/codasuaka/ui/screen/kelola_outlet/KelolaOutletScreen.kt) | `KelolaOutletViewModel` | CRUD Outlet |
| [`KelolaKaryawanScreen`](app/src/main/java/com/example/codasuaka/ui/screen/kelola_karyawan/KelolaKaryawanScreen.kt) | `KelolaKaryawanViewModel` | CRUD Karyawan |
| [`DivisiScreen`](app/src/main/java/com/example/codasuaka/ui/screen/divisi/DivisiScreen.kt) | `DivisiViewModel` | Kelola Divisi |
| [`RiwayatKehadiranScreen`](app/src/main/java/com/example/codasuaka/ui/screen/riwayat_kehadiran/RiwayatKehadiranScreen.kt) | `RiwayatKehadiranViewModel` | Riwayat Kehadiran |
| [`KalenderScreen`](app/src/main/java/com/example/codasuaka/ui/screen/kalender/KalenderScreen.kt) | `KalenderViewModel` | Kalender Jadwal |
| [`PenugasanScreen`](app/src/main/java/com/example/codasuaka/ui/screen/penugasan/PenugasanScreen.kt) | `PenugasanViewModel` | Penugasan / Tugas |
| [`PengajuanScreen`](app/src/main/java/com/example/codasuaka/ui/screen/pengajuan/PengajuanScreen.kt) | `PengajuanViewModel` | Pengajuan Cuti/Izin/Sakit |
| [`LaporanKeuanganScreen`](app/src/main/java/com/example/codasuaka/ui/screen/laporan_keuangan/LaporanKeuanganScreen.kt) | `LaporanKeuanganViewModel` | Laporan Keuangan |
| [`ApprovalKeuanganScreen`](app/src/main/java/com/example/codasuaka/ui/screen/approval_keuangan/ApprovalKeuanganScreen.kt) | `ApprovalKeuanganViewModel` | Approval Transaksi |
| [`ChatContactListScreen`](app/src/main/java/com/example/codasuaka/ui/screen/chat/ChatContactListScreen.kt) | `ChatContactViewModel` | Daftar Kontak Chat |
| [`ChatDetailScreen`](app/src/main/java/com/example/codasuaka/ui/screen/chat/ChatDetailScreen.kt) | `ChatDetailViewModel` | Detail Chat |
| [`NotificationScreen`](app/src/main/java/com/example/codasuaka/ui/screen/notifikasi/NotificationScreen.kt) | `NotificationViewModel` | Notifikasi |
| [`PoinKinerjaScreen`](app/src/main/java/com/example/codasuaka/ui/screen/poin_kinerja/PoinKinerjaScreen.kt) | `PoinKinerjaViewModel` | Poin Kinerja |
| [`KasirScreen`](app/src/main/java/com/example/codasuaka/ui/screen/kasir/KasirScreen.kt) | `KasirViewModel` | Kasir / POS |
| [`KelolaBarangJasaScreen`](app/src/main/java/com/example/codasuaka/ui/screen/kelola_barang_jasa/KelolaBarangJasaScreen.kt) | `KelolaBarangJasaViewModel` | CRUD Barang/Jasa |
| [`NotaPembelianScreen`](app/src/main/java/com/example/codasuaka/ui/screen/nota_pembelian/NotaPembelianScreen.kt) | `NotaPembelianViewModel` | Buat Nota Pembelian |
| [`RiwayatNotaScreen`](app/src/main/java/com/example/codasuaka/ui/screen/riwayat_nota/RiwayatNotaScreen.kt) | `RiwayatNotaViewModel` | Riwayat Nota |
| [`NotaDetailScreen`](app/src/main/java/com/example/codasuaka/ui/screen/nota_detail/NotaDetailScreen.kt) | `NotaDetailViewModel` | Detail Nota + Cetak PDF |

---

## Dependency Injection

Menggunakan **Koin 3.5.6** dengan 4 modul terpisah:

| Modul | File | Tanggung Jawab |
|-------|------|----------------|
| `appModule` | [`AppModule.kt`](app/src/main/java/com/example/codasuaka/di/AppModule.kt) | `TokenManager`, dependency global |
| `dataModule` | [`DataModule.kt`](app/src/main/java/com/example/codasuaka/di/DataModule.kt) | `ApiService`, `AuthInterceptor`, semua Repository |
| `domainModule` | [`DomainModule.kt`](app/src/main/java/com/example/codasuaka/di/DomainModule.kt) | Use Cases (`LoginUseCase`, `RegisterUseCase`) |
| `viewModelModule` | [`ViewModelModule.kt`](app/src/main/java/com/example/codasuaka/di/ViewModelModule.kt) | Semua ViewModel (19 ViewModel) |

### Flow Dependency

```
ViewModel → Repository Interface (domain) → Repository Impl (data) → ApiService (Retrofit)
                ↑                                      ↑
           DomainModule                          DataModule
```

---

## Navigasi

Menggunakan **Navigation Compose** dengan route-based routing.

### Route Definitions

Semua route didefinisikan di [`AppNavigation.kt`](app/src/main/java/com/example/codasuaka/navigation/AppNavigation.kt):

```kotlin
object Routes {
    const val AUTH = "auth"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val DASHBOARD = "dashboard"              // Owner
    const val DASHBOARD_KARYAWAN = "dashboard_karyawan"  // Karyawan, Manager, Keuangan, Staff
    const val KELOLA_OUTLET = "kelola_outlet"
    const val KELOLA_KARYAWAN = "kelola_karyawan"
    const val KALENDER = "kalender"
    const val RIWAYAT_KEHADIRAN = "riwayat_kehadiran"
    const val DIVISI = "divisi"
    const val PENGAJUAN = "pengajuan"
    const val PENUGASAN = "penugasan"
    const val LAPORAN_KEUANGAN = "laporan_keuangan"
    const val APPROVAL_KEUANGAN = "approval_keuangan"
    const val CONTACT_LIST = "contact_list"
    const val CHAT_DETAIL = "chat_detail/{userId}/{userName}"
    const val NOTIFIKASI = "notifikasi"
    const val POIN_KINERJA = "poin_kinerja"
}
```

### Navigasi Berdasarkan Role

```
┌──────────┐
│   AUTH   │ ← Cek token
└────┬─────┘
     │
     ├── Token valid → Role == Owner? ──→ DASHBOARD
     │                     │
     │                     └── Role == Keuangan/Manager/Staff/Karyawan → DASHBOARD_KARYAWAN
     │
     └── Token tidak valid → LOGIN
                               │
                               ├── Register → REGISTER
                               └── Login Success → DASHBOARD / DASHBOARD_KARYAWAN
```

---

## Setup & Instalasi

### Prasyarat

- **Android Studio** Hedgehog (2023.1.1) atau lebih baru
- **JDK** 11+
- **Android SDK** 36 (akan didownload otomatis oleh Android Studio)
- **Device/emulator** dengan Android 10+ (API 29)
- Backend API CodaSuaka sudah berjalan (lihat [README Backend](../coda-suaka-backend/README.md))

### Langkah Instalasi

#### 1. Clone & Buka di Android Studio

```bash
# Clone repository
git clone <repo-url>
cd coda-suaka-frontend
```

Buka folder `coda-suaka-frontend` di Android Studio: **File → Open** → pilih folder project.

#### 2. Konfigurasi Base URL API

Buka file [`gradle.properties`](gradle.properties) dan tambahkan/base URL server backend:

```properties
# Untuk development (Android emulator → localhost)
BASE_URL=http://10.0.2.2:8000/api/

# Untuk produksi
# BASE_URL=https://your-domain.com/api/
```

> **Catatan:**
> - Android emulator menggunakan `10.0.2.2` sebagai alias untuk `localhost` di host machine
> - Pastikan backend sudah berjalan di URL yang sesuai
> - Base URL di [`build.gradle.kts`](app/build.gradle.kts) sudah memiliki default value, tapi bisa di-override via `gradle.properties`

#### 3. Build & Run

```bash
# Build debug APK via CLI
./gradlew assembleDebug

# Install ke device/emulator
./gradlew installDebug
```

Atau langsung dari Android Studio: klik ▶ **Run 'app'**.

### Build Variants

| Variant | Minify | Deskripsi |
|---------|--------|-----------|
| `debug` | ❌ | Untuk development, logging aktif |
| `release` | ✅ | ProGuard enabled, optimized |

### Testing

```bash
# Unit tests
./gradlew test

# Instrumented tests (memerlukan device/emulator)
./gradlew connectedAndroidTest
```

---

## Konfigurasi

### Base URL API

Base URL dikonfigurasi di [`build.gradle.kts`](app/build.gradle.kts:20) melalui `buildConfigField`:

```kotlin
buildConfigField("String", "BASE_URL",
    "\"${project.findProperty("BASE_URL") ?: "http://10.0.2.2:8000/api/"}\"")
```

Default value adalah `http://10.0.2.2:8000/api/` (untuk Android emulator). Untuk mengubah, edit file [`gradle.properties`](gradle.properties):

```properties
# Development (emulator)
BASE_URL=http://10.0.2.2:8000/api/

# Production
BASE_URL=https://your-domain.com/api/
```

> ⚠️ **Jangan hardcode URL produksi di source code.** Gunakan `gradle.properties` untuk konfigurasi per environment. Untuk CI/CD, pass value via command line:
> ```bash
> ./gradlew assembleRelease -PBASE_URL=https://your-domain.com/api/
> ```

### Auth Interceptor

[`AuthInterceptor.kt`](app/src/main/java/com/example/codasuaka/data/remote/interceptor/AuthInterceptor.kt) otomatis melampirkan token Sanctum ke setiap request API.

### Token Storage

Token disimpan menggunakan kombinasi:
- **DataStore Preferences** — penyimpanan key-value modern
- **Security Crypto** — enkripsi data sensitif

---

## Lisensi

MIT
