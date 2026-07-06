package com.example.codasuaka.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.codasuaka.data.remote.dto.ContactGroupDto
import com.example.codasuaka.domain.repository.ChatRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ChatContactUiState(
    val contactGroups: List<ContactGroupDto> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class ChatContactViewModel(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatContactUiState())
    val uiState: StateFlow<ChatContactUiState> = _uiState

    private var pollingJob: Job? = null

    init {
        loadContacts()
        startPolling()
    }

    fun loadContacts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            chatRepository.getContacts()
                .onSuccess { groups ->
                    _uiState.value = _uiState.value.copy(
                        contactGroups = groups,
                        isLoading = false
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Gagal memuat kontak"
                    )
                }
        }
    }

    /**
     * Polling setiap 30 detik untuk update daftar kontak (unread count, dll).
     */
    private fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (true) {
                delay(30_000L)
                chatRepository.getContacts()
                    .onSuccess { groups ->
                        _uiState.value = _uiState.value.copy(
                            contactGroups = groups,
                            errorMessage = null
                        )
                    }
                // Abaikan error polling agar tidak mengganggu UI
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
    }
}
