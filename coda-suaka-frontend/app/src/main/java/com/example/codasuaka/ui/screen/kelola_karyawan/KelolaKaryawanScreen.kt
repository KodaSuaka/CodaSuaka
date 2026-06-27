package com.example.codasuaka.ui.screen.kelola_karyawan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.codasuaka.ui.screen.kelola_outlet.Outlet
import com.example.codasuaka.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KelolaKaryawanScreen(
    onBack: () -> Unit,
    viewModel: KelolaKaryawanViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text("Kelola Karyawan", fontWeight = FontWeight.SemiBold, color = OnPrimary)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Kembali", tint = OnPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openDialogTambah() },
                containerColor = Primary,
                contentColor = OnPrimary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.PersonAdd, "Tambah Karyawan", modifier = Modifier.size(28.dp))
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
                uiState.dialogMode !is KaryawanDialogMode.Tambah &&
                uiState.dialogMode !is KaryawanDialogMode.Edit
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
                        .padding(start = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Daftar Karyawan",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = OnSurfaceVariant
                    )
                    Surface(shape = RoundedCornerShape(12.dp), color = Primary.copy(alpha = 0.1f)) {
                        Text(
                            "${uiState.karyawanList.size}/5 karyawan",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = Primary
                        )
                    }
                }
            }

            // ── Filter Outlet Dropdown (hanya jika >1 outlet) ──
            if (uiState.outlets.size > 1) {
                item {
                    FilterOutletDropdown(
                        outlets = uiState.outlets,
                        selectedOutletId = uiState.selectedOutletId,
                        onOutletSelected = { outletId ->
                            viewModel.loadKaryawan(outletId)
                        }
                    )
                }
            }

            // ── Daftar Karyawan atau Empty State ──
            if (uiState.karyawanList.isEmpty()) {
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
                                Icons.Default.People,
                                null,
                                tint = Neutral,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                "Belum ada karyawan",
                                style = MaterialTheme.typography.bodyLarge,
                                color = OnSurfaceVariant
                            )
                            Text(
                                "Tekan tombol + untuk menambahkan karyawan baru.",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceVariant
                            )
                        }
                    }
                }
            }

            items(uiState.karyawanList, key = { it.id }) { karyawan ->
                KaryawanListItem(
                    karyawan = karyawan,
                    onClick = { viewModel.openDialogEdit(karyawan) }
                )
            }

            item { Spacer(modifier = Modifier.height(72.dp)) }
        }
    }

    // ─── Dialog ──────────────────────────────────────────────
    when (val dialog = uiState.dialogMode) {
        is KaryawanDialogMode.Tambah -> {
            DialogTambahKaryawan(
                nama = uiState.formNama,
                alamat = uiState.formAlamat,
                selectedRoleId = uiState.formRoleId,
                selectedOutletId = uiState.formOutletId,
                roles = uiState.roles,
                outlets = uiState.outlets,
                isSaving = uiState.isSaving,
                errorMessage = uiState.errorMessage,
                onNamaChange = viewModel::onFormNamaChange,
                onAlamatChange = viewModel::onFormAlamatChange,
                onRoleChange = viewModel::onFormRoleChange,
                onOutletChange = viewModel::onFormOutletChange,
                onSimpan = viewModel::simpanKaryawan,
                onDismiss = viewModel::closeDialog
            )
        }
        is KaryawanDialogMode.Edit -> {
            DialogEditKaryawan(
                nama = uiState.formNama,
                alamat = uiState.formAlamat,
                selectedOutletId = uiState.formOutletId,
                outlets = uiState.outlets,
                roleName = dialog.karyawan.role?.namaRole ?: "-",
                isSaving = uiState.isSaving,
                errorMessage = uiState.errorMessage,
                onNamaChange = viewModel::onFormNamaChange,
                onAlamatChange = viewModel::onFormAlamatChange,
                onOutletChange = viewModel::onFormOutletChange,
                onSimpan = viewModel::updateKaryawan,
                onHapus = { viewModel.hapusKaryawan(dialog.karyawan.id) },
                onDismiss = viewModel::closeDialog
            )
        }
        KaryawanDialogMode.Closed -> { /* tidak ada dialog */ }
    }
}

// ═══════════════════════════════════════════════════════════
// FILTER OUTLET DROPDOWN
// ═══════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterOutletDropdown(
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
// LIST ITEM — Satu karyawan di daftar
// ═══════════════════════════════════════════════════════════

@Composable
private fun KaryawanListItem(
    karyawan: Karyawan,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Secondary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    null,
                    tint = Secondary,
                    modifier = Modifier.size(26.dp)
                )
            }

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    karyawan.namaLengkap,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = OnSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        karyawan.alamat,
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (karyawan.role != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Secondary.copy(alpha = 0.1f)
                        ) {
                            Text(
                                karyawan.role.namaRole,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = Secondary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Icon(
                Icons.Default.ChevronRight,
                null,
                tint = OnSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════
// DIALOG — Tambah Karyawan
// ═══════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogTambahKaryawan(
    nama: String,
    alamat: String,
    selectedRoleId: Int,
    selectedOutletId: Int,
    roles: List<Role>,
    outlets: List<Outlet>,
    isSaving: Boolean,
    errorMessage: String?,
    onNamaChange: (String) -> Unit,
    onAlamatChange: (String) -> Unit,
    onRoleChange: (Int) -> Unit,
    onOutletChange: (Int) -> Unit,
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
                        "Tambah Karyawan",
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

                // Nama
                OutlinedTextField(
                    value = nama,
                    onValueChange = onNamaChange,
                    label = { Text("Nama Karyawan") },
                    placeholder = { Text("Masukkan nama karyawan") },
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

                // Alamat
                OutlinedTextField(
                    value = alamat,
                    onValueChange = onAlamatChange,
                    label = { Text("Alamat Karyawan") },
                    placeholder = { Text("Masukkan alamat karyawan") },
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

                // Role Dropdown
                RoleDropdown(
                    roles = roles,
                    selectedRoleId = selectedRoleId,
                    onRoleSelected = onRoleChange
                )

                // Outlet Dropdown (hanya jika >1 outlet)
                if (outlets.size > 1) {
                    OutletDropdown(
                        outlets = outlets,
                        selectedOutletId = selectedOutletId,
                        onOutletSelected = onOutletChange
                    )
                }

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
                    Icon(Icons.Default.PersonAdd, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tambah Karyawan", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// DIALOG — Edit Karyawan
// ═══════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogEditKaryawan(
    nama: String,
    alamat: String,
    selectedOutletId: Int,
    outlets: List<Outlet>,
    roleName: String,
    isSaving: Boolean,
    errorMessage: String?,
    onNamaChange: (String) -> Unit,
    onAlamatChange: (String) -> Unit,
    onOutletChange: (Int) -> Unit,
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
                        "Edit Karyawan",
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

                // Nama
                OutlinedTextField(
                    value = nama,
                    onValueChange = onNamaChange,
                    label = { Text("Nama Karyawan") },
                    placeholder = { Text("Masukkan nama karyawan") },
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

                // Alamat
                OutlinedTextField(
                    value = alamat,
                    onValueChange = onAlamatChange,
                    label = { Text("Alamat Karyawan") },
                    placeholder = { Text("Masukkan alamat karyawan") },
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

                // Role (read-only)
                OutlinedTextField(
                    value = roleName,
                    onValueChange = {},
                    label = { Text("Role") },
                    readOnly = true,
                    enabled = false,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Neutral,
                        unfocusedBorderColor = Neutral,
                        focusedContainerColor = Tertiary,
                        unfocusedContainerColor = Tertiary,
                        disabledBorderColor = Neutral,
                        disabledContainerColor = Tertiary,
                        disabledLabelColor = OnSurfaceVariant,
                        disabledTextColor = OnSurface
                    )
                )

                // Outlet Dropdown (hanya jika >1 outlet)
                if (outlets.size > 1) {
                    OutletDropdown(
                        outlets = outlets,
                        selectedOutletId = selectedOutletId,
                        onOutletSelected = onOutletChange
                    )
                }

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
// REUSABLE — Role Dropdown
// ═══════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoleDropdown(
    roles: List<Role>,
    selectedRoleId: Int,
    onRoleSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedRole = roles.find { it.id == selectedRoleId }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selectedRole?.namaRole ?: "Pilih Role",
            onValueChange = {},
            readOnly = true,
            label = { Text("Role") },
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
            roles.forEach { role ->
                DropdownMenuItem(
                    text = { Text(role.namaRole) },
                    onClick = {
                        onRoleSelected(role.id)
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
private fun OutletDropdown(
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
