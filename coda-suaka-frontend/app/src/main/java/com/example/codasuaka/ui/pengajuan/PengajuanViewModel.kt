package com.example.codasuaka.ui.pengajuan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

// ─── Enum Jenis Pengajuan ───

enum class JenisPengajuan(val displayName: String) {
    CUTI_TAHUNAN("Cuti Tahunan"),
    IZIN_SAKIT("Izin Sakit"),
    MENDADAK("Mendadak")
}

// ─── Data Model ───

data class Pengajuan(
    val id: Int = 0,
    val jenis: JenisPengajuan = JenisPengajuan.CUTI_TAHUNAN,
    val tanggalMulai: String = "",
    val tanggalSelesai: String = "",
    val jumlahHari: Int = 0,
    val keterangan: String = "",
    val status: String = "pending", // pending / disetujui / ditolak
    val createdAt: String = ""
)

// ─── UI State ───

data class PengajuanUiState(
    val selectedJenis: JenisPengajuan? = null,
    val tanggalMulai: String = "",
    val tanggalMulaiMillis: Long? = null,
    val tanggalSelesai: String = "",
    val tanggalSelesaiMillis: Long? = null,
    val keterangan: String = "",
    val jumlahHari: Int = 0,
    val riwayatPengajuan: List<Pengajuan> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

// ─── ViewModel ───

/**
 * ViewModel untuk halaman Pengajuan (Cuti/Izin).
 * TODO: Integrasi dengan API backend GET/POST /api/pengajuan
 */
class PengajuanViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PengajuanUiState())
    val uiState: StateFlow<PengajuanUiState> = _uiState

    private val dateFormatter = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
    private var nextId = 6 // mulai dari 6 karena dummy data sudah 5

    init {
        loadRiwayat()
    }

    /**
     * Memuat riwayat pengajuan dummy.
     * TODO: Panggil API GET /api/pengajuan
     */
    private fun loadRiwayat() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            delay(600)

            val dummyRiwayat = listOf(
                Pengajuan(
                    id = 1,
                    jenis = JenisPengajuan.CUTI_TAHUNAN,
                    tanggalMulai = "12 Juni 2026",
                    tanggalSelesai = "15 Juni 2026",
                    jumlahHari = 4,
                    keterangan = "Cuti tahunan untuk liburan keluarga ke Bali",
                    status = "disetujui",
                    createdAt = "01 Juni 2026"
                ),
                Pengajuan(
                    id = 2,
                    jenis = JenisPengajuan.IZIN_SAKIT,
                    tanggalMulai = "10 Juni 2026",
                    tanggalSelesai = "10 Juni 2026",
                    jumlahHari = 1,
                    keterangan = "Demam dan tidak bisa masuk kerja",
                    status = "pending",
                    createdAt = "10 Juni 2026"
                ),
                Pengajuan(
                    id = 3,
                    jenis = JenisPengajuan.MENDADAK,
                    tanggalMulai = "05 Juni 2026",
                    tanggalSelesai = "05 Juni 2026",
                    jumlahHari = 1,
                    keterangan = "Ada urusan keluarga mendadak",
                    status = "ditolak",
                    createdAt = "05 Juni 2026"
                ),
                Pengajuan(
                    id = 4,
                    jenis = JenisPengajuan.CUTI_TAHUNAN,
                    tanggalMulai = "20 Mei 2026",
                    tanggalSelesai = "22 Mei 2026",
                    jumlahHari = 3,
                    keterangan = "Cuti tahunan",
                    status = "disetujui",
                    createdAt = "15 Mei 2026"
                ),
                Pengajuan(
                    id = 5,
                    jenis = JenisPengajuan.IZIN_SAKIT,
                    tanggalMulai = "10 Mei 2026",
                    tanggalSelesai = "12 Mei 2026",
                    jumlahHari = 3,
                    keterangan = "Sakit typus",
                    status = "disetujui",
                    createdAt = "10 Mei 2026"
                )
            )

            _uiState.value = _uiState.value.copy(
                riwayatPengajuan = dummyRiwayat,
                isLoading = false
            )
        }
    }

    // ─── Form Input ───

    fun setJenisPengajuan(jenis: JenisPengajuan) {
        _uiState.value = _uiState.value.copy(
            selectedJenis = jenis,
            errorMessage = null
        )
    }

    fun setTanggalMulai(millis: Long) {
        val dateStr = dateFormatter.format(Date(millis))
        val state = _uiState.value
        val newState = state.copy(
            tanggalMulai = dateStr,
            tanggalMulaiMillis = millis,
            errorMessage = null
        )
        // Hitung ulang jumlah hari jika tanggal selesai sudah dipilih
        newState.tanggalSelesaiMillis?.let { selesaiMillis ->
            newState.copy(
                jumlahHari = hitungJumlahHari(millis, selesaiMillis)
            )
        }.let {
            _uiState.value = it ?: newState
        }
    }

    fun setTanggalSelesai(millis: Long) {
        val dateStr = dateFormatter.format(Date(millis))
        val state = _uiState.value
        val newState = state.copy(
            tanggalSelesai = dateStr,
            tanggalSelesaiMillis = millis,
            errorMessage = null
        )
        // Hitung ulang jumlah hari jika tanggal mulai sudah dipilih
        newState.tanggalMulaiMillis?.let { mulaiMillis ->
            newState.copy(
                jumlahHari = hitungJumlahHari(mulaiMillis, millis)
            )
        }.let {
            _uiState.value = it ?: newState
        }
    }

    fun onKeteranganChange(value: String) {
        _uiState.value = _uiState.value.copy(
            keterangan = value,
            errorMessage = null
        )
    }

    // ─── Actions ───

    /**
     * Mengajukan pengajuan cuti/izin baru.
     * TODO: Panggil API POST /api/pengajuan
     */
    fun ajukanPengajuan() {
        val state = _uiState.value

        // Validasi
        if (state.selectedJenis == null) {
            _uiState.value = state.copy(errorMessage = "Pilih jenis pengajuan terlebih dahulu.")
            return
        }
        if (state.tanggalMulaiMillis == null) {
            _uiState.value = state.copy(errorMessage = "Pilih tanggal mulai izin.")
            return
        }
        if (state.tanggalSelesaiMillis == null) {
            _uiState.value = state.copy(errorMessage = "Pilih tanggal selesai izin.")
            return
        }
        if (state.tanggalSelesaiMillis < state.tanggalMulaiMillis) {
            _uiState.value = state.copy(errorMessage = "Tanggal selesai harus setelah atau sama dengan tanggal mulai.")
            return
        }
        if (state.keterangan.trim().length < 10) {
            _uiState.value = state.copy(errorMessage = "Alasan minimal 10 karakter.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)
            delay(800) // Simulasi API call

            val pengajuanBaru = Pengajuan(
                id = nextId++,
                jenis = state.selectedJenis!!,
                tanggalMulai = state.tanggalMulai,
                tanggalSelesai = state.tanggalSelesai,
                jumlahHari = state.jumlahHari,
                keterangan = state.keterangan.trim(),
                status = "pending",
                createdAt = dateFormatter.format(Date())
            )

            _uiState.value = _uiState.value.copy(
                riwayatPengajuan = listOf(pengajuanBaru) + _uiState.value.riwayatPengajuan,
                isSaving = false,
                isSuccess = true,
                selectedJenis = null,
                tanggalMulai = "",
                tanggalMulaiMillis = null,
                tanggalSelesai = "",
                tanggalSelesaiMillis = null,
                keterangan = "",
                jumlahHari = 0,
                successMessage = "Pengajuan ${pengajuanBaru.jenis.displayName} berhasil diajukan."
            )
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null,
            isSuccess = false
        )
    }

    // ─── Private Helpers ───

    /**
     * Menghitung jumlah hari antara dua tanggal (inklusif).
     */
    private fun hitungJumlahHari(mulaiMillis: Long, selesaiMillis: Long): Int {
        val diff = selesaiMillis - mulaiMillis
        val days = TimeUnit.MILLISECONDS.toDays(diff).toInt()
        return days + 1 // inklusif
    }
}
