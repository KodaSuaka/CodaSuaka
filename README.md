# 🛡️ CodaSuaka — Aplikasi Manajemen Bisnis All-in-One

CodaSuaka adalah aplikasi **manajemen bisnis terpadu** yang dirancang untuk membantu pengelolaan operasional perusahaan — mencakup **manajemen karyawan, presensi, pengajuan Cuti/Izin/Sakit, divisi & outlet, keuangan (buku kas, laporan keuangan), approval workflow, chat internal**, dan **panel Super Admin** untuk mengelola seluruh instansi.

> **Tech Stack:**
> - **Backend:** Laravel 13.x + PHP 8.4 + Sanctum (REST API)
> - **Frontend Android:** Kotlin (Jetpack Compose)
> - **Database:** MySQL / MariaDB
> - **Export PDF:** barryvdh/laravel-dompdf
> - **Export Excel:** openspout/openspout

---

## 📋 Daftar Isi

- [Fitur Utama](#-fitur-utama)
- [Role & Permission](#-role--permission)
- [Autentikasi](#-autentikasi)
- [Cara Setup Backend](#-cara-setup-backend)
- [Base URL & Format Response API](#-base-url--format-response-api)
- [Dokumentasi Endpoint API](#-dokumentasi-endpoint-api)
  - [1. Autentikasi (Public)](#1-autentikasi-public)
  - [2. User & Profil](#2-user--profil)
  - [3. Dashboard](#3-dashboard)
  - [4. Outlet](#4-outlet)
  - [5. Karyawan](#5-karyawan)
  - [6. Role & Permission](#6-role--permission)
  - [7. Divisi & Anggota Divisi](#7-divisi--anggota-divisi)
  - [8. Presensi / Absensi](#8-presensi--absensi)
  - [9. Rekap Kehadiran](#9-rekap-kehadiran)
  - [10. Pengajuan Cuti/Izin/Sakit](#10-pengajuan-cutizinsakit)
  - [11. Jadwal / Kalender](#11-jadwal--kalender)
  - [12. Penugasan / Tugas](#12-penugasan--tugas)
  - [13. Paket](#13-paket)
  - [14. Transaksi Paket](#14-transaksi-paket)
  - [15. Keuangan — Kategori Transaksi](#15-keuangan--kategori-transaksi)
  - [16. Keuangan — Buku Kas (Transaksi Kas)](#16-keuangan--buku-kas-transaksi-kas)
  - [17. Keuangan — Laporan & Ekspor](#17-keuangan--laporan--ekspor)
  - [18. Keuangan — Approval Workflow](#18-keuangan--approval-workflow)
  - [19. Chat](#19-chat)
  - [20. Super Admin — Instansi](#20-super-admin--instansi)
  - [21. Super Admin — Owner](#21-super-admin--owner)
  - [22. Super Admin — Paket](#22-super-admin--paket)
- [Contoh Alur Penggunaan API](#-contoh-alur-penggunaan-api)
- [Environment Variables](#-environment-variables)

---

## 🎯 Fitur Utama

| Modul             | Deskripsi |
|------------------|-----------|
| **Autentikasi**   | Register (Owner), Register Super Admin, Login, Logout |
| **Multi-Tenant**  | Setiap pengguna terikat pada satu `instansi` (perusahaan). Data terisolasi otomatis via Tenant Scope |
| **Manajemen Karyawan** | CRUD karyawan, filter per outlet, profil lengkap |
| **Role & Permission** | RBAC dengan role: `Super Admin`, `Owner`, `Keuangan`, `Manajemen`, `Staff` — permission granular |
| **Outlet**        | CRUD outlet/cabang perusahaan |
| **Divisi**        | CRUD divisi, anggota divisi |
| **Presensi**      | Checkin & Checkout, riwayat presensi, rekap kehadiran (filter bulan/tahun) |
| **Pengajuan**     | Cuti tahunan, izin sakit, izin mendadak — lengkap dengan approval oleh Owner |
| **Jadwal**        | Kalender kerja / jadwal kegiatan |
| **Penugasan**     | Tugas dengan penanggung jawab, status (belum/proses/selesai), tenggat |
| **Paket & Transaksi Paket** | Kelola paket layanan dan transaksinya |
| **Keuangan**      | Buku Kas (pemasukan/pengeluaran), Kategori Transaksi, multi-outlet, multi-metode pembayaran |
| **Laporan**       | Ringkasan keuangan grafik bulanan, Arus Kas (Operasi/Investasi/Pendanaan) |
| **Export**        | PDF (Buku Kas, Laba Rugi, Arus Kas) & Excel (Buku Kas, Arus Kas) |
| **Approval Workflow** | Transaksi kas keluar nominal ≥ threshold otomatis perlu approval atasan |
| **Chat**          | Chat internal antar pengguna dalam satu instansi |
| **Super Admin**   | Panel khusus untuk mengelola seluruh instansi, owner, dan paket secara terpusat |

---

## 🔐 Role & Permission

| Role | Permission Utama |
|------|------------------|
| **Super Admin** | Akses penuh ke semua data lintas instansi (panel khusus) |
| **Owner** | `view/manage:presensi, pengajuan, divisi, penugasan, jadwal, karyawan, keuangan, laporan` |
| **Keuangan** | `view/manage:keuangan`, `view:laporan`, `view:presensi` |
| **Manajemen** | `view/manage:presensi, penugasan, jadwal, karyawan`, `view:pengajuan, divisi, keuangan, laporan` |
| **Staff** | `view:presensi, penugasan, jadwal` |

> Permission dapat dikustomisasi per role melalui endpoint [`/api/role-permissions`](#6-role--permission).

---

## 🔑 Autentikasi

CodaSuaka menggunakan **Laravel Sanctum** dengan **Token-Based Authentication**.

- Semua endpoint **wajib menyertakan header** `Authorization: Bearer {token}`, kecuali endpoint public (`/login`, `/register`, `/register-super-admin`).
- Token diperoleh dari response **login** atau **register**.
- Token dikirim sebagai **Bearer Token**.

### Format Header

```
Authorization: Bearer 1|abc123def456...
Accept: application/json
```

---

## 🚀 Cara Setup Backend

### Prasyarat

- PHP 8.4+
- Composer
- MySQL / MariaDB
- Node.js & npm

### Langkah Instalasi

```bash
# Masuk ke direktori backend
cd coda-suaka-backend

# Copy environment
cp .env.example .env

# Atur koneksi database di file .env
# DB_DATABASE=codasuaka
# DB_USERNAME=root
# DB_PASSWORD=

# Install dependencies
composer install

# Generate key
php artisan key:generate

# Jalankan migrasi & seeder
php artisan migrate --seed

# (Opsional) Symlink storage
php artisan storage:link

# Jalankan server
php artisan serve

# (Opsional) Jalankan queue worker
php artisan queue:listen
```

### Development Mode (All-in-One)

```bash
# Menjalankan server, queue, logs, dan Vite secara bersamaan
composer dev
```

---

## 🌐 Base URL & Format Response API

**Base URL (Development):** `http://localhost:8000/api`

### Format Response Success

```json
{
    "status": "success",
    "message": "Login berhasil",
    "data": { ... }
}
```

### Format Response Error

```json
{
    "status": "error",
    "message": "Email atau Password yang Anda masukkan salah."
}
```

### Format Response Paginated

```json
{
    "status": "success",
    "message": "Berhasil",
    "data": [ ... ],
    "meta": {
        "current_page": 1,
        "last_page": 5,
        "per_page": 50,
        "total": 234
    }
}
```

---

## 📚 Dokumentasi Endpoint API

### 1. Autentikasi (Public)

#### `POST /api/register`
> Mendaftarkan Owner baru beserta Instansi barunya.

**Request Body:**

| Field           | Tipe   | Required | Deskripsi |
|----------------|--------|----------|-----------|
| `nama_instansi` | string | ✅       | Nama perusahaan |
| `nama_pemilik`  | string | ✅       | Nama pemilik (Owner) |
| `email`         | string | ✅       | Email untuk login |
| `password`      | string | ✅       | Password (min. 6 karakter) |

**Response (201):**
```json
{
    "status": "success",
    "message": "Registrasi Owner dan Instansi berhasil",
    "data": {
        "user": {
            "id": 1,
            "name": "John Doe",
            "email": "john@example.com",
            "role": "Owner",
            "instansi_id": 1,
            "outlet_id": null
        },
        "permissions": ["view:presensi", "manage:presensi", ...],
        "access_token": "1|abc123...",
        "token_type": "Bearer"
    }
}
```

---

#### `POST /api/register-super-admin`
> Mendaftarkan akun Super Admin (global, lintas instansi).

**Request Body:**

| Field      | Tipe   | Required | Deskripsi |
|-----------|--------|----------|-----------|
| `name`     | string | ✅       | Nama Super Admin |
| `email`    | string | ✅       | Email untuk login |
| `password` | string | ✅       | Password (min. 6 karakter) |

> **⚠️ Endpoint ini tidak dilindungi middleware, karena digunakan untuk inisialisasi awal. Disarankan untuk menonaktifkan setelah Super Admin pertama dibuat.**

**Response (201):**
```json
{
    "status": "success",
    "message": "Registrasi Super Admin berhasil",
    "data": {
        "user": {
            "id": 2,
            "name": "Admin Utama",
            "email": "admin@example.com",
            "role": "Super Admin"
        },
        "access_token": "2|def456...",
        "token_type": "Bearer"
    }
}
```

---

#### `POST /api/login`

**Request Body:**

| Field      | Tipe   | Required | Deskripsi |
|-----------|--------|----------|-----------|
| `email`    | string | ✅       | Email terdaftar |
| `password` | string | ✅       | Password |

**Response (200):**
```json
{
    "status": "success",
    "message": "Login berhasil",
    "data": {
        "user": {
            "id": 1,
            "email": "john@example.com",
            "role": "Owner",
            "instansi_id": 1,
            "outlet_id": 1,
            "nama_lengkap": "John Doe"
        },
        "permissions": ["view:presensi", "manage:keuangan", ...],
        "access_token": "1|abc123...",
        "token_type": "Bearer"
    }
}
```

---

### 🔐 Endpoint yang Dilindungi (memerlukan token Sanctum)

Semua endpoint berikut membutuhkan header:

```
Authorization: Bearer {your_token}
Accept: application/json
```

---

### 2. User & Profil

#### `POST /api/logout`
> Hapus token akses saat ini.

**Response (200):**
```json
{
    "status": "success",
    "message": "Logout berhasil",
    "data": null
}
```

---

#### `GET /api/user`
> Mendapatkan data user yang sedang login beserta relasi (role, profil karyawan, outlet).

**Response:**
```json
{
    "status": "success",
    "data": {
        "id": 1,
        "name": "John Doe",
        "email": "john@example.com",
        "role": { "id": 1, "nama_role": "Owner" },
        "profilKaryawan": { ... },
        "outlet": { ... }
    }
}
```

---

### 3. Dashboard

#### `GET /api/dashboard`
> Dashboard Owner — ringkasan data instansi. (Role: Owner only)

| Query Param | Tipe | Required | Deskripsi |
|------------|------|----------|-----------|
| -          | -    | -        | Tidak ada parameter |

**Response:**
```json
{
    "status": "success",
    "data": {
        "total_karyawan": 25,
        "total_outlet": 3,
        "total_divisi": 8,
        "presensi_hari_ini": 18,
        "pengajuan_pending": 2,
        "tugas_stats": {
            "belum": 5,
            "proses": 3,
            "selesai": 12
        }
    }
}
```

---

#### `GET /api/karyawan/dashboard`
> Dashboard untuk karyawan biasa — status presesi, tugas aktif, pengajuan pending, sisa cuti, menu peran.

**Response:**
```json
{
    "status": "success",
    "data": {
        "karyawan": { ... },
        "presensi_hari_ini": { ... },
        "sudah_checkin": true,
        "sudah_checkout": false,
        "tugas_aktif": [ ... ],
        "pengajuan_pending_count": 1,
        "sisa_cuti": 12,
        "role_menu_items": [ ... ],
        "additional_content": [ ... ]
    }
}
```

---

#### `GET /api/dashboard/omset`
> Total omset (pemasukan) berdasarkan periode.

| Query Param   | Tipe    | Required | Default           | Deskripsi |
|--------------|---------|----------|-------------------|-----------|
| `start_date` | date    | ❌       | Awal bulan berjalan | Tanggal mulai |
| `end_date`   | date    | ❌       | Hari ini          | Tanggal akhir |

**Response:**
```json
{
    "status": "success",
    "data": {
        "start_date": "2026-07-01",
        "end_date": "2026-07-19",
        "total_omset": 50000000.0
    }
}
```

---

### 4. Outlet

#### `GET /api/outlets` — Daftar semua outlet dalam instansi
#### `POST /api/outlets` — Buat outlet baru
#### `GET /api/outlets/{outlet}` — Detail outlet
#### `PUT /api/outlets/{outlet}` — Update outlet
#### `DELETE /api/outlets/{outlet}` — Hapus outlet

**Request Body (POST/PUT):**
| Field | Tipe | Required | Deskripsi |
|-------|------|----------|-----------|
| `nama_outlet` | string | ✅ | Nama outlet/cabang |
| `alamat` | string | ❌ | Alamat |
| `kontak` | string | ❌ | Nomor kontak |

---

### 5. Karyawan

#### `GET /api/karyawans`
> Daftar karyawan dalam instansi user.

| Query Param  | Tipe  | Required | Deskripsi |
|-------------|-------|----------|-----------|
| `outlet_id`  | int   | ❌       | Filter berdasarkan outlet |

**Response:**
```json
{
    "status": "success",
    "data": [
        {
            "id": 1,
            "user_id": 1,
            "nama_lengkap": "John Doe",
            "kontak": "08123456789",
            "alamat": "Jl. Merdeka No.1",
            "outlet_id": 1,
            "sisa_cuti": 12,
            "foto_profil": null,
            "user": { "id": 1, "email": "...", "role": { "nama_role": "Staff" } },
            "outlet": { "id": 1, "nama_outlet": "Outlet Pusat" }
        }
    ]
}
```

---

#### `GET /api/karyawans/me`
> Profil karyawan yang sedang login.

---

#### `POST /api/karyawans`
> Tambah karyawan baru (otomatis membuat User account).

**Request Body:**

| Field | Tipe | Required | Deskripsi |
|-------|------|----------|-----------|
| `nama_lengkap` | string | ✅ | Nama lengkap |
| `email` | string | ✅ | Email (untuk login) |
| `password` | string | ✅ | Password minimal 6 karakter |
| `role_id` | int | ✅ | ID role |
| `outlet_id` | int | ❌ | Penempatan outlet |
| `kontak` | string | ❌ | Nomor telepon |
| `alamat` | string | ❌ | Alamat |
| `sisa_cuti` | int | ❌ | Jatah cuti (default: 0) |

---

#### `GET /api/karyawans/{karyawan}` — Detail karyawan (dengan relasi divisi & anggota)
#### `PUT /api/karyawans/{karyawan}` — Update karyawan
#### `DELETE /api/karyawans/{karyawan}` — Hapus karyawan

---

### 6. Role & Permission

#### `GET /api/roles` — Daftar semua role
#### `POST /api/roles` — Buat role baru
#### `GET /api/roles/{role}` — Detail role
#### `PUT /api/roles/{role}` — Update role
#### `DELETE /api/roles/{role}` — Hapus role

#### `GET /api/role-permissions` — Daftar semua permission
#### `POST /api/role-permissions` — Tambah permission ke role
#### `DELETE /api/role-permissions/{role_permission}` — Hapus permission dari role

**Request Body (POST):**

| Field           | Tipe   | Required | Deskripsi |
|----------------|--------|----------|-----------|
| `role_id`       | int    | ✅       | ID role |
| `permission`    | string | ✅       | Nama permission (contoh: `manage:karyawan`) |

#### `POST /api/role-permissions/sync` — Sinkronisasi permission untuk role

**Request Body:**

```json
{
    "role_id": 1,
    "permissions": ["view:presensi", "manage:presensi", "view:keuangan"]
}
```

---

### 7. Divisi & Anggota Divisi

#### `GET /api/divisis` — Daftar divisi
#### `POST /api/divisis` — Buat divisi baru
#### `GET /api/divisis/{divisi}` — Detail divisi
#### `PUT /api/divisis/{divisi}` — Update divisi
#### `DELETE /api/divisis/{divisi}` — Hapus divisi

**Request Body (POST/PUT):**

| Field | Tipe | Required | Deskripsi |
|-------|------|----------|-----------|
| `nama_divisi` | string | ✅ | Nama divisi |
| `outlet_id` | int | ❌ | Outlet terkait (jika spesifik) |

---

#### `GET /api/anggota-divisis` — Daftar anggota divisi
#### `POST /api/anggota-divisis` — Tambah anggota ke divisi
#### `DELETE /api/anggota-divisis/{anggotaDivisi}` — Hapus anggota dari divisi

**Request Body (POST):**

| Field | Tipe | Required | Deskripsi |
|-------|------|----------|-----------|
| `divisi_id` | int | ✅ | ID divisi |
| `karyawan_id` | int | ✅ | ID karyawan |

---

### 8. Presensi / Absensi

#### `GET /api/presensis`
> Riwayat presensi.

| Query Param | Tipe | Required | Deskripsi |
|------------|------|----------|-----------|
| `user_id`    | int  | ❌       | Filter user (Owner/Manajemen) |
| `tanggal`    | date | ❌       | Filter tanggal spesifik |
| `bulan`      | int  | ❌       | Filter bulan (1-12) — perlu `tahun` |
| `tahun`      | int  | ❌       | Filter tahun |

---

#### `POST /api/presensis/checkin`
> Absen masuk.

**Request Body:**

| Field   | Tipe   | Required | Deskripsi |
|---------|--------|----------|-----------|
| `lokasi` | string | ❌       | Lokasi checkin (maks 255 karakter) |

---

#### `POST /api/presensis/checkout`
> Absen pulang. (Tidak perlu body)

---

#### `GET /api/presensis/today`
> Status presensi hari ini.

**Response:**
```json
{
    "status": "success",
    "data": {
        "id": 10,
        "user_id": 1,
        "tanggal": "2026-07-19",
        "jam_checkin": "08:00:00",
        "jam_checkout": null,
        "status": "hadir",
        "lokasi_checkin": "Kantor Pusat",
        "user": { ... }
    }
}
```

---

### 9. Rekap Kehadiran

#### `GET /api/rekap-kehadiran`
> Rekap kehadiran per karyawan dalam periode tertentu.

| Query Param | Tipe | Required | Deskripsi |
|------------|------|----------|-----------|
| `bulan` | int | ❌ | Bulan (default: bulan berjalan) |
| `tahun` | int | ❌ | Tahun (default: tahun berjalan) |

---

### 10. Pengajuan Cuti/Izin/Sakit

#### `GET /api/pengajuans`
> Daftar pengajuan. Owner bisa lihat semua, karyawan hanya milik sendiri.

| Query Param | Tipe | Required | Deskripsi |
|------------|------|----------|-----------|
| `status` | string | ❌ | Filter: `pending`, `disetujui`, `ditolak` |

---

#### `POST /api/pengajuans`
> Buat pengajuan baru.

**Request Body:**

| Field | Tipe | Required | Deskripsi |
|-------|------|----------|-----------|
| `jenis` | string | ✅ | `cuti_tahunan`, `izin_sakit`, atau `mendadak` |
| `tanggal_mulai` | date | ✅ | Tanggal mulai |
| `tanggal_selesai` | date | ✅ | Tanggal selesai (≥ tanggal_mulai) |
| `keterangan` | string | ❌ | Keterangan / alasan |

> ⚠️ Untuk `cuti_tahunan`, otomatis dicek sisa cuti. Jika habis (≤ 0), pengajuan ditolak.

---

#### `GET /api/pengajuans/{pengajuan}` — Detail pengajuan
#### `PUT /api/pengajuans/{pengajuan}/approve` — Setujui pengajuan (Owner)
#### `PUT /api/pengajuans/{pengajuan}/reject` — Tolak pengajuan (Owner)

---

### 11. Jadwal / Kalender

#### `GET /api/jadwals` — Daftar jadwal
#### `POST /api/jadwals` — Buat jadwal baru
#### `GET /api/jadwals/{jadwal}` — Detail jadwal
#### `PUT /api/jadwals/{jadwal}` — Update jadwal
#### `DELETE /api/jadwals/{jadwal}` — Hapus jadwal

---

### 12. Penugasan / Tugas

#### `GET /api/penugasans` — Daftar penugasan
#### `POST /api/penugasans` — Buat penugasan baru
#### `GET /api/penugasans/{penugasan}` — Detail penugasan
#### `PUT /api/penugasans/{penugasan}` — Update penugasan
#### `DELETE /api/penugasans/{penugasan}` — Hapus penugasan

---

### 13. Paket

> **Akses:** Semua user terautentikasi (read-only). CRUD write hanya melalui endpoint Super Admin.

#### `GET /api/pakets` — Daftar paket
#### `GET /api/pakets/{paket}` — Detail paket

---

### 14. Transaksi Paket

#### `GET /api/transaksi-pakets` — Daftar transaksi paket
#### `POST /api/transaksi-pakets` — Buat transaksi paket baru
#### `GET /api/transaksi-pakets/{transaksi_paket}` — Detail transaksi paket
#### `PUT /api/transaksi-pakets/{transaksi_paket}` — Update transaksi paket

---

### 15. Keuangan — Kategori Transaksi

#### `GET /api/kategori-transaksis` — Daftar kategori transaksi (default + kustom per instansi)
#### `POST /api/kategori-transaksis` — Buat kategori baru
#### `GET /api/kategori-transaksis/{kategori_transaksi}` — Detail kategori
#### `PUT /api/kategori-transaksis/{kategori_transaksi}` — Update kategori
#### `DELETE /api/kategori-transaksis/{kategori_transaksi}` — Hapus kategori

**Request Body (POST/PUT):**

| Field | Tipe | Required | Deskripsi |
|-------|------|----------|-----------|
| `nama_kategori` | string | ✅ | Nama kategori (contoh: `Penjualan`, `Gaji`) |
| `tipe` | string | ❌ | `masuk` / `keluar` (membatasi tipe transaksi) |

---

### 16. Keuangan — Buku Kas (Transaksi Kas)

#### `GET /api/transaksi-kas`
> Daftar entri Buku Kas dengan berbagai filter.

| Query Param | Tipe | Required | Deskripsi |
|------------|------|----------|-----------|
| `outlet_id` | int | ❌ | Filter outlet |
| `tipe` | string | ❌ | `masuk` / `keluar` |
| `kategori_transaksi_id` | int | ❌ | Filter kategori |
| `start_date` | date | ❌ | Tanggal mulai |
| `end_date` | date | ❌ | Tanggal akhir |
| `status_approval` | string | ❌ | `pending` / `disetujui` / `ditolak` / `tidak_perlu` |
| `per_page` | int | ❌ | Jumlah per halaman (default: 50) |

---

#### `POST /api/transaksi-kas`
> Buat transaksi kas baru. Jika transaksi keluar nominal ≥ threshold, otomatis masuk workflow approval.

**Request Body:**

| Field | Tipe | Required | Deskripsi |
|-------|------|----------|-----------|
| `tanggal` | date | ✅ | Tanggal transaksi (≤ hari ini) |
| `tipe` | string | ✅ | `masuk` atau `keluar` |
| `nominal` | numeric | ✅ | Jumlah nominal |
| `kategori_transaksi_id` | int | ❌ | ID kategori transaksi |
| `outlet_id` | int | ❌ | Outlet terkait |
| `metode_pembayaran` | string | ❌ | `tunai`, `transfer_bank`, `kartu_kredit`, `kartu_debit`, `e_wallet`, `cek_giro`, atau `lainnya` |
| `keterangan` | string | ❌ | Deskripsi transaksi (maks 1000 karakter) |
| `lampiran_url` | string | ❌ | URL bukti/Lampiran (maks 255 karakter) |

---

#### `GET /api/transaksi-kas/saldo`
> Saldo kas terkini.

**Response:**
```json
{
    "status": "success",
    "data": {
        "total_masuk": 150000000.0,
        "total_keluar": 85000000.0,
        "saldo_akhir": 65000000.0
    }
}
```

---

#### `GET /api/transaksi-kas/laporan/laba-rugi`
> Laporan Laba Rugi berdasarkan periode.

| Query Param | Tipe | Required | Default | Deskripsi |
|------------|------|----------|---------|-----------|
| `start_date` | date | ❌ | Awal bulan | Tanggal mulai |
| `end_date` | date | ❌ | Hari ini | Tanggal akhir |
| `outlet_id` | int | ❌ | Semua | Filter outlet |

---

#### `GET /api/transaksi-kas/{transaksi_kas}` — Detail transaksi kas
#### `PUT /api/transaksi-kas/{transaksi_kas}` — Update transaksi kas
#### `DELETE /api/transaksi-kas/{transaksi_kas}` — Hapus transaksi kas

---

### 17. Keuangan — Laporan & Ekspor

#### `GET /api/laporan/arus-kas`
> Laporan Arus Kas (Cash Flow) — dikelompokkan ke aktivitas **Operasi**, **Investasi**, dan **Pendanaan**.

| Query Param | Tipe | Required | Default | Deskripsi |
|------------|------|----------|---------|-----------|
| `start_date` | date | ❌ | Awal bulan | Tanggal mulai |
| `end_date` | date | ❌ | Hari ini | Tanggal akhir |
| `outlet_id` | int | ❌ | Semua | Filter outlet |

---

#### `GET /api/laporan/ringkasan-keuangan`
> Data agregat per-bulan untuk grafik dashboard.

| Query Param | Tipe | Required | Default | Deskripsi |
|------------|------|----------|---------|-----------|
| `tahun` | int | ❌ | Tahun berjalan | Tahun laporan |

**Response:**
```json
{
    "status": "success",
    "data": {
        "tahun": 2026,
        "series": [
            { "bulan": "2026-01", "pendapatan": 50000000, "beban": 30000000, "laba": 20000000 },
            { "bulan": "2026-02", "pendapatan": 45000000, "beban": 28000000, "laba": 17000000 }
        ]
    }
}
```

---

#### Export PDF & Excel

| Method | Endpoint | Deskripsi |
|--------|----------|-----------|
| `GET` | `/api/laporan/buku-kas/export/pdf` | Export Buku Kas ke PDF |
| `GET` | `/api/laporan/buku-kas/export/excel` | Export Buku Kas ke Excel |
| `GET` | `/api/laporan/laba-rugi/export/pdf` | Export Laba Rugi ke PDF |
| `GET` | `/api/laporan/arus-kas/export/pdf` | Export Arus Kas ke PDF |
| `GET` | `/api/laporan/arus-kas/export/excel` | Export Arus Kas ke Excel |

**Query params untuk endpoint export:** `start_date`, `end_date`, `outlet_id` (opsional, filter periode & outlet).

---

### 18. Keuangan — Approval Workflow

> Workflow approval untuk transaksi kas keluar dengan nominal ≥ threshold (konfigurasi via [`config/keuangan.php`](coda-suaka-backend/config/keuangan.php)).

📌 **Konfigurasi Approval:**

| Key              | Default     | Deskripsi |
|------------------|-------------|-----------|
| `enabled`         | `true`      | Aktif/nonaktifkan approval |
| `threshold_nominal` | `1000000` | Nominal transaksi keluar yang perlu approval |
| `tipe_perlu_approval` | `['keluar']` | Tipe transaksi yang perlu approval |
| `role_pemeriksa`  | `['Owner', 'Manajemen']` | Role yang bisa menjadi pemeriksa (approver) |

---

#### `GET /api/approval/pending`
> Daftar transaksi yang menunggu approval. (Role: approver)

| Query Param | Tipe | Required | Deskripsi |
|------------|------|----------|-----------|
| `outlet_id` | int | ❌ | Filter outlet |
| `start_date` | date | ❌ | Filter tanggal mulai |
| `end_date` | date | ❌ | Filter tanggal akhir |
| `per_page` | int | ❌ | Pagination |

---

#### `GET /api/approval/riwayat`
> Riwayat approval (semua status).

| Query Param | Tipe | Required | Deskripsi |
|------------|------|----------|-----------|
| `status` | string | ❌ | Filter status approval |
| `outlet_id` | int | ❌ | Filter outlet |
| `start_date` | date | ❌ | Filter tanggal mulai |
| `end_date` | date | ❌ | Filter tanggal akhir |
| `per_page` | int | ❌ | Pagination |

---

#### `POST /api/approval/{transaksi_kas}/ajukan`
> Ajukan transaksi untuk approval.

> **Catatan:** Transaksi keluar dengan nominal ≥ threshold otomatis masuk workflow approval saat dibuat. Endpoint ini digunakan untuk mengajukan **ulang** jika sebelumnya ditolak.

---

#### `POST /api/approval/{approval_log}/setujui`
> Setujui transaksi yang diajukan.

**Request Body:**

| Field | Tipe | Required | Deskripsi |
|-------|------|----------|-----------|
| `catatan` | string | ❌ | Catatan persetujuan |

---

#### `POST /api/approval/{approval_log}/tolak`
> Tolak transaksi yang diajukan.

**Request Body:**

| Field | Tipe | Required | Deskripsi |
|-------|------|----------|-----------|
| `catatan` | string | ✅ | Alasan penolakan |

---

### 19. Chat

#### `GET /api/chat/contacts`
> Daftar kontak (user dalam satu instansi) untuk chat.

---

#### `GET /api/chat/messages/{user}`
> Riwayat pesan dengan user tertentu.

| Parameter | Tipe | Deskripsi |
|-----------|------|-----------|
| `user` | int (route) | ID user tujuan |

---

#### `POST /api/chat/send`
> Kirim pesan baru.

**Request Body:**

| Field | Tipe | Required | Deskripsi |
|-------|------|----------|-----------|
| `receiver_id` | int | ✅ | ID user penerima |
| `message` | string | ✅ | Isi pesan |

---

#### `PUT /api/chat/read/{user}`
> Tandai pesan dari user tertentu sebagai sudah dibaca.

---

### 20. Super Admin — Instansi

> **⚠️ Semua endpoint Super Admin membutuhkan role `Super Admin`.**

#### `GET /api/super-admin/dashboard`
> Dashboard Super Admin — ringkasan data global.

#### `GET /api/super-admin/instansis` — Daftar semua instansi (dengan owner & paket)
#### `POST /api/super-admin/instansis` — Buat instansi baru beserta owner
#### `GET /api/super-admin/instansis/{instansi}` — Detail instansi
#### `PUT /api/super-admin/instansis/{instansi}` — Update instansi
#### `DELETE /api/super-admin/instansis/{instansi}` — Hapus instansi

**Request Body (POST/PUT):**

| Field | Tipe | Required | Deskripsi |
|-------|------|----------|-----------|
| `nama_instansi` | string | ✅ | Nama perusahaan |
| `paket_id` | int | ❌ | ID paket |
| `owner_name` | string | ✅* | Nama owner (hanya POST) |
| `owner_email` | string | ✅* | Email owner (hanya POST) |
| `owner_password` | string | ✅* | Password owner (hanya POST) |

---

### 21. Super Admin — Owner

#### `GET /api/super-admin/owners` — Daftar semua Owner
#### `POST /api/super-admin/owners` — Buat Owner baru
#### `GET /api/super-admin/owners/{user}` — Detail Owner
#### `PUT /api/super-admin/owners/{user}` — Update Owner
#### `DELETE /api/super-admin/owners/{user}` — Hapus Owner

**Request Body (POST):**

| Field | Tipe | Required | Deskripsi |
|-------|------|----------|-----------|
| `name` | string | ✅ | Nama Owner |
| `email` | string | ✅ | Email (untuk login) |
| `password` | string | ✅ | Password |
| `instansi_id` | int | ✅ | ID instansi yang akan dikelola |

---

### 22. Super Admin — Paket

#### `GET /api/super-admin/pakets` — Daftar semua paket
#### `POST /api/super-admin/pakets` — Buat paket baru
#### `GET /api/super-admin/pakets/{paket}` — Detail paket
#### `PUT /api/super-admin/pakets/{paket}` — Update paket
#### `DELETE /api/super-admin/pakets/{paket}` — Hapus paket

**Request Body (POST/PUT):**

| Field | Tipe | Required | Deskripsi |
|-------|------|----------|-----------|
| `nama_paket` | string | ✅ | Nama paket |
| `deskripsi` | string | ❌ | Deskripsi paket |
| `harga` | numeric | ✅ | Harga paket |
| `durasi_hari` | int | ✅ | Masa berlaku paket (hari) |
| `fitur` | json | ❌ | Daftar fitur yang termasuk |

---

## 💡 Contoh Alur Penggunaan API

### Alur Registrasi & Login Owner

```bash
# 1. Registrasi Owner baru
curl -X POST http://localhost:8000/api/register \
  -H "Accept: application/json" \
  -H "Content-Type: application/json" \
  -d '{
    "nama_instansi": "PT Maju Sejahtera",
    "nama_pemilik": "Budi Santoso",
    "email": "budi@majusejahtera.com",
    "password": "rahasia123"
  }'

# 2. Login (jika sudah punya akun)
curl -X POST http://localhost:8000/api/login \
  -H "Accept: application/json" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "budi@majusejahtera.com",
    "password": "rahasia123"
  }'

# Simpan access_token dari response untuk digunakan di endpoint berikutnya
```

### Alur Presensi (Checkin → Checkout)

```bash
TOKEN="1|abc123def456..."

# 1. Checkin
curl -X POST http://localhost:8000/api/presensis/checkin \
  -H "Accept: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"lokasi": "Kantor Pusat"}'

# 2. Cek status hari ini
curl http://localhost:8000/api/presensis/today \
  -H "Accept: application/json" \
  -H "Authorization: Bearer $TOKEN"

# 3. Checkout (pulang)
curl -X POST http://localhost:8000/api/presensis/checkout \
  -H "Accept: application/json" \
  -H "Authorization: Bearer $TOKEN"
```

### Alur Buku Kas & Approval

```bash
TOKEN="1|abc123def456..."

# 1. Buat transaksi pemasukan
curl -X POST http://localhost:8000/api/transaksi-kas \
  -H "Accept: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "tanggal": "2026-07-19",
    "tipe": "masuk",
    "nominal": 5000000,
    "kategori_transaksi_id": 1,
    "metode_pembayaran": "transfer_bank",
    "keterangan": "Penjualan tunai"
  }'

# 2. Cek saldo terkini
curl http://localhost:8000/api/transaksi-kas/saldo \
  -H "Accept: application/json" \
  -H "Authorization: Bearer $TOKEN"

# 3. Buat transaksi pengeluaran besar (≥ threshold → otomatis perlu approval)
curl -X POST http://localhost:8000/api/transaksi-kas \
  -H "Accept: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "tanggal": "2026-07-19",
    "tipe": "keluar",
    "nominal": 2000000,
    "kategori_transaksi_id": 2,
    "keterangan": "Pembelian inventaris"
  }'
# Response akan menyertakan status_approval: "pending"
```

> **Catatan:** Untuk workflow approval, transaksi keluar dengan nominal ≥ `APPROVAL_THRESHOLD` (default: Rp1.000.000) akan otomatis masuk status `pending` dan perlu disetujui oleh role `Owner` atau `Manajemen`.

---

## ⚙️ Environment Variables

Berikut konfigurasi penting di file [`.env.example`](coda-suaka-backend/.env.example):

| Variabel | Default | Deskripsi |
|----------|---------|-----------|
| `APP_NAME` | `CodaSuaka` | Nama aplikasi |
| `APP_URL` | `http://localhost:8000` | Base URL aplikasi |
| `DB_DATABASE` | `codasuaka` | Nama database |
| `APPROVAL_ENABLED` | `true` | Aktifkan workflow approval transaksi |
| `APPROVAL_THRESHOLD` | `1000000` | Threshold nominal untuk approval |
| `SANCTUM_STATEFUL_DOMAINS` | — | Domain untuk SPA (jika ada) |

Konfigurasi keuangan tambahan di [`config/keuangan.php`](coda-suaka-backend/config/keuangan.php):

```php
'approval' => [
    'enabled'            => env('APPROVAL_ENABLED', true),
    'threshold_nominal'  => env('APPROVAL_THRESHOLD', 1000000),
    'tipe_perlu_approval' => ['keluar'],
    'role_pemeriksa'     => ['Owner', 'Manajemen'],
],
```

---

## 📁 Struktur Direktori Backend

```
coda-suaka-backend/
├── app/
│   ├── Http/
│   │   ├── Controllers/      # Controller untuk setiap modul
│   │   ├── Middleware/        # Role & Permission middleware
│   │   └── Requests/          # Form Request validasi
│   ├── Models/                # Eloquent Models
│   ├── Policies/              # Authorization Policies
│   ├── Services/              # Business Logic Services
│   └── Traits/                # ApiResponse trait
├── config/
│   ├── cors.php               # Konfigurasi CORS
│   ├── keuangan.php           # Konfigurasi keuangan & approval
│   └── roles.php              # Mapping Role → Permission default
├── database/
│   ├── migrations/            # Skema database
│   └── seeders/               # Data awal (roles, permissions)
├── resources/views/laporan/   # Template PDF (Blade)
└── routes/api.php             # Semua definisi endpoint API
```

---

## 📱 Frontend Android

Aplikasi Android (Kotlin + Jetpack Compose) berada di direktori:

```
coda-suaka-frontend/
```

Struktur utama:
- **Data Layer:** `data/local/TokenManager.kt` — Manajemen token lokal
- **UI Layer:** `ui/screen/` — Screen seperti Divisi, Kelola Karyawan, dll.
- **ViewModel:** Setiap screen memiliki ViewModel sendiri (contoh: `DivisiViewModel.kt`)

---

## 📄 Lisensi

Proyek ini dikembangkan untuk kebutuhan manajemen bisnis internal. Penggunaan dan distribusi diatur sesuai kebijakan masing-masing instansi.

---

> 💡 **Butuh bantuan lebih lanjut?** Jelajahi kode di [`coda-suaka-backend/routes/api.php`](coda-suaka-backend/routes/api.php) untuk referensi endpoint terlengkap, atau lihat masing-masing Controller di [`app/Http/Controllers/`](coda-suaka-backend/app/Http/Controllers) untuk detail implementasi.
