package com.example.codasuaka.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.codasuaka.ui.theme.Error
import com.example.codasuaka.ui.theme.ErrorLight
import com.example.codasuaka.ui.theme.InfoBg
import com.example.codasuaka.ui.theme.InfoColor
import com.example.codasuaka.ui.theme.Success
import com.example.codasuaka.ui.theme.SuccessLight
import com.example.codasuaka.ui.theme.WarningBg
import com.example.codasuaka.ui.theme.WarningColor
import com.example.codasuaka.util.ErrorMessageMapper
import kotlinx.coroutines.delay

@Composable
fun NotificationBanner(
    message: String,
    type: ErrorMessageMapper.NotificationType? = null, // Optional for auto-detection
    title: String? = null,
    autoDismissMs: Long = 5000L,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    mapFromServer: Boolean = false
) {
    val displayMessage = remember(message, mapFromServer) {
        if (mapFromServer) ErrorMessageMapper.map(message, null).message else message
    }
    val displayTitle = remember(title, mapFromServer, message) {
        if (title != null) title else if (mapFromServer) ErrorMessageMapper.map(message, null).title else null
    }
    val displayType = remember(type, mapFromServer, displayMessage) {
        if (type != null) return@remember type
        
        val lowerMessage = displayMessage.lowercase()
        when {
            lowerMessage.contains("gagal") || lowerMessage.contains("kesalahan") || lowerMessage.contains("error") -> 
                ErrorMessageMapper.NotificationType.ERROR
            lowerMessage.contains("hapus") || lowerMessage.contains("buang") || lowerMessage.contains("delete") -> 
                ErrorMessageMapper.NotificationType.ERROR // Use Red/Coral for destructive actions
            lowerMessage.contains("perbarui") || lowerMessage.contains("simpan") || lowerMessage.contains("edit") || lowerMessage.contains("ubah") -> 
                ErrorMessageMapper.NotificationType.INFO
            lowerMessage.contains("berhasil") || lowerMessage.contains("sukses") -> 
                ErrorMessageMapper.NotificationType.SUCCESS
            else -> ErrorMessageMapper.NotificationType.ERROR // Fallback for visibility
        }
    }

    var visible by remember { mutableStateOf(true) }
    LaunchedEffect(autoDismissMs) {
        if (autoDismissMs > 0) {
            delay(autoDismissMs)
            visible = false
            delay(300)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { -it }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
        exit = slideOutVertically(targetOffsetY = { -it }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300)),
        modifier = modifier
    ) {
        NotificationContent(message = displayMessage, type = displayType, title = displayTitle, onDismiss = { visible = false; onDismiss() })
    }
}

@Composable
fun NotificationBannerStatic(
    message: String,
    type: ErrorMessageMapper.NotificationType? = null, // Optional for auto-detect
    title: String? = null,
    onDismiss: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    mapFromServer: Boolean = false
) {
    val displayMessage = remember(message, mapFromServer) {
        if (mapFromServer) ErrorMessageMapper.map(message, null).message else message
    }
    val displayTitle = remember(title, mapFromServer, message) {
        if (title != null) title else if (mapFromServer) ErrorMessageMapper.map(message, null).title else null
    }
    val displayType = remember(type, mapFromServer, displayMessage) {
        if (type != null) return@remember type
        
        val lowerMessage = displayMessage.lowercase()
        when {
            lowerMessage.contains("gagal") || lowerMessage.contains("kesalahan") || lowerMessage.contains("error") -> 
                ErrorMessageMapper.NotificationType.ERROR
            lowerMessage.contains("hapus") || lowerMessage.contains("buang") || lowerMessage.contains("delete") -> 
                ErrorMessageMapper.NotificationType.ERROR
            lowerMessage.contains("perbarui") || lowerMessage.contains("simpan") || lowerMessage.contains("edit") || lowerMessage.contains("ubah") -> 
                ErrorMessageMapper.NotificationType.INFO
            lowerMessage.contains("berhasil") || lowerMessage.contains("sukses") -> 
                ErrorMessageMapper.NotificationType.SUCCESS
            else -> ErrorMessageMapper.NotificationType.ERROR
        }
    }
    NotificationContent(message = displayMessage, type = displayType, title = displayTitle, onDismiss = onDismiss ?: {}, showCloseButton = onDismiss != null)
}

@Composable
fun CodaSuakaSnackbarHost(hostState: SnackbarHostState, modifier: Modifier = Modifier) {
    SnackbarHost(hostState = hostState, modifier = modifier.padding(16.dp)) { data ->
        val message = data.visuals.message
        val type = when {
            message.contains("berhasil", ignoreCase = true) || message.contains("sukses", ignoreCase = true) -> ErrorMessageMapper.NotificationType.SUCCESS
            else -> ErrorMessageMapper.NotificationType.INFO
        }
        NotificationContent(message = message, type = type, title = null, onDismiss = { data.dismiss() })
    }
}

@Composable
fun NotificationContent(message: String, type: ErrorMessageMapper.NotificationType, title: String?, onDismiss: () -> Unit, showCloseButton: Boolean = true) {
    val (backgroundColor, contentColor, borderColor, icon) = when (type) {
        ErrorMessageMapper.NotificationType.ERROR -> NotificationColors(container = ErrorLight, content = Error, border = Error.copy(alpha = 0.2f), icon = Icons.Filled.Error)
        ErrorMessageMapper.NotificationType.SUCCESS -> NotificationColors(container = SuccessLight, content = Success, border = Success.copy(alpha = 0.2f), icon = Icons.Filled.CheckCircle)
        ErrorMessageMapper.NotificationType.WARNING -> NotificationColors(container = WarningBg, content = WarningColor, border = WarningColor.copy(alpha = 0.2f), icon = Icons.Filled.Warning)
        ErrorMessageMapper.NotificationType.INFO -> NotificationColors(container = InfoBg, content = InfoColor, border = InfoColor.copy(alpha = 0.2f), icon = Icons.Filled.Info)
    }
    Card(modifier = Modifier.fillMaxWidth().animateContentSize(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = backgroundColor), border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
            Icon(imageVector = icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(22.dp).padding(top = 1.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                if (title != null) Text(text = title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = contentColor)
                Text(text = message, style = MaterialTheme.typography.bodySmall, color = contentColor.copy(alpha = 0.9f), maxLines = 3, overflow = TextOverflow.Ellipsis)
            }
            if (showCloseButton) IconButton(onClick = onDismiss, modifier = Modifier.size(20.dp)) { Icon(imageVector = Icons.Outlined.Close, contentDescription = "Tutup", tint = contentColor.copy(alpha = 0.6f), modifier = Modifier.size(16.dp)) }
        }
    }
}

private data class NotificationColors(val container: Color, val content: Color, val border: Color, val icon: androidx.compose.ui.graphics.vector.ImageVector)
