# 🧹 Auto-Clean Gradle Project - Git Hook

## 📋 Deskripsi

Auto-clean Gradle project akan dijalankan **otomatis** setiap kali kamu melakukan `git push`. Script ini membersihkan artefak build Gradle (Android) untuk memastikan repository tetap bersih dari file-file build yang usang.

## 📁 Struktur File

```
scripts/
├── gradle-auto-clean.sh      # Script auto-clean (Unix/Mac)
├── gradle-auto-clean.bat     # Script auto-clean (Windows)
├── install-hooks.sh          # Install Git hooks (Unix/Mac)
├── install-hooks.bat         # Install Git hooks (Windows)
├── uninstall-hooks.sh        # Uninstall Git hooks (Unix/Mac)
└── uninstall-hooks.bat       # Uninstall Git hooks (Windows)
```

## 🚀 Cara Setup

### Windows (Recommended untuk project ini)

Buka terminal di root directory `CodaSuaka` dan jalankan:

```cmd
scripts\install-hooks.bat
```

### Unix / Mac

Buka terminal di root directory `CodaSuaka` dan jalankan:

```bash
chmod +x scripts/install-hooks.sh
./scripts/install-hooks.sh
```

### Manual Setup

Jika ingin install manual, buat file `.git/hooks/pre-push` dengan isi:

```bash
#!/bin/bash
cd coda-suaka-frontend
./gradlew clean --no-daemon --quiet
cd -
exit 0
```

Lalu buat executable:
```bash
chmod +x .git/hooks/pre-push
```

## ✅ Cara Kerja

1. **Saat push**: Git akan menjalankan hook `pre-push` secara otomatis
2. **Hook menjalankan**: `gradle clean` pada direktori `coda-suaka-frontend/`
3. **Pembersihan**: Menghapus direktori `build/`, `.gradle/`, dan `app/build/`
4. **Push dilanjutkan**: Setelah clean selesai, push akan berlanjut

## 🛑 Uninstall

### Windows

```cmd
scripts\uninstall-hooks.bat
```

### Unix / Mac

```bash
chmod +x scripts/uninstall-hooks.sh
./scripts/uninstall-hooks.sh
```

## ⚠️ Catatan Penting

- Hook hanya berjalan di **lokal komputer** kamu (client-side hook)
- Setiap developer perlu menjalankan `install-hooks` di komputer mereka masing-masing
- Jika hook sudah ada, backup otomatis dibuat dengan ekstensi `.backup`
- Hook bisa di-skip sementara dengan: `git push --no-verify`

## 🔧 Troubleshooting

### Hook tidak berjalan
```bash
# Cek apakah hook ada dan executable
ls -la .git/hooks/pre-push

# Pastikan file memiliki permission execute
chmod +x .git/hooks/pre-push
```

### Gradle clean gagal
- Pastikan `JAVA_HOME` sudah terkonfigurasi
- Pastikan `coda-suaka-frontend/gradlew` ada
- Coba jalankan manual: `cd coda-suaka-frontend && ./gradlew clean`

### Skip hook sementara
```bash
# Skip hook untuk satu kali push
git push --no-verify

# Atau
git push -n
```
