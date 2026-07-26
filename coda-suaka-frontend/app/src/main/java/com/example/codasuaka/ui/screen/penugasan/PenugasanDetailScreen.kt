package com.example.codasuaka.ui.screen.penugasan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codasuaka.data.remote.dto.PenugasanDto
import com.example.codasuaka.ui.theme.*
import com.example.codasuaka.util.DateTimeUtil

// ─── Colors (sama dengan PenugasanScreen) ─────────────────
private val UrgentColor = Color(0xFFEF4444)
private val SedangColor = Color(0xFFF59E0B)
private val RendahColor = Color(0xFF10B981)
private val StatusBelum = Color(0xFF6B7280)
private val StatusProses = Color(0xFF3B82F6)
private val StatusSelesai = Color(0xFF10B981)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PenugasanDetailScreen(
    penugasan: PenugasanDto,
    onBack: () -> Unit,
    onAccept: () -> Unit,
    onComplete: () -> Unit,
    canManage: Boolean = false,
    isAssigned: Boolean = false,
    isProcessing: Boolean = false
) {
    val urgencyColor = when (penugasan.urgency) {
        "urgent" -> UrgentColor
        "sedang" -> SedangColor
        "rendah" -> RendahColor
        else -> SedangColor
    }
    val statusColor = when (penugasan.status) {
        "belum" -> StatusBelum
        "proses" -> StatusProses
        "selesai" -> StatusSelesai
        else -> StatusBelum
    }
    val statusLabel = when (penugasan.status) {
        "belum" -> "Belum Dikerjakan"
        "proses" -> "Sedang Dikerjakan"
        "selesai" -> "Selesai"
        else -> penugasan.status
    }
    val urgencyLabel = when (penugasan.urgency) {
        "urgent" -> "Urgent"
        "sedang" -> "Sedang"
        "rendah" -> "Rendah"
        else -> penugasan.urgency ?: "-"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Tugas") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Header: Judul + Status Badge ──
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Judul
                    Text(
                        text = penugasan.judul,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    // Badges: Urgency + Status
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Urgency badge
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = urgencyColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = urgencyLabel,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                color = urgencyColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        // Status badge
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = statusColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = statusLabel,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                color = statusColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        // Poin badge
                        if (penugasan.poin != null && penugasan.poin > 0) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0xFF8B5CF6).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "${penugasan.poin} Poin",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    color = Color(0xFF8B5CF6),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // ── Deskripsi ──
            if (!penugasan.deskripsi.isNullOrBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Deskripsi",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = penugasan.deskripsi,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // ── Info Detail ──
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Informasi Tugas",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Penanggung Jawab
                    DetailInfoRow(
                        icon = Icons.Default.Person,
                        label = "Penanggung Jawab",
                        value = penugasan.penanggungJawab?.namaLengkap ?: "-"
                    )

                    // Divisi
                    DetailInfoRow(
                        icon = Icons.Default.Business,
                        label = "Divisi",
                        value = penugasan.divisi?.namaDivisi ?: "-"
                    )

                    // Tenggat
                    DetailInfoRow(
                        icon = Icons.Default.Event,
                        label = "Tenggat",
                        value = if (!penugasan.tenggat.isNullOrBlank()) {
                            DateTimeUtil.formatDateDisplay(penugasan.tenggat)
                        } else "-"
                    )

                    // Dibuat Oleh
                    DetailInfoRow(
                        icon = Icons.Default.Create,
                        label = "Dibuat Oleh",
                        value = penugasan.pembuat?.name ?: "-"
                    )

                    // Dibuat Pada
                    DetailInfoRow(
                        icon = Icons.Default.AccessTime,
                        label = "Dibuat Pada",
                        value = if (!penugasan.createdAt.isNullOrBlank()) {
                            DateTimeUtil.formatDateTimeDisplay(penugasan.createdAt)
                        } else "-"
                    )

                    // Waktu Dikerjakan
                    if (!penugasan.acceptedAt.isNullOrBlank()) {
                        DetailInfoRow(
                            icon = Icons.Default.PlayArrow,
                            label = "Mulai Dikerjakan",
                            value = DateTimeUtil.formatDateTimeDisplay(penugasan.acceptedAt)
                        )
                    }

                    // Waktu Selesai
                    if (!penugasan.completedAt.isNullOrBlank()) {
                        DetailInfoRow(
                            icon = Icons.Default.CheckCircle,
                            label = "Selesai Pada",
                            value = DateTimeUtil.formatDateTimeDisplay(penugasan.completedAt)
                        )
                    }
                }
            }

            // ── Action Buttons untuk Karyawan ──
            if (isAssigned && !canManage) {
                when (penugasan.status) {
                    "belum" -> {
                        Button(
                            onClick = onAccept,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isProcessing,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF3B82F6)
                            )
                        ) {
                            if (isProcessing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Terima & Mulai Kerjakan")
                        }
                    }
                    "proses" -> {
                        Button(
                            onClick = onComplete,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isProcessing,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF10B981)
                            )
                        ) {
                            if (isProcessing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Tandai Selesai")
                        }
                    }
                    "selesai" -> {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Tugas sudah selesai",
                                    color = Color(0xFF10B981),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun DetailInfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
