package com.example.codasuaka.ui.screen.stok

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.codasuaka.data.remote.dto.StokDto
import com.example.codasuaka.data.remote.dto.StokMutationDto
import com.example.codasuaka.ui.components.NotificationBannerStatic
import com.example.codasuaka.ui.theme.*
import com.example.codasuaka.ui.util.formatRupiah
import com.example.codasuaka.util.ErrorMessageMapper

/**
 * Screen Stok (bahan baku / barang produksi).
 *
 * Tampilan utama: daftar stok + FAB (+) untuk tambah.
 * Tap item -> dialog edit. Dari dialog edit bisa buka Mutasi Stok,
 * Riwayat Mutasi, atau Hapus.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StokScreen(
    onBack: () -> Unit,
    viewModel: StokViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text("Stok", fontWeight = FontWeight.Bold, color = Secondary)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Kembali", tint = Secondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openDialogTambah() },
                containerColor = Primary,
                contentColor = OnPrimary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, "Tambah Stok", modifier = Modifier.size(32.dp))
            }
        }
    ) { innerPadding ->
        if (uiState.isLoading && uiState.items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Tertiary),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Tertiary)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(16.dp)) }

            if (uiState.successMessage != null) {
                item {
                    NotificationBannerStatic(
                        message = uiState.successMessage ?: "",
                        type = com.example.codasuaka.util.ErrorMessageMapper.NotificationType.SUCCESS,
                        onDismiss = { viewModel.clearMessages() }
                    )
                }
            }

            if (uiState.errorMessage != null && uiState.dialogMode is StokDialogMode.Closed) {
                item {
                    NotificationBannerStatic(
                        message = uiState.errorMessage ?: "",
                        mapFromServer = true,
                        onDismiss = { viewModel.clearMessages() }
                    )
                }
            }

            // ─── Search Bar ───
            item {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = viewModel::onSearchQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Cari nama stok...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Secondary) },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = OnSurfaceVariant)
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = NeutralBorder,
                        focusedContainerColor = Surface,
                        unfocusedContainerColor = Surface
                    )
                )
            }

            // ─── Filter Kategori ───
            item {
                val options = uiState.kategoriOptions
                if (options.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                    ) {
                        item {
                            val selected = uiState.kategoriFilter == null
                            FilterChip(
                                selected = selected,
                                onClick = { viewModel.onKategoriFilterChange(null) },
                                label = {
                                    Text(
                                        "Semua",
                                        fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Bold,
                                        color = if (selected) OnPrimary else Secondary
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Primary,
                                    containerColor = Surface
                                ),
                                border = null,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                        items(options.size) { index ->
                            val kategori = options[index]
                            val selected = uiState.kategoriFilter == kategori
                            FilterChip(
                                selected = selected,
                                onClick = { viewModel.onKategoriFilterChange(kategori) },
                                label = {
                                    Text(
                                        kategori,
                                        fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Bold,
                                        color = if (selected) OnPrimary else Secondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Primary,
                                    containerColor = Surface
                                ),
                                border = null,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Daftar Stok",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Secondary
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Primary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            "${uiState.filteredItems.size} item",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                    }
                }
            }

            if (uiState.filteredItems.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Inventory2,
                                null,
                                tint = Neutral,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                if (uiState.searchQuery.isEmpty() && uiState.kategoriFilter == null) {
                                    "Belum ada stok"
                                } else {
                                    "Pencarian tidak ditemukan"
                                },
                                style = MaterialTheme.typography.bodyLarge,
                                color = OnSurfaceVariant
                            )
                            Text(
                                if (uiState.searchQuery.isEmpty() && uiState.kategoriFilter == null) {
                                    "Tekan tombol + untuk menambahkan."
                                } else {
                                    "Coba kata kunci lain."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceVariant
                            )
                        }
                    }
                }
            }

            items(uiState.filteredItems, key = { it.id }) { stok ->
                StokListItem(
                    item = stok,
                    onClick = { viewModel.openDialogEdit(stok) }
                )
            }

            item { Spacer(modifier = Modifier.height(72.dp)) }
        }
    }

    // ─── Dialog Tambah/Edit ───
    when (val dialog = uiState.dialogMode) {
        is StokDialogMode.Tambah, is StokDialogMode.Edit -> {
            DialogFormStok(
                uiState = uiState,
                onNamaChange = viewModel::onFormNamaChange,
                onKategoriChange = viewModel::onFormKategoriChange,
                onSatuanChange = viewModel::onFormSatuanChange,
                onStokMinimumChange = viewModel::onFormStokMinimumChange,
                onHargaBeliChange = viewModel::onFormHargaBeliChange,
                onKeteranganChange = viewModel::onFormKeteranganChange,
                onSimpan = viewModel::simpan,
                onMutasi = {
                    val item = (uiState.dialogMode as? StokDialogMode.Edit)?.item
                    if (item != null) viewModel.openDialogMutasi(item)
                },
                onRiwayat = {
                    val item = (uiState.dialogMode as? StokDialogMode.Edit)?.item
                    if (item != null) viewModel.openDialogRiwayat(item)
                },
                onHapus = { viewModel.requestDelete() },
                onDismiss = viewModel::closeDialog
            )
        }
        is StokDialogMode.Mutasi -> {
            DialogMutasiStok(
                uiState = uiState,
                onJenisChange = viewModel::onMutasiJenisChange,
                onJumlahChange = viewModel::onMutasiJumlahChange,
                onKeteranganChange = viewModel::onMutasiKeteranganChange,
                onSubmit = viewModel::submitMutasi,
                onDismiss = viewModel::closeDialog
            )
        }
        is StokDialogMode.Riwayat -> {
            DialogRiwayatStok(
                uiState = uiState,
                onDismiss = viewModel::closeDialog
            )
        }
        StokDialogMode.Closed -> { /* tidak ada dialog */ }
    }

    // ─── Dialog Konfirmasi Hapus ───
    if (uiState.showDeleteConfirm) {
        val editing = (uiState.dialogMode as? StokDialogMode.Edit)?.item
        if (editing != null) {
            DeleteStokDialog(
                nama = editing.nama,
                isDeleting = uiState.isDeleting,
                onDismiss = { viewModel.cancelDelete() },
                onConfirm = { viewModel.confirmDelete() }
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════
// LIST ITEM — Satu stok di daftar
// ═══════════════════════════════════════════════════════════

@Composable
private fun StokListItem(
    item: StokDto,
    onClick: () -> Unit
) {
    val isMenipis = item.stokMinimum != null && item.stok <= item.stokMinimum!!
    val stokColor = when {
        isMenipis -> Error
        item.stok <= 0 -> WarningColor
        else -> Success
    }

    Card(
        onClick = onClick,
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
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (isMenipis) Error.copy(alpha = 0.1f)
                        else if (item.stok <= 0) WarningColor.copy(alpha = 0.15f)
                        else Primary.copy(alpha = 0.1f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Warehouse,
                    null,
                    tint = if (isMenipis) Error else if (item.stok <= 0) WarningColor else Primary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.nama,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "${formatStok(item.stok)} ${item.satuan}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = stokColor
                    )

                    if (item.stokMinimum != null) {
                        Text("•", color = Secondary.copy(alpha = 0.3f))
                        Text(
                            "Min ${formatStok(item.stokMinimum!!)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Secondary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            item.kategori ?: "Umum",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Secondary,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }

                    if (item.hargaBeli != null) {
                        Text(
                            formatRupiah(item.hargaBeli!!),
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceVariant
                        )
                    }
                }
            }

            Icon(Icons.Default.ChevronRight, null, tint = Neutral, modifier = Modifier.size(24.dp))
        }
    }
}

// ═══════════════════════════════════════════════════════════
// DIALOG — Tambah/Edit Stok
// ═══════════════════════════════════════════════════════════

@Composable
private fun DialogFormStok(
    uiState: StokUiState,
    onNamaChange: (String) -> Unit,
    onKategoriChange: (String) -> Unit,
    onSatuanChange: (String) -> Unit,
    onStokMinimumChange: (String) -> Unit,
    onHargaBeliChange: (String) -> Unit,
    onKeteranganChange: (String) -> Unit,
    onSimpan: () -> Unit,
    onMutasi: () -> Unit,
    onRiwayat: () -> Unit,
    onHapus: () -> Unit,
    onDismiss: () -> Unit
) {
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Primary,
        unfocusedBorderColor = NeutralBorder,
        focusedContainerColor = InputBackground,
        unfocusedContainerColor = InputBackground,
        cursorColor = Primary,
        focusedLabelColor = Primary,
        unfocusedLabelColor = Secondary.copy(alpha = 0.6f),
        focusedTextColor = Secondary,
        unfocusedTextColor = Secondary
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .heightIn(max = 640.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (uiState.isEditing) "Edit Stok" else "Tambah Stok",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Secondary
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.background(Neutral.copy(alpha = 0.5f), CircleShape).size(32.dp)) {
                        Icon(Icons.Default.Close, "Tutup", tint = Secondary, modifier = Modifier.size(18.dp))
                    }
                }

                HorizontalDivider(color = Neutral, thickness = 1.dp)

                if (uiState.errorMessage != null) {
                    val mapped = ErrorMessageMapper.map(uiState.errorMessage)
                    Surface(shape = RoundedCornerShape(8.dp), color = Error.copy(alpha = 0.1f)) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Error, null, tint = Error, modifier = Modifier.size(18.dp))
                            Text(mapped.message, color = Error, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                if (uiState.deleteError != null) {
                    NotificationBannerStatic(
                        message = uiState.deleteError,
                        mapFromServer = false,
                        onDismiss = null
                    )
                }

                // Info stok saat ini (hanya di mode edit)
                if (uiState.isEditing) {
                    val editing = (uiState.dialogMode as? StokDialogMode.Edit)?.item
                    if (editing != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Primary.copy(alpha = 0.08f)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Stok saat ini",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Secondary.copy(alpha = 0.7f)
                                )
                                Text(
                                    "${formatStok(editing.stok)} ${editing.satuan}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Primary
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = uiState.formNama,
                    onValueChange = onNamaChange,
                    label = { Text("Nama *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = fieldColors,
                    isError = uiState.namaError != null,
                    supportingText = uiState.namaError?.let { { Text(it) } }
                )

                OutlinedTextField(
                    value = uiState.formKategori,
                    onValueChange = onKategoriChange,
                    label = { Text("Kategori (opsional)") },
                    placeholder = { Text("Bahan Baku, Barang Produksi, ...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = fieldColors
                )

                OutlinedTextField(
                    value = uiState.formSatuan,
                    onValueChange = onSatuanChange,
                    label = { Text("Satuan *") },
                    placeholder = { Text("pcs, kg, liter, botol, ...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = fieldColors,
                    isError = uiState.satuanError != null,
                    supportingText = uiState.satuanError?.let { { Text(it) } }
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = uiState.formStokMinimum,
                        onValueChange = onStokMinimumChange,
                        label = { Text("Stok Minimum") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = fieldColors,
                        isError = uiState.stokMinimumError != null,
                        supportingText = uiState.stokMinimumError?.let { { Text(it) } }
                    )
                    OutlinedTextField(
                        value = uiState.formHargaBeli,
                        onValueChange = onHargaBeliChange,
                        label = { Text("Harga Beli") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = fieldColors,
                        isError = uiState.hargaBeliError != null,
                        supportingText = uiState.hargaBeliError?.let { { Text(it) } }
                    )
                }

                OutlinedTextField(
                    value = uiState.formKeterangan,
                    onValueChange = onKeteranganChange,
                    label = { Text("Keterangan (opsional)") },
                    minLines = 2,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = fieldColors
                )

                Button(
                    onClick = onSimpan,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = !uiState.isSaving,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        contentColor = OnPrimary,
                        disabledContainerColor = Primary.copy(alpha = 0.5f)
                    )
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = OnPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (uiState.isEditing) "Simpan Perubahan" else "Tambah Stok",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }

                if (uiState.isEditing) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = onMutasi,
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Primary)
                        ) {
                            Icon(Icons.Default.SwapVert, null, modifier = Modifier.size(18.dp), tint = Primary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Mutasi Stok", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Primary)
                        }
                        OutlinedButton(
                            onClick = onRiwayat,
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Primary)
                        ) {
                            Icon(Icons.Default.History, null, modifier = Modifier.size(18.dp), tint = Primary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Riwayat", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Primary)
                        }
                    }

                    TextButton(
                        onClick = onHapus,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        enabled = !uiState.isDeleting,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Error,
                            containerColor = Error.copy(alpha = 0.08f)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = Error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Hapus Stok",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = Error
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// DIALOG — Mutasi Stok (masuk / keluar / penyesuaian)
// ═══════════════════════════════════════════════════════════

@Composable
private fun DialogMutasiStok(
    uiState: StokUiState,
    onJenisChange: (String) -> Unit,
    onJumlahChange: (String) -> Unit,
    onKeteranganChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit
) {
    val item = (uiState.dialogMode as? StokDialogMode.Mutasi)?.item

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .heightIn(max = 600.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Mutasi Stok",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Secondary
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.background(Neutral.copy(alpha = 0.5f), CircleShape).size(32.dp)) {
                        Icon(Icons.Default.Close, "Tutup", tint = Secondary, modifier = Modifier.size(18.dp))
                    }
                }

                HorizontalDivider(color = Neutral, thickness = 1.dp)

                if (item != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Primary.copy(alpha = 0.08f)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    item.nama,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Secondary
                                )
                                Text(
                                    "Stok sekarang: ${formatStok(item.stok)} ${item.satuan}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OnSurfaceVariant
                                )
                            }
                        }
                    }
                }

                if (uiState.mutasiError != null) {
                    val mapped = ErrorMessageMapper.map(uiState.mutasiError)
                    Surface(shape = RoundedCornerShape(8.dp), color = Error.copy(alpha = 0.1f)) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Error, null, tint = Error, modifier = Modifier.size(18.dp))
                            Text(mapped.message, color = Error, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                Column {
                    Text("Jenis Mutasi", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Secondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            "masuk" to "Masuk",
                            "keluar" to "Keluar",
                            "penyesuaian" to "Penyesuaian"
                        ).forEach { (value, label) ->
                            val selected = uiState.mutasiJenis == value
                            FilterChip(
                                selected = selected,
                                onClick = { onJenisChange(value) },
                                label = {
                                    Text(
                                        label,
                                        fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Bold,
                                        color = if (selected) OnPrimary else Secondary
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = if (value == "keluar") Error else Primary,
                                    containerColor = Neutral.copy(alpha = 0.7f)
                                ),
                                border = null,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        when (uiState.mutasiJenis) {
                            "masuk" -> "Menambah stok (pembelian bahan, penerimaan barang)."
                            "keluar" -> "Mengurangi stok (pemakaian produksi, penjualan bahan)."
                            else -> "Menetapkan jumlah stok secara langsung (stock opname)."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                }

                val fieldColors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = NeutralBorder,
                    focusedContainerColor = InputBackground,
                    unfocusedContainerColor = InputBackground,
                    cursorColor = Primary,
                    focusedLabelColor = Primary,
                    unfocusedLabelColor = Secondary.copy(alpha = 0.6f),
                    focusedTextColor = Secondary,
                    unfocusedTextColor = Secondary
                )

                OutlinedTextField(
                    value = uiState.mutasiJumlah,
                    onValueChange = onJumlahChange,
                    label = { Text("Jumlah *") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = fieldColors,
                    isError = uiState.mutasiJumlahError != null,
                    supportingText = uiState.mutasiJumlahError?.let { { Text(it) } }
                )

                OutlinedTextField(
                    value = uiState.mutasiKeterangan,
                    onValueChange = onKeteranganChange,
                    label = { Text("Keterangan (opsional)") },
                    placeholder = { Text("mis. pembelian bulanan, rusak, ...") },
                    minLines = 2,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = fieldColors
                )

                Button(
                    onClick = onSubmit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = !uiState.isMutating,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        contentColor = OnPrimary,
                        disabledContainerColor = Primary.copy(alpha = 0.5f)
                    )
                ) {
                    if (uiState.isMutating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = OnPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Icon(Icons.Default.SwapVert, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Simpan Mutasi", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// DIALOG — Riwayat Mutasi
// ═══════════════════════════════════════════════════════════

@Composable
private fun DialogRiwayatStok(
    uiState: StokUiState,
    onDismiss: () -> Unit
) {
    val item = (uiState.dialogMode as? StokDialogMode.Riwayat)?.item

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .heightIn(max = 600.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Riwayat Mutasi",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Secondary
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.background(Neutral.copy(alpha = 0.5f), CircleShape).size(32.dp)) {
                        Icon(Icons.Default.Close, "Tutup", tint = Secondary, modifier = Modifier.size(18.dp))
                    }
                }

                HorizontalDivider(color = Neutral, thickness = 1.dp)

                if (item != null) {
                    Text(
                        item.nama,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Secondary
                    )
                }

                when {
                    uiState.riwayatLoading -> {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Primary)
                        }
                    }

                    uiState.riwayatError != null -> {
                        val mapped = ErrorMessageMapper.map(uiState.riwayatError)
                        Surface(shape = RoundedCornerShape(8.dp), color = Error.copy(alpha = 0.1f)) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Error, null, tint = Error, modifier = Modifier.size(18.dp))
                                Text(mapped.message, color = Error, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    uiState.riwayatList.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Belum ada riwayat mutasi.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = OnSurfaceVariant
                            )
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth().weight(1f, fill = false),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(uiState.riwayatList, key = { it.id }) { mutasi ->
                                RiwayatItem(mutasi = mutasi)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun RiwayatItem(mutasi: StokMutationDto) {
    val isMasuk = mutasi.jenis == "masuk"
    val isKeluar = mutasi.jenis == "keluar"
    val signColor = if (isMasuk) Success else if (isKeluar) Error else Primary
    val signPrefix = if (isMasuk) "+" else if (isKeluar) "-" else ""

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Tertiary),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Neutral)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(signColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    when (mutasi.jenis) {
                        "masuk" -> Icons.Default.ArrowDownward
                        "keluar" -> Icons.Default.ArrowUpward
                        else -> Icons.Default.Tune
                    },
                    null,
                    tint = signColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        when (mutasi.jenis) {
                            "masuk" -> "Stok Masuk"
                            "keluar" -> "Stok Keluar"
                            else -> "Penyesuaian"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Secondary
                    )
                    Text(
                        formatTanggal(mutasi.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceVariant
                    )
                }

                if (!mutasi.keterangan.isNullOrBlank()) {
                    Text(
                        mutasi.keterangan,
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "$signPrefix${formatStok(mutasi.jumlah)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = signColor
                )
                Text(
                    "→ ${formatStok(mutasi.stokSesudah)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceVariant
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// DIALOG — Konfirmasi Hapus
// ═══════════════════════════════════════════════════════════

@Composable
private fun DeleteStokDialog(
    nama: String,
    isDeleting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
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
                Box(modifier = Modifier.size(64.dp).clip(CircleShape).background(Error.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Delete, null, tint = Error, modifier = Modifier.size(32.dp))
                }
                Text("Hapus Stok", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = Secondary)
                Text(
                    "Yakin ingin menghapus \"$nama\"? Tindakan ini permanen. Stok dengan riwayat mutasi tidak bisa dihapus.",
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
                        colors = ButtonDefaults.buttonColors(containerColor = Error),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isDeleting) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = OnPrimary, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text("Hapus", color = OnPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ─── Helpers ────────────────────────────────────────────────

/** Format angka stok: 15000.0 -> "15000", 15000.5 -> "15000.5" */
private fun formatStok(value: Double): String =
    if (value == value.toLong().toDouble()) value.toLong().toString() else value.toString()

/** Format tanggal API "2026-08-06T14:30:00.000000Z" -> "06 Aug 14:30" (fallback raw) */
private fun formatTanggal(iso: String?): String {
    if (iso.isNullOrBlank()) return "-"
    return runCatching {
        val cleaned = iso.replace("Z", "")
        val dateTime = java.time.LocalDateTime.parse(
            if (cleaned.contains(".")) cleaned.substringBefore(".") else cleaned
        )
        val formatter = java.time.format.DateTimeFormatter.ofPattern("dd MMM HH:mm")
        dateTime.format(formatter)
    }.getOrDefault(iso)
}
