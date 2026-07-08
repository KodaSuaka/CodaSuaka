package com.example.codasuaka.ui.screen.laporan_keuangan

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Placeholder screen for Laporan Keuangan.
 * ISSUE #6 (FIX): This screen was previously an undefined route causing crashes at runtime.
 * Implement full financial reporting features here.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaporanKeuanganScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Laporan Keuangan") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Fitur Laporan Keuangan akan sehadir",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
