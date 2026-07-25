package com.example.codasuaka.ui.screen.notifikasi

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codasuaka.data.remote.dto.NotificationDto
import com.example.codasuaka.ui.theme.*
import org.koin.androidx.compose.koinViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    onNavigateBack: () -> Unit,
    viewModel: NotificationViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf<NotificationDto?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Notifikasi",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    if (uiState.unreadCount > 0) {
                        TextButton(
                            onClick = { viewModel.markAllAsRead() }
                        ) {
                            Text(
                                text = "Baca Semua",
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Secondary
                )
            )
        },
        containerColor = Neutral
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Primary
                    )
                }
                uiState.error != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = uiState.error ?: "Terjadi kesalahan",
                            color = Error,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.loadNotifications() },
                            colors = ButtonDefaults.buttonColors(containerColor = Primary)
                        ) {
                            Text("Coba Lagi", color = Color.White)
                        }
                    }
                }
                uiState.notifications.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsOff,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = OnSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Belum ada notifikasi",
                            fontSize = 16.sp,
                            color = OnSurfaceVariant
                        )
                    }
                }
                else -> {
                    val listState = rememberLazyListState()

                    // Infinite scroll
                    LaunchedEffect(listState) {
                        snapshotFlow {
                            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                            val totalItems = listState.layoutInfo.totalItemsCount
                            lastVisibleItem >= totalItems - 3 && !uiState.isPaginating
                        }.collect { shouldLoadMore ->
                            if (shouldLoadMore) {
                                viewModel.loadMore()
                            }
                        }
                    }

                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = uiState.notifications,
                            key = { it.id }
                        ) { notification ->
                            NotificationItem(
                                notification = notification,
                                onClick = {
                                    if (!notification.isRead) {
                                        viewModel.markAsRead(notification.id)
                                    }
                                },
                                onDelete = { showDeleteDialog = notification }
                            )
                        }

                        if (uiState.isPaginating) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = Primary,
                                        strokeWidth = 2.dp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    showDeleteDialog?.let { notification ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Hapus Notifikasi") },
            text = { Text("Apakah Anda yakin ingin menghapus notifikasi ini?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteNotification(notification.id)
                        showDeleteDialog = null
                    }
                ) {
                    Text("Hapus", color = Error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
private fun NotificationItem(
    notification: NotificationDto,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    val backgroundColor = if (notification.isRead) Surface else InfoBg.copy(alpha = 0.3f)
    val iconColor = when (notification.color) {
        "#F59E0B" -> WarningColor
        "#10B981" -> Success
        "#EF4444" -> Error
        "#6366F1" -> Primary
        "#3B82F6" -> InfoColor
        "#8B5CF6" -> PurpleLog
        else -> Primary
    }
    val icon = when (notification.icon) {
        "Description" -> Icons.Default.Description
        "CheckCircle" -> Icons.Default.CheckCircle
        "Cancel" -> Icons.Default.Cancel
        "Assignment" -> Icons.Default.Assignment
        "AccessTime" -> Icons.Default.AccessTime
        "AccountBalance" -> Icons.Default.AccountBalance
        "Error" -> Icons.Default.Error
        else -> Icons.Default.Notifications
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (notification.isRead) 0.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = notification.title,
                    fontSize = 14.sp,
                    fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.Bold,
                    color = OnSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notification.body,
                    fontSize = 13.sp,
                    color = OnSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatTimeAgo(notification.createdAt),
                    fontSize = 11.sp,
                    color = OnSurfaceVariant
                )
            }

            // Unread indicator & delete
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!notification.isRead) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Primary)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Hapus",
                        tint = OnSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

private fun formatTimeAgo(dateString: String?): String {
    if (dateString == null) return ""
    return try {
        val instant = Instant.parse(dateString)
        val now = Instant.now()
        val duration = java.time.Duration.between(instant, now)
        val seconds = duration.seconds

        when {
            seconds < 60 -> "Baru saja"
            seconds < 3600 -> "${seconds / 60} menit lalu"
            seconds < 86400 -> "${seconds / 3600} jam lalu"
            seconds < 604800 -> "${seconds / 86400} hari lalu"
            else -> {
                val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale("id"))
                    .withZone(ZoneId.systemDefault())
                formatter.format(instant)
            }
        }
    } catch (e: Exception) {
        dateString
    }
}