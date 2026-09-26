@echo off
REM =============================================================================
REM Uninstall Git Hooks Script (Windows)
REM =============================================================================

setlocal enabledelayedexpansion

set "HOOKS_DIR=.git\hooks"
set "PRE_PUSH_HOOK=%HOOKS_DIR%\pre-push"
set "BACKUP_HOOK=%HOOKS_DIR%\pre-push.backup"

echo.
echo ============================================
echo   Uninstall Git Hooks (Windows)
echo ============================================
echo.

REM Cek apakah .git\hooks ada
if not exist "%HOOKS_DIR%" (
    echo [ERROR] Direktori .git\hooks tidak ditemukan.
    exit /b 1
)

REM Hapus pre-push hook
if exist "%PRE_PUSH_HOOK%" (
    del /f /q "%PRE_PUSH_HOOK%"
    echo [OK] Pre-push hook berhasil dihapus.
) else (
    echo [WARN] Pre-push hook tidak ditemukan.
)

REM Restore backup jika ada
if exist "%BACKUP_HOOK%" (
    copy /y "%BACKUP_HOOK%" "%PRE_PUSH_HOOK%" >nul 2>&1
    del /f /q "%BACKUP_HOOK%"
    echo [OK] Backup hook lama berhasil direstore.
)

echo.
echo   Uninstall selesai!
echo.

endlocal
