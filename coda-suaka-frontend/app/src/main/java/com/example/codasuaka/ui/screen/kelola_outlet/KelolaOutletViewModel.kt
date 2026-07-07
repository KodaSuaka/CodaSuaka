package com.example.codasuaka.ui.screen.kelola_outlet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.codasuaka.data.remote.dto.OutletDto
import com.example.codasuaka.data.remote.dto.OutletRequest
import com.example.codasuaka.domain.repository.OutletRepository
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
 * Terintegrasi dengan API backend.
 */
class KelolaOutletViewModel(
    private val outletRepository: OutletRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(KelolaOutletUiState())
    val uiState: StateFlow<KelolaOutletUiState> = _uiState

    init {
        loadOutlets()
    }

    /**
     * Memuat daftar outlet dari API.
     */
    private fun loadOutlets() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            outletRepository.getOutlets()
                .onSuccess { outletDtos ->
                    val outlets = outletDtos.map { it.toOutlet() }
                    _uiState.value = _uiState.value.copy(
                        outlets = outlets,
                        isLoading = false
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message
                    )
                }
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
     * Menyimpan outlet baru dari dialog via API.
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

            outletRepository.createOutlet(
                OutletRequest(
                    namaOutlet = state.formNamaOutlet.trim(),
                    alamatOutlet = state.formAlamatOutlet.trim()
                )
            )
                .onSuccess { outletDto ->
                    val newOutlet = outletDto.toOutlet()
                    _uiState.value = _uiState.value.copy(
                        outlets = _uiState.value.outlets + newOutlet,
                        isSaving = false,
                        dialogMode = DialogMode.Closed,
                        formNamaOutlet = "",
                        formAlamatOutlet = "",
                        successMessage = "Outlet \"${newOutlet.namaOutlet}\" berhasil ditambahkan."
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        errorMessage = error.message
                    )
                }
        }
    }

    /**
     * Menghapus outlet via API.
     */
    fun hapusOutlet(id: Int) {
        viewModelScope.launch {
            outletRepository.deleteOutlet(id)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        outlets = _uiState.value.outlets.filter { it.id != id },
                        dialogMode = DialogMode.Closed,
                        successMessage = "Outlet berhasil dihapus."
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = error.message
                    )
                }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }

    // ─── Extension ───────────────────────────────────────────

    companion object {
        fun OutletDto.toOutlet() = Outlet(
            id = this.id,
            namaOutlet = this.namaOutlet,
            alamatOutlet = this.alamatOutlet ?: "",
            jumlahKaryawan = 0
        )
    }
}
