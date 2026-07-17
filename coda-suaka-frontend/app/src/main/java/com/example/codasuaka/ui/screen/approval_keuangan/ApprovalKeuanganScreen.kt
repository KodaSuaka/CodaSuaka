package com.example.codasuaka.ui.screen.approval_keuangan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.codasuaka.data.remote.dto.ApprovalLogDto
import com.example.codasuaka.ui.screen.laporan_keuangan.LaporanKeuanganViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApprovalKeuanganScreen(
    onBack: () -> Unit,
    viewModel: ApprovalKeuanganViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Snackbar untuk error & success
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSuccessMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Approval Transaksi") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tab Row: Pending | Riwayat
            TabRow(
                selectedTabIndex = uiState.selectedTab
            ) {
                Tab(
                    selected = uiState.selectedTab == 0,
                    onClick = { viewModel.setSelectedTab(0) },
                    text = { Text("Menunggu") },
                    icon = { Icon(Icons.Default.HourglassEmpty, contentDescription = null) }
                )
                Tab(
                    selected = uiState.selectedTab == 1,
                    onClick = { viewModel.setSelectedTab(1) },
                    text = { Text("Riwayat") },
                    icon = { Icon(Icons.Default.CheckCircle, contentDescription = null) }
                )
            }

            // Content
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                when {
                    uiState.isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    uiState.approvals.isEmpty() -> {
                        Text(
                            text = if (uiState.selectedTab == 0)
                                "Tidak ada transaksi yang menunggu approval"
                            else
                                "Belum ada riwayat approval",
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(32.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(uiState.approvals) { log ->
                                ApprovalCard(
                                    log = log,
                                    onClick = { viewModel.showDetail(log) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Detail Bottom Sheet
    if (uiState.showDetailSheet && uiState.selectedApprovalLog != null) {
        ApprovalDetailBottomSheet(
            log = uiState.selectedApprovalLog!!,
            isProcessing = uiState.isProcessing,
            onDismiss = { viewModel.hideDetail() },
            onSetujui = { viewModel.setujuiApproval() },
            onTolak = { viewModel.showTolakDialog() }
        )
    }

    // Dialog Penolakan
    if (uiState.showTolakDialog) {
        TolakDialog(
            catatan = uiState.catatanPenolakan,
            isProcessing = uiState.isProcessing,
            onCatatanChange = { viewModel.setCatatanPenolakan(it) },
            onConfirm = { viewModel.tolakApproval() },
            onDismiss = { viewModel.hideTolakDialog() }
        )
    }
}

@Composable
fun ApprovalCard(
    log: ApprovalLogDto,
    onClick: () -> Unit
) {
    val transaksi = log.transaksiKas
    val statusColor = when (log.status) {
        "pending" -> Color(0xFFFFA000)      // Orange
        "disetujui" -> Color(0xFF4CAF50)    // Green
        "ditolak" -> Color(0xFFF44336)      // Red
        else -> Color.Gray
    }
    val statusLabel = when (log.status) {
        "pending" -> "Menunggu"
        "disetujui" -> "Disetujui"
        "ditolak" -> "Ditolak"
        else -> log.status
    }
    val tipeColor = if (transaksi?.tipe == "masuk") Color(0xFF4CAF50) else Color(0xFFF44336)
    val tipeLabel = if (transaksi?.tipe == "masuk") "MASUK" else "KELUAR"

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status badge
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = statusLabel,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        color = statusColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Tipe badge
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = tipeColor.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = tipeLabel,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        color = tipeColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Kategori & nominal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = transaksi?.kategoriTransaksi?.namaKategori ?: "Tanpa Kategori",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = transaksi?.tanggal ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = formatRupiah(transaksi?.nominal ?: 0.0),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = tipeColor
                )
            }

            // Keterangan
            if (!transaksi?.keterangan.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = transaksi?.keterangan ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }

            // Pengaju
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Diajukan oleh: ${log.pengaju?.name ?: "-"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (log.pemeriksa != null) {
                    Text(
                        text = "Diproses oleh: ${log.pemeriksa.name ?: "-"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApprovalDetailBottomSheet(
    log: ApprovalLogDto,
    isProcessing: Boolean,
    onDismiss: () -> Unit,
    onSetujui: () -> Unit,
    onTolak: () -> Unit
) {
    val transaksi = log.transaksiKas
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Status badge
            val statusColor = when (log.status) {
                "pending" -> Color(0xFFFFA000)
                "disetujui" -> Color(0xFF4CAF50)
                "ditolak" -> Color(0xFFF44336)
                else -> Color.Gray
            }
            val statusLabel = when (log.status) {
                "pending" -> "MENUNGGU"
                "disetujui" -> "DISETUJUI"
                "ditolak" -> "DITOLAK"
                else -> log.status
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Detail Approval",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = statusLabel,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = statusColor,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Informasi Transaksi
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    InfoRow("Tanggal", transaksi?.tanggal ?: "-")
                    InfoRow("Tipe", if (transaksi?.tipe == "masuk") "Pemasukan" else "Pengeluaran")
                    InfoRow("Kategori", transaksi?.kategoriTransaksi?.namaKategori ?: "Tanpa Kategori")
                    InfoRow("Nominal", formatRupiah(transaksi?.nominal ?: 0.0))
                    InfoRow("Metode", transaksi?.metodePembayaran ?: "-")
                    InfoRow("Outlet", transaksi?.outlet?.namaOutlet ?: "-")
                    if (!transaksi?.keterangan.isNullOrBlank()) {
                        InfoRow("Keterangan", transaksi?.keterangan ?: "")
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Informasi Pengaju & Pemeriksa
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    InfoRow("Diajukan oleh", log.pengaju?.name ?: "-")
                    InfoRow("Tanggal diajukan", log.tanggalDiajukan)
                    if (log.pemeriksa != null) {
                        InfoRow("Diproses oleh", log.pemeriksa.name ?: "-")
                        InfoRow("Tanggal diproses", log.tanggalDiproses ?: "-")
                    }
                    if (!log.catatan.isNullOrBlank()) {
                        InfoRow("Catatan", log.catatan)
                    }
                }
            }

            // Tombol Aksi (hanya untuk pending)
            if (log.status == "pending") {
                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onTolak,
                        modifier = Modifier.weight(1f),
                        enabled = !isProcessing,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFF44336)
                        )
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Close, contentDescription = null)
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tolak")
                    }

                    Button(
                        onClick = onSetujui,
                        modifier = Modifier.weight(1f),
                        enabled = !isProcessing,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        )
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Setujui")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(0.6f),
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun TolakDialog(
    catatan: String,
    isProcessing: Boolean,
    onCatatanChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Tolak Transaksi", fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text(
                    text = "Berikan alasan penolakan:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = catatan,
                    onValueChange = onCatatanChange,
                    label = { Text("Catatan Penolakan") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    enabled = !isProcessing
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isProcessing && catatan.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF44336)
                )
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Text("Tolak")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isProcessing
            ) {
                Text("Batal")
            }
        }
    )
}

private fun formatRupiah(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    format.maximumFractionDigits = 0
    return format.format(amount)
}
