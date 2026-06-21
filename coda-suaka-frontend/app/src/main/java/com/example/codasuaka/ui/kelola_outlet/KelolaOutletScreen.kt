package com.example.codasuaka.ui.kelola_outlet

import androidx.compose.foundation.background
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
import com.example.codasuaka.ui.theme.*

/**
 * Screen Kelola Outlet.
 *
 * Tampilan utama: daftar outlet + FAB (+) untuk tambah outlet.
 * Tampilan kedua (dialog): form tambah outlet atau detail outlet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KelolaOutletScreen(
    onBack: () -> Unit,
    viewModel: KelolaOutletViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text("Kelola Outlet", fontWeight = FontWeight.SemiBold, color = OnPrimary)
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
                Icon(Icons.Default.Add, "Tambah Outlet", modifier = Modifier.size(28.dp))
            }
        }
    ) { innerPadding ->
        // ── Loading ──
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
            // Success message
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

            // Error message
            if (uiState.errorMessage != null && uiState.dialogMode !is DialogMode.Tambah) {
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

            // Header + badge
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Daftar Outlet",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = OnSurfaceVariant
                    )
                    Surface(shape = RoundedCornerShape(12.dp), color = Primary.copy(alpha = 0.1f)) {
                        Text(
                            "${uiState.outlets.size} outlet",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = Primary
                        )
                    }
                }
            }

            // Daftar outlet atau empty state
            if (uiState.outlets.isEmpty()) {
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
                                Icons.Default.Store,
                                null,
                                tint = Neutral,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                "Belum ada outlet",
                                style = MaterialTheme.typography.bodyLarge,
                                color = OnSurfaceVariant
                            )
                            Text(
                                "Tekan tombol + untuk menambahkan outlet baru.",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceVariant
                            )
                        }
                    }
                }
            }

            items(uiState.outlets, key = { it.id }) { outlet ->
                OutletListItem(
                    outlet = outlet,
                    onClick = { viewModel.openDialogDetail(outlet) }
                )
            }

            item { Spacer(modifier = Modifier.height(72.dp)) }
        }
    }

    // ─── Dialog ──────────────────────────────────────────────
    when (val dialog = uiState.dialogMode) {
        is DialogMode.Tambah -> {
            DialogTambahOutlet(
                namaOutlet = uiState.formNamaOutlet,
                alamatOutlet = uiState.formAlamatOutlet,
                isSaving = uiState.isSaving,
                errorMessage = uiState.errorMessage,
                onNamaChange = viewModel::onFormNamaOutletChange,
                onAlamatChange = viewModel::onFormAlamatOutletChange,
                onSimpan = viewModel::simpanOutlet,
                onDismiss = viewModel::closeDialog
            )
        }
        is DialogMode.Detail -> {
            DialogDetailOutlet(
                outlet = dialog.outlet,
                onHapus = { viewModel.hapusOutlet(dialog.outlet.id) },
                onDismiss = viewModel::closeDialog
            )
        }
        DialogMode.Closed -> { /* tidak ada dialog */ }
    }
}

// ═══════════════════════════════════════════════════════════
// LIST ITEM — Satu outlet di daftar
// ═══════════════════════════════════════════════════════════

@Composable
private fun OutletListItem(
    outlet: Outlet,
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
            // Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Store,
                    null,
                    tint = Primary,
                    modifier = Modifier.size(26.dp)
                )
            }

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    outlet.namaOutlet,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = OnSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        outlet.alamatOutlet,
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
                            "${outlet.jumlahKaryawan} karyawan",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Secondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Arrow
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
// DIALOG — Tambah Outlet
// ═══════════════════════════════════════════════════════════

@Composable
private fun DialogTambahOutlet(
    namaOutlet: String,
    alamatOutlet: String,
    isSaving: Boolean,
    errorMessage: String?,
    onNamaChange: (String) -> Unit,
    onAlamatChange: (String) -> Unit,
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
                        "Tambah Outlet",
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
                    value = namaOutlet,
                    onValueChange = onNamaChange,
                    label = { Text("Nama Outlet") },
                    placeholder = { Text("Masukkan nama outlet") },
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
                    value = alamatOutlet,
                    onValueChange = onAlamatChange,
                    label = { Text("Alamat Outlet") },
                    placeholder = { Text("Masukkan alamat outlet") },
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

                // Tombol
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
                    Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Buat Outlet", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// DIALOG — Detail Outlet
// ═══════════════════════════════════════════════════════════

@Composable
private fun DialogDetailOutlet(
    outlet: Outlet,
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
                        "Detail Outlet",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = OnSurface
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, "Tutup", tint = OnSurfaceVariant)
                    }
                }

                HorizontalDivider(color = Neutral)

                // Icon besar
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Primary.copy(alpha = 0.1f))
                        .align(Alignment.CenterHorizontally),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Store,
                        null,
                        tint = Primary,
                        modifier = Modifier.size(40.dp)
                    )
                }

                // Nama
                Text(
                    outlet.namaOutlet,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                // Alamat
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.LocationOn, null, tint = OnSurfaceVariant, modifier = Modifier.size(20.dp))
                    Text(outlet.alamatOutlet, style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
                }

                // Jumlah karyawan
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.People, null, tint = OnSurfaceVariant, modifier = Modifier.size(20.dp))
                    Text(
                        "${outlet.jumlahKaryawan} Karyawan",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )
                }

                HorizontalDivider(color = Neutral)

                // Tombol hapus
                Button(
                    onClick = onHapus,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Error,
                        contentColor = OnPrimary
                    )
                ) {
                    Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Hapus Outlet", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
            }
        }
    }
}
