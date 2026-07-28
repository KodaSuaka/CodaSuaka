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
    val categoryColor = when (produk.kategori) {
        "Minuman" -> BlueSchedule
        "Makanan" -> OrangeManage
        "Camilan" -> PurpleLog
        "Jasa" -> TealStatus
        else -> Secondary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Neutral)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header: Category Badge
            Surface(
                color = categoryColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    text = produk.kategori,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = categoryColor
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Placeholder Image / Icon
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(categoryColor.copy(alpha = 0.05f)),
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
                    tint = categoryColor,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = produk.nama,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Black,
                color = Secondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )

            Text(
                text = formatRupiahKasir(produk.harga),
                style = MaterialTheme.typography.titleMedium,
                color = OnSurface,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Action: Add/Minus
            if (quantity == 0) {
                Button(
                    onClick = onAdd,
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tambah", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier.size(36.dp).background(Neutral, CircleShape)
                    ) {
                        Icon(Icons.Default.Remove, null, tint = Secondary, modifier = Modifier.size(18.dp))
                    }
                    
                    Text(
                        text = quantity.toString(),
                        fontWeight = FontWeight.Black,
                        color = Secondary,
                        fontSize = 16.sp
                    )

                    IconButton(
                        onClick = onAdd,
                        modifier = Modifier.size(36.dp).background(categoryColor, CircleShape)
                    ) {
                        Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(18.dp))
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
            .height(72.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Primary.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingBag, 
                        null, 
                        tint = Primary, 
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "$totalItems Item terpilih",
                        fontSize = 12.sp,
                        color = OnSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = formatRupiahKasir(totalPrice),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Secondary
                    )
                }
            }

            Button(
                onClick = { 
                    // To prevent immediate navigation, let's open the sheet first
                    onClick()
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Success),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Text("Bayar", fontWeight = FontWeight.Black, fontSize = 15.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Icon(Icons.Default.ChevronRight, null, modifier = Modifier.size(20.dp))
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
                    .height(60.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Success),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    text = "Konfirmasi & Bayar",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(12.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(24.dp))
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
    val categoryColor = when (item.produk.kategori) {
        "Minuman" -> BlueSchedule
        "Makanan" -> OrangeManage
        "Camilan" -> PurpleLog
        "Jasa" -> TealStatus
        else -> Secondary
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp), // Menambah ruang agar tidak sesak
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Ikon Produk
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(categoryColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when(item.produk.kategori) {
                    "Minuman" -> Icons.Default.LocalCafe
                    "Makanan" -> Icons.Default.Restaurant
                    "Jasa" -> Icons.Default.Build
                    else -> Icons.Default.Fastfood
                },
                contentDescription = null,
                tint = categoryColor,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Info Barang
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.produk.nama,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = formatRupiahKasir(item.produk.harga),
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
        }

        // Kontrol Jumlah (Slim Design)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Neutral.copy(alpha = 0.3f))
                .padding(horizontal = 4.dp, vertical = 4.dp)
        ) {
            // Tombol Minus
            Surface(
                onClick = onRemove,
                modifier = Modifier.size(26.dp),
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 1.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = null,
                        tint = Secondary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Text(
                text = item.quantity.toString(),
                fontWeight = FontWeight.Black,
                color = Secondary,
                fontSize = 15.sp
            )

            // Tombol Plus
            Surface(
                onClick = onAdd,
                modifier = Modifier.size(26.dp),
                shape = CircleShape,
                color = categoryColor,
                shadowElevation = 2.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
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
