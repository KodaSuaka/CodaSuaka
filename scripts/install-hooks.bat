@echo off
REM =============================================================================
REM Install Git Hooks Script (Windows)
REM =============================================================================
REM Script ini menginstall Git hooks untuk auto-clean Gradle saat push.
REM Jalankan script ini sekali dari root repository untuk mengkonfigurasi.
REM =============================================================================

setlocal enabledelayedexpansion

set "HOOKS_DIR=.git\hooks"
set "PRE_PUSH_HOOK=%HOOKS_DIR%\pre-push"
set "BACKUP_HOOK=%HOOKS_DIR%\pre-push.backup"

echo.
echo ============================================
echo   Install Git Hooks (Windows)
echo ============================================
echo.

REM Cek apakah .git\hooks ada
if not exist "%HOOKS_DIR%" (
    echo [ERROR] Direktori .git\hooks tidak ditemukan.
    echo         Pastikan Anda menjalankan script ini dari root repository.
    exit /b 1
)

REM Backup hook yang sudah ada
if exist "%PRE_PUSH_HOOK%" (
    echo [WARN] File pre-push hook sudah ada. Backup ke pre-push.backup
    copy /y "%PRE_PUSH_HOOK%" "%BACKUP_HOOK%" >nul 2>&1
)

REM Buat pre-push hook
(
echo #!/bin/bash
echo # Pre-Push Git Hook - Auto-clean Gradle project
echo #
echo # Hook ini dijalankan otomatis oleh Git sebelum push dimulai.
echo # Membersihkan proyek Gradle untuk memastikan tidak ada artefak build usang.
echo.
echo # Dapatkan root directory dari Git
echo GIT_ROOT=%%^(git rev-parse --show-toplevel%%^)
echo.
echo # Konfigurasi
echo GRADLE_PROJECT_DIR="%%{GIT_ROOT}/coda-suaka-frontend"
echo GRADLEW_SCRIPT="%%{GRADLE_PROJECT_DIR}/gradlew"
echo.
echo RED='\033[0;31m'
echo GREEN='\033[0;32m'
echo YELLOW='\033[1;33m'
echo BLUE='\033[0;34m'
echo NC='\033[0m'
echo.
echo echo ""
echo echo -e "%%{BLUE}[Pre-Push Hook] Auto-clean Gradle project...%%{NC}"
echo echo ""
echo.
echo # Cek apakah direktori proyek Gradle ada
echo if [ ! -d "%%{GRADLE_PROJECT_DIR}" ]; then
echo     echo -e "%%{YELLOW}Direktori Gradle tidak ditemukan, skip auto-clean.%%{NC}"
echo     exit 0
echo fi
echo.
echo # Cek apakah gradlew ada
echo if [ ! -f "%%{GRADLEW_SCRIPT}" ]; then
echo     echo -e "%%{YELLOW}File gradlew tidak ditemukan, skip auto-clean.%%{NC}"
echo     exit 0
echo fi
echo.
echo # Pastikan gradlew executable
echo chmod +x "%%{GRADLEW_SCRIPT}" 2^>/dev/null ^|^| true
echo.
echo # Jalankan clean
echo echo -e "%%{BLUE}Target: coda-suaka-frontend%%{NC}"
echo cd "%%{GRADLE_PROJECT_DIR}"
echo.
echo if ./gradlew clean --no-daemon --quiet 2^>/dev/null; then
echo     echo -e "%%{GREEN}Gradle clean berhasil%%{NC}"
echo else
echo     echo -e "%%{YELLOW}Gradle clean selesai dengan warning%%{NC}"
echo fi
echo.
echo # Hapus build directories
echo for dir in "app/build" ".gradle" "build"; do
echo     if [ -d "%%{dir}" ]; then
echo         rm -rf "%%{dir}"
echo         echo -e "%%{GREEN}%%{dir} berhasil dihapus%%{NC}"
echo     fi
echo done
echo.
echo cd "%%{GIT_ROOT}"
echo.
echo echo -e "%%{GREEN}Auto-clean selesai! Siap untuk push.%%{NC}"
echo echo ""
echo.
echo exit 0
) > "%PRE_PUSH_HOOK%"

echo [OK] Pre-push hook berhasil diinstall!
echo.
echo ============================================
echo   Catatan:
echo   - Hook akan otomatis menjalankan gradle clean sebelum push
echo   - Backup hook lama: %BACKUP_HOOK%
echo   - Untuk uninstall: jalankan uninstall-hooks.bat
echo ============================================
echo.

endlocal
