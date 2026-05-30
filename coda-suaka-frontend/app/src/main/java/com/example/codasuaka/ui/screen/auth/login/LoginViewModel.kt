package com.example.codasuaka.ui.screen.auth.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class LoginViewModel : ViewModel() {
    var ownername by mutableStateOf("")
        private set
    
    var password by mutableStateOf("")
        private set
    
    var rememberMe by mutableStateOf(false)
        private set

    fun onOwnernameChange(newValue: String) {
        ownername = newValue
    }

    fun onPasswordChange(newValue: String) {
        password = newValue
    }

    fun onRememberMeChange(newValue: Boolean) {
        rememberMe = newValue
    }

    fun onLoginClick() {
        // Logic for login
    }
}
