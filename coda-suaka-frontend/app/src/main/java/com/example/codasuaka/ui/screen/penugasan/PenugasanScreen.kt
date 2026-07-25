package com.example.codasuaka.ui.screen.penugasan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codasuaka.data.remote.dto.DivisiDto
import com.example.codasuaka.data.remote.dto.KaryawanDto
import com.example.codasuaka.data.remote.dto.PenugasanDto
import com.example.codasuaka.ui.components.NotificationBannerStatic
import com.example.codasuaka.ui.theme.*

// ─── Colors ────────────────────────────────────────────────
private val UrgentColor = Color(0xFFEF4444)
private val SedangColor = Color(0xFFF59E0B)
private val RendahColor = Color(0xFF10B981)
private val StatusBelum = Color(0xFF6B7280)
private val StatusProses = Color(0xFF3B82F6)
private val StatusSelesai = Color(0xFF10B981)

// ─── PenugasanScreen ──────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PenugasanScreen(
    onBack: () -> Unit,
    viewModel: PenugasanViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Penugasan",
                        fontWeight = FontWeight.Bold,
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
                actions = {
                    IconButton(onClick = { viewModel.showCreateDialog() }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Tambah Tugas",
                            tint = Secondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showCreateDialog() },
                containerColor = Primary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Tugas")
            }
        },
        containerColor = Tertiary
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Error / Success Message ──
            if (uiState.errorMessage != null) {
                NotificationBannerStatic(
                    message = uiState.errorMessage ?: "",
                    mapFromServer = true,
                    onDismiss = { viewModel.clearError() }
                )
            }
            if (uiState.successMessage != null) {
                NotificationBannerStatic(
                    message = uiState.successMessage ?: "",
                    mapFromServer = false,
                    onDismiss = { viewModel.clearSuccess() }
                )
            }

            // ── Filter Chips ──
            FilterChipRow(
                selectedStatus = uiState.filterStatus,
                onStatusSelected = { viewModel.filterByStatus(it) }
            )

            // ── Loading ──
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Primary)
                }
            } else if (uiState.penugasans.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Assignment,
                            contentDescription = null,
                            tint = OnSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Belum ada tugas",
                            style = MaterialTheme.typography.bodyLarge,
                            color = OnSurfaceVariant
                        )
                    }
                }
            } else {
                // ── Task List ──
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.penugasans, key = { it.id }) { penugasan ->
                        PenugasanCard(
                            penugasan = penugasan,
                            onDelete = { viewModel.deletePenugasan(penugasan.id) }
                        )
                    }
                }
            }
        }
    }

    // ── Create Dialog ──
    if (uiState.showCreateDialog) {
        CreatePenugasanDialog(
            uiState = uiState,
            onDismiss = { viewModel.dismissCreateDialog() },
            onJudulChange = { viewModel.updateFormJudul(it) },
            onDeskripsiChange = { viewModel.updateFormDeskripsi(it) },
            onPenanggungJawabChange = { viewModel.updateFormPenanggungJawabId(it) },
            onDivisiChange = { viewModel.updateFormDivisiId(it) },
            onTenggatChange = { viewModel.updateFormTenggat(it) },
            onUrgencyChange = { viewModel.updateFormUrgency(it) },
            onCreate = { viewModel.createPenugasan() }
        )
    }
}

// ─── Filter Chips ─────────────────────────────────────────

@Composable
private fun FilterChipRow(
    selectedStatus: String?,
    onStatusSelected: (String?) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        FilterChip(
            selected = selectedStatus == null,
            onClick = { onStatusSelected(null) },
            label = { Text("Semua", fontSize = 12.sp) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = Primary,
                selectedLabelColor = Color.White
            )
        )
        listOf("belum", "proses", "selesai").forEach { status ->
            FilterChip(
                selected = selectedStatus == status,
                onClick = { onStatusSelected(status) },
                label = {
                    Text(
                        text = status.replaceFirstChar { it.uppercase() },
                        fontSize = 12.sp
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = when (status) {
                        "belum" -> StatusBelum
                        "proses" -> StatusProses
                        else -> StatusSelesai
                    },
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

// ─── Penugasan Card ───────────────────────────────────────

@Composable
private fun PenugasanCard(
    penugasan: PenugasanDto,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val urgencyColor = when (penugasan.urgency) {
        "urgent" -> UrgentColor
        "sedang" -> SedangColor
        else -> RendahColor
    }

    val statusColor = when (penugasan.status) {
        "belum" -> StatusBelum
        "proses" -> StatusProses
        "selesai" -> StatusSelesai
        else -> StatusBelum
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ── Header: Judul + Actions ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = penugasan.judul,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Secondary,
                    modifier = Modifier.weight(1f)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Urgency Badge
                    Surface(
                        color = urgencyColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = penugasan.urgency?.replaceFirstChar { it.uppercase() } ?: "-",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = urgencyColor
                        )
                    }
                    // Status Badge
                    Surface(
                        color = statusColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = penugasan.status.replaceFirstChar { it.uppercase() },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                }
            }

            // ── Deskripsi ──
            if (!penugasan.deskripsi.isNullOrBlank()) {
                Text(
                    text = penugasan.deskripsi,
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // ── Info Row ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Penanggung Jawab
                if (penugasan.penanggungJawab != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = OnSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = penugasan.penanggungJawab.namaLengkap,
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceVariant
                        )
                    }
                }

                // Divisi
                if (penugasan.divisi != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Business,
                            contentDescription = null,
                            tint = OnSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = penugasan.divisi.namaDivisi,
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceVariant
                        )
                    }
                }

                // Tenggat
                if (!penugasan.tenggat.isNullOrBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = OnSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = penugasan.tenggat,
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceVariant
                        )
                    }
                }
            }

            // ── Delete Button ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = { showDeleteConfirm = true },
                    colors = ButtonDefaults.textButtonColors(contentColor = Error)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Hapus", fontSize = 12.sp)
                }
            }
        }
    }

    // ── Delete Confirmation Dialog ──
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Hapus Tugas", fontWeight = FontWeight.Bold) },
            text = { Text("Yakin ingin menghapus tugas \"${penugasan.judul}\"?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Error)
                ) {
                    Text("Hapus", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirm = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

// ─── Create Dialog ────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreatePenugasanDialog(
    uiState: PenugasanUiState,
    onDismiss: () -> Unit,
    onJudulChange: (String) -> Unit,
    onDeskripsiChange: (String) -> Unit,
    onPenanggungJawabChange: (String) -> Unit,
    onDivisiChange: (Int?) -> Unit,
    onTenggatChange: (String) -> Unit,
    onUrgencyChange: (String) -> Unit,
    onCreate: () -> Unit
) {
    var expandedKaryawan by remember { mutableStateOf(false) }
    var expandedDivisi by remember { mutableStateOf(false) }
    var expandedUrgency by remember { mutableStateOf(false) }

    val urgencyOptions = listOf("rendah", "sedang", "urgent")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Buat Tugas Baru", fontWeight = FontWeight.Bold, color = Secondary)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Judul
                OutlinedTextField(
                    value = uiState.formJudul,
                    onValueChange = onJudulChange,
                    label = { Text("Judul Tugas *") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Deskripsi
                OutlinedTextField(
                    value = uiState.formDeskripsi,
                    onValueChange = onDeskripsiChange,
                    label = { Text("Deskripsi") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 2,
                    maxLines = 4
                )

                // Penanggung Jawab (Dropdown)
                ExposedDropdownMenuBox(
                    expanded = expandedKaryawan,
                    onExpandedChange = { expandedKaryawan = !expandedKaryawan }
                ) {
                    OutlinedTextField(
                        value = uiState.karyawans.find { it.id == uiState.formPenanggungJawabId }?.namaLengkap ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Penanggung Jawab *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedKaryawan) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedKaryawan,
                        onDismissRequest = { expandedKaryawan = false }
                    ) {
                        uiState.karyawans.forEach { karyawan ->
                            DropdownMenuItem(
                                text = { Text(karyawan.namaLengkap) },
                                onClick = {
                                    onPenanggungJawabChange(karyawan.id)
                                    expandedKaryawan = false
                                }
                            )
                        }
                    }
                }

                // Divisi (Dropdown)
                ExposedDropdownMenuBox(
                    expanded = expandedDivisi,
                    onExpandedChange = { expandedDivisi = !expandedDivisi }
                ) {
                    OutlinedTextField(
                        value = uiState.divisis.find { it.id == uiState.formDivisiId }?.namaDivisi ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Divisi") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDivisi) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedDivisi,
                        onDismissRequest = { expandedDivisi = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("— Tidak Ada —") },
                            onClick = {
                                onDivisiChange(null)
                                expandedDivisi = false
                            }
                        )
                        uiState.divisis.forEach { divisi ->
                            DropdownMenuItem(
                                text = { Text(divisi.namaDivisi) },
                                onClick = {
                                    onDivisiChange(divisi.id)
                                    expandedDivisi = false
                                }
                            )
                        }
                    }
                }

                // Tenggat
                OutlinedTextField(
                    value = uiState.formTenggat,
                    onValueChange = onTenggatChange,
                    label = { Text("Tenggat (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    placeholder = { Text("2026-12-31") }
                )

                // Urgency (Dropdown)
                ExposedDropdownMenuBox(
                    expanded = expandedUrgency,
                    onExpandedChange = { expandedUrgency = !expandedUrgency }
                ) {
                    OutlinedTextField(
                        value = uiState.formUrgency.replaceFirstChar { it.uppercase() },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Urgency") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedUrgency) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedUrgency,
                        onDismissRequest = { expandedUrgency = false }
                    ) {
                        urgencyOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.replaceFirstChar { it.uppercase() }) },
                                onClick = {
                                    onUrgencyChange(option)
                                    expandedUrgency = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onCreate,
                enabled = !uiState.isCreating,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                if (uiState.isCreating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Buat Tugas", color = Color.White)
                }
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) {
                Text("Batal")
            }
        }
    )
}
