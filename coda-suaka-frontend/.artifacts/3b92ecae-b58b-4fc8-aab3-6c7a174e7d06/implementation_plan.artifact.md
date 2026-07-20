# Implementation Plan - Theme Fix & UI Refinement

This plan addresses theme inconsistencies where some screens "force" dark mode or have clashing UI elements. We will ensure a consistent "Light Soft" theme across key screens, even when the system is in dark mode, and fix text overlapping issues.

## User Review Required

> [!IMPORTANT]
> I will be wrapping `ApprovalKeuanganScreen`, `LaporanKeuanganScreen`, and the `DatePicker` dialogs in a forced `LightColorScheme`. This ensures the "Soft & Colorful" aesthetic remains intact regardless of the user's system theme settings.

## Proposed Changes

### 1. Approval Keuangan Screen
#### [MODIFY] [ApprovalKeuanganScreen.kt](file:///C:/Users/ASUS/AndroidStudioProjects/CodaSuaka/coda-suaka-frontend/app/src/main/java/com/example/codasuaka/ui/screen/approval_keuangan/ApprovalKeuanganScreen.kt)
- Wrap the entire screen in `MaterialTheme(colorScheme = LightColorScheme)` to force light mode.
- Update `TopAppBar` to use `Surface` background with `Secondary` (Navy) text to match the other screens.
- Update `TabRow` to use `Surface` container color and `Primary` indicators.
- Set `Scaffold` background to `Tertiary` (White).
- Update `ApprovalCard` to use `Surface` container color and ensure text colors are consistent with the light theme.
- Fix `InfoRow` and `TolakDialog` colors.

### 2. Dashboard Screen
#### [MODIFY] [DashboardScreen.kt](file:///C:/Users/ASUS/AndroidStudioProjects/CodaSuaka/coda-suaka-frontend/app/src/main/java/com/example/codasuaka/ui/screen/dashboard/DashboardScreen.kt)
- Resolve text clashing in `MenuCard`:
    - Increase card height to **135.dp** to give more breathing room for 2-line labels like "Laporan Keuangan".
    - Adjust `verticalArrangement` to `Arrangement.Top` with a small spacer instead of `SpaceBetween` to prevent labels from being pushed too far down.

### 3. Laporan Keuangan (Buku Kas) Screen
#### [MODIFY] [LaporanKeuanganScreen.kt](file:///C:/Users/ASUS/AndroidStudioProjects/CodaSuaka/coda-suaka-frontend/app/src/main/java/com/example/codasuaka/ui/screen/laporan_keuangan/LaporanKeuanganScreen.kt)
- Wrap the screen in a forced light theme.
- Ensure the `TopAppBar` title and icons have adequate spacing.
- Refine `FilterBar`:
    - Fix potential text clashing in `FilterChip` labels.
    - Use a softer grey for the date indicator border.

### 4. Date Picker Refinement
#### [MODIFY] [KalenderScreen.kt](file:///C:/Users/ASUS/AndroidStudioProjects/CodaSuaka/coda-suaka-frontend/app/src/main/java/com/example/codasuaka/ui/screen/kalender/KalenderScreen.kt)
#### [MODIFY] [RiwayatKehadiranScreen.kt](file:///C:/Users/ASUS/AndroidStudioProjects/CodaSuaka/coda-suaka-frontend/app/src/main/java/com/example/codasuaka/ui/screen/riwayat_kehadiran/RiwayatKehadiranScreen.kt)
- Refine the `DatePicker` color scheme:
    - `onSurfaceVariant` will use `SecondaryLight` to differentiate from the main header text.
    - Ensure the header background is clearly separated from the calendar body.
    - Fix the "edit" icon tint and month/year selector colors.

## Verification Plan

### Manual Verification
- Deploy the app and toggle System Dark Mode.
- Verify that `ApprovalKeuanganScreen` and `LaporanKeuanganScreen` remain in the "Light Soft" theme.
- Check "Laporan Keuangan" menu card in Dashboard for text clashing.
- Open the Date Picker and verify the header and month/year navigation are clean and legible.
