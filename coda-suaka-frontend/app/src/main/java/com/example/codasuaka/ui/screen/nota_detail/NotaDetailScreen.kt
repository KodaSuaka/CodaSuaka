package com.example.codasuaka.ui.screen.nota_detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codasuaka.data.remote.dto.NotaDto
import com.example.codasuaka.data.remote.dto.NotaItemDto
import com.example.codasuaka.ui.components.CodaSuakaSnackbarHost
import com.example.codasuaka.ui.screen.nota_pembelian.formatRupiah
import com.example.codasuaka.ui.theme.*
import com.example.codasuaka.util.ErrorMessageMapper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotaDetailScreen(
    onBack: () -> Unit,
    onDeleted: () -> Unit,
    viewModel: NotaDetailViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // ─── Snackbar ───
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.loadError) {
        uiState.loadError?.let {
            snackbarHostState.showSnackbar(ErrorMessageMapper.map(it, "memuat detail nota").message)
            viewModel.clearError()
        }
    }
    LaunchedEffect(uiState.downloadError) {
        uiState.downloadError?.let {
            snackbarHostState.showSnackbar(ErrorMessageMapper.map(it, "mengunduh PDF").message)
            viewModel.clearError()
        }
    }
    LaunchedEffect(uiState.downloadSuccessPath) {
        uiState.downloadSuccessPath?.let { path ->
            snackbarHostState.showSnackbar("PDF tersimpan di Downloads: $path")
            viewModel.clearDownloadSuccess()
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
            onDeleted()
        }
    }

    // ─── Dialog konfirmasi hapus ───
    var showDeleteDialog by remember { mutableStateOf(false) }
    if (showDeleteDialog) {
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
                onDismissRequest = { showDeleteDialog = false },
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
                    Text("Yakin hapus nota ${uiState.nota?.nomorNota ?: ""}? Tindakan ini tidak bisa dibatalkan.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteNota()
                            showDeleteDialog = false
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
                    TextButton(onClick = { showDeleteDialog = false }) {
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
                            text = "Detail Nota",
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
                    actions = {},
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface)
                )
            },
            containerColor = Tertiary
        ) { paddingValues ->
            when {
                uiState.isLoading && uiState.nota == null -> {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Primary)
                    }
                }

                uiState.nota == null -> {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(paddingValues).padding(32.dp),
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
                                "Detail nota tidak ditemukan",
                                fontWeight = FontWeight.Medium,
                                color = OnSurfaceVariant
                            )
                        }
                    }
                }

                else -> {
                    val nota = uiState.nota!!
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        HeaderCard(nota = nota)
                        ItemsCard(items = nota.items.orEmpty())
                        TotalCard(nota = nota)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { viewModel.downloadPdf() },
                                enabled = !uiState.isDownloading,
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = OnPrimary)
                            ) {
                                if (uiState.isDownloading) {
                                    CircularProgressIndicator(
                                        color = OnPrimary,
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(18.dp)
                                    )
                                } else {
                                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Unduh PDF", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }

                            Button(
                                onClick = { showDeleteDialog = true },
                                enabled = !uiState.isDeleting,
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Error,
                                    contentColor = OnPrimary
                                )
                            ) {
                                if (uiState.isDeleting) {
                                    CircularProgressIndicator(
                                        color = OnPrimary,
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(18.dp)
                                    )
                                } else {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Hapus", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── Header ────────────────────────────────────────────────

@Composable
private fun HeaderCard(nota: NotaDto) {
    val isPembelian = nota.tipe.equals("pembelian", ignoreCase = true)
    val badgeColor = if (isPembelian) Primary else Secondary
    val badgeLabel = if (isPembelian) "Pembelian" else "Penjualan"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = nota.nomorNota,
                    fontWeight = FontWeight.ExtraBold,
                    color = Secondary,
                    fontSize = 16.sp,
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

            DetailRow(label = "Tanggal", value = nota.tanggal)
            nota.pihakTerkait?.let { DetailRow(label = "Pihak Terkait", value = it) }
            nota.metodePembayaran?.let { DetailRow(label = "Metode", value = it) }
            nota.catatan?.let { DetailRow(label = "Catatan", value = it) }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = OnSurfaceVariant,
            modifier = Modifier.width(120.dp)
        )
        Text(
            text = value,
            fontSize = 12.sp,
            color = OnSurface,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
    }
}

// ─── Daftar item ───────────────────────────────────────────

@Composable
private fun ItemsCard(items: List<NotaItemDto>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "Item Nota (${items.size})",
                fontWeight = FontWeight.Bold,
                color = Secondary,
                fontSize = 14.sp
            )

            if (items.isEmpty()) {
                Text("Tidak ada item", fontSize = 12.sp, color = OnSurfaceVariant)
            } else {
                // Header
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text("Item", fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = OnSurfaceVariant, modifier = Modifier.weight(2.2f))
                    Text("Qty", fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = OnSurfaceVariant, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                    Text("Harga", fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = OnSurfaceVariant, textAlign = TextAlign.End, modifier = Modifier.weight(1.4f))
                    Text("Subtotal", fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = OnSurfaceVariant, textAlign = TextAlign.End, modifier = Modifier.weight(1.6f))
                }
                HorizontalDivider(color = NeutralBorder, thickness = 1.dp)

                items.forEach { item ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(2.2f)) {
                            Text(
                                text = item.namaItem,
                                fontSize = 12.sp,
                                color = OnSurface,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = item.satuan,
                                fontSize = 10.sp,
                                color = OnSurfaceVariant
                            )
                        }
                        Text(
                            text = formatQty(item.kuantitas),
                            fontSize = 12.sp,
                            color = OnSurface,
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = formatRupiah(item.hargaSatuan),
                            fontSize = 12.sp,
                            color = OnSurface,
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(1.4f)
                        )
                        Text(
                            text = formatRupiah(item.subtotal),
                            fontSize = 12.sp,
                            color = OnSurface,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(1.6f)
                        )
                    }
                }
            }
        }
    }
}

// ─── Total ─────────────────────────────────────────────────

@Composable
private fun TotalCard(nota: NotaDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TOTAL",
                    fontWeight = FontWeight.ExtraBold,
                    color = Secondary,
                    fontSize = 15.sp
                )
                Text(
                    text = formatRupiah(nota.total),
                    fontWeight = FontWeight.ExtraBold,
                    color = Secondary,
                    fontSize = 18.sp
                )
            }
        }
    }
}

private fun formatQty(value: Double): String {
    return if (value == value.toLong().toDouble()) {
        value.toLong().toString()
    } else {
        value.toString()
    }
}
