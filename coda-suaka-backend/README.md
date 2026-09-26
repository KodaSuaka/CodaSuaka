# 🚀 CodaSuaka — Backend API

> REST API backend untuk aplikasi manajemen bisnis (salon/spa) multi-tenant. Mengelola karyawan, kehadiran, keuangan, penugasan, komunikasi internal, dan laporan.

---

## 📋 Daftar Isi

- [Arsitektur](#arsitektur)
- [Tech Stack](#tech-stack)
- [Struktur Direktori](#struktur-direktori)
- [Fitur](#fitur)
- [Model & Database](#model--database)
- [Autentikasi & Autorisasi](#autentikasi--otorisasi)
- [API Endpoints](#api-endpoints)
- [Artisan Commands](#artisan-commands)
- [Setup & Instalasi](#setup--instalasi)
- [Konfigurasi](#konfigurasi)

---

## Arsitektur

```
┌──────────────────────────────────────────────────────────────┐
│                      CLIENT (Mobile / Web)                   │
└──────────────────────────┬───────────────────────────────────┘
                           │ HTTP (JSON)
┌──────────────────────────▼───────────────────────────────────┐
│                     LARAVEL 13 API                           │
│  ┌─────────────┐  ┌──────────────┐  ┌────────────────────┐  │
│  │   Sanctum   │  │   Policies   │  │  TenantScope       │  │
│  │   (Auth)    │  │  (AuthZ)     │  │  (Multi-Tenant)    │  │
│  └──────┬──────┘  └──────┬───────┘  └─────────┬──────────┘  │
│         │                │                     │              │
│  ┌──────▼────────────────▼─────────────────────▼──────────┐  │
│  │                    Controllers                         │  │
│  │  Auth │ Outlet │ Karyawan │ Divisi │ Keuangan │ Chat   │  │
│  │  Presensi │ Penugasan │ Jadwal │ Pengajuan │ Laporan  │  │
│  └──────────────────────────┬─────────────────────────────┘  │
│                             │                                │
│  ┌──────────────────────────▼─────────────────────────────┐  │
│  │                 Eloquent Models + Scopes                │  │
│  │        (TenantScope → filter otomatis instansi_id)     │  │
│  └──────────────────────────┬─────────────────────────────┘  │
│                             │                                │
│  ┌──────────────────────────▼─────────────────────────────┐  │
│  │              Database (SQLite / MySQL)                  │  │
│  └────────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────────┘
```

### Prinsip Arsitektur

| Prinsip | Penerapan |
|---------|-----------|
| **Multi-Tenant** | [`TenantScope`](app/Models/Scopes/TenantScope.php) — filter otomatis berdasarkan `instansi_id` user yang login |
| **RBAC** | [`config/permissions.php`](config/permissions.php) — single source of truth untuk semua permission, di-sync via artisan command |
| **Authorization** | [Policies](app/Policies/) — 15 policy untuk kontrol akses per-model |
| **Standardized Response** | [`ApiResponse`](app/Traits/ApiResponse.php) — trait untuk format JSON response konsisten (`success`, `error`, `paginated`) |
| **Form Validation** | [Form Requests](app/Http/Requests/) — 42 request class untuk validasi input terpisah dari controller |
| **Service Layer** | [`ApprovalService`](app/Services/ApprovalService.php), [`NotificationService`](app/Services/NotificationService.php), [`LaporanExportService`](app/Services/LaporanExportService.php) |

---

## Tech Stack

| Komponen | Teknologi | Versi |
|----------|-----------|-------|
| **Framework** | Laravel | 13.8 |
| **PHP** | PHP | ≥ 8.4 |
| **Autentikasi API** | Laravel Sanctum | 4.0 |
| **Database** | SQLite (default) / MySQL | — |
| **PDF Export** | barryvdh/laravel-dompdf | 3.1 |
| **Excel Export** | openspout/openspout | 4.24 |
| **Testing** | PHPUnit | 12.5 |
| **Code Style** | Laravel Pint | 1.27 |
| **Dev Tools** | Laravel Pail (logs), Laravel Tinker | — |

---

## Struktur Direktori

```
coda-suaka-backend/
├── app/
│   ├── Console/Commands/          # Artisan commands (sync permissions, reminder, cleanup)
│   ├── Http/
│   │   ├── Controllers/           # 26 controller (API endpoints)
│   │   ├── Middleware/             # RoleMiddleware, PermissionMiddleware
│   │   └── Requests/              # 42 Form Request validation classes
│   ├── Models/                    # Eloquent models + Scopes/
│   ├── Policies/                  # 15 authorization policies
│   ├── Providers/                 # AppServiceProvider (register policies & gates)
│   ├── Services/                  # ApprovalService, NotificationService, LaporanExportService
│   └── Traits/                    # ApiResponse (standardized JSON response)
├── config/
│   ├── keuangan.php               # Konfigurasi approval & cuti
│   ├── permissions.php            # Master permission registry & role mapping
│   └── roles.php                  # Konfigurasi role
├── database/
│   ├── factories/                 # Model factories untuk testing
│   ├── migrations/                # 30+ migration files
│   └── seeders/                   # Database seeders
├── resources/views/laporan/       # Blade templates untuk PDF export
│   ├── buku_kas_pdf.blade.php
│   ├── laba_rugi_pdf.blade.php
│   └── arus_kas_pdf.blade.php
├── routes/
│   ├── api.php                    # Semua API routes
│   └── console.php                # Schedule & artisan routes
├── tests/
│   └── Feature/Keuangan/          # Feature tests
└── docs/                          # Dokumentasi internal
```

---

## Fitur

### 🔐 Autentikasi & Otorisasi
- **Login / Register** — token-based auth via Sanctum
- **Super Admin** — role khusus untuk mengelola instansi, owner, dan paket
- **RBAC Granular** — permission per modul (`view`, `manage`, `delete`, `export`, `approve`)
- **6 Role** — Super Admin, Owner, Keuangan, Manager, Staff, Karyawan

### 🏢 Manajemen Instansi & Outlet
- **Instansi** — profil perusahaan (CRUD oleh Super Admin & Owner)
- **Outlet** — CRUD lokasi/branch dengan data alamat & kontak

### 👥 Manajemen Karyawan (HRD)
- **Karyawan** — CRUD data karyawan (nama, jabatan, kontak, outlet)
- **Divisi** — organisasi divisi per outlet
- **Anggota Divisi** — assign karyawan ke divisi

### ⏰ Presensi & Kehadiran
- **Check-in / Check-out** — presensi harian karyawan
- **Riwayat Kehadiran** — rekap kehadiran per bulan/tahun
- **Pengajuan** — pengajuan cuti, izin, sakit dengan alur approve/reject

### 📋 Penugasan & Jadwal
- **Penugasan** — buat & kelola tugas karyawan dengan urgensi & poin kinerja
- **Template Penugasan** — template tugas berulang (maks 10 per instansi)
- **Jadwal Kerja** — kalender jadwal per outlet

### 💰 Keuangan (Buku Kas)
- **Kategori Transaksi** — kategori pemasukan/pengeluaran
- **Transaksi Kas** — CRUD transaksi dengan status approval (masuk/keluar)
- **Saldo** — perhitungan saldo per outlet & global
- **Laba Rugi** — laporan laba/rugi

### 🛒 Kasir (POS)
- **Barang/Jasa** — katalog item dengan stok, harga jual/beli, status aktif
- **Nota** — nota penjualan & pembelian multi-item (dari katalog atau item lepas)
- **Impor Pembelian** — impor nota pembelian massal via Excel + lampiran
- **Cetak Nota** — export nota ke PDF (DomPDF)
- **Integrasi Keuangan** — tiap nota otomatis membuat `TransaksiKas` yang tertaut (`nota.transaksi_kas_id`): penjualan → kas masuk (tanpa approval), pembelian → kas keluar via `ApprovalService` (alur approval yang sama dengan input kas manual). Stok berkurang/bertambah atomik; hapus nota mengembalikan stok + menghapus entri kas terkait.

### 📊 Laporan & Ekspor
- **Arus Kas** — laporan arus kas per periode
- **Ringkasan Keuangan** — ringkasan tahunan
- **Export PDF** — buku kas, laba rugi, arus kas (via DomPDF)
- **Export Excel** — buku kas, arus kas (via Openspout)

### 💬 Chat / Komunikasi Internal
- **Kontak** — daftar kontak sesama karyawan dalam instansi yang sama
- **Pesan** — kirim & baca pesan antar karyawan
- **Read Receipt** — tanda pesan sudah dibaca

### 🔔 Notifikasi
- **In-App Notification** — notifikasi untuk aktivitas penting
- **Unread Count** — jumlah notifikasi belum dibaca
- **Mark Read** — tandai sudah dibaca (per item / semua)

### 📈 Dashboard
- **Dashboard Owner** — ringkasan data bisnis, omset, statistik
- **Dashboard Karyawan** — presensi hari ini, penugasan aktif, poin kinerja
- **Poin Kinerja** — skor kinerja karyawan berdasarkan penugasan

### 🛒 Paket & Transaksi Paket
- **Paket** — paket layanan yang dijual (dikelola Super Admin)
- **Transaksi Paket** — catatan penjualan paket ke pelanggan

### 🔍 Audit Log
- **Activity Logging** — pencatatan aktivitas sistem untuk audit trail (fitur advance)

### ✅ Approval Workflow
- **Approval Transaksi** — alur persetujuan transaksi keuangan dengan threshold nominal (fitur advance)

---

## Model & Database

### Model Utama

| Model | Deskripsi | Relasi Utama |
|-------|-----------|--------------|
| [`User`](app/Models/User.php) | Pengguna sistem | → Role, Karyawan, Outlet, Instansi |
| [`Instansi`](app/Models/Instansi.php) | Perusahaan/organisasi | → Users, Outlets |
| [`Outlet`](app/Models/outlet.php) | Lokasi/branch | → Instansi, Karyawans, Divisis |
| [`Karyawan`](app/Models/karyawan.php) | Data karyawan | → User, Outlet, Divisi |
| [`Role`](app/Models/role.php) | Role pengguna | → RolePermissions |
| [`Divisi`](app/Models/Divisi.php) | Divisi organisasi | → Outlet, AnggotaDivisi |
| [`Attandence`](app/Models/attandence.php) | Presensi/absensi | → Karyawan |
| [`Pengajuan`](app/Models/pengajuan.php) | Pengajuan cuti/izin/sakit | → Karyawan |
| [`Penugasan`](app/Models/penugasan.php) | Tugas karyawan | → Karyawan, Divisi |
| [`TemplatePenugasan`](app/Models/TemplatePenugasan.php) | Template tugas | → Penugasan |
| [`Jadwal`](app/Models/jadwal.php) | Jadwal kerja | → Outlet |
| [`Paket`](app/Models/paket.php) | Paket layanan | → TransaksiPaket |
| [`TransaksiPaket`](app/Models/transaksi_paket.php) | Penjualan paket | → Paket, Karyawan |
| [`KategoriTransaksi`](app/Models/KategoriTransaksi.php) | Kategori keuangan | → TransaksiKas |
| [`TransaksiKas`](app/Models/TransaksiKas.php) | Transaksi buku kas | → KategoriTransaksi, Outlet, Nota |
| [`BarangJasa`](app/Models/BarangJasa.php) | Katalog barang/jasa + stok | → NotaItem |
| [`Nota`](app/Models/Nota.php) | Nota penjualan/pembelian | → NotaItem, TransaksiKas, KategoriTransaksi, Outlet |
| [`NotaItem`](app/Models/NotaItem.php) | Baris item pada nota | → Nota, BarangJasa |
| [`Chat`](app/Models/Chat.php) | Pesan internal | → Users |
| [`Notification`](app/Models/Notification.php) | Notifikasi | → User |
| [`AuditLog`](app/Models/AuditLog.php) | Log aktivitas | — |
| [`ApprovalLog`](app/Models/ApprovalLog.php) | Log approval | → TransaksiKas |

---

## Autentikasi & Otorisasi

### Autentikasi (Sanctum)
Semua endpoint dilindungi token Sanctum. Endpoint publik hanya:
- `POST /api/register`
- `POST /api/register-super-admin`
- `POST /api/login`

### Otorisasi (RBAC)

Permission dikelola via [`config/permissions.php`](config/permissions.php) dan di-sync ke database menggunakan:

```bash
php artisan permission:sync            # Interactive
php artisan permission:sync --dry-run  # Preview saja
php artisan permission:sync --force    # Skip konfirmasi
```

### Role & Permission Matrix

| Modul | Super Admin | Owner | Keuangan | Manager | Staff | Karyawan |
|-------|:-----------:|:-----:|:--------:|:-------:|:-----:|:--------:|
| Presensi | — | ✅ CRUD | 👁 | ✅ CRUD | 👁 | — |
| Pengajuan | — | ✅ CRUD | — | ✅ CRUD | — | — |
| Divisi | — | ✅ CRUD | — | ✅ CRUD | — | — |
| Penugasan | — | ✅ CRUD | 👁 | ✅ CRUD | 👁 | — |
| Jadwal | — | ✅ CRUD | — | ✅ CRUD | 👁 | — |
| Karyawan | — | ✅ CRUD | — | ✅ CRUD | — | — |
| Outlet | — | ✅ CRUD | — | — | — | — |
| Keuangan | — | ✅ CRUD + Export | ✅ CRUD + Export | — | — | — |
| Laporan | — | ✅ CRUD | 👁 | — | — | — |
| Paket | ✅ CRUD | 👁 | — | — | — | — |
| Instansi | ✅ CRUD | ✅ Update | — | — | — | — |

> **Keterangan:** ✅ CRUD = Create/Read/Update/Delete, 👁 = Read-only, — = Tidak ada akses

---

## API Endpoints

### Auth
| Method | Endpoint | Deskripsi |
|--------|----------|-----------|
| POST | `/api/register` | Register akun baru |
| POST | `/api/register-super-admin` | Register Super Admin |
| POST | `/api/login` | Login |
| POST | `/api/logout` | Logout |
| GET | `/api/user` | Data user login |

### Dashboard
| Method | Endpoint | Deskripsi |
|--------|----------|-----------|
| GET | `/api/dashboard` | Dashboard Owner |
| GET | `/api/dashboard/omset` | Data omset |
| GET | `/api/karyawan/dashboard` | Dashboard Karyawan |
| GET | `/api/karyawan/poin-kinerja` | Poin kinerja |

### Manajemen Data
| Method | Endpoint | Deskripsi |
|--------|----------|-----------|
| GET/PUT | `/api/instansi` | Profil instansi |
| CRUD | `/api/outlets` | Kelola outlet |
| CRUD | `/api/roles` | Kelola role |
| CRUD | `/api/karyawans` | Kelola karyawan |
| CRUD | `/api/divisis` | Kelola divisi |
| GET/POST/DELETE | `/api/anggota-divisis` | Kelola anggota divisi |

### Presensi & Pengajuan
| Method | Endpoint | Deskripsi |
|--------|----------|-----------|
| GET | `/api/presensis` | Daftar presensi |
| POST | `/api/presensis/checkin` | Check-in |
| POST | `/api/presensis/checkout` | Check-out |
| GET | `/api/presensis/today` | Presensi hari ini |
| GET | `/api/rekap-kehadiran` | Rekap kehadiran |
| CRUD | `/api/pengajuans` | Kelola pengajuan |
| PUT | `/api/pengajuans/{id}/approve` | Setujui pengajuan |
| PUT | `/api/pengajuans/{id}/reject` | Tolak pengajuan |

### Penugasan & Jadwal
| Method | Endpoint | Deskripsi |
|--------|----------|-----------|
| CRUD | `/api/penugasans` | Kelola penugasan |
| CRUD | `/api/template-penugasans` | Kelola template penugasan |
| CRUD | `/api/jadwals` | Kelola jadwal |

### Keuangan
| Method | Endpoint | Deskripsi |
|--------|----------|-----------|
| CRUD | `/api/kategori-transaksis` | Kelola kategori transaksi |
| GET/POST/PUT/DELETE | `/api/transaksi-kas` | Kelola transaksi kas |
| GET | `/api/transaksi-kas/saldo` | Lihat saldo |
| GET | `/api/transaksi-kas/laporan/laba-rugi` | Laporan laba rugi |

### Kasir (POS)
| Method | Endpoint | Deskripsi |
|--------|----------|-----------|
| CRUD | `/api/barang-jasas` | Kelola katalog barang/jasa + stok |
| GET/POST | `/api/notas` | Daftar & buat nota penjualan/pembelian |
| POST | `/api/notas/import` | Impor nota pembelian massal via Excel |
| GET | `/api/notas/{nota}` | Detail nota beserta item |
| GET | `/api/notas/{nota}/pdf` | Cetak nota PDF |
| DELETE | `/api/notas/{nota}` | Hapus nota (kembalikan stok + entri kas) |

### Laporan & Ekspor
| Method | Endpoint | Deskripsi |
|--------|----------|-----------|
| GET | `/api/laporan/arus-kas` | Arus kas |
| GET | `/api/laporan/ringkasan-keuangan` | Ringkasan keuangan |
| GET | `/api/laporan/buku-kas/export/pdf` | Export buku kas PDF |
| GET | `/api/laporan/buku-kas/export/excel` | Export buku kas Excel |
| GET | `/api/laporan/laba-rugi/export/pdf` | Export laba rugi PDF |
| GET | `/api/laporan/arus-kas/export/pdf` | Export arus kas PDF |
| GET | `/api/laporan/arus-kas/export/excel` | Export arus kas Excel |

### Chat & Notifikasi
| Method | Endpoint | Deskripsi |
|--------|----------|-----------|
| GET | `/api/chat/contacts` | Daftar kontak |
| GET | `/api/chat/messages/{user}` | Pesan dengan user |
| POST | `/api/chat/send` | Kirim pesan |
| GET | `/api/notifications` | Daftar notifikasi |
| GET | `/api/notifications/unread-count` | Jumlah belum dibaca |

### Super Admin
| Method | Endpoint | Deskripsi |
|--------|----------|-----------|
| GET | `/api/super-admin/dashboard` | Dashboard SA |
| CRUD | `/api/super-admin/instansis` | Kelola instansi |
| CRUD | `/api/super-admin/owners` | Kelola owner |
| CRUD | `/api/super-admin/pakets` | Kelola paket |

> 📖 Untuk daftar lengkap, lihat [`routes/api.php`](routes/api.php)

---

## Artisan Commands

| Command | Deskripsi |
|---------|-----------|
| `php artisan permission:sync` | Sinkronisasi permission dari config ke database |
| `php artisan cleanup:expired-tokens` | Bersihkan token Sanctum yang expired |
| `php artisan reminder:pending-approval` | Kirim reminder untuk approval yang pending |
| `php artisan reminder:penugasan-deadline` | Kirim reminder untuk penugasan yang mendekati deadline |

---

## Setup & Instalasi

### Prasyarat

- **PHP** ≥ 8.4
- **Composer** (package manager PHP)
- **Node.js** ≥ 18 + npm (untuk Vite asset build)
- **SQLite** (default, tanpa konfigurasi tambahan) **atau** **MySQL** ≥ 8.0

### Langkah Instalasi

#### 1. Clone & Install Dependencies

```bash
# Clone repository
git clone <repo-url>
cd coda-suaka-backend

# Install PHP dependencies
composer install
```

#### 2. Setup Environment (.env)

```bash
# Copy template .env.example ke .env
copy .env.example .env      # Windows
# cp .env.example .env      # macOS / Linux

# Generate APP_KEY otomatis
php artisan key:generate
```

> ⚠️ **PENTING:** File `.env` berisi secrets (APP_KEY, database credentials) dan **sudah di-`.gitignore`**. Jangan pernah commit file `.env` ke repository!

#### 3. Konfigurasi Database

Buka file `.env` dan sesuaikan konfigurasi database:

**Opsi A — SQLite (default, tanpa install tambahan):**

```env
DB_CONNECTION=sqlite
```

Buat file database SQLite:

```bash
type nul > database\database.sqlite    # Windows
# touch database/database.sqlite       # macOS / Linux
```

**Opsi B — MySQL:**

```env
DB_CONNECTION=mysql
DB_HOST=127.0.0.1
DB_PORT=3306
DB_DATABASE=your_database_name
DB_USERNAME=your_username
DB_PASSWORD=your_password
```

> Ganti `your_database_name`, `your_username`, dan `your_password` dengan data database kamu. **Jangan commit file `.env` ke repository!**

#### 4. Konfigurasi URL Frontend (CORS)

Di file `.env`, sesuaikan `APP_URL` dan `FRONTEND_URLS`:

```env
APP_URL=http://localhost:8000
FRONTEND_URLS=http://localhost:8000,http://10.0.2.2:8000
```

- `APP_URL` — URL backend itu sendiri
- `FRONTEND_URLS` — URL frontend yang diizinkan akses API (Android emulator pakai `10.0.2.2` untuk mengakses localhost)
- Untuk production, tambahkan domain frontend: `FRONTEND_URLS=https://your-domain.com`

#### 5. Migration, Seed & Permissions

```bash
# Jalankan migrasi database
php artisan migrate

# Seed data awal (opsional — untuk development)
php artisan db:seed

# Sinkronisasi permission RBAC dari config/permissions.php ke database
php artisan permission:sync --force
```

#### 6. Build Assets

```bash
npm install
npm run build
```

#### 7. Jalankan Aplikasi

```bash
# Otomatis jalankan: server + queue + logs + vite (rekomendasi)
composer dev

# Atau manual:
php artisan serve
```

Aplikasi akan berjalan di `http://localhost:8000`.

### Testing

```bash
composer test
```

---

## Konfigurasi

### Environment Variables

| Variable | Deskripsi | Default | Contoh |
|----------|-----------|---------|---------|
| `APP_URL` | URL backend | `http://localhost` | `https://api.your-domain.com` |
| `DB_CONNECTION` | Driver database | `sqlite` | `sqlite` atau `mysql` |
| `DB_HOST` | Host database (MySQL) | `127.0.0.1` | `127.0.0.1` |
| `DB_PORT` | Port database (MySQL) | `3306` | `3306` |
| `DB_DATABASE` | Nama database | — | `codasuaka_db` |
| `DB_USERNAME` | Username database | — | `root` |
| `DB_PASSWORD` | Password database | — | `secret` |
| `FRONTEND_URLS` | URL frontend untuk CORS | `${APP_URL}` | `https://your-domain.com` |
| `APPROVAL_ENABLED` | Aktifkan approval workflow | `true` | `true` / `false` |
| `APPROVAL_THRESHOLD` | Threshold nominal untuk approval | `1000000` | `500000` |
| `CUTI_KUOTA_TAHUNAN_DEFAULT` | Kuota cuti default per tahun | `12` | `12` |

> ⚠️ Semua values di atas **contoh saja**. Isi sesuai kebutuhan server kamu di file `.env`. **Jangan commit `.env` ke repository!**

### Konfigurasi Keuangan

Lihat [`config/keuangan.php`](config/keuangan.php) untuk pengaturan:
- **Approval Workflow** — threshold nominal, tipe transaksi yang perlu approval, role pemeriksa
- **Cuti Tahunan** — kuota cuti default

### Konfigurasi Permission

Lihat [`config/permissions.php`](config/permissions.php) — single source of truth untuk:
- Registry semua permission yang tersedia
- Mapping role → permission IDs
- Jalankan `php artisan permission:sync` setelah mengubah

---

## License

MIT
