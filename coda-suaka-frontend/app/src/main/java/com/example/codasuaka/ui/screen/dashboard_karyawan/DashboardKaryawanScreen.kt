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
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
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
    val pullRefreshState = rememberPullToRefreshState()

    // Muat ulang data & reset navbar index
    com.example.codasuaka.util.OnResumeEffect { 
        viewModel.loadDashboardData() 
        viewModel.onBottomNavSelected(0)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Tertiary,
        topBar = {
            TopAppBar(
                title = { Text(text = "Dashboard Karyawan", fontWeight = FontWeight.Bold, color = Secondary) },
                actions = {
                    IconButton(onClick = { notificationViewModel.toggleSidebar(true) }) {
                        BadgedBox(
                            badge = {
                                if (notificationUiState.unreadCount > 0) {
                                    Badge(containerColor = Error, modifier = Modifier.size(16.dp).offset(x = (-4).dp, y = 4.dp)) {
                                        Text(text = if (notificationUiState.unreadCount > 99) "9+" else notificationUiState.unreadCount.toString(), fontSize = 9.sp, color = OnPrimary)
                                    }
                                }
                            }
                        ) { Icon(imageVector = Icons.Default.Notifications, contentDescription = "Notifikasi", tint = Primary) }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface)
            )
        },
        bottomBar = {
            CodaSuakaNavbar(
                items = listOf(
                    NavbarItem(selectedIcon = Icons.Default.Home, unselectedIcon = Icons.Outlined.Home, label = "Beranda", index = 0),
                    NavbarItem(selectedIcon = Icons.AutoMirrored.Filled.Assignment, unselectedIcon = Icons.AutoMirrored.Outlined.Assignment, label = "Pengajuan", index = 1),
                    NavbarItem(selectedIcon = Icons.Default.ChatBubble, unselectedIcon = Icons.Outlined.ChatBubbleOutline, label = "Pesan", index = 2, hasBadge = uiState.hasUnreadMessages)
                ),
                selectedIndex = uiState.selectedBottomNav,
                onItemSelected = { index ->
                    viewModel.onBottomNavSelected(index)
                    when (index) {
                        0 -> {}
                        1 -> onNavigateTo("pengajuan")
                        2 -> onNavigateTo("contact_list")
                    }
                }
            )
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.refreshDashboard() },
            state = pullRefreshState,
            modifier = Modifier.fillMaxSize().padding(innerPadding).background(Tertiary),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (uiState.isLoading && !uiState.isRefreshing) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Primary) }
                }

                if (uiState.errorMessage != null) {
                    NotificationBannerStatic(message = uiState.errorMessage ?: "", mapFromServer = true, onDismiss = { viewModel.clearError() })
                }

                SectionEmployeeInfo(employee = uiState.employeeInfo)
                SectionPresensiToday(
                    absensiStatus = uiState.absensiStatus, absensiTime = uiState.absensiTime, statusKeterangan = uiState.statusKeterangan, jamCheckinStandar = uiState.jamCheckinStandar,
                    specialEvent = uiState.specialEvent, showSpecialEvent = uiState.showSpecialEvent, isLoading = uiState.isLoading,
                    onCheckClick = { viewModel.toggleAbsensi() }, onRiwayatPresensiClick = { onNavigateTo("riwayat_kehadiran") }, onJadwalShiftClick = { viewModel.toggleJadwalDialog(true) }
                )

                if (uiState.showJadwalDialog) DialogDaftarJadwal(jadwalList = uiState.jadwalList, onDismiss = { viewModel.toggleJadwalDialog(false) })
                SectionRoleMenu(items = uiState.roleMenuItems, onItemClick = { route -> if (route != null) onNavigateTo(route) })
                SectionPerformance(poinKinerja = uiState.poinKinerja, onDetailKinerjaClick = { onNavigateTo("poin_kinerja") })
                SectionTaskNotification(daftarTugas = uiState.daftarTugas, totalTugas = uiState.totalTugas, tugasSelesai = uiState.tugasSelesai, onClick = { viewModel.toggleTugasDialog(true) })
                if (uiState.showTugasDialog) DialogDaftarTugas(tugasList = uiState.daftarTugas, onDismiss = { viewModel.toggleTugasDialog(false) })
                SectionLeave(sisaCuti = uiState.sisaCuti, onSisaCutiClick = { onNavigateTo("pengajuan") })

                var showLogoutConfirm by remember { mutableStateOf(false) }
                Button(onClick = { showLogoutConfirm = true }, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Coral.copy(alpha = 0.1f), contentColor = Coral), elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)) {
                    Icon(imageVector = Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(20.dp), tint = Coral)
                    Spacer(modifier = Modifier.width(10.dp)); Text(text = "Keluar dari Akun", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Coral)
                }

                if (showLogoutConfirm) {
                    Dialog(onDismissRequest = { showLogoutConfirm = false }) {
                        Card(modifier = Modifier.fillMaxWidth().padding(24.dp), shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = Surface), elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)) {
                            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                Box(modifier = Modifier.size(64.dp).clip(CircleShape).background(Coral.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) { Icon(imageVector = Icons.Default.Logout, contentDescription = null, tint = Coral, modifier = Modifier.size(32.dp)) }
                                Text(text = "Keluar Akun", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Secondary)
                                Text(text = "Apakah Anda yakin ingin keluar dari aplikasi Coda Suaka?", style = MaterialTheme.typography.bodyMedium, color = Secondary.copy(alpha = 0.6f), textAlign = TextAlign.Center)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    OutlinedButton(onClick = { showLogoutConfirm = false }, modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, Neutral)) { Text("Batal", color = Secondary.copy(alpha = 0.6f)) }
                                    Button(onClick = { showLogoutConfirm = false; onLogout() }, modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = Coral)) { Text("Keluar", color = OnPrimary) }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    NotificationSidebar(uiState = notificationUiState, onClose = { notificationViewModel.toggleSidebar(false) }, onMarkAsRead = { notificationViewModel.markAsRead(it) }, onMarkAllAsRead = { notificationViewModel.markAllAsRead() }, onRefresh = { notificationViewModel.refresh() })
}

@Composable
private fun SectionEmployeeInfo(employee: EmployeeInfo) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Surface), elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(modifier = Modifier.size(64.dp).clip(CircleShape).background(Primary), contentAlignment = Alignment.Center) { Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = OnPrimary, modifier = Modifier.size(36.dp)) }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = employee.nama, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Secondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = employee.jabatan, style = MaterialTheme.typography.bodyLarge, color = Secondary.copy(alpha = 0.7f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val scoreFraction = (employee.poinPerforma / 100f).coerceIn(0f, 1f)
                    val scoreColor = when { employee.poinPerforma >= 80 -> ScoreGreen; employee.poinPerforma >= 60 -> Amber; else -> Coral }
                    Box(modifier = Modifier.width(80.dp).height(8.dp).clip(RoundedCornerShape(4.dp)).background(Neutral)) { Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(fraction = scoreFraction).clip(RoundedCornerShape(4.dp)).background(scoreColor)) }
                    Text(text = "${employee.poinPerforma} Poin", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = scoreColor)
                }
            }
        }
    }
}

@Composable
private fun SectionPresensiToday(absensiStatus: AbsensiStatus, absensiTime: String?, statusKeterangan: String?, jamCheckinStandar: String? = null, specialEvent: String?, showSpecialEvent: Boolean, isLoading: Boolean, onCheckClick: () -> Unit, onRiwayatPresensiClick: () -> Unit, onJadwalShiftClick: () -> Unit) {
    val isCompleted = absensiStatus == AbsensiStatus.COMPLETED
    val statusText = when (absensiStatus) { AbsensiStatus.CHECKED_IN -> "Sudah Check-in"; AbsensiStatus.COMPLETED -> "Sudah Selesai Absen"; else -> "Belum Check-in" }
    val detailText = when (absensiStatus) { AbsensiStatus.CHECKED_IN -> "Masuk pukul ${absensiTime ?: "-"}"; AbsensiStatus.COMPLETED -> "Jam Kerja: $absensiTime"; else -> if (jamCheckinStandar != null) "Jadwal masuk: $jamCheckinStandar" else "Belum ada catatan" }
    val statusColor = when (absensiStatus) { AbsensiStatus.CHECKED_IN -> ScoreGreen; AbsensiStatus.COMPLETED -> OceanBlue; else -> Secondary.copy(alpha = 0.6f) }
    val buttonText = when (absensiStatus) { AbsensiStatus.CHECKED_IN -> "Check-out"; AbsensiStatus.COMPLETED -> "Selesai"; else -> "Check-in" }
    val buttonColor = when (absensiStatus) { AbsensiStatus.CHECKED_IN -> Coral; else -> ScoreGreen }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = "Presensi Hari Ini", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = Secondary.copy(alpha = 0.7f), modifier = Modifier.padding(start = 4.dp))
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Surface), elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                val today = java.time.LocalDate.now()
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(imageVector = Icons.Default.DateRange, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
                    Text(text = today.format(java.time.format.DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", java.util.Locale.forLanguageTag("id-ID"))), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = Primary)
                }
                Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(OceanBlue.copy(alpha = 0.1f)).padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(OceanBlue.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) { Icon(imageVector = Icons.Default.Schedule, contentDescription = null, tint = OceanBlue, modifier = Modifier.size(22.dp)) }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Hari Ini", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = Secondary)
                        Text(text = today.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.forLanguageTag("id-ID")), style = MaterialTheme.typography.labelSmall, color = Secondary.copy(alpha = 0.6f))
                    }
                    Text(text = "Lihat Jadwal", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = OceanBlue, modifier = Modifier.clip(RoundedCornerShape(99.dp)).background(OceanBlue.copy(alpha = 0.1f)).clickable { onJadwalShiftClick() }.padding(horizontal = 10.dp, vertical = 2.dp))
                }
                HorizontalDivider(color = Neutral)
                if (statusKeterangan != null) {
                    val (label, chipColor) = when (statusKeterangan) { "tepat_waktu" -> "✅ Tepat Waktu" to ScoreGreen; "checkin_awal" -> "🔵 Check-in Awal" to OceanBlue; "checkin_terlambat" -> "🔴 Check-in Terlambat" to Coral; "checkout_awal" -> "🟡 Check-out Awal" to Amber; "checkout_terlambat" -> "🔴 Check-out Terlambat" to Coral; else -> statusKeterangan to Secondary.copy(alpha = 0.6f) }
                    Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(chipColor.copy(alpha = 0.1f)).padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) { Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(chipColor)); Text(text = label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = chipColor) }
                }
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(statusColor))
                        Column { Text(text = statusText, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = Secondary); Text(text = detailText, style = MaterialTheme.typography.labelSmall, color = Secondary.copy(alpha = 0.6f)) }
                    }
                    Button(onClick = onCheckClick, enabled = !isCompleted && !isLoading, shape = RoundedCornerShape(99.dp), colors = ButtonDefaults.buttonColors(containerColor = buttonColor, disabledContainerColor = Neutral, disabledContentColor = Secondary.copy(alpha = 0.6f)), contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)) { Text(text = buttonText, fontWeight = FontWeight.SemiBold, fontSize = 13.sp) }
                }
                if (showSpecialEvent && specialEvent != null) {
                    Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Amber.copy(alpha = 0.1f)).padding(10.dp, 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) { Icon(imageVector = Icons.Default.Celebration, contentDescription = null, tint = Amber, modifier = Modifier.size(18.dp)); Text(text = specialEvent, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium, color = Color(0xFF92400E), maxLines = 2, overflow = TextOverflow.Ellipsis) }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = onRiwayatPresensiClick, modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = OnPrimary), contentPadding = PaddingValues(vertical = 14.dp)) { Icon(imageVector = Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp), tint = OnPrimary); Spacer(modifier = Modifier.width(6.dp)); Text(text = "Riwayat", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = OnPrimary) }
                    OutlinedButton(onClick = onJadwalShiftClick, modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary), border = BorderStroke(1.5.dp, Primary), contentPadding = PaddingValues(vertical = 14.dp)) { Icon(imageVector = Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp), tint = Primary); Spacer(modifier = Modifier.width(6.dp)); Text(text = "Jadwal", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Primary) }
                }
            }
        }
    }
}

@Composable
private fun SectionRoleMenu(items: List<RoleMenuItem>, onItemClick: (String?) -> Unit) {
    if (items.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(text = "Menu Jabatan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Secondary, modifier = Modifier.padding(start = 4.dp) )
        items.chunked(2).forEach { rowItems ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                rowItems.forEach { item -> RoleMenuCard(modifier = Modifier.weight(1f), item = item, onClick = { onItemClick(item.route) }) }
                if (rowItems.size < 2) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun RoleMenuCard(modifier: Modifier = Modifier, item: RoleMenuItem, onClick: () -> Unit) {
    val icon = mapIcon(item.iconResName)
    val iconColor = when { item.label.contains("Tugas", ignoreCase = true) -> OceanBlue; item.label.contains("Absensi", ignoreCase = true) -> Teal; item.label.contains("Pengajuan", ignoreCase = true) -> Mint; else -> Primary }
    Card(onClick = onClick, modifier = modifier.height(115.dp), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Surface), elevation = CardDefaults.cardElevation(defaultElevation = 0.dp), border = BorderStroke(1.dp, Neutral)) {
        Column(modifier = Modifier.fillMaxSize().padding(14.dp), horizontalAlignment = Alignment.Start, verticalArrangement = Arrangement.SpaceBetween) {
            Box(modifier = Modifier.size(56.dp).clip(RoundedCornerShape(14.dp)).background(iconColor.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) { Icon(imageVector = icon, contentDescription = item.label, tint = iconColor, modifier = Modifier.size(32.dp)) }
            Text(text = item.label, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, lineHeight = 14.sp), color = Secondary, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun SectionPerformance(poinKinerja: Int, onDetailKinerjaClick: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = "Poin Kinerja", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = Secondary.copy(alpha = 0.7f), modifier = Modifier.padding(start = 4.dp))
        Card(onClick = onDetailKinerjaClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) { Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) { Text(text = "$poinKinerja", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = ScoreGreen); Text(text = "/ 100", style = MaterialTheme.typography.bodyMedium, color = Secondary.copy(alpha = 0.6f)) } }
                Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Detail", tint = Secondary.copy(alpha = 0.5f), modifier = Modifier.size(28.dp))
            }
        }
    }
}

@Composable
private fun SectionTaskNotification(daftarTugas: List<TugasItem>, totalTugas: Int, tugasSelesai: Int, onClick: () -> Unit) {
    if (daftarTugas.isEmpty()) return
    val tugasHarian = daftarTugas.filter { !it.isTugasKhusus && !it.isSelesai }
    val tugasKhusus = daftarTugas.filter { it.isTugasKhusus && !it.isSelesai }
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Primary.copy(alpha = 0.08f)), elevation = CardDefaults.cardElevation(defaultElevation = 0.dp), border = BorderStroke(1.dp, Primary.copy(alpha = 0.15f))) {
        Column(modifier = Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(Primary.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) { Icon(imageVector = Icons.Default.Assignment, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp)) }
                Text(text = "Tugas Hari Ini", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = Secondary)
                Spacer(modifier = Modifier.weight(1f))
                if (totalTugas > 0) { Surface(shape = RoundedCornerShape(10.dp), color = Primary.copy(alpha = 0.15f)) { Text(text = "$tugasSelesai/$totalTugas", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Primary, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)) } }
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (tugasHarian.isNotEmpty()) TaskStatusRow(label = "${tugasHarian.size} Tugas Harian", color = BlueSchedule, examples = tugasHarian.take(2).map { it.judul })
                if (tugasKhusus.isNotEmpty()) TaskStatusRow(label = "${tugasKhusus.size} Tugas Khusus", color = OrangeManage, examples = tugasKhusus.take(2).map { it.judul })
                if (tugasHarian.isEmpty() && tugasKhusus.isEmpty() && totalTugas > 0) { Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.CheckCircle, null, tint = ScoreGreen, modifier = Modifier.size(16.dp)); Text(text = "Semua tugas hari ini telah selesai dikerjakan!", style = MaterialTheme.typography.labelMedium, color = ScoreGreen, fontWeight = FontWeight.Bold) } }
            }
            Text(text = "Ketuk untuk melihat rincian daftar tugas", style = MaterialTheme.typography.labelSmall, color = Secondary.copy(alpha = 0.5f), fontWeight = FontWeight.Medium, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 4.dp))
        }
    }
}

@Composable
private fun TaskStatusRow(label: String, color: Color, examples: List<String>) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        Surface(shape = RoundedCornerShape(8.dp), color = color.copy(alpha = 0.12f)) { Text(text = label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold, color = color, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)) }
        Text(text = examples.joinToString(", ") + if (examples.size > 1) "..." else "", style = MaterialTheme.typography.bodySmall, color = Secondary.copy(alpha = 0.7f), maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun SectionLeave(sisaCuti: Int, onSisaCutiClick: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = "Sisa Cuti", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = Secondary.copy(alpha = 0.7f), modifier = Modifier.padding(start = 4.dp))
        Card(onClick = onSisaCutiClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(OceanBlue.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) { Icon(imageVector = Icons.Default.BeachAccess, contentDescription = "Sisa Cuti", tint = OceanBlue, modifier = Modifier.size(24.dp)) }
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Sisa Cuti Tahunan", style = MaterialTheme.typography.bodyMedium, color = Secondary.copy(alpha = 0.7f))
                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) { Text(text = "$sisaCuti", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Primary); Text(text = "hari", style = MaterialTheme.typography.bodyMedium, color = Secondary.copy(alpha = 0.6f), modifier = Modifier.padding(bottom = 2.dp)) }
                }
                Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Detail", tint = Secondary.copy(alpha = 0.5f), modifier = Modifier.size(28.dp))
            }
        }
    }
}

@Composable
private fun DialogDaftarTugas(tugasList: List<TugasItem>, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(modifier = Modifier.fillMaxWidth().padding(24.dp), shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = Surface), elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)) {
            Column(modifier = Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Primary.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) { Icon(imageVector = Icons.Default.Assignment, contentDescription = null, tint = Primary, modifier = Modifier.size(22.dp)) }
                        Text(text = "Tugas Hari Ini", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = Secondary)
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.background(Neutral, CircleShape).size(32.dp)) { Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup", tint = Secondary, modifier = Modifier.size(18.dp)) }
                }
                HorizontalDivider(color = Neutral)
                if (tugasList.isEmpty()) { Box(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) { Text("Tidak ada tugas hari ini.", color = Secondary.copy(alpha = 0.6f)) } }
                else { Column(modifier = Modifier.fillMaxWidth().weight(weight = 1f, fill = false).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(14.dp)) { tugasList.forEach { DialogTugasItemRow(it) } } }
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Primary)) { Text("Dimengerti", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp) }
            }
        }
    }
}

@Composable
private fun DialogTugasItemRow(tugas: TugasItem) {
    val statusColor = if (tugas.isSelesai) ScoreGreen else if (tugas.urgency == "urgent") Coral else OceanBlue
    val statusBg = statusColor.copy(alpha = 0.1f)
    val statusLabel = if (tugas.isSelesai) "Selesai" else if (tugas.urgency == "urgent") "Urgent" else "Proses"
    Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Neutral.copy(alpha = 0.3f)).padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(if (tugas.isTugasKhusus) OrangeManage.copy(alpha = 0.1f) else BlueSchedule.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) { Icon(imageVector = if (tugas.isTugasKhusus) Icons.Default.Star else Icons.Default.Assignment, contentDescription = null, tint = if (tugas.isTugasKhusus) OrangeManage else BlueSchedule, modifier = Modifier.size(20.dp)) }
        Column(modifier = Modifier.weight(1f)) { Text(text = tugas.judul, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Secondary, maxLines = 2, overflow = TextOverflow.Ellipsis); Text(text = if (tugas.isTugasKhusus) "Tugas Khusus" else "Tugas Harian", style = MaterialTheme.typography.labelSmall, color = Secondary.copy(alpha = 0.6f)) }
        Surface(shape = RoundedCornerShape(8.dp), color = statusBg) { Text(text = statusLabel, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = statusColor) }
    }
}

@Composable
private fun DialogDaftarJadwal(jadwalList: List<JadwalDto>, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(modifier = Modifier.fillMaxWidth().padding(24.dp), shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = Surface), elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)) {
            Column(modifier = Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(OceanBlue.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) { Icon(imageVector = Icons.AutoMirrored.Filled.EventNote, contentDescription = null, tint = OceanBlue, modifier = Modifier.size(22.dp)) }
                        Text(text = "Agenda & Event", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Secondary)
                    }
                    IconButton(onClick = onDismiss) { Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup", tint = Secondary.copy(alpha = 0.6f)) }
                }
                HorizontalDivider(color = Neutral)
                if (jadwalList.isEmpty()) { Box(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) { Text(text = "Tidak ada agenda atau event bulan ini.", style = MaterialTheme.typography.bodyMedium, color = Secondary.copy(alpha = 0.6f), textAlign = TextAlign.Center) } }
                else { val sortedList = jadwalList.sortedBy { it.tanggal }; Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) { sortedList.forEach { JadwalItemRow(it) } } }
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = Primary)) { Text("Tutup", fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@Composable
private fun JadwalItemRow(jadwal: JadwalDto) {
    val dateText = DateTimeUtil.formatIsoToLocal(jadwal.tanggal)
    val categoryColor = when (jadwal.kategori.lowercase()) { "libur" -> Coral; "tugas" -> OrangeManage; else -> OceanBlue }
    Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Neutral.copy(alpha = 0.3f)).padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(categoryColor.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) { val icon = when (jadwal.kategori.lowercase()) { "libur" -> Icons.Default.Info; "tugas" -> Icons.Default.Assignment; else -> Icons.Default.Star }; Icon(imageVector = icon, contentDescription = null, tint = categoryColor, modifier = Modifier.size(20.dp)) }
        Column(modifier = Modifier.weight(1f)) { Text(text = jadwal.namaEvent, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Secondary, maxLines = 1, overflow = TextOverflow.Ellipsis); Text(text = dateText, style = MaterialTheme.typography.labelSmall, color = Secondary.copy(alpha = 0.6f)) }
        Surface(shape = RoundedCornerShape(8.dp), color = categoryColor.copy(alpha = 0.1f)) { Text(text = jadwal.kategori.replaceFirstChar { it.uppercase() }, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = categoryColor) }
    }
}

private fun mapIcon(iconName: String): ImageVector {
    return when (iconName) {
        "Description" -> Icons.Default.Description; "FactCheck" -> Icons.Default.FactCheck; "Assignment" -> Icons.Default.Assignment
        "School" -> Icons.Default.School; "EmojiEvents" -> Icons.Default.EmojiEvents; "People" -> Icons.Default.People
        "PeopleAlt" -> Icons.Default.PeopleAlt; "AccountBalance" -> Icons.Default.AccountBalance; "Settings" -> Icons.Default.Settings
        "Receipt" -> Icons.Default.Receipt; "Schedule" -> Icons.Default.Schedule; "Book" -> Icons.Default.Book
        "Groups" -> Icons.Default.Groups; "Store" -> Icons.Default.Store; "Person" -> Icons.Default.Person
        "Star" -> Icons.Default.Star; "Favorite" -> Icons.Default.Favorite; "TrendingUp" -> Icons.Default.TrendingUp
        "PointOfSale" -> Icons.Default.PointOfSale; "Inventory2" -> Icons.Default.Inventory2; "ReceiptLong" -> Icons.Default.ReceiptLong
        else -> Icons.Default.List
    }
}
