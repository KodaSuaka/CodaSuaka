package com.example.codasuaka.ui.screen.dashboard_karyawan

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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

// ─── Color Palette Tambahan ─────
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
                        2 -> onNavigateTo("contact_list")
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
                specialEvent = uiState.specialEvent,
                showSpecialEvent = uiState.showSpecialEvent,
                onCheckClick = { viewModel.toggleAbsensi() },
                onRiwayatPresensiClick = { onNavigateTo("riwayat_kehadiran") },
                onJadwalShiftClick = { onNavigateTo("kalender") }
            )

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
                onDetailKinerjaClick = { onNavigateTo("kalender") }
            )

            // ══════════════════════════════════════════════════
            // 5. Sisa Cuti
            // ══════════════════════════════════════════════════
            SectionLeave(
                sisaCuti = uiState.sisaCuti,
                onSisaCutiClick = { onNavigateTo("pengajuan") }
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
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
    specialEvent: String?,
    showSpecialEvent: Boolean,
    onCheckClick: () -> Unit,
    onRiwayatPresensiClick: () -> Unit,
    onJadwalShiftClick: () -> Unit
) {
    val isCheckedIn = absensiStatus == AbsensiStatus.CHECKED_IN

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
                    Text(
                        text = "Senin, 7 Juli 2026",
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
                        .background(SoftBlue)
                        .padding(12.dp, 12.dp, 16.dp, 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(SoftBlue.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Shift Pagi",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = OnSurface
                        )
                        Text(
                            text = "07:00 - 15:00 WIB",
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceVariant
                        )
                    }
                    Text(
                        text = "Lihat Jadwal",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Primary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(99.dp))
                            .background(SoftBlue.copy(alpha = 0.6f))
                            .padding(horizontal = 10.dp, vertical = 2.dp)
                    )
                }

                HorizontalDivider(color = Neutral)

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
                                .background(if (isCheckedIn) ScoreGreen else OnSurfaceVariant)
                        )
                        Column {
                            Text(
                                text = if (isCheckedIn) "Sudah Check-in" else "Belum Check-in",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = OnSurface
                            )
                            Text(
                                text = if (isCheckedIn) "Masuk pukul ${absensiTime ?: "-"} WIB" else "Belum ada catatan",
                                style = MaterialTheme.typography.labelSmall,
                                color = OnSurfaceVariant
                            )
                        }
                    }
                    Button(
                        onClick = onCheckClick,
                        shape = RoundedCornerShape(99.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isCheckedIn) Coral else ScoreGreen
                        ),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = if (isCheckedIn) "Check-out" else "Check-in",
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
                            .background(AmberLight)
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
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onRiwayatPresensiClick,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary
                        ),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Riwayat Presensi",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }

                    OutlinedButton(
                        onClick = onJadwalShiftClick,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Primary
                        ),
                        border = BorderStroke(1.dp, Primary),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Jadwal Shift",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// 3. Menu Jabatan
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
