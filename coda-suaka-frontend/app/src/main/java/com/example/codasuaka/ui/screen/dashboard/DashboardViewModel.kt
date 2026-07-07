package com.example.codasuaka.ui.screen.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.codasuaka.domain.repository.DashboardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * State untuk halaman Dashboard.
 */
data class DashboardUiState(
    val outletName: String = "",
    val omsetTotal: Long = 0,
    val isLoading: Boolean = false,
    val isDrawerOpen: Boolean = false,
    val errorMessage: String? = null,
    val selectedBottomNav: Int = 0 // 0 = Dashboard, 1 = Tugas Tim, 2 = Pesan, 3 = Divisi
)

/**
 * ViewModel untuk Dashboard.
 * Mengelola state omset, outlet, navigasi drawer & bottom nav.
 */
class DashboardViewModel(
    private val dashboardRepository: DashboardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState

    init {
        loadDashboardData()
    }

    /**
     * Memuat data awal dashboard (outlet & omset) dari API.
     */
    private fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            dashboardRepository.getDashboard()
                .onSuccess { data ->
                    _uiState.value = _uiState.value.copy(
                        outletName = "Outlet Pusat", // default, bisa diganti dari data user
                        omsetTotal = 0,
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

    /**
     * Memuat ulang data omset berdasarkan filter tanggal.
     * @param startDate Tanggal mulai (format: yyyy-MM-dd)
     * @param endDate   Tanggal akhir (format: yyyy-MM-dd)
     */
    fun loadOmset(startDate: String, endDate: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            dashboardRepository.getOmset(startDate, endDate)
                .onSuccess { data ->
                    _uiState.value = _uiState.value.copy(
                        omsetTotal = data.totalOmset,
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

    // ─── Drawer ──────────────────────────────────────────────

    fun openDrawer() {
        _uiState.value = _uiState.value.copy(isDrawerOpen = true)
    }

    fun closeDrawer() {
        _uiState.value = _uiState.value.copy(isDrawerOpen = false)
    }

    fun toggleDrawer() {
        _uiState.value = _uiState.value.copy(
            isDrawerOpen = !_uiState.value.isDrawerOpen
        )
    }

    // ─── Bottom Navigation ───────────────────────────────────

    fun onBottomNavSelected(index: Int) {
        _uiState.value = _uiState.value.copy(selectedBottomNav = index)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
