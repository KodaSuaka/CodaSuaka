package com.example.codasuaka.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import org.koin.androidx.compose.koinViewModel
import com.example.codasuaka.ui.auth.AuthScreen
import com.example.codasuaka.ui.auth.AuthViewModel
import com.example.codasuaka.ui.pengajuan.PengajuanScreen
import com.example.codasuaka.ui.pengajuan.PengajuanViewModel
import com.example.codasuaka.ui.screen.divisi.DivisiScreen
import com.example.codasuaka.ui.screen.divisi.DivisiViewModel
import com.example.codasuaka.ui.screen.kelola_outlet.KelolaOutletScreen
import com.example.codasuaka.ui.screen.kelola_outlet.KelolaOutletViewModel
import com.example.codasuaka.ui.screen.kelola_karyawan.KelolaKaryawanScreen
import com.example.codasuaka.ui.screen.kelola_karyawan.KelolaKaryawanViewModel
import com.example.codasuaka.ui.screen.riwayat_kehadiran.RiwayatKehadiranScreen
import com.example.codasuaka.ui.screen.riwayat_kehadiran.RiwayatKehadiranViewModel
import com.example.codasuaka.ui.screen.kalender.KalenderScreen
import com.example.codasuaka.ui.screen.kalender.KalenderViewModel
import com.example.codasuaka.ui.chat.ChatContactListScreen
import com.example.codasuaka.ui.chat.ChatContactViewModel
import com.example.codasuaka.ui.chat.ChatDetailScreen
import com.example.codasuaka.ui.chat.ChatDetailViewModel
import com.example.codasuaka.ui.screen.dashboard.DashboardScreen
import com.example.codasuaka.ui.screen.dashboard.DashboardViewModel
import com.example.codasuaka.ui.screen.dashboard_karyawan.DashboardKaryawanScreen
import com.example.codasuaka.ui.screen.dashboard_karyawan.DashboardKaryawanViewModel
import com.example.codasuaka.ui.screen.login.LoginScreen
import com.example.codasuaka.ui.screen.login.LoginViewModel
import com.example.codasuaka.ui.screen.register.RegisterScreen
import com.example.codasuaka.ui.screen.register.RegisterViewModel
import com.example.codasuaka.ui.theme.Primary
import org.koin.core.parameter.parametersOf

object Routes {
    const val AUTH = "auth"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val DASHBOARD = "dashboard"
    const val DASHBOARD_KARYAWAN = "dashboard_karyawan"
    const val KELOLA_OUTLET = "kelola_outlet"
    const val KELOLA_KARYAWAN = "kelola_karyawan"
    const val KALENDER = "kalender"
    const val RIWAYAT_KEHADIRAN = "riwayat_kehadiran"
    const val LOG_ABSENSI = "log_absensi"
    const val LAPORAN_KEUANGAN = "laporan_keuangan"
    const val STATUS_KARYAWAN = "status_karyawan"
    const val TUGAS_TIM = "tugas_tim"
    const val PESAN = "pesan"
    const val CONTACT_LIST = "contact_list"
    const val CHAT_DETAIL = "chat_detail/{userId}/{userName}"
    const val DIVISI = "divisi"
    const val DATA_PERSETUJUAN = "data_persetujuan"
    const val TAMBAH_KARYAWAN = "tambah_karyawan"
    const val KELOLA_SHIFT = "kelola_shift"
    const val PENGAJUAN = "pengajuan"
    const val DETAIL_KINERJA = "detail_kinerja"
    const val SISA_CUTI = "sisa_cuti"
    const val PELATIHAN = "pelatihan"
    const val PENGHARGAAN = "penghargaan"
}

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.DASHBOARD
    ) {
        // ── Auth Check (gatekeeper) ──
        composable(Routes.AUTH) {
            val authViewModel: AuthViewModel = koinViewModel()
            AuthScreen(
                viewModel = authViewModel,
                onAuthenticated = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.AUTH) { inclusive = true }
                    }
                },
                onUnauthenticated = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.AUTH) { inclusive = true }
                    }
                }
            )
        }

        // ── Login ──
        composable(Routes.LOGIN) {
            val loginViewModel: LoginViewModel = koinViewModel()
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Routes.REGISTER)
                },
                viewModel = loginViewModel
            )
        }

        // ── Register ──
        composable(Routes.REGISTER) {
            val registerViewModel: RegisterViewModel = koinViewModel()
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                viewModel = registerViewModel
            )
        }

        // ── Dashboard ──
        composable(Routes.DASHBOARD) {
            val dashboardViewModel: DashboardViewModel = koinViewModel()
            DashboardScreen(
                viewModel = dashboardViewModel,
                onNavigateTo = { route ->
                    navController.navigate(route)
                },
                onLogout = {
                    val authViewModel: AuthViewModel = koinViewModel()
                    authViewModel.logout()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // ── Kelola Outlet ──
        composable(Routes.KELOLA_OUTLET) {
            val kelolaOutletViewModel: KelolaOutletViewModel = koinViewModel()
            KelolaOutletScreen(
                onBack = { navController.popBackStack() },
                viewModel = kelolaOutletViewModel
            )
        }

        // ── Kelola Karyawan ──
        composable(Routes.KELOLA_KARYAWAN) {
            val kelolaKaryawanViewModel: KelolaKaryawanViewModel = koinViewModel()
            KelolaKaryawanScreen(
                onBack = { navController.popBackStack() },
                viewModel = kelolaKaryawanViewModel
            )
        }

        // ── Kalender / Jadwal ──
        composable(Routes.KALENDER) {
            val kalenderViewModel: KalenderViewModel = koinViewModel()
            KalenderScreen(
                onBack = { navController.popBackStack() },
                viewModel = kalenderViewModel
            )
        }

        // ── Riwayat Kehadiran ──
        composable(Routes.RIWAYAT_KEHADIRAN) {
            val riwayatKehadiranViewModel: RiwayatKehadiranViewModel = koinViewModel()
            RiwayatKehadiranScreen(
                onBack = { navController.popBackStack() },
                viewModel = riwayatKehadiranViewModel
            )
        }

        // ── Placeholder screens untuk fitur lainnya ──
        composable(Routes.LOG_ABSENSI) {
            PlaceholderScreen(title = "Log Absensi")
        }
        composable(Routes.LAPORAN_KEUANGAN) {
            PlaceholderScreen(title = "Laporan Keuangan")
        }
        composable(Routes.STATUS_KARYAWAN) {
            PlaceholderScreen(title = "Status Karyawan")
        }
        composable(Routes.TUGAS_TIM) {
            PlaceholderScreen(title = "Tugas Tim")
        }
        composable(Routes.CONTACT_LIST) {
            val chatContactViewModel: ChatContactViewModel = koinViewModel()
            ChatContactListScreen(
                onBack = { navController.popBackStack() },
                onContactClick = { userId, userName ->
                    navController.navigate("chat_detail/$userId/$userName")
                },
                viewModel = chatContactViewModel
            )
        }

        // ── Chat Detail ──
        composable(Routes.CHAT_DETAIL) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")?.toIntOrNull() ?: return@composable
            val userName = backStackEntry.arguments?.getString("userName") ?: "User"
            val chatDetailViewModel: ChatDetailViewModel = koinViewModel(
                parameters = { parametersOf(userId, userName) }
            )
            ChatDetailScreen(
                onBack = { navController.popBackStack() },
                viewModel = chatDetailViewModel
            )
        }

        composable(Routes.PESAN) {
            PlaceholderScreen(title = "Pesan")
        }
        composable(Routes.DIVISI) {
            val divisiViewModel: DivisiViewModel = koinViewModel()
            DivisiScreen(
                onBack = { navController.popBackStack() },
                viewModel = divisiViewModel
            )
        }
        composable(Routes.DATA_PERSETUJUAN) {
            PlaceholderScreen(title = "Data Persetujuan")
        }
        composable(Routes.TAMBAH_KARYAWAN) {
            val kelolaKaryawanViewModel: KelolaKaryawanViewModel = koinViewModel()
            KelolaKaryawanScreen(
                onBack = { navController.popBackStack() },
                viewModel = kelolaKaryawanViewModel
            )
        }
        composable(Routes.KELOLA_SHIFT) {
            PlaceholderScreen(title = "Kelola Shift")
        }

        // ── Dashboard Karyawan ──
        composable(Routes.DASHBOARD_KARYAWAN) {
            val dashboardKaryawanViewModel: DashboardKaryawanViewModel = koinViewModel()
            DashboardKaryawanScreen(
                onNavigateTo = { route ->
                    navController.navigate(route)
                },
                onLogout = {
                    val authViewModel: AuthViewModel = koinViewModel()
                    authViewModel.logout()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                viewModel = dashboardKaryawanViewModel
            )
        }

        // ── Pengajuan (Cuti/Izin) ──
        composable(Routes.PENGAJUAN) {
            val pengajuanViewModel: PengajuanViewModel = koinViewModel()
            PengajuanScreen(
                onBack = { navController.popBackStack() },
                viewModel = pengajuanViewModel
            )
        }
        composable(Routes.DETAIL_KINERJA) { PlaceholderScreen(title = "Detail Kinerja") }
        composable(Routes.SISA_CUTI) { PlaceholderScreen(title = "Sisa Cuti") }
        composable(Routes.PELATIHAN) { PlaceholderScreen(title = "Pelatihan") }
        composable(Routes.PENGHARGAAN) { PlaceholderScreen(title = "Penghargaan") }
    }
}

/**
 * Placeholder screen untuk fitur yang belum diimplementasikan.
 * Menampilkan judul halaman di tengah layar dengan tema aplikasi.
 */
@Composable
private fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Primary
        )
    }
}
