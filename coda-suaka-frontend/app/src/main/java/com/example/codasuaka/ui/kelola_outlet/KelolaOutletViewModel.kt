package com.example.codasuaka.ui.kelola_outlet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Data class untuk satu outlet.
 */
data class Outlet(
    val id: Int = 0,
    val namaOutlet: String = "",
    val alamatOutlet: String = "",
    val jumlahKaryawan: Int = 0
)

/**
 * Mode dialog yang sedang aktif.
 */
sealed class DialogMode {
    /** Tidak ada dialog */
    data object Closed : DialogMode()
    /** Dialog form tambah outlet baru */
    data object Tambah : DialogMode()
    /** Dialog detail outlet yang sudah ada */
    data class Detail(val outlet: Outlet) : DialogMode()
}

/**
 * State halaman Kelola Outlet.
 */
data class KelolaOutletUiState(
    val outlets: List<Outlet> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val dialogMode: DialogMode = DialogMode.Closed,
    // Form fields
    val formNamaOutlet: String = "",
    val formAlamatOutlet: String = ""
)

/**
 * ViewModel untuk halaman Kelola Outlet.
 * TODO: Integrasi dengan API backend GET/POST/DELETE /api/outlet
 */
class KelolaOutletViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(KelolaOutletUiState())
    val uiState: StateFlow<KelolaOutletUiState> = _uiState

    private var nextId = 1

    init {
        loadOutlets()
    }

    /**
     * Memuat daftar outlet (dummy).
     * TODO: Panggil API GET /api/outlet
     */
    private fun loadOutlets() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            delay(600)

            val dummyOutlets = listOf(
                Outlet(id = nextId++, namaOutlet = "Outlet Pusat",
                    alamatOutlet = "Jl. Merdeka No. 123, Jakarta", jumlahKaryawan = 12),
                Outlet(id = nextId++, namaOutlet = "Outlet Cabang",
                    alamatOutlet = "Jl. Sudirman No. 45, Bandung", jumlahKaryawan = 8)
            )

            _uiState.value = _uiState.value.copy(outlets = dummyOutlets, isLoading = false)
        }
    }

    // ─── Dialog ──────────────────────────────────────────────

    fun openDialogTambah() {
        _uiState.value = _uiState.value.copy(
            dialogMode = DialogMode.Tambah,
            formNamaOutlet = "",
            formAlamatOutlet = "",
            errorMessage = null,
            successMessage = null
        )
    }

    fun openDialogDetail(outlet: Outlet) {
        _uiState.value = _uiState.value.copy(
            dialogMode = DialogMode.Detail(outlet),
            errorMessage = null,
            successMessage = null
        )
    }

    fun closeDialog() {
        _uiState.value = _uiState.value.copy(
            dialogMode = DialogMode.Closed,
            formNamaOutlet = "",
            formAlamatOutlet = "",
            errorMessage = null
        )
    }

    // ─── Form Input ──────────────────────────────────────────

    fun onFormNamaOutletChange(value: String) {
        _uiState.value = _uiState.value.copy(formNamaOutlet = value, errorMessage = null)
    }

    fun onFormAlamatOutletChange(value: String) {
        _uiState.value = _uiState.value.copy(formAlamatOutlet = value, errorMessage = null)
    }

    /**
     * Menyimpan outlet baru dari dialog.
     * TODO: Panggil API POST /api/outlet
     */
    fun simpanOutlet() {
        val state = _uiState.value
        if (state.formNamaOutlet.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Nama outlet harus diisi.")
            return
        }
        if (state.formAlamatOutlet.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Alamat outlet harus diisi.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)
            delay(500)

            val newOutlet = Outlet(
                id = nextId++,
                namaOutlet = state.formNamaOutlet.trim(),
                alamatOutlet = state.formAlamatOutlet.trim(),
                jumlahKaryawan = 0
            )

            _uiState.value = _uiState.value.copy(
                outlets = _uiState.value.outlets + newOutlet,
                isSaving = false,
                dialogMode = DialogMode.Closed,
                formNamaOutlet = "",
                formAlamatOutlet = "",
                successMessage = "Outlet \"${newOutlet.namaOutlet}\" berhasil ditambahkan."
            )
        }
    }

    /**
     * Menghapus outlet.
     * TODO: Panggil API DELETE /api/outlet/{id}
     */
    fun hapusOutlet(id: Int) {
        viewModelScope.launch {
            delay(300)
            _uiState.value = _uiState.value.copy(
                outlets = _uiState.value.outlets.filter { it.id != id },
                dialogMode = DialogMode.Closed,
                successMessage = "Outlet berhasil dihapus."
            )
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }
}
