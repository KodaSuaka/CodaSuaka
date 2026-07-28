package com.example.codasuaka.ui.screen.dashboard_karyawan

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.codasuaka.data.remote.dto.JadwalDto
import com.example.codasuaka.ui.components.CodaSuakaNavbar
import com.example.codasuaka.ui.components.CodaSuakaSnackbarHost
import com.example.codasuaka.ui.components.NavbarItem
import com.example.codasuaka.ui.components.NotificationBannerStatic
import com.example.codasuaka.ui.screen.notifikasi.NotificationSidebar
import com.example.codasuaka.ui.screen.notifikasi.NotificationViewModel
import com.example.codasuaka.ui.theme.*
import com.example.codasuaka.util.ErrorMessageMapper
import com.example.codasuaka.util.ClickHelper
import com.example.codasuaka.util.DateTimeUtil
import org.koin.androidx.compose.koinViewModel
import java.time.format.DateTimeFormatter

// ─── Color Palette Tambahan (Fresh & Soft) ─────
private val Teal = Color(0xFF2DD4BF)      // Soft Teal
private val OceanBlue = Color(0xFF60A5FA) // Ocean Blue (turunan Primary)
private val Mint = Color(0xFF34D399)      // Mint Green
private val Amber = Color(0xFFFBBF24)     // Soft Amber
private val Coral = Color(0xFFF87171)     // Soft Coral
private val ScoreGreen = Color(0xFF10B981)

// ─── DashboardKaryawanScreen ──────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardKaryawanScreen(
    onNavigateTo: (String) -> Unit,
    onLogout: () -> Unit,
    viewModel: DashboardKaryawanViewModel,
    notificationViewModel: NotificationViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val notificationUiState by notificationViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // ── Handle messages via Snackbar ──
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Tertiary, // Fix: Ensure gaps around floating navbar are not dark
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Dashboard Karyawan",
                        fontWeight = FontWeight.Bold,
                        color = Secondary
                    )
                },
                actions = {
                    IconButton(onClick = { notificationViewModel.toggleSidebar(true) }) {
                        BadgedBox(
                            badge = {
                                if (notificationUiState.unreadCount > 0) {
                                    Badge(
                                        containerColor = Error,
                                        modifier = Modifier.size(16.dp).offset(x = (-4).dp, y = 4.dp)
                                    ) {
                                        Text(
                                            text = if (notificationUiState.unreadCount > 99) "9+" else notificationUiState.unreadCount.toString(),
                                            fontSize = 9.sp,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifikasi",
                                tint = Primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Surface
                )
            )
        },
        bottomBar = {
            CodaSuakaNavbar(
                items = listOf(
                    NavbarItem(
                        selectedIcon = Icons.Default.Home, 
                        unselectedIcon = Icons.Outlined.Home, 
                        label = "Beranda", 
                        index = 0
                    ),
                    NavbarItem(
                        selectedIcon = Icons.AutoMirrored.Filled.Assignment, 
                        unselectedIcon = Icons.AutoMirrored.Outlined.Assignment, 
                        label = "Pengajuan", 
                        index = 1
                    ),
                    NavbarItem(
                        selectedIcon = Icons.Default.ChatBubble, 
                        unselectedIcon = Icons.Outlined.ChatBubbleOutline, 
                        label = "Pesan", 
                        index = 2, 
                        hasBadge = notificationUiState.unreadCount > 0
                    )
                ),
                selectedIndex = uiState.selectedBottomNav,
                onItemSelected = { index ->
                    viewModel.onBottomNavSelected(index)
                    when (index) {
                        0 -> { /* Beranda */ }
                        1 -> onNavigateTo("pengajuan")
                        2 -> onNavigateTo("contact_list")
                    }
                }
            )
        },
        snackbarHost = { CodaSuakaSnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Tertiary)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Loading Indicator ──
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Primary)
                }
            }

            // ── Error Message (User-Friendly Notification) ──
            // Moved to SnackbarHost

            // ══════════════════════════════════════════════════
            // 1. Data Diri Karyawan
            // ══════════════════════════════════════════════════
            SectionEmployeeInfo(
                employee = uiState.employeeInfo
            )

            // ══════════════════════════════════════════════════
            // 2. Presensi Hari Ini (1 kartu terpadu)
            // ══════════════════════════════════════════════════
            SectionPresensiToday(
                absensiStatus = uiState.absensiStatus,
                absensiTime = uiState.absensiTime,
                statusKeterangan = uiState.statusKeterangan,
                specialEvent = uiState.specialEvent,
                showSpecialEvent = uiState.showSpecialEvent,
                isLoading = uiState.isLoading,
                onCheckClick = { viewModel.toggleAbsensi() },
                onRiwayatPresensiClick = { onNavigateTo("riwayat_kehadiran") },
                onJadwalShiftClick = { viewModel.toggleJadwalDialog(true) }
            )

            // ── Popup Daftar Jadwal/Event ──
            if (uiState.showJadwalDialog) {
                DialogDaftarJadwal(
                    jadwalList = uiState.jadwalList,
                    onDismiss = { viewModel.toggleJadwalDialog(false) }
                )
            }

            // ══════════════════════════════════════════════════
            // 3. Menu Jabatan
            // ══════════════════════════════════════════════════
            SectionRoleMenu(
                items = uiState.roleMenuItems,
                onItemClick = { route ->
                    if (route != null) onNavigateTo(route)
                }
            )

            // ══════════════════════════════════════════════════
            // 4. Poin Kinerja
            // ══════════════════════════════════════════════════
            SectionPerformance(
                poinKinerja = uiState.poinKinerja,
                onDetailKinerjaClick = { onNavigateTo("poin_kinerja") }
            )

            // ══════════════════════════════════════════════════
            // 4b. Notifikasi Tugas Aktif
            // ══════════════════════════════════════════════════
            SectionTaskNotification(
                daftarTugas = uiState.daftarTugas,
                totalTugas = uiState.totalTugas,
                tugasSelesai = uiState.tugasSelesai
            )

            // ══════════════════════════════════════════════════
            // 5. Sisa Cuti
            // ══════════════════════════════════════════════════
            SectionLeave(
                sisaCuti = uiState.sisaCuti,
                onSisaCutiClick = { onNavigateTo("pengajuan") }
            )

            // ══════════════════════════════════════════════════
            // 6. Logout
            // ══════════════════════════════════════════════════            // 6. Logout
            var showLogoutConfirm by remember { mutableStateOf(false) }

            Button(
                onClick = { showLogoutConfirm = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Coral.copy(alpha = 0.1f),
                    contentColor = Coral
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Coral
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Keluar dari Akun",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Coral
                )
            }

            // ── Konfirmasi Logout (Redesign) ──
            if (showLogoutConfirm) {
                Dialog(onDismissRequest = { showLogoutConfirm = false }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = Surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(Coral.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Logout,
                                    contentDescription = null,
                                    tint = Coral,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Text(
                                text = "Keluar Akun",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Secondary
                            )

                            Text(
                                text = "Apakah Anda yakin ingin keluar dari aplikasi Coda Suaka?",
                                style = MaterialTheme.typography.bodyMedium,
                                color = OnSurfaceVariant,
                                textAlign = TextAlign.Center
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { showLogoutConfirm = false },
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, Neutral)
                                ) {
                                    Text("Batal", color = OnSurfaceVariant)
                                }

                                Button(
                                    onClick = {
                                        showLogoutConfirm = false
                                        onLogout()
                                    },
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Coral)
                                ) {
                                    Text("Keluar", color = OnPrimary)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    // ── Sidebar Notifikasi (Overlay) ──
    NotificationSidebar(
        uiState = notificationUiState,
        onClose = { notificationViewModel.toggleSidebar(false) },
        onMarkAsRead = { notificationViewModel.markAsRead(it) },
        onMarkAllAsRead = { notificationViewModel.markAllAsRead() },
        onRefresh = { notificationViewModel.refresh() }
    )
}

// ═══════════════════════════════════════════════════════════════
// 1. Data Diri Karyawan
// ═══════════════════════════════════════════════════════════════

@Composable
private fun SectionEmployeeInfo(
    employee: EmployeeInfo
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = OnPrimary,
                    modifier = Modifier.size(36.dp)
                )
            }

            // Nama, Jabatan, Poin Performa
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = employee.nama,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = employee.jabatan,
                    style = MaterialTheme.typography.bodyLarge,
                    color = OnSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Badge Poin Performa
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val scoreFraction = (employee.poinPerforma / 100f).coerceIn(0f, 1f)
                    val scoreColor = when {
                        employee.poinPerforma >= 80 -> ScoreGreen
                        employee.poinPerforma >= 60 -> Amber
                        else -> Coral
                    }

                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Neutral)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(fraction = scoreFraction)
                                .clip(RoundedCornerShape(4.dp))
                                .background(scoreColor)
                        )
                    }

                    Text(
                        text = "${employee.poinPerforma} Poin",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = scoreColor
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// 2. Presensi Hari Ini (1 kartu terpadu)
// ═══════════════════════════════════════════════════════════════

@Composable
private fun SectionPresensiToday(
    absensiStatus: AbsensiStatus,
    absensiTime: String?,
    statusKeterangan: String?,
    specialEvent: String?,
    showSpecialEvent: Boolean,
    isLoading: Boolean,
    onCheckClick: () -> Unit,
    onRiwayatPresensiClick: () -> Unit,
    onJadwalShiftClick: () -> Unit
) {
    val isCheckedIn = absensiStatus == AbsensiStatus.CHECKED_IN
    val isCompleted = absensiStatus == AbsensiStatus.COMPLETED

    val statusText = when (absensiStatus) {
        AbsensiStatus.CHECKED_IN -> "Sudah Check-in"
        AbsensiStatus.COMPLETED -> "Sudah Selesai Absen"
        else -> "Belum Check-in"
    }

    val detailText = when (absensiStatus) {
        AbsensiStatus.CHECKED_IN -> "Masuk pukul ${absensiTime ?: "-"}"
        AbsensiStatus.COMPLETED -> "Jam Kerja: $absensiTime"
        else -> "Belum ada catatan"
    }

    val statusColor = when (absensiStatus) {
        AbsensiStatus.CHECKED_IN -> ScoreGreen
        AbsensiStatus.COMPLETED -> OceanBlue
        else -> OnSurfaceVariant
    }

    val buttonText = when (absensiStatus) {
        AbsensiStatus.CHECKED_IN -> "Check-out"
        AbsensiStatus.COMPLETED -> "Selesai"
        else -> "Check-in"
    }

    val buttonColor = when (absensiStatus) {
        AbsensiStatus.CHECKED_IN -> Coral
        else -> ScoreGreen
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Judul Section
        Text(
            text = "Presensi Hari Ini",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = OnSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp)
        )

        // 1 kartu terpadu
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                val today = java.time.LocalDate.now()

                // ── Tanggal ──
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(18.dp)
                    )
                    val indonesianMonths = listOf(
                        "Januari", "Februari", "Maret", "April", "Mei", "Juni",
                        "Juli", "Agustus", "September", "Oktober", "November", "Desember"
                    )
                    val indonesianDays = listOf(
                        "Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu"
                    )
                    val dayName = indonesianDays[today.dayOfWeek.value - 1]
                    val monthName = indonesianMonths[today.monthValue - 1]
                    Text(
                        text = "$dayName, ${today.dayOfMonth} $monthName ${today.year}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Primary
                    )
                }

                // ── Jadwal Shift Info ──
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(OceanBlue.copy(alpha = 0.1f))
                        .padding(12.dp, 12.dp, 16.dp, 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(OceanBlue.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = OceanBlue,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Hari Ini",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = OnSurface
                        )
                        Text(
                            text = today.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("id", "ID"))
                                .replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceVariant
                        )
                    }
                    Text(
                        text = "Lihat Jadwal",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = OceanBlue,
                        modifier = Modifier
                            .clip(RoundedCornerShape(99.dp))
                            .background(OceanBlue.copy(alpha = 0.1f))
                            .clickable { onJadwalShiftClick() }
                            .padding(horizontal = 10.dp, vertical = 2.dp)
                    )
                }

                HorizontalDivider(color = Neutral)

                // ── Status Keterangan (Checkin/Checkout Timing) ──
                if (statusKeterangan != null) {
                    val (label, chipColor) = when (statusKeterangan) {
                        "tepat_waktu" -> "✅ Tepat Waktu" to ScoreGreen
                        "checkin_awal" -> "🔵 Check-in Awal" to OceanBlue
                        "checkin_terlambat" -> "🔴 Check-in Terlambat" to Coral
                        "checkout_awal" -> "🟡 Check-out Awal" to Amber
                        "checkout_terlambat" -> "🔴 Check-out Terlambat" to Coral
                        else -> statusKeterangan to OnSurfaceVariant
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(chipColor.copy(alpha = 0.1f))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(chipColor)
                        )
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = chipColor
                        )
                    }
                }

                // ── Status Absensi ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                        Column {
                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = OnSurface
                            )
                            Text(
                                text = detailText,
                                style = MaterialTheme.typography.labelSmall,
                                color = OnSurfaceVariant
                            )
                        }
                    }
                    Button(
                        onClick = onCheckClick,
                        enabled = !isCompleted && !isLoading,
                        shape = RoundedCornerShape(99.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = buttonColor,
                            disabledContainerColor = Neutral,
                            disabledContentColor = OnSurfaceVariant
                        ),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = buttonText,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                }

                // ── Event Spesial (kadang muncul) ──
                if (showSpecialEvent && specialEvent != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Amber.copy(alpha = 0.1f))
                            .padding(10.dp, 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Celebration,
                            contentDescription = null,
                            tint = Amber,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = specialEvent,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF92400E),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // ── Footer Buttons ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onRiwayatPresensiClick,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary,
                            contentColor = OnPrimary // Memastikan teks terlihat putih di atas biru
                        ),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = OnPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Riwayat",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = OnPrimary
                        )
                    }

                    OutlinedButton(
                        onClick = onJadwalShiftClick,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Primary
                        ),
                        border = BorderStroke(1.5.dp, Primary),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Jadwal",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Primary
                        )
                    }
                }
            }
        }
    }
}

// ─── Section Menu Jabatan ──────────────────────────────────────

@Composable
private fun SectionRoleMenu(
    items: List<RoleMenuItem>,
    onItemClick: (String?) -> Unit
) {
    if (items.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Menu Jabatan",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Secondary,
            modifier = Modifier.padding(start = 4.dp)
        )

        // Membagi items menjadi 2 kolom ke bawah (grid)
        items.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                rowItems.forEach { item ->
                    RoleMenuCard(
                        modifier = Modifier.weight(1f),
                        item = item,
                        onClick = { onItemClick(item.route) }
                    )
                }
                // Jika item ganjil, tambahkan spacer agar tetap seimbang
                if (rowItems.size < 2) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun RoleMenuCard(
    modifier: Modifier = Modifier,
    item: RoleMenuItem,
    onClick: () -> Unit
) {
    val icon = mapIcon(item.iconResName)
    
    // Tentukan warna ikon berdasarkan label menu agar variatif tapi setema
    val iconColor = when {
        item.label.contains("Tugas", ignoreCase = true) -> OceanBlue
        item.label.contains("Absensi", ignoreCase = true) -> Teal
        item.label.contains("Pengajuan", ignoreCase = true) -> Mint
        else -> Primary
    }

    Card(
        onClick = onClick,
        modifier = modifier.height(115.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, Neutral)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(iconColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = item.label,
                    tint = iconColor,
                    modifier = Modifier.size(32.dp)
                )
            }

            Text(
                text = item.label,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    lineHeight = 14.sp
                ),
                color = Secondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// 4. Poin Kinerja
// ═══════════════════════════════════════════════════════════════

@Composable
private fun SectionPerformance(
    poinKinerja: Int,
    onDetailKinerjaClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Poin Kinerja",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = OnSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp)
        )

        Card(
            onClick = onDetailKinerjaClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "$poinKinerja",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = ScoreGreen
                        )

                        Text(
                            text = "/ 100",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurfaceVariant
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Detail",
                    tint = OnSurfaceVariant,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// 4b. Notifikasi Tugas Aktif (Compact Banner)
// ═══════════════════════════════════════════════════════════════

@Composable
private fun SectionTaskNotification(
    daftarTugas: List<TugasItem>,
    totalTugas: Int,
    tugasSelesai: Int
) {
    if (daftarTugas.isEmpty()) return

    val tugasHarian = daftarTugas.filter { !it.isTugasKhusus && !it.isSelesai }
    val tugasKhusus = daftarTugas.filter { it.isTugasKhusus && !it.isSelesai }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = PrimaryLight.copy(alpha = 0.15f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Assignment,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Tugas Hari Ini",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Secondary
                )
                Spacer(modifier = Modifier.weight(1f))
                if (totalTugas > 0) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Primary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "$tugasSelesai/$totalTugas",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Primary,
                            modifier = Modifier.padding(
                                horizontal = 8.dp,
                                vertical = 3.dp
                            )
                        )
                    }
                }
            }

            // Tugas Harian (dari template)
            if (tugasHarian.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = BlueSchedule.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "📋 ${tugasHarian.size} tugas harian",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = BlueSchedule,
                            modifier = Modifier.padding(
                                horizontal = 8.dp,
                                vertical = 3.dp
                            )
                        )
                    }
                    // Ringkas judul pertama
                    if (tugasHarian.size <= 2) {
                        tugasHarian.forEach { t ->
                            Text(
                                text = t.judul,
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    } else {
                        Text(
                            text = "${tugasHarian.first().judul} +${tugasHarian.size - 1} lagi",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Tugas Khusus (dari pemilik)
            if (tugasKhusus.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = OrangeManage.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "⭐ ${tugasKhusus.size} tugas khusus",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = OrangeManage,
                            modifier = Modifier.padding(
                                horizontal = 8.dp,
                                vertical = 3.dp
                            )
                        )
                    }
                    if (tugasKhusus.size <= 2) {
                        tugasKhusus.forEach { t ->
                            Text(
                                text = t.judul,
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    } else {
                        Text(
                            text = "${tugasKhusus.first().judul} +${tugasKhusus.size - 1} lagi",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// 5. Sisa Cuti
// ═══════════════════════════════════════════════════════════════

@Composable
private fun SectionLeave(
    sisaCuti: Int,
    onSisaCutiClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Sisa Cuti",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = OnSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp)
        )

        Card(
            onClick = onSisaCutiClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(OceanBlue.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.BeachAccess,
                        contentDescription = "Sisa Cuti",
                        tint = OceanBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Sisa Cuti Tahunan",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )

                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "$sisaCuti",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )

                        Text(
                            text = "hari",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurfaceVariant,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Detail",
                    tint = OnSurfaceVariant,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// DIALOG — Daftar Jadwal (Popup Event)
// ═══════════════════════════════════════════════════════════════

@Composable
private fun DialogDaftarJadwal(
    jadwalList: List<JadwalDto>,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(OceanBlue.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.EventNote,
                                contentDescription = null,
                                tint = OceanBlue,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Text(
                            text = "Agenda & Event",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Secondary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = OnSurfaceVariant)
                    }
                }

                HorizontalDivider(color = Neutral)

                // List
                if (jadwalList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Tidak ada agenda atau event bulan ini.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
                    val sortedJadwal = jadwalList.sortedBy { it.tanggal }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        sortedJadwal.forEach { jadwal ->
                            JadwalItemRow(jadwal, dateFormatter)
                        }
                    }
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text("Tutup", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun JadwalItemRow(
    jadwal: JadwalDto,
    formatter: DateTimeFormatter
) {
    val dateText = DateTimeUtil.formatIsoToLocal(jadwal.tanggal)

    val categoryColor = when (jadwal.kategori.lowercase()) {
        "libur" -> Coral
        "tugas" -> OrangeManage
        else -> OceanBlue
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Neutral.copy(alpha = 0.3f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(categoryColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            val icon = when (jadwal.kategori.lowercase()) {
                "libur" -> Icons.Default.Info
                "tugas" -> Icons.Default.Assignment
                else -> Icons.Default.Star
            }
            Icon(imageVector = icon, contentDescription = null, tint = categoryColor, modifier = Modifier.size(20.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = jadwal.namaEvent,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = dateText,
                style = MaterialTheme.typography.labelSmall,
                color = OnSurfaceVariant
            )
        }

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = categoryColor.copy(alpha = 0.1f)
        ) {
            Text(
                text = jadwal.kategori.replaceFirstChar { it.uppercase() },
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = categoryColor
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// UTILITY — Map icon name to ImageVector
// ═══════════════════════════════════════════════════════════════

private fun mapIcon(iconName: String): ImageVector {
    return when (iconName) {
        "Description" -> Icons.Default.Description
        "FactCheck" -> Icons.Default.FactCheck
        "Assignment" -> Icons.Default.Assignment
        "School" -> Icons.Default.School
        "EmojiEvents" -> Icons.Default.EmojiEvents
        "People" -> Icons.Default.People
        "PeopleAlt" -> Icons.Default.PeopleAlt
        "AccountBalance" -> Icons.Default.AccountBalance
        "Settings" -> Icons.Default.Settings
        "Receipt" -> Icons.Default.Receipt
        "Schedule" -> Icons.Default.Schedule
        "Book" -> Icons.Default.Book
        "Groups" -> Icons.Default.Groups
        "Store" -> Icons.Default.Store
        "Person" -> Icons.Default.Person
        "Star" -> Icons.Default.Star
        "Favorite" -> Icons.Default.Favorite
        "TrendingUp" -> Icons.Default.TrendingUp
        else -> Icons.Default.List
    }
}
