package com.example.codasuaka.ui.screen.jam_operasional

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codasuaka.ui.components.CodaSuakaSnackbarHost
import com.example.codasuaka.ui.theme.*

/**
 * Hari dalam seminggu dengan label Indonesia.
 * Menggunakan nilai Calendar.DAY_OF_WEEK: 1=Minggu, 2=Senin, ..., 7=Sabtu.
 */
private data class HariInfo(
    val value: Int,
    val label: String,
    val singkat: String
)

private val daftarHari = listOf(
    HariInfo(1, "Minggu", "Min"),
    HariInfo(2, "Senin", "Sen"),
    HariInfo(3, "Selasa", "Sel"),
    HariInfo(4, "Rabu", "Rab"),
    HariInfo(5, "Kamis", "Kam"),
    HariInfo(6, "Jumat", "Jum"),
    HariInfo(7, "Sabtu", "Sab")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JamOperasionalScreen(
    onBack: () -> Unit,
    viewModel: JamOperasionalViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Tampilkan snackbar untuk pesan sukses/error
    LaunchedEffect(uiState.successMessage, uiState.errorMessage) {
        val message = uiState.successMessage ?: uiState.errorMessage
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Jam Operasional",
                        fontWeight = FontWeight.Bold,
                        color = Secondary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Secondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Surface
                )
            )
        },
        snackbarHost = { CodaSuakaSnackbarHost(hostState = snackbarHostState) },
        containerColor = Tertiary
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ... (rest of the cards)
                // (I will do a shorter target/replacement to avoid long content errors)
                    // ── Header Info ──
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Primary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AccessTime, null, tint = Primary, modifier = Modifier.size(26.dp))
                            }
                            Column {
                                Text(
                                    text = uiState.namaInstansi,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Secondary
                                )
                                Text(
                                    text = "Pengaturan waktu operasional toko",
                                    fontSize = 13.sp,
                                    color = OnSurfaceVariant
                                )
                            }
                        }
                    }

                    // ── Jam Buka & Jam Tutup ──
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Neutral)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text(
                                text = "Waktu Operasional",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Secondary
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Jam Buka
                                TimePickerField(
                                    modifier = Modifier.weight(1f),
                                    label = "Jam Buka",
                                    value = uiState.jamBuka,
                                    onValueChange = { viewModel.updateJamBuka(it) }
                                )

                                // Jam Tutup
                                TimePickerField(
                                    modifier = Modifier.weight(1f),
                                    label = "Jam Tutup",
                                    value = uiState.jamTutup,
                                    onValueChange = { viewModel.updateJamTutup(it) }
                                )
                            }
                        }
                    }

                    // ── Hari Operasional ──
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Neutral)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text(
                                text = "Hari Aktif",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Secondary
                            )
                            Text(
                                text = "Tentukan hari kerja instansi",
                                fontSize = 12.sp,
                                color = OnSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(20.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                daftarHari.forEach { hari ->
                                    HariChip(
                                        modifier = Modifier.weight(1f),
                                        hari = hari,
                                        isSelected = hari.value in uiState.hariOperasional,
                                        onClick = { viewModel.toggleHari(hari.value) }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // ── Tombol Simpan ──
                    Button(
                        onClick = { viewModel.saveJamOperasional() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary,
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                        enabled = !uiState.isSaving
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 3.dp
                            )
                        } else {
                            Icon(
                                Icons.Default.Save,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                "Simpan Pengaturan",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }


// ─── Time Picker Field ────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerField(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    var showTimePicker by remember { mutableStateOf(false) }
    val parts = value.split(":")
    val hour = parts.getOrNull(0)?.toIntOrNull() ?: 8
    val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0

    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = OnSurfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))

        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showTimePicker = true },
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.outlinedCardColors(
                containerColor = InputBackground
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = value,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = OnSurface
                )
            }
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = hour,
            initialMinute = minute,
            is24Hour = true
        )

        MaterialTheme(
            colorScheme = lightColorScheme(
                surface = Color.White,
                onSurface = OnSurface,
                primary = Primary,
                secondary = Secondary
            )
        ) {
            AlertDialog(
                onDismissRequest = { showTimePicker = false },
                title = {
                    Text(
                        text = label,
                        fontWeight = FontWeight.Bold,
                        color = Secondary
                    )
                },
                text = {
                    TimeInput(state = timePickerState)
                },
                confirmButton = {
                    TextButton(onClick = {
                        val h = timePickerState.hour.toString().padStart(2, '0')
                        val m = timePickerState.minute.toString().padStart(2, '0')
                        onValueChange("$h:$m")
                        showTimePicker = false
                    }) {
                        Text("OK", fontWeight = FontWeight.Bold, color = Primary)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showTimePicker = false }) {
                        Text("Batal", color = OnSurfaceVariant)
                    }
                },
                containerColor = Color.White
            )
        }
    }
}

// ─── Hari Chip ────────────────────────────────────────────────

@Composable
private fun HariChip(
    modifier: Modifier = Modifier,
    hari: HariInfo,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) Primary else Neutral.copy(alpha = 0.5f)
    val textColor = if (isSelected) Color.White else Secondary
    val borderColor = if (isSelected) Primary else Color.Transparent

    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = hari.singkat,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
            color = textColor,
            textAlign = TextAlign.Center
        )
    }
}
