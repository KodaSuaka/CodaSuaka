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
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
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
    val isEditing: Boolean = false,
    val editingId: Int? = null,
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

    // Dimuat via OnResumeEffect di PenugasanScreen (bukan init{}) supaya
    // daftar tugas ikut refresh saat layar ini kembali terlihat — termasuk
    // setelah pergantian hari selagi app tetap terbuka, bukan cuma sekali
    // per proses/ViewModel.
    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                coroutineScope {
                    // Fetch user info/role and data in parallel
                    val userDeferred = async { apiService.getUser() }
                    val penugasansDeferred = async { penugasanRepository.getPenugasans(status = _uiState.value.filterStatus) }
                    val divisisDeferred = async { divisiRepository.getDivisis() }
                    val karyawansDeferred = async { karyawanRepository.getKaryawans() }
                    val roleDeferred = async { tokenManager.getUserRole() }

                    val userResponse = userDeferred.await()
                    val penugasans = penugasansDeferred.await().getOrThrow()
                    val divisis = divisisDeferred.await().getOrThrow()
                    val karyawans = karyawansDeferred.await().getOrThrow()
                    val role = roleDeferred.await()

                    val canManage = role in listOf("Owner", "Manager")
                    var currentUserId: Int? = null
                    var currentKaryawanId: String? = null

                    if (userResponse.isSuccessful) {
                        val userData = userResponse.body()?.data
                        currentUserId = userData?.id
                        currentKaryawanId = userData?.profilKaryawan?.id
                    }

                    _uiState.update {
                        it.copy(
                            penugasans = penugasans,
                            divisis = divisis,
                            karyawans = karyawans,
                            isLoading = false,
                            canManagePenugasan = canManage,
                            userRole = role,
                            currentUserId = currentUserId,
                            currentKaryawanId = currentKaryawanId
                        )
                    }
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
                isEditing = false,
                editingId = null,
                errorMessage = null,
                successMessage = null
            )
        }
    }

    fun showEditDialog(penugasan: PenugasanDto) {
        _uiState.update {
            it.copy(
                showCreateDialog = true,
                isEditing = true,
                editingId = penugasan.id,
                formJudul = penugasan.judul,
                formDeskripsi = penugasan.deskripsi ?: "",
                formPenanggungJawabId = penugasan.penanggungJawabId ?: "",
                formDivisiId = penugasan.divisiId,
                formTenggat = penugasan.tenggat?.take(10) ?: "",
                formUrgency = penugasan.urgency ?: "sedang",
                errorMessage = null,
                successMessage = null
            )
        }
    }

    /**
     * Daftar template yang bisa dipakai sebagai titik awal saat pemilik
     * membuat tugas khusus (sudah termuat dari [loadData], tanpa panggilan API baru).
     */
    val templates: List<PenugasanDto>
        get() = _uiState.value.penugasans.filter { it.isTemplate == true }

    /**
     * Isi form tugas dari sebuah template — pemilik masih bisa mengubah
     * semua field (termasuk judul/deskripsi) sebelum menyimpan sebagai
     * tugas khusus, jadi template hanya jadi titik awal, bukan nilai tetap.
     */
    fun applyTemplate(template: PenugasanDto) {
        _uiState.update {
            it.copy(
                formJudul = template.judul,
                formDeskripsi = template.deskripsi ?: "",
                formUrgency = template.urgency ?: "sedang"
            )
        }
    }

    fun dismissCreateDialog() {
        _uiState.update { it.copy(showCreateDialog = false, isEditing = false, editingId = null) }
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

    fun createOrUpdatePenugasan() {
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
                if (state.isEditing && state.editingId != null) {
                    // Update
                    val request = com.example.codasuaka.data.remote.dto.UpdatePenugasanRequest(
                        judul = state.formJudul,
                        deskripsi = state.formDeskripsi.ifBlank { null },
                        penanggungJawabId = state.formPenanggungJawabId,
                        divisiId = state.formDivisiId,
                        tenggat = state.formTenggat.ifBlank { null },
                        status = null
                    )
                    penugasanRepository.updatePenugasan(state.editingId, request).getOrThrow()
                    _uiState.update {
                        it.copy(
                            isCreating = false,
                            showCreateDialog = false,
                            isEditing = false,
                            editingId = null,
                            successMessage = "Tugas berhasil diperbarui"
                        )
                    }
                } else {
                    // Create
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
                }
                loadData()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isCreating = false,
                        errorMessage = e.message ?: "Gagal memproses tugas"
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
     * Pemilik/manager memvalidasi tugas yang menunggu_validasi.
     */
    fun validasiPenugasan(id: Int, disetujui: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, errorMessage = null) }
            try {
                penugasanRepository.validasiPenugasan(id, disetujui).getOrThrow()
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        successMessage = if (disetujui) "Tugas disetujui & selesai" else "Tugas dikembalikan ke karyawan"
                    )
                }
                loadData()
                refreshSelectedPenugasan(id)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        errorMessage = e.message ?: "Gagal memvalidasi tugas"
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
