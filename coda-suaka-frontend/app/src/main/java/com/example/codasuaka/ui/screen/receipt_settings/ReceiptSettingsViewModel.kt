package com.example.codasuaka.ui.screen.receipt_settings

import androidx.lifecycle.ViewModel
import com.example.codasuaka.data.local.PreferenceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ReceiptSettingsUiState(
    val header: String = "",
    val tagline: String = "",
    val footer1: String = "",
    val footer2: String = "",
    val isSaved: Boolean = false
)

class ReceiptSettingsViewModel(
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReceiptSettingsUiState())
    val uiState: StateFlow<ReceiptSettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        _uiState.update {
            it.copy(
                header = preferenceManager.getHeaderText(),
                tagline = preferenceManager.getTaglineText(),
                footer1 = preferenceManager.getFooterText1(),
                footer2 = preferenceManager.getFooterText2()
            )
        }
    }

    fun onHeaderChange(value: String) = _uiState.update { it.copy(header = value, isSaved = false) }
    fun onTaglineChange(value: String) = _uiState.update { it.copy(tagline = value, isSaved = false) }
    fun onFooter1Change(value: String) = _uiState.update { it.copy(footer1 = value, isSaved = false) }
    fun onFooter2Change(value: String) = _uiState.update { it.copy(footer2 = value, isSaved = false) }

    fun saveSettings() {
        val state = _uiState.value
        preferenceManager.saveReceiptSettings(
            header = state.header,
            tagline = state.tagline,
            footer1 = state.footer1,
            footer2 = state.footer2
        )
        _uiState.update { it.copy(isSaved = true) }
    }

    fun resetToDefault() {
        preferenceManager.resetToDefault()
        loadSettings()
        _uiState.update { it.copy(isSaved = true) }
    }

    fun clearSavedSignal() {
        _uiState.update { it.copy(isSaved = false) }
    }
}
