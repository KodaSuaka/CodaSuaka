package com.example.codasuaka.ui.screen.riwayat_nota

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codasuaka.data.remote.dto.NotaDto
import com.example.codasuaka.ui.components.CodaSuakaSnackbarHost
import com.example.codasuaka.ui.screen.nota_pembelian.formatRupiah
import com.example.codasuaka.ui.theme.*
import com.example.codasuaka.util.ErrorMessageMapper
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiwayatNotaScreen(
    onBack: () -> Unit,
    onNotaClick: (Int) -> Unit,
    viewModel: RiwayatNotaViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    // ─── Snackbar ───
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.loadError) {
        uiState.loadError?.let {
            snackbarHostState.showSnackbar(ErrorMessageMapper.map(it, "memuat riwayat nota").message)
            viewModel.clearError()
        }
    }
    LaunchedEffect(uiState.deleteError) {
        uiState.deleteError?.let {
            snackbarHostState.showSnackbar(ErrorMessageMapper.map(it, "menghapus nota").message)
            viewModel.clearError()
        }
    }
    LaunchedEffect(uiState.deleteSuccess) {
        uiState.deleteSuccess?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearDeleteSuccess()
        }
    }

    // ─── Picker tanggal ───
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }
    val startPickerState = rememberDatePickerState(
        initialSelectedDateMillis = runCatching {
            LocalDate.parse(uiState.filterStartDate)
                .atStartOfDay(java.time.ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        }.getOrNull()
    )
    val endPickerState = rememberDatePickerState(
        initialSelectedDateMillis = runCatching {
            LocalDate.parse(uiState.filterEndDate)
                .atStartOfDay(java.time.ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        }.getOrNull()
    )

    // ─── Force Light Theme (dialog render di window terpisah) ───
    val datePickerColorScheme = lightColorScheme(
        primary = Primary,
        onPrimary = Color.White,
        secondary = Secondary,
        surface = Color.White,
        onSurface = OnSurface
    )

    if (showStartPicker) {
        MaterialTheme(colorScheme = datePickerColorScheme) {
            DatePickerDialog(
                onDismissRequest = { showStartPicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        startPickerState.selectedDateMillis?.let { millis ->
                            val date = java.time.Instant.ofEpochMilli(millis)
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate()
                            viewModel.setFilterDateRange(date.toString(), uiState.filterEndDate)
                        }
                        showStartPicker = false
                    }) { Text("Pilih") }
                },
                dismissButton = {
                    TextButton(onClick = { showStartPicker = false }) { Text("Batal") }
                }
            ) {
                DatePicker(state = startPickerState)
            }
        }
    }

    if (showEndPicker) {
        MaterialTheme(colorScheme = datePickerColorScheme) {
            DatePickerDialog(
                onDismissRequest = { showEndPicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        endPickerState.selectedDateMillis?.let { millis ->
                            val date = java.time.Instant.ofEpochMilli(millis)
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate()
                            viewModel.setFilterDateRange(uiState.filterStartDate, date.toString())
                        }
                        showEndPicker = false
                    }) { Text("Pilih") }
                },
                dismissButton = {
                    TextButton(onClick = { showEndPicker = false }) { Text("Batal") }
                }
            ) {
                DatePicker(state = endPickerState)
            }
        }
    }

    // ─── Dialog konfirmasi hapus ───
    var notaToDelete by remember { mutableStateOf<NotaDto?>(null) }
    notaToDelete?.let { nota ->
        // ─── Force Light Theme (dialog render di window terpisah) ───
        MaterialTheme(
            colorScheme = lightColorScheme(
                surface = Color.White,
                onSurface = OnSurface,
                onSurfaceVariant = OnSurfaceVariant,
                primary = Primary,
                secondary = Secondary,
                error = Error
            )
        ) {
            AlertDialog(
                onDismissRequest = { notaToDelete = null },
                containerColor = Color.White,
                titleContentColor = Secondary,
                textContentColor = OnSurface,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Default.Warning, null, tint = Error)
                        Text("Hapus Nota", fontWeight = FontWeight.ExtraBold, color = Secondary)
                    }
                },
                text = {
                    Text("Yakin hapus nota ${nota.nomorNota}? Tindakan ini tidak bisa dibatalkan.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteNota(nota.id)
                            notaToDelete = null
                        },
                        enabled = !uiState.isDeleting,
                        colors = ButtonDefaults.buttonColors(containerColor = Error),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (uiState.isDeleting) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text("Hapus", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { notaToDelete = null }) {
                        Text("Batal", color = OnSurfaceVariant)
                    }
                }
            )
        }
    }

    // ─── Force Light Theme ───
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Primary,
            onPrimary = Color.White,
            secondary = Secondary,
            onSecondary = Color.White,
            surface = Color.White,
            onSurface = OnSurface,
            onSurfaceVariant = OnSurfaceVariant,
            tertiary = Tertiary,
            outline = NeutralBorder
        )
    ) {
        Scaffold(
            snackbarHost = { CodaSuakaSnackbarHost(hostState = snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Riwayat Nota",
                            fontWeight = FontWeight.ExtraBold,
                            color = Secondary
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Kembali",
                                tint = Secondary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface)
                )
            },
            containerColor = Tertiary
        ) { paddingValues ->
            Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                FilterSection(
                    uiState = uiState,
                    onTipeSelect = { viewModel.setFilterTipe(it) },
                    onPickStart = { showStartPicker = true },
                    onPickEnd = { showEndPicker = true },
                    onOutletChange = { viewModel.setFilterOutlet(it) }
                )

                when {
                    uiState.isLoading && uiState.notaList.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Primary)
                        }
                    }

                    uiState.notaList.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.ReceiptLong,
                                    contentDescription = null,
                                    tint = NeutralBorder,
                                    modifier = Modifier.size(56.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    "Belum ada nota",
                                    fontWeight = FontWeight.Medium,
                                    color = OnSurfaceVariant
                                )
                                Text(
                                    "Ubah filter atau buat nota baru",
                                    fontSize = 12.sp,
                                    color = OnSurfaceVariant
                                )
                            }
                        }
                    }

                    else -> {
                        val listState = rememberLazyListState()
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(uiState.notaList, key = { it.id }) { nota ->
                                NotaCard(
                                    nota = nota,
                                    onClick = { onNotaClick(nota.id) },
                                    onDelete = { notaToDelete = nota }
                                )
                            }

                            // Load-more indicator
                            if (uiState.isLoadingMore) {
                                item {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            color = Primary,
                                            strokeWidth = 2.dp,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Trigger load more saat scroll mendekati akhir
                        val shouldLoadMore by remember {
                            derivedStateOf {
                                val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
                                val total = listState.layoutInfo.totalItemsCount
                                lastVisible >= total - 3
                            }
                        }
                        LaunchedEffect(shouldLoadMore) {
                            if (shouldLoadMore) viewModel.loadMore()
                        }
                    }
                }
            }
        }
    }
}

// ─── Filter ────────────────────────────────────────────────

@Composable
private fun FilterSection(
    uiState: RiwayatNotaUiState,
    onTipeSelect: (NotaTipeFilter) -> Unit,
    onPickStart: () -> Unit,
    onPickEnd: () -> Unit,
    onOutletChange: (Int?) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().background(Surface).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Chips tipe
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NotaTipeFilter.entries.forEach { tipe ->
                FilterChip(
                    selected = uiState.filterTipe == tipe,
                    onClick = { onTipeSelect(tipe) },
                    label = { Text(tipe.label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Primary,
                        selectedLabelColor = Color.White,
                        containerColor = Neutral.copy(alpha = 0.7f),
                        labelColor = OnSurfaceVariant
                    )
                )
            }
        }

        // Range tanggal
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DateField(
                label = "Dari",
                date = uiState.filterStartDate,
                onClick = onPickStart,
                modifier = Modifier.weight(1f)
            )
            Text("s/d", color = OnSurfaceVariant, fontSize = 12.sp)
            DateField(
                label = "Sampai",
                date = uiState.filterEndDate,
                onClick = onPickEnd,
                modifier = Modifier.weight(1f)
            )
        }

        // Dropdown outlet
        var outletExpanded by remember { mutableStateOf(false) }
        Box {
            OutlinedTextField(
                value = uiState.filterOutletId
                    ?.let { id -> uiState.outletList.firstOrNull { it.id == id }?.namaOutlet }
                    ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Outlet (Semua)") },
                trailingIcon = {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                },
                modifier = Modifier.fillMaxWidth().clickable { outletExpanded = true },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = NeutralBorder
                )
            )
            DropdownMenu(
                expanded = outletExpanded,
                onDismissRequest = { outletExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Semua Outlet") },
                    onClick = {
                        onOutletChange(null)
                        outletExpanded = false
                    }
                )
                uiState.outletList.forEach { outlet ->
                    DropdownMenuItem(
                        text = { Text(outlet.namaOutlet) },
                        onClick = {
                            onOutletChange(outlet.id)
                            outletExpanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DateField(
    label: String,
    date: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = date,
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Primary,
            unfocusedBorderColor = NeutralBorder
        )
    )
}

// ─── Kartu nota ────────────────────────────────────────────

@Composable
private fun NotaCard(
    nota: NotaDto,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val isPembelian = nota.tipe.equals("pembelian", ignoreCase = true)
    val badgeColor = if (isPembelian) Primary else Secondary
    val badgeLabel = if (isPembelian) "Pembelian" else "Penjualan"

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = nota.nomorNota,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier.clip(RoundedCornerShape(50)).background(badgeColor).padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = badgeLabel,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = nota.tanggal,
                fontSize = 12.sp,
                color = OnSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    nota.pihakTerkait?.let {
                        Text(
                            text = it,
                            fontSize = 12.sp,
                            color = OnSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    nota.metodePembayaran?.let {
                        Text(
                            text = it,
                            fontSize = 11.sp,
                            color = OnSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Text(
                    text = formatRupiah(nota.total),
                    fontWeight = FontWeight.ExtraBold,
                    color = Secondary,
                    fontSize = 15.sp
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Hapus nota",
                        tint = Error
                    )
                }
            }
        }
    }
}
