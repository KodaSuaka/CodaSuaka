package com.example.codasuaka.ui.screen.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codasuaka.ui.theme.*

// ─── Data class menu items ───────────────────────────────────

private data class MenuItem(
    val label: String,
    val icon: ImageVector,
    val color: androidx.compose.ui.graphics.Color = Primary
)

// ─── DashboardScreen ─────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateTo: (String) -> Unit,
    onLogout: () -> Unit,
    viewModel: DashboardViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    // ── Tangani drawer via ViewModel ──
    LaunchedEffect(uiState.isDrawerOpen) {
        if (uiState.isDrawerOpen) drawerState.open() else drawerState.close()
    }
    LaunchedEffect(drawerState.isClosed) {
        if (drawerState.isClosed) viewModel.closeDrawer()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                viewModel = viewModel,
                onNavigateTo = onNavigateTo,
                onLogout = onLogout
            )
        },
        gesturesEnabled = uiState.isDrawerOpen
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = uiState.outletName.ifEmpty { "Dashboard" },
                            fontWeight = FontWeight.SemiBold,
                            color = OnPrimary
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.toggleDrawer() }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = OnPrimary
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { /* notifikasi */ }) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifikasi",
                                tint = OnPrimary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Primary
                    )
                )
            },
            bottomBar = {
                BottomNavigationBar(
                    selectedIndex = uiState.selectedBottomNav,
                    onItemSelected = { index ->
                        viewModel.onBottomNavSelected(index)
                        // Navigate based on selection
                        when (index) {
                            0 -> { /* already on dashboard */ }
                            1 -> onNavigateTo("contact_list")
                            2 -> onNavigateTo("contact_list")
                            3 -> onNavigateTo("divisi")
                        }
                    }
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
                // ── Loading Indicator ──
                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Primary)
                    }
                }

                // ── Error Message ──
                if (uiState.errorMessage != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Error.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = uiState.errorMessage ?: "",
                            color = Error,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // ══════════════════════════════════════════════
                // SECTION ATAS — Omset
                // ══════════════════════════════════════════════
                SectionOmset(
                    omsetTotal = uiState.omsetTotal,
                    onCariOmset = { startDate, endDate ->
                        viewModel.loadOmset(startDate, endDate)
                    }
                )

                // ══════════════════════════════════════════════
                // SECTION TENGAH — Kelola Outlet & Log Absensi
                // ══════════════════════════════════════════════
                SectionMenuGrid(
                    title = "Menu Utama",
                    items = listOf(
                        MenuItem("Kelola Outlet", Icons.Default.Store, Primary),
                        MenuItem("Jadwal", Icons.Default.CalendarMonth, Primary),
                        MenuItem("Log Absensi", Icons.Default.FactCheck, Secondary)
                    ),
                    onItemClick = { label ->
                        when (label) {
                            "Kelola Outlet" -> onNavigateTo("kelola_outlet")
                            "Jadwal" -> onNavigateTo("kalender")
                            "Log Absensi" -> onNavigateTo("log_absensi")
                        }
                    }
                )

                // ══════════════════════════════════════════════
                // SECTION BAWAH — Laporan Keuangan & Status Karyawan
                // ══════════════════════════════════════════════
                SectionMenuGrid(
                    title = "Laporan & Status",
                    items = listOf(
                        MenuItem("Laporan Keuangan", Icons.Default.AccountBalance, Primary),
                        MenuItem("Status Karyawan", Icons.Default.PeopleAlt, Secondary)
                    ),
                    onItemClick = { label ->
                        when (label) {
                            "Laporan Keuangan" -> onNavigateTo("laporan_keuangan")
                            "Status Karyawan" -> onNavigateTo("status_karyawan")
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

// ─── Section Omset ──────────────────────────────────────────

@Composable
private fun SectionOmset(
    omsetTotal: Long,
    onCariOmset: (startDate: String, endDate: String) -> Unit
) {
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Header ──
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = Success,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = "OMSET",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface
                )
            }

            // ── Nilai Omset ──
            Text(
                text = "Rp ${formatRupiah(omsetTotal)}",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Primary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            HorizontalDivider(color = Neutral, thickness = 1.dp)

            // ── Filter Tanggal ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = startDate,
                    onValueChange = { startDate = it },
                    label = { Text("Tgl Mulai") },
                    placeholder = { Text("yyyy-MM-dd") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    textStyle = MaterialTheme.typography.bodySmall,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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

                OutlinedTextField(
                    value = endDate,
                    onValueChange = { endDate = it },
                    label = { Text("Tgl Akhir") },
                    placeholder = { Text("yyyy-MM-dd") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    textStyle = MaterialTheme.typography.bodySmall,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
            }

            // ── Tombol Cari ──
            Button(
                onClick = { onCariOmset(startDate, endDate) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                    contentColor = OnPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Cari",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

// ─── Section Menu Grid ──────────────────────────────────────

@Composable
private fun SectionMenuGrid(
    title: String,
    items: List<MenuItem>,
    onItemClick: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = OnSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items.forEach { item ->
                MenuCard(
                    modifier = Modifier.weight(1f),
                    item = item,
                    onClick = { onItemClick(item.label) }
                )
            }
        }
    }
}

// ─── Menu Card ──────────────────────────────────────────────

@Composable
private fun MenuCard(
    modifier: Modifier = Modifier,
    item: MenuItem,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(120.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon dengan circle background
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(item.color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.label,
                    tint = item.color,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = OnSurface,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

// ─── Bottom Navigation ──────────────────────────────────────

@Composable
private fun BottomNavigationBar(
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit
) {
    NavigationBar(
        containerColor = Surface,
        tonalElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Item definitions
        val items = listOf(
            Triple(Icons.Default.Home, "Dashboard", 0),
            Triple(Icons.Default.Assignment, "Tugas Tim", 1),
            Triple(Icons.Default.Email, "Pesan", 2),
            Triple(Icons.Default.Groups, "Divisi", 3)
        )

        items.forEach { (icon, label, index) ->
            NavigationBarItem(
                selected = selectedIndex == index,
                onClick = { onItemSelected(index) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label
                    )
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 11.sp
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Primary,
                    selectedTextColor = Primary,
                    unselectedIconColor = OnSurfaceVariant,
                    unselectedTextColor = OnSurfaceVariant,
                    indicatorColor = Primary.copy(alpha = 0.1f)
                )
            )
        }
    }
}

// ─── Drawer Content ─────────────────────────────────────────

@Composable
private fun DrawerContent(
    viewModel: DashboardViewModel,
    onNavigateTo: (String) -> Unit,
    onLogout: () -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier.width(280.dp),
        drawerContainerColor = Surface
    ) {
        // ── Header Drawer ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Primary)
                .padding(24.dp)
        ) {
            Column {
                // Avatar placeholder
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(PrimaryLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = OnPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = (user.namaLengkap ?: "Nama Pengguna"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OnPrimary
                )

                Text(
                    text = (user.email),
                    style = MaterialTheme.typography.bodySmall,
                    color = OnPrimary.copy(alpha = 0.8f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ── Menu Drawer ──
        DrawerItem(
            icon = Icons.Default.Checklist,
            label = "Data Persetujuan",
            onClick = {
                viewModel.closeDrawer()
                onNavigateTo("riwayat_kehadiran")
            }
        )

        DrawerItem(
            icon = Icons.Default.PersonAddAlt,
            label = "Tambah Karyawan",
            onClick = {
                viewModel.closeDrawer()
                onNavigateTo("tambah_karyawan")
            }
        )

        DrawerItem(
            icon = Icons.Default.Schedule,
            label = "Kalender",
            onClick = {
                viewModel.closeDrawer()
                onNavigateTo("kalender")
            }
        )

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = Neutral
        )

        // ── Logout ──
        DrawerItem(
            icon = Icons.Default.Logout,
            label = "Logout",
            iconTint = Error,
            labelColor = Error,
            onClick = {
                viewModel.closeDrawer()
                onLogout()
            }
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ─── Drawer Item ────────────────────────────────────────────

@Composable
private fun DrawerItem(
    icon: ImageVector,
    label: String,
    iconTint: androidx.compose.ui.graphics.Color = OnSurface,
    labelColor: androidx.compose.ui.graphics.Color = OnSurface,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = labelColor
        )
    }
}

// ─── Utility ────────────────────────────────────────────────

/**
 * Memformat angka ke format Rupiah tanpa desimal.
 * Contoh: 15750000 → "15.750.000"
 */
private fun formatRupiah(amount: Long): String {
    val str = amount.toString()
    val sb = StringBuilder()
    var count = 0
    for (i in str.lastIndex downTo 0) {
        if (count > 0 && count % 3 == 0) sb.insert(0, '.')
        sb.insert(0, str[i])
        count++
    }
    return sb.toString()
}
