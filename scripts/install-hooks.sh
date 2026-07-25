#!/bin/bash
# =============================================================================
# Install Git Hooks Script
# =============================================================================
# Script ini menginstall Git hooks untuk auto-clean Gradle saat push.
# Jalankan script ini sekali untuk mengkonfigurasi Git hooks.
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

echo ""
echo -e "${BLUE}============================================${NC}"
echo -e "${BLUE}  🔧 Install Git Hooks                      ${NC}"
echo -e "${BLUE}============================================${NC}"
echo ""

# Cek apakah .git/hooks ada
if [ ! -d "$HOOKS_DIR" ]; then
    echo -e "${RED}❌ Direktori .git/hooks tidak ditemukan.${NC}"
    echo -e "${RED}   Pastikan Anda menjalankan script ini dari root repository.${NC}"
    exit 1
fi

# Backup hook yang sudah ada
if [ -f "$PRE_PUSH_HOOK" ]; then
    echo -e "${YELLOW}⚠️  File pre-push hook sudah ada. Backup ke pre-push.backup${NC}"
    cp "$PRE_PUSH_HOOK" "${PRE_PUSH_HOOK}.backup"
fi

# Buat pre-push hook
cat > "$PRE_PUSH_HOOK" << 'HOOK_CONTENT'
#!/bin/bash
# =============================================================================
# Pre-Push Git Hook
# Auto-clean Gradle project sebelum push
# =============================================================================
# Hook ini dijalankan otomatis oleh Git sebelum push dimulai.
# Script ini akan membersihkan proyek Gradle untuk memastikan
# tidak ada artefak build yang usang di repository.
# =============================================================================

# Dapatkan root directory dari Git
GIT_ROOT=$(git rev-parse --show-toplevel)

# Konfigurasi
GRADLE_PROJECT_DIR="${GIT_ROOT}/coda-suaka-frontend"
GRADLEW_SCRIPT="${GRADLE_PROJECT_DIR}/gradlew"

# Warna untuk output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo ""
echo -e "${BLUE}🧹 [Pre-Push Hook] Auto-clean Gradle project...${NC}"
echo ""

# Cek apakah direktori proyek Gradle ada
if [ ! -d "$GRADLE_PROJECT_DIR" ]; then
    echo -e "${YELLOW}⚠️  Direktori Gradle tidak ditemukan, skip auto-clean.${NC}"
    exit 0
fi

# Cek apakah gradlew ada
if [ ! -f "$GRADLEW_SCRIPT" ]; then
    echo -e "${YELLOW}⚠️  File gradlew tidak ditemukan, skip auto-clean.${NC}"
    exit 0
fi

# Pastikan gradlew executable
chmod +x "$GRADLEW_SCRIPT" 2>/dev/null || true

# Jalankan clean
echo -e "${BLUE}   📂 Target: coda-suaka-frontend${NC}"
cd "$GRADLE_PROJECT_DIR"

if ./gradlew clean --no-daemon --quiet 2>/dev/null; then
    echo -e "${GREEN}   ✅ Gradle clean berhasil${NC}"
else
    echo -e "${YELLOW}   ⚠️  Gradle clean selesai dengan warning${NC}"
fi

# Hapus build directories
echo -e "${BLUE}   🗑️  Menghapus build directories...${NC}"
for dir in "app/build" ".gradle" "build"; do
    if [ -d "$dir" ]; then
        rm -rf "$dir"
        echo -e "${GREEN}   ✅ ${dir} berhasil dihapus${NC}"
    fi
done

cd "$GIT_ROOT"

echo ""
echo -e "${GREEN}   ✅ Auto-clean selesai! Siap untuk push.${NC}"
echo ""

exit 0
HOOK_CONTENT

# Buat hook executable
chmod +x "$PRE_PUSH_HOOK"

echo -e "${GREEN}✅ Pre-push hook berhasil diinstall!${NC}"
echo ""
echo -e "${BLUE}📌 Catatan:${NC}"
echo -e "   - Hook akan otomatis menjalankan gradle clean sebelum push"
echo -e "   - Backup hook lama tersimpan di: ${PRE_PUSH_HOOK}.backup"
echo -e "   - Untuk uninstall: hapus ${PRE_PUSH_HOOK} atau jalankan uninstall-hooks.sh"
echo ""
