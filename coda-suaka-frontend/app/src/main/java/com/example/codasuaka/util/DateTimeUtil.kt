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
    
        /**
         * Format date string (yyyy-MM-dd) ke format terbaca.
         * Contoh: "2026-07-25" -> "25 Juli 2026"
         */
        fun formatDateDisplay(dateString: String?): String {
            if (dateString.isNullOrBlank()) return "-"
            return try {
                val ld = LocalDate.parse(dateString.take(10))
                val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.forLanguageTag("id-ID"))
                ld.format(formatter)
            } catch (_: Exception) {
                dateString.take(10)
            }
        }
    
        /**
         * Format datetime string (ISO 8601) ke format terbaca lengkap.
         * Contoh: "2026-07-25T17:00:00Z" -> "25 Juli 2026, 00:00"
         */
        fun formatDateTimeDisplay(isoString: String?): String {
            if (isoString.isNullOrBlank()) return "-"
            return try {
                val instant = Instant.parse(isoString)
                val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm", Locale.forLanguageTag("id-ID"))
                    .withZone(localZoneId)
                formatter.format(instant)
            } catch (_: Exception) {
                // Fallback: coba format sebagai date-only
                formatDateDisplay(isoString)
            }
        }
    }
}
