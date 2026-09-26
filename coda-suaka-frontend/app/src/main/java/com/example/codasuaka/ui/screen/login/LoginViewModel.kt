package com.example.codasuaka.ui.screen.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.codasuaka.data.local.PreferenceManager
import com.example.codasuaka.domain.usecase.LoginUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val loginSuccess: Boolean = false,
    val userRole: String? = null,
    val userPermissions: List<String>? = null,
    val showPrivacyPopup: Boolean = false
)

class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    init {
        val isAccepted = preferenceManager.isPrivacyAccepted()
        _uiState.value = _uiState.value.copy(showPrivacyPopup = !isAccepted)
    }

    fun acceptPrivacyPolicy() {
        preferenceManager.setPrivacyAccepted(true)
        _uiState.value = _uiState.value.copy(showPrivacyPopup = false)
    }

    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(email = email, errorMessage = null)
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(password = password, errorMessage = null)
    }

    fun login() {
        val state = _uiState.value
        _uiState.value = state.copy(isLoading = true, errorMessage = null, loginSuccess = false)

        viewModelScope.launch {
            loginUseCase(state.email, state.password)
                .onSuccess { user ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        loginSuccess = true,
                        userRole = user.role,
                        userPermissions = user.permissions
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Login gagal."
                    )
                }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
