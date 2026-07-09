package com.example.codasuaka.ui.screen.laporan_keuangan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
private val SurfaceCard = Color(0xFFF8FAFC)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaporanKeuanganScreen(
    onBack: () -> Unit,
    viewModel: LaporanKeuanganViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    // ── Snackbar ──
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.submitSuccess) {
        uiState.submitSuccess?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSubmitSuccess()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text("Buku Kas", fontWeight = FontWeight.SemiBold, color = OnPrimary)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Kembali", tint = OnPrimary)
                    }
                },
                actions = {
                    // Tombol Saldo
                    IconButton(onClick = { viewModel.toggleSaldoSheet() }) {
                        Icon(Icons.Default.AccountBalanceWallet, "Saldo", tint = OnPrimary)
                    }
                    // Tombol Laba Rugi
                    IconButton(onClick = { viewModel.toggleLabaRugiSheet() }) {
                        Icon(Icons.Default.BarChart, "Laba Rugi", tint = OnPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showAddForm("masuk") },
                containerColor = Primary
            ) {
                Icon(Icons.Default.Add, "Tambah Transaksi", tint = OnPrimary)
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
                            onDelete = { viewModel.deleteTransaksi(transaksi.id) }
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceCard)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Filter Tipe
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = selectedTipe == null,
                onClick = { onSelectTipe(null) },
                label = { Text("Semua", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Primary.copy(alpha = 0.15f)
                )
            )
            FilterChip(
                selected = selectedTipe == "masuk",
                onClick = { onSelectTipe("masuk") },
                label = { Text("Masuk", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MasukColor.copy(alpha = 0.15f)
                )
            )
            FilterChip(
                selected = selectedTipe == "keluar",
                onClick = { onSelectTipe("keluar") },
                label = { Text("Keluar", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = KeluarColor.copy(alpha = 0.15f)
                )
            )

            Spacer(modifier = Modifier.weight(1f))

            // Indikator periode
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = Neutral
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${startDate.takeLast(5)} - ${endDate.takeLast(5)}",
                    fontSize = 12.sp,
                    color = Neutral
                )
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
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Primary)
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Masuk
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Pemasukan", fontSize = 11.sp, color = Neutral)
                    Text(
                        LaporanKeuanganViewModel.formatRupiah(totalMasuk),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MasukColor
                    )
                }
                // Keluar
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Pengeluaran", fontSize = 11.sp, color = Neutral)
                    Text(
                        LaporanKeuanganViewModel.formatRupiah(totalKeluar),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = KeluarColor
                    )
                }
                // Saldo
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Saldo Akhir", fontSize = 11.sp, color = Neutral)
                    Text(
                        LaporanKeuanganViewModel.formatRupiah(saldoAkhir),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (saldoAkhir >= 0) Primary else KeluarColor
                    )
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
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Indikator tipe
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (transaksi.tipe == "masuk") MasukBg else KeluarBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (transaksi.tipe == "masuk")
                        Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                    contentDescription = null,
                    tint = if (transaksi.tipe == "masuk") MasukColor else KeluarColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                // Nama kategori + nominal
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = transaksi.kategoriTransaksi?.namaKategori ?: "Tanpa Kategori",
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = LaporanKeuanganViewModel.formatRupiah(transaksi.nominal),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (transaksi.tipe == "masuk") MasukColor else KeluarColor
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Tanggal + keterangan
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = Neutral
                    )
                    Text(
                        text = transaksi.tanggal,
                        fontSize = 11.sp,
                        color = Neutral
                    )
                    if (!transaksi.keterangan.isNullOrBlank()) {
                        Icon(
                            Icons.Default.Description,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = Neutral
                        )
                        Text(
                            text = transaksi.keterangan,
                            fontSize = 11.sp,
                            color = Neutral,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Metode pembayaran
                if (!transaksi.metodePembayaran.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = transaksi.metodePembayaran,
                        fontSize = 10.sp,
                        color = Neutral
                    )
                }
            }

            // Tombol edit & delete
            Column {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        modifier = Modifier.size(16.dp),
                        tint = InfoColor
                    )
                }
                IconButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Hapus",
                        modifier = Modifier.size(16.dp),
                        tint = KeluarColor
                    )
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
    // Filter kategori based on tipe
    val filteredKategori = kategoriList.filter { it.tipe == formTipe || it.tipe.isEmpty() }

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = {
            Text(
                if (isEditing) "Edit Transaksi" else "Tambah Transaksi",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Error
                if (submitError != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = KeluarColor.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            submitError,
                            color = KeluarColor,
                            modifier = Modifier.padding(8.dp),
                            fontSize = 13.sp
                        )
                    }
                }

                // Tipe (hanya saat tambah, atau jika edit)
                if (!isEditing) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = formTipe == "masuk",
                            onClick = { onFieldChanged("masuk", null, null, null, null, null) },
                            label = { Text("Pemasukan") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MasukColor.copy(alpha = 0.15f)
                            )
                        )
                        FilterChip(
                            selected = formTipe == "keluar",
                            onClick = { onFieldChanged("keluar", null, null, null, null, null) },
                            label = { Text("Pengeluaran") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = KeluarColor.copy(alpha = 0.15f)
                            )
                        )
                    }
                }

                // Tanggal
                OutlinedTextField(
                    value = formTanggal,
                    onValueChange = { onFieldChanged(null, null, null, it, null, null) },
                    label = { Text("Tanggal") },
                    placeholder = { Text("YYYY-MM-DD") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii)
                )

                // Kategori
                if (filteredKategori.isNotEmpty()) {
                    var expanded by remember { mutableStateOf(false) }
                    val selectedKategori = filteredKategori.find { it.id == formKategoriId }

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedKategori?.namaKategori ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Kategori") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            singleLine = true
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            filteredKategori.forEach { kategori ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(kategori.namaKategori, fontWeight = FontWeight.Medium)
                                            Text(
                                                "${kategori.sifat} · ${if (kategori.termasukHpp) "Termasuk HPP" else "Non-HPP"}",
                                                fontSize = 11.sp,
                                                color = Neutral
                                            )
                                        }
                                    },
                                    onClick = {
                                        onFieldChanged(null, null, kategori.id, null, null, null)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        "Tidak ada kategori untuk tipe ini",
                        color = KeluarColor,
                        fontSize = 13.sp
                    )
                }

                // Nominal
                OutlinedTextField(
                    value = formNominal,
                    onValueChange = { value ->
                        // Hanya angka
                        if (value.all { it.isDigit() }) {
                            onFieldChanged(null, value, null, null, null, null)
                        }
                    },
                    label = { Text("Nominal (Rp)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    prefix = { Text("Rp ") }
                )

                // Metode Pembayaran
                OutlinedTextField(
                    value = formMetodePembayaran,
                    onValueChange = { onFieldChanged(null, null, null, null, it, null) },
                    label = { Text("Metode Pembayaran (opsional)") },
                    placeholder = { Text("Tunai / Transfer / QRIS") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Keterangan
                OutlinedTextField(
                    value = formKeterangan,
                    onValueChange = { onFieldChanged(null, null, null, null, null, it) },
                    label = { Text("Keterangan (opsional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSubmit,
                enabled = !isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = OnPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(if (isEditing) "Simpan" else "Tambah")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isSubmitting
            ) {
                Text("Batal")
            }
        }
    )
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
