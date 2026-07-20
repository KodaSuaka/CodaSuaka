package com.example.codasuaka.ui.screen.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
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
import com.example.codasuaka.data.remote.dto.ContactDto
import com.example.codasuaka.data.remote.dto.ContactGroupDto
import com.example.codasuaka.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatContactListScreen(
    onBack: () -> Unit,
    onContactClick: (userId: Int, userName: String) -> Unit,
    viewModel: ChatContactViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Kontak",
                        fontWeight = FontWeight.Bold,
                        color = OnPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Kembali",
                            tint = OnPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* Implementasi Pencarian */ }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Cari",
                            tint = OnPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White)
        ) {
            if (uiState.isLoading && uiState.contactGroups.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Primary)
                }
            } else if (uiState.errorMessage != null && uiState.contactGroups.isEmpty()) {
                // ... Error UI tetap sama ...
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = uiState.errorMessage ?: "Terjadi kesalahan",
                        color = Error,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.loadContacts() },
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Text("Coba Lagi")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    uiState.contactGroups.forEach { group ->
                        // Section header per role
                        item {
                            Text(
                                text = group.role,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = Primary,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Neutral.copy(alpha = 0.3f))
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }

                        // Daftar kontak
                        items(group.contacts, key = { it.id }) { contact ->
                            ContactListItem(
                                contact = contact,
                                onClick = { onContactClick(contact.id, contact.namaLengkap ?: contact.name ?: "Unknown") }
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 72.dp),
                                thickness = 0.5.dp,
                                color = Neutral
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ContactListItem(
    contact: ContactDto,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Avatar Bulat
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Primary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = OnPrimary,
                modifier = Modifier.size(28.dp)
            )
        }

        // Konten Teks
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = contact.namaLengkap ?: contact.name ?: "Unknown",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = OnSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (contact.lastMessageTime != null) {
                    Text(
                        text = contact.lastMessageTime ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (contact.unreadCount > 0) Primary else OnSurfaceVariant
                    )
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = contact.lastMessage ?: "Mulai percakapan",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                if (contact.unreadCount > 0) {
                    Surface(
                        shape = CircleShape,
                        color = Primary, // Menggunakan Primary biru kita agar lebih modern
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            text = if (contact.unreadCount > 99) "99+" else contact.unreadCount.toString(),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = OnPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
