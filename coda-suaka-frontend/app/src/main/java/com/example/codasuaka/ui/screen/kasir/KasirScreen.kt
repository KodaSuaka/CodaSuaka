package com.example.codasuaka.ui.screen.kasir

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codasuaka.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KasirScreen(
    onBack: () -> Unit,
    viewModel: KasirViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    // ─── Force Light Theme ───
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Primary,
            onPrimary = Color.White,
            secondary = Secondary,
            onSecondary = Color.White,
            surface = Color.White,
            onSurface = OnSurface,
            onSurfaceVariant = OnSurfaceVariant,
            tertiary = Tertiary,
            outline = NeutralBorder
        )
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Text(
                            text = "Kasir", 
                            fontWeight = FontWeight.ExtraBold, 
                            color = Secondary 
                        ) 
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack, 
                                contentDescription = "Kembali", 
                                tint = Secondary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface)
                )
            },
            containerColor = Tertiary
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // ── Header: Search & Category ──
                    HeaderKasir(
                        searchQuery = uiState.searchQuery,
                        onSearchChange = { viewModel.onSearchQueryChange(it) },
                        selectedCategory = uiState.selectedCategory,
                        onCategorySelect = { viewModel.onCategorySelect(it) }
                    )

                    // ── Product Grid ──
                    if (uiState.filteredProducts.isEmpty()) {
                        EmptyStateKasir()
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(uiState.filteredProducts, key = { it.id }) { produk ->
                                val cartItem = uiState.cartItems[produk.id]
                                ProductCard(
                                    produk = produk,
                                    quantity = cartItem?.quantity ?: 0,
                                    onAdd = { viewModel.addToCart(produk) },
                                    onRemove = { viewModel.removeFromCart(produk) }
                                )
                            }
                        }
                    }
                }

                // ── Floating Cart Bar ──
                AnimatedVisibility(
                    visible = uiState.totalItems > 0,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    CartSummaryBar(
                        totalItems = uiState.totalItems,
                        totalPrice = uiState.totalPrice,
                        onClick = { viewModel.toggleCartSheet(true) },
                        onCheckout = { /* Next Stage */ }
                    )
                }
            }
        }

        // ── Cart Details Popup (Bottom Sheet) ──
        if (uiState.showCartSheet) {
            CartDetailsSheet(
                cartItems = uiState.cartItems.values.toList(),
                totalPrice = uiState.totalPrice,
                onDismiss = { viewModel.toggleCartSheet(false) },
                onAdd = { viewModel.addToCart(it) },
                onRemove = { viewModel.removeFromCart(it) },
                onCheckout = { /* Next Stage */ }
            )
        }
    }
}

@Composable
private fun HeaderKasir(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedCategory: String,
    onCategorySelect: (String) -> Unit
) {
    val categories = listOf("Semua", "Minuman", "Makanan", "Camilan", "Jasa")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("Cari produk...", fontSize = 14.sp) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Icon(Icons.Default.Search, null, tint = OnSurfaceVariant) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                unfocusedBorderColor = Neutral,
                focusedContainerColor = InputBackground,
                unfocusedContainerColor = InputBackground
            )
        )

        // Categories Scrollable Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            categories.forEach { category ->
                val isSelected = category == selectedCategory
                
                // Racikan Warna Dinamis
                val categoryColor = when (category) {
                    "Semua" -> Primary
                    "Minuman" -> BlueSchedule
                    "Makanan" -> OrangeManage
                    "Camilan" -> PurpleLog
                    "Jasa" -> TealStatus
                    else -> Secondary
                }

                if (category == "Semua") {
                    // Style ala Buku Kas (Minimalist Outlined)
                    FilterChip(
                        selected = isSelected,
                        onClick = { onCategorySelect(category) },
                        label = { 
                            Text(
                                text = category,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                                color = if (isSelected) Primary else Secondary
                            ) 
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Primary.copy(alpha = 0.08f),
                            containerColor = Color.White
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = NeutralBorder,
                            selectedBorderColor = Primary.copy(alpha = 0.2f),
                            borderWidth = 1.dp
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                } else {
                    // Style Warna-warni untuk Kategori
                    FilterChip(
                        selected = isSelected,
                        onClick = { onCategorySelect(category) },
                        label = { 
                            Text(
                                text = category,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                                color = if (isSelected) Color.White else Secondary
                            ) 
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = categoryColor,
                            containerColor = Neutral.copy(alpha = 0.7f)
                        ),
                        border = null,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductCard(
    produk: Produk,
    quantity: Int,
    onAdd: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Neutral)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Placeholder Image / Icon
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Primary.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when(produk.kategori) {
                        "Minuman" -> Icons.Default.LocalCafe
                        "Makanan" -> Icons.Default.Restaurant
                        "Jasa" -> Icons.Default.Build
                        else -> Icons.Default.Fastfood
                    },
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = produk.nama,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Secondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = formatRupiahKasir(produk.harga),
                style = MaterialTheme.typography.titleMedium,
                color = Primary,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Action: Add/Minus
            if (quantity == 0) {
                Button(
                    onClick = onAdd,
                    modifier = Modifier.fillMaxWidth().height(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                    Text("Tambah", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier.size(32.dp).background(Neutral, CircleShape)
                    ) {
                        Icon(Icons.Default.Remove, null, tint = Secondary, modifier = Modifier.size(16.dp))
                    }
                    
                    Text(
                        text = quantity.toString(),
                        fontWeight = FontWeight.ExtraBold,
                        color = Secondary
                    )

                    IconButton(
                        onClick = onAdd,
                        modifier = Modifier.size(32.dp).background(Primary, CircleShape)
                    ) {
                        Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun CartSummaryBar(
    totalItems: Int,
    totalPrice: Double,
    onClick: () -> Unit,
    onCheckout: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(64.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Secondary),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(36.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.ShoppingBag, null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "$totalItems Item",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = formatRupiahKasir(totalPrice),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }

            Button(
                onClick = onCheckout,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("Bayar", fontWeight = FontWeight.ExtraBold)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.ChevronRight, null, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CartDetailsSheet(
    cartItems: List<CartItem>,
    totalPrice: Double,
    onDismiss: () -> Unit,
    onAdd: (Produk) -> Unit,
    onRemove: (Produk) -> Unit,
    onCheckout: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle(color = NeutralBorder.copy(alpha = 0.5f)) },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Rincian Pesanan",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Secondary
                )
                TextButton(onClick = onDismiss) {
                    Text("Tutup", color = OnSurfaceVariant)
                }
            }

            // Item List
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(weight = 1f, fill = false)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                cartItems.forEach { item ->
                    CartItemRow(
                        item = item,
                        onAdd = { onAdd(item.produk) },
                        onRemove = { onRemove(item.produk) }
                    )
                }
            }

            HorizontalDivider(color = Neutral, thickness = 1.dp)

            // Summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total Pembayaran",
                    style = MaterialTheme.typography.bodyLarge,
                    color = OnSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = formatRupiahKasir(totalPrice),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = Primary
                )
            }

            // Action
            Button(
                onClick = { 
                    onDismiss()
                    onCheckout()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text(
                    text = "Konfirmasi & Bayar",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(10.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null)
            }
        }
    }
}

@Composable
private fun CartItemRow(
    item: CartItem,
    onAdd: () -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.produk.nama,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Secondary
            )
            Text(
                text = formatRupiahKasir(item.produk.harga),
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceVariant
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(30.dp).background(Neutral.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(Icons.Default.Remove, null, tint = Secondary, modifier = Modifier.size(14.dp))
            }

            Text(
                text = item.quantity.toString(),
                fontWeight = FontWeight.ExtraBold,
                color = Secondary,
                fontSize = 15.sp
            )

            IconButton(
                onClick = onAdd,
                modifier = Modifier.size(30.dp).background(Primary.copy(alpha = 0.1f), CircleShape)
            ) {
                Icon(Icons.Default.Add, null, tint = Primary, modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
private fun EmptyStateKasir() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.SearchOff, null, modifier = Modifier.size(64.dp), tint = Neutral)
            Text("Produk tidak ditemukan", color = OnSurfaceVariant)
        }
    }
}

private fun formatRupiahKasir(amount: Double): String {
    val absStr = kotlin.math.abs(amount).toLong().toString()
    val sb = StringBuilder()
    var count = 0
    for (i in absStr.lastIndex downTo 0) {
        if (count > 0 && count % 3 == 0) sb.insert(0, '.')
        sb.insert(0, absStr[i])
        count++
    }
    return "Rp $sb"
}
