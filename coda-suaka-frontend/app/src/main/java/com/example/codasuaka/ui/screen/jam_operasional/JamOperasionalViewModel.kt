package com.example.codasuaka.ui.screen.jam_operasional

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.codasuaka.data.remote.ApiService
import com.example.codasuaka.data.remote.dto.UpdateInstansiRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * State untuk halaman Jam Operasional.
 */
data class JamOperasionalUiState(
    val namaInstansi: String = "",
    val jamBuka: String = "08:00",
    val jamTutup: String = "17:00",
    val hariOperasional: Set<Int> = setOf(2, 3, 4, 5, 6, 7), // Senin-Sabtu
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

/**
 * ViewModel untuk halaman Jam Operasional.
 * Mengelola data jam operasional instansi (jam_buka, jam_tutup, hari_operasional).
 */
class JamOperasionalViewModel(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(JamOperasionalUiState())
    val uiState: StateFlow<JamOperasionalUiState> = _uiState

    init {
        loadJamOperasional()
    }

    /**
     * Muat data jam operasional dari API.
     */
    fun loadJamOperasional() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            try {
                val response = apiService.getInstansi()
                if (response.isSuccessful && response.body()?.status == "success") {
                    val instansi = response.body()?.data
                    val jo = instansi?.jamOperasional
                    _uiState.update {
                        it.copy(
                            namaInstansi = instansi?.namaInstansi ?: "",
                            jamBuka = jo?.jamBuka ?: "08:00",
                            jamTutup = jo?.jamTutup ?: "17:00",
                            hariOperasional = jo?.hariOperasional?.toSet() ?: setOf(2, 3, 4, 5, 6, 7),
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Gagal memuat data instansi") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Terjadi kesalahan") }
            }
        }
    }

    /**
     * Update jam buka.
     */
    fun updateJamBuka(jam: String) {
        _uiState.update { it.copy(jamBuka = jam) }
    }

    /**
     * Update jam tutup.
     */
    fun updateJamTutup(jam: String) {
        _uiState.update { it.copy(jamTutup = jam) }
    }

    /**
     * Toggle hari operasional.
     */
    fun toggleHari(hari: Int) {
        _uiState.update { state ->
            val newHari = if (hari in state.hariOperasional) {
                state.hariOperasional - hari
            } else {
                state.hariOperasional + hari
            }
            state.copy(hariOperasional = newHari)
        }
    }

    /**
     * Simpan jam operasional ke API.
     */
    fun saveJamOperasional() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null, successMessage = null) }
            try {
                val request = UpdateInstansiRequest(
                    jamOperasional = mapOf(
                        "jam_buka" to _uiState.value.jamBuka,
                        "jam_tutup" to _uiState.value.jamTutup,
                        "hari_operasional" to _uiState.value.hariOperasional.toList()
                    )
                )
                val response = apiService.updateInstansi(request)
                if (response.isSuccessful && response.body()?.status == "success") {
                    _uiState.update {
                        it.copy(isSaving = false, successMessage = "Jam operasional berhasil disimpan")
                    }
                } else {
                    val msg = response.body()?.message ?: "Gagal menyimpan jam operasional"
                    _uiState.update { it.copy(isSaving = false, errorMessage = msg) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, errorMessage = e.message ?: "Terjadi kesalahan") }
            }
        }
    }

    /**
     * Clear messages.
     */
    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
