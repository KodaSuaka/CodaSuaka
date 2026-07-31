package com.example.codasuaka.ui.screen.riwayat_nota

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.codasuaka.data.remote.dto.NotaDto
import com.example.codasuaka.ui.components.CodaSuakaSnackbarHost
import com.example.codasuaka.ui.screen.nota_pembelian.formatRupiah
import com.example.codasuaka.ui.theme.*
import com.example.codasuaka.util.ErrorMessageMapper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiwayatNotaScreen(
    onBack: () -> Unit,
    onNotaClick: (Int) -> Unit,
    viewModel: RiwayatNotaViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

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
    var showRangePicker by remember { mutableStateOf(false) }
    val dateRangePickerState = rememberDateRangePickerState()

    if (showRangePicker) {
        DatePickerDialog(
            onDismissRequest = { showRangePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val start = dateRangePickerState.selectedStartDateMillis
                    val end = dateRangePickerState.selectedEndDateMillis
                    if (start != null && end != null) {
                        val startDate = java.time.Instant.ofEpochMilli(start)
                            .atZone(java.time.ZoneId.of("UTC"))
                            .toLocalDate()
                        val endDate = java.time.Instant.ofEpochMilli(end)
                            .atZone(java.time.ZoneId.of("UTC"))
                            .toLocalDate()
                        viewModel.setFilterDateRange(startDate.toString(), endDate.toString())
                    }
                    showRangePicker = false
                }) { Text("OK", fontWeight = FontWeight.ExtraBold, color = Secondary) }
            },
            dismissButton = {
                TextButton(onClick = { showRangePicker = false }) { Text("Batal", color = Secondary.copy(alpha = 0.6f)) }
            },
            colors = DatePickerDefaults.colors(containerColor = Surface),
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            CodaSuakaTheme { 
                DateRangePicker(
                    state = dateRangePickerState,
                    modifier = Modifier.weight(1f).padding(top = 16.dp),
                    title = { Text("Pilih Rentang Tanggal", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold, color = Secondary) },
                    headline = { /* rapi */ },
                    showModeToggle = false,
                    colors = DatePickerDefaults.colors(
                        containerColor = Surface,
                        titleContentColor = Secondary,
                        headlineContentColor = Secondary,
                        weekdayContentColor = Secondary.copy(alpha = 0.6f),
                        subheadContentColor = Secondary.copy(alpha = 0.6f),
                        selectedDayContainerColor = Primary,
                        selectedDayContentColor = OnPrimary,
                        dayContentColor = Secondary,
                        todayContentColor = Primary,
                        todayDateBorderColor = Primary,
                        dayInSelectionRangeContainerColor = Primary.copy(alpha = 0.15f),
                        dayInSelectionRangeContentColor = Secondary
                    )
                )
            }
        }
    }

    // ─── Dialog konfirmasi hapus ───
    var notaToDelete by remember { mutableStateOf<NotaDto?>(null) }
    if (notaToDelete != null) {
        DeleteNotaDialog(
            nomorNota = notaToDelete!!.nomorNota,
            isDeleting = uiState.isDeleting,
            onDismiss = { notaToDelete = null },
            onConfirm = {
                viewModel.deleteNota(notaToDelete!!.id)
                notaToDelete = null
            }
        )
    }

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
                onRangeClick = { showRangePicker = true },
                onOutletChange = { viewModel.setFilterOutlet(it) },
                onSearchChange = viewModel::onSearchQueryChange
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

                uiState.filteredNotaList.isEmpty() -> {
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
                                if (uiState.searchQuery.isEmpty()) "Belum ada nota" else "Nota tidak ditemukan",
                                fontWeight = FontWeight.Bold,
                                color = Secondary 
                            )
                            Text(
                                if (uiState.searchQuery.isEmpty()) "Ubah filter atau buat nota baru" else "Coba cari nomor nota atau nama lain",
                                fontSize = 12.sp,
                                color = Secondary.copy(alpha = 0.6f)
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
                        items(uiState.filteredNotaList, key = { it.id }) { nota ->
                            NotaCard(
                                nota = nota,
                                onClick = { onNotaClick(nota.id) },
                                onDelete = { notaToDelete = nota }
                            )
                        }

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

// ─── Filter ────────────────────────────────────────────────

@Composable
private fun FilterSection(
    uiState: RiwayatNotaUiState,
    onTipeSelect: (NotaTipeFilter) -> Unit,
    onRangeClick: () -> Unit,
    onOutletChange: (Int?) -> Unit,
    onSearchChange: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Surface,
        border = BorderStroke(1.dp, Neutral)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { 
                    Text(
                        "Cari nomor nota atau nama...", 
                        fontSize = 14.sp, 
                        color = Secondary.copy(alpha = 0.5f) 
                    ) 
                },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Secondary, modifier = Modifier.size(20.dp)) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = Secondary),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = NeutralBorder,
                    focusedContainerColor = InputBackground,
                    unfocusedContainerColor = InputBackground,
                    focusedTextColor = Secondary,
                    unfocusedTextColor = Secondary
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NotaTipeFilter.entries.forEach { tipe ->
                        val isSelected = uiState.filterTipe == tipe
                        FilterChip(
                            selected = isSelected,
                            onClick = { onTipeSelect(tipe) },
                            label = { 
                                Text(
                                    text = tipe.label, 
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Primary else Secondary
                                ) 
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Primary.copy(alpha = 0.08f),
                                containerColor = Color.White
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = NeutralBorder,
                                selectedBorderColor = Primary.copy(alpha = 0.2f),
                                borderWidth = 1.dp
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                Surface(
                    onClick = onRangeClick,
                    shape = RoundedCornerShape(100.dp),
                    color = Primary.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, Primary.copy(alpha = 0.15f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange, 
                            contentDescription = null, 
                            tint = Primary, 
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Tanggal", 
                            fontSize = 12.sp, 
                            fontWeight = FontWeight.Bold, 
                            color = Primary
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                var outletExpanded by remember { mutableStateOf(false) }
                
                Box(modifier = Modifier.weight(1.3f)) {
                    // Custom Dropdown Card (Soft UI Style)
                    Surface(
                        onClick = { outletExpanded = true },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = InputBackground,
                        border = BorderStroke(1.dp, NeutralBorder.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = uiState.filterOutletId
                                    ?.let { id -> uiState.outletList.firstOrNull { it.id == id }?.namaOutlet }
                                    ?: "Semua Outlet",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Secondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = Secondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = outletExpanded,
                        onDismissRequest = { outletExpanded = false },
                        modifier = Modifier.background(Surface)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Semua Outlet", fontWeight = FontWeight.Medium, color = Secondary) },
                            onClick = {
                                onOutletChange(null)
                                outletExpanded = false
                            }
                        )
                        uiState.outletList.forEach { outlet ->
                            DropdownMenuItem(
                                text = { Text(outlet.namaOutlet, fontWeight = FontWeight.Medium, color = Secondary) },
                                onClick = {
                                    onOutletChange(outlet.id)
                                    outletExpanded = false
                                }
                            )
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text("Periode:", fontSize = 10.sp, color = Secondary, fontWeight = FontWeight.ExtraBold)
                    Text(
                        text = "${uiState.filterStartDate} s/d ${uiState.filterEndDate}",
                        fontSize = 11.sp,
                        color = Secondary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// ─── Kartu nota ────────────────────────────────────────────

@Composable
private fun NotaCard(
    nota: NotaDto,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val isPembelian = nota.tipe.equals("pembelian", ignoreCase = true)
    
    val badgeColor = if (isPembelian) Error else Success
    val badgeBg = if (isPembelian) Error.copy(alpha = 0.1f) else Success.copy(alpha = 0.1f)
    val badgeLabel = if (isPembelian) "Pembelian" else "Penjualan"

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, Neutral)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = nota.nomorNota,
                    fontWeight = FontWeight.ExtraBold,
                    color = Secondary,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = badgeBg
                ) {
                    Text(
                        text = badgeLabel,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = badgeColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = nota.tanggal,
                fontSize = 12.sp,
                color = Secondary, 
                fontWeight = FontWeight.Bold
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
                            fontSize = 13.sp,
                            color = Secondary, 
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    nota.metodePembayaran?.let {
                        Text(
                            text = it,
                            fontSize = 11.sp,
                            color = Secondary.copy(alpha = 0.7f),
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

// ─── Delete Confirmation Dialog ───

@Composable
private fun DeleteNotaDialog(
    nomorNota: String,
    isDeleting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        CodaSuakaTheme {
            Card(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
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
                        modifier = Modifier.size(64.dp).clip(CircleShape).background(Coral.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Warning, null, tint = Coral, modifier = Modifier.size(32.dp))
                    }
                    Text("Hapus Nota", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = Secondary)
                    Text(
                        text = "Yakin ingin menghapus nota \"$nomorNota\"? Tindakan ini tidak bisa dibatalkan.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Secondary.copy(alpha = 0.7f)
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                            Text("Batal", color = Secondary.copy(alpha = 0.6f))
                        }
                        Button(
                            onClick = onConfirm,
                            enabled = !isDeleting,
                            modifier = Modifier.weight(1.5f),
                            colors = ButtonDefaults.buttonColors(containerColor = Coral),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isDeleting) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text("Hapus", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
