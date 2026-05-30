package com.example.codasuaka.ui.screen.auth.register

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class RegisterViewModel : ViewModel() {
    var ownerName by mutableStateOf("")
        private set
    
    var email by mutableStateOf("")
        private set
    
    var phoneNumber by mutableStateOf("")
        private set
    
    var password by mutableStateOf("")
        private set

    fun onOwnerNameChange(newValue: String) {
        ownerName = newValue
    }

    fun onEmailChange(newValue: String) {
        email = newValue
    }

    fun onPhoneNumberChange(newValue: String) {
        phoneNumber = newValue
    }

    fun onPasswordChange(newValue: String) {
        password = newValue
    }

    fun onRegisterClick() {
        // Logic for register
    }
}
