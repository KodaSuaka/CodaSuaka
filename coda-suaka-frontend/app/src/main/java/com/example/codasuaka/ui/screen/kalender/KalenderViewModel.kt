package com.example.codasuaka.ui.screen.kalender

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
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
class KalenderViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(KalenderUiState())
    val uiState: StateFlow<KalenderUiState> = _uiState

    private var nextId = 1

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
     * Memuat daftar event untuk bulan aktif (dummy).
     * TODO: Panggil API GET /api/jadwal?bulan=&tahun=&outlet_id=
     */
    private fun loadEvents() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            delay(400)

            val month = _uiState.value.currentMonth
            val dummyEvents = listOf(
                KalenderEvent(
                    id = nextId++,
                    namaEvent = "Libur Nasional — Idul Adha",
                    tanggal = month.atDay(7),
                    kategori = EventCategory.LIBUR
                ),
                KalenderEvent(
                    id = nextId++,
                    namaEvent = "Meeting Evaluasi Bulanan",
                    tanggal = month.atDay(10),
                    kategori = EventCategory.TUGAS
                ),
                KalenderEvent(
                    id = nextId++,
                    namaEvent = "Acara Team Building",
                    tanggal = month.atDay(15),
                    kategori = EventCategory.EVENT
                ),
                KalenderEvent(
                    id = nextId++,
                    namaEvent = "Cuti Bersama",
                    tanggal = month.atDay(20),
                    kategori = EventCategory.LIBUR
                ),
                KalenderEvent(
                    id = nextId++,
                    namaEvent = "Deadline Laporan Keuangan",
                    tanggal = month.atDay(25),
                    kategori = EventCategory.TUGAS
                ),
                KalenderEvent(
                    id = nextId++,
                    namaEvent = "Workshop Kompetensi Karyawan",
                    tanggal = month.atDay(28),
                    kategori = EventCategory.EVENT
                )
            )

            _uiState.value = _uiState.value.copy(
                events = dummyEvents,
                isLoading = false
            )
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
     * Menyimpan event baru dari dialog.
     * TODO: Panggil API POST /api/jadwal
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
            delay(500)

            val newEvent = KalenderEvent(
                id = nextId++,
                namaEvent = state.formNamaEvent.trim(),
                tanggal = tanggal,
                kategori = state.formKategori
            )

            _uiState.value = _uiState.value.copy(
                events = _uiState.value.events + newEvent,
                isSaving = false,
                dialogMode = KalenderDialogMode.Closed,
                formNamaEvent = "",
                formTanggal = "",
                formKategori = EventCategory.EVENT,
                successMessage = "Event \"${newEvent.namaEvent}\" berhasil ditambahkan."
            )
        }
    }

    /**
     * Menghapus event.
     * TODO: Panggil API DELETE /api/jadwal/{id}
     */
    fun hapusEvent(id: Int) {
        viewModelScope.launch {
            delay(300)
            _uiState.value = _uiState.value.copy(
                events = _uiState.value.events.filter { it.id != id },
                dialogMode = KalenderDialogMode.Closed,
                successMessage = "Event berhasil dihapus."
            )
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }
}
