package com.example.codasuaka.ui.screen.notifikasi

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.codasuaka.data.remote.dto.NotificationDto
import com.example.codasuaka.ui.theme.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun NotificationSidebar(
    uiState: NotificationUiState,
    onClose: () -> Unit,
    onMarkAsRead: (Int) -> Unit,
    onMarkAllAsRead: () -> Unit,
    onRefresh: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().zIndex(100f)) {
        // Backdrop
        AnimatedVisibility(
            visible = uiState.isSidebarOpen,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onClose() }
            )
        }

        // Sidebar Sheet
        AnimatedVisibility(
            visible = uiState.isSidebarOpen,
            enter = slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(400, easing = FastOutSlowInEasing)
            ),
            exit = slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(400, easing = FastOutSlowInEasing)
            ),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(320.dp),
                color = Surface,
                tonalElevation = 8.dp,
                shape = RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header
                    SidebarHeader(
                        unreadCount = uiState.unreadCount,
                        onClose = onClose,
                        onMarkAllAsRead = onMarkAllAsRead
                    )

                    HorizontalDivider(color = Neutral, thickness = 1.dp)

                    // Content
                    Box(modifier = Modifier.weight(1f)) {
                        when {
                            uiState.isLoading && uiState.notifications.isEmpty() -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.align(Alignment.Center),
                                    color = Primary
                                )
                            }
                            uiState.notifications.isEmpty() -> {
                                EmptyNotifications(onRefresh)
                            }
                            else -> {
                                NotificationList(
                                    notifications = uiState.notifications,
                                    onMarkAsRead = onMarkAsRead
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SidebarHeader(
    unreadCount: Int,
    onClose: () -> Unit,
    onMarkAllAsRead: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "Notifikasi",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Secondary
            )
            if (unreadCount > 0) {
                Text(
                    text = "$unreadCount belum dibaca",
                    style = MaterialTheme.typography.labelMedium,
                    color = Primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (unreadCount > 0) {
                IconButton(onClick = onMarkAllAsRead) {
                    Icon(
                        imageVector = Icons.Default.DoneAll,
                        contentDescription = "Baca Semua",
                        tint = Primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Tutup",
                    tint = OnSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun NotificationList(
    notifications: List<NotificationDto>,
    onMarkAsRead: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(notifications, key = { it.id }) { notification ->
            SidebarNotificationItem(
                notification = notification,
                onClick = { if (!notification.isRead) onMarkAsRead(notification.id) }
            )
        }
    }
}

@Composable
private fun SidebarNotificationItem(
    notification: NotificationDto,
    onClick: () -> Unit
) {
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
        "Assignment" -> Icons.AutoMirrored.Filled.Assignment
        "AccessTime" -> Icons.Default.AccessTime
        "AccountBalance" -> Icons.Default.AccountBalance
        "Error" -> Icons.Default.Error
        else -> Icons.Default.Notifications
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) Surface else Primary.copy(alpha = 0.05f)
        ),
        border = if (!notification.isRead) androidx.compose.foundation.BorderStroke(1.dp, Primary.copy(alpha = 0.1f)) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (notification.isRead) FontWeight.SemiBold else FontWeight.ExtraBold,
                    color = Secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = notification.body,
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant,
                    maxLines = 3,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = formatTimeAgoSidebar(notification.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            if (!notification.isRead) {
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Primary)
                )
            }
        }
    }
}

@Composable
private fun EmptyNotifications(onRefresh: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Neutral),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.NotificationsNone,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = OnSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Belum ada notifikasi",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Secondary
        )
        Text(
            text = "Semua update terbaru akan muncul di sini.",
            style = MaterialTheme.typography.bodySmall,
            color = OnSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRefresh,
            colors = ButtonDefaults.buttonColors(containerColor = Primary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Refresh", fontWeight = FontWeight.Bold)
        }
    }
}

private fun formatTimeAgoSidebar(dateString: String?): String {
    if (dateString == null) return ""
    return try {
        val instant = Instant.parse(dateString)
        val now = Instant.now()
        val duration = java.time.Duration.between(instant, now)
        val seconds = duration.seconds

        when {
            seconds < 60 -> "Baru saja"
            seconds < 3600 -> "${seconds / 60}m lalu"
            seconds < 86400 -> "${seconds / 3600}j lalu"
            seconds < 604800 -> "${seconds / 86400}h lalu"
            else -> {
                val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.forLanguageTag("id"))
                    .withZone(ZoneId.systemDefault())
                formatter.format(instant)
            }
        }
    } catch (_: Exception) {
        dateString
    }
}
