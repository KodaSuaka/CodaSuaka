# Implementation Plan - Receipt Customization Feature

This plan outlines the steps to add a "Receipt Settings" (Pengaturan Struk) feature, allowing users to customize the header, tagline, and footer of the printed Bluetooth receipt.

## User Review Required

> [!IMPORTANT]
> The customization will be stored locally on the device using `SharedPreferences`. This means if the user switches devices, they will need to re-configure their receipt settings.

## Proposed Changes

### 1. Local Storage for Settings
Create a manager to handle storing and retrieving receipt settings.

#### [NEW] [PreferenceManager.kt](file:///C:/Users/ASUS/AndroidStudioProjects/CodaSuaka/coda-suaka-frontend/app/src/main/java/com/example/codasuaka/data/local/PreferenceManager.kt)
- Manage keys for `header_text`, `tagline_text`, `footer_text_1`, and `footer_text_2`.
- Provide default values (e.g., "CODA SUAKA", "Penyegar Dahaga & Jiwa").

#### [MODIFY] [AppModule.kt](file:///C:/Users/ASUS/AndroidStudioProjects/CodaSuaka/coda-suaka-frontend/app/src/main/java/com/example/codasuaka/di/AppModule.kt)
- Register `PreferenceManager` as a single instance in Koin.

---

### 2. Receipt Settings UI
Add a screen for the user to edit these settings.

#### [NEW] [ReceiptSettingsScreen.kt](file:///C:/Users/ASUS/AndroidStudioProjects/CodaSuaka/coda-suaka-frontend/app/src/main/java/com/example/codasuaka/ui/screen/receipt_settings/ReceiptSettingsScreen.kt)
- A form with text fields for Header, Tagline, and Footer messages.
- A "Simpan" button to persist changes.
- A "Reset ke Default" option.

#### [NEW] [ReceiptSettingsViewModel.kt](file:///C:/Users/ASUS/AndroidStudioProjects/CodaSuaka/coda-suaka-frontend/app/src/main/java/com/example/codasuaka/ui/screen/receipt_settings/ReceiptSettingsViewModel.kt)
- Handle UI state and interaction with `PreferenceManager`.

#### [MODIFY] [ViewModelModule.kt](file:///C:/Users/ASUS/AndroidStudioProjects/CodaSuaka/coda-suaka-frontend/app/src/main/java/com/example/codasuaka/di/ViewModelModule.kt)
- Register `ReceiptSettingsViewModel`.

---

### 3. Navigation & Integration
Link the new screen to the rest of the app.

#### [MODIFY] [AppNavigation.kt](file:///C:/Users/ASUS/AndroidStudioProjects/CodaSuaka/coda-suaka-frontend/app/src/main/java/com/example/codasuaka/navigation/AppNavigation.kt)
- Add `Routes.RECEIPT_SETTINGS`.
- Add the composable route to `AppNavigation`.

#### [MODIFY] [DashboardScreen.kt](file:///C:/Users/ASUS/AndroidStudioProjects/CodaSuaka/coda-suaka-frontend/app/src/main/java/com/example/codasuaka/ui/screen/dashboard/DashboardScreen.kt)
- Add "Pengaturan Struk" to the navigation drawer under "Operasional Toko".

---

### 4. Printer Logic Update
Update the Bluetooth printing logic to use customized values.

#### [MODIFY] [BluetoothPrinterManager.kt](file:///C:/Users/ASUS/AndroidStudioProjects/CodaSuaka/coda-suaka-frontend/app/src/main/java/com/example/codasuaka/util/BluetoothPrinterManager.kt)
- Inject `PreferenceManager`.
- Replace hardcoded strings in `printNota` with values retrieved from `PreferenceManager`.

## Verification Plan

### Automated Tests
- N/A

### Manual Verification
1.  **Navigate:** Open Sidebar -> Click "Pengaturan Struk".
2.  **Edit:** Change the Header to "CODA SUAKA - CABANG A", change the Footer to "Terima Kasih Banyak!".
3.  **Save:** Click "Simpan".
4.  **Print:** Go to "Riwayat Nota" -> Pick a nota -> Click "Cetak Struk".
5.  **Verify:** Check the printout (or logs) to confirm the new Header and Footer are used.
6.  **Reset:** Click "Reset ke Default" in settings and verify the printout returns to original values.
