package com.example.codasuaka.ui.screen.nota_pembelian

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codasuaka.data.remote.dto.BarangJasaDto
import com.example.codasuaka.ui.components.CodaSuakaDatePickerDialog
import com.example.codasuaka.ui.components.CodaSuakaSnackbarHost
import com.example.codasuaka.ui.theme.*
import com.example.codasuaka.util.ErrorMessageMapper
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotaPembelianScreen(
    onBack: () -> Unit,
    viewModel: NotaPembelianViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
    val showDatePicker = remember { mutableStateOf(false) }
    if (showDatePicker.value) {
<<<<<<< HEAD
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
=======
        CodaSuakaDatePickerDialog(
            initialDate = runCatching { LocalDate.parse(uiState.tanggal) }.getOrElse { LocalDate.now() },
            onDateSelected = { date ->
                viewModel.setTanggal(date.toString())
                showDatePicker.value = false
            },
            onDismiss = { showDatePicker.value = false }
        )
>>>>>>> 69e8a7268ce4d561ca3a4d56f8711b36abf79404
    }

    // ─── Picker file Excel ───
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { viewModel.onFileSelected(context, it) }
    }

<<<<<<< HEAD
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
=======
    Scaffold(
        snackbarHost = { CodaSuakaSnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Nota Pembelian", fontWeight = FontWeight.ExtraBold, color = Secondary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali", tint = Secondary)
>>>>>>> 69e8a7268ce4d561ca3a4d56f8711b36abf79404
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface)
            )
        },
        bottomBar = {
            if (uiState.mode == NotaPembelianMode.MANUAL) {
                StickyBottomAction(
                    total = uiState.cartTotal,
                    itemCount = uiState.cartItems.size,
                    isSubmitting = uiState.isSubmitting,
                    onSave = { viewModel.submitManual() }
                )
            }
        },
        containerColor = Tertiary
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // ── Tab: Manual / Impor Excel ──
            CompactModeTab(
                selectedMode = uiState.mode,
                onModeChange = { viewModel.setMode(it) }
            )

            // ── Form Umum (Compact/Expandable) ──
            ExpandableFormUmum(
                uiState = uiState,
                onToggle = { viewModel.toggleHeader(!uiState.isHeaderExpanded) },
                onPickDate = { showDatePicker.value = true },
                onPihakTerkaitChange = { viewModel.setPihakTerkait(it) },
                onCatatanChange = { viewModel.setCatatan(it) }
            )

            when (uiState.mode) {
                NotaPembelianMode.MANUAL -> {
                    MainManualContent(
                        uiState = uiState,
                        onOpenKatalog = { viewModel.toggleKatalog(true) },
                        onOpenManual = { viewModel.toggleManualInput(true) },
                        onRemoveItem = { viewModel.removeCartItem(it) },
                        onClearCart = { viewModel.clearCart() }
                    )
                }

                NotaPembelianMode.IMPORT -> {
                    ImportSection(
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

    // ─── Bottom Sheet: Katalog ───
    if (uiState.isKatalogOpen) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.toggleKatalog(false) },
            sheetState = sheetState,
            containerColor = Surface
        ) {
            KatalogSheetContent(
                uiState = uiState,
                onSearch = { viewModel.onSearchQueryChange(it) },
                onSelect = { viewModel.selectKatalogItem(it) }
            )
        }
    }

    // ─── Bottom Sheet: Input Manual ───
    if (uiState.isManualInputOpen) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.toggleManualInput(false) },
            sheetState = sheetState,
            containerColor = Surface
        ) {
            ManualInputSheetContent(
                uiState = uiState,
                onNamaChange = { viewModel.updateItemNama(it) },
                onQtyChange = { viewModel.updateItemKuantitas(it) },
                onSatuanChange = { viewModel.updateItemSatuan(it) },
                onHargaChange = { viewModel.updateItemHarga(it) },
                onAdd = { viewModel.addItemToCart() }
            )
        }
    }

    // ─── Bottom Sheet: Prompt Qty (After Katalog Selection) ───
    if (uiState.isQtyPromptOpen) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.closeQtyPrompt() },
            sheetState = sheetState,
            containerColor = Surface
        ) {
            QtyPromptSheetContent(
                uiState = uiState,
                onQtyChange = { viewModel.updateItemKuantitas(it) },
                onHargaChange = { viewModel.updateItemHarga(it) },
                onConfirm = { viewModel.addItemToCart() }
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════
// SUB-COMPONENTS
// ═══════════════════════════════════════════════════════════

@Composable
<<<<<<< HEAD
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
=======
private fun CompactModeTab(selectedMode: NotaPembelianMode, onModeChange: (NotaPembelianMode) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
>>>>>>> 69e8a7268ce4d561ca3a4d56f8711b36abf79404
    ) {
        listOf(NotaPembelianMode.MANUAL to "Input Manual", NotaPembelianMode.IMPORT to "Impor Excel").forEach { (mode, label) ->
            val isSelected = selectedMode == mode
            Surface(
                modifier = Modifier.weight(1f).height(44.dp),
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) Primary else InputBackground,
                onClick = { onModeChange(mode) }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(label, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (isSelected) OnPrimary else OnSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun ExpandableFormUmum(
    uiState: NotaPembelianUiState,
    onToggle: () -> Unit,
    onPickDate: () -> Unit,
    onPihakTerkaitChange: (String) -> Unit,
    onCatatanChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        border = BorderStroke(1.dp, Neutral)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { onToggle() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Info, null, tint = Primary, modifier = Modifier.size(20.dp))
                    Text(
                        text = if (uiState.pihakTerkait.isEmpty()) "Informasi Nota" else "Supplier: ${uiState.pihakTerkait}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Secondary
                    )
                }
                Icon(
                    if (uiState.isHeaderExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    null,
                    tint = Secondary
                )
            }

            AnimatedVisibility(visible = uiState.isHeaderExpanded) {
                Column(modifier = Modifier.padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = uiState.tanggal, onValueChange = {}, readOnly = true, label = { Text("Tanggal") },
                        trailingIcon = { IconButton(onClick = onPickDate) { Icon(Icons.Default.DateRange, null, tint = Primary) } },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)
                    )
                    
                    // Outlet & Pihak Terkait (Simplikasi: skip dropdown details here for brevity, assume similar logic)
                    OutlinedTextField(
                        value = uiState.pihakTerkait, onValueChange = onPihakTerkaitChange,
                        label = { Text("Pihak Terkait / Supplier") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)
                    )
                    
                    OutlinedTextField(
                        value = uiState.catatan, onValueChange = onCatatanChange,
                        label = { Text("Catatan") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MainManualContent(
    uiState: NotaPembelianUiState,
    onOpenKatalog: () -> Unit,
    onOpenManual: () -> Unit,
    onRemoveItem: (Int) -> Unit,
    onClearCart: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = onOpenKatalog,
                modifier = Modifier.weight(1.2f).height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Secondary)
            ) {
                Icon(Icons.AutoMirrored.Filled.List, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cari di Katalog", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            OutlinedButton(
                onClick = onOpenManual,
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.5.dp, Primary)
            ) {
                Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp), tint = Primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Isi Manual", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Primary)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Daftar Belanja", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Secondary)
            if (uiState.cartItems.isNotEmpty()) {
                TextButton(onClick = onClearCart) { Text("Kosongkan", color = Error, fontSize = 12.sp) }
            }
        }

        if (uiState.cartItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(bottom = 60.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.ShoppingCart, null, tint = Neutral, modifier = Modifier.size(64.dp))
                    Text("Belum ada item", fontWeight = FontWeight.Bold, color = OnSurfaceVariant)
                    Text("Pilih barang dari katalog di atas", fontSize = 12.sp, color = OnSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(uiState.cartItems) { index, item ->
                    CartItemRow(item = item, onRemove = { onRemoveItem(index) })
                }
            }
        }
    }
}

@Composable
private fun CartItemRow(item: PembelianCartItem, onRemove: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        border = BorderStroke(1.dp, Neutral)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.namaItem, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Secondary)
                Text(
                    text = "${formatQty(item.kuantitas)} ${item.satuan} × ${formatRupiah(item.hargaSatuan)}", 
                    fontSize = 12.sp, 
                    color = Secondary.copy(alpha = 0.6f), // Navy transparan agar jelas
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                text = formatRupiah(item.subtotal), 
                fontWeight = FontWeight.ExtraBold, 
                fontSize = 14.sp, 
                color = Secondary // Nominal Navy
            )
            IconButton(onClick = onRemove) { Icon(Icons.Default.DeleteOutline, null, tint = Error, modifier = Modifier.size(20.dp)) }
        }
    }
}

@Composable
private fun StickyBottomAction(total: Double, itemCount: Int, isSubmitting: Boolean, onSave: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp).navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Total ($itemCount item)", fontSize = 12.sp, color = Secondary.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
                Text(
                    text = formatRupiah(total), 
                    fontSize = 20.sp, 
                    fontWeight = FontWeight.ExtraBold, 
                    color = Secondary // Total Utama Navy
                )
            }
            Button(
                onClick = onSave,
                enabled = itemCount > 0 && !isSubmitting,
                modifier = Modifier.height(48.dp).weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary, 
                    contentColor = OnPrimary
                )
            ) {
                if (isSubmitting) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = OnPrimary, strokeWidth = 2.dp)
                else Text("Simpan Nota", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun KatalogSheetContent(uiState: NotaPembelianUiState, onSearch: (String) -> Unit, onSelect: (BarangJasaDto) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Pilih Produk", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Secondary)
        OutlinedTextField(
            value = uiState.searchQuery, onValueChange = onSearch, placeholder = { Text("Cari produk...") },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = Primary) },
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true
        )
        LazyColumn(modifier = Modifier.heightIn(max = 400.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            itemsIndexed(uiState.filteredKatalog) { _, p ->
                Card(onClick = { onSelect(p) }, shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = InputBackground)) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(p.nama, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("${p.satuan} • Harga: ${formatRupiah(p.hargaBeli ?: 0.0)}", fontSize = 12.sp, color = OnSurfaceVariant)
                        }
                        Icon(Icons.Default.AddCircle, null, tint = Primary)
                    }
                }
            }
        }
    }
}

@Composable
private fun ManualInputSheetContent(uiState: NotaPembelianUiState, onNamaChange: (String) -> Unit, onQtyChange: (String) -> Unit, onSatuanChange: (String) -> Unit, onHargaChange: (String) -> Unit, onAdd: () -> Unit) {
    CodaSuakaTheme { // Memaksa tema Navy & Putih
        Column(modifier = Modifier.fillMaxWidth().padding(24.dp).padding(bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Input Manual", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = Secondary)
            OutlinedTextField(
                value = uiState.itemNama, 
                onValueChange = onNamaChange, 
                label = { Text("Nama Barang") }, 
                modifier = Modifier.fillMaxWidth(), 
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = NeutralBorder,
                    focusedTextColor = Secondary,
                    unfocusedTextColor = Secondary
                )
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = uiState.itemKuantitas, 
                    onValueChange = onQtyChange, 
                    label = { Text("Qty") }, 
                    modifier = Modifier.weight(1f), 
                    shape = RoundedCornerShape(12.dp), 
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Secondary, unfocusedTextColor = Secondary)
                )
                OutlinedTextField(
                    value = uiState.itemSatuan, 
                    onValueChange = onSatuanChange, 
                    label = { Text("Satuan") }, 
                    modifier = Modifier.weight(1f), 
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Secondary, unfocusedTextColor = Secondary)
                )
            }
            OutlinedTextField(
                value = uiState.itemHarga, 
                onValueChange = onHargaChange, 
                label = { Text("Harga Satuan") }, 
                modifier = Modifier.fillMaxWidth(), 
                shape = RoundedCornerShape(12.dp), 
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Secondary, unfocusedTextColor = Secondary)
            )
            Button(onClick = onAdd, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = Primary)) { 
                Text("Tambah ke Daftar", fontWeight = FontWeight.Bold) 
            }
        }
    }
}

@Composable
private fun QtyPromptSheetContent(uiState: NotaPembelianUiState, onQtyChange: (String) -> Unit, onHargaChange: (String) -> Unit, onConfirm: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(24.dp).padding(bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(uiState.itemNama, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Secondary)
        Text("Masukkan jumlah yang dibeli:", fontSize = 14.sp, color = OnSurfaceVariant)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(value = uiState.itemKuantitas, onValueChange = onQtyChange, label = { Text("Jumlah") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            Text(uiState.itemSatuan, fontWeight = FontWeight.Bold, color = Secondary)
        }
        OutlinedTextField(value = uiState.itemHarga, onValueChange = onHargaChange, label = { Text("Harga Satuan") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
        Button(onClick = onConfirm, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(12.dp)) { Text("Tambahkan") }
    }
}

@Composable
private fun ImportSection(uiState: NotaPembelianUiState, onPickFile: () -> Unit, onClearFile: () -> Unit, onSubmit: () -> Unit, isSubmitting: Boolean) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Surface), border = BorderStroke(1.dp, Neutral)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Impor via Excel", fontWeight = FontWeight.Bold, color = Secondary)
                Text("Format: nama_item | jenis | kuantitas | satuan | harga_satuan", fontSize = 12.sp, color = OnSurfaceVariant)
                if (uiState.importFile == null) {
                    Button(onClick = onPickFile, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) { Icon(Icons.Default.UploadFile, null); Spacer(modifier = Modifier.width(8.dp)); Text("Pilih File Excel") }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Description, null, tint = Primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(uiState.importFile.fileName, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        IconButton(onClick = onClearFile) { Icon(Icons.Default.Close, null, tint = Error) }
                    }
                }
            }
        }
        Button(onClick = onSubmit, enabled = uiState.importFile != null && !isSubmitting, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(12.dp)) {
            if (isSubmitting) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = OnPrimary)
            else Text("Proses Impor")
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
    return if (value == value.toLong().toDouble()) value.toLong().toString() else value.toString()
}
