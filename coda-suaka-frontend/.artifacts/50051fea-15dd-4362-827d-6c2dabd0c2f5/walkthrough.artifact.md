# Walkthrough - Receipt Customization Feature

I have implemented the "Receipt Settings" feature, which allows you to fully customize the printed Bluetooth receipts "directly from here" in the app.

## New Feature: Pengaturan Struk

### 1. Custom Text Control
- **Header:** You can now change the store name (e.g., from "CODA SUAKA" to your specific branch name).
- **Tagline:** Customize the slogan below the header.
- **Footer:** Two lines of customizable text at the bottom of the receipt for "Thank You" messages or social media handles.
- **Local Persistence:** All settings are saved locally on your device and will be remembered for all future prints.

### 2. Enhanced Information on Receipt
- Added **Nama Pelanggan (Pihak Terkait)** to the printed receipt.
- Added **Catatan (Note)** to the printed receipt if filled.

## How to Use
1. Open the **Sidebar (Navigation Drawer)** from the Dashboard.
2. Select **Pengaturan Struk** under the "Operasional Toko" category.
3. Edit the fields as desired and click **Simpan Pengaturan**.
4. Go to any Nota in "Riwayat Nota" and click **Cetak Struk**. The receipt will now use your custom text.
5. If you want to go back to original settings, just click **Reset ke Default** in the settings screen.

## Technical Details
- **Manager:** Created `PreferenceManager` using Android's `SharedPreferences` for fast local access.
- **Printer Integration:** Modified `BluetoothPrinterManager` to inject these preferences before sending data to the printer.

> [!SUCCESS]
> The receipt is now fully customizable within the app!
