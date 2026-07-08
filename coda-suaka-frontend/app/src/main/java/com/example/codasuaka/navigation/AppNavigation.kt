package com.example.codasuaka.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import org.koin.androidx.compose.koinViewModel
import com.example.codasuaka.ui.screen.auth.AuthScreen
import com.example.codasuaka.ui.screen.auth.AuthViewModel
import com.example.codasuaka.ui.screen.pengajuan.PengajuanScreen
import com.example.codasuaka.ui.screen.pengajuan.PengajuanViewModel
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
import com.example.codasuaka.ui.screen.chat.ChatContactListScreen
import com.example.codasuaka.ui.screen.chat.ChatContactViewModel
import com.example.codasuaka.ui.screen.chat.ChatDetailScreen
import com.example.codasuaka.ui.screen.chat.ChatDetailViewModel
import com.example.codasuaka.ui.screen.dashboard.DashboardScreen
import com.example.codasuaka.ui.screen.dashboard.DashboardViewModel
import com.example.codasuaka.ui.screen.dashboard_karyawan.DashboardKaryawanScreen
import com.example.codasuaka.ui.screen.dashboard_karyawan.DashboardKaryawanViewModel
import com.example.codasuaka.ui.screen.laporan_keuangan.LaporanKeuanganScreen
import com.example.codasuaka.ui.screen.login.LoginScreen
import com.example.codasuaka.ui.screen.login.LoginViewModel
import com.example.codasuaka.ui.screen.register.RegisterScreen
import com.example.codasuaka.ui.screen.register.RegisterViewModel
import org.koin.core.parameter.parametersOf
import java.net.URLEncoder

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
    const val CONTACT_LIST = "contact_list"
    const val CHAT_DETAIL = "chat_detail/{userId}/{userName}"
    const val DIVISI = "divisi"
    const val TAMBAH_KARYAWAN = "tambah_karyawan"
    const val PENGAJUAN = "pengajuan"
    const val LOG_ABSENSI = "log_absensi"
    const val LAPORAN_KEUANGAN = "laporan_keuangan"
    const val STATUS_KARYAWAN = "status_karyawan"

    fun chatDetail(userId: Int, userName: String): String {
        val encodedName = URLEncoder.encode(userName, "UTF-8")
        return "chat_detail/$userId/$encodedName"
    }
}

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.AUTH
    ) {
        // ── Auth Check (gatekeeper) ──
        composable(Routes.AUTH) {
            val authViewModel: AuthViewModel = koinViewModel()
            AuthScreen(
                viewModel = authViewModel,
                onAuthenticated = {
                    // Navigasi berdasarkan role akan ditentukan oleh AuthScreen
                    // Default ke DASHBOARD (Owner), jika Karyawan akan pakai DASHBOARD_KARYAWAN
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
                onLoginSuccess = { role ->
                    // Functional roles (Keuangan, Manajemen, Staff) dan Karyawan
                    // semua masuk ke DASHBOARD_KARYAWAN
                    // Owner masuk ke DASHBOARD
                    val functionalRoles = listOf("Keuangan", "Manajemen", "Staff", "Karyawan")
                    val destination = if (role in functionalRoles) {
                        Routes.DASHBOARD_KARYAWAN
                    } else {
                        Routes.DASHBOARD // Owner (dan role lain yang tidak dikenal)
                    }
                    navController.navigate(destination) {
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

        // ── Dashboard (Owner) ──
        composable(Routes.DASHBOARD) {
            val dashboardViewModel: DashboardViewModel = koinViewModel()
            val authViewModel: AuthViewModel = koinViewModel()
            DashboardScreen(
                viewModel = dashboardViewModel,
                onNavigateTo = { route ->
                    try {
                        navController.navigate(route)
                    } catch (_: Exception) {
                        // Route not available — ignore
                    }
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // ── Dashboard Karyawan ──
        composable(Routes.DASHBOARD_KARYAWAN) {
            val dashboardKaryawanViewModel: DashboardKaryawanViewModel = koinViewModel()
            val authViewModel: AuthViewModel = koinViewModel()
            DashboardKaryawanScreen(
                onNavigateTo = { route ->
                    try {
                        navController.navigate(route)
                    } catch (_: Exception) {
                        // Route not available — ignore
                    }
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                viewModel = dashboardKaryawanViewModel
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

        // ── Contact List (Chat) ──
        composable(Routes.CONTACT_LIST) {
            val chatContactViewModel: ChatContactViewModel = koinViewModel()
            ChatContactListScreen(
                onBack = { navController.popBackStack() },
                onContactClick = { userId, userName ->
                    navController.navigate(Routes.chatDetail(userId, userName))
                },
                viewModel = chatContactViewModel
            )
        }

        // ── Chat Detail ──
        composable(
            route = Routes.CHAT_DETAIL,
            arguments = listOf(
                navArgument("userId") { type = NavType.IntType },
                navArgument("userName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: return@composable
            val userName = backStackEntry.arguments?.getString("userName") ?: "User"
            val chatDetailViewModel: ChatDetailViewModel = koinViewModel(
                parameters = { parametersOf(userId, userName) }
            )
            ChatDetailScreen(
                onBack = { navController.popBackStack() },
                viewModel = chatDetailViewModel
            )
        }

        // ── Divisi ──
        composable(Routes.DIVISI) {
            val divisiViewModel: DivisiViewModel = koinViewModel()
            DivisiScreen(
                onBack = { navController.popBackStack() },
                viewModel = divisiViewModel
            )
        }

        // ── Tambah Karyawan (alias ke KelolaKaryawan) ──
        composable(Routes.TAMBAH_KARYAWAN) {
            val kelolaKaryawanViewModel: KelolaKaryawanViewModel = koinViewModel()
            KelolaKaryawanScreen(
                onBack = { navController.popBackStack() },
                viewModel = kelolaKaryawanViewModel
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

        // ── Log Absensi (redirect ke Riwayat Kehadiran) ──
        composable(Routes.LOG_ABSENSI) {
            val riwayatKehadiranViewModel: RiwayatKehadiranViewModel = koinViewModel()
            RiwayatKehadiranScreen(
                onBack = { navController.popBackStack() },
                viewModel = riwayatKehadiranViewModel
            )
        }

        // ── Laporan Keuangan (placeholder) ──
        composable(Routes.LAPORAN_KEUANGAN) {
            LaporanKeuanganScreen(onBack = { navController.popBackStack() })
        }

        // ── Status Karyawan (redirect ke Kelola Karyawan) ──
        composable(Routes.STATUS_KARYAWAN) {
            val kelolaKaryawanViewModel: KelolaKaryawanViewModel = koinViewModel()
            KelolaKaryawanScreen(
                onBack = { navController.popBackStack() },
                viewModel = kelolaKaryawanViewModel
            )
        }
    }
}
