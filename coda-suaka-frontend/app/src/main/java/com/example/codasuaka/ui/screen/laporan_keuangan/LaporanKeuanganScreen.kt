package com.example.codasuaka.ui.screen.laporan_keuangan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.codasuaka.data.remote.dto.ArusKasData
import com.example.codasuaka.data.remote.dto.ArusKasDetail
import com.example.codasuaka.data.remote.dto.KategoriTransaksiDto
import com.example.codasuaka.data.remote.dto.TransaksiKasDto
import com.example.codasuaka.ui.components.CustomCalendarNavigation
import com.example.codasuaka.ui.components.YearPickerDialog
import com.example.codasuaka.ui.screen.components.CustomTextField
import com.example.codasuaka.ui.theme.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.ui.draw.clipToBounds

// ─── Warna Bantu ──────────────────────────────────────────────
private val MasukColor = Color(0xFF10B981)
private val MasukBg = Color(0xFFD1FAE5)
private val KeluarColor = Color(0xFFEF4444)
private val KeluarBg = Color(0xFFFEE2E2)
private val InfoColor = Color(0xFF3B82F6)
private val InfoBg = Color(0xFFDBEAFE)
private val WarningColor = Color(0xFFF59E0B)
private val WarningBg = Color(0xFFFEF3C7)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaporanKeuanganScreen(
    onBack: () -> Unit,
    viewModel: LaporanKeuanganViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    // ── Dropdown menu state ──
    var showExportMenu by remember { mutableStateOf(false) }
    var showReportsMenu by remember { mutableStateOf(false) }

    // ── Snackbar ──
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.submitSuccess) {
        uiState.submitSuccess?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSubmitSuccess()
        }
    }
    LaunchedEffect(uiState.exportSuccessPath) {
        uiState.exportSuccessPath?.let { path ->
            snackbarHostState.showSnackbar("File tersimpan: $path")
            viewModel.clearExportSuccess()
        }
    }
    LaunchedEffect(uiState.exportError) {
        uiState.exportError?.let {
            snackbarHostState.showSnackbar("Gagal: $it")
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text("Buku Kas", fontWeight = FontWeight.Bold, color = Secondary)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali", tint = Secondary)
                    }
                },
                actions = {
                    Row(
                        modifier = Modifier.padding(end = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Dropdown Menu Laporan (Menggantikan 3 icon terpisah)
                        Box {
                            IconButton(onClick = { showReportsMenu = true }) {
                                Icon(Icons.Default.Analytics, "Laporan", tint = Primary)
                            }
                            MaterialTheme(colorScheme = lightColorScheme(
                                surface = Color.White,
                                onSurface = Secondary,
                                onSurfaceVariant = Secondary.copy(alpha = 0.7f)
                            )) {
                                DropdownMenu(
                                    expanded = showReportsMenu,
                                    onDismissRequest = { showReportsMenu = false },
                                    modifier = Modifier.background(Color.White)
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Ringkasan Saldo", fontWeight = FontWeight.Medium, color = Secondary) },
                                        onClick = {
                                            showReportsMenu = false
                                            viewModel.toggleSaldoSheet()
                                        },
                                        leadingIcon = { Icon(Icons.Default.AccountBalanceWallet, null, tint = Primary) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Laporan Laba Rugi", fontWeight = FontWeight.Medium, color = Secondary) },
                                        onClick = {
                                            showReportsMenu = false
                                            viewModel.toggleLabaRugiSheet()
                                        },
                                        leadingIcon = { Icon(Icons.Default.BarChart, null, tint = OrangeManage) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Aliran Arus Kas", fontWeight = FontWeight.Medium, color = Secondary) },
                                        onClick = {
                                            showReportsMenu = false
                                            viewModel.toggleArusKasSheet()
                                        },
                                        leadingIcon = { Icon(Icons.Default.AccountTree, null, tint = PurpleLog) }
                                    )
                                }
                            }
                        }

                        // Dropdown Menu Ekspor
                        Box {
                            IconButton(onClick = { showExportMenu = true }) {
                                Icon(Icons.Default.FileDownload, "Ekspor", tint = Primary)
                            }
                            MaterialTheme(colorScheme = lightColorScheme(
                                surface = Color.White,
                                onSurface = Secondary,
                                onSurfaceVariant = Secondary.copy(alpha = 0.7f)
                            )) {
                                DropdownMenu(
                                    expanded = showExportMenu,
                                    onDismissRequest = { showExportMenu = false },
                                    modifier = Modifier.background(Color.White)
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Buku Kas (PDF)", fontWeight = FontWeight.Medium, color = Secondary) },
                                        onClick = {
                                            showExportMenu = false
                                            viewModel.exportBukuKasPdf()
                                        },
                                        leadingIcon = { Icon(Icons.Default.PictureAsPdf, null, tint = KeluarColor) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Buku Kas (Excel)", fontWeight = FontWeight.Medium, color = Secondary) },
                                        onClick = {
                                            showExportMenu = false
                                            viewModel.exportBukuKasExcel()
                                        },
                                        leadingIcon = { Icon(Icons.Default.TableChart, null, tint = MasukColor) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Laba Rugi (PDF)", fontWeight = FontWeight.Medium, color = Secondary) },
                                        onClick = {
                                            showExportMenu = false
                                            viewModel.exportLabaRugiPdf()
                                        },
                                        leadingIcon = { Icon(Icons.Default.PictureAsPdf, null, tint = KeluarColor) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Arus Kas (PDF)", fontWeight = FontWeight.Medium, color = Secondary) },
                                        onClick = {
                                            showExportMenu = false
                                            viewModel.exportArusKasPdf()
                                        },
                                        leadingIcon = { Icon(Icons.Default.PictureAsPdf, null, tint = KeluarColor) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Arus Kas (Excel)", fontWeight = FontWeight.Medium, color = Secondary) },
                                        onClick = {
                                            showExportMenu = false
                                            viewModel.exportArusKasExcel()
                                        },
                                        leadingIcon = { Icon(Icons.Default.TableChart, null, tint = MasukColor) }
                                    )
                                }
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showAddForm("masuk") },
                containerColor = Primary,
                contentColor = OnPrimary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, "Tambah Transaksi", modifier = Modifier.size(28.dp))
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Tertiary)
        ) {
            // ── Date Picker Filter State ──
            var showRangePicker by remember { mutableStateOf(false) }
            var showYearPicker by remember { mutableStateOf(false) }
            
            if (showRangePicker) {
                val datePickerState = rememberDatePickerState()
                val locale = remember { Locale("id", "ID") }
                val formatter = remember { DateTimeFormatter.ofPattern("MMMM yyyy", locale) }
                
                MaterialTheme(colorScheme = lightColorScheme(
                    surface = Color.White,
                    onSurface = Color.Black,
                    primary = Primary,
                    onPrimary = Color.White,
                    secondary = Secondary
                )) {
                    DatePickerDialog(
                        onDismissRequest = { showRangePicker = false },
                        confirmButton = {
                            TextButton(onClick = {
                                datePickerState.selectedDateMillis?.let {
                                    val ld = Instant.ofEpochMilli(it)
                                        .atZone(ZoneId.systemDefault())
                                        .toLocalDate()
                                    // Update filter range: set start and end to the selected day
                                    viewModel.setFilterDateRange(ld.toString(), ld.toString())
                                }
                                showRangePicker = false
                            }) { Text("Pilih", color = Primary, fontWeight = FontWeight.Bold) }
                        },
                        dismissButton = {
                            TextButton(onClick = { showRangePicker = false }) {
                                Text("Batal", color = OnSurfaceVariant)
                            }
                        },
                        colors = DatePickerDefaults.colors(containerColor = Color.White)
                    ) {
                        if (showYearPicker) {
                            val displayMonth = Instant.ofEpochMilli(datePickerState.displayedMonthMillis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                                
                            YearPickerDialog(
                                selectedYear = displayMonth.year,
                                onYearSelected = { year ->
                                    val cal = java.util.Calendar.getInstance().apply {
                                        timeInMillis = datePickerState.displayedMonthMillis
                                    }
                                    cal.set(java.util.Calendar.YEAR, year)
                                    datePickerState.displayedMonthMillis = cal.timeInMillis
                                    showYearPicker = false
                                },
                                onDismiss = { showYearPicker = false }
                            )
                        }

                        Column(modifier = Modifier.padding(top = 16.dp)) {
                            // Header Kustom < Bulan Tahun >
                            val displayMonth = Instant.ofEpochMilli(datePickerState.displayedMonthMillis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                            
                            val monthTitle = remember(displayMonth) { displayMonth.format(formatter) }
                            
                            CustomCalendarNavigation(
                                title = monthTitle.replaceFirstChar { it.uppercase() },
                                onPrevClick = {
                                    val cal = java.util.Calendar.getInstance().apply {
                                        timeInMillis = datePickerState.displayedMonthMillis
                                    }
                                    cal.add(java.util.Calendar.MONTH, -1)
                                    datePickerState.displayedMonthMillis = cal.timeInMillis
                                },
                                onNextClick = {
                                    val cal = java.util.Calendar.getInstance().apply {
                                        timeInMillis = datePickerState.displayedMonthMillis
                                    }
                                    cal.add(java.util.Calendar.MONTH, 1)
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

            // ── Filter Row ──
            FilterBar(
                selectedTipe = uiState.filterTipe,
                onSelectTipe = { viewModel.setFilterTipe(it) },
                startDate = uiState.filterStartDate,
                endDate = uiState.filterEndDate,
                onDateRangeClick = { showRangePicker = true }
            )

            // ── Saldo Card ──
            if (uiState.saldoData != null) {
                SaldoRingkasanCard(
                    totalMasuk = uiState.saldoData!!.totalMasuk,
                    totalKeluar = uiState.saldoData!!.totalKeluar,
                    saldoAkhir = uiState.saldoData!!.saldoAkhir,
                    isLoading = uiState.isLoadingSaldo
                )
            }

            // ── Daftar Transaksi ──
            if (uiState.isLoadingTransaksi && uiState.transaksiList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Primary)
                }
            } else if (uiState.transaksiList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.AccountBalance,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Neutral
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Belum ada transaksi",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Neutral
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Tekan + untuk menambah transaksi",
                            style = MaterialTheme.typography.bodySmall,
                            color = Neutral
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.transaksiList, key = { it.id }) { transaksi ->
                        TransaksiCard(
                            transaksi = transaksi,
                            onEdit = { viewModel.showEditForm(transaksi) },
                            onDelete = { viewModel.deleteTransaksi(transaksi.id) },
                            onAjukanApproval = { viewModel.ajukanApproval(transaksi.id) }
                        )
                    }

                    // Loading more indicator
                    if (uiState.isLoadingMore) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // ── Form Dialog ──
    if (uiState.showFormDialog) {
        FormTransaksiDialog(
            isEditing = uiState.isEditing,
            formTipe = uiState.formTipe,
            formNominal = uiState.formNominal,
            formKategoriId = uiState.formKategoriId,
            formTanggal = uiState.formTanggal,
            formMetodePembayaran = uiState.formMetodePembayaran,
            formKeterangan = uiState.formKeterangan,
            kategoriList = uiState.kategoriList,
            isSubmitting = uiState.isSubmitting,
            submitError = uiState.submitError,
            onFieldChanged = { tipe, nominal, kategoriId, tanggal, metode, keterangan ->
                viewModel.updateFormField(tipe, nominal, kategoriId, tanggal, metode, keterangan)
            },
            onSubmit = { viewModel.submitForm() },
            onDismiss = { viewModel.hideForm() }
        )
    }

    // ── Bottom Sheet Saldo ──
    if (uiState.showSaldoSheet) {
        SaldoBottomSheet(
            saldoData = uiState.saldoData,
            isLoading = uiState.isLoadingSaldo,
            error = uiState.saldoError,
            onDismiss = { viewModel.toggleSaldoSheet() }
        )
    }

    // ── Bottom Sheet Laba Rugi ──
    if (uiState.showLabaRugiSheet) {
        LabaRugiBottomSheet(
            labaRugiData = uiState.labaRugiData,
            isLoading = uiState.isLoadingLabaRugi,
            error = uiState.labaRugiError,
            onDismiss = { viewModel.toggleLabaRugiSheet() }
        )
    }

    // ── Bottom Sheet Arus Kas ──
    if (uiState.showArusKasSheet) {
        ArusKasBottomSheet(
            arusKasData = uiState.arusKasData,
            isLoading = uiState.isLoadingArusKas,
            error = uiState.arusKasError,
            onDismiss = { viewModel.toggleArusKasSheet() }
        )
    }

    // ── Loading overlay saat ekspor ──
    if (uiState.isExporting) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.7f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.padding(24.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = Primary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Mengekspor...", fontWeight = FontWeight.Medium, color = Secondary)
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  FILTER BAR
// ═══════════════════════════════════════════════════════════════

@Composable
private fun FilterBar(
    selectedTipe: String?,
    onSelectTipe: (String?) -> Unit,
    startDate: String,
    endDate: String,
    onDateRangeClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Surface,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // -- SEMUA --
            val isSemuaSelected = selectedTipe == null
            FilterChip(
                selected = isSemuaSelected,
                onClick = { onSelectTipe(null) },
                label = { 
                    Text(
                        text = "Semua", 
                        fontSize = 12.sp, 
                        fontWeight = FontWeight.Bold,
                        color = if (isSemuaSelected) Primary else Secondary
                    ) 
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Primary.copy(alpha = 0.08f),
                    containerColor = Color.White
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSemuaSelected,
                    borderColor = NeutralBorder,
                    selectedBorderColor = Primary.copy(alpha = 0.2f),
                    borderWidth = 1.dp
                ),
                shape = RoundedCornerShape(12.dp)
            )

            // -- PEMASUKAN --
            val isMasukSelected = selectedTipe == "masuk"
            FilterChip(
                selected = isMasukSelected,
                onClick = { onSelectTipe("masuk") },
                label = { 
                    Text(
                        text = "Pemasukan", 
                        fontSize = 12.sp, 
                        fontWeight = FontWeight.Bold,
                        color = if (isMasukSelected) MasukColor else Secondary
                    ) 
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MasukColor.copy(alpha = 0.08f),
                    containerColor = Color.White
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isMasukSelected,
                    borderColor = NeutralBorder,
                    selectedBorderColor = MasukColor.copy(alpha = 0.2f),
                    borderWidth = 1.dp
                ),
                shape = RoundedCornerShape(12.dp)
            )

            // -- PENGELUARAN --
            val isKeluarSelected = selectedTipe == "keluar"
            FilterChip(
                selected = isKeluarSelected,
                onClick = { onSelectTipe("keluar") },
                label = { 
                    Text(
                        text = "Pengeluaran", 
                        fontSize = 12.sp, 
                        fontWeight = FontWeight.Bold,
                        color = if (isKeluarSelected) KeluarColor else Secondary
                    ) 
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = KeluarColor.copy(alpha = 0.08f),
                    containerColor = Color.White
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isKeluarSelected,
                    borderColor = NeutralBorder,
                    selectedBorderColor = KeluarColor.copy(alpha = 0.2f),
                    borderWidth = 1.dp
                ),
                shape = RoundedCornerShape(12.dp)
            )

            VerticalDivider(modifier = Modifier.height(24.dp).padding(horizontal = 4.dp), color = Neutral)

            // Indikator periode (Pill shape)
            Surface(
                shape = RoundedCornerShape(100.dp),
                color = Primary.copy(alpha = 0.08f),
                modifier = Modifier.clickable { onDateRangeClick() },
                border = androidx.compose.foundation.BorderStroke(1.dp, Primary.copy(alpha = 0.15f))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${startDate.takeLast(5)} - ${endDate.takeLast(5)}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  SALDO RINGKASAN CARD
// ═══════════════════════════════════════════════════════════════

@Composable
private fun SaldoRingkasanCard(
    totalMasuk: Double,
    totalKeluar: Double,
    saldoAkhir: Double,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Neutral)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Saldo Akhir (Highlight)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Saldo Akhir",
                    style = MaterialTheme.typography.titleSmall,
                    color = OnSurfaceVariant
                )
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Primary, strokeWidth = 2.dp)
                } else {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (saldoAkhir >= 0) MasukBg else KeluarBg
                    ) {
                        Text(
                            text = if (saldoAkhir >= 0) "Surplus" else "Defisit",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (saldoAkhir >= 0) MasukColor else KeluarColor
                        )
                    }
                }
            }

            Text(
                text = LaporanKeuanganViewModel.formatRupiah(saldoAkhir),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = if (saldoAkhir >= 0) Secondary else KeluarColor
            )

            HorizontalDivider(color = Neutral, thickness = 1.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Pemasukan
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(8.dp).clip(CircleShape).background(MasukColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Pemasukan", fontSize = 10.sp, color = OnSurfaceVariant)
                        Text(
                            LaporanKeuanganViewModel.formatRupiah(totalMasuk),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                        )
                    }
                }

                // Pengeluaran
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(8.dp).clip(CircleShape).background(KeluarColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Pengeluaran", fontSize = 10.sp, color = OnSurfaceVariant)
                        Text(
                            LaporanKeuanganViewModel.formatRupiah(totalKeluar),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  TRANSAKSI CARD
// ═══════════════════════════════════════════════════════════════

@Composable
private fun TransaksiCard(
    transaksi: TransaksiKasDto,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAjukanApproval: () -> Unit = {}
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val statusApproval = transaksi.statusApproval // "disetujui", "pending", "ditolak", or null
    val isPending = statusApproval == "pending"
    val isDitolak = statusApproval == "ditolak"

    Column {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Neutral)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Indikator tipe dengan icon yang lebih besar
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (transaksi.tipe == "masuk") MasukBg else KeluarBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (transaksi.tipe == "masuk")
                            Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                        contentDescription = null,
                        tint = if (transaksi.tipe == "masuk") MasukColor else KeluarColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Info Utama
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = transaksi.kategoriTransaksi?.namaKategori ?: "Umum",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Secondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Event, null, tint = OnSurfaceVariant, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = transaksi.tanggal,
                            fontSize = 11.sp,
                            color = OnSurfaceVariant
                        )

                        if (!transaksi.metodePembayaran.isNullOrBlank()) {
                            Text(" • ", color = OnSurfaceVariant)
                            Text(
                                text = transaksi.metodePembayaran,
                                fontSize = 11.sp,
                                color = OnSurfaceVariant
                            )
                        }

                        if (!transaksi.keterangan.isNullOrBlank()) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.Default.Description,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = OnSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = transaksi.keterangan,
                                fontSize = 11.sp,
                                color = OnSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Status approval badge
                    if (statusApproval != null && statusApproval != "disetujui") {
                        Spacer(modifier = Modifier.height(4.dp))
                        val (badgeColor, badgeBg, badgeText) = when (statusApproval) {
                            "pending" -> Triple(WarningColor, WarningBg, "Menunggu Approval")
                            "ditolak" -> Triple(KeluarColor, KeluarBg, "Ditolak")
                            else -> Triple(MasukColor, MasukBg, "Disetujui")
                        }
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = badgeBg
                        ) {
                            Text(
                                text = badgeText,
                                color = badgeColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Nominal & Actions
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = (if (transaksi.tipe == "masuk") "+" else "-") +
                               LaporanKeuanganViewModel.formatRupiah(transaksi.nominal),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = if (transaksi.tipe == "masuk") MasukColor else KeluarColor
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row {
                        IconButton(
                            onClick = onEdit,
                            enabled = !isPending,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                "Edit",
                                tint = if (isPending) Neutral.copy(alpha = 0.3f) else Secondary.copy(alpha = 0.6f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(
                            onClick = { showDeleteConfirm = true },
                            enabled = !isPending,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                "Hapus",
                                tint = if (isPending) Neutral.copy(alpha = 0.3f) else KeluarColor.copy(alpha = 0.6f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // Tombol Ajukan Approval (khusus ditolak — bisa diajukan ulang)
        if (isDitolak) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = onAjukanApproval,
                    colors = ButtonDefaults.buttonColors(containerColor = WarningColor),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ajukan Approval", fontSize = 12.sp)
                }
            }
        }
    }

    // ── Konfirmasi Hapus ──
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Hapus Transaksi") },
            text = { Text("Yakin ingin menghapus transaksi ini? Tindakan ini tidak dapat dibatalkan.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = KeluarColor)
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

// ═══════════════════════════════════════════════════════════════
//  FORM DIALOG (Tambah / Edit)
// ═══════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormTransaksiDialog(
    isEditing: Boolean,
    formTipe: String,
    formNominal: String,
    formKategoriId: Int?,
    formTanggal: String,
    formMetodePembayaran: String,
    formKeterangan: String,
    kategoriList: List<KategoriTransaksiDto>,
    isSubmitting: Boolean,
    submitError: String?,
    onFieldChanged: (
        tipe: String?,
        nominal: String?,
        kategoriId: Int?,
        tanggal: String?,
        metode: String?,
        keterangan: String?
    ) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit
) {
    // Suppress unused warning if needed, or use it. Currently just removing warning
    val _unused = formMetodePembayaran 
    val filteredKategori = kategoriList.filter { it.tipe == formTipe || it.tipe.isEmpty() }

    Dialog(onDismissRequest = { if (!isSubmitting) onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (isEditing) "Edit Transaksi" else "Tambah Transaksi",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Secondary
                )

                HorizontalDivider(color = Neutral)

                if (submitError != null) {
                    Surface(shape = RoundedCornerShape(12.dp), color = KeluarBg) {
                        Text(
                            submitError,
                            color = KeluarColor,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                // Tipe Selection
                if (!isEditing) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f).clickable { onFieldChanged("masuk", null, null, null, null, null) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (formTipe == "masuk") MasukBg else Tertiary,
                            border = if (formTipe == "masuk") androidx.compose.foundation.BorderStroke(1.dp, MasukColor) else null
                        ) {
                            Text("Masuk", modifier = Modifier.padding(12.dp), textAlign = TextAlign.Center,
                                 fontWeight = FontWeight.Bold, color = if (formTipe == "masuk") MasukColor else OnSurfaceVariant)
                        }
                        Surface(
                            modifier = Modifier.weight(1f).clickable { onFieldChanged("keluar", null, null, null, null, null) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (formTipe == "keluar") KeluarBg else Tertiary,
                            border = if (formTipe == "keluar") androidx.compose.foundation.BorderStroke(1.dp, KeluarColor) else null
                        ) {
                            Text("Keluar", modifier = Modifier.padding(12.dp), textAlign = TextAlign.Center,
                                 fontWeight = FontWeight.Bold, color = if (formTipe == "keluar") KeluarColor else OnSurfaceVariant)
                        }
                    }
                }

                // Input Nominal
                CustomTextField(
                    value = formNominal,
                    onValueChange = { if (it.all { char -> char.isDigit() }) onFieldChanged(null, it, null, null, null, null) },
                    label = "Nominal",
                    prefix = { Text("Rp ", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                // Kategori Dropdown
                var expanded by remember { mutableStateOf(false) }
                val selectedKategori = filteredKategori.find { it.id == formKategoriId }

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    CustomTextField(
                        value = selectedKategori?.namaKategori ?: "Pilih Kategori",
                        onValueChange = {},
                        readOnly = true,
                        label = "Kategori",
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        filteredKategori.forEach { kategori ->
                            DropdownMenuItem(
                                text = { Text(kategori.namaKategori) },
                                onClick = { onFieldChanged(null, null, kategori.id, null, null, null); expanded = false }
                            )
                        }
                    }
                }

                // Tanggal & Lainnya
                var showDatePicker by remember { mutableStateOf(false) }
                var showYearPicker by remember { mutableStateOf(false) }
                
                CustomTextField(
                    value = formTanggal,
                    onValueChange = {},
                    readOnly = true,
                    enabled = false,
                    label = "Tanggal",
                    placeholder = "Pilih tanggal transaksi",
                    modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                    trailingIcon = { Icon(Icons.Default.CalendarMonth, null, tint = Primary) }
                )

                if (showDatePicker) {
                    val datePickerState = rememberDatePickerState()
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
                                TextButton(onClick = {
                                    datePickerState.selectedDateMillis?.let {
                                        val ld = Instant.ofEpochMilli(it)
                                            .atZone(ZoneId.systemDefault())
                                            .toLocalDate()
                                        onFieldChanged(null, null, null, ld.toString(), null, null)
                                    }
                                    showDatePicker = false
                                }) { Text("Pilih", color = Primary, fontWeight = FontWeight.Bold) }
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
                                        val cal = java.util.Calendar.getInstance().apply {
                                            timeInMillis = datePickerState.displayedMonthMillis
                                        }
                                        cal.set(java.util.Calendar.YEAR, year)
                                        datePickerState.displayedMonthMillis = cal.timeInMillis
                                        showYearPicker = false
                                    },
                                    onDismiss = { showYearPicker = false }
                                )
                            }

                            Column(modifier = Modifier.padding(top = 16.dp)) {
                                val displayMonth = Instant.ofEpochMilli(datePickerState.displayedMonthMillis)
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate()
                                
                                val monthTitle = displayMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale("id", "ID")))
                                
                                CustomCalendarNavigation(
                                    title = monthTitle.replaceFirstChar { it.uppercase() },
                                    onPrevClick = {
                                        val cal = java.util.Calendar.getInstance().apply {
                                            timeInMillis = datePickerState.displayedMonthMillis
                                        }
                                        cal.add(java.util.Calendar.MONTH, -1)
                                        datePickerState.displayedMonthMillis = cal.timeInMillis
                                    },
                                    onNextClick = {
                                        val cal = java.util.Calendar.getInstance().apply {
                                            timeInMillis = datePickerState.displayedMonthMillis
                                        }
                                        cal.add(java.util.Calendar.MONTH, 1)
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

                CustomTextField(
                    value = formKeterangan,
                    onValueChange = { onFieldChanged(null, null, null, null, null, it) },
                    label = "Catatan",
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Batal", color = OnSurfaceVariant)
                    }
                    Button(
                        onClick = onSubmit,
                        enabled = !isSubmitting,
                        modifier = Modifier.weight(1.5f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Secondary)
                    ) {
                        if (isSubmitting) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = OnPrimary)
                        else Text(if (isEditing) "Simpan" else "Tambah Transaksi")
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  BOTTOM SHEET SALDO
// ═══════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SaldoBottomSheet(
    saldoData: com.example.codasuaka.data.remote.dto.SaldoData?,
    isLoading: Boolean,
    error: String?,
    onDismiss: () -> Unit
) {
    MaterialTheme(colorScheme = lightColorScheme(
        surface = Color.White,
        onSurface = Color.Black,
        primary = Primary,
        onPrimary = Color.White,
        secondary = Secondary
    )) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Ringkasan Saldo",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge,
                    color = Secondary
                )

                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Primary)
                    }
                } else if (error != null) {
                    Text(error, color = KeluarColor)
                } else if (saldoData != null) {
                    // Total Masuk
                    SaldoRowItem(
                        icon = Icons.Default.ArrowDownward,
                        label = "Total Pemasukan",
                        amount = saldoData.totalMasuk,
                        color = MasukColor,
                        bgColor = MasukBg
                    )
                    // Total Keluar
                    SaldoRowItem(
                        icon = Icons.Default.ArrowUpward,
                        label = "Total Pengeluaran",
                        amount = saldoData.totalKeluar,
                        color = KeluarColor,
                        bgColor = KeluarBg
                    )
                    // Divider
                    HorizontalDivider(color = NeutralBorder.copy(alpha = 0.5f))
                    // Saldo Akhir
                    SaldoRowItem(
                        icon = Icons.Default.AccountBalance,
                        label = "Saldo Akhir",
                        amount = saldoData.saldoAkhir,
                        color = if (saldoData.saldoAkhir >= 0) MasukColor else KeluarColor,
                        bgColor = if (saldoData.saldoAkhir >= 0) MasukBg else KeluarBg
                    )

                    if (saldoData.startDate != null && saldoData.endDate != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Periode: ${saldoData.startDate} s/d ${saldoData.endDate}",
                            fontSize = 12.sp,
                            color = OnSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SaldoRowItem(
    icon: ImageVector,
    label: String,
    amount: Double,
    color: Color,
    bgColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label, 
                fontSize = 13.sp, 
                color = OnSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Text(
                LaporanKeuanganViewModel.formatRupiah(amount),
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = color
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  BOTTOM SHEET LABA RUGI
// ═══════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LabaRugiBottomSheet(
    labaRugiData: com.example.codasuaka.data.remote.dto.LabaRugiData?,
    isLoading: Boolean,
    error: String?,
    onDismiss: () -> Unit
) {
    MaterialTheme(colorScheme = lightColorScheme(
        surface = Color.White,
        onSurface = Color.Black,
        primary = Primary,
        onPrimary = Color.White,
        secondary = Secondary
    )) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Laporan Laba Rugi",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge,
                    color = Secondary
                )

                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Primary)
                    }
                } else if (error != null) {
                    Text(error, color = KeluarColor)
                } else if (labaRugiData != null) {
                    // Pendapatan
                    SaldoRowItem(
                        icon = Icons.Default.TrendingUp,
                        label = "Pendapatan",
                        amount = labaRugiData.pendapatan,
                        color = MasukColor,
                        bgColor = MasukBg
                    )
                    // HPP
                    SaldoRowItem(
                        icon = Icons.Default.ShoppingCart,
                        label = "HPP (Harga Pokok Penjualan)",
                        amount = labaRugiData.hpp,
                        color = WarningColor,
                        bgColor = WarningBg
                    )
                    // Beban Operasional
                    SaldoRowItem(
                        icon = Icons.Default.Receipt,
                        label = "Beban Operasional",
                        amount = labaRugiData.bebanOperasional,
                        color = KeluarColor,
                        bgColor = KeluarBg
                    )
                    // Divider
                    HorizontalDivider(color = NeutralBorder.copy(alpha = 0.5f))
                    // Laba Rugi
                    val isProfit = labaRugiData.labaRugi >= 0
                    SaldoRowItem(
                        icon = if (isProfit) Icons.Default.ThumbUp else Icons.Default.ThumbDown,
                        label = if (isProfit) "Laba Bersih" else "Rugi Bersih",
                        amount = kotlin.math.abs(labaRugiData.labaRugi),
                        color = if (isProfit) MasukColor else KeluarColor,
                        bgColor = if (isProfit) MasukBg else KeluarBg
                    )

                    if (labaRugiData.startDate != null && labaRugiData.endDate != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Periode: ${labaRugiData.startDate} s/d ${labaRugiData.endDate}",
                            fontSize = 12.sp,
                            color = OnSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  BOTTOM SHEET ARUS KAS
// ═══════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArusKasBottomSheet(
    arusKasData: ArusKasData?,
    isLoading: Boolean,
    error: String?,
    onDismiss: () -> Unit
) {
    MaterialTheme(colorScheme = lightColorScheme(
        surface = Color.White,
        onSurface = Color.Black,
        primary = Primary,
        onPrimary = Color.White,
        secondary = Secondary
    )) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Laporan Arus Kas",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge,
                    color = Secondary
                )

                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(150.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Primary)
                    }
                } else if (error != null) {
                    Text(error, color = KeluarColor)
                } else if (arusKasData != null) {
                    // ── Arus Kas Operasi ──
                    Text(
                        text = "Arus Kas dari Aktivitas Operasi", 
                        fontWeight = FontWeight.SemiBold, 
                        fontSize = 14.sp,
                        color = Secondary
                    )
                    if (!arusKasData.detailOperasi.isNullOrEmpty()) {
                        arusKasData.detailOperasi.forEach { detail ->
                            ArusKasDetailRow(detail)
                        }
                    }
                    SaldoRowItem(
                        icon = Icons.Default.TrendingUp,
                        label = "Total Arus Kas Operasi",
                        amount = arusKasData.arusKasOperasi,
                        color = if (arusKasData.arusKasOperasi >= 0) MasukColor else KeluarColor,
                        bgColor = if (arusKasData.arusKasOperasi >= 0) MasukBg else KeluarBg
                    )

                    HorizontalDivider(color = NeutralBorder.copy(alpha = 0.5f))

                    // ── Arus Kas Investasi ──
                    Text(
                        text = "Arus Kas dari Aktivitas Investasi", 
                        fontWeight = FontWeight.SemiBold, 
                        fontSize = 14.sp,
                        color = Secondary
                    )
                    SaldoRowItem(
                        icon = Icons.Default.Build,
                        label = "Total Arus Kas Investasi",
                        amount = arusKasData.arusKasInvestasi,
                        color = if (arusKasData.arusKasInvestasi >= 0) MasukColor else KeluarColor,
                        bgColor = if (arusKasData.arusKasInvestasi >= 0) MasukBg else KeluarBg
                    )

                    HorizontalDivider(color = NeutralBorder.copy(alpha = 0.5f))

                    // ── Arus Kas Pendanaan ──
                    Text(
                        text = "Arus Kas dari Aktivitas Pendanaan", 
                        fontWeight = FontWeight.SemiBold, 
                        fontSize = 14.sp,
                        color = Secondary
                    )
                    if (!arusKasData.detailPendanaan.isNullOrEmpty()) {
                        arusKasData.detailPendanaan.forEach { detail ->
                            ArusKasDetailRow(detail)
                        }
                    }
                    SaldoRowItem(
                        icon = Icons.Default.AccountBalance,
                        label = "Total Arus Kas Pendanaan",
                        amount = arusKasData.arusKasPendanaan,
                        color = if (arusKasData.arusKasPendanaan >= 0) MasukColor else KeluarColor,
                        bgColor = if (arusKasData.arusKasPendanaan >= 0) MasukBg else KeluarBg
                    )

                    HorizontalDivider(color = NeutralBorder.copy(alpha = 0.8f))

                    // ── Ringkasan ──
                    SaldoRowItem(
                        icon = Icons.Default.TrendingUp,
                        label = "Kenaikan Bersih Kas",
                        amount = arusKasData.kenaikanBersihKas,
                        color = if (arusKasData.kenaikanBersihKas >= 0) MasukColor else KeluarColor,
                        bgColor = if (arusKasData.kenaikanBersihKas >= 0) MasukBg else KeluarBg
                    )
                    SaldoRowItem(
                        icon = Icons.Default.AccountBalanceWallet,
                        label = "Saldo Awal",
                        amount = arusKasData.saldoAwal,
                        color = InfoColor,
                        bgColor = InfoBg
                    )
                    SaldoRowItem(
                        icon = Icons.Default.AccountBalanceWallet,
                        label = "Saldo Akhir",
                        amount = arusKasData.saldoAkhir,
                        color = InfoColor,
                        bgColor = InfoBg
                    )

                    if (arusKasData.startDate != null && arusKasData.endDate != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Periode: ${arusKasData.startDate} s/d ${arusKasData.endDate}",
                            fontSize = 12.sp,
                            color = OnSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ArusKasDetailRow(detail: ArusKasDetail) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            detail.kategori,
            fontSize = 13.sp,
            color = OnSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        if (detail.masuk > 0) {
            Text(
                LaporanKeuanganViewModel.formatRupiah(detail.masuk),
                fontSize = 13.sp,
                color = MasukColor,
                fontWeight = FontWeight.Medium
            )
        } else if (detail.keluar > 0) {
            Text(
                LaporanKeuanganViewModel.formatRupiah(detail.keluar),
                fontSize = 13.sp,
                color = KeluarColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
