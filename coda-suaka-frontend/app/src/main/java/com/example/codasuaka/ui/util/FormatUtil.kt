package com.example.codasuaka.ui.util

import java.text.NumberFormat
import java.util.Locale

/**
 * Util pemformatan angka untuk seluruh aplikasi.
 *
 * Semua definisi lokal formatRupiah/formatQty yang tersebar di screen
 * dikonsolidasikan ke sini agar format konsisten di semua layar.
 */

/** Format Rupiah: "Rp 12.345", negatif jadi "-Rp 12.345". */
fun formatRupiah(amount: Double): String {
    val isNegative = amount < 0
    val absStr = kotlin.math.abs(amount).toLong().toString()
    val sb = StringBuilder()
    var count = 0
    for (i in absStr.lastIndex downTo 0) {
        if (count > 0 && count % 3 == 0) sb.insert(0, '.')
        sb.insert(0, absStr[i])
        count++
    }
    val prefix = if (isNegative) "-Rp " else "Rp "
    return "$prefix$sb"
}

/**
 * Format kuantitas: bulat tampil tanpa desimal ("5"), pecahan tampil apa adanya ("2.5").
 */
fun formatQty(value: Double): String {
    return if (value == value.toLong().toDouble()) value.toLong().toString() else value.toString()
}

/** Format Rupiah via NumberFormat Indonesia (dipakai ApprovalKeuangan dkk). */
fun formatRupiahNumber(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    format.maximumFractionDigits = 0
    return format.format(amount)
}
