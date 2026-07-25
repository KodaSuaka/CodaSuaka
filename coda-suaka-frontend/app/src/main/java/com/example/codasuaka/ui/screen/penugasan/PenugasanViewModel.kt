package com.example.codasuaka.ui.screen.penugasan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.codasuaka.data.remote.dto.CreatePenugasanRequest
import com.example.codasuaka.data.remote.dto.DivisiDto
import com.example.codasuaka.data.remote.dto.KaryawanDto
import com.example.codasuaka.data.remote.dto.PenugasanDto
import com.example.codasuaka.domain.repository.DivisiRepository
import com.example.codasuaka.domain.repository.KaryawanRepository
import com.example.codasuaka.domain.repository.PenugasanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PenugasanUiState(
    val penugasans: List<PenugasanDto> = emptyList(),
    val divisis: List<DivisiDto> = emptyList(),
    val karyawans: List<KaryawanDto> = emptyList(),
    val isLoading: Boolean = false,
    val isCreating: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val showCreateDialog: Boolean = false,
    // Filter
    val filterStatus: String? = null,
    // Form fields
    val formJudul: String = "",
    val formDeskripsi: String = "",
    val formPenanggungJawabId: String = "",
    val formDivisiId: Int? = null,
    val formTenggat: String = "",
    val formUrgency: String = "sedang"
)

class PenugasanViewModel(
    private val penugasanRepository: PenugasanRepository,
    private val divisiRepository: DivisiRepository,
    private val karyawanRepository: KaryawanRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PenugasanUiState())
    val uiState: StateFlow<PenugasanUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val penugasans = penugasanRepository.getPenugasans(
                    status = _uiState.value.filterStatus
                ).getOrThrow()
                val divisis = divisiRepository.getDivisis().getOrThrow()
                val karyawans = karyawanRepository.getKaryawans().getOrThrow()
                _uiState.update {
                    it.copy(
                        penugasans = penugasans,
                        divisis = divisis,
                        karyawans = karyawans,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Gagal memuat data"
                    )
                }
            }
        }
    }

    fun filterByStatus(status: String?) {
        _uiState.update { it.copy(filterStatus = status) }
        loadData()
    }

    fun showCreateDialog() {
        _uiState.update {
            it.copy(
                showCreateDialog = true,
                formJudul = "",
                formDeskripsi = "",
                formPenanggungJawabId = "",
                formDivisiId = null,
                formTenggat = "",
                formUrgency = "sedang",
                errorMessage = null,
                successMessage = null
            )
        }
    }

    fun dismissCreateDialog() {
        _uiState.update { it.copy(showCreateDialog = false) }
    }

    fun updateFormJudul(value: String) {
        _uiState.update { it.copy(formJudul = value) }
    }

    fun updateFormDeskripsi(value: String) {
        _uiState.update { it.copy(formDeskripsi = value) }
    }

    fun updateFormPenanggungJawabId(value: String) {
        _uiState.update { it.copy(formPenanggungJawabId = value) }
    }

    fun updateFormDivisiId(value: Int?) {
        _uiState.update { it.copy(formDivisiId = value) }
    }

    fun updateFormTenggat(value: String) {
        _uiState.update { it.copy(formTenggat = value) }
    }

    fun updateFormUrgency(value: String) {
        _uiState.update { it.copy(formUrgency = value) }
    }

    fun createPenugasan() {
        val state = _uiState.value
        if (state.formJudul.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Judul tugas harus diisi") }
            return
        }
        if (state.formPenanggungJawabId.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Penanggung jawab harus dipilih") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isCreating = true, errorMessage = null) }
            try {
                penugasanRepository.createPenugasan(
                    CreatePenugasanRequest(
                        judul = state.formJudul,
                        deskripsi = state.formDeskripsi.ifBlank { null },
                        penanggungJawabId = state.formPenanggungJawabId,
                        divisiId = state.formDivisiId,
                        tenggat = state.formTenggat.ifBlank { null },
                        status = null
                    )
                ).getOrThrow()
                _uiState.update {
                    it.copy(
                        isCreating = false,
                        showCreateDialog = false,
                        successMessage = "Tugas berhasil dibuat"
                    )
                }
                loadData()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isCreating = false,
                        errorMessage = e.message ?: "Gagal membuat tugas"
                    )
                }
            }
        }
    }

    fun deletePenugasan(id: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                penugasanRepository.deletePenugasan(id).getOrThrow()
                _uiState.update { it.copy(successMessage = "Tugas berhasil dihapus") }
                loadData()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Gagal menghapus tugas"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun clearSuccess() {
        _uiState.update { it.copy(successMessage = null) }
    }
}
