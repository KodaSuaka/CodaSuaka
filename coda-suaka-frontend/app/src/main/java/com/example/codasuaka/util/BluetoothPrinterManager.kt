package com.example.codasuaka.util

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.codasuaka.data.remote.dto.NotaDto
import com.example.codasuaka.ui.util.formatRupiah
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.util.*

class BluetoothPrinterManager(private val context: Context) {

    private val bluetoothManager: BluetoothManager? = 
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

    private val SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")

    /**
     * Cek apakah izin Bluetooth sudah diberikan.
     */
    fun hasPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Ambil daftar perangkat Bluetooth yang sudah dipasangkan (paired).
     */
    @SuppressLint("MissingPermission")
    fun getPairedDevices(): List<BluetoothDevice> {
        if (!hasPermissions() || bluetoothAdapter == null) return emptyList()
        return bluetoothAdapter.bondedDevices.toList()
    }

    /**
     * Cetak Nota ke printer yang dipilih.
     */
    @SuppressLint("MissingPermission")
    suspend fun printNota(device: BluetoothDevice, nota: NotaDto, operatorName: String? = null): Result<Unit> = withContext(Dispatchers.IO) {
        var socket: BluetoothSocket? = null
        try {
            socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
            socket.connect()
            
            val outputStream = socket.outputStream
            
            // Format Struk
            outputStream.write(ESC_POS.INIT)
            outputStream.write(ESC_POS.ALIGN_CENTER)
            outputStream.write("CODA SUAKA\n".toByteArray())
            outputStream.write("Penyegar Dahaga & Jiwa\n".toByteArray())
            outputStream.write("--------------------------------\n".toByteArray())
            
            outputStream.write(ESC_POS.ALIGN_LEFT)
            outputStream.write("No: ${nota.nomorNota}\n".toByteArray())
            outputStream.write("Tgl: ${nota.tanggal}\n".toByteArray())
            operatorName?.let {
                outputStream.write("Kasir: $it\n".toByteArray())
            }
            outputStream.write("--------------------------------\n".toByteArray())
            
            nota.items?.forEach { item ->
                outputStream.write("${item.namaItem}\n".toByteArray())
                val qtyStr = formatQty(item.kuantitas)
                val priceStr = formatRupiah(item.hargaSatuan)
                val subtotalStr = formatRupiah(item.subtotal)
                
                // Baris detail: "1 x 10.000      10.000"
                val detailLine = String.format("%s x %s", qtyStr, priceStr).padEnd(20) + subtotalStr.padStart(12)
                outputStream.write("$detailLine\n".toByteArray())
            }
            
            outputStream.write("--------------------------------\n".toByteArray())
            outputStream.write(ESC_POS.ALIGN_RIGHT)
            outputStream.write("TOTAL: ${formatRupiah(nota.total)}\n".toByteArray())
            
            outputStream.write(ESC_POS.ALIGN_CENTER)
            outputStream.write("\nTerima Kasih\n".toByteArray())
            outputStream.write("Selamat Menikmati!\n\n\n\n".toByteArray())
            
            outputStream.flush()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            try { socket?.close() } catch (e: Exception) {}
        }
    }

    private fun formatQty(value: Double): String {
        return if (value == value.toLong().toDouble()) value.toLong().toString() else value.toString()
    }

    /**
     * Kumpulan perintah ESC/POS dasar.
     */
    object ESC_POS {
        val INIT = byteArrayOf(0x1B, 0x40)
        val ALIGN_LEFT = byteArrayOf(0x1B, 0x61, 0x00)
        val ALIGN_CENTER = byteArrayOf(0x1B, 0x61, 0x01)
        val ALIGN_RIGHT = byteArrayOf(0x1B, 0x61, 0x02)
        val BOLD_ON = byteArrayOf(0x1B, 0x45, 0x01)
        val BOLD_OFF = byteArrayOf(0x1B, 0x45, 0x00)
    }
}
