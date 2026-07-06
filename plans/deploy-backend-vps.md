# Langkah-Langkah Clone/Deploy Backend ke aaPanel VPS (DEVELOPMENT MODE)

> Project: `coda-suaka-backend` — Laravel 11 (PHP ^8.3, Sanctum, SQLite/MySQL)
> Mode: **Development** (`APP_ENV=local`, `APP_DEBUG=true`) — cocok untuk testing & pengembangan

---

## 📋 Daftar Isi

- [🧪 Development Mode — Konfigurasi](#-development-mode--konfigurasi-env)
- [🔄 Pull Update (Update Kode Terbaru dari Git)](#-pull-update--update-backend-di-vps-dari-git)
- [🔧 Clone Pertama Kali (Fresh Install)](#-clone-pertama-kali-fresh-install)

---

## 🧪 DEVELOPMENT MODE — Konfigurasi .env

Ini yang membedakan development vs production. Di `.env` kamu cukup set:

```env
APP_NAME=CodaSuaka
APP_ENV=local         # ← local / development
APP_DEBUG=true         # ← true agar lihat error detail
APP_URL=http://api.codasuaka.com   # pakai http dulu kalau belum SSL

# Pilih salah satu database:
DB_CONNECTION=sqlite   # atau mysql

# Jika SQLite:
# DB_DATABASE=/www/wwwroot/api.codasuaka.com/database/database.sqlite

# Jika MySQL:
# DB_HOST=127.0.0.1
# DB_PORT=3306
# DB_DATABASE=nama_database
# DB_USERNAME=user_database
# DB_PASSWORD=password_database

SESSION_DRIVER=file
CACHE_STORE=file
QUEUE_CONNECTION=sync
```

> **⚠️ Penting:** Karena development, **JANGAN** jalankan `php artisan config:cache` atau `route:cache` — nanti perubahan kode tidak terdeteksi!

---

## 🔄 PULL UPDATE — Update Backend di VPS dari Git

Ini yang kamu perlukan setiap ada perubahan kode di repository.

### Langkah Pull Update (Development):

```bash
# 1. SSH ke VPS atau buka aaPanel Terminal
# 2. Masuk ke folder backend
cd /www/wwwroot/api.codasuaka.com

# 3. Backup .env (penting! jangan sampai ketimpa)
cp .env .env.backup

# 4. Simpan perubahan lokal sementara, lalu pull
git stash
git pull origin main
# atau git pull origin master (tergantung branch utama)

# 5. Kembalikan .env yang distash tadi
git stash pop

# 6. Install dependency baru (jika ada perubahan di composer.json)
composer install

# 7. Jalankan migrasi baru
php artisan migrate

# 8. Hanya clear view cache (jangan config:cache biar development)
php artisan view:clear

# 9. Set permission
chmod -R 775 storage bootstrap/cache
chown -R www:www .
```

### Satu Perintah untuk Pull Update:

```bash
cd /www/wwwroot/api.codasuaka.com && \
cp .env .env.backup && \
git stash && \
git pull origin main && \
git stash pop && \
composer install && \
php artisan migrate && \
php artisan view:clear && \
chmod -R 775 storage bootstrap/cache && \
chown -R www:www .
```

---

## 🔧 CLONE PERTAMA KALI (Fresh Install)

### 1. Persiapkan Domain & Site di aaPanel

1. Login ke aaPanel.
2. **Website** → **Add site**.
3. Masukkan domain/subdomain (contoh: `api.codasuaka.com`).
4. Pilih **PHP version** ≥ **8.3**.
5. Database: pilih **MySQL** (atau lewati jika pakai SQLite).
6. Klik **Submit**.

Catat:
- Path root: `/www/wwwroot/api.codasuaka.com`
- Database name, user, password (jika MySQL)

---

### 2. Clone Project

```bash
cd /www/wwwroot/
git clone https://github.com/username/coda-suaka-backend.git api.codasuaka.com
```

Atau jika folder sudah ada:
```bash
cd /www/wwwroot/
git clone https://github.com/username/coda-suaka-backend.git temp
cp -r temp/* api.codasuaka.com/
rm -rf temp
```

> **Alternatif:** Download ZIP dari GitHub, upload via aaPanel File Manager, extract.

---

### 3. Set Permission

```bash
cd /www/wwwroot/api.codasuaka.com
chmod -R 775 storage bootstrap/cache
chown -R www:www .
```

---

### 4. Setup .env (Development)

```bash
cp .env.example .env
```

Edit `.env` — set seperti di bagian [Development Mode](#-development-mode--konfigurasi-env) di atas.

Jika pakai SQLite:
```bash
touch database/database.sqlite
chmod 666 database/database.sqlite
```

---

### 5. Install Dependencies

```bash
cd /www/wwwroot/api.codasuaka.com

# Install dengan dev dependencies (penting untuk development!)
composer install

# Generate APP_KEY
php artisan key:generate

# Jalankan migrasi
php artisan migrate

# (Opsional) Seeder
php artisan db:seed

# Buat storage link
php artisan storage:link
```

---

### 6. Set Root Document di aaPanel

1. **Website** → **Settings** (domain backend).
2. **Root Path** → arahkan ke `/www/wwwroot/api.codasuaka.com/public`
3. **URL Rewrite** → pilih template **Laravel** (atau manual):

```
location / {
    try_files $uri $uri/ /index.php?$query_string;
}
```

4. **SSL** → aktifkan jika sudah siap (opsional untuk development, bisa pakai HTTP dulu).

---

### 7. Restart PHP & Nginx

- **App Store** → **PHP 8.3** → **Reload**
- **Website** → site → **Nginx** → **Reload**

---

### 8. Uji Coba

```bash
curl http://api.codasuaka.com/api/user
# Harusnya 401 Unauthenticated

curl -X POST http://api.codasuaka.com/api/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Test","email":"test@test.com","password":"password123","password_confirmation":"password123"}'
```

---

### 9. Update Frontend API URL

Di aplikasi Android, set base URL ke:
```
http://api.codasuaka.com/api
```
(pakai `http` dulu selama development, ganti ke `https` kalau sudah production)

---

> **📌 Catatan untuk Development:**
> - ❌ Jangan jalankan `php artisan config:cache` — biar perubahan kode langsung terdeteksi
> - ❌ Jangan pakai `--optimize-autoloader` atau `--no-dev` di composer
> - ✅ Set `APP_DEBUG=true` biar lihat error detail
> - ✅ Set `APP_ENV=local`
> - Boleh pakai HTTP dulu, SSL tidak wajib untuk development
> - Setiap pull update, cukup `composer install` + `php artisan migrate` saja
