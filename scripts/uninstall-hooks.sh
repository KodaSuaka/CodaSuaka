#!/bin/bash
# =============================================================================
# Uninstall Git Hooks Script
# =============================================================================
# Script ini menghapus Git hooks yang sudah diinstall.
# =============================================================================

set -e

# Warna untuk output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Lokasi hooks
HOOKS_DIR=".git/hooks"
PRE_PUSH_HOOK="${HOOKS_DIR}/pre-push"
BACKUP_HOOK="${HOOKS_DIR}/pre-push.backup"

echo ""
echo -e "${BLUE}============================================${NC}"
echo -e "${BLUE}  🗑️  Uninstall Git Hooks                    ${NC}"
echo -e "${BLUE}============================================${NC}"
echo ""

# Cek apakah .git/hooks ada
if [ ! -d "$HOOKS_DIR" ]; then
    echo -e "${RED}❌ Direktori .git/hooks tidak ditemukan.${NC}"
    exit 1
fi

# Hapus pre-push hook
if [ -f "$PRE_PUSH_HOOK" ]; then
    rm -f "$PRE_PUSH_HOOK"
    echo -e "${GREEN}✅ Pre-push hook berhasil dihapus.${NC}"
else
    echo -e "${YELLOW}⚠️  Pre-push hook tidak ditemukan.${NC}"
fi

# Restore backup jika ada
if [ -f "$BACKUP_HOOK" ]; then
    cp "$BACKUP_HOOK" "$PRE_PUSH_HOOK"
    chmod +x "$PRE_PUSH_HOOK"
    rm -f "$BACKUP_HOOK"
    echo -e "${GREEN}✅ Backup hook lama berhasil direstore.${NC}"
fi

echo ""
echo -e "${GREEN}   Uninstall selesai!${NC}"
echo ""
