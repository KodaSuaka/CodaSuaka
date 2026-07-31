package com.example.codasuaka.ui.screen.riwayat_nota

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.codasuaka.data.remote.dto.NotaDto
import com.example.codasuaka.data.remote.dto.OutletDto
import com.example.codasuaka.domain.repository.KasirRepository
import com.example.codasuaka.domain.repository.OutletRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

/** Filter tipe nota. */
enum class NotaTipeFilter(val label: String, val value: String?) {
    SEMUA("Semua", null),
    PEMBELIAN("Pembelian", "pembelian"),
    PENJUALAN("Penjualan", "penjualan")
}

data class RiwayatNotaUiState(
    val notaList: List<NotaDto> = emptyList(),
    val outletList: List<OutletDto> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val loadError: String? = null,

    // Filter
    val filterTipe: NotaTipeFilter = NotaTipeFilter.SEMUA,
    val filterOutletId: Int? = null,
    val filterStartDate: String = LocalDate.now().withDayOfMonth(1).toString(),
    val filterEndDate: String = LocalDate.now().toString(),

    // Pagination
    val currentPage: Int = 1,
    val lastPage: Int = 1,

    // Delete
    val isDeleting: Boolean = false,
    val deleteError: String? = null,
    val deleteSuccess: String? = null
)

class RiwayatNotaViewModel(
    private val kasirRepository: KasirRepository,
    private val outletRepository: OutletRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RiwayatNotaUiState())
    val uiState: StateFlow<RiwayatNotaUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadError = null) }
            try {
                coroutineScope {
                    val notaDeferred = async {
                        kasirRepository.getNotaList(
                            tipe = _uiState.value.filterTipe.value,
                            status = null,
                            page = 1,
                            outletId = _uiState.value.filterOutletId,
                            startDate = _uiState.value.filterStartDate,
                            endDate = _uiState.value.filterEndDate,
                            perPage = 50
                        )
                    }
                    val outletDeferred = async { outletRepository.getOutlets() }

                    val notaResult = notaDeferred.await()
                    val outletResult = outletDeferred.await()

                    _uiState.update { state ->
                        state.copy(
                            notaList = notaResult.getOrNull()?.first ?: emptyList(),
                            currentPage = notaResult.getOrNull()?.second?.currentPage ?: 1,
                            lastPage = notaResult.getOrNull()?.second?.lastPage ?: 1,
                            outletList = outletResult.getOrNull() ?: emptyList(),
                            isLoading = false,
                            loadError = notaResult.exceptionOrNull()?.message
                        )
                    }
                }
            } catch (_: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun loadNota(page: Int = 1) {
        viewModelScope.launch {
            if (page == 1) {
                _uiState.update { it.copy(isLoading = true, loadError = null) }
            } else {
                _uiState.update { it.copy(isLoadingMore = true) }
            }

            kasirRepository.getNotaList(
                tipe = _uiState.value.filterTipe.value,
                status = null,
                page = page,
                outletId = _uiState.value.filterOutletId,
                startDate = _uiState.value.filterStartDate,
                endDate = _uiState.value.filterEndDate,
                perPage = 50
            ).onSuccess { (list, meta) ->
                _uiState.update { state ->
                    state.copy(
                        notaList = if (page == 1) list else state.notaList + list,
                        isLoading = false,
                        isLoadingMore = false,
                        currentPage = meta?.currentPage ?: 1,
                        lastPage = meta?.lastPage ?: 1
                    )
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        loadError = e.message ?: "Gagal memuat nota"
                    )
                }
            }
        }
    }

    // ─── Filter ────────────────────────────────────────────

    fun setFilterTipe(tipe: NotaTipeFilter) {
        _uiState.update { it.copy(filterTipe = tipe) }
        loadNota(page = 1)
    }

    fun setFilterOutlet(outletId: Int?) {
        _uiState.update { it.copy(filterOutletId = outletId) }
        loadNota(page = 1)
    }

    fun setFilterDateRange(startDate: String, endDate: String) {
        _uiState.update { it.copy(filterStartDate = startDate, filterEndDate = endDate) }
        loadNota(page = 1)
    }

    fun loadMore() {
        val state = _uiState.value
        if (state.isLoadingMore || state.currentPage >= state.lastPage) return
        loadNota(page = state.currentPage + 1)
    }

    // ─── Delete ────────────────────────────────────────────

    fun deleteNota(id: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, deleteError = null) }
            kasirRepository.deleteNota(id)
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(
                            isDeleting = false,
                            deleteSuccess = "Nota berhasil dihapus",
                            notaList = state.notaList.filterNot { it.id == id }
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isDeleting = false, deleteError = e.message ?: "Gagal menghapus nota") }
                }
        }
    }

    // ─── Cleanup ───────────────────────────────────────────

    fun clearError() {
        _uiState.update { it.copy(loadError = null, deleteError = null) }
    }

    fun clearDeleteSuccess() {
        _uiState.update { it.copy(deleteSuccess = null) }
    }
}
