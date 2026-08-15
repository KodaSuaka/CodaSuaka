package com.example.codasuaka.ui.screen.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
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
import com.example.codasuaka.ui.components.CodaSuakaNavbar
import com.example.codasuaka.ui.components.CustomCalendarNavigation
import com.example.codasuaka.ui.components.NavbarItem
import com.example.codasuaka.ui.components.YearPickerDialog
import com.example.codasuaka.ui.components.NotificationBannerStatic
import com.example.codasuaka.ui.components.CodaSuakaSnackbarHost
import com.example.codasuaka.ui.screen.notifikasi.NotificationSidebar
import com.example.codasuaka.ui.screen.notifikasi.NotificationViewModel
import com.example.codasuaka.ui.theme.*
import com.example.codasuaka.ui.util.formatRupiah
import org.koin.androidx.compose.koinViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.ui.draw.clipToBounds
import com.example.codasuaka.util.ClickHelper
import com.example.codasuaka.util.DateTimeUtil

// ─── Data class menu items ───
private data class MenuItem(
    val label: String,
    val icon: ImageVector,
    val color: Color = Primary,
    val allowedRoles: List<String> = emptyList(), // empty = all roles
    val requiredPermission: String? = null // when set, takes precedence over allowedRoles — checked against the user's actual synced permissions, not their role name, so custom roles work correctly
)

// ─── DashboardScreen ─────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateTo: (String) -> Unit,
    onLogout: () -> Unit,
    viewModel: DashboardViewModel,
    notificationViewModel: NotificationViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val notificationUiState by notificationViewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val snackbarHostState = remember { SnackbarHostState() }

    // Pull to Refresh state
    val pullRefreshState = rememberPullToRefreshState()

    // Reset navbar index to Home every time we return to this screen
    com.example.codasuaka.util.OnResumeEffect {
        viewModel.onBottomNavSelected(0)
    }

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
            containerColor = Tertiary, 
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
                        IconButton(onClick = { notificationViewModel.toggleSidebar(true) }) {
                            BadgedBox(
                                badge = {
                                    if (notificationUiState.unreadCount > 0) {
                                        Badge(
                                            containerColor = Error,
                                            modifier = Modifier.size(16.dp).offset(x = (-4).dp, y = 4.dp)
                                        ) {
                                            Text(
                                                text = if (notificationUiState.unreadCount > 99) "9+" else notificationUiState.unreadCount.toString(),
                                                fontSize = 9.sp,
                                                color = OnPrimary
                                            )
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notifikasi",
                                    tint = Primary
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Surface
                    )
                )
            },
            bottomBar = {
                CodaSuakaNavbar(
                    items = listOf(
                        NavbarItem(
                            selectedIcon = Icons.Default.Home, 
                            unselectedIcon = Icons.Outlined.Home, 
                            label = "Beranda", 
                            index = 0
                        ),
                        NavbarItem(
                            selectedIcon = Icons.AutoMirrored.Filled.Assignment, 
                            unselectedIcon = Icons.AutoMirrored.Outlined.Assignment, 
                            label = "Kehadiran", 
                            index = 1
                        ),
                        NavbarItem(
                            selectedIcon = Icons.Default.ChatBubble, 
                            unselectedIcon = Icons.Outlined.ChatBubbleOutline, 
                            label = "Pesan", 
                            index = 2, 
                            hasBadge = uiState.hasUnreadMessages
                        ),
                        NavbarItem(
                            selectedIcon = Icons.Default.PointOfSale, 
                            unselectedIcon = Icons.Outlined.PointOfSale, 
                            label = "Kasir", 
                            index = 3
                        )
                    ),
                    selectedIndex = uiState.selectedBottomNav,
                    onItemSelected = { index ->
                        viewModel.onBottomNavSelected(index)
                        when (index) {
                            0 -> { /* Beranda */ }
                            1 -> onNavigateTo("riwayat_kehadiran")
                            2 -> onNavigateTo("contact_list")
                            3 -> onNavigateTo("kasir")
                        }
                    }
                )
            },
            snackbarHost = { CodaSuakaSnackbarHost(hostState = snackbarHostState) }
        ) { innerPadding ->
            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = { viewModel.refreshDashboard() },
                state = pullRefreshState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Tertiary),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // ── Loading Indicator (Internal, only if not refreshing via pull) ──
                    if (uiState.isLoading && !uiState.isRefreshing) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Primary)
                        }
                    }

                    // ── Error Message (User-Friendly Notification) ──
                    if (uiState.errorMessage != null) {
                        NotificationBannerStatic(
                            message = uiState.errorMessage ?: "",
                            mapFromServer = true,
                            onDismiss = { viewModel.clearError() }
                        )
                    }

                    // ══════════════════════════════════════════════
                    // SECTION ATAS — Omset
                    // ══════════════════════════════════════════════
                    SectionOmset(
                        omsetTotal = uiState.omsetTotal,
                        onCariOmset = { start, end ->
                            viewModel.loadOmset(start, end)
                        }
                    )

                    // ══════════════════════════════════════════════
                    // SECTION TENGAH — Menu Utama (4 Ikon Besar)
                    // ══════════════════════════════════════════════
                    SectionMenuGrid(
                        title = "Menu Utama",
                        userRole = uiState.userRole,
                        userPermissions = uiState.userPermissions,
                        items = listOf(
                            MenuItem("Laporan Keuangan", Icons.Default.AccountBalance, Primary, allowedRoles = listOf("Owner")),
                            MenuItem("Approval Keuangan", Icons.Default.FactCheck, Primary, requiredPermission = "approve:keuangan"),
                            MenuItem("Penugasan", Icons.AutoMirrored.Filled.Assignment, Primary, allowedRoles = listOf("Owner")),
                            MenuItem("Riwayat Nota", Icons.Default.ReceiptLong, Primary, requiredPermission = "view:kasir")
                        ),
                        onItemClick = { label ->
                            when (label) {
                                "Laporan Keuangan" -> onNavigateTo("laporan_keuangan")
                                "Approval Keuangan" -> onNavigateTo("approval_keuangan")
                                "Penugasan" -> onNavigateTo("penugasan")
                                "Riwayat Nota" -> onNavigateTo("riwayat_nota")
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    NotificationSidebar(
        uiState = notificationUiState,
        onClose = { notificationViewModel.toggleSidebar(false) },
        onMarkAsRead = { notificationViewModel.markAsRead(it) },
        onMarkAllAsRead = { notificationViewModel.markAllAsRead() },
        onDelete = { notificationViewModel.deleteNotification(it) },
        onRefresh = { notificationViewModel.refresh() }
    )
}

// ─── Section Omset ──────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SectionOmset(
    omsetTotal: Double,
    onCariOmset: (String, String) -> Unit
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
                containerColor = Surface
            )
        ) {
            if (showYearPicker) {
                val displayMonth = Instant.ofEpochMilli(datePickerState.displayedMonthMillis)
                    .atZone(java.time.ZoneOffset.UTC)
                    .toLocalDate()
                    
                YearPickerDialog(
                    selectedYear = displayMonth.year,
                    onYearSelected = { year ->
                        val currentMonth = Instant.ofEpochMilli(datePickerState.displayedMonthMillis)
                            .atZone(java.time.ZoneOffset.UTC)
                            .toLocalDate()
                        val targetMonth = currentMonth.withYear(year)
                        datePickerState.displayedMonthMillis = targetMonth.atStartOfDay(java.time.ZoneOffset.UTC).toInstant().toEpochMilli()
                        showYearPicker = false
                    },
                    onDismiss = { showYearPicker = false }
                )
            }

            Column(
                modifier = Modifier.padding(top = 16.dp)
            ) {
                // Header Kustom < Bulan Tahun >
                val displayMonth = Instant.ofEpochMilli(datePickerState.displayedMonthMillis)
                    .atZone(java.time.ZoneOffset.UTC)
                    .toLocalDate()
                
                val monthTitle = remember(displayMonth) { displayMonth.format(formatter) }
                
                CustomCalendarNavigation(
                    title = monthTitle.replaceFirstChar { it.uppercase() },
                    onPrevClick = {
                        val currentMonth = Instant.ofEpochMilli(datePickerState.displayedMonthMillis)
                            .atZone(java.time.ZoneOffset.UTC)
                            .toLocalDate()
                            .withDayOfMonth(1)
                        val prevMonth = currentMonth.minusMonths(1)
                        datePickerState.displayedMonthMillis = prevMonth.atStartOfDay(java.time.ZoneOffset.UTC).toInstant().toEpochMilli()
                    },
                    onNextClick = {
                        val currentMonth = Instant.ofEpochMilli(datePickerState.displayedMonthMillis)
                            .atZone(java.time.ZoneOffset.UTC)
                            .toLocalDate()
                            .withDayOfMonth(1)
                        val nextMonth = currentMonth.plusMonths(1)
                        datePickerState.displayedMonthMillis = nextMonth.atStartOfDay(java.time.ZoneOffset.UTC).toInstant().toEpochMilli()
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
                            containerColor = Surface,
                            titleContentColor = Secondary,
                            headlineContentColor = Secondary,
                            weekdayContentColor = Secondary.copy(alpha = 0.6f),
                            subheadContentColor = Secondary.copy(alpha = 0.6f),
                            yearContentColor = Secondary.copy(alpha = 0.7f),
                            currentYearContentColor = Primary,
                            selectedYearContentColor = OnPrimary,
                            selectedYearContainerColor = Primary,
                            dayContentColor = OnSurface,
                            selectedDayContentColor = OnPrimary,
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
                        containerColor = Secondary, 
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
    userPermissions: List<String> = emptyList(),
    onItemClick: (String) -> Unit
) {
    val filteredItems = items.filter { item ->
        val requiredPermission = item.requiredPermission
        if (requiredPermission != null) {
            requiredPermission in userPermissions
        } else if (userRole.isBlank()) {
            true
        } else {
            item.allowedRoles.isEmpty() || userRole in item.allowedRoles
        }
    }

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
        // ── Header Drawer (Profile) ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 40.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(
                    modifier = Modifier.size(60.dp).clip(CircleShape).background(Primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, null, tint = Primary, modifier = Modifier.size(32.dp))
                }
                Column {
                    Text(text = uiState.userNamaLengkap, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Secondary)
                    Surface(color = Primary.copy(alpha = 0.1f), shape = RoundedCornerShape(6.dp)) {
                        Text(text = uiState.userRole.ifEmpty { "Member" }, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Primary)
                    }
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp), color = Neutral)

        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .weight(1f)
                .padding(vertical = 16.dp)
        ) {
            // ── Kategori: Operasional Toko ──
            DrawerCategoryLabel("Operasional Toko")
            DrawerItem(Icons.Default.Store, "Kelola Outlet", iconTint = Primary) { onCloseDrawer(); onNavigateTo("kelola_outlet") }
            DrawerItem(Icons.Default.AccessTime, "Jam Operasional", iconTint = Primary) { onCloseDrawer(); onNavigateTo("jam_operasional") }
            DrawerItem(Icons.Default.Groups, "Divisi", iconTint = Primary) { onCloseDrawer(); onNavigateTo("divisi") }
            DrawerItem(Icons.Default.CalendarMonth, "Jadwal", iconTint = Primary) { onCloseDrawer(); onNavigateTo("kalender") }
            DrawerItem(Icons.Default.Print, "Pengaturan Struk", iconTint = Primary) { onCloseDrawer(); onNavigateTo("receipt_settings") }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Kategori: Produk & Stok ──
            DrawerCategoryLabel("Produk & Stok")
            DrawerItem(Icons.Default.Receipt, "Nota Pembelian", iconTint = Primary) { onCloseDrawer(); onNavigateTo("nota_pembelian") }
            DrawerItem(Icons.Default.Inventory2, "Kelola Produk", iconTint = Primary) { onCloseDrawer(); onNavigateTo("kelola_barang_jasa") }
            DrawerItem(Icons.Default.Warehouse, "Stok", iconTint = Primary) { onCloseDrawer(); onNavigateTo("stok") }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Kategori: Karyawan ──
            DrawerCategoryLabel("Manajemen Karyawan")
            DrawerItem(Icons.Default.PeopleAlt, "Kelola Karyawan", iconTint = Primary) { onCloseDrawer(); onNavigateTo("kelola_karyawan") }
        }

        HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp), color = Neutral)

        // ── Logout (Paling Bawah) ──
        DrawerItem(
            icon = Icons.AutoMirrored.Filled.Logout,
            label = "Keluar dari Akun",
            iconTint = Error,
            labelColor = Error,
            onClick = { onCloseDrawer(); onLogout() }
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun DrawerCategoryLabel(label: String) {
    Text(
        text = label,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Black,
        color = Secondary.copy(alpha = 0.4f),
        letterSpacing = 1.sp
    )
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

