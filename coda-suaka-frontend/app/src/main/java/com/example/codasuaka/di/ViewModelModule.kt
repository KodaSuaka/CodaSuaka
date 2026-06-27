package com.example.codasuaka.di

import com.example.codasuaka.ui.auth.AuthViewModel
import com.example.codasuaka.ui.screen.kelola_outlet.KelolaOutletViewModel
import com.example.codasuaka.ui.screen.kelola_karyawan.KelolaKaryawanViewModel
import com.example.codasuaka.ui.screen.riwayat_kehadiran.RiwayatKehadiranViewModel
import com.example.codasuaka.ui.screen.kalender.KalenderViewModel
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
    viewModel { KelolaKaryawanViewModel() }
    viewModel { RiwayatKehadiranViewModel() }
    viewModel { KalenderViewModel() }
}
