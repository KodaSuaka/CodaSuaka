package com.example.codasuaka.ui.screen.approval_keuangan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.codasuaka.data.remote.dto.ApprovalLogDto
import com.example.codasuaka.domain.repository.KeuanganRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ApprovalKeuanganUiState(
    val approvals: List<ApprovalLogDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedTab: Int = 0, // 0 = Pending, 1 = Riwayat
    val selectedApprovalLog: ApprovalLogDto? = null,
    val showDetailSheet: Boolean = false,
    val catatanPenolakan: String = "",
    val showTolakDialog: Boolean = false,
    val isProcessing: Boolean = false,
    val successMessage: String? = null,
)

class ApprovalKeuanganViewModel(
    private val keuanganRepository: KeuanganRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ApprovalKeuanganUiState())
    val uiState: StateFlow<ApprovalKeuanganUiState> = _uiState.asStateFlow()

    init {
        loadPendingApprovals()
    }

    fun loadPendingApprovals() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            keuanganRepository.getApprovalPending()
                .onSuccess { approvals ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        approvals = approvals
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Gagal memuat data approval"
                    )
                }
        }
    }

    fun loadRiwayatApproval() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            keuanganRepository.getApprovalRiwayat()
                .onSuccess { approvals ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        approvals = approvals
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Gagal memuat riwayat approval"
                    )
                }
        }
    }

    fun setSelectedTab(tab: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = tab, approvals = emptyList())
        if (tab == 0) {
            loadPendingApprovals()
        } else {
            loadRiwayatApproval()
        }
    }

    fun showDetail(approvalLog: ApprovalLogDto) {
        _uiState.value = _uiState.value.copy(
            selectedApprovalLog = approvalLog,
            showDetailSheet = true
        )
    }

    fun hideDetail() {
        _uiState.value = _uiState.value.copy(
            showDetailSheet = false,
            selectedApprovalLog = null
        )
    }

    fun showTolakDialog() {
        _uiState.value = _uiState.value.copy(
            showTolakDialog = true,
            catatanPenolakan = ""
        )
    }

    fun hideTolakDialog() {
        _uiState.value = _uiState.value.copy(
            showTolakDialog = false,
            catatanPenolakan = ""
        )
    }

    fun setCatatanPenolakan(catatan: String) {
        _uiState.value = _uiState.value.copy(catatanPenolakan = catatan)
    }

    fun setujuiApproval() {
        val log = _uiState.value.selectedApprovalLog ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true, error = null)
            keuanganRepository.setujuiApproval(log.id)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isProcessing = false,
                        showDetailSheet = false,
                        selectedApprovalLog = null,
                        successMessage = "Transaksi berhasil disetujui"
                    )
                    // Reload data
                    if (_uiState.value.selectedTab == 0) {
                        loadPendingApprovals()
                    } else {
                        loadRiwayatApproval()
                    }
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isProcessing = false,
                        error = e.message ?: "Gagal menyetujui transaksi"
                    )
                }
        }
    }

    fun tolakApproval() {
        val log = _uiState.value.selectedApprovalLog ?: return
        val catatan = _uiState.value.catatanPenolakan

        if (catatan.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Catatan penolakan wajib diisi")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true, error = null)
            keuanganRepository.tolakApproval(log.id, catatan)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isProcessing = false,
                        showDetailSheet = false,
                        showTolakDialog = false,
                        selectedApprovalLog = null,
                        catatanPenolakan = "",
                        successMessage = "Transaksi berhasil ditolak"
                    )
                    // Reload data
                    if (_uiState.value.selectedTab == 0) {
                        loadPendingApprovals()
                    } else {
                        loadRiwayatApproval()
                    }
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isProcessing = false,
                        error = e.message ?: "Gagal menolak transaksi"
                    )
                }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }
}
