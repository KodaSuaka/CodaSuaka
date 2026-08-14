package com.example.codasuaka.data.local

import android.content.Context
import android.content.SharedPreferences

/**
 * Manajer penyimpanan pengaturan aplikasi non-auth (seperti kustomisasi struk).
 */
class PreferenceManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "codasuaka_app_prefs"
        private const val KEY_HEADER = "receipt_header"
        private const val KEY_TAGLINE = "receipt_tagline"
        private const val KEY_FOOTER_1 = "receipt_footer_1"
        private const val KEY_FOOTER_2 = "receipt_footer_2"

        // Default values
        private const val DEFAULT_HEADER = "CODA SUAKA"
        private const val DEFAULT_TAGLINE = "Penyegar Dahaga & Jiwa"
        private const val DEFAULT_FOOTER_1 = "Terima Kasih"
        private const val DEFAULT_FOOTER_2 = "Selamat Menikmati!"
    }

    fun getHeaderText(): String = prefs.getString(KEY_HEADER, DEFAULT_HEADER) ?: DEFAULT_HEADER
    fun getTaglineText(): String = prefs.getString(KEY_TAGLINE, DEFAULT_TAGLINE) ?: DEFAULT_TAGLINE
    fun getFooterText1(): String = prefs.getString(KEY_FOOTER_1, DEFAULT_FOOTER_1) ?: DEFAULT_FOOTER_1
    fun getFooterText2(): String = prefs.getString(KEY_FOOTER_2, DEFAULT_FOOTER_2) ?: DEFAULT_FOOTER_2

    fun saveReceiptSettings(header: String, tagline: String, footer1: String, footer2: String) {
        prefs.edit()
            .putString(KEY_HEADER, header.ifBlank { DEFAULT_HEADER })
            .putString(KEY_TAGLINE, tagline)
            .putString(KEY_FOOTER_1, footer1)
            .putString(KEY_FOOTER_2, footer2)
            .apply()
    }

    fun resetToDefault() {
        prefs.edit().clear().apply()
    }
}
