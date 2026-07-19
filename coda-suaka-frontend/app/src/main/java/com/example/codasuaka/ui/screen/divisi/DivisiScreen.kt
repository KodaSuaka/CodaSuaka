package com.example.codasuaka.ui.screen.divisi

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.codasuaka.ui.screen.kelola_karyawan.Karyawan
import com.example.codasuaka.ui.screen.kelola_outlet.Outlet
import com.example.codasuaka.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DivisiScreen(
    onBack: () -> Unit,
    viewModel: DivisiViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text("Divisi", fontWeight = FontWeight.Bold, color = Secondary)
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
                Icon(Icons.Default.Add, "Tambah Divisi", modifier = Modifier.size(32.dp))
            }
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
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
            // ── Success Message ──
            if (uiState.successMessage != null) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Success.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, null, tint = Success, modifier = Modifier.size(20.dp))
                            Text(
                                uiState.successMessage ?: "",
                                color = Success,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            // ── Error Message ──
            if (uiState.errorMessage != null &&
                uiState.dialogMode !is DivisiDialogMode.Tambah &&
                uiState.dialogMode !is DivisiDialogMode.Edit
            ) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Error.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Error, null, tint = Error, modifier = Modifier.size(20.dp))
                            Text(
                                uiState.errorMessage ?: "",
                                color = Error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            // ── Header + Counter ──
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Daftar Divisi",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Secondary
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp), 
                        color = Primary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            "${uiState.divisiList.size} divisi",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                    }
                }
            }

            // ── Filter Outlet Dropdown (hanya jika >1 outlet) ──
            if (uiState.outlets.size > 1) {
                item {
                    FilterOutletDropdownDivisi(
                        outlets = uiState.outlets,
                        selectedOutletId = uiState.selectedOutletId,
                        onOutletSelected = { outletId ->
                            viewModel.loadDivisi(outletId)
                        }
                    )
                }
            }

            // ── Daftar Divisi atau Empty State ──
            if (uiState.divisiList.isEmpty()) {
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
                                Icons.Default.Group,
                                null,
                                tint = Neutral,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                "Belum ada divisi",
                                style = MaterialTheme.typography.bodyLarge,
                                color = OnSurfaceVariant
                            )
                            Text(
                                "Tekan tombol + untuk menambahkan divisi baru.",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceVariant
                            )
                        }
                    }
                }
            }

            items(uiState.divisiList, key = { it.id }) { divisi ->
                DivisiListItem(
                    divisi = divisi,
                    onClick = { viewModel.openDialogEdit(divisi) }
                )
            }

            item { Spacer(modifier = Modifier.height(72.dp)) }
        }
    }

    // ─── Dialog ──────────────────────────────────────────────
    when (val dialog = uiState.dialogMode) {
        is DivisiDialogMode.Tambah -> {
            DialogTambahDivisi(
                namaDivisi = uiState.formNamaDivisi,
                deskripsi = uiState.formDeskripsi,
                selectedKetuaId = uiState.formKetuaKaryawanId,
                selectedOutletId = uiState.formOutletId,
                anggotaTerpilih = uiState.formAnggota,
                availableKaryawan = uiState.formAvailableKaryawan,
                karyawanList = uiState.karyawanList,
                outlets = uiState.outlets,
                isSaving = uiState.isSaving,
                errorMessage = uiState.errorMessage,
                onNamaDivisiChange = viewModel::onFormNamaDivisiChange,
                onDeskripsiChange = viewModel::onFormDeskripsiChange,
                onKetuaChange = viewModel::onFormKetuaChange,
                onOutletChange = viewModel::onFormOutletChange,
                onTambahAnggota = viewModel::tambahAnggota,
                onHapusAnggota = viewModel::hapusAnggota,
                onSimpan = viewModel::simpanDivisi,
                onDismiss = viewModel::closeDialog
            )
        }
        is DivisiDialogMode.Edit -> {
            DialogEditDivisi(
                namaDivisi = uiState.formNamaDivisi,
                deskripsi = uiState.formDeskripsi,
                selectedKetuaId = uiState.formKetuaKaryawanId,
                selectedOutletId = uiState.formOutletId,
                anggotaTerpilih = uiState.formAnggota,
                availableKaryawan = uiState.formAvailableKaryawan,
                ketuaName = dialog.divisi.ketua?.namaLengkap ?: "-",
                karyawanList = uiState.karyawanList,
                outlets = uiState.outlets,
                isSaving = uiState.isSaving,
                errorMessage = uiState.errorMessage,
                onNamaDivisiChange = viewModel::onFormNamaDivisiChange,
                onDeskripsiChange = viewModel::onFormDeskripsiChange,
                onKetuaChange = viewModel::onFormKetuaChange,
                onOutletChange = viewModel::onFormOutletChange,
                onTambahAnggota = viewModel::tambahAnggota,
                onHapusAnggota = viewModel::hapusAnggota,
                onSimpan = viewModel::updateDivisi,
                onHapus = { viewModel.hapusDivisi(dialog.divisi.id) },
                onDismiss = viewModel::closeDialog
            )
        }
        DivisiDialogMode.Closed -> { /* tidak ada dialog */ }
    }
}

// ═══════════════════════════════════════════════════════════
// FILTER OUTLET DROPDOWN
// ═══════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterOutletDropdownDivisi(
    outlets: List<Outlet>,
    selectedOutletId: Int?,
    onOutletSelected: (Int?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedOutlet = outlets.find { it.id == selectedOutletId }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selectedOutlet?.namaOutlet ?: "Semua Outlet",
            onValueChange = {},
            readOnly = true,
            label = { Text("Filter Outlet") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                unfocusedBorderColor = Neutral,
                focusedContainerColor = Surface,
                unfocusedContainerColor = Surface,
                cursorColor = Primary,
                focusedLabelColor = Primary,
                unfocusedLabelColor = OnSurfaceVariant
            )
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Semua Outlet") },
                onClick = {
                    onOutletSelected(null)
                    expanded = false
                }
            )
            outlets.forEach { outlet ->
                DropdownMenuItem(
                    text = { Text(outlet.namaOutlet) },
                    onClick = {
                        onOutletSelected(outlet.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// LIST ITEM — Satu divisi di daftar
// ═══════════════════════════════════════════════════════════

@Composable
private fun DivisiListItem(
    divisi: Divisi,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Neutral)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Baris 1: Avatar + Nama Divisi + Chevron
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Avatar dengan ukuran yang disesuaikan
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Secondary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Group,
                        null,
                        tint = Secondary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Nama Divisi
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        divisi.namaDivisi,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = Secondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Icon(
                    Icons.Default.ChevronRight,
                    null,
                    tint = Neutral,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Deskripsi
            if (divisi.deskripsi.isNotBlank()) {
                Text(
                    divisi.deskripsi,
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Baris info: Ketua + Anggota
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Ketua
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Default.Person,
                        null,
                        tint = Secondary.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Ketua: ${divisi.ketua?.namaLengkap ?: "-"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Secondary,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Badge Anggota
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Primary.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.People,
                            null,
                            tint = Primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            "${divisi.anggotaCount} Anggota",
                            style = MaterialTheme.typography.labelSmall,
                            color = Primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// SHARED — Anggota Section (used in both Tambah & Edit)
// ═══════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AnggotaSection(
    anggotaTerpilih: List<Karyawan>,
    availableKaryawan: List<Karyawan>,
    onTambahAnggota: (Karyawan) -> Unit,
    onHapusAnggota: (Karyawan) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Anggota Divisi",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = OnSurface
            )
            Surface(shape = RoundedCornerShape(12.dp), color = Primary.copy(alpha = 0.1f)) {
                Text(
                    "${anggotaTerpilih.size} anggota",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = Primary
                )
            }
        }

        // Dropdown tambah anggota
        if (availableKaryawan.isNotEmpty()) {
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = "Tambah Anggota",
                    onValueChange = {},
                    readOnly = true,
                    leadingIcon = {
                        Icon(Icons.Default.PersonAdd, null, tint = OnSurfaceVariant, modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = Neutral,
                        focusedContainerColor = Surface,
                        unfocusedContainerColor = Surface,
                        cursorColor = Primary,
                        focusedLabelColor = Primary,
                        unfocusedLabelColor = OnSurfaceVariant
                    )
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    availableKaryawan.forEach { karyawan ->
                        DropdownMenuItem(
                            text = { Text(karyawan.namaLengkap) },
                            onClick = {
                                onTambahAnggota(karyawan)
                                expanded = false
                            }
                        )
                    }
                }
            }
        } else {
            Text(
                "Semua karyawan sudah menjadi anggota",
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceVariant
            )
        }

        // Daftar anggota terpilih
        if (anggotaTerpilih.isEmpty()) {
            Text(
                "Belum ada anggota",
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceVariant
            )
        } else {
            anggotaTerpilih.forEach { karyawan ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Tertiary)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Person,
                        null,
                        tint = Secondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        karyawan.namaLengkap,
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurface,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { onHapusAnggota(karyawan) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            "Hapus anggota",
                            tint = Error,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// DIALOG — Tambah Divisi
// ═══════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogTambahDivisi(
    namaDivisi: String,
    deskripsi: String,
    selectedKetuaId: String?,
    selectedOutletId: Int,
    anggotaTerpilih: List<Karyawan>,
    availableKaryawan: List<Karyawan>,
    karyawanList: List<Karyawan>,
    outlets: List<Outlet>,
    isSaving: Boolean,
    errorMessage: String?,
    onNamaDivisiChange: (String) -> Unit,
    onDeskripsiChange: (String) -> Unit,
    onKetuaChange: (String?) -> Unit,
    onOutletChange: (Int) -> Unit,
    onTambahAnggota: (Karyawan) -> Unit,
    onHapusAnggota: (Karyawan) -> Unit,
    onSimpan: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Tambah Divisi",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = OnSurface
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, "Tutup", tint = OnSurfaceVariant)
                    }
                }

                HorizontalDivider(color = Neutral)

                // Error message
                if (errorMessage != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Error.copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Error, null, tint = Error, modifier = Modifier.size(18.dp))
                            Text(errorMessage, color = Error, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                // Nama Divisi
                OutlinedTextField(
                    value = namaDivisi,
                    onValueChange = onNamaDivisiChange,
                    label = { Text("Nama Divisi") },
                    placeholder = { Text("Masukkan nama divisi") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = Neutral,
                        focusedContainerColor = Surface,
                        unfocusedContainerColor = Surface,
                        cursorColor = Primary,
                        focusedLabelColor = Primary,
                        unfocusedLabelColor = OnSurfaceVariant
                    )
                )

                // Deskripsi
                OutlinedTextField(
                    value = deskripsi,
                    onValueChange = onDeskripsiChange,
                    label = { Text("Deskripsi") },
                    placeholder = { Text("Masukkan deskripsi divisi") },
                    minLines = 2,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = Neutral,
                        focusedContainerColor = Surface,
                        unfocusedContainerColor = Surface,
                        cursorColor = Primary,
                        focusedLabelColor = Primary,
                        unfocusedLabelColor = OnSurfaceVariant
                    )
                )

                // Ketua Dropdown
                KetuaDropdown(
                    karyawanList = karyawanList,
                    selectedKetuaId = selectedKetuaId,
                    onKetuaSelected = onKetuaChange
                )

                // Outlet Dropdown (selalu tampilkan)
                OutletDropdownDivisi(
                    outlets = outlets,
                    selectedOutletId = selectedOutletId,
                    onOutletSelected = onOutletChange
                )

                // ── Anggota Section ──
                AnggotaSection(
                    anggotaTerpilih = anggotaTerpilih,
                    availableKaryawan = availableKaryawan,
                    onTambahAnggota = onTambahAnggota,
                    onHapusAnggota = onHapusAnggota
                )

                // Tombol Simpan
                Button(
                    onClick = onSimpan,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = !isSaving,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        contentColor = OnPrimary,
                        disabledContainerColor = Primary.copy(alpha = 0.5f)
                    )
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = OnPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tambah Divisi", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// DIALOG — Edit Divisi
// ═══════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogEditDivisi(
    namaDivisi: String,
    deskripsi: String,
    selectedKetuaId: String?,
    selectedOutletId: Int,
    anggotaTerpilih: List<Karyawan>,
    availableKaryawan: List<Karyawan>,
    ketuaName: String,
    karyawanList: List<Karyawan>,
    outlets: List<Outlet>,
    isSaving: Boolean,
    errorMessage: String?,
    onNamaDivisiChange: (String) -> Unit,
    onDeskripsiChange: (String) -> Unit,
    onKetuaChange: (String?) -> Unit,
    onOutletChange: (Int) -> Unit,
    onTambahAnggota: (Karyawan) -> Unit,
    onHapusAnggota: (Karyawan) -> Unit,
    onSimpan: () -> Unit,
    onHapus: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Edit Divisi",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = OnSurface
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, "Tutup", tint = OnSurfaceVariant)
                    }
                }

                HorizontalDivider(color = Neutral)

                // Error message
                if (errorMessage != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Error.copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Error, null, tint = Error, modifier = Modifier.size(18.dp))
                            Text(errorMessage, color = Error, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                // Nama Divisi
                OutlinedTextField(
                    value = namaDivisi,
                    onValueChange = onNamaDivisiChange,
                    label = { Text("Nama Divisi") },
                    placeholder = { Text("Masukkan nama divisi") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = Neutral,
                        focusedContainerColor = Surface,
                        unfocusedContainerColor = Surface,
                        cursorColor = Primary,
                        focusedLabelColor = Primary,
                        unfocusedLabelColor = OnSurfaceVariant
                    )
                )

                // Deskripsi
                OutlinedTextField(
                    value = deskripsi,
                    onValueChange = onDeskripsiChange,
                    label = { Text("Deskripsi") },
                    placeholder = { Text("Masukkan deskripsi divisi") },
                    minLines = 2,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = Neutral,
                        focusedContainerColor = Surface,
                        unfocusedContainerColor = Surface,
                        cursorColor = Primary,
                        focusedLabelColor = Primary,
                        unfocusedLabelColor = OnSurfaceVariant
                    )
                )

                // Ketua Dropdown
                KetuaDropdown(
                    karyawanList = karyawanList,
                    selectedKetuaId = selectedKetuaId,
                    onKetuaSelected = onKetuaChange
                )

                // Outlet Dropdown (selalu tampilkan)
                OutletDropdownDivisi(
                    outlets = outlets,
                    selectedOutletId = selectedOutletId,
                    onOutletSelected = onOutletChange
                )

                // ── Anggota Section ──
                AnggotaSection(
                    anggotaTerpilih = anggotaTerpilih,
                    availableKaryawan = availableKaryawan,
                    onTambahAnggota = onTambahAnggota,
                    onHapusAnggota = onHapusAnggota
                )

                // Baris Tombol: Hapus | Simpan
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onHapus,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Error,
                            contentColor = OnPrimary
                        )
                    ) {
                        Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Hapus", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }

                    Button(
                        onClick = onSimpan,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        enabled = !isSaving,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary,
                            contentColor = OnPrimary,
                            disabledContainerColor = Primary.copy(alpha = 0.5f)
                        )
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = OnPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Simpan", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// REUSABLE — Ketua Dropdown
// ═══════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KetuaDropdown(
    karyawanList: List<Karyawan>,
    selectedKetuaId: String?,
    onKetuaSelected: (String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedKaryawan = karyawanList.find { it.id == selectedKetuaId }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selectedKaryawan?.namaLengkap ?: "Pilih Ketua",
            onValueChange = {},
            readOnly = true,
            label = { Text("Ketua Divisi") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                unfocusedBorderColor = Neutral,
                focusedContainerColor = Surface,
                unfocusedContainerColor = Surface,
                cursorColor = Primary,
                focusedLabelColor = Primary,
                unfocusedLabelColor = OnSurfaceVariant
            )
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Pilih Ketua") },
                onClick = {
                    onKetuaSelected(null)
                    expanded = false
                }
            )
            karyawanList.forEach { karyawan ->
                DropdownMenuItem(
                    text = { Text(karyawan.namaLengkap) },
                    onClick = {
                        onKetuaSelected(karyawan.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// REUSABLE — Outlet Dropdown
// ═══════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OutletDropdownDivisi(
    outlets: List<Outlet>,
    selectedOutletId: Int,
    onOutletSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedOutlet = outlets.find { it.id == selectedOutletId }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selectedOutlet?.namaOutlet ?: "Pilih Outlet",
            onValueChange = {},
            readOnly = true,
            label = { Text("Outlet") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                unfocusedBorderColor = Neutral,
                focusedContainerColor = Surface,
                unfocusedContainerColor = Surface,
                cursorColor = Primary,
                focusedLabelColor = Primary,
                unfocusedLabelColor = OnSurfaceVariant
            )
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            outlets.forEach { outlet ->
                DropdownMenuItem(
                    text = { Text(outlet.namaOutlet) },
                    onClick = {
                        onOutletSelected(outlet.id)
                        expanded = false
                    }
                )
            }
        }
    }
}
