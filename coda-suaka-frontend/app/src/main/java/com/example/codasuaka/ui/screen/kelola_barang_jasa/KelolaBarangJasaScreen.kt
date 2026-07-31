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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.codasuaka.data.remote.dto.BarangJasaDto
import com.example.codasuaka.ui.components.NotificationBannerStatic
import com.example.codasuaka.ui.theme.*

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
                    contentColor = Color.White,
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
                        .background(Color.White),
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
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
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

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 8.dp),
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
                                "${uiState.items.size} item",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Primary
                            )
                        }
                    }
                }

                if (uiState.items.isEmpty()) {
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
                                    "Belum ada barang/jasa",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = OnSurfaceVariant
                                )
                                Text(
                                    "Tekan tombol + untuk menambahkan.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OnSurfaceVariant
                                )
                            }
                        }
                    }
                }

                items(uiState.items, key = { it.id }) { barangJasa ->
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
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        item.nama,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = Secondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (!item.isActive) {
                        Surface(shape = RoundedCornerShape(8.dp), color = Error.copy(alpha = 0.1f)) {
                            Text(
                                "Nonaktif",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Error
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${formatRupiahBarangJasa(item.hargaJual)} / ${item.satuan}",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Secondary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            item.kategori ?: (if (isJasa) "Jasa" else "Barang"),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Secondary,
                            fontWeight = FontWeight.Bold
                        )
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
        unfocusedBorderColor = Neutral,
        focusedContainerColor = Surface,
        unfocusedContainerColor = Surface,
        cursorColor = Primary,
        focusedLabelColor = Primary,
        unfocusedLabelColor = OnSurfaceVariant
    )

    // ─── Force Light Theme (dialog render di window terpisah) ───
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
            error = Error,
            outline = NeutralBorder
        )
    ) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .heightIn(max = 640.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Surface)
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
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                        )
                        IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Close, "Tutup", tint = OnSurfaceVariant)
                        }
                    }

                    HorizontalDivider(color = Neutral)

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
                                            color = if (selected) Color.White else Secondary
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
                        colors = fieldColors
                    )

                    OutlinedTextField(
                        value = uiState.formKategori,
                        onValueChange = onKategoriChange,
                        label = { Text("Kategori (opsional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = fieldColors
                    )

                    OutlinedTextField(
                        value = uiState.formSatuan,
                        onValueChange = onSatuanChange,
                        label = { Text("Satuan *") },
                        placeholder = { Text("pcs, kg, jam, ...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = fieldColors
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
                            colors = fieldColors
                        )
                        OutlinedTextField(
                            value = uiState.formHargaBeli,
                            onValueChange = onHargaBeliChange,
                            label = { Text("Harga Beli") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = fieldColors
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
                            colors = fieldColors
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
                            contentColor = Color.White,
                            disabledContainerColor = Primary.copy(alpha = 0.5f)
                        )
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
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
                        OutlinedButton(
                            onClick = onHapus,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            enabled = !uiState.isDeleting,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Error),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Error.copy(alpha = 0.4f))
                        ) {
                            Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Hapus", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                    }
                }
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
            onDismissRequest = onDismiss,
            containerColor = Color.White,
            titleContentColor = Secondary,
            textContentColor = OnSurface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.Warning, null, tint = Error)
                    Text("Hapus Barang/Jasa", fontWeight = FontWeight.ExtraBold, color = Secondary)
                }
            },
            text = {
                Text("Yakin ingin menghapus \"$nama\"? Tindakan ini permanen.")
            },
            confirmButton = {
                Button(
                    onClick = onConfirm,
                    enabled = !isDeleting,
                    colors = ButtonDefaults.buttonColors(containerColor = Error),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isDeleting) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("Hapus", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Batal", color = OnSurfaceVariant)
                }
            }
        )
    }
}

// ─── Helpers ────────────────────────────────────────────────

private fun formatRupiahBarangJasa(amount: Double): String {
    val absStr = kotlin.math.abs(amount).toLong().toString()
    val sb = StringBuilder()
    var count = 0
    for (i in absStr.lastIndex downTo 0) {
        if (count > 0 && count % 3 == 0) sb.insert(0, '.')
        sb.insert(0, absStr[i])
        count++
    }
    return "Rp $sb"
}
