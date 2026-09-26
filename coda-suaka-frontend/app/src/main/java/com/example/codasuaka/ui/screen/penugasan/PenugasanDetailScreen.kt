package com.example.codasuaka.ui.screen.penugasan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.codasuaka.data.remote.dto.PenugasanDto
import com.example.codasuaka.ui.theme.*
import com.example.codasuaka.util.DateTimeUtil

// ─── Colors ───────────────────────────────────────────────
private val UrgentColor = Color(0xFFEF4444)
private val SedangColor = Color(0xFFF59E0B)
private val RendahColor = Color(0xFF10B981)
private val StatusBelum = Color(0xFF6B7280)
private val StatusProses = Color(0xFF3B82F6)
private val StatusSelesai = Color(0xFF10B981)

@Composable
fun PenugasanDetailScreen(
    penugasan: PenugasanDto,
    onBack: () -> Unit,
    onAccept: () -> Unit,
    onComplete: () -> Unit,
    onValidasi: (disetujui: Boolean) -> Unit = {},
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {},
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
        "menunggu_validasi" -> SedangColor
        "selesai" -> StatusSelesai
        else -> StatusBelum
    }
    val statusLabel = when (penugasan.status) {
        "belum" -> "Belum Dikerjakan"
        "proses" -> "Sedang Dikerjakan"
        "menunggu_validasi" -> "Menunggu Validasi"
        "selesai" -> "Selesai"
        else -> penugasan.status
    }
    val urgencyLabel = when (penugasan.urgency) {
        "urgent" -> "Urgent"
        "sedang" -> "Sedang"
        "rendah" -> "Rendah"
        else -> penugasan.urgency ?: "-"
    }

    Dialog(
        onDismissRequest = onBack,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        MaterialTheme(
            colorScheme = lightColorScheme(
                surface = Color.White,
                onSurface = OnSurface,
                primary = Primary,
                secondary = Secondary
            )
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // ── Header ──
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Detail Tugas",
                                style = MaterialTheme.typography.labelMedium,
                                color = Primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = penugasan.judul,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = Secondary,
                                lineHeight = 26.sp
                            )
                        }
                        
                        // Action Menu (Three Dots) - Simplified header
                        var showMenu by remember { mutableStateOf(false) }
                        
                        Box {
                            IconButton(
                                onClick = { showMenu = true },
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Neutral.copy(alpha = 0.5f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Opsi",
                                    tint = Secondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                                modifier = Modifier.background(Color.White)
                            ) {
                                // Edit & Delete only for Managers/Owner AND if not finished
                                if (canManage && penugasan.isTemplate != true && penugasan.status != "selesai") {
                                    DropdownMenuItem(
                                        text = { Text("Edit Tugas", fontWeight = FontWeight.Medium, color = Secondary) },
                                        onClick = {
                                            showMenu = false
                                            onEdit()
                                        },
                                        leadingIcon = { Icon(Icons.Default.Edit, null, tint = Primary, modifier = Modifier.size(18.dp)) }
                                    )
                                    
                                    DropdownMenuItem(
                                        text = { Text("Hapus Tugas", fontWeight = FontWeight.Medium, color = Coral) },
                                        onClick = {
                                            showMenu = false
                                            onDelete()
                                        },
                                        leadingIcon = { Icon(Icons.Default.Delete, null, tint = Coral, modifier = Modifier.size(18.dp)) }
                                    )
                                    
                                    HorizontalDivider(color = Neutral, modifier = Modifier.padding(vertical = 4.dp))
                                }
                                
                                // Close Action
                                DropdownMenuItem(
                                    text = { Text("Tutup", fontWeight = FontWeight.Medium, color = Secondary) },
                                    onClick = {
                                        showMenu = false
                                        onBack()
                                    },
                                    leadingIcon = { Icon(Icons.Default.Close, null, tint = Secondary, modifier = Modifier.size(18.dp)) }
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = Neutral)

                    // ── Content (Scrollable) ──
                    Column(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            BadgeItem(text = urgencyLabel, color = urgencyColor)
                            BadgeItem(text = statusLabel, color = statusColor)
                            if (penugasan.poin != null && penugasan.poin > 0) {
                                BadgeItem(text = "${penugasan.poin} Poin", color = Color(0xFF8B5CF6))
                            }
                        }

                        if (!penugasan.deskripsi.isNullOrBlank()) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Deskripsi", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = Secondary)
                                Surface(
                                    color = Neutral.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = penugasan.deskripsi,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = OnSurface.copy(alpha = 0.8f),
                                        modifier = Modifier.padding(14.dp),
                                        lineHeight = 20.sp
                                    )
                                }
                            }
                        }

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            DetailItem(Icons.Default.Person, "PJ", penugasan.penanggungJawab?.namaLengkap ?: "-")
                            DetailItem(Icons.Default.Groups, "Divisi", penugasan.divisi?.namaDivisi ?: "-")
                            DetailItem(Icons.Default.Event, "Tenggat", DateTimeUtil.formatIsoToLocal(penugasan.tenggat))
                            DetailItem(Icons.Default.Create, "Pembuat", penugasan.pembuat?.name ?: "-")
                            DetailItem(Icons.Default.AccessTime, "Dibuat", DateTimeUtil.formatIsoToLocal(penugasan.createdAt))
                        }
                    }

                    // ── Actions ──
                    if (!canManage && (isAssigned || penugasan.isTemplate == true)) {
                        Spacer(modifier = Modifier.height(8.dp))
                        when (penugasan.status) {
                            "belum" -> ActionButton("Terima & Mulai Kerja", Icons.Default.PlayArrow, Primary, onAccept, isProcessing)
                            "proses" -> ActionButton("Selesaikan Tugas", Icons.Default.CheckCircle, Success, onComplete, isProcessing)
                            "selesai" -> {
                                Surface(modifier = Modifier.fillMaxWidth(), color = Success.copy(alpha = 0.1f), shape = RoundedCornerShape(14.dp)) {
                                    Text("Tugas Selesai", modifier = Modifier.padding(14.dp), textAlign = TextAlign.Center, color = Success, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    if (canManage && penugasan.status == "menunggu_validasi") {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(onClick = { onValidasi(false) }, modifier = Modifier.weight(1f), enabled = !isProcessing) {
                                Icon(Icons.Default.Close, contentDescription = null); Spacer(modifier = Modifier.width(8.dp)); Text("Kembalikan")
                            }
                            Button(onClick = { onValidasi(true) }, modifier = Modifier.weight(1f), enabled = !isProcessing, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))) {
                                if (isProcessing) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Icon(Icons.Default.CheckCircle, contentDescription = null); Spacer(modifier = Modifier.width(8.dp)); Text("Setujui")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BadgeItem(text: String, color: Color) {
    Surface(color = color.copy(alpha = 0.12f), shape = RoundedCornerShape(8.dp)) {
        Text(text = text, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun DetailItem(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Neutral), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = Secondary.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
        }
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Secondary)
        }
    }
}

@Composable
private fun ActionButton(text: String, icon: ImageVector, color: Color, onClick: () -> Unit, isLoading: Boolean) {
    Button(onClick = onClick, enabled = !isLoading, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = color)) {
        if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 3.dp)
        else { Icon(icon, null, modifier = Modifier.size(20.dp)); Spacer(modifier = Modifier.width(10.dp)); Text(text, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp) }
    }
}
