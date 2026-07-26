package com.example.codasuaka.ui.screen.penugasan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.codasuaka.data.local.TokenManager
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
    val isProcessing: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val showCreateDialog: Boolean = false,
    // Permission
    val canManagePenugasan: Boolean = false,
    // Current user info
    val currentUserId: Int? = null,
    val currentKaryawanId: String? = null,
    val userRole: String? = null,
    // Detail view
    val selectedPenugasan: PenugasanDto? = null,
    val showDetail: Boolean = false,
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
    private val karyawanRepository: KaryawanRepository,
    private val tokenManager: TokenManager,
    private val apiService: com.example.codasuaka.data.remote.ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(PenugasanUiState())
    val uiState: StateFlow<PenugasanUiState> = _uiState.asStateFlow()

    init {
        loadUserRole()
        loadData()
    }

    private fun loadUserRole() {
        viewModelScope.launch {
            val role = tokenManager.getUserRole()
            val canManage = role in listOf("Owner", "Manager")
            try {
                val userResponse = apiService.getUser()
                if (userResponse.isSuccessful) {
                    val userData = userResponse.body()?.data
                    _uiState.update {
                        it.copy(
                            canManagePenugasan = canManage,
                            currentUserId = userData?.id,
                            currentKaryawanId = userData?.profilKaryawan?.id,
                            userRole = role
                        )
                    }
                    return@launch
                }
            } catch (_: Exception) {}
            _uiState.update { it.copy(canManagePenugasan = canManage, userRole = role) }
        }
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

    /**
     * Cek apakah user adalah karyawan yang ditugasi pada tugas tertentu.
     */
    /**
     * Bug #3: Manager/Owner bisa accept semua tugas via backend policy.
     * Frontend juga harus izinkan tombol "Terima Tugas" untuk mereka.
     */
    fun isAssignedTo(penugasan: PenugasanDto): Boolean {
        val state = _uiState.value
        // Manager/Owner dengan manage:penugasan boleh accept semua tugas
        if (state.canManagePenugasan) {
            return true
        }
        return state.currentKaryawanId != null && penugasan.penanggungJawabId == state.currentKaryawanId
    }

    /**
     * Karyawan menerima tugas (belum → proses).
     */
    fun acceptPenugasan(id: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, errorMessage = null) }
            try {
                penugasanRepository.acceptPenugasan(id).getOrThrow()
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        successMessage = "Tugas berhasil diterima"
                    )
                }
                loadData()
                refreshSelectedPenugasan(id)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        errorMessage = e.message ?: "Gagal menerima tugas"
                    )
                }
            }
        }
    }

    /**
     * Karyawan menyelesaikan tugas (proses → selesai).
     */
    fun completePenugasan(id: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, errorMessage = null) }
            try {
                penugasanRepository.completePenugasan(id).getOrThrow()
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        successMessage = "Tugas berhasil diselesaikan"
                    )
                }
                loadData()
                refreshSelectedPenugasan(id)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        errorMessage = e.message ?: "Gagal menyelesaikan tugas"
                    )
                }
            }
        }
    }

    /**
     * Tampilkan detail penugasan.
     */
    fun showPenugasanDetail(penugasan: PenugasanDto) {
        _uiState.update { it.copy(selectedPenugasan = penugasan, showDetail = true) }
    }

    /**
     * Sembunyikan detail penugasan.
     */
    fun hidePenugasanDetail() {
        _uiState.update { it.copy(selectedPenugasan = null, showDetail = false) }
    }

    /**
     * Refresh selected penugasan setelah accept/complete.
     */
    fun refreshSelectedPenugasan(id: Int) {
        viewModelScope.launch {
            try {
                val updated = penugasanRepository.getPenugasan(id).getOrNull()
                if (updated != null) {
                    _uiState.update { it.copy(selectedPenugasan = updated) }
                }
            } catch (_: Exception) {}
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun clearSuccess() {
        _uiState.update { it.copy(successMessage = null) }
    }
}
