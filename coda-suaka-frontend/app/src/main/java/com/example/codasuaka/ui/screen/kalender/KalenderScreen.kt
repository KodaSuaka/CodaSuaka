package com.example.codasuaka.ui.screen.kalender

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.codasuaka.ui.theme.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

/**
 * Warna untuk kategori event.
 */
private val CategoryColorLibur = Error          // Merah
private val CategoryColorTugas = Color(0xFFD69E2E) // Kuning
private val CategoryColorEvent = Success        // Hijau

/**
 * Screen Kalender / Jadwal.
 *
 * Terdiri dari 3 bagian:
 * 1. Section atas — Kalender satu bulan (dengan navigasi)
 * 2. Section tengah — Daftar event
 * 3. FAB — Tombol menambah event
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KalenderScreen(
    onBack: () -> Unit,
    viewModel: KalenderViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text("Jadwal", fontWeight = FontWeight.SemiBold, color = OnPrimary)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Kembali", tint = OnPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openDialogTambah() },
                containerColor = Primary,
                contentColor = OnPrimary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, "Tambah Event", modifier = Modifier.size(28.dp))
            }
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
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
            // ── Success message ──
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
                            Text(uiState.successMessage ?: "", color = Success, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            // ── Error message ──
            if (uiState.errorMessage != null && uiState.dialogMode !is KalenderDialogMode.Tambah) {
                item {
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
                            Text(uiState.errorMessage ?: "", color = Error, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            // ══════════════════════════════════════════════════
            // SECTION ATAS — Kalender Bulanan
            // ══════════════════════════════════════════════════
            item {
                CalendarSection(
                    currentMonth = uiState.currentMonth,
                    events = uiState.events,
                    onPrevMonth = viewModel::prevMonth,
                    onNextMonth = viewModel::nextMonth,
                    onDateClick = { date ->
                        // Filter events for this date — open first one or show info
                    }
                )
            }

            // ══════════════════════════════════════════════════
            // SECTION TENGAH — Daftar Event
            // ══════════════════════════════════════════════════
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Daftar Event",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = OnSurfaceVariant
                    )
                    Surface(shape = RoundedCornerShape(12.dp), color = Primary.copy(alpha = 0.1f)) {
                        Text(
                            "${uiState.events.size} event",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = Primary
                        )
                    }
                }
            }

            // Event list
            if (uiState.events.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.EventBusy, null, tint = Neutral, modifier = Modifier.size(48.dp))
                            Text("Belum ada event", style = MaterialTheme.typography.bodyLarge, color = OnSurfaceVariant)
                            Text("Tekan tombol + untuk menambahkan event baru.", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                        }
                    }
                }
            }

            // Kelompok event per tanggal
            val eventsGrouped = uiState.events.sortedBy { it.tanggal }
            items(eventsGrouped, key = { it.id }) { event ->
                EventListItem(
                    event = event,
                    onClick = { viewModel.openDialogDetail(event) }
                )
            }

            item { Spacer(modifier = Modifier.height(72.dp)) }
        }
    }

    // ─── Dialog ──────────────────────────────────────────────
    when (val dialog = uiState.dialogMode) {
        is KalenderDialogMode.Tambah -> {
            DialogTambahEvent(
                namaEvent = uiState.formNamaEvent,
                tanggal = uiState.formTanggal,
                kategori = uiState.formKategori,
                isSaving = uiState.isSaving,
                errorMessage = uiState.errorMessage,
                onNamaChange = viewModel::onFormNamaEventChange,
                onTanggalChange = viewModel::onFormTanggalChange,
                onKategoriChange = viewModel::onFormKategoriChange,
                onSimpan = viewModel::simpanEvent,
                onDismiss = viewModel::closeDialog
            )
        }
        is KalenderDialogMode.Detail -> {
            DialogDetailEvent(
                event = dialog.event,
                onHapus = { viewModel.hapusEvent(dialog.event.id) },
                onDismiss = viewModel::closeDialog
            )
        }
        KalenderDialogMode.Closed -> { /* tidak ada dialog */ }
    }
}

// ═══════════════════════════════════════════════════════════
// KALENDER BULANAN
// ═══════════════════════════════════════════════════════════

@Composable
private fun CalendarSection(
    currentMonth: YearMonth,
    events: List<KalenderEvent>,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDateClick: (LocalDate) -> Unit
) {
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfMonth = currentMonth.atDay(1)
    // DayOfWeek.MONDAY = 1 ... SUNDAY = 7
    val startDayOfWeek = firstDayOfMonth.dayOfWeek.value // 1=Senin, 7=Minggu

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // ── Header: Bulan + Navigasi ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPrevMonth) {
                    Icon(Icons.Default.ChevronLeft, "Bulan sebelumnya", tint = Primary)
                }

                Text(
                    text = currentMonth.month.getDisplayName(TextStyle.FULL, Locale("id", "ID"))
                        .replaceFirstChar { it.uppercase() } + " ${currentMonth.year}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface
                )

                IconButton(onClick = onNextMonth) {
                    Icon(Icons.Default.ChevronRight, "Bulan berikutnya", tint = Primary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Grid Header (Senin - Minggu) ──
            Row(modifier = Modifier.fillMaxWidth()) {
                val dayNames = listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min")
                dayNames.forEach { name ->
                    Text(
                        text = name,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Grid Tanggal ──
            // Hitung jumlah baris yang dibutuhkan
            val totalCells = startDayOfWeek - 1 + daysInMonth
            val rows = (totalCells + 6) / 7

            for (row in 0 until rows) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (col in 0..6) {
                        val cellIndex = row * 7 + col
                        val dayNumber = cellIndex - (startDayOfWeek - 1) + 1

                        if (dayNumber in 1..daysInMonth) {
                            val date = currentMonth.atDay(dayNumber)
                            val eventOnDate = events.filter { it.tanggal == date }
                            val hasEvent = eventOnDate.isNotEmpty()

                            // Tentukan warna berdasarkan event dengan prioritas: LIBUR > TUGAS > EVENT
                            val dotColor = if (hasEvent) {
                                when {
                                    eventOnDate.any { it.kategori == EventCategory.LIBUR } -> CategoryColorLibur
                                    eventOnDate.any { it.kategori == EventCategory.TUGAS } -> CategoryColorTugas
                                    else -> CategoryColorEvent
                                }
                            } else null

                            // Cek apakah hari ini
                            val isToday = date == LocalDate.now()

                            DateCell(
                                dayNumber = dayNumber,
                                isToday = isToday,
                                dotColor = dotColor,
                                onClick = { onDateClick(date) }
                            )
                        } else {
                            // Sel kosong
                            Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                        }
                    }
                }
            }

            // ── Legenda ──
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                LegendItem(color = CategoryColorLibur, label = "Libur")
                LegendItem(color = CategoryColorTugas, label = "Tugas")
                LegendItem(color = CategoryColorEvent, label = "Event")
            }
        }
    }
}

@Composable
private fun RowScope.DateCell(
    dayNumber: Int,
    isToday: Boolean,
    dotColor: Color?,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1f)
            .clip(CircleShape)
            .then(
                if (isToday) Modifier.border(2.dp, Primary, CircleShape) else Modifier
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = dayNumber.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                color = if (isToday) Primary else OnSurface
            )
            if (dotColor != null) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
    }
}

// ═══════════════════════════════════════════════════════════
// LIST ITEM — Satu event di daftar
// ═══════════════════════════════════════════════════════════

@Composable
private fun EventListItem(
    event: KalenderEvent,
    onClick: () -> Unit
) {
    val categoryColor = when (event.kategori) {
        EventCategory.LIBUR -> CategoryColorLibur
        EventCategory.TUGAS -> CategoryColorTugas
        EventCategory.EVENT -> CategoryColorEvent
    }

    Card(
        onClick = onClick,
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
            // Tanggal badge
            Column(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(categoryColor.copy(alpha = 0.1f)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = event.tanggal.dayOfMonth.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = categoryColor
                )
                Text(
                    text = event.tanggal.month.getDisplayName(TextStyle.SHORT, Locale("id", "ID")),
                    style = MaterialTheme.typography.labelSmall,
                    color = categoryColor
                )
            }

            // Info event
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    event.namaEvent,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = OnSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = categoryColor.copy(alpha = 0.1f)
                ) {
                    Text(
                        event.kategori.displayName,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = categoryColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Arrow
            Icon(
                Icons.Default.ChevronRight,
                null,
                tint = OnSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════
// DIALOG — Tambah Event
// ═══════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogTambahEvent(
    namaEvent: String,
    tanggal: String,
    kategori: EventCategory,
    isSaving: Boolean,
    errorMessage: String?,
    onNamaChange: (String) -> Unit,
    onTanggalChange: (String) -> Unit,
    onKategoriChange: (EventCategory) -> Unit,
    onSimpan: () -> Unit,
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
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Tambah Event",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = OnSurface
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, "Tutup", tint = OnSurfaceVariant)
                    }
                }

                HorizontalDivider(color = Neutral)

                // Error message
                if (errorMessage != null) {
                    Surface(shape = RoundedCornerShape(8.dp), color = Error.copy(alpha = 0.1f)) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Error, null, tint = Error, modifier = Modifier.size(18.dp))
                            Text(errorMessage, color = Error, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                // Nama Event
                OutlinedTextField(
                    value = namaEvent,
                    onValueChange = onNamaChange,
                    label = { Text("Nama Event") },
                    placeholder = { Text("Masukkan nama event") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
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

                // Tanggal
                OutlinedTextField(
                    value = tanggal,
                    onValueChange = onTanggalChange,
                    label = { Text("Tanggal") },
                    placeholder = { Text("yyyy-MM-dd (contoh: 2026-06-15)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
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

                // Kategori dropdown
                val kategoriOptions = EventCategory.entries
                var expanded by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = kategori.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Kategori") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        shape = RoundedCornerShape(12.dp),
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
                        kategoriOptions.forEach { option ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        val dotColor = when (option) {
                                            EventCategory.LIBUR -> CategoryColorLibur
                                            EventCategory.TUGAS -> CategoryColorTugas
                                            EventCategory.EVENT -> CategoryColorEvent
                                        }
                                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(dotColor))
                                        Text(option.displayName)
                                    }
                                },
                                onClick = {
                                    onKategoriChange(option)
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                // Tombol Simpan
                Button(
                    onClick = onSimpan,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = !isSaving,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        contentColor = OnPrimary,
                        disabledContainerColor = Primary.copy(alpha = 0.5f)
                    )
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = OnPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Buat Event", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// DIALOG — Detail Event
// ═══════════════════════════════════════════════════════════

@Composable
private fun DialogDetailEvent(
    event: KalenderEvent,
    onHapus: () -> Unit,
    onDismiss: () -> Unit
) {
    val categoryColor = when (event.kategori) {
        EventCategory.LIBUR -> CategoryColorLibur
        EventCategory.TUGAS -> CategoryColorTugas
        EventCategory.EVENT -> CategoryColorEvent
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Detail Event",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = OnSurface
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, "Tutup", tint = OnSurfaceVariant)
                    }
                }

                HorizontalDivider(color = Neutral)

                // Tanggal badge besar
                Column(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(categoryColor.copy(alpha = 0.1f))
                        .align(Alignment.CenterHorizontally),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = event.tanggal.dayOfMonth.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = categoryColor
                    )
                    Text(
                        text = event.tanggal.month.getDisplayName(TextStyle.FULL, Locale("id", "ID")),
                        style = MaterialTheme.typography.bodySmall,
                        color = categoryColor
                    )
                }

                // Nama Event
                Text(
                    event.namaEvent,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                // Kategori
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = categoryColor.copy(alpha = 0.1f)
                ) {
                    Text(
                        event.kategori.displayName,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = categoryColor
                    )
                }

                // Tanggal lengkap
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Icon(Icons.Default.CalendarMonth, null, tint = OnSurfaceVariant, modifier = Modifier.size(20.dp))
                    Text(
                        text = "${event.tanggal.dayOfMonth} ${event.tanggal.month.getDisplayName(TextStyle.FULL, Locale("id", "ID"))} ${event.tanggal.year}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )
                }

                HorizontalDivider(color = Neutral)

                // Tombol hapus
                Button(
                    onClick = onHapus,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Error,
                        contentColor = OnPrimary
                    )
                ) {
                    Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Hapus Event", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
            }
        }
    }
}
