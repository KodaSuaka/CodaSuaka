package com.example.codasuaka.ui.screen.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codasuaka.ui.components.CustomCalendarNavigation
import com.example.codasuaka.ui.components.YearPickerDialog
import com.example.codasuaka.ui.theme.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.ui.draw.clipToBounds

// ─── Data class menu items ───────────────────────────────────

private data class MenuItem(
    val label: String,
    val icon: ImageVector,
    val color: androidx.compose.ui.graphics.Color = Primary,
    val allowedRoles: List<String> = emptyList() // empty = all roles
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
                uiState = uiState,
                onNavigateTo = onNavigateTo,
                onLogout = onLogout,
                onCloseDrawer = { viewModel.closeDrawer() }
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
                            fontWeight = FontWeight.Bold,
                            color = Secondary
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.toggleDrawer() }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = Secondary
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { /* notifikasi */ }) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifikasi",
                                tint = Secondary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Surface
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
                            1 -> onNavigateTo("riwayat_kehadiran")
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
                    userRole = uiState.userRole,
                    items = listOf(
                        MenuItem("Kelola Outlet", Icons.Default.Store, OrangeManage, allowedRoles = listOf("Owner")),
                        MenuItem("Jadwal", Icons.Default.CalendarMonth, BlueSchedule),
                        MenuItem("Log Absensi", Icons.AutoMirrored.Filled.FactCheck, PurpleLog)
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
                    userRole = uiState.userRole,
                    items = listOf(
                        MenuItem("Laporan Keuangan", Icons.Default.AccountBalance, GreenFinance, allowedRoles = listOf("Owner")),
                        MenuItem("Status Karyawan", Icons.Default.PeopleAlt, TealStatus)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SectionOmset(
    omsetTotal: Double,
    onCariOmset: (startDate: String, endDate: String) -> Unit
) {
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    
    var showDatePicker by remember { mutableStateOf(false) }
    var pickingStartDate by remember { mutableStateOf(true) }
    
    var showYearPicker by remember { mutableStateOf(false) }
    
    val datePickerState = rememberDatePickerState()
    
    val locale = remember { Locale("id", "ID") }
    val formatter = remember { DateTimeFormatter.ofPattern("MMMM yyyy", locale) }

    if (showDatePicker) {
        // Paksa tema terang agar teks terlihat jelas
        MaterialTheme(colorScheme = lightColorScheme(
            surface = Color.White,
            onSurface = Color.Black,
            primary = Primary,
            onPrimary = Color.White,
            secondary = Secondary,
            onSecondary = Color.White
        )) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let {
                            val formattedDate = Instant.ofEpochMilli(it)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                                .format(DateTimeFormatter.ISO_LOCAL_DATE)

                            if (pickingStartDate) startDate = formattedDate else endDate = formattedDate
                        }
                        showDatePicker = false
                    }) { Text("OK", fontWeight = FontWeight.Bold, color = Secondary) }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text("Batal", color = OnSurfaceVariant) }
                },
                colors = DatePickerDefaults.colors(
                    containerColor = Color.White
                )
            ) {
                if (showYearPicker) {
                    val displayMonth = Instant.ofEpochMilli(datePickerState.displayedMonthMillis)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                        
                    YearPickerDialog(
                        selectedYear = displayMonth.year,
                        onYearSelected = { year ->
                            val cal = java.util.Calendar.getInstance().apply {
                                timeInMillis = datePickerState.displayedMonthMillis
                            }
                            cal.set(java.util.Calendar.YEAR, year)
                            datePickerState.displayedMonthMillis = cal.timeInMillis
                            showYearPicker = false
                        },
                        onDismiss = { showYearPicker = false }
                    )
                }

                Column(
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    // ── Header Kustom < Bulan Tahun > ──
                    val displayMonth = Instant.ofEpochMilli(datePickerState.displayedMonthMillis)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                    
                    val monthTitle = remember(displayMonth) { displayMonth.format(formatter) }
                    
                    CustomCalendarNavigation(
                        title = monthTitle.replaceFirstChar { it.uppercase() },
                        onPrevClick = {
                            val cal = java.util.Calendar.getInstance().apply {
                                timeInMillis = datePickerState.displayedMonthMillis
                            }
                            cal.add(java.util.Calendar.MONTH, -1)
                            datePickerState.displayedMonthMillis = cal.timeInMillis
                        },
                        onNextClick = {
                            val cal = java.util.Calendar.getInstance().apply {
                                timeInMillis = datePickerState.displayedMonthMillis
                            }
                            cal.add(java.util.Calendar.MONTH, 1)
                            datePickerState.displayedMonthMillis = cal.timeInMillis
                        },
                        onTitleClick = { showYearPicker = true },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // ── DatePicker (Sembunyikan header asli) ──
                    // Kita gunakan Box dengan clip untuk membuang baris pager asli
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(340.dp) // Sesuaikan tinggi agar pager asli terpotong
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
                                weekdayContentColor = Color.Gray,
                                subheadContentColor = Color.Gray,
                                yearContentColor = Color.DarkGray,
                                currentYearContentColor = Primary,
                                selectedYearContentColor = Color.White,
                                selectedYearContainerColor = Primary,
                                dayContentColor = Color.Black,
                                selectedDayContentColor = Color.White,
                                selectedDayContainerColor = Primary,
                                todayContentColor = Primary,
                                todayDateBorderColor = Primary
                            ),
                            modifier = Modifier.offset(y = (-48).dp) // Geser ke atas untuk sembunyikan pager asli
                        )
                    }
                }
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Neutral)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Header & Trend ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Success.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = null,
                            tint = Success,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "Total Omset",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = OnSurfaceVariant
                    )
                }
                
                Surface(
                    color = Success.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "+5.2%",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Success
                    )
                }
            }

            // ── Nilai Omset ──
            Text(
                text = formatRupiah(omsetTotal),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Secondary,
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider(color = Neutral, thickness = 1.dp)

            // ── Filter Tanggal (Modern Date Picker Trigger) ──
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DatePickerField(
                        label = "Tgl Mulai",
                        value = startDate,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            pickingStartDate = true
                            showDatePicker = true
                        }
                    )
                    DatePickerField(
                        label = "Tgl Akhir",
                        value = endDate,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            pickingStartDate = false
                            showDatePicker = true
                        }
                    )
                }

                Button(
                    onClick = { onCariOmset(startDate, endDate) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary, // Menggunakan #63B3ED
                        contentColor = OnPrimary
                    )
                ) {
                    Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Filter Data", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun DatePickerField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        label = { Text(label, fontSize = 12.sp) },
        placeholder = { Text("Pilih Tanggal", fontSize = 12.sp) },
        readOnly = true,
        enabled = false,
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        textStyle = MaterialTheme.typography.bodySmall,
        trailingIcon = {
            Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(18.dp))
        },
        colors = OutlinedTextFieldDefaults.colors(
            disabledBorderColor = NeutralBorder,
            disabledLabelColor = OnSurfaceVariant,
            disabledTextColor = OnSurface,
            disabledContainerColor = InputBackground,
            disabledTrailingIconColor = Primary
        )
    )
}

// ─── Section Menu Grid ──────────────────────────────────────

@Composable
private fun SectionMenuGrid(
    title: String,
    items: List<MenuItem>,
    userRole: String = "",
    onItemClick: (String) -> Unit
) {
    val filteredItems = if (userRole.isBlank()) items
    else items.filter { it.allowedRoles.isEmpty() || userRole in it.allowedRoles }

    if (filteredItems.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Secondary,
            modifier = Modifier.padding(start = 4.dp)
        )

        // Menggunakan chunked untuk membuat baris yang konsisten (2 kolom)
        filteredItems.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                rowItems.forEach { item ->
                    MenuCard(
                        modifier = Modifier.weight(1f),
                        item = item,
                        onClick = { onItemClick(item.label) }
                    )
                }
                // Jika item dalam baris ganjil, tambahkan spacer agar ukuran tetap konsisten
                if (rowItems.size < 2) {
                    Spacer(modifier = Modifier.weight(1f))
                }
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
        modifier = modifier.height(115.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Neutral)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(item.color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.label,
                    tint = item.color,
                    modifier = Modifier.size(32.dp)
                )
            }

            Text(
                text = item.label,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    lineHeight = 14.sp
                ),
                color = Secondary,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
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
            Triple(Icons.Default.Home, "Beranda", 0),
            Triple(Icons.AutoMirrored.Filled.Assignment, "Kehadiran", 1),
            Triple(Icons.Default.ChatBubble, "Pesan", 2),
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
    uiState: DashboardUiState,
    onNavigateTo: (String) -> Unit,
    onLogout: () -> Unit,
    onCloseDrawer: () -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier.width(300.dp),
        drawerContainerColor = Surface,
        drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
    ) {
        // ── Header Drawer (Modern & Minimalist) ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 40.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Profile Section
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(Primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    
                    Column {
                        Text(
                            text = uiState.userNamaLengkap,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Secondary
                        )
                        Surface(
                            color = Primary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = uiState.userRole.ifEmpty { "Member" },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Primary
                            )
                        }
                    }
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp), color = Neutral)
        Spacer(modifier = Modifier.height(16.dp))

        // ── Menu Drawer ──
        DrawerItem(
            icon = Icons.Default.Checklist,
            label = "Data Persetujuan",
            onClick = {
                onCloseDrawer()
                onNavigateTo("riwayat_kehadiran")
            }
        )

        DrawerItem(
            icon = Icons.Default.PersonAddAlt,
            label = "Tambah Karyawan",
            onClick = {
                onCloseDrawer()
                onNavigateTo("tambah_karyawan")
            }
        )

        DrawerItem(
            icon = Icons.Default.Schedule,
            label = "Kalender",
            onClick = {
                onCloseDrawer()
                onNavigateTo("kalender")
            }
        )

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = Neutral
        )

        // ── Logout ──
        DrawerItem(
            icon = Icons.AutoMirrored.Filled.Logout,
            label = "Logout",
            iconTint = Error,
            labelColor = Error,
            onClick = {
                onCloseDrawer()
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
private fun formatRupiah(amount: Double): String {
    val isNegative = amount < 0
    val absStr = kotlin.math.abs(amount).toLong().toString()
    val sb = StringBuilder()
    var count = 0
    for (i in absStr.lastIndex downTo 0) {
        if (count > 0 && count % 3 == 0) sb.insert(0, '.')
        sb.insert(0, absStr[i])
        count++
    }
    val prefix = if (isNegative) "-Rp " else "Rp "
    return "$prefix$sb"
}
