package com.example.codasuaka.ui.screen.pengajuan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codasuaka.ui.components.CustomCalendarNavigation
import com.example.codasuaka.ui.components.YearPickerDialog
import com.example.codasuaka.ui.screen.components.CustomTextField
import com.example.codasuaka.ui.theme.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import androidx.compose.ui.draw.clipToBounds

// ─── Color Palette Tambahan ─────
private val Teal = Color(0xFF0D9488)
private val TealLight = Color(0xFFCCFBF1)
private val Amber = Color(0xFFF59E0B)
private val AmberLight = Color(0xFFFEF3C7)
private val SoftBlue = Color(0xFFE0F2FE)
private val Coral = Color(0xFFF43F5E)
private val CoralLight = Color(0xFFFFE4E6)
private val ScoreGreen = Color(0xFF10B981)
private val ScoreGreenLight = Color(0xFFD1FAE5)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PengajuanScreen(
    onBack: () -> Unit,
    viewModel: PengajuanViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text("Pengajuan", fontWeight = FontWeight.Bold, color = Secondary)
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
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Tertiary)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Success Message ──
            if (uiState.isSuccess) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = ScoreGreen.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = ScoreGreen, modifier = Modifier.size(20.dp))
                        Text(
                            uiState.successMessage ?: "Pengajuan berhasil diajukan.",
                            color = ScoreGreen,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // ── Error Message ──
            if (uiState.errorMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Error.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Error, null, tint = Error, modifier = Modifier.size(20.dp))
                        Text(
                            uiState.errorMessage ?: "",
                            color = Error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // ── Card Info Header ──
            CardInfoHeader()

            // ── Jenis Pengajuan Dropdown ──
            JenisPengajuanDropdown(
                selectedJenis = uiState.selectedJenis,
                onJenisSelected = viewModel::setJenisPengajuan
            )

            // ── Tanggal Mulai Izin ──
            DatePickerField(
                label = "Mulai Izin",
                value = uiState.tanggalMulai,
                onClick = { viewModel.setTanggalMulai(it) }
            )

            // ── Tanggal Selesai Izin ──
            DatePickerField(
                label = "Sampai Izin",
                value = uiState.tanggalSelesai,
                onClick = { viewModel.setTanggalSelesai(it) }
            )

            // ── Informasi Jumlah Hari ──
            if (uiState.jumlahHari > 0) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Primary.copy(alpha = 0.08f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            Icons.Default.DateRange,
                            null,
                            tint = Primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            "Total: ${uiState.jumlahHari} hari",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Primary
                        )
                    }
                }
            }

            // ── Keterangan/Alasan ──
            CustomTextField(
                value = uiState.keterangan,
                onValueChange = viewModel::onKeteranganChange,
                label = "Keterangan/Alasan",
                placeholder = "Jelaskan alasan pengajuan cuti/izin Anda...",
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                singleLine = false,
                errorMessage = if (uiState.keterangan.isNotEmpty() && uiState.keterangan.length < 10) 
                    "${uiState.keterangan.length}/10 minimal karakter" else null,
                isError = uiState.keterangan.isNotEmpty() && uiState.keterangan.length < 10
            )

            // ── Tombol Ajukan ──
            Button(
                onClick = viewModel::ajukanPengajuan,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = !uiState.isSaving,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                    contentColor = OnPrimary,
                    disabledContainerColor = Primary.copy(alpha = 0.5f)
                )
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = OnPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Icon(
                    Icons.Default.Send,
                    null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Ajukan Pengajuan",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            }

            // ── Riwayat Pengajuan ──
            if (uiState.riwayatPengajuan.isNotEmpty()) {
                HorizontalDivider(color = Neutral, modifier = Modifier.padding(vertical = 4.dp))

                Text(
                    "Riwayat Pengajuan",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = OnSurfaceVariant
                )

                uiState.riwayatPengajuan.take(5).forEach { pengajuan ->
                    RiwayatPengajuanItem(pengajuan = pengajuan)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ═══════════════════════════════════════════════════════════
// CARD INFO HEADER
// ═══════════════════════════════════════════════════════════

@Composable
private fun CardInfoHeader() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SoftBlue),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Description,
                    null,
                    tint = Primary,
                    modifier = Modifier.size(26.dp)
                )
            }

            Column {
                Text(
                    "Ajukan Cuti / Izin",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface
                )
                Text(
                    "Isi form di bawah untuk mengajukan cuti atau izin.",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// JENIS PENGAJUAN DROPDOWN
// ═══════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun JenisPengajuanDropdown(
    selectedJenis: JenisPengajuan?,
    onJenisSelected: (JenisPengajuan) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Jenis Pengajuan",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = OnSurfaceVariant
            )

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                CustomTextField(
                    value = selectedJenis?.displayName ?: "Pilih Jenis Pengajuan",
                    onValueChange = {},
                    readOnly = true,
                    label = "Jenis Pengajuan",
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    leadingIcon = if (selectedJenis != null) getJenisIcon(selectedJenis) else Icons.Outlined.Description,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                MaterialTheme(colorScheme = lightColorScheme(
                    surface = Color.White,
                    onSurface = Secondary,
                    onSurfaceVariant = Secondary.copy(alpha = 0.7f)
                )) {
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        JenisPengajuan.entries.forEach { jenis ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(getJenisColor(jenis).copy(alpha = 0.12f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = getJenisIcon(jenis),
                                                contentDescription = null,
                                                tint = getJenisColor(jenis),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Text(
                                            text = jenis.displayName,
                                            fontWeight = FontWeight.Bold,
                                            color = Secondary
                                        )
                                    }
                                },
                                onClick = {
                                    onJenisSelected(jenis)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// DATE PICKER FIELD
// ═══════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerField(
    label: String,
    value: String,
    onClick: (Long) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showYearPicker by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = OnSurfaceVariant
            )

            CustomTextField(
                value = value.ifEmpty { "Pilih tanggal $label" },
                onValueChange = {},
                readOnly = true,
                enabled = false,
                label = "",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true },
                trailingIcon = {
                    Icon(
                        Icons.Default.CalendarMonth,
                        null,
                        tint = Primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )

            // DatePicker Dialog
            if (showDatePicker) {
                val datePickerState = rememberDatePickerState(
                    initialSelectedDateMillis = System.currentTimeMillis()
                )

                MaterialTheme(
                    colorScheme = lightColorScheme(
                        primary = Primary,
                        onPrimary = OnPrimary,
                        surface = Surface,
                        onSurface = Color.Black,
                        onSurfaceVariant = Color.Gray,
                        secondary = Secondary
                    )
                ) {
                    DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    datePickerState.selectedDateMillis?.let { millis ->
                                        onClick(millis)
                                    }
                                    showDatePicker = false
                                }
                            ) {
                                Text("Pilih", color = Primary, fontWeight = FontWeight.ExtraBold)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDatePicker = false }) {
                                Text("Batal", color = OnSurfaceVariant)
                            }
                        },
                        colors = DatePickerDefaults.colors(
                            containerColor = Color.White
                        )
                    ) {
                        if (showYearPicker) {
                            val displayMonth = Instant.ofEpochMilli(datePickerState.displayedMonthMillis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                                
                            YearPickerDialog(
                                selectedYear = displayMonth.year,
                                onYearSelected = { year ->
                                    val cal = Calendar.getInstance().apply {
                                        timeInMillis = datePickerState.displayedMonthMillis
                                    }
                                    cal.set(Calendar.YEAR, year)
                                    datePickerState.displayedMonthMillis = cal.timeInMillis
                                    showYearPicker = false
                                },
                                onDismiss = { showYearPicker = false }
                            )
                        }

                        Column(modifier = Modifier.padding(top = 16.dp)) {
                            // Header Kustom
                            val displayMonth = Instant.ofEpochMilli(datePickerState.displayedMonthMillis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                            
                            val monthTitle = displayMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale("id", "ID")))
                            
                            CustomCalendarNavigation(
                                title = monthTitle.replaceFirstChar { it.uppercase() },
                                onPrevClick = {
                                    val cal = Calendar.getInstance().apply {
                                        timeInMillis = datePickerState.displayedMonthMillis
                                    }
                                    cal.add(Calendar.MONTH, -1)
                                    datePickerState.displayedMonthMillis = cal.timeInMillis
                                },
                                onNextClick = {
                                    val cal = Calendar.getInstance().apply {
                                        timeInMillis = datePickerState.displayedMonthMillis
                                    }
                                    cal.add(Calendar.MONTH, 1)
                                    datePickerState.displayedMonthMillis = cal.timeInMillis
                                },
                                onTitleClick = { showYearPicker = true },
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(340.dp)
                                    .clipToBounds()
                            ) {
                                DatePicker(
                                    state = datePickerState,
                                    title = null,
                                    headline = null,
                                    showModeToggle = false,
                                    colors = DatePickerDefaults.colors(
                                        containerColor = Color.White,
                                        titleContentColor = Secondary,
                                        headlineContentColor = Secondary,
                                        weekdayContentColor = Color.Gray,
                                        subheadContentColor = Color.Gray,
                                        yearContentColor = Color.DarkGray,
                                        currentYearContentColor = Primary,
                                        selectedYearContentColor = Color.White,
                                        selectedYearContainerColor = Primary,
                                        dayContentColor = Color.Black,
                                        selectedDayContentColor = Color.White,
                                        selectedDayContainerColor = Primary,
                                        todayContentColor = Primary,
                                        todayDateBorderColor = Primary
                                    ),
                                    modifier = Modifier.offset(y = (-48).dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// RIWAYAT PENGAJUAN ITEM
// ═══════════════════════════════════════════════════════════

@Composable
private fun RiwayatPengajuanItem(
    pengajuan: Pengajuan
) {
    val (bgColor, icon) = when (pengajuan.jenis) {
        JenisPengajuan.CUTI_TAHUNAN -> TealLight to Icons.Default.BeachAccess
        JenisPengajuan.IZIN_SAKIT -> AmberLight to Icons.Default.LocalHospital
        JenisPengajuan.MENDADAK -> CoralLight to Icons.Default.Warning
    }
    val iconColor = when (pengajuan.jenis) {
        JenisPengajuan.CUTI_TAHUNAN -> Teal
        JenisPengajuan.IZIN_SAKIT -> Amber
        JenisPengajuan.MENDADAK -> Coral
    }

    val (statusText, statusColor, statusBg) = when (pengajuan.status.lowercase()) {
        "disetujui" -> Triple("Disetujui", ScoreGreen, ScoreGreenLight)
        "ditolak" -> Triple("Ditolak", Error, Error.copy(alpha = 0.1f))
        else -> Triple("Pending", Amber, AmberLight)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    null,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    pengajuan.jenis.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = OnSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "${pengajuan.tanggalMulai} - ${pengajuan.tanggalSelesai} (${pengajuan.jumlahHari} hari)",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Status Badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = statusBg
            ) {
                Text(
                    statusText,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = statusColor
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// HELPER FUNCTIONS
// ═══════════════════════════════════════════════════════════

private fun getJenisIcon(jenis: JenisPengajuan): ImageVector {
    return when (jenis) {
        JenisPengajuan.CUTI_TAHUNAN -> Icons.Default.CalendarMonth
        JenisPengajuan.IZIN_SAKIT -> Icons.Default.MedicalServices
        JenisPengajuan.MENDADAK -> Icons.Default.NotificationImportant
    }
}

private fun getJenisColor(jenis: JenisPengajuan): Color {
    return when (jenis) {
        JenisPengajuan.CUTI_TAHUNAN -> Teal
        JenisPengajuan.IZIN_SAKIT -> Amber
        JenisPengajuan.MENDADAK -> Coral
    }
}
