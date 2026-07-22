package com.example.codasuaka.ui.screen.kalender

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.codasuaka.data.remote.dto.CreateJadwalRequest
import com.example.codasuaka.data.remote.dto.JadwalDto
import com.example.codasuaka.domain.repository.JadwalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

/**
 * Kategori event pada kalender.
 */
enum class EventCategory(val displayName: String) {
    LIBUR("Libur"),
    TUGAS("Tugas"),
    EVENT("Event")
}

/**
 * Data class untuk satu event jadwal.
 */
data class KalenderEvent(
    val id: Int = 0,
    val namaEvent: String = "",
    val tanggal: LocalDate = LocalDate.now(),
    val kategori: EventCategory = EventCategory.EVENT
)

/**
 * Mode dialog yang sedang aktif.
 */
sealed class KalenderDialogMode {
    /** Tidak ada dialog */
    data object Closed : KalenderDialogMode()
    /** Dialog form tambah event baru */
    data object Tambah : KalenderDialogMode()
    /** Dialog detail event yang sudah ada */
    data class Detail(val event: KalenderEvent) : KalenderDialogMode()
}

/**
 * State halaman Kalender.
 */
data class KalenderUiState(
    val currentMonth: YearMonth = YearMonth.now(),
    val events: List<KalenderEvent> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val dialogMode: KalenderDialogMode = KalenderDialogMode.Closed,
    // Form fields
    val formNamaEvent: String = "",
    val formTanggal: String = "",
    val formKategori: EventCategory = EventCategory.EVENT
)

/**
 * ViewModel untuk halaman Kalender / Jadwal.
 */
class KalenderViewModel(
    private val jadwalRepository: JadwalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(KalenderUiState())
    val uiState: StateFlow<KalenderUiState> = _uiState

    init {
        loadEvents()
    }

    // ─── Navigasi Bulan ──────────────────────────────────────

    fun nextMonth() {
        _uiState.value = _uiState.value.copy(
            currentMonth = _uiState.value.currentMonth.plusMonths(1)
        )
        loadEvents()
    }

    fun prevMonth() {
        _uiState.value = _uiState.value.copy(
            currentMonth = _uiState.value.currentMonth.minusMonths(1)
        )
        loadEvents()
    }

    /**
     * Mengubah tahun secara langsung.
     */
    fun selectYear(year: Int) {
        _uiState.value = _uiState.value.copy(
            currentMonth = _uiState.value.currentMonth.withYear(year)
        )
        loadEvents()
    }

    /**
     * Memuat daftar event untuk bulan aktif dari API.
     */
    private fun loadEvents() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val month = _uiState.value.currentMonth
            val result = jadwalRepository.getJadwals(
                bulan = month.monthValue,
                tahun = month.year
            )

            result.onSuccess { dtos ->
                _uiState.value = _uiState.value.copy(
                    events = dtos.map { it.toKalenderEvent() },
                    isLoading = false
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = it.message ?: "Gagal memuat jadwal"
                )
            }
        }
    }

    // ─── Dialog ──────────────────────────────────────────────

    fun openDialogTambah() {
        _uiState.value = _uiState.value.copy(
            dialogMode = KalenderDialogMode.Tambah,
            formNamaEvent = "",
            formTanggal = "",
            formKategori = EventCategory.EVENT,
            errorMessage = null,
            successMessage = null
        )
    }

    fun openDialogDetail(event: KalenderEvent) {
        _uiState.value = _uiState.value.copy(
            dialogMode = KalenderDialogMode.Detail(event),
            errorMessage = null,
            successMessage = null
        )
    }

    fun closeDialog() {
        _uiState.value = _uiState.value.copy(
            dialogMode = KalenderDialogMode.Closed,
            formNamaEvent = "",
            formTanggal = "",
            formKategori = EventCategory.EVENT,
            errorMessage = null
        )
    }

    // ─── Form Input ──────────────────────────────────────────

    fun onFormNamaEventChange(value: String) {
        _uiState.value = _uiState.value.copy(formNamaEvent = value, errorMessage = null)
    }

    fun onFormTanggalChange(value: String) {
        _uiState.value = _uiState.value.copy(formTanggal = value, errorMessage = null)
    }

    fun onFormKategoriChange(value: EventCategory) {
        _uiState.value = _uiState.value.copy(formKategori = value, errorMessage = null)
    }

    /**
     * Menyimpan event baru dari dialog ke API.
     */
    fun simpanEvent() {
        val state = _uiState.value
        if (state.formNamaEvent.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Nama event harus diisi.")
            return
        }
        if (state.formTanggal.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Tanggal harus diisi.")
            return
        }

        // Parse tanggal
        val tanggal = try {
            LocalDate.parse(state.formTanggal.trim())
        } catch (e: Exception) {
            _uiState.value = state.copy(errorMessage = "Format tanggal tidak valid. Gunakan yyyy-MM-dd.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)

            val request = CreateJadwalRequest(
                namaEvent = state.formNamaEvent.trim(),
                tanggal = tanggal.toString(),
                kategori = state.formKategori.name.lowercase()
            )

            val result = jadwalRepository.createJadwal(request)
            result.onSuccess { dto ->
                val newEvent = dto.toKalenderEvent()
                _uiState.value = _uiState.value.copy(
                    events = _uiState.value.events + newEvent,
                    isSaving = false,
                    dialogMode = KalenderDialogMode.Closed,
                    formNamaEvent = "",
                    formTanggal = "",
                    formKategori = EventCategory.EVENT,
                    successMessage = "Event \"${newEvent.namaEvent}\" berhasil ditambahkan."
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = it.message ?: "Gagal menyimpan event"
                )
            }
        }
    }

    /**
     * Menghapus event via API.
     */
    fun hapusEvent(id: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)

            val result = jadwalRepository.deleteJadwal(id)
            result.onSuccess {
                _uiState.value = _uiState.value.copy(
                    events = _uiState.value.events.filter { it.id != id },
                    isSaving = false,
                    dialogMode = KalenderDialogMode.Closed,
                    successMessage = "Event berhasil dihapus."
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = it.message ?: "Gagal menghapus event"
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }

    companion object {
        fun JadwalDto.toKalenderEvent(): KalenderEvent {
            val kategori = when (this.kategori.lowercase()) {
                "libur" -> EventCategory.LIBUR
                "tugas" -> EventCategory.TUGAS
                else -> EventCategory.EVENT
            }
            val date = try {
                LocalDate.parse(this.tanggal?.take(10))
            } catch (e: Exception) {
                LocalDate.now()
            }
            return KalenderEvent(
                id = this.id,
                namaEvent = this.namaEvent,
                tanggal = date,
                kategori = kategori
            )
        }
    }
}
