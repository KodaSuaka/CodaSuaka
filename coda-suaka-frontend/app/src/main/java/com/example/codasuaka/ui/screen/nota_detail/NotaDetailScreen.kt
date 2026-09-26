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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import android.bluetooth.BluetoothDevice
import com.example.codasuaka.data.remote.dto.NotaDto
import com.example.codasuaka.data.remote.dto.NotaItemDto
import com.example.codasuaka.ui.components.CodaSuakaSnackbarHost
import com.example.codasuaka.ui.theme.*
import com.example.codasuaka.ui.util.formatQty
import com.example.codasuaka.ui.util.formatRupiah
import com.example.codasuaka.util.BluetoothPrinterManager
import com.example.codasuaka.util.ErrorMessageMapper
import com.example.codasuaka.data.local.TokenManager
import org.koin.compose.koinInject
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotaDetailScreen(
    onBack: () -> Unit,
    onDeleted: () -> Unit,
    viewModel: NotaDetailViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val printerManager: BluetoothPrinterManager = koinInject()
    val tokenManager: TokenManager = koinInject()

    // Ambil nama user untuk audit struk
    var currentUserName by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        currentUserName = tokenManager.getUserName()
    }

    // ─── Bluetooth State ───
    var showPrinterDialog by remember { mutableStateOf(false) }
    var pairedDevices by remember { mutableStateOf<List<BluetoothDevice>>(emptyList()) }
    var isPrinting by remember { mutableStateOf(false) }

    // Launcher for Bluetooth Permissions
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val allGranted = perms.values.all { it }
        if (allGranted) {
            pairedDevices = printerManager.getPairedDevices()
            showPrinterDialog = true
        } else {
            scope.launch {
                // snackbarHostState.showSnackbar("Izin Bluetooth diperlukan untuk mencetak struk")
            }
        }
    }

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

    // ─── Gunakan tema aplikasi (CodaSuakaTheme)
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
                                tint = Primary
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

                        // Row Aksi: Unduh PDF & Cetak Struk
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
                                onClick = {
                                    if (printerManager.hasPermissions()) {
                                        pairedDevices = printerManager.getPairedDevices()
                                        showPrinterDialog = true
                                    } else {
                                        val perms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                            arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
                                        } else {
                                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
                                        }
                                        permissionLauncher.launch(perms)
                                    }
                                },
                                enabled = !isPrinting,
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Secondary, contentColor = OnPrimary)
                            ) {
                                if (isPrinting) {
                                    CircularProgressIndicator(
                                        color = OnPrimary,
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(18.dp)
                                    )
                                } else {
                                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Cetak Struk", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
            
            // ─── Dialog Pilih Printer ───
            if (showPrinterDialog) {
                AlertDialog(
                    onDismissRequest = { showPrinterDialog = false },
                    title = { Text("Pilih Printer Bluetooth", fontWeight = FontWeight.Bold, color = Secondary) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            if (pairedDevices.isEmpty()) {
                                Text("Tidak ada printer yang dipasangkan. Silakan hubungkan printer di pengaturan Bluetooth HP Anda.", fontSize = 13.sp, color = OnSurfaceVariant)
                            } else {
                                pairedDevices.forEach { device ->
                                    @SuppressLint("MissingPermission")
                                    Surface(
                                        onClick = {
                                            showPrinterDialog = false
                                            isPrinting = true
                                            scope.launch {
                                                val result = printerManager.printNota(device, uiState.nota!!, currentUserName)
                                                isPrinting = false
                                                if (result.isSuccess) {
                                                    snackbarHostState.showSnackbar("Nota berhasil dicetak")
                                                } else {
                                                    snackbarHostState.showSnackbar("Gagal mencetak: ${result.exceptionOrNull()?.message}")
                                                }
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        color = InputBackground,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, NeutralBorder.copy(alpha = 0.5f))
                                    ) {
                                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Bluetooth, null, tint = Primary)
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(device.name ?: "Unknown Device", fontWeight = FontWeight.Bold, color = Secondary)
                                                Text(device.address, fontSize = 11.sp, color = OnSurfaceVariant)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {},
                    dismissButton = {
                        TextButton(onClick = { showPrinterDialog = false }) { Text("Batal", color = OnSurfaceVariant) }
                    },
                    containerColor = Surface
                )
            }
    }
}

// ─── Header ────────────────────────────────────────────────

@Composable
private fun HeaderCard(nota: NotaDto) {
    val isPembelian = nota.tipe.equals("pembelian", ignoreCase = true)
    val badgeColor = if (isPembelian) Error else Success
    val badgeBg = if (isPembelian) Error.copy(alpha = 0.1f) else Success.copy(alpha = 0.1f)
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
                    Text("Qty", fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = OnSurfaceVariant, textAlign = TextAlign.End, maxLines = 1, modifier = Modifier.weight(1f))
                    Text("Harga", fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = OnSurfaceVariant, textAlign = TextAlign.End, maxLines = 1, modifier = Modifier.weight(1.4f))
                    Text("Subtotal", fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = OnSurfaceVariant, textAlign = TextAlign.End, maxLines = 1, modifier = Modifier.weight(1.6f))
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
                                color = OnSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = formatQty(item.kuantitas),
                            fontSize = 12.sp,
                            color = OnSurface,
                            textAlign = TextAlign.End,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = formatRupiah(item.hargaSatuan),
                            fontSize = 12.sp,
                            color = OnSurface,
                            textAlign = TextAlign.End,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1.4f)
                        )
                        Text(
                            text = formatRupiah(item.subtotal),
                            fontSize = 12.sp,
                            color = OnSurface,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.End,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
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

