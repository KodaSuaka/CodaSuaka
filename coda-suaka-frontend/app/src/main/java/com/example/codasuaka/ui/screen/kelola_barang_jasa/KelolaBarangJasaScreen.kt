package com.example.codasuaka.ui.screen.kelola_barang_jasa

import androidx.compose.foundation.background
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
import com.example.codasuaka.data.remote.dto.BarangJasaDto
import com.example.codasuaka.ui.components.NotificationBannerStatic
import com.example.codasuaka.ui.theme.*
import com.example.codasuaka.ui.util.formatRupiah

/**
 * Screen Kelola Barang/Jasa (katalog Kasir).
 *
 * Tampilan utama: daftar barang/jasa + FAB (+) untuk tambah.
 * Tap item -> dialog edit (form yang sama dengan tambah).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KelolaBarangJasaScreen(
    onBack: () -> Unit,
    viewModel: KelolaBarangJasaViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text("Kelola Barang/Jasa", fontWeight = FontWeight.Bold, color = Secondary)
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
                Icon(Icons.Default.Add, "Tambah Barang/Jasa", modifier = Modifier.size(32.dp))
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

            if (uiState.errorMessage != null && uiState.dialogMode is BarangJasaDialogMode.Closed) {
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
                    placeholder = { Text("Cari nama atau kategori...") },
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

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Daftar Barang/Jasa",
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
                                if (uiState.searchQuery.isEmpty()) "Belum ada barang/jasa" else "Pencarian tidak ditemukan",
                                style = MaterialTheme.typography.bodyLarge,
                                color = OnSurfaceVariant
                            )
                            Text(
                                if (uiState.searchQuery.isEmpty()) "Tekan tombol + untuk menambahkan." else "Coba kata kunci lain.",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceVariant
                            )
                        }
                    }
                }
            }

            items(uiState.filteredItems, key = { it.id }) { barangJasa ->
                BarangJasaListItem(
                    item = barangJasa,
                    onClick = { viewModel.openDialogEdit(barangJasa) }
                )
            }

            item { Spacer(modifier = Modifier.height(72.dp)) }
        }
    }

    // ─── Dialog Tambah/Edit ───
    when (val dialog = uiState.dialogMode) {
        is BarangJasaDialogMode.Tambah, is BarangJasaDialogMode.Edit -> {
            DialogFormBarangJasa(
                uiState = uiState,
                onNamaChange = viewModel::onFormNamaChange,
                onJenisChange = viewModel::onFormJenisChange,
                onKategoriChange = viewModel::onFormKategoriChange,
                onSatuanChange = viewModel::onFormSatuanChange,
                onHargaJualChange = viewModel::onFormHargaJualChange,
                onHargaBeliChange = viewModel::onFormHargaBeliChange,
                onStokChange = viewModel::onFormStokChange,
                onKeteranganChange = viewModel::onFormKeteranganChange,
                onSimpan = viewModel::simpan,
                onHapus = { viewModel.requestDelete() },
                onDismiss = viewModel::closeDialog
            )
        }
        BarangJasaDialogMode.Closed -> { /* tidak ada dialog */ }
    }

    // ─── Dialog Konfirmasi Hapus ───
    if (uiState.showDeleteConfirm) {
        val editing = (uiState.dialogMode as? BarangJasaDialogMode.Edit)?.item
        if (editing != null) {
            DeleteBarangJasaDialog(
                nama = editing.nama,
                isDeleting = uiState.isDeleting,
                onDismiss = { viewModel.cancelDelete() },
                onConfirm = { viewModel.confirmDelete() }
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════
// LIST ITEM — Satu barang/jasa di daftar
// ═══════════════════════════════════════════════════════════

@Composable
private fun BarangJasaListItem(
    item: BarangJasaDto,
    onClick: () -> Unit
) {
    val isJasa = item.jenis == "jasa"

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
                    .background(Primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isJasa) Icons.Default.Build else Icons.Default.Inventory2,
                    null,
                    tint = Primary,
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
                        formatRupiah(item.hargaJual),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Secondary 
                    )
                    
                    Text("•", color = Secondary.copy(alpha = 0.3f))
                    
                    Text(
                        item.satuan,
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )

                    if (item.jenis == "barang") {
                        val stok = item.stok ?: 0
                        val stokColor = if (stok <= 5) Error else if (stok <= 20) WarningColor else Success
                        
                        Text("•", color = Secondary.copy(alpha = 0.3f))
                        Text(
                            "Stok: $stok",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = stokColor
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
                            item.kategori ?: (if (isJasa) "Jasa" else "Barang"),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Secondary,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }

                    if (!item.isActive) {
                        Surface(shape = RoundedCornerShape(8.dp), color = Error.copy(alpha = 0.1f)) {
                            Text(
                                "Nonaktif",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Error
                            )
                        }
                    }
                }
            }

            Icon(Icons.Default.ChevronRight, null, tint = Neutral, modifier = Modifier.size(24.dp))
        }
    }
}

// ═══════════════════════════════════════════════════════════
// DIALOG — Tambah/Edit Barang/Jasa
// ═══════════════════════════════════════════════════════════

@Composable
private fun DialogFormBarangJasa(
    uiState: KelolaBarangJasaUiState,
    onNamaChange: (String) -> Unit,
    onJenisChange: (String) -> Unit,
    onKategoriChange: (String) -> Unit,
    onSatuanChange: (String) -> Unit,
    onHargaJualChange: (String) -> Unit,
    onHargaBeliChange: (String) -> Unit,
    onStokChange: (String) -> Unit,
    onKeteranganChange: (String) -> Unit,
    onSimpan: () -> Unit,
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
        focusedTextColor = Secondary,   // Memaksa teks input menjadi Navy
        unfocusedTextColor = Secondary  // Memaksa teks input menjadi Navy
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
                            if (uiState.isEditing) "Edit Barang/Jasa" else "Tambah Barang/Jasa",
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
                        Surface(shape = RoundedCornerShape(8.dp), color = Error.copy(alpha = 0.1f)) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Error, null, tint = Error, modifier = Modifier.size(18.dp))
                                Text(uiState.errorMessage, color = Error, style = MaterialTheme.typography.bodySmall)
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

                    // Jenis: barang / jasa
                    Column {
                        Text("Jenis", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Secondary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("barang" to "Barang", "jasa" to "Jasa").forEach { (value, label) ->
                                val selected = uiState.formJenis == value
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
                                        selectedContainerColor = Primary,
                                        containerColor = Neutral.copy(alpha = 0.7f)
                                    ),
                                    border = null,
                                    shape = RoundedCornerShape(12.dp)
                                )
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
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = fieldColors,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = OnSurface)
                    )

                    OutlinedTextField(
                        value = uiState.formSatuan,
                        onValueChange = onSatuanChange,
                        label = { Text("Satuan *") },
                        placeholder = { Text("pcs, kg, jam, ...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = fieldColors,
                        isError = uiState.satuanError != null,
                        supportingText = uiState.satuanError?.let { { Text(it) } }
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = uiState.formHargaJual,
                            onValueChange = onHargaJualChange,
                            label = { Text("Harga Jual *") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = fieldColors,
                            isError = uiState.hargaJualError != null,
                            supportingText = uiState.hargaJualError?.let { { Text(it) } }
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

                    if (uiState.formJenis != "jasa") {
                        OutlinedTextField(
                            value = uiState.formStok,
                            onValueChange = onStokChange,
                            label = { Text("Stok (opsional)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = fieldColors,
                            isError = uiState.stokError != null,
                            supportingText = uiState.stokError?.let { { Text(it) } }
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
                            if (uiState.isEditing) "Simpan Perubahan" else "Tambah Barang/Jasa",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }

                    if (uiState.isEditing) {
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
                                tint = Error // Paksa ikon jadi merah
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Hapus Produk", 
                                fontWeight = FontWeight.ExtraBold, 
                                fontSize = 14.sp,
                                color = Error // Paksa teks jadi merah tegas
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp)) // Tambahan padding bawah agar tidak mepet
                }
            }
    }
}

// ═══════════════════════════════════════════════════════════
// DIALOG — Konfirmasi Hapus
// ═══════════════════════════════════════════════════════════

@Composable
private fun DeleteBarangJasaDialog(
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
                    Text("Hapus Produk", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = Secondary)
                    Text("Yakin ingin menghapus \"$nama\"? Tindakan ini permanen.", textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium, color = Secondary.copy(alpha = 0.7f))
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

