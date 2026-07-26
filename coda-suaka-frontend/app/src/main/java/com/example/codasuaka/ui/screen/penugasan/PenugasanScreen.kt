package com.example.codasuaka.ui.screen.penugasan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codasuaka.data.remote.dto.DivisiDto
import com.example.codasuaka.data.remote.dto.KaryawanDto
import com.example.codasuaka.data.remote.dto.PenugasanDto
import com.example.codasuaka.ui.components.NotificationBannerStatic
import com.example.codasuaka.ui.theme.*
import com.example.codasuaka.util.DateTimeUtil

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

    // ─── Force Light Theme for this screen ───
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Primary,
            onPrimary = Color.White,
            secondary = Secondary,
            onSecondary = Color.White,
            tertiary = Tertiary,
            surface = Surface,
            onSurface = OnSurface,
            onSurfaceVariant = OnSurfaceVariant,
            error = Error,
            outline = NeutralBorder
        )
    ) {
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
}

// ─── Filter Chips ─────────────────────────────────────────

@Composable
private fun FilterChipRow(
    selectedStatus: String?,
    onStatusSelected: (String?) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp), // Spasi antar chip diperlebar
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 4.dp, vertical = 12.dp) // Padding luar diperbaiki
    ) {
        // --- Chip SEMUA ---
        val isSemuaSelected = selectedStatus == null
        FilterChip(
            selected = isSemuaSelected,
            onClick = { onStatusSelected(null) },
            label = { 
                Text(
                    text = "Semua", 
                    fontSize = 13.sp, 
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isSemuaSelected) Color.White else Secondary // Paksa warna teks
                ) 
            },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = Primary,
                containerColor = Neutral.copy(alpha = 0.8f) // Background unselected lebih tegas
            ),
            border = null
        )

        // --- Chip Status Lainnya ---
        listOf("belum", "proses", "selesai").forEach { status ->
            val isSelected = selectedStatus == status
            val selectedColor = when (status) {
                "belum" -> StatusBelum
                "proses" -> StatusProses
                else -> StatusSelesai
            }
            
            FilterChip(
                selected = isSelected,
                onClick = { onStatusSelected(status) },
                label = { 
                    Text(
                        text = status.replaceFirstChar { it.uppercase() },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isSelected) Color.White else Secondary // Paksa warna teks
                    ) 
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = selectedColor,
                    containerColor = Neutral.copy(alpha = 0.8f) // Background unselected lebih tegas
                ),
                border = null
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
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Neutral)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Header: Judul & Action ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = penugasan.judul,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Secondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        // Template Badge
                        if (penugasan.isTemplate == true) {
                            Surface(
                                color = Primary.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "📋 Template",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Primary
                                )
                            }
                        }
                        // Urgency Badge
                        Surface(
                            color = urgencyColor.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = penugasan.urgency?.replaceFirstChar { it.uppercase() } ?: "-",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = urgencyColor
                            )
                        }
                        // Status Badge
                        Surface(
                            color = statusColor.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = penugasan.status.replaceFirstChar { it.uppercase() },
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                        }
                    }
                }

                // Delete Button — sembunyikan untuk template
                if (penugasan.isTemplate != true) {
                    IconButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier
                            .size(32.dp)
                            .background(Error.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Hapus",
                            tint = Error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // ── Deskripsi ──
            if (!penugasan.deskripsi.isNullOrBlank()) {
                Surface(
                    color = Neutral.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = penugasan.deskripsi,
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurface.copy(alpha = 0.8f),
                        modifier = Modifier.padding(12.dp),
                        lineHeight = 16.sp
                    )
                }
            }

            HorizontalDivider(color = Neutral, thickness = 1.dp)

            // ── Metadata Row (Grid-like) ──
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Baris PJ & Divisi
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (penugasan.penanggungJawab != null) {
                        MetadataItem(
                            icon = Icons.Default.Person,
                            text = penugasan.penanggungJawab.namaLengkap,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (penugasan.divisi != null) {
                        MetadataItem(
                            icon = Icons.Default.Groups,
                            text = penugasan.divisi.namaDivisi,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                // Baris Tenggat
                if (!penugasan.tenggat.isNullOrBlank()) {
                    MetadataItem(
                        icon = Icons.AutoMirrored.Filled.EventNote,
                        text = "Tenggat: ${DateTimeUtil.formatIsoToLocal(penugasan.tenggat)}",
                        iconColor = Primary
                    )
                }
            }
        }
    }

    // ── Delete Confirmation Dialog ──
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = Surface,
            titleContentColor = Secondary,
            textContentColor = OnSurfaceVariant,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.Warning, null, tint = Error)
                    Text("Hapus Tugas", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text("Apakah Anda yakin ingin menghapus tugas \"${penugasan.judul}\"? Tindakan ini tidak dapat dibatalkan.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Error),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Hapus", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Batal", color = OnSurfaceVariant)
                }
            }
        )
    }
}

@Composable
private fun MetadataItem(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
    iconColor: Color = OnSurfaceVariant
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = Secondary.copy(alpha = 0.8f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
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
        containerColor = Surface,
        titleContentColor = Secondary,
        textContentColor = OnSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier.size(36.dp).clip(CircleShape).background(Primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.EditCalendar, null, tint = Primary, modifier = Modifier.size(20.dp))
                }
                Text("Buat Tugas Baru", fontWeight = FontWeight.ExtraBold)
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                // Judul
                OutlinedTextField(
                    value = uiState.formJudul,
                    onValueChange = onJudulChange,
                    label = { Text("Judul Tugas *", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = NeutralBorder,
                        focusedLabelColor = Primary
                    )
                )

                // Deskripsi
                OutlinedTextField(
                    value = uiState.formDeskripsi,
                    onValueChange = onDeskripsiChange,
                    label = { Text("Deskripsi", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    minLines = 2,
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = NeutralBorder,
                        focusedLabelColor = Primary
                    )
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
                        label = { Text("Penanggung Jawab *", fontWeight = FontWeight.Bold) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedKaryawan) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = NeutralBorder,
                            focusedLabelColor = Primary
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expandedKaryawan,
                        onDismissRequest = { expandedKaryawan = false },
                        modifier = Modifier.background(Surface)
                    ) {
                        uiState.karyawans.forEach { karyawan ->
                            DropdownMenuItem(
                                text = { Text(karyawan.namaLengkap, color = OnSurface) },
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
                        label = { Text("Divisi", fontWeight = FontWeight.Bold) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDivisi) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = NeutralBorder,
                            focusedLabelColor = Primary
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expandedDivisi,
                        onDismissRequest = { expandedDivisi = false },
                        modifier = Modifier.background(Surface)
                    ) {
                        DropdownMenuItem(
                            text = { Text("— Tidak Ada —", color = OnSurfaceVariant) },
                            onClick = {
                                onDivisiChange(null)
                                expandedDivisi = false
                            }
                        )
                        uiState.divisis.forEach { divisi ->
                            DropdownMenuItem(
                                text = { Text(divisi.namaDivisi, color = OnSurface) },
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
                    label = { Text("Tenggat (YYYY-MM-DD)", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    placeholder = { Text("Contoh: 2026-12-31") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = NeutralBorder,
                        focusedLabelColor = Primary
                    ),
                    trailingIcon = { Icon(Icons.Default.CalendarToday, null, tint = Primary, modifier = Modifier.size(20.dp)) }
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
                        label = { Text("Prioritas", fontWeight = FontWeight.Bold) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedUrgency) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = NeutralBorder,
                            focusedLabelColor = Primary
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expandedUrgency,
                        onDismissRequest = { expandedUrgency = false },
                        modifier = Modifier.background(Surface)
                    ) {
                        urgencyOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.replaceFirstChar { it.uppercase() }, color = OnSurface) },
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
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.height(48.dp).fillMaxWidth(0.5f),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                if (uiState.isCreating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Buat Tugas", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.height(48.dp)
            ) {
                Text("Batal", color = OnSurfaceVariant, fontWeight = FontWeight.SemiBold)
            }
        }
    )
}
