package com.example.codasuaka.ui.screen.receipt_settings

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codasuaka.ui.theme.*
import com.example.codasuaka.ui.screen.components.CustomTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptSettingsScreen(
    onBack: () -> Unit,
    viewModel: ReceiptSettingsViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            snackbarHostState.showSnackbar("Pengaturan struk berhasil disimpan")
            viewModel.clearSavedSignal()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Pengaturan Struk", fontWeight = FontWeight.Bold, color = Secondary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali", tint = Secondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Tertiary)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Card Informasi ──
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Primary.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.Info, null, tint = Primary)
                    Text(
                        "Ubah tampilan teks yang akan muncul pada struk Bluetooth Anda.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Secondary
                    )
                }
            }

            // ── Form Header ──
            Text("Bagian Atas Struk", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Secondary)
            
            CustomTextField(
                value = uiState.header,
                onValueChange = viewModel::onHeaderChange,
                label = "Nama Toko (Header)",
                placeholder = "Contoh: CODA SUAKA",
                modifier = Modifier.fillMaxWidth()
            )

            CustomTextField(
                value = uiState.tagline,
                onValueChange = viewModel::onTaglineChange,
                label = "Slogan (Tagline)",
                placeholder = "Contoh: Penyegar Dahaga & Jiwa",
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider(color = Neutral)

            // ── Form Footer ──
            Text("Bagian Bawah Struk", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Secondary)

            CustomTextField(
                value = uiState.footer1,
                onValueChange = viewModel::onFooter1Change,
                label = "Pesan Penutup 1",
                placeholder = "Contoh: Terima Kasih",
                modifier = Modifier.fillMaxWidth()
            )

            CustomTextField(
                value = uiState.footer2,
                onValueChange = viewModel::onFooter2Change,
                label = "Pesan Penutup 2",
                placeholder = "Contoh: Selamat Menikmati!",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── Aksi ──
            Button(
                onClick = { viewModel.saveSettings() },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Icon(Icons.Default.Save, null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Simpan Pengaturan", fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = { viewModel.resetToDefault() },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Error),
                border = androidx.compose.foundation.BorderStroke(1.dp, Error.copy(alpha = 0.5f))
            ) {
                Icon(Icons.Default.Refresh, null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reset ke Default")
            }
        }
    }
}
