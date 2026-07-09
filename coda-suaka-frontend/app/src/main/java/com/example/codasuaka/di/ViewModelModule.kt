package com.example.codasuaka.di

import com.example.codasuaka.data.repository.AuthRepositoryImpl
import com.example.codasuaka.domain.repository.*
import com.example.codasuaka.ui.screen.auth.AuthViewModel
import com.example.codasuaka.ui.screen.chat.ChatContactViewModel
import com.example.codasuaka.ui.screen.chat.ChatDetailViewModel
import com.example.codasuaka.ui.screen.pengajuan.PengajuanViewModel
import com.example.codasuaka.ui.screen.dashboard.DashboardViewModel
import com.example.codasuaka.ui.screen.dashboard_karyawan.DashboardKaryawanViewModel
import com.example.codasuaka.ui.screen.divisi.DivisiViewModel
import com.example.codasuaka.ui.screen.kalender.KalenderViewModel
import com.example.codasuaka.ui.screen.kelola_karyawan.KelolaKaryawanViewModel
import com.example.codasuaka.ui.screen.kelola_outlet.KelolaOutletViewModel
import com.example.codasuaka.ui.screen.login.LoginViewModel
import com.example.codasuaka.ui.screen.register.RegisterViewModel
import com.example.codasuaka.ui.screen.laporan_keuangan.LaporanKeuanganViewModel
import com.example.codasuaka.ui.screen.riwayat_kehadiran.RiwayatKehadiranViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Module Koin untuk ViewModel.
 */
val viewModelModule = module {
    // Auth
    viewModel { LoginViewModel(loginUseCase = get()) }
    viewModel { RegisterViewModel(registerUseCase = get()) }
    viewModel { AuthViewModel(authRepository = get(), tokenManager = get()) }

    // Dashboard
    viewModel { DashboardViewModel(
        dashboardRepository = get(),
        tokenManager = get()
    ) }

    // Dashboard Karyawan
    viewModel { DashboardKaryawanViewModel(
        presensiRepository = get(),
        penugasanRepository = get(),
        karyawanRepository = get(),
        pengajuanRepository = get(),
        dashboardRepository = get()
    ) }

    // Kelola Outlet
    viewModel { KelolaOutletViewModel(outletRepository = get()) }

    // Kelola Karyawan
    viewModel { KelolaKaryawanViewModel(
        karyawanRepository = get(),
        outletRepository = get()
    ) }

    // Divisi
    viewModel { DivisiViewModel(
        divisiRepository = get(),
        karyawanRepository = get(),
        outletRepository = get()
    ) }

    // Riwayat Kehadiran
    viewModel { RiwayatKehadiranViewModel(
        presensiRepository = get(),
        pengajuanRepository = get(),
        outletRepository = get()
    ) }

    // Kalender
    viewModel { KalenderViewModel(jadwalRepository = get()) }

    // Pengajuan
    viewModel { PengajuanViewModel(pengajuanRepository = get()) }

    // Chat
    viewModel { ChatContactViewModel(chatRepository = get()) }
    viewModel { params ->
        ChatDetailViewModel(
            userId = params.get(),
            userName = params.get(),
            chatRepository = get<ChatRepository>()
        )
    }

    // Keuangan
    viewModel { LaporanKeuanganViewModel(keuanganRepository = get()) }
}
