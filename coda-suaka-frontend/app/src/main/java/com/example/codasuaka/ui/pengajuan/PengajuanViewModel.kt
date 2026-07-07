package com.example.codasuaka.ui.pengajuan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.codasuaka.data.remote.dto.CreatePengajuanRequest
import com.example.codasuaka.data.remote.dto.PengajuanDto
import com.example.codasuaka.domain.repository.PengajuanRepository
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
 */
class PengajuanViewModel(
    private val pengajuanRepository: PengajuanRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PengajuanUiState())
    val uiState: StateFlow<PengajuanUiState> = _uiState

    private val dateFormatter = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
    private val apiDateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale("id", "ID"))

    init {
        loadRiwayat()
    }

    /**
     * Memuat riwayat pengajuan dari API.
     */
    private fun loadRiwayat() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val result = pengajuanRepository.getPengajuans()
            result.onSuccess { dtos ->
                _uiState.value = _uiState.value.copy(
                    riwayatPengajuan = dtos.reversed().map { it.toPengajuan() },
                    isLoading = false
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = it.message ?: "Gagal memuat riwayat pengajuan"
                )
            }
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
     * Mengajukan pengajuan cuti/izin baru ke API.
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

            val tanggalMulaiApi = apiDateFormatter.format(Date(state.tanggalMulaiMillis))
            val tanggalSelesaiApi = apiDateFormatter.format(Date(state.tanggalSelesaiMillis))

            val request = CreatePengajuanRequest(
                jenis = state.selectedJenis.name.lowercase(),
                tanggalMulai = tanggalMulaiApi,
                tanggalSelesai = tanggalSelesaiApi,
                keterangan = state.keterangan.trim()
            )

            val result = pengajuanRepository.createPengajuan(request)
            result.onSuccess { dto ->
                val pengajuanBaru = dto.toPengajuan()
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
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = it.message ?: "Gagal mengajukan pengajuan"
                )
            }
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

    companion object {
        fun PengajuanDto.toPengajuan(): Pengajuan {
            val jenis = when (this.jenis.lowercase()) {
                "cuti_tahunan" -> JenisPengajuan.CUTI_TAHUNAN
                "izin_sakit" -> JenisPengajuan.IZIN_SAKIT
                "mendadak" -> JenisPengajuan.MENDADAK
                else -> JenisPengajuan.CUTI_TAHUNAN
            }
            return Pengajuan(
                id = this.id,
                jenis = jenis,
                tanggalMulai = this.tanggalMulai ?: "",
                tanggalSelesai = this.tanggalSelesai ?: "",
                jumlahHari = 0, // akan dihitung jika perlu
                keterangan = this.keterangan ?: "",
                status = this.status,
                createdAt = this.createdAt ?: ""
            )
        }
    }
}
