#!/bin/bash
# =============================================================================
# Gradle Auto-Clean Script
# Dijalankan otomatis saat push ke Git repository
# =============================================================================
# Script ini akan melakukan clean build pada proyek Gradle (Android)
# untuk memastikan tidak ada artefak build yang usang.
# =============================================================================

set -e

# Warna untuk output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Konfigurasi
GRADLE_PROJECT_DIR="coda-suaka-frontend"
GRADLEW_SCRIPT="./${GRADLE_PROJECT_DIR}/gradlew"

echo ""
echo -e "${BLUE}============================================${NC}"
echo -e "${BLUE}  🧹 Gradle Auto-Clean Script               ${NC}"
echo -e "${BLUE}============================================${NC}"
echo ""

# Cek apakah direktori proyek Gradle ada
if [ ! -d "$GRADLE_PROJECT_DIR" ]; then
    echo -e "${RED}❌ Direktori proyek Gradle tidak ditemukan: ${GRADLE_PROJECT_DIR}${NC}"
    exit 1
fi

# Cek apakah gradlew ada dan executable
if [ ! -f "$GRADLEW_SCRIPT" ]; then
    echo -e "${RED}❌ File gradlew tidak ditemukan di ${GRADLE_PROJECT_DIR}/${NC}"
    exit 1
fi

# Pastikan gradlew executable
chmod +x "$GRADLEW_SCRIPT" 2>/dev/null || true

echo -e "${YELLOW}📂 Working directory: $(pwd)${NC}"
echo -e "${YELLOW}🎯 Target: ${GRADLE_PROJECT_DIR}${NC}"
echo ""

# Step 1: Clean build
echo -e "${BLUE}📋 Step 1/3: Menjalankan gradle clean...${NC}"
if $GRADLEW_SCRIPT clean --no-daemon --quiet 2>/dev/null; then
    echo -e "${GREEN}   ✅ Gradle clean berhasil${NC}"
else
    echo -e "${YELLOW}   ⚠️  Gradle clean selesai dengan warning (biasa terjadi)${NC}"
fi

# Step 2: Hapus build cache directory
echo -e "${BLUE}📋 Step 2/3: Menghapus build cache...${NC}"
BUILD_DIR="${GRADLE_PROJECT_DIR}/app/build"
if [ -d "$BUILD_DIR" ]; then
    rm -rf "$BUILD_DIR"
    echo -e "${GREEN}   ✅ Build directory berhasil dihapus${NC}"
else
    echo -e "${GREEN}   ✅ Build directory sudah bersih${NC}"
fi

# Step 3: Bersihkan generated files
echo -e "${BLUE}📋 Step 3/3: Membersihkan generated files...${NC}"
GENERATED_DIRS=(
    "${GRADLE_PROJECT_DIR}/app/build"
    "${GRADLE_PROJECT_DIR}/.gradle"
    "${GRADLE_PROJECT_DIR}/build"
)

for dir in "${GENERATED_DIRS[@]}"; do
    if [ -d "$dir" ]; then
        rm -rf "$dir"
        echo -e "${GREEN}   ✅ ${dir} berhasil dihapus${NC}"
    fi
done

echo ""
echo -e "${GREEN}============================================${NC}"
echo -e "${GREEN}  ✅ Auto-clean selesai!                      ${NC}"
echo -e "${GREEN}============================================${NC}"
echo ""
