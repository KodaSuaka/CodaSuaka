package com.example.codasuaka.ui.screen.notifikasi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.codasuaka.data.remote.dto.NotificationDto
import com.example.codasuaka.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NotificationUiState(
    val notifications: List<NotificationDto> = emptyList(),
    val unreadCount: Int = 0,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isSidebarOpen: Boolean = false,
    val error: String? = null,
    val currentPage: Int = 1,
    val lastPage: Int = 1,
    val isPaginating: Boolean = false,
)

class NotificationViewModel(
    private val notificationRepository: NotificationRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    init {
        loadNotifications()
        loadUnreadCount()
    }

    fun toggleSidebar(open: Boolean) {
        _uiState.value = _uiState.value.copy(isSidebarOpen = open)
        if (open) {
            refresh() // Refresh when opening to show latest
        }
    }

    fun loadNotifications() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            notificationRepository.getNotifications(page = 1, perPage = 20)
                .onSuccess { (notifications, meta) ->
                    _uiState.value = _uiState.value.copy(
                        notifications = notifications,
                        currentPage = 1,
                        lastPage = meta?.lastPage ?: 1,
                        isLoading = false,
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Gagal memuat notifikasi",
                    )
                }
        }
    }

    fun loadMore() {
        val state = _uiState.value
        if (state.isPaginating || state.currentPage >= state.lastPage) return

        viewModelScope.launch {
            _uiState.value = state.copy(isPaginating = true)
            val nextPage = state.currentPage + 1
            notificationRepository.getNotifications(page = nextPage, perPage = 20)
                .onSuccess { (notifications, meta) ->
                    _uiState.value = _uiState.value.copy(
                        notifications = state.notifications + notifications,
                        currentPage = nextPage,
                        lastPage = meta?.lastPage ?: state.lastPage,
                        isPaginating = false,
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(isPaginating = false)
                }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true, error = null)
            notificationRepository.getNotifications(page = 1, perPage = 20)
                .onSuccess { (notifications, meta) ->
                    _uiState.value = _uiState.value.copy(
                        notifications = notifications,
                        currentPage = 1,
                        lastPage = meta?.lastPage ?: 1,
                        isRefreshing = false,
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isRefreshing = false,
                        error = e.message ?: "Gagal memuat notifikasi",
                    )
                }
            loadUnreadCount()
        }
    }

    fun loadUnreadCount() {
        viewModelScope.launch {
            notificationRepository.getUnreadCount()
                .onSuccess { count ->
                    _uiState.value = _uiState.value.copy(unreadCount = count)
                }
        }
    }

    fun markAsRead(notificationId: Int) {
        viewModelScope.launch {
            notificationRepository.markAsRead(notificationId)
                .onSuccess {
                    // Update local state
                    val updatedNotifications = _uiState.value.notifications.map { notif ->
                        if (notif.id == notificationId) notif.copy(isRead = true) else notif
                    }
                    _uiState.value = _uiState.value.copy(
                        notifications = updatedNotifications,
                        unreadCount = (_uiState.value.unreadCount - 1).coerceAtLeast(0),
                    )
                }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            notificationRepository.markAllAsRead()
                .onSuccess {
                    val updatedNotifications = _uiState.value.notifications.map {
                        it.copy(isRead = true)
                    }
                    _uiState.value = _uiState.value.copy(
                        notifications = updatedNotifications,
                        unreadCount = 0,
                    )
                }
        }
    }

    fun deleteNotification(notificationId: Int) {
        viewModelScope.launch {
            notificationRepository.deleteNotification(notificationId)
                .onSuccess {
                    val updatedNotifications = _uiState.value.notifications.filter {
                        it.id != notificationId
                    }
                    val wasUnread = _uiState.value.notifications.find { it.id == notificationId }?.isRead == false
                    _uiState.value = _uiState.value.copy(
                        notifications = updatedNotifications,
                        unreadCount = if (wasUnread) (_uiState.value.unreadCount - 1).coerceAtLeast(0) else _uiState.value.unreadCount,
                    )
                }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}