package com.example.codasuaka.ui.screen.nota_detail

import android.content.ContentValues
import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.codasuaka.data.remote.dto.NotaDto
import com.example.codasuaka.domain.repository.KasirRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import java.io.IOException

data class NotaDetailUiState(
    val nota: NotaDto? = null,
    val isLoading: Boolean = false,
    val loadError: String? = null,

    // Unduh PDF
    val isDownloading: Boolean = false,
    val downloadError: String? = null,
    val downloadSuccessPath: String? = null,

    // Hapus
    val isDeleting: Boolean = false,
    val deleteError: String? = null,
    val deleteSuccess: String? = null
)

class NotaDetailViewModel(
    private val notaId: Int,
    private val kasirRepository: KasirRepository,
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotaDetailUiState())
    val uiState: StateFlow<NotaDetailUiState> = _uiState.asStateFlow()

    init {
        loadNotaDetail()
    }

    fun loadNotaDetail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadError = null) }
            kasirRepository.getNotaDetail(notaId)
                .onSuccess { nota ->
                    _uiState.update { it.copy(isLoading = false, nota = nota) }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isLoading = false, loadError = e.message ?: "Gagal memuat detail nota")
                    }
                }
        }
    }

    fun downloadPdf() {
        val nota = _uiState.value.nota ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isDownloading = true, downloadError = null) }
            kasirRepository.getNotaPdf(notaId)
                .onSuccess { body ->
                    val sanitized = nota.nomorNota.replace(Regex("[^A-Za-z0-9._-]"), "_")
                    saveFile(body, "nota_${sanitized}.pdf")
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isDownloading = false, downloadError = e.message ?: "Gagal mengunduh PDF")
                    }
                }
        }
    }

    fun deleteNota() {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, deleteError = null) }
            kasirRepository.deleteNota(notaId)
                .onSuccess {
                    _uiState.update { it.copy(isDeleting = false, deleteSuccess = "Nota berhasil dihapus") }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isDeleting = false, deleteError = e.message ?: "Gagal menghapus nota")
                    }
                }
        }
    }

    private fun saveFile(body: ResponseBody, filename: String) {
        try {
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                ?: throw IOException("Gagal membuat entri file di Downloads")
            resolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(body.bytes())
            } ?: throw IOException("Gagal membuka output stream")
            _uiState.update {
                it.copy(isDownloading = false, downloadSuccessPath = filename)
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    isDownloading = false,
                    downloadError = "Gagal menyimpan file: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(loadError = null, downloadError = null, deleteError = null) }
    }

    fun clearDownloadSuccess() {
        _uiState.update { it.copy(downloadSuccessPath = null) }
    }

    fun clearDeleteSuccess() {
        _uiState.update { it.copy(deleteSuccess = null) }
    }
}
