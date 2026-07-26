package com.example.codasuaka.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Utility untuk menangani konversi tanggal dan waktu dengan dukungan Zona Waktu.
 */
object DateTimeUtil {

    private val localZoneId = ZoneId.systemDefault()

    /**
     * Mengonversi ISO String (UTC) ke Format Terbaca (Lokal).
     * Contoh: "2026-07-25T17:00:00Z" -> "26 Juli 2026" (WIB)
     * Mendukung format ISO datetime lengkap dan date-only (yyyy-MM-dd).
     */
    fun formatIsoToLocal(isoString: String?, pattern: String = "dd MMM yyyy"): String {
        if (isoString.isNullOrBlank()) return "-"
        val formatter = DateTimeFormatter.ofPattern(pattern, Locale.forLanguageTag("id-ID"))
        // Coba parse sebagai ISO datetime lengkap
        return try {
            val instant = Instant.parse(isoString)
            formatter.withZone(localZoneId).format(instant)
        } catch (_: Exception) {
            // Fallback: coba parse sebagai date-only (yyyy-MM-dd)
            try {
                val ld = LocalDate.parse(isoString.take(10))
                ld.format(formatter)
            } catch (_: Exception) {
                isoString.take(10)
            }
        }
    }

    /**
     * Mengonversi ISO String (UTC) ke objek LocalDate (Lokal).
     */
    fun toLocalLocalDate(isoString: String?): LocalDate {
        if (isoString.isNullOrBlank()) return LocalDate.now()
        return try {
            val instant = Instant.parse(isoString)
            instant.atZone(localZoneId).toLocalDate()
        } catch (_: Exception) {
            try {
                LocalDate.parse(isoString.take(10))
            } catch (_: Exception) {
                LocalDate.now()
            }
        }
    }

    /**
     * Mengonversi ISO String (UTC) ke Format Jam (Lokal).
     * Contoh: "2026-07-25T17:30:00Z" -> "00:30" (WIB)
     */
    fun formatIsoToTime(isoString: String?): String {
        if (isoString.isNullOrBlank()) return "-"
        return try {
            val instant = Instant.parse(isoString)
            val formatter = DateTimeFormatter.ofPattern("HH:mm", Locale.forLanguageTag("id-ID"))
                .withZone(localZoneId)
            formatter.format(instant)
        } catch (_: Exception) {
            isoString
        }
    }
}
