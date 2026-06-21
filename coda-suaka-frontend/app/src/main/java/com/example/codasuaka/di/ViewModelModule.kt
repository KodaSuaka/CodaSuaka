package com.example.codasuaka.di

import com.example.codasuaka.ui.auth.AuthViewModel
import com.example.codasuaka.ui.kelola_outlet.KelolaOutletViewModel
import com.example.codasuaka.ui.screen.dashboard.DashboardViewModel
import com.example.codasuaka.ui.screen.login.LoginViewModel
import com.example.codasuaka.ui.screen.register.RegisterViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Module Koin untuk ViewModel.
 */
val viewModelModule = module {
    viewModel { LoginViewModel(loginUseCase = get()) }
    viewModel { RegisterViewModel(registerUseCase = get()) }
    viewModel { AuthViewModel(tokenManager = get()) }
    viewModel { DashboardViewModel() }
    viewModel { KelolaOutletViewModel() }
}
