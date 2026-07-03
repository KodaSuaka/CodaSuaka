package com.example.codasuaka.ui.screen.dashboard_karyawan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import com.example.codasuaka.ui.theme.*

// ─── Color Palette Tambahan (dari Helper/asset/image.png) ─────
private val Teal = Color(0xFF0D9488)
private val TealLight = Color(0xFFCCFBF1)
private val Amber = Color(0xFFF59E0B)
private val AmberLight = Color(0xFFFEF3C7)
private val SoftBlue = Color(0xFFE0F2FE)
private val Coral = Color(0xFFF43F5E)
private val CoralLight = Color(0xFFFFE4E6)
private val Indigo = Color(0xFF6366F1)
private val IndigoLight = Color(0xFFE0E7FF)
private val Purple = Color(0xFF8B5CF6)
private val PurpleLight = Color(0xFFEDE9FE)
private val ScoreGreen = Color(0xFF10B981)
private val ScoreGreenLight = Color(0xFFD1FAE5)

// ─── DashboardKaryawanScreen ──────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardKaryawanScreen(
    onNavigateTo: (String) -> Unit,
    onLogout: () -> Unit,
    viewModel: DashboardKaryawanViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Dashboard Karyawan",
                        fontWeight = FontWeight.SemiBold,
                        color = OnPrimary
                    )
                },
                actions = {
                    IconButton(onClick = { /* notifikasi */ }) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifikasi",
                            tint = OnPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary
                )
            )
        },
        bottomBar = {
            BottomNavigationBar(
                selectedIndex = uiState.selectedBottomNav,
                onItemSelected = { index ->
                    viewModel.onBottomNavSelected(index)
                    when (index) {
                        0 -> { /* already on dashboard */ }
                        1 -> onNavigateTo("pengajuan")
                        2 -> onNavigateTo("pesan")
                    }
                }
            )
        }
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

            // ── Error Message ──
            if (uiState.errorMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = CoralLight
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = uiState.errorMessage ?: "",
                        color = Coral,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // ══════════════════════════════════════════════════
            // SECTION ATAS — Data Diri Karyawan
            // ══════════════════════════════════════════════════
            SectionEmployeeInfo(
                employee = uiState.employeeInfo
            )

            // ══════════════════════════════════════════════════
            // SECTION TENGAH PERSONAL — Absensi, Jadwal, Event
            // ══════════════════════════════════════════════════
            SectionPersonalMenu(
                absensiStatus = uiState.absensiStatus,
                absensiTime = uiState.absensiTime,
                specialEvent = uiState.specialEvent,
                showSpecialEvent = uiState.showSpecialEvent,
                onAbsensiClick = { viewModel.toggleAbsensi() },
                onJadwalShiftClick = { onNavigateTo("kalender") }
            )

            // ══════════════════════════════════════════════════
            // SECTION TENGAH JABATAN — Tombol Role Permission
            // ══════════════════════════════════════════════════
            SectionRoleMenu(
                items = uiState.roleMenuItems,
                onItemClick = { route ->
                    if (route != null) onNavigateTo(route)
                }
            )

            // ══════════════════════════════════════════════════
            // SECTION BAWAH 1 — Detail Kinerja & Daftar Tugas
            // ══════════════════════════════════════════════════
            SectionPerformance(
                poinKinerja = uiState.poinKinerja,
                totalTugas = uiState.totalTugas,
                tugasSelesai = uiState.tugasSelesai,
                onDetailKinerjaClick = { onNavigateTo("detail_kinerja") }
            )

            // Daftar Tugas (visible jika ada)
            if (uiState.daftarTugas.isNotEmpty()) {
                SectionTaskList(
                    tasks = uiState.daftarTugas,
                    onLihatSemuaClick = { onNavigateTo("tugas_tim") }
                )
            }

            // ══════════════════════════════════════════════════
            // SECTION BAWAH 2 — Sisa Cuti & Konten Tambahan
            // ══════════════════════════════════════════════════
            SectionLeaveAndAdditional(
                sisaCuti = uiState.sisaCuti,
                additionalItems = uiState.additionalContent,
                onSisaCutiClick = { onNavigateTo("sisa_cuti") },
                onAdditionalItemClick = { route ->
                    if (route != null) onNavigateTo(route)
                }
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// SECTION ATAS — Data Diri Karyawan
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
                    // Progress indicator
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
// SECTION TENGAH PERSONAL
// ═══════════════════════════════════════════════════════════════

@Composable
private fun SectionPersonalMenu(
    absensiStatus: AbsensiStatus,
    absensiTime: String?,
    specialEvent: String?,
    showSpecialEvent: Boolean,
    onAbsensiClick: () -> Unit,
    onJadwalShiftClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Judul Section
        Text(
            text = "Menu Personal",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = OnSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp)
        )

        // Baris 1: Absensi & Jadwal Shift
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Absensi (Checkin/Checkout)
            CardAbsensi(
                modifier = Modifier.weight(1f),
                absensiStatus = absensiStatus,
                absensiTime = absensiTime,
                onClick = onAbsensiClick
            )

            // Jadwal Shift
            CardJadwalShift(
                modifier = Modifier.weight(1f),
                onClick = onJadwalShiftClick
            )
        }

        // Special Event (kadang muncul)
        if (showSpecialEvent && specialEvent != null) {
            CardSpecialEvent(
                event = specialEvent
            )
        }
    }
}

@Composable
private fun CardAbsensi(
    modifier: Modifier = Modifier,
    absensiStatus: AbsensiStatus,
    absensiTime: String?,
    onClick: () -> Unit
) {
    val isCheckedIn = absensiStatus == AbsensiStatus.CHECKED_IN
    val bgColor = if (isCheckedIn) ScoreGreenLight else AmberLight
    val iconColor = if (isCheckedIn) ScoreGreen else Amber
    val statusText = if (isCheckedIn) "Checkout" else "Checkin"
    val statusIcon = if (isCheckedIn) Icons.Default.Logout else Icons.Default.Login

    Card(
        onClick = onClick,
        modifier = modifier.height(120.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = statusIcon,
                    contentDescription = statusText,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = statusText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = OnSurface
            )

            if (absensiTime != null) {
                Text(
                    text = absensiTime,
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CardJadwalShift(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(120.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(SoftBlue),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Jadwal Shift",
                    tint = Primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Jadwal\nShift",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = OnSurface,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun CardSpecialEvent(
    event: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = AmberLight
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Celebration,
                contentDescription = null,
                tint = Amber,
                modifier = Modifier.size(24.dp)
            )

            Text(
                text = event,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = OnSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// SECTION TENGAH JABATAN — Tombol Role Permission
// ═══════════════════════════════════════════════════════════════

@Composable
private fun SectionRoleMenu(
    items: List<RoleMenuItem>,
    onItemClick: (String?) -> Unit
) {
    if (items.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Menu Jabatan",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = OnSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items.forEach { item ->
                RoleMenuCard(
                    modifier = Modifier.weight(1f),
                    item = item,
                    onClick = { onItemClick(item.route) }
                )
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

    Card(
        onClick = onClick,
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(IndigoLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = item.label,
                    tint = Indigo,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = item.label,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = OnSurface,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// SECTION BAWAH 1 — Detail Kinerja & Daftar Tugas
// ═══════════════════════════════════════════════════════════════

@Composable
private fun SectionPerformance(
    poinKinerja: Int,
    totalTugas: Int,
    tugasSelesai: Int,
    onDetailKinerjaClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Kinerja",
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
                // Ringkasan Kinerja
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Poin Kinerja",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )

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

                    // Progress tugas
                    if (totalTugas > 0) {
                        Text(
                            text = "Tugas: $tugasSelesai / $totalTugas selesai",
                            style = MaterialTheme.typography.labelMedium,
                            color = OnSurfaceVariant
                        )
                    }
                }

                // Arrow icon
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

@Composable
private fun SectionTaskList(
    tasks: List<TugasItem>,
    onLihatSemuaClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Header dengan tombol "Lihat Semua"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Daftar Tugas",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = OnSurfaceVariant
            )

            Text(
                text = "Lihat Semua",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                color = Primary,
                modifier = Modifier.clickable(onClick = onLihatSemuaClick)
            )
        }

        // Daftar tugas (max 3 item)
        tasks.take(3).forEach { tugas ->
            TaskItemCard(tugas = tugas)
        }
    }
}

@Composable
private fun TaskItemCard(
    tugas: TugasItem
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (tugas.isSelesai) ScoreGreenLight else Surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Checkbox / status icon
            Icon(
                imageVector = if (tugas.isSelesai)
                    Icons.Default.CheckCircle
                else
                    Icons.Outlined.RadioButtonUnchecked,
                contentDescription = if (tugas.isSelesai) "Selesai" else "Belum",
                tint = if (tugas.isSelesai) ScoreGreen else OnSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )

            // Detail tugas
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = tugas.judul,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = OnSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "Tenggat: ${tugas.tenggat}",
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceVariant
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// SECTION BAWAH 2 — Sisa Cuti & Konten Tambahan
// ═══════════════════════════════════════════════════════════════

@Composable
private fun SectionLeaveAndAdditional(
    sisaCuti: Int,
    additionalItems: List<AdditionalMenuItem>,
    onSisaCutiClick: () -> Unit,
    onAdditionalItemClick: (String?) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Cuti & Lainnya",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = OnSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp)
        )

        // Sisa Cuti
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
                // Icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(SoftBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.BeachAccess,
                        contentDescription = "Sisa Cuti",
                        tint = Primary,
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

        // Konten Tambahan (terikat role)
        if (additionalItems.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                additionalItems.forEach { item ->
                    AdditionalCard(
                        modifier = Modifier.weight(1f),
                        item = item,
                        onClick = { onAdditionalItemClick(item.route) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AdditionalCard(
    modifier: Modifier = Modifier,
    item: AdditionalMenuItem,
    onClick: () -> Unit
) {
    val icon = mapIcon(item.iconResName)

    Card(
        onClick = onClick,
        modifier = modifier.height(90.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(PurpleLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = item.label,
                    tint = Purple,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = item.label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = OnSurface,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// BOTTOM NAVIGATION
// ═══════════════════════════════════════════════════════════════

@Composable
private fun BottomNavigationBar(
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit
) {
    NavigationBar(
        containerColor = Surface,
        tonalElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        val items = listOf(
            Triple(Icons.Default.Home, "Dashboard", 0),
            Triple(Icons.Default.Description, "Pengajuan", 1),
            Triple(Icons.Default.Email, "Pesan", 2)
        )

        items.forEach { (icon, label, index) ->
            NavigationBarItem(
                selected = selectedIndex == index,
                onClick = { onItemSelected(index) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label
                    )
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 11.sp
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Primary,
                    selectedTextColor = Primary,
                    unselectedIconColor = OnSurfaceVariant,
                    unselectedTextColor = OnSurfaceVariant,
                    indicatorColor = Primary.copy(alpha = 0.1f)
                )
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
        "Settings" -> Icons.Default.Settings
        "Receipt" -> Icons.Default.Receipt
        "Schedule" -> Icons.Default.Schedule
        "Book" -> Icons.Default.Book
        "Groups" -> Icons.Default.Groups
        "Store" -> Icons.Default.Store
        "Person" -> Icons.Default.Person
        "Star" -> Icons.Default.Star
        "Favorite" -> Icons.Default.Favorite
        else -> Icons.Default.List
    }
}
