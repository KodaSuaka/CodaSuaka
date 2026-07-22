package com.example.codasuaka.ui.screen.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.codasuaka.data.local.TokenManager
import com.example.codasuaka.data.remote.dto.MessageDto
import com.example.codasuaka.domain.repository.ChatRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ChatDetailUiState(
    val messages: List<MessageDto> = emptyList(),
    val isLoading: Boolean = true,
    val isSending: Boolean = false,
    val errorMessage: String? = null,
    val contactName: String = "",
    val contactId: Int = 0,
    val contactPhoto: String? = null
)

class ChatDetailViewModel(
    private val userId: Int,
    private val userName: String,
    private val chatRepository: ChatRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    val contactUserId: Int get() = userId
    private var currentUserId: Int = 0

    private val _uiState = MutableStateFlow(
        ChatDetailUiState(contactName = userName, contactId = userId)
    )
    val uiState: StateFlow<ChatDetailUiState> = _uiState

    private var pollingJob: Job? = null

    init {
        loadCurrentUserId()
        loadMessages()
        markAsRead()
        startPolling()
    }

    private fun loadCurrentUserId() {
        viewModelScope.launch {
            currentUserId = tokenManager.getUserId()?.toIntOrNull() ?: 0
        }
    }

    private fun markAsRead() {
        viewModelScope.launch {
            chatRepository.markAsRead(userId)
        }
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

            // Gunakan userId sebagai penerimaId
            chatRepository.sendMessage(userId, pesan)
                .onSuccess { newMessage ->
                    // Memastikan ID pengirim benar (Me)
                    val finalizedMessage = if (newMessage.pengirimId == 0) {
                        newMessage.copy(pengirimId = currentUserId, penerimaId = userId)
                    } else {
                        newMessage
                    }

                    // Tambahkan pesan baru ke daftar
                    val updatedMessages = _uiState.value.messages + finalizedMessage
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
