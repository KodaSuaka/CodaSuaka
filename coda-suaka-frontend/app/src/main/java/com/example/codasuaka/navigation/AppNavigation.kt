package com.example.codasuaka.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import org.koin.androidx.compose.get
import org.koin.androidx.compose.koinViewModel
import com.example.codasuaka.data.local.TokenManager
import com.example.codasuaka.util.ClickHelper
import com.example.codasuaka.ui.screen.auth.AuthScreen
import com.example.codasuaka.ui.screen.auth.AuthViewModel
import com.example.codasuaka.ui.screen.pengajuan.PengajuanScreen
import com.example.codasuaka.ui.screen.pengajuan.PengajuanViewModel
import com.example.codasuaka.ui.screen.divisi.DivisiScreen
import com.example.codasuaka.ui.screen.divisi.DivisiViewModel
import com.example.codasuaka.ui.screen.kelola_outlet.KelolaOutletScreen
import com.example.codasuaka.ui.screen.kelola_outlet.KelolaOutletViewModel
import com.example.codasuaka.ui.screen.kelola_barang_jasa.KelolaBarangJasaScreen
import com.example.codasuaka.ui.screen.kelola_barang_jasa.KelolaBarangJasaViewModel
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
import com.example.codasuaka.ui.screen.laporan_keuangan.LaporanKeuanganViewModel
import com.example.codasuaka.ui.screen.approval_keuangan.ApprovalKeuanganScreen
import com.example.codasuaka.ui.screen.approval_keuangan.ApprovalKeuanganViewModel
import com.example.codasuaka.ui.screen.notifikasi.NotificationViewModel
import com.example.codasuaka.ui.screen.poin_kinerja.PoinKinerjaScreen
import com.example.codasuaka.ui.screen.poin_kinerja.PoinKinerjaViewModel
import com.example.codasuaka.ui.screen.penugasan.PenugasanScreen
import com.example.codasuaka.ui.screen.penugasan.PenugasanViewModel
import com.example.codasuaka.ui.screen.receipt_settings.ReceiptSettingsScreen
import com.example.codasuaka.ui.screen.receipt_settings.ReceiptSettingsViewModel
import com.example.codasuaka.ui.screen.jam_operasional.JamOperasionalScreen
import com.example.codasuaka.ui.screen.jam_operasional.JamOperasionalViewModel
import com.example.codasuaka.ui.screen.kasir.KasirScreen
import com.example.codasuaka.ui.screen.kasir.KasirViewModel
import com.example.codasuaka.ui.screen.nota_pembelian.NotaPembelianScreen
import com.example.codasuaka.ui.screen.nota_pembelian.NotaPembelianViewModel
import com.example.codasuaka.ui.screen.riwayat_nota.RiwayatNotaScreen
import com.example.codasuaka.ui.screen.riwayat_nota.RiwayatNotaViewModel
import com.example.codasuaka.ui.screen.nota_detail.NotaDetailScreen
import com.example.codasuaka.ui.screen.nota_detail.NotaDetailViewModel
import com.example.codasuaka.ui.screen.stok.StokScreen
import com.example.codasuaka.ui.screen.stok.StokViewModel
import com.example.codasuaka.ui.screen.login.LoginScreen
import com.example.codasuaka.ui.screen.login.LoginViewModel
import com.example.codasuaka.ui.screen.register.RegisterScreen
import com.example.codasuaka.ui.screen.register.RegisterViewModel
import org.koin.core.parameter.parametersOf
import java.net.URLDecoder
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
    const val APPROVAL_KEUANGAN = "approval_keuangan"
    const val POIN_KINERJA = "poin_kinerja"
    const val PENUGASAN = "penugasan"
    const val JAM_OPERASIONAL = "jam_operasional"
    const val KASIR = "kasir"
    const val KELOLA_BARANG_JASA = "kelola_barang_jasa"
    const val NOTA_PEMBELIAN = "nota_pembelian"
    const val RIWAYAT_NOTA = "riwayat_nota"
    const val NOTA_DETAIL = "nota_detail/{notaId}"
    const val STOK = "stok"
    const val RECEIPT_SETTINGS = "receipt_settings"

    fun chatDetail(userId: Int, userName: String): String {
        val encodedName = URLEncoder.encode(userName, "UTF-8")
        return "chat_detail/$userId/$encodedName"
    }

    fun notaDetail(notaId: Int): String = "nota_detail/$notaId"

    /**
     * Dashboard tujuan berdasarkan role — dipakai baik oleh alur login baru
     * maupun gatekeeper AUTH (resume tanpa logout), supaya keduanya konsisten
     * dan Karyawan tidak pernah diarahkan ke Dashboard Owner.
     */
    private val FUNCTIONAL_ROLES = listOf("Keuangan", "Manager", "Staff", "Karyawan")

    fun dashboardForRole(role: String): String =
        if (role in FUNCTIONAL_ROLES) DASHBOARD_KARYAWAN else DASHBOARD
}

@Composable
fun AppNavigation(navController: NavHostController) {
    // Helper untuk navigasi balik dengan proteksi anti-spam
    val safePopBackStack = {
        if (ClickHelper.canClick()) {
            navController.popBackStack()
        }
    }

    // Helper untuk navigasi maju dengan proteksi anti-spam
    val safeNavigate: (String) -> Unit = { route ->
        if (ClickHelper.canClick()) {
            try {
                navController.navigate(route) {
                    launchSingleTop = true
                }
            } catch (_: Exception) {}
        }
    }

    // Tombol back fisik/gestur tidak melewati safePopBackStack secara default,
    // jadi spam-tap bisa memicu banyak pop berturut-turut lebih cepat dari
    // recomposition/ViewModel state sempat settle. Rutekan lewat debounce yang
    // sama dengan tombol back di layar. `enabled` mengikuti apakah masih ada
    // entry sebelumnya, supaya di layar akar back tetap keluar app seperti biasa.
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val canPopBack = navController.previousBackStackEntry != null
    BackHandler(enabled = canPopBack) {
        safePopBackStack()
    }

    // Sesi habis (401) di mana pun → kembali ke Login, bersihkan seluruh back stack.
    val tokenManager: TokenManager = get()
    LaunchedEffect(Unit) {
        tokenManager.sessionExpired.collect {
            navController.navigate(Routes.LOGIN) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.AUTH,
        enterTransition = {
            slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(400)) + fadeIn(animationSpec = tween(400))
        },
        exitTransition = {
            slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(400)) + fadeOut(animationSpec = tween(400))
        },
        popEnterTransition = {
            slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(400)) + fadeIn(animationSpec = tween(400))
        },
        popExitTransition = {
            slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(400)) + fadeOut(animationSpec = tween(400))
        }
    ) {
        // ── Auth Check (gatekeeper) ──
        composable(Routes.AUTH) {
            val authViewModel: AuthViewModel = koinViewModel()
            AuthScreen(
                viewModel = authViewModel,
                onAuthenticated = { role ->
                    // Route berdasarkan role saat ini — sebelumnya selalu ke
                    // DASHBOARD (Owner) tanpa cek role, jadi Karyawan yang
                    // resume tanpa logout ikut mendapat akses Dashboard Owner.
                    navController.navigate(Routes.dashboardForRole(role)) {
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
                onLoginSuccess = { role, permissions ->
                    navController.navigate(Routes.dashboardForRole(role)) {
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
                    safePopBackStack()
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
                onNavigateTo = { route -> safeNavigate(route) },
                onLogout = {
                    if (ClickHelper.canClick()) {
                        authViewModel.logout()
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }

        // ── Dashboard Karyawan ──
        composable(Routes.DASHBOARD_KARYAWAN) {
            val dashboardKaryawanViewModel: DashboardKaryawanViewModel = koinViewModel()
            val authViewModel: AuthViewModel = koinViewModel()
            DashboardKaryawanScreen(
                onNavigateTo = { route -> safeNavigate(route) },
                onLogout = {
                    if (ClickHelper.canClick()) {
                        authViewModel.logout()
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
                viewModel = dashboardKaryawanViewModel
            )
        }

        // ── Kelola Outlet ──
        composable(Routes.KELOLA_OUTLET) {
            val kelolaOutletViewModel: KelolaOutletViewModel = koinViewModel()
            KelolaOutletScreen(
                onBack = { safePopBackStack() },
                viewModel = kelolaOutletViewModel
            )
        }

        // ── Kelola Barang/Jasa ──
        composable(Routes.KELOLA_BARANG_JASA) {
            val kelolaBarangJasaViewModel: KelolaBarangJasaViewModel = koinViewModel()
            KelolaBarangJasaScreen(
                onBack = { safePopBackStack() },
                viewModel = kelolaBarangJasaViewModel
            )
        }

        // ── Kelola Karyawan ──
        composable(Routes.KELOLA_KARYAWAN) {
            val kelolaKaryawanViewModel: KelolaKaryawanViewModel = koinViewModel()
            KelolaKaryawanScreen(
                onBack = { safePopBackStack() },
                viewModel = kelolaKaryawanViewModel
            )
        }

        // ── Kalender / Jadwal ──
        composable(Routes.KALENDER) {
            val kalenderViewModel: KalenderViewModel = koinViewModel()
            KalenderScreen(
                onBack = { safePopBackStack() },
                viewModel = kalenderViewModel
            )
        }

        // ── Riwayat Kehadiran ──
        composable(Routes.RIWAYAT_KEHADIRAN) {
            val riwayatKehadiranViewModel: RiwayatKehadiranViewModel = koinViewModel()
            RiwayatKehadiranScreen(
                onBack = { safePopBackStack() },
                viewModel = riwayatKehadiranViewModel
            )
        }

        // ── Contact List (Chat) ──
        composable(Routes.CONTACT_LIST) {
            val chatContactViewModel: ChatContactViewModel = koinViewModel()
            ChatContactListScreen(
                onBack = { safePopBackStack() },
                onContactClick = { userId, userName ->
                    if (ClickHelper.canClick()) {
                        navController.navigate(Routes.chatDetail(userId, userName))
                    }
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
            val rawUserName = backStackEntry.arguments?.getString("userName") ?: "User"
            val userName = try {
                URLDecoder.decode(rawUserName, "UTF-8")
            } catch (_: Exception) {
                rawUserName
            }
            
            val chatDetailViewModel: ChatDetailViewModel = koinViewModel(
                parameters = { parametersOf(userId, userName) }
            )
            ChatDetailScreen(
                onBack = { safePopBackStack() },
                viewModel = chatDetailViewModel
            )
        }

        // ── Divisi ──
        composable(Routes.DIVISI) {
            val divisiViewModel: DivisiViewModel = koinViewModel()
            DivisiScreen(
                onBack = { safePopBackStack() },
                viewModel = divisiViewModel
            )
        }

        // ── Tambah Karyawan (alias ke KelolaKaryawan) ──
        composable(Routes.TAMBAH_KARYAWAN) {
            val kelolaKaryawanViewModel: KelolaKaryawanViewModel = koinViewModel()
            KelolaKaryawanScreen(
                onBack = { safePopBackStack() },
                viewModel = kelolaKaryawanViewModel
            )
        }

        // ── Pengajuan (Cuti/Izin) ──
        composable(Routes.PENGAJUAN) {
            val pengajuanViewModel: PengajuanViewModel = koinViewModel()
            PengajuanScreen(
                onBack = { safePopBackStack() },
                viewModel = pengajuanViewModel
            )
        }

        // ── Log Absensi (redirect ke Riwayat Kehadiran) ──
        composable(Routes.LOG_ABSENSI) {
            val riwayatKehadiranViewModel: RiwayatKehadiranViewModel = koinViewModel()
            RiwayatKehadiranScreen(
                onBack = { safePopBackStack() },
                viewModel = riwayatKehadiranViewModel
            )
        }

        // ── Buku Kas (Laporan Keuangan) ──
        composable(Routes.LAPORAN_KEUANGAN) {
            val laporanKeuanganViewModel: LaporanKeuanganViewModel = koinViewModel()
            LaporanKeuanganScreen(
                onBack = { safePopBackStack() },
                viewModel = laporanKeuanganViewModel
            )
        }

        // ── Approval Keuangan ──
        composable(Routes.APPROVAL_KEUANGAN) {
            val approvalKeuanganViewModel: ApprovalKeuanganViewModel = koinViewModel()
            ApprovalKeuanganScreen(
                onBack = { safePopBackStack() },
                viewModel = approvalKeuanganViewModel
            )
        }

        // ── Poin Kinerja ──
        composable(Routes.POIN_KINERJA) {
            val poinKinerjaViewModel: PoinKinerjaViewModel = koinViewModel()
            PoinKinerjaScreen(
                onBack = { safePopBackStack() },
                viewModel = poinKinerjaViewModel
            )
        }


        // ── Penugasan ──
        composable(Routes.PENUGASAN) {
            val penugasanViewModel: PenugasanViewModel = koinViewModel()
            PenugasanScreen(
                onBack = { safePopBackStack() },
                viewModel = penugasanViewModel
            )
        }

        // ── Jam Operasional ──
        composable(Routes.JAM_OPERASIONAL) {
            val jamOperasionalViewModel: JamOperasionalViewModel = koinViewModel()
            JamOperasionalScreen(
                onBack = { safePopBackStack() },
                viewModel = jamOperasionalViewModel
            )
        }

        // ── Kasir ──
        composable(Routes.KASIR) {
            val kasirViewModel: KasirViewModel = koinViewModel()
            KasirScreen(
                onBack = { safePopBackStack() },
                onNavigateTo = { route -> safeNavigate(route) },
                viewModel = kasirViewModel
            )
        }

        // ── Nota Pembelian ──
        composable(Routes.NOTA_PEMBELIAN) {
            val notaPembelianViewModel: NotaPembelianViewModel = koinViewModel()
            NotaPembelianScreen(
                onBack = { safePopBackStack() },
                viewModel = notaPembelianViewModel
            )
        }

        // ── Riwayat Nota ──
        composable(Routes.RIWAYAT_NOTA) {
            val riwayatNotaViewModel: RiwayatNotaViewModel = koinViewModel()
            RiwayatNotaScreen(
                onBack = { safePopBackStack() },
                onNotaClick = { notaId ->
                    if (ClickHelper.canClick()) {
                        navController.navigate(Routes.notaDetail(notaId))
                    }
                },
                viewModel = riwayatNotaViewModel
            )
        }

        // ── Stok ──
        composable(Routes.STOK) {
            val stokViewModel: StokViewModel = koinViewModel()
            StokScreen(
                onBack = { safePopBackStack() },
                viewModel = stokViewModel
            )
        }

        // ── Detail Nota ──
        composable(
            route = Routes.NOTA_DETAIL,
            arguments = listOf(
                navArgument("notaId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val notaId = backStackEntry.arguments?.getInt("notaId") ?: return@composable
            val notaDetailViewModel: NotaDetailViewModel = koinViewModel(
                parameters = { parametersOf(notaId) }
            )
            NotaDetailScreen(
                onBack = { safePopBackStack() },
                onDeleted = {
                    if (ClickHelper.canClick()) {
                        navController.popBackStack()
                    }
                },
                viewModel = notaDetailViewModel
            )
        }

        // ── Status Karyawan (redirect ke Kelola Karyawan) ──
        composable(Routes.STATUS_KARYAWAN) {
            val kelolaKaryawanViewModel: KelolaKaryawanViewModel = koinViewModel()
            KelolaKaryawanScreen(
                onBack = { safePopBackStack() },
                viewModel = kelolaKaryawanViewModel
            )
        }

        // ── Pengaturan Struk ──
        composable(Routes.RECEIPT_SETTINGS) {
            val receiptSettingsViewModel: ReceiptSettingsViewModel = koinViewModel()
            ReceiptSettingsScreen(
                onBack = { safePopBackStack() },
                viewModel = receiptSettingsViewModel
            )
        }
    }
}
