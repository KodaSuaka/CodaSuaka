package com.example.codasuaka.ui.screen.penugasan

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codasuaka.data.remote.dto.DivisiDto
import com.example.codasuaka.data.remote.dto.KaryawanDto
import com.example.codasuaka.data.remote.dto.PenugasanDto
import com.example.codasuaka.ui.components.CustomCalendarNavigation
import com.example.codasuaka.ui.components.NotificationBannerStatic
import com.example.codasuaka.ui.components.YearPickerDialog
import com.example.codasuaka.ui.theme.*
import com.example.codasuaka.util.DateTimeUtil
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

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
    var showDeleteConfirmByDetail by remember { mutableStateOf<Int?>(null) }

    // Muat ulang setiap kali layar ini kembali terlihat
    com.example.codasuaka.util.OnResumeEffect { viewModel.loadData() }

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
                if (uiState.canManagePenugasan) {
                    FloatingActionButton(
                        onClick = { viewModel.showCreateDialog() },
                        containerColor = Primary,
                        contentColor = Color.White,
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Tambah Tugas")
                    }
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

                // ── Animated Content Body ──
                AnimatedContent(
                    targetState = uiState.isLoading,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(400)) togetherWith fadeOut(animationSpec = tween(400))
                    },
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    label = "penugasanBody"
                ) { isLoading ->
                    if (isLoading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Primary)
                        }
                    } else if (uiState.penugasans.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            itemsIndexed(uiState.penugasans, key = { _, item -> item.id }) { index, penugasan ->
                                // Simplified Staggered Animation
                                var visible by remember(penugasan.id) { mutableStateOf(false) }
                                LaunchedEffect(penugasan.id) { visible = true }
                                
                                AnimatedVisibility(
                                    visible = visible,
                                    enter = slideInVertically(initialOffsetY = { 30 }) + fadeIn(),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    PenugasanCard(
                                        penugasan = penugasan,
                                        onCardClick = { viewModel.showPenugasanDetail(penugasan) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── Create/Edit Dialog ──
        if (uiState.showCreateDialog) {
            AssignmentFormDialog(
                uiState = uiState,
                templates = viewModel.templates,
                onTemplateSelected = { viewModel.applyTemplate(it) },
                onDismiss = { viewModel.dismissCreateDialog() },
                onJudulChange = { viewModel.updateFormJudul(it) },
                onDeskripsiChange = { viewModel.updateFormDeskripsi(it) },
                onPenanggungJawabChange = { viewModel.updateFormPenanggungJawabId(it) },
                onDivisiChange = { viewModel.updateFormDivisiId(it) },
                onTenggatChange = { viewModel.updateFormTenggat(it) },
                onUrgencyChange = { viewModel.updateFormUrgency(it) },
                onSave = { viewModel.createOrUpdatePenugasan() }
            )
        }

        // ── Detail Screen Overlay ──
        if (uiState.showDetail && uiState.selectedPenugasan != null) {
            PenugasanDetailScreen(
                penugasan = uiState.selectedPenugasan!!,
                onBack = { viewModel.hidePenugasanDetail() },
                onAccept = { viewModel.acceptPenugasan(uiState.selectedPenugasan!!.id) },
                onComplete = { viewModel.completePenugasan(uiState.selectedPenugasan!!.id) },
                onValidasi = { disetujui -> viewModel.validasiPenugasan(uiState.selectedPenugasan!!.id, disetujui) },
                onEdit = { 
                    viewModel.showEditDialog(uiState.selectedPenugasan!!)
                    viewModel.hidePenugasanDetail()
                },
                onDelete = { 
                    showDeleteConfirmByDetail = uiState.selectedPenugasan!!.id
                    viewModel.hidePenugasanDetail()
                },
                canManage = uiState.canManagePenugasan,
                isAssigned = viewModel.isAssignedTo(uiState.selectedPenugasan!!),
                isProcessing = uiState.isProcessing
            )
        }

        // ── Delete Confirmation for Detail Popup ──
        if (showDeleteConfirmByDetail != null) {
            val taskToDelete = uiState.penugasans.find { it.id == showDeleteConfirmByDetail }
            if (taskToDelete != null) {
                DeleteTaskDialog(
                    taskTitle = taskToDelete.judul,
                    onDismiss = { showDeleteConfirmByDetail = null },
                    onConfirm = {
                        viewModel.deletePenugasan(taskToDelete.id)
                        showDeleteConfirmByDetail = null
                    }
                )
            }
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
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 4.dp, vertical = 12.dp)
    ) {
        val isSemuaSelected = selectedStatus == null
        FilterChip(
            selected = isSemuaSelected,
            onClick = { onStatusSelected(null) },
            label = { 
                Text(
                    text = "Semua", 
                    fontSize = 13.sp, 
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isSemuaSelected) Color.White else Secondary
                ) 
            },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = Primary,
                containerColor = Neutral.copy(alpha = 0.8f)
            ),
            border = null
        )

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
                        color = if (isSelected) Color.White else Secondary
                    ) 
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = selectedColor,
                    containerColor = Neutral.copy(alpha = 0.8f)
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
    onCardClick: () -> Unit = {}
) {
    val urgencyColor = when (penugasan.urgency) {
        "urgent" -> UrgentColor
        "sedang" -> SedangColor
        else -> RendahColor
    }

    val statusColor = when (penugasan.status) {
        "belum" -> StatusBelum
        "proses" -> StatusProses
        "menunggu_validasi" -> SedangColor
        "selesai" -> StatusSelesai
        else -> StatusBelum
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
        shape = RoundedCornerShape(24.dp), // More rounded
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Neutral)
    ) {
        Column(
            modifier = Modifier.padding(24.dp), // Increased padding
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Header: Judul & Badges ──
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = penugasan.judul,
                    style = MaterialTheme.typography.titleLarge, // Slightly larger
                    fontWeight = FontWeight.ExtraBold,
                    color = Secondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 26.sp
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (penugasan.isTemplate == true) {
                        BadgeSurface(text = "📋 Template", color = Primary)
                    }
                    BadgeSurface(
                        text = penugasan.urgency?.replaceFirstChar { it.uppercase() } ?: "-",
                        color = urgencyColor
                    )
                    BadgeSurface(
                        text = penugasan.status.replace('_', ' ').replaceFirstChar { it.uppercase() },
                        color = statusColor
                    )
                }
            }

            // ── Deskripsi ──
            if (!penugasan.deskripsi.isNullOrBlank()) {
                Text(
                    text = penugasan.deskripsi,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Secondary.copy(alpha = 0.7f),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 22.sp
                )
            }

            HorizontalDivider(color = Neutral, thickness = 1.dp)

            // ── Metadata Row ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // PJ & Divisi Group
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (penugasan.penanggungJawab != null) {
                        MetadataItem(
                            icon = Icons.Default.Person,
                            text = penugasan.penanggungJawab.namaLengkap
                        )
                    }
                    if (penugasan.divisi != null) {
                        MetadataItem(
                            icon = Icons.Default.Groups,
                            text = penugasan.divisi.namaDivisi
                        )
                    }
                }
                
                // Tenggat
                if (!penugasan.tenggat.isNullOrBlank()) {
                    MetadataItem(
                        icon = Icons.AutoMirrored.Filled.EventNote,
                        text = DateTimeUtil.formatIsoToLocal(penugasan.tenggat),
                        iconColor = Primary
                    )
                }
            }
        }
    }
}

@Composable
private fun BadgeSurface(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color
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
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = 120.dp) // Prevent metadata from pushing others
        )
    }
}

// ─── Delete Confirmation Dialog ───

@Composable
private fun DeleteTaskDialog(
    taskTitle: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Default.Warning, null, tint = Coral)
                Text("Hapus Tugas", fontWeight = FontWeight.ExtraBold, color = Secondary)
            }
        },
        text = {
            Text("Yakin ingin menghapus tugas \"$taskTitle\"? Tindakan ini permanen.")
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Coral),
                shape = RoundedCornerShape(12.dp)
            ) {
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

// ─── Create/Edit Dialog ────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AssignmentFormDialog(
    uiState: PenugasanUiState,
    templates: List<com.example.codasuaka.data.remote.dto.PenugasanDto> = emptyList(),
    onTemplateSelected: (com.example.codasuaka.data.remote.dto.PenugasanDto) -> Unit = {},
    onDismiss: () -> Unit,
    onJudulChange: (String) -> Unit,
    onDeskripsiChange: (String) -> Unit,
    onPenanggungJawabChange: (String) -> Unit,
    onDivisiChange: (Int?) -> Unit,
    onTenggatChange: (String) -> Unit,
    onUrgencyChange: (String) -> Unit,
    onSave: () -> Unit
) {
    var expandedKaryawan by remember { mutableStateOf(false) }
    var expandedDivisi by remember { mutableStateOf(false) }
    var expandedUrgency by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showYearPicker by remember { mutableStateOf(false) }

    val urgencyOptions = listOf("rendah", "sedang", "urgent")
    val titleText = if (uiState.isEditing) "Edit Tugas" else "Buat Tugas Baru"
    val buttonText = if (uiState.isEditing) "Simpan" else "Buat Tugas"

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
                    Icon(
                        imageVector = if (uiState.isEditing) Icons.Default.EditNote else Icons.Default.EditCalendar,
                        contentDescription = null, 
                        tint = Primary, 
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(titleText, fontWeight = FontWeight.ExtraBold)
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                if (templates.isNotEmpty()) {
                    Column {
                        Text(
                            "Mulai dari template (opsional)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Secondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                        ) {
                            templates.forEach { template ->
                                AssistChip(
                                    onClick = { onTemplateSelected(template) },
                                    label = { 
                                        Text(
                                            template.judul,
                                            color = Secondary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        ) 
                                    },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = Primary.copy(alpha = 0.1f),
                                        labelColor = Secondary
                                    ),
                                    border = AssistChipDefaults.assistChipBorder(
                                        enabled = true,
                                        borderColor = Primary.copy(alpha = 0.2f),
                                        borderWidth = 1.dp
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }
                    }
                }

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

                OutlinedTextField(
                    value = uiState.formTenggat,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tenggat (YYYY-MM-DD)", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    enabled = false,
                    placeholder = { Text("Contoh: 2026-12-31") },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = NeutralBorder,
                        disabledLabelColor = Secondary,
                        disabledTextColor = OnSurface,
                        disabledTrailingIconColor = Primary
                    ),
                    trailingIcon = { Icon(Icons.Default.CalendarToday, null, tint = Primary, modifier = Modifier.size(20.dp)) }
                )

                if (showDatePicker) {
                    val datePickerState = rememberDatePickerState()
                    val locale = remember { Locale("id", "ID") }
                    val formatter = remember { DateTimeFormatter.ofPattern("MMMM yyyy", locale) }
                    
                    MaterialTheme(colorScheme = lightColorScheme(
                        surface = Color.White,
                        onSurface = Color.Black,
                        primary = Primary,
                        onPrimary = Color.White,
                        secondary = Secondary
                    )) {
                        DatePickerDialog(
                            onDismissRequest = { showDatePicker = false },
                            confirmButton = {
                                TextButton(onClick = {
                                    datePickerState.selectedDateMillis?.let {
                                        val ld = Instant.ofEpochMilli(it)
                                            .atZone(ZoneId.of("UTC"))
                                            .toLocalDate()
                                        onTenggatChange(ld.toString())
                                    }
                                    showDatePicker = false
                                }) { Text("Pilih", color = Primary, fontWeight = FontWeight.Bold) }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDatePicker = false }) {
                                    Text("Batal", color = OnSurfaceVariant)
                                }
                            },
                            colors = DatePickerDefaults.colors(containerColor = Color.White)
                        ) {
                            if (showYearPicker) {
                                val displayMonth = Instant.ofEpochMilli(datePickerState.displayedMonthMillis)
                                    .atZone(ZoneId.of("UTC"))
                                    .toLocalDate()
                                    
                                YearPickerDialog(
                                    selectedYear = displayMonth.year,
                                    onYearSelected = { year ->
                                        val tz = java.util.TimeZone.getTimeZone("UTC")
                                        val cal = java.util.Calendar.getInstance(tz).apply {
                                            timeInMillis = datePickerState.displayedMonthMillis
                                            set(java.util.Calendar.YEAR, year)
                                        }
                                        datePickerState.displayedMonthMillis = cal.timeInMillis
                                        
                                        val selCal = java.util.Calendar.getInstance(tz).apply {
                                            timeInMillis = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
                                            set(java.util.Calendar.YEAR, year)
                                        }
                                        datePickerState.selectedDateMillis = selCal.timeInMillis
                                        
                                        showYearPicker = false
                                    },
                                    onDismiss = { showYearPicker = false }
                                )
                            }

                            Column(modifier = Modifier.padding(top = 16.dp)) {
                                val displayMonth = Instant.ofEpochMilli(datePickerState.displayedMonthMillis)
                                    .atZone(ZoneId.of("UTC"))
                                    .toLocalDate()
                                
                                val monthTitle = remember(displayMonth) { displayMonth.format(formatter) }
                                
                                CustomCalendarNavigation(
                                    title = monthTitle.replaceFirstChar { it.uppercase() },
                                    onPrevClick = {
                                        val cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
                                            timeInMillis = datePickerState.displayedMonthMillis
                                            add(java.util.Calendar.MONTH, -1)
                                        }
                                        datePickerState.displayedMonthMillis = cal.timeInMillis
                                    },
                                    onNextClick = {
                                        val cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
                                            timeInMillis = datePickerState.displayedMonthMillis
                                            add(java.util.Calendar.MONTH, 1)
                                        }
                                        datePickerState.displayedMonthMillis = cal.timeInMillis
                                    },
                                    onTitleClick = { showYearPicker = true },
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(340.dp)
                                        .clipToBounds()
                                ) {
                                    DatePicker(
                                        state = datePickerState,
                                        title = null,
                                        headline = null,
                                        showModeToggle = false,
                                        colors = DatePickerDefaults.colors(
                                            containerColor = Color.White,
                                            titleContentColor = Secondary,
                                            headlineContentColor = Secondary,
                                            weekdayContentColor = Secondary.copy(alpha = 0.6f),
                                            subheadContentColor = Secondary.copy(alpha = 0.6f),
                                            yearContentColor = Secondary.copy(alpha = 0.7f),
                                            currentYearContentColor = Primary,
                                            selectedYearContentColor = Color.White,
                                            selectedYearContainerColor = Primary,
                                            dayContentColor = OnSurface,
                                            selectedDayContentColor = Color.White,
                                            selectedDayContainerColor = Primary,
                                            todayContentColor = Secondary,
                                            todayDateBorderColor = Primary
                                        ),
                                        modifier = Modifier.offset(y = (-48).dp)
                                    )
                                }
                            }
                        }
                    }
                }

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
                onClick = onSave,
                enabled = !uiState.isCreating,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.height(48.dp).fillMaxWidth(0.6f),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                if (uiState.isCreating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(buttonText, color = Color.White, fontWeight = FontWeight.Bold)
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
