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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codasuaka.data.remote.dto.BarangJasaDto
import com.example.codasuaka.data.remote.dto.StokDto
import com.example.codasuaka.ui.components.CodaSuakaDatePickerDialog
import com.example.codasuaka.ui.components.CodaSuakaSnackbarHost
import com.example.codasuaka.ui.theme.*
import com.example.codasuaka.ui.util.formatQty
import com.example.codasuaka.ui.util.formatRupiah
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
        CodaSuakaTheme {
            CodaSuakaDatePickerDialog(
                initialDate = runCatching { LocalDate.parse(uiState.tanggal) }.getOrElse { LocalDate.now() },
                onDateSelected = { date ->
                    viewModel.setTanggal(date.toString())
                    showDatePicker.value = false
                },
                onDismiss = { showDatePicker.value = false }
            )
        }
    }

    // ─── Picker file Excel ───
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { viewModel.onFileSelected(context, it) }
    }

    Scaffold(
        snackbarHost = { CodaSuakaSnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Nota Pembelian", fontWeight = FontWeight.ExtraBold, color = Secondary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali", tint = Secondary)
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
                        onOpenStok = { viewModel.toggleStokKatalog(true) },
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

    // ─── Bottom Sheet: Katalog Barang Produksi (Stok) ───
    if (uiState.isStokKatalogOpen) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.toggleStokKatalog(false) },
            sheetState = sheetState,
            containerColor = Surface
        ) {
            StokSheetContent(
                uiState = uiState,
                onSearch = { viewModel.onSearchQueryChange(it) },
                onSelect = { viewModel.selectStokItem(it) }
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
private fun CompactModeTab(selectedMode: NotaPembelianMode, onModeChange: (NotaPembelianMode) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
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
    onOpenStok: () -> Unit,
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
                Text("Produk Jual", fontSize = 13.sp, fontWeight = FontWeight.Bold)
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

        Spacer(modifier = Modifier.height(10.dp))

        // Barang produksi → diarahkan ke tabel Stok (tidak muncul di produk penjualan).
        OutlinedButton(
            onClick = onOpenStok,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.5.dp, Secondary)
        ) {
            Icon(Icons.Default.Inventory2, null, modifier = Modifier.size(18.dp), tint = Secondary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Barang Produksi (Stok)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Secondary)
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
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(item.namaItem, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Secondary)
                    if (item.isProduksi) {
                        Text(
                            "Produksi",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnPrimary,
                            modifier = Modifier
                                .background(Secondary, RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 1.dp)
                        )
                    }
                }
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
private fun StokSheetContent(uiState: NotaPembelianUiState, onSearch: (String) -> Unit, onSelect: (StokDto) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Pilih Barang Produksi", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Secondary)
        OutlinedTextField(
            value = uiState.searchQuery, onValueChange = onSearch, placeholder = { Text("Cari barang produksi...") },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = Primary) },
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true
        )
        if (uiState.filteredStok.isEmpty()) {
            Text("Belum ada barang produksi. Tambahkan dulu di menu Stok.", fontSize = 12.sp, color = OnSurfaceVariant)
        }
        LazyColumn(modifier = Modifier.heightIn(max = 400.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            itemsIndexed(uiState.filteredStok) { _, s ->
                Card(onClick = { onSelect(s) }, shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = InputBackground)) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(s.nama, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Stok: ${s.stok} ${s.satuan} • Beli: ${formatRupiah(s.hargaBeli ?: 0.0)}", fontSize = 12.sp, color = OnSurfaceVariant)
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

