package com.example.codasuaka.ui.screen.riwayat_kehadiran

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codasuaka.ui.screen.kelola_outlet.Outlet
import com.example.codasuaka.ui.theme.*
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiwayatKehadiranScreen(
    onBack: () -> Unit,
    viewModel: RiwayatKehadiranViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text("Riwayat Kehadiran", fontWeight = FontWeight.Bold, color = Secondary)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Kembali", tint = Secondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface)
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading && uiState.presensiList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Tertiary)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Success Message ──
            if (uiState.successMessage != null) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Success.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, null, tint = Success, modifier = Modifier.size(20.dp))
                            Text(
                                uiState.successMessage ?: "",
                                color = Success,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            // ═════════════════════════════════════
            // SECTION ATAS — Filter Outlet & Tanggal
            // ═════════════════════════════════════
            item {
                FilterSection(
                    outlets = uiState.outlets,
                    selectedOutletId = uiState.selectedOutletId,
                    selectedDate = uiState.selectedDate,
                    onOutletSelected = viewModel::onOutletSelected,
                    onDateSelected = viewModel::onDateSelected
                )
            }

            // ═════════════════════════════════════
            // TAB BAR
            // ═════════════════════════════════════
            item {
                TabBarRiwayat(
                    selectedTab = uiState.selectedTab,
                    onTabSelected = viewModel::onTabSelected
                )
            }

            // ═════════════════════════════════════
            // TAB — LOG PRESENSI
            // ═════════════════════════════════════
            if (uiState.selectedTab == TabRiwayat.LOG_PRESENSI) {
                item {
                    SectionLabel("Daftar Kehadiran Karyawan")
                }

                if (uiState.presensiList.isEmpty()) {
                    item {
                        EmptyState(
                            icon = Icons.Default.CalendarMonth,
                            title = "Belum ada data presensi",
                            subtitle = "Tidak ada catatan kehadiran untuk filter ini."
                        )
                    }
                }

                items(uiState.presensiList, key = { it.id }) { presensi ->
                    PresensiCard(presensi = presensi)
                }
            }

            // ═════════════════════════════════════
            // TAB — PERSETUJUAN
            // ═════════════════════════════════════
            if (uiState.selectedTab == TabRiwayat.PERSETUJUAN) {
                item {
                    SectionLabel("Daftar Pengajuan Persetujuan")
                }

                if (uiState.persetujuanList.isEmpty()) {
                    item {
                        EmptyState(
                            icon = Icons.Default.TaskAlt,
                            title = "Tidak ada pengajuan",
                            subtitle = "Semua persetujuan telah diproses."
                        )
                    }
                }

                items(uiState.persetujuanList, key = { it.id }) { pengajuan ->
                    PersetujuanCard(
                        pengajuan = pengajuan,
                        isApproving = uiState.isApproving,
                        onSetujui = { viewModel.setujuiPersetujuan(pengajuan.id) },
                        onTolak = { viewModel.tolakPersetujuan(pengajuan.id) }
                    )
                }
            }

            // ═════════════════════════════════════
            // SECTION AKHIR — Rekap Bulanan
            // ═════════════════════════════════════
            item {
                RecapSection(
                    rekap = uiState.rekapBulanan,
                    recapMonthOffset = uiState.recapMonthOffset,
                    onPrevMonth = viewModel::onRecapPrevMonth,
                    onNextMonth = viewModel::onRecapNextMonth
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// FILTER SECTION
// ═══════════════════════════════════════════════════════════

@Composable
private fun FilterSection(
    outlets: List<Outlet>,
    selectedOutletId: Int?,
    selectedDate: String,
    onOutletSelected: (Int?) -> Unit,
    onDateSelected: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Outlet Dropdown
            OutletFilterDropdown(
                outlets = outlets,
                selectedOutletId = selectedOutletId,
                onOutletSelected = onOutletSelected,
                modifier = Modifier.weight(1f)
            )

            // Date Picker
            DatePickerField(
                selectedDate = selectedDate,
                onDateSelected = onDateSelected,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OutletFilterDropdown(
    outlets: List<Outlet>,
    selectedOutletId: Int?,
    onOutletSelected: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedOutlet = outlets.find { it.id == selectedOutletId }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedOutlet?.namaOutlet ?: "Semua Outlet",
            onValueChange = {},
            readOnly = true,
            label = { Text("Pilih Outlet") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                unfocusedBorderColor = Neutral,
                focusedContainerColor = Surface,
                unfocusedContainerColor = Surface,
                cursorColor = Primary,
                focusedLabelColor = Primary,
                unfocusedLabelColor = OnSurfaceVariant
            )
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Semua Outlet") },
                onClick = {
                    onOutletSelected(null)
                    expanded = false
                }
            )
            outlets.forEach { outlet ->
                DropdownMenuItem(
                    text = { Text(outlet.namaOutlet) },
                    onClick = {
                        onOutletSelected(outlet.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerField(
    selectedDate: String,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = if (selectedDate.isNotEmpty()) {
            try {
                val parts = selectedDate.split("-")
                val ld = LocalDate.of(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
                ld.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
            } catch (_: Exception) {
                null
            }
        } else null
    )

    OutlinedTextField(
        value = selectedDate.ifEmpty { LocalDate.now().toString() },
        onValueChange = {},
        readOnly = true,
        label = { Text("Pilih Tanggal") },
        trailingIcon = {
            IconButton(onClick = { showDatePicker = true }) {
                Icon(Icons.Default.CalendarMonth, "Pilih tanggal", tint = Primary)
            }
        },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Primary,
            unfocusedBorderColor = Neutral,
            focusedContainerColor = Surface,
            unfocusedContainerColor = Surface,
            cursorColor = Primary,
            focusedLabelColor = Primary,
            unfocusedLabelColor = OnSurfaceVariant
        )
    )

    if (showDatePicker) {
        // Memaksa DatePicker menggunakan skema warna Navy-Putih yang sangat kontras
        MaterialTheme(
            colorScheme = lightColorScheme(
                primary = Primary,
                onPrimary = OnPrimary,
                surface = Surface,
                onSurface = Secondary, // Memaksa teks header/bulan-tahun menjadi Navy
                onSurfaceVariant = Secondary, // Memaksa teks "PILIH TANGGAL" menjadi Navy
                secondary = Secondary,
                onSecondary = OnPrimary,
                primaryContainer = Primary.copy(alpha = 0.1f),
                onPrimaryContainer = Secondary
            )
        ) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                val ld = java.time.Instant.ofEpochMilli(millis)
                                    .atZone(java.time.ZoneId.systemDefault())
                                    .toLocalDate()
                                onDateSelected(ld.toString())
                            }
                            showDatePicker = false
                        }
                    ) {
                        Text("Pilih", color = Primary, fontWeight = FontWeight.ExtraBold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Batal", color = Secondary.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
                    }
                }
            ) {
                DatePicker(
                    state = datePickerState,
                    colors = DatePickerDefaults.colors(
                        containerColor = Surface,
                        titleContentColor = Secondary,
                        headlineContentColor = Secondary,
                        navigationContentColor = Secondary,
                        subheadContentColor = Secondary,
                        weekdayContentColor = OnSurfaceVariant,
                        yearContentColor = OnSurface,
                        currentYearContentColor = Primary,
                        selectedYearContentColor = OnPrimary,
                        selectedYearContainerColor = Primary,
                        dayContentColor = OnSurface,
                        selectedDayContainerColor = Primary,
                        selectedDayContentColor = OnPrimary,
                        todayContentColor = Primary,
                        todayDateBorderColor = Primary
                    )
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// TAB BAR
// ═══════════════════════════════════════════════════════════

@Composable
private fun TabBarRiwayat(
    selectedTab: TabRiwayat,
    onTabSelected: (TabRiwayat) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Surface,
        border = ButtonDefaults.outlinedButtonBorder
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            TabRiwayat.entries.forEach { tab ->
                val isSelected = selectedTab == tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) Primary else Surface)
                        .clickable { onTabSelected(tab) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab.label,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (isSelected) OnPrimary else OnSurfaceVariant
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// SECTION LABEL
// ═══════════════════════════════════════════════════════════

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = OnSurfaceVariant,
        modifier = Modifier.padding(start = 4.dp)
    )
}

// ═══════════════════════════════════════════════════════════
// EMPTY STATE
// ═══════════════════════════════════════════════════════════

@Composable
private fun EmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, null, tint = Neutral, modifier = Modifier.size(48.dp))
            Text(title, style = MaterialTheme.typography.bodyLarge, color = OnSurfaceVariant)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════
// PRESENSI CARD
// ═══════════════════════════════════════════════════════════

@Composable
private fun PresensiCard(presensi: Presensi) {
    Card(
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
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Secondary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    null,
                    tint = Secondary,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    presensi.namaKaryawan,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = OnSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Outlet
                    Text(
                        "🏪 ${presensi.outlet}",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    // Role badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Secondary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            presensi.role,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Secondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                // Jam kehadiran & status
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "⏰ ${presensi.jamKehadiran}",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                    StatusBadge(
                        status = when (presensi.status) {
                            StatusKehadiran.HADIR -> "Hadir"
                            StatusKehadiran.TERLAMBAT -> "Terlambat"
                            StatusKehadiran.IZIN -> "Izin"
                            StatusKehadiran.SAKIT -> "Sakit"
                            StatusKehadiran.ALPHA -> "Alpha"
                        },
                        isWarning = presensi.status == StatusKehadiran.TERLAMBAT
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// PERSETUJUAN CARD
// ═══════════════════════════════════════════════════════════

@Composable
private fun PersetujuanCard(
    pengajuan: PengajuanPersetujuan,
    isApproving: Boolean,
    onSetujui: () -> Unit,
    onTolak: () -> Unit
) {
    val isPending = pengajuan.statusPersetujuan == StatusPersetujuan.PENDING

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Secondary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        null,
                        tint = Secondary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        pengajuan.namaKaryawan,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = OnSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "🏪 ${pengajuan.outlet}",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Text(
                            "📅 ${pengajuan.tanggal}",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant
                        )
                    }
                }

                // Status badge
                StatusBadge(
                    status = when (pengajuan.statusPersetujuan) {
                        StatusPersetujuan.PENDING -> "Pending"
                        StatusPersetujuan.DISETUJUI -> "Disetujui"
                        StatusPersetujuan.DITOLAK -> "Ditolak"
                    },
                    isWarning = pengajuan.statusPersetujuan == StatusPersetujuan.PENDING,
                    isError = pengajuan.statusPersetujuan == StatusPersetujuan.DITOLAK,
                    isSuccess = pengajuan.statusPersetujuan == StatusPersetujuan.DISETUJUI
                )
            }

            // Alasan
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Tertiary
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Info,
                        null,
                        tint = OnSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        "💬 ${pengajuan.alasanIzin}",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                }
            }

            // Action buttons (hanya jika PENDING)
            if (isPending) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Tolak button
                    Button(
                        onClick = onTolak,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        enabled = !isApproving,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Error,
                            contentColor = OnPrimary,
                            disabledContainerColor = Error.copy(alpha = 0.5f)
                        )
                    ) {
                        Icon(Icons.Default.Close, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Tolak", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }

                    // Setujui button
                    Button(
                        onClick = onSetujui,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        enabled = !isApproving,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary,
                            contentColor = OnPrimary,
                            disabledContainerColor = Primary.copy(alpha = 0.5f)
                        )
                    ) {
                        if (isApproving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = OnPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Setujui", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// STATUS BADGE
// ═══════════════════════════════════════════════════════════

@Composable
private fun StatusBadge(
    status: String,
    isWarning: Boolean = false,
    isError: Boolean = false,
    isSuccess: Boolean = false
) {
    val bgColor = when {
        isSuccess -> Success.copy(alpha = 0.15f)
        isWarning -> CustomWarning.copy(alpha = 0.15f)
        isError -> Error.copy(alpha = 0.15f)
        else -> Success.copy(alpha = 0.15f)  // default = hadir
    }
    val textColor = when {
        isSuccess -> Success
        isWarning -> CustomWarning
        isError -> Error
        else -> Success
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = bgColor
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}

// ═══════════════════════════════════════════════════════════
// Custom Warning color (since it's not in Color.kt directly)
// ═══════════════════════════════════════════════════════════

private val CustomWarning = androidx.compose.ui.graphics.Color(0xFFD69E2E)

// ═══════════════════════════════════════════════════════════
// RECAP SECTION — Total Kehadiran PER KARYAWAN
// ═══════════════════════════════════════════════════════════

@Composable
private fun RecapSection(
    rekap: RekapBulanan,
    recapMonthOffset: Int,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "📊 Rekap Bulanan",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = OnSurface
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = onPrevMonth,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.ChevronLeft, "Bulan sebelumnya", tint = OnSurface)
                }

                val monthName = try {
                    val month = java.time.Month.of(rekap.bulan + 1)
                    "${month.getDisplayName(TextStyle.FULL, Locale("id", "ID"))} ${rekap.tahun}"
                } catch (_: Exception) {
                    "Bulan $rekap.tahun"
                }

                Text(
                    text = monthName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = OnSurface,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                IconButton(
                    onClick = onNextMonth,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.ChevronRight, "Bulan berikutnya", tint = OnSurface)
                }
            }
        }

        // ── Employee Recap Table ──
        RecapEmployeeTable(rekap = rekap)
    }
}

@Composable
private fun RecapEmployeeTable(rekap: RekapBulanan) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            // Table Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Primary.copy(alpha = 0.1f))
                    .padding(vertical = 12.dp, horizontal = 8.dp)
            ) {
                Text(
                    "Karyawan",
                    modifier = Modifier.weight(2f),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Secondary,
                    fontSize = 11.sp
                )
                Text("Hadir", modifier = Modifier.weight(1f), textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold,
                    color = Secondary, fontSize = 11.sp)
                Text("Telat", modifier = Modifier.weight(1f), textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold,
                    color = Secondary, fontSize = 11.sp)
                Text("Izin", modifier = Modifier.weight(1f), textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold,
                    color = Secondary, fontSize = 11.sp)
                Text("Sakit", modifier = Modifier.weight(1f), textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold,
                    color = Secondary, fontSize = 11.sp)
                Text("Alpha", modifier = Modifier.weight(1f), textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold,
                    color = Secondary, fontSize = 11.sp)
            }

            // Employee Rows
            rekap.rekapKaryawan.forEach { karyawan ->
                RecapEmployeeRow(karyawan = karyawan)
                HorizontalDivider(color = Neutral, thickness = 0.5.dp)
            }

            // Total Row
            RecapTotalRow(rekap = rekap)
        }
    }
}

@Composable
private fun RecapEmployeeRow(karyawan: RekapKaryawan) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(2f)) {
            Text(
                karyawan.namaKaryawan,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = OnSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                "${karyawan.role} • ${karyawan.outlet}",
                style = MaterialTheme.typography.labelSmall,
                color = OnSurfaceVariant,
                fontSize = 10.sp
            )
        }
        Text(karyawan.totalHadir.toString(), modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center, fontWeight = FontWeight.Bold,
            color = Success, fontSize = 13.sp)
        Text(karyawan.totalTerlambat.toString(), modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center, fontWeight = FontWeight.Bold,
            color = CustomWarning, fontSize = 13.sp)
        Text(karyawan.totalIzin.toString(), modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center, fontWeight = FontWeight.Bold,
            color = CustomWarning, fontSize = 13.sp)
        Text(karyawan.totalSakit.toString(), modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center, fontWeight = FontWeight.Bold,
            color = Error, fontSize = 13.sp)
        Text(karyawan.totalAlpha.toString(), modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center, fontWeight = FontWeight.Bold,
            color = Error, fontSize = 13.sp)
    }
}

@Composable
private fun RecapTotalRow(rekap: RekapBulanan) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Tertiary)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "🔢 Total",
            modifier = Modifier.weight(2f),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = OnSurface
        )
        Text(rekap.totalHadir.toString(), modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center, fontWeight = FontWeight.Bold,
            color = Success, fontSize = 14.sp)
        Text(rekap.totalTerlambat.toString(), modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center, fontWeight = FontWeight.Bold,
            color = CustomWarning, fontSize = 14.sp)
        Text(rekap.totalIzin.toString(), modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center, fontWeight = FontWeight.Bold,
            color = CustomWarning, fontSize = 14.sp)
        Text(rekap.totalSakit.toString(), modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center, fontWeight = FontWeight.Bold,
            color = Error, fontSize = 14.sp)
        Text(rekap.totalAlpha.toString(), modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center, fontWeight = FontWeight.Bold,
            color = Error, fontSize = 14.sp)
    }
}
