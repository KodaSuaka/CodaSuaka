package com.example.codasuaka.util

/**
 * Penerjemah pesan error dari server menjadi pesan ramah pengguna (Bahasa Indonesia).
 *
 * Tujuan: menghilangkan kode HTTP, stack trace, atau pesan teknis server
 * dan menggantinya dengan pesan yang dapat dipahami pengguna awam.
 */
object ErrorMessageMapper {

    // ── Tipe Notifikasi ──────────────────────────────────────
    enum class NotificationType {
        ERROR,
        SUCCESS,
        WARNING,
        INFO
    }

    // ── Data class untuk hasil mapping ────────────────────────
    data class MappedMessage(
        val message: String,
        val type: NotificationType,
        val title: String? = null
    )

    // ── Pola regex untuk mendeteksi HTTP status code ──────────
    private val httpCodePattern = Regex("""(?:Gagal[^:]*:\s*)?(\d{3})""")
    private val exceptionPattern = Regex("""(?i)(java\.\w+[.\w]*Exception|kotlin\.\w+[.\w]*Exception)""")
    private val connectionPattern = Regex("""(?i)(timeout|connection|refused|unreachable|resolve|unknown host)""")

    /**
     * Map error message mentah dari server menjadi pesan user-friendly.
     * @param rawMessage pesan error asli dari server / exception
     * @param context konteks operasi (opsional, untuk pesan lebih spesifik)
     * @return MappedMessage dengan pesan ramah pengguna
     */
    fun map(rawMessage: String?, context: String? = null): MappedMessage {
        if (rawMessage.isNullOrBlank()) {
            return MappedMessage(
                message = mapContextToDefault(context),
                type = NotificationType.ERROR
            )
        }

        val msg = rawMessage.trim()

        // ── 1. Deteksi masalah koneksi/jaringan ──
        if (connectionPattern.containsMatchIn(msg)) {
            return MappedMessage(
                message = "Tidak dapat terhubung ke server. Periksa koneksi internet Anda dan coba lagi.",
                type = NotificationType.ERROR,
                title = "Koneksi Terputus"
            )
        }

        // ── 2. Deteksi HTTP status code ──
        val codeMatch = httpCodePattern.find(msg)
        if (codeMatch != null) {
            val code = codeMatch.groupValues[1].toIntOrNull()
            if (code != null) {
                return mapHttpCode(code, context, msg)
            }
        }

        // ── 3. Deteksi exception mentah ──
        if (exceptionPattern.containsMatchIn(msg)) {
            return MappedMessage(
                message = mapContextToDefault(context),
                type = NotificationType.ERROR
            )
        }

        // ── 4. Deteksi pesan error umum dari server ──
        val serverMapped = mapServerMessage(msg, context)
        if (serverMapped != null) {
            return serverMapped
        }

        // ── 5. Jika pesan mengandung kata kunci "Gagal" atau sejenisnya, tampilkan apa adanya ──
        // daripada fallback ke generic message.
        if (msg.length < 100 && (msg.contains("Gagal") || msg.contains("Error") || msg.contains("Tidak"))) {
            return MappedMessage(
                message = msg,
                type = NotificationType.ERROR
            )
        }

        // ── 6. Fallback: gunakan pesan konteks atau generic ──
        return MappedMessage(
            message = mapContextToDefault(context),
            type = NotificationType.ERROR
        )
    }

    /**
     * Map HTTP status code ke pesan ramah pengguna.
     */
    private fun mapHttpCode(code: Int, context: String?, msg: String): MappedMessage {
        return when (code) {
            400 -> MappedMessage(
                message = "Data yang dikirim tidak valid. Periksa kembali isian Anda.",
                type = NotificationType.WARNING,
                title = "Data Tidak Valid"
            )
            401 -> MappedMessage(
                message = "Sesi Anda telah berakhir. Silakan masuk kembali.",
                type = NotificationType.ERROR,
                title = "Sesi Berakhir"
            )
            403 -> MappedMessage(
                message = "Anda tidak memiliki izin untuk melakukan akses ini.",
                type = NotificationType.ERROR,
                title = "Akses Ditolak"
            )
            404 -> MappedMessage(
                message = if (context != null) {
                    "${capitalizeContext(context)} tidak ditemukan."
                } else {
                    "Data yang dicari tidak ditemukan."
                },
                type = NotificationType.WARNING,
                title = "Tidak Ditemukan"
            )
            408, 504 -> MappedMessage(
                message = "Permintaan memakan waktu terlalu lama. Silakan coba beberapa saat lagi.",
                type = NotificationType.ERROR,
                title = "Waktu Habis"
            )
            409 -> MappedMessage(
                message = if (context != null) {
                    "${capitalizeContext(context)} sudah ada atau terjadi konflik data."
                } else {
                    "Terjadi konflik data. Periksa kembali data Anda."
                },
                type = NotificationType.WARNING,
                title = "Konflik Data"
            )
            413, 422 -> {
                // Bug #7: Coba ekstrak detail validasi dari pesan server
                val validationDetail = extractValidationDetails(msg)
                MappedMessage(
                    message = validationDetail
                        ?: "Format data tidak sesuai. Periksa kembali isian Anda.",
                    type = NotificationType.WARNING,
                    title = "Format Tidak Sesuai"
                )
            }
            429 -> MappedMessage(
                message = "Terlalu banyak permintaan. Tunggu sebentar lalu coba lagi.",
                type = NotificationType.WARNING,
                title = "Terlalu Banyak Permintaan"
            )
            in 500..599 -> MappedMessage(
                message = "Terjadi kesalahan pada server. Tim teknis telah diberitahu. Silakan coba lagi nanti.",
                type = NotificationType.ERROR,
                title = "Kesalahan Server"
            )
            else -> MappedMessage(
                message = mapContextToDefault(context),
                type = NotificationType.ERROR
            )
        }
    }

    /**
     * Map pesan error server yang umum ke pesan user-friendly.
     */
    private fun mapServerMessage(msg: String, context: String?): MappedMessage? {
        val lower = msg.lowercase()

        // ── Validation errors ──
        if ("validation" in lower || "required" in lower || "field" in lower) {
            return MappedMessage(
                message = "Ada field yang belum diisi atau formatnya salah. Periksa kembali isian Anda.",
                type = NotificationType.WARNING,
                title = "Validasi Gagal"
            )
        }

        // ── Duplicate / unique ──
        if ("duplicate" in lower || "already" in lower || "unique" in lower || "sudah ada" in lower) {
            return MappedMessage(
                message = if (context != null) {
                    "${capitalizeContext(context)} dengan data yang sama sudah ada."
                } else {
                    "Data yang sama sudah ada dalam sistem."
                },
                type = NotificationType.WARNING,
                title = "Data Duplikat"
            )
        }

        // ── Not found ──
        if ("not found" in lower || "tidak ditemukan" in lower || "no results" in lower) {
            return MappedMessage(
                message = if (context != null) {
                    "${capitalizeContext(context)} tidak ditemukan."
                } else {
                    "Data yang dicari tidak ditemukan."
                },
                type = NotificationType.WARNING,
                title = "Tidak Ditemukan"
            )
        }

        // ── Unauthorized ──
        if ("unauthorized" in lower || "token" in lower || "expired" in lower) {
            return MappedMessage(
                message = "Sesi Anda telah berakhir. Silakan masuk kembali.",
                type = NotificationType.ERROR,
                title = "Sesi Berakhir"
            )
        }

        // ── Forbidden ──
        if ("forbidden" in lower || "dilarang" in lower || "izinkan" in lower) {
            return MappedMessage(
                message = "Anda tidak memiliki izin untuk melakukan aksi ini.",
                type = NotificationType.ERROR,
                title = "Akses Ditolak"
            )
        }

        // ── Server internal error ──
        if ("internal" in lower || "server error" in lower || "500" in lower) {
            return MappedMessage(
                message = "Terjadi kesalahan internal. Silakan coba lagi nanti.",
                type = NotificationType.ERROR,
                title = "Kesalahan Server"
            )
        }

        // ── No internet ──
        if ("network" in lower || "internet" in lower || "offline" in lower) {
            return MappedMessage(
                message = "Tidak ada koneksi internet. Periksa jaringan Anda.",
                type = NotificationType.ERROR,
                title = "Tanpa Koneksi"
            )
        }

        return null // tidak terdeteksi, gunakan fallback
    }

    /**
     * Default message berdasarkan context operasi.
     */
    private fun mapContextToDefault(context: String?): String {
        return if (context != null) {
            "Terjadi kesalahan saat memproses ${context.lowercase()}. Silakan coba lagi."
        } else {
            "Terjadi kesalahan yang tidak terduga. Silakan coba lagi."
        }
    }

    /**
     * Capitalize context untuk judul notifikasi.
     */
    private fun capitalizeContext(context: String): String {
        return context.replaceFirstChar { it.uppercase() }
    }

    /**
     * Bug #7: Ekstrak detail pesan validasi dari response server.
     * Contoh input: "The kategori field is required." atau
     * "422: {\"message\":\"The selected kategori is invalid.\", ...}"
     */
    private fun extractValidationDetails(msg: String): String? {
        // Deteksi pola validasi Laravel umum
        val patterns = listOf(
            Regex("""(?i)the\s+(\w[\w\s]*?)\s+field\s+(is\s+required|must|should)""", RegexOption.IGNORE_CASE),
            Regex("""(?i)the\s+selected\s+(\w[\w\s]*?)\s+is\s+invalid""", RegexOption.IGNORE_CASE),
            Regex("""(?i)(\w[\w\s]*?)\s+(must be|cannot|should be|is not)""", RegexOption.IGNORE_CASE),
        )

        val details = mutableListOf<String>()
        for (pattern in patterns) {
            val matches = pattern.findAll(msg)
            for (match in matches) {
                val field = match.groupValues[1].trim().lowercase()
                val issue = if (match.groupValues.size > 2) match.groupValues[2].trim() else ""
                if (field.isNotBlank() && field != "the") {
                    details.add("• ${capitalizeContext(field)}: $issue")
                }
            }
        }

        if (details.isNotEmpty()) {
            return "Terdapat kesalahan pada isian data:\n${details.joinToString("\n")}\n\nPeriksa kembali isian Anda."
        }

        return null
    }

}

