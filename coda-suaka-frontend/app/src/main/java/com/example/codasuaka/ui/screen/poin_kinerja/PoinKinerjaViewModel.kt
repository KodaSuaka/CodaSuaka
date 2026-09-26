package com.example.codasuaka.ui.screen.poin_kinerja

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.codasuaka.data.remote.dto.DetailUrgency
import com.example.codasuaka.data.remote.dto.PoinKinerjaData
import com.example.codasuaka.domain.repository.DashboardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class PoinKinerjaUiState(
    val totalPoin: Int = 0,
    val totalTugasSelesai: Int = 0,
    val rataRataPoin: Double = 0.0,
    val detailUrgency: List<DetailUrgency> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class PoinKinerjaViewModel(
    private val dashboardRepository: DashboardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PoinKinerjaUiState())
    val uiState: StateFlow<PoinKinerjaUiState> = _uiState

    init {
        loadPoinKinerja()
    }

    fun loadPoinKinerja() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            dashboardRepository.getPoinKinerja()
                .onSuccess { data ->
                    _uiState.value = _uiState.value.copy(
                        totalPoin = data.totalPoin,
                        totalTugasSelesai = data.totalTugasSelesai,
                        rataRataPoin = data.rataRataPoin,
                        detailUrgency = data.detailUrgency,
                        isLoading = false
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Gagal memuat data poin kinerja"
                    )
                }
        }
    }
}
