@echo off
REM =============================================================================
REM Gradle Auto-Clean Script (Windows)
REM Dijalankan otomatis saat push ke Git repository
REM =============================================================================
REM Script ini akan melakukan clean build pada proyek Gradle (Android)
REM untuk memastikan tidak ada artefak build yang usang.
REM =============================================================================

setlocal enabledelayedexpansion

REM Konfigurasi
set "GRADLE_PROJECT_DIR=coda-suaka-frontend"
set "GRADLEW_SCRIPT=%GRADLE_PROJECT_DIR%\gradlew.bat"

echo.
echo ============================================
echo   Gradle Auto-Clean Script (Windows)
echo ============================================
echo.

REM Cek apakah direktori proyek Gradle ada
if not exist "%GRADLE_PROJECT_DIR%" (
    echo [ERROR] Direktori proyek Gradle tidak ditemukan: %GRADLE_PROJECT_DIR%
    exit /b 1
)

REM Cek apakah gradlew.bat ada
if not exist "%GRADLEW_SCRIPT%" (
    echo [ERROR] File gradlew.bat tidak ditemukan di %GRADLE_PROJECT_DIR%\
    exit /b 1
)

echo [INFO] Working directory: %CD%
echo [INFO] Target: %GRADLE_PROJECT_DIR%
echo.

REM Step 1: Clean build
echo [STEP 1/3] Menjalankan gradle clean...
call "%GRADLEW_SCRIPT%" clean --no-daemon --quiet 2>nul
if %ERRORLEVEL% EQU 0 (
    echo [OK] Gradle clean berhasil
) else (
    echo [WARN] Gradle clean selesai dengan warning (biasa terjadi)
)

REM Step 2: Hapus build cache directory
echo [STEP 2/3] Menghapus build cache...
set "BUILD_DIR=%GRADLE_PROJECT_DIR%\app\build"
if exist "%BUILD_DIR%" (
    rmdir /s /q "%BUILD_DIR%"
    echo [OK] Build directory berhasil dihapus
) else (
    echo [OK] Build directory sudah bersih
)

REM Step 3: Bersihkan generated files
echo [STEP 3/3] Membersihkan generated files...

set "DIRS=%GRADLE_PROJECT_DIR%\app\build %GRADLE_PROJECT_DIR%\.gradle %GRADLE_PROJECT_DIR%\build"

for %%D in (%DIRS%) do (
    if exist "%%D" (
        rmdir /s /q "%%D"
        echo [OK] %%D berhasil dihapus
    )
)

echo.
echo ============================================
echo   Auto-clean selesai!
echo ============================================
echo.

endlocal
