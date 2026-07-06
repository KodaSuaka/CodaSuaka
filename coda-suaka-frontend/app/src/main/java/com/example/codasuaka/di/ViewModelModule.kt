package com.example.codasuaka.di

import com.example.codasuaka.domain.repository.ChatRepository
import com.example.codasuaka.ui.auth.AuthViewModel
import com.example.codasuaka.ui.chat.ChatContactViewModel
import com.example.codasuaka.ui.chat.ChatDetailViewModel
import com.example.codasuaka.ui.pengajuan.PengajuanViewModel
import com.example.codasuaka.ui.screen.kelola_outlet.KelolaOutletViewModel
import com.example.codasuaka.ui.screen.kelola_karyawan.KelolaKaryawanViewModel
import com.example.codasuaka.ui.screen.riwayat_kehadiran.RiwayatKehadiranViewModel
import com.example.codasuaka.ui.screen.kalender.KalenderViewModel
import com.example.codasuaka.ui.screen.dashboard.DashboardViewModel
import com.example.codasuaka.ui.screen.dashboard_karyawan.DashboardKaryawanViewModel
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
    viewModel { DashboardKaryawanViewModel() }
    viewModel { KelolaOutletViewModel() }
    viewModel { KelolaKaryawanViewModel() }
    viewModel { RiwayatKehadiranViewModel() }
    viewModel { KalenderViewModel() }
    viewModel { PengajuanViewModel() }

    // Chat ViewModels
    viewModel { ChatContactViewModel(chatRepository = get()) }
    viewModel { params ->
        ChatDetailViewModel(
            userId = params.get(),
            userName = params.get(),
            chatRepository = get<ChatRepository>()
        )
    }
}
