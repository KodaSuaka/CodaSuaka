package com.example.codasuaka.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.codasuaka.data.remote.dto.MessageDto
import com.example.codasuaka.domain.repository.ChatRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ChatDetailUiState(
    val messages: List<MessageDto> = emptyList(),
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val errorMessage: String? = null,
    val contactName: String = "",
    val contactId: Int = 0,
    val contactPhoto: String? = null
)

class ChatDetailViewModel(
    private val userId: Int,
    private val userName: String,
    private val chatRepository: ChatRepository
) : ViewModel() {

    val contactUserId: Int get() = userId

    private val _uiState = MutableStateFlow(
        ChatDetailUiState(contactName = userName)
    )
    val uiState: StateFlow<ChatDetailUiState> = _uiState

    private var pollingJob: Job? = null

    init {
        loadMessages()
        startPolling()
    }

    fun loadMessages() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            chatRepository.getMessages(userId)
                .onSuccess { messages ->
                    _uiState.value = _uiState.value.copy(
                        messages = messages,
                        isLoading = false
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Gagal memuat pesan"
                    )
                }
        }
    }

    fun sendMessage(pesan: String) {
        if (pesan.isBlank()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSending = true)

            chatRepository.sendMessage(userId, pesan)
                .onSuccess { newMessage ->
                    // Tambahkan pesan baru ke daftar
                    val updatedMessages = _uiState.value.messages + newMessage
                    _uiState.value = _uiState.value.copy(
                        messages = updatedMessages,
                        isSending = false
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isSending = false,
                        errorMessage = error.message ?: "Gagal mengirim pesan"
                    )
                }
        }
    }

    /**
     * Polling setiap 5 detik untuk cek pesan baru.
     */
    private fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (true) {
                delay(5_000L)
                chatRepository.getMessages(userId)
                    .onSuccess { messages ->
                        _uiState.value = _uiState.value.copy(
                            messages = messages,
                            errorMessage = null
                        )
                    }
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
