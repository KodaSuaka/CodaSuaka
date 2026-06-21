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
import com.example.codasuaka.ui.kelola_outlet.KelolaOutletScreen
import com.example.codasuaka.ui.kelola_outlet.KelolaOutletViewModel
import com.example.codasuaka.ui.screen.dashboard.DashboardScreen
import com.example.codasuaka.ui.screen.dashboard.DashboardViewModel
import com.example.codasuaka.ui.screen.login.LoginScreen
import com.example.codasuaka.ui.screen.login.LoginViewModel
import com.example.codasuaka.ui.screen.register.RegisterScreen
import com.example.codasuaka.ui.screen.register.RegisterViewModel
import com.example.codasuaka.ui.theme.Primary

object Routes {
    const val AUTH = "auth"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val DASHBOARD = "dashboard"
    const val KELOLA_OUTLET = "kelola_outlet"
    const val LOG_ABSENSI = "log_absensi"
    const val LAPORAN_KEUANGAN = "laporan_keuangan"
    const val STATUS_KARYAWAN = "status_karyawan"
    const val TUGAS_TIM = "tugas_tim"
    const val PESAN = "pesan"
    const val DIVISI = "divisi"
    const val DATA_PERSETUJUAN = "data_persetujuan"
    const val TAMBAH_KARYAWAN = "tambah_karyawan"
    const val KELOLA_SHIFT = "kelola_shift"
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
        composable(Routes.PESAN) {
            PlaceholderScreen(title = "Pesan")
        }
        composable(Routes.DIVISI) {
            PlaceholderScreen(title = "Divisi")
        }
        composable(Routes.DATA_PERSETUJUAN) {
            PlaceholderScreen(title = "Data Persetujuan")
        }
        composable(Routes.TAMBAH_KARYAWAN) {
            PlaceholderScreen(title = "Tambah Karyawan")
        }
        composable(Routes.KELOLA_SHIFT) {
            PlaceholderScreen(title = "Kelola Shift")
        }
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
