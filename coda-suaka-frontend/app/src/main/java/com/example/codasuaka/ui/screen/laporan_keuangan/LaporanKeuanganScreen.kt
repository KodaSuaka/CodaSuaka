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
import com.example.codasuaka.ui.theme.*

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
                        Icon(Icons.Default.ArrowBack, "Kembali", tint = Secondary)
                    }
                },
                actions = {
                    // Tombol Arus Kas
                    IconButton(onClick = { viewModel.toggleArusKasSheet() }) {
                        Icon(Icons.Default.AccountTree, "Arus Kas", tint = OnPrimary)
                    }
                    // Tombol Saldo
                    IconButton(onClick = { viewModel.toggleSaldoSheet() }) {
                        Icon(Icons.Default.AccountBalanceWallet, "Saldo", tint = Secondary)
                    }
                    // Tombol Laba Rugi
                    IconButton(onClick = { viewModel.toggleLabaRugiSheet() }) {
                        Icon(Icons.Default.BarChart, "Laba Rugi", tint = Secondary)
                    }
                    // Tombol Ekspor (Dropdown)
                    Box {
                        IconButton(onClick = { showExportMenu = true }) {
                            Icon(Icons.Default.FileDownload, "Ekspor", tint = OnPrimary)
                        }
                        DropdownMenu(
                            expanded = showExportMenu,
                            onDismissRequest = { showExportMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Buku Kas (PDF)") },
                                onClick = {
                                    showExportMenu = false
                                    viewModel.exportBukuKasPdf()
                                },
                                leadingIcon = { Icon(Icons.Default.PictureAsPdf, null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Buku Kas (Excel)") },
                                onClick = {
                                    showExportMenu = false
                                    viewModel.exportBukuKasExcel()
                                },
                                leadingIcon = { Icon(Icons.Default.TableChart, null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Laba Rugi (PDF)") },
                                onClick = {
                                    showExportMenu = false
                                    viewModel.exportLabaRugiPdf()
                                },
                                leadingIcon = { Icon(Icons.Default.PictureAsPdf, null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Arus Kas (PDF)") },
                                onClick = {
                                    showExportMenu = false
                                    viewModel.exportArusKasPdf()
                                },
                                leadingIcon = { Icon(Icons.Default.PictureAsPdf, null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Arus Kas (Excel)") },
                                onClick = {
                                    showExportMenu = false
                                    viewModel.exportArusKasExcel()
                                },
                                leadingIcon = { Icon(Icons.Default.TableChart, null) }
                            )
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
            // ── Filter Row ──
            FilterBar(
                selectedTipe = uiState.filterTipe,
                onSelectTipe = { viewModel.setFilterTipe(it) },
                startDate = uiState.filterStartDate,
                endDate = uiState.filterEndDate
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
                    modifier = Modifier.fillMaxSize(),
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
            modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.padding(24.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = Primary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Mengekspor...", fontWeight = FontWeight.Medium)
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
    endDate: String
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
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = selectedTipe == null,
                onClick = { onSelectTipe(null) },
                label = { Text("Semua", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Secondary.copy(alpha = 0.1f),
                    selectedLabelColor = Secondary,
                    containerColor = Tertiary,
                    labelColor = OnSurfaceVariant
                ),
                border = null,
                shape = RoundedCornerShape(12.dp)
            )
            FilterChip(
                selected = selectedTipe == "masuk",
                onClick = { onSelectTipe("masuk") },
                label = { Text("Pemasukan", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MasukColor.copy(alpha = 0.1f),
                    selectedLabelColor = MasukColor,
                    containerColor = Tertiary,
                    labelColor = OnSurfaceVariant
                ),
                border = null,
                shape = RoundedCornerShape(12.dp)
            )
            FilterChip(
                selected = selectedTipe == "keluar",
                onClick = { onSelectTipe("keluar") },
                label = { Text("Pengeluaran", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = KeluarColor.copy(alpha = 0.1f),
                    selectedLabelColor = KeluarColor,
                    containerColor = Tertiary,
                    labelColor = OnSurfaceVariant
                ),
                border = null,
                shape = RoundedCornerShape(12.dp)
            )

            VerticalDivider(modifier = Modifier.height(24.dp).padding(horizontal = 4.dp), color = Neutral)

            // Indikator periode
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Tertiary,
                modifier = Modifier.clickable { /* future: date range picker */ }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Secondary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${startDate.takeLast(5)} - ${endDate.takeLast(5)}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Secondary
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
                            Icons.Default.TrendingUp else Icons.Default.TrendingDown,
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
                        Icons.Default.Send,
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
                OutlinedTextField(
                    value = formNominal,
                    onValueChange = { if (it.all { char -> char.isDigit() }) onFieldChanged(null, it, null, null, null, null) },
                    label = { Text("Nominal") },
                    prefix = { Text("Rp ", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = Neutral,
                        focusedContainerColor = Tertiary,
                        unfocusedContainerColor = Tertiary
                    )
                )

                // Kategori Dropdown
                var expanded by remember { mutableStateOf(false) }
                val selectedKategori = filteredKategori.find { it.id == formKategoriId }

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedKategori?.namaKategori ?: "Pilih Kategori",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Kategori") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = Neutral,
                            focusedContainerColor = Tertiary,
                            unfocusedContainerColor = Tertiary
                        )
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
                OutlinedTextField(
                    value = formTanggal,
                    onValueChange = { onFieldChanged(null, null, null, it, null, null) },
                    label = { Text("Tanggal (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = Neutral,
                        focusedContainerColor = Tertiary,
                        unfocusedContainerColor = Tertiary
                    )
                )

                OutlinedTextField(
                    value = formKeterangan,
                    onValueChange = { onFieldChanged(null, null, null, null, null, it) },
                    label = { Text("Catatan") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = Neutral,
                        focusedContainerColor = Tertiary,
                        unfocusedContainerColor = Tertiary
                    )
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
    ModalBottomSheet(onDismissRequest = onDismiss) {
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
                style = MaterialTheme.typography.titleLarge
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
                HorizontalDivider(color = Neutral.copy(alpha = 0.3f))
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
                        color = Neutral
                    )
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
                .clip(RoundedCornerShape(8.dp))
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 13.sp, color = Neutral)
            Text(
                LaporanKeuanganViewModel.formatRupiah(amount),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
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
    ModalBottomSheet(onDismissRequest = onDismiss) {
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
                style = MaterialTheme.typography.titleLarge
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
                HorizontalDivider(color = Neutral.copy(alpha = 0.3f))
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
                        color = Neutral
                    )
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
    ModalBottomSheet(onDismissRequest = onDismiss) {
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
                style = MaterialTheme.typography.titleLarge
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
                Text("Arus Kas dari Aktivitas Operasi", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
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

                HorizontalDivider(color = Neutral.copy(alpha = 0.3f))

                // ── Arus Kas Investasi ──
                Text("Arus Kas dari Aktivitas Investasi", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                SaldoRowItem(
                    icon = Icons.Default.Build,
                    label = "Total Arus Kas Investasi",
                    amount = arusKasData.arusKasInvestasi,
                    color = if (arusKasData.arusKasInvestasi >= 0) MasukColor else KeluarColor,
                    bgColor = if (arusKasData.arusKasInvestasi >= 0) MasukBg else KeluarBg
                )

                HorizontalDivider(color = Neutral.copy(alpha = 0.3f))

                // ── Arus Kas Pendanaan ──
                Text("Arus Kas dari Aktivitas Pendanaan", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
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

                HorizontalDivider(color = Neutral.copy(alpha = 0.5f))

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
                        color = Neutral
                    )
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
            color = Neutral,
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
