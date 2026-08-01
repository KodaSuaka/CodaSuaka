package com.example.codasuaka.ui.screen.nota_pembelian

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.example.codasuaka.data.remote.dto.BarangJasaDto
import com.example.codasuaka.ui.components.CodaSuakaSnackbarHost
import com.example.codasuaka.ui.theme.*
import com.example.codasuaka.util.ErrorMessageMapper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotaPembelianScreen(
    onBack: () -> Unit,
    viewModel: NotaPembelianViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // ─── Snackbar ───
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.loadError) {
        uiState.loadError?.let {
            snackbarHostState.showSnackbar(ErrorMessageMapper.map(it, "memuat data").message)
            viewModel.clearError()
        }
    }
    LaunchedEffect(uiState.submitError) {
        uiState.submitError?.let {
            snackbarHostState.showSnackbar(ErrorMessageMapper.map(it, "menyimpan nota").message)
            viewModel.clearError()
        }
    }
    LaunchedEffect(uiState.submitSuccessNota) {
        uiState.submitSuccessNota?.let { nota ->
            snackbarHostState.showSnackbar("Nota pembelian ${nota.nomorNota} berhasil disimpan")
            viewModel.clearSubmitSuccess()
        }
    }

    // ─── Picker tanggal ───
    val datePickerState = rememberDatePickerState()
    val showDatePicker = remember { mutableStateOf(false) }
    if (showDatePicker.value) {
        // ─── Force Light Theme (dialog render di window terpisah) ───
        MaterialTheme(
            colorScheme = lightColorScheme(
                primary = Primary,
                onPrimary = Color.White,
                secondary = Secondary,
                surface = Color.White,
                onSurface = OnSurface
            )
        ) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker.value = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = java.time.Instant.ofEpochMilli(millis)
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate()
                            viewModel.setTanggal(date.toString())
                        }
                        showDatePicker.value = false
                    }) { Text("Pilih") }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker.value = false }) { Text("Batal") }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }

    // ─── Picker file Excel ───
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { viewModel.onFileSelected(context, it) }
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
                            text = "Nota Pembelian",
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
                // ── Tab: Manual / Impor Excel ──
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ModeTab(
                        label = "Input Manual",
                        selected = uiState.mode == NotaPembelianMode.MANUAL,
                        onClick = { viewModel.setMode(NotaPembelianMode.MANUAL) },
                        modifier = Modifier.weight(1f)
                    )
                    ModeTab(
                        label = "Impor Excel",
                        selected = uiState.mode == NotaPembelianMode.IMPORT,
                        onClick = { viewModel.setMode(NotaPembelianMode.IMPORT) },
                        modifier = Modifier.weight(1f)
                    )
                }

                // ── Form umum (kedua mode) ──
                FormUmum(
                    tanggal = uiState.tanggal,
                    onPickDate = { showDatePicker.value = true },
                    outletList = uiState.outletList,
                    selectedOutletId = uiState.selectedOutletId,
                    onOutletChange = { viewModel.setOutlet(it) },
                    pihakTerkait = uiState.pihakTerkait,
                    onPihakTerkaitChange = { viewModel.setPihakTerkait(it) },
                    metodePembayaran = uiState.metodePembayaran,
                    onMetodeChange = { viewModel.setMetodePembayaran(it) },
                    catatan = uiState.catatan,
                    onCatatanChange = { viewModel.setCatatan(it) }
                )

                when (uiState.mode) {
                    NotaPembelianMode.MANUAL -> {
                        ManualSection(
                            modifier = Modifier.weight(1f),
                            uiState = uiState,
                            onSearchChange = { viewModel.onSearchQueryChange(it) },
                            onSelectKatalog = { viewModel.selectKatalogItem(it) },
                            onNamaChange = { viewModel.updateItemNama(it) },
                            onJenisChange = { viewModel.updateItemJenis(it) },
                            onKuantitasChange = { viewModel.updateItemKuantitas(it) },
                            onSatuanChange = { viewModel.updateItemSatuan(it) },
                            onHargaChange = { viewModel.updateItemHarga(it) },
                            onAddItem = { viewModel.addItemToCart() },
                            onRemoveItem = { index -> viewModel.removeCartItem(index) },
                            onClearCart = { viewModel.clearCart() },
                            onSubmit = { viewModel.submitManual() },
                            isSubmitting = uiState.isSubmitting
                        )
                    }

                    NotaPembelianMode.IMPORT -> {
                        ImportSection(
                            modifier = Modifier.weight(1f),
                            uiState = uiState,
                            onPickFile = { filePicker.launch(arrayOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "application/octet-stream")) },
                            onClearFile = { viewModel.clearImportFile() },
                            onSubmit = { viewModel.submitImport(context) },
                            isSubmitting = uiState.isSubmitting
                        )
                    }
                }
            }
        }
    }
}

// ─── Tab Mode ─────────────────────────────────────────────

@Composable
private fun ModeTab(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.clip(RoundedCornerShape(10.dp)),
        color = if (selected) Primary else InputBackground,
        onClick = onClick
    ) {
        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
            Text(
                text = label,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = if (selected) Color.White else OnSurfaceVariant
            )
        }
    }
}

// ─── Form Umum ────────────────────────────────────────────

@Composable
private fun FormUmum(
    tanggal: String,
    onPickDate: () -> Unit,
    outletList: List<com.example.codasuaka.data.remote.dto.OutletDto>,
    selectedOutletId: Int?,
    onOutletChange: (Int?) -> Unit,
    pihakTerkait: String,
    onPihakTerkaitChange: (String) -> Unit,
    metodePembayaran: String,
    onMetodeChange: (String) -> Unit,
    catatan: String,
    onCatatanChange: (String) -> Unit
) {
    val metodeOptions = listOf("Tunai", "Transfer", "QRIS", "Kartu Kredit", "Kartu Debit", "Lainnya")
    var metodeExpanded by remember { mutableStateOf(false) }
    var outletExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Tanggal
            OutlinedTextField(
                value = tanggal,
                onValueChange = {},
                readOnly = true,
                label = { Text("Tanggal") },
                trailingIcon = {
                    IconButton(onClick = onPickDate) {
                        Icon(Icons.Default.DateRange, contentDescription = "Pilih tanggal", tint = Primary)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Outlet dropdown
            Box {
                OutlinedTextField(
                    value = outletList.firstOrNull { it.id == selectedOutletId }?.namaOutlet ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Outlet (opsional)") },
                    placeholder = { Text("Pilih outlet") },
                    trailingIcon = {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth().clickable { outletExpanded = true }
                )
                DropdownMenu(
                    expanded = outletExpanded,
                    onDismissRequest = { outletExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Tanpa outlet") },
                        onClick = {
                            onOutletChange(null)
                            outletExpanded = false
                        }
                    )
                    outletList.forEach { outlet ->
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

            // Pihak terkait
            OutlinedTextField(
                value = pihakTerkait,
                onValueChange = onPihakTerkaitChange,
                label = { Text("Pihak Terkait (opsional)") },
                placeholder = { Text("Nama supplier/vendor") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Metode pembayaran
            Box {
                OutlinedTextField(
                    value = metodePembayaran,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Metode Pembayaran (opsional)") },
                    placeholder = { Text("Pilih metode") },
                    trailingIcon = {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth().clickable { metodeExpanded = true }
                )
                DropdownMenu(
                    expanded = metodeExpanded,
                    onDismissRequest = { metodeExpanded = false }
                ) {
                    metodeOptions.forEach { metode ->
                        DropdownMenuItem(
                            text = { Text(metode) },
                            onClick = {
                                onMetodeChange(metode)
                                metodeExpanded = false
                            }
                        )
                    }
                }
            }

            // Catatan
            OutlinedTextField(
                value = catatan,
                onValueChange = onCatatanChange,
                label = { Text("Catatan (opsional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ─── Manual Section ───────────────────────────────────────

@Composable
private fun ManualSection(
    modifier: Modifier = Modifier,
    uiState: NotaPembelianUiState,
    onSearchChange: (String) -> Unit,
    onSelectKatalog: (BarangJasaDto) -> Unit,
    onNamaChange: (String) -> Unit,
    onJenisChange: (String) -> Unit,
    onKuantitasChange: (String) -> Unit,
    onSatuanChange: (String) -> Unit,
    onHargaChange: (String) -> Unit,
    onAddItem: () -> Unit,
    onRemoveItem: (Int) -> Unit,
    onClearCart: () -> Unit,
    onSubmit: () -> Unit,
    isSubmitting: Boolean
) {
    // Modifier dari caller (weight(1f)) memberi tinggi terbatas, sehingga
    // verticalScroll di bawah ini aman dipakai (tidak infinite-height).
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // ── Tambah Item ──
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Tambah Item", fontWeight = FontWeight.Bold, color = Secondary, fontSize = 14.sp)

                // Search katalog
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = onSearchChange,
                    label = { Text("Cari barang/jasa dari katalog") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Primary) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Daftar katalog (maksimal ~4 baris)
                if (uiState.filteredKatalog.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 140.dp)
                    ) {
                        itemsIndexed(uiState.filteredKatalog) { _, produk ->
                            val selected = uiState.itemKatalogId == produk.id
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selected) InfoBg else Color.Transparent)
                                    .clickable { onSelectKatalog(produk) }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(produk.nama, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = OnSurface)
                                    Text(
                                        "${produk.satuan} • Stok: ${produk.stok ?: "-"}",
                                        fontSize = 11.sp,
                                        color = OnSurfaceVariant
                                    )
                                }
                                Text(
                                    formatRupiah(produk.hargaBeli ?: 0.0),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Primary
                                )
                            }
                        }
                    }
                }

                // Jenis: barang / jasa
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("barang" to "Barang", "jasa" to "Jasa").forEach { (value, label) ->
                        val selected = uiState.itemJenis == value
                        FilterChip(
                            selected = selected,
                            onClick = { onJenisChange(value) },
                            label = {
                                Text(
                                    label,
                                    fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Bold,
                                    color = if (selected) Color.White else Secondary
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Primary,
                                containerColor = InputBackground
                            ),
                            border = null,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                // Form item
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = uiState.itemNama,
                        onValueChange = onNamaChange,
                        label = { Text("Nama Item") },
                        singleLine = true,
                        modifier = Modifier.weight(2f)
                    )
                    OutlinedTextField(
                        value = uiState.itemKuantitas,
                        onValueChange = onKuantitasChange,
                        label = { Text("Qty") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = uiState.itemSatuan,
                        onValueChange = onSatuanChange,
                        label = { Text("Satuan") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = uiState.itemHarga,
                        onValueChange = onHargaChange,
                        label = { Text("Harga Satuan") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Button(
                    onClick = onAddItem,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Tambah ke Keranjang")
                }
            }
        }

        // ── Keranjang ──
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Daftar Item", fontWeight = FontWeight.Bold, color = Secondary, fontSize = 14.sp)
                    if (uiState.cartItems.isNotEmpty()) {
                        TextButton(onClick = onClearCart) {
                            Text("Kosongkan", color = Error, fontSize = 12.sp)
                        }
                    }
                }

                if (uiState.cartItems.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(
                            "Belum ada item. Tambahkan item dari katalog atau isi manual di atas.",
                            textAlign = TextAlign.Center,
                            color = OnSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    // Non-lazy: kartu ini sudah hidup di dalam parent yang
                    // verticalScroll (lihat ManualSection), LazyColumn tanpa
                    // tinggi tetap di dalam parent scroll akan crash.
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        uiState.cartItems.forEachIndexed { index, item ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.namaItem, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = OnSurface)
                                    Text(
                                        "${formatQty(item.kuantitas)} ${item.satuan} × ${formatRupiah(item.hargaSatuan)}",
                                        fontSize = 11.sp,
                                        color = OnSurfaceVariant
                                    )
                                }
                                Text(
                                    formatRupiah(item.subtotal),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = OnSurface
                                )
                                IconButton(onClick = { onRemoveItem(index) }) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Hapus item",
                                        tint = Error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = Neutral, thickness = 1.dp)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total", fontWeight = FontWeight.ExtraBold, color = Secondary, fontSize = 16.sp)
                    Text(
                        formatRupiah(uiState.cartTotal),
                        fontWeight = FontWeight.ExtraBold,
                        color = Primary,
                        fontSize = 16.sp
                    )
                }

                Button(
                    onClick = onSubmit,
                    enabled = uiState.cartItems.isNotEmpty() && !isSubmitting,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                    } else {
                        Icon(Icons.Default.Save, contentDescription = null)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isSubmitting) "Menyimpan..." else "Simpan Nota Pembelian")
                }
            }
        }
    }
}

// ─── Import Section ───────────────────────────────────────

@Composable
private fun ImportSection(
    modifier: Modifier = Modifier,
    uiState: NotaPembelianUiState,
    onPickFile: () -> Unit,
    onClearFile: () -> Unit,
    onSubmit: () -> Unit,
    isSubmitting: Boolean
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Format Template Excel", fontWeight = FontWeight.Bold, color = Secondary, fontSize = 14.sp)
                Text(
                    "Kolom (baris 1 = header, dilewati):\n" +
                        "1. nama_item | 2. jenis (barang/jasa) | 3. kuantitas | 4. satuan | 5. harga_satuan\n\n" +
                        "Contoh baris data:\n" +
                        "Shampoo | barang | 10 | botol | 25000\n" +
                        "Hair Spa | jasa | 5 | pcs | 150000",
                    fontSize = 12.sp,
                    color = OnSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }

        // File picker
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Pilih File .xlsx", fontWeight = FontWeight.Bold, color = Secondary, fontSize = 14.sp)

                if (uiState.importFile == null) {
                    Button(
                        onClick = onPickFile,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Icon(Icons.Default.UploadFile, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pilih File Excel")
                    }
                } else {
                    val file = uiState.importFile
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Description, contentDescription = null, tint = Primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(file.fileName, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = OnSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(
                                formatFileSize(file.sizeBytes),
                                fontSize = 11.sp,
                                color = OnSurfaceVariant
                            )
                        }
                        IconButton(onClick = onClearFile) {
                            Icon(Icons.Default.Close, contentDescription = "Hapus file", tint = Error)
                        }
                    }
                }
            }
        }

        Button(
            onClick = onSubmit,
            enabled = uiState.importFile != null && !isSubmitting,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Primary)
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
            } else {
                Icon(Icons.Default.FileUpload, contentDescription = null)
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(if (isSubmitting) "Mengimpor..." else "Impor Nota Pembelian")
        }
    }
}

// ─── Helpers ──────────────────────────────────────────────

internal fun formatRupiah(amount: Double): String {
    val isNegative = amount < 0
    val absStr = kotlin.math.abs(amount).toLong().toString()
    val sb = StringBuilder()
    var count = 0
    for (i in absStr.lastIndex downTo 0) {
        if (count > 0 && count % 3 == 0) sb.insert(0, '.')
        sb.insert(0, absStr[i])
        count++
    }
    val prefix = if (isNegative) "-Rp " else "Rp "
    return "$prefix$sb"
}

private fun formatQty(value: Double): String {
    return if (value == value.toLong().toDouble()) {
        value.toLong().toString()
    } else {
        value.toString()
    }
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes >= 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
        bytes >= 1024 -> String.format("%.1f KB", bytes / 1024.0)
        else -> "$bytes B"
    }
}
