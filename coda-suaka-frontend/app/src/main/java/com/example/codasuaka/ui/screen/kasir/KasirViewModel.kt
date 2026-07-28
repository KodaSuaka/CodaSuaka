package com.example.codasuaka.ui.screen.kasir

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// ─── Data Models ──────────────────────────────────────────

data class Produk(
    val id: Int,
    val nama: String,
    val harga: Double,
    val kategori: String,
    val stok: Int = 99
)

data class CartItem(
    val produk: Produk,
    val quantity: Int
)

data class KasirUiState(
    val allProducts: List<Produk> = emptyList(),
    val filteredProducts: List<Produk> = emptyList(),
    val cartItems: Map<Int, CartItem> = emptyMap(), // Key: Produk ID
    val selectedCategory: String = "Semua",
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val showCartSheet: Boolean = false,
    val totalPrice: Double = 0.0,
    val totalItems: Int = 0
)

class KasirViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(KasirUiState())
    val uiState: StateFlow<KasirUiState> = _uiState.asStateFlow()

    init {
        loadMockProducts()
    }

    private fun loadMockProducts() {
        val mockData = listOf(
            Produk(1, "Americano Coffee", 18000.0, "Minuman"),
            Produk(2, "Caffè Latte", 25000.0, "Minuman"),
            Produk(3, "Matcha Green Tea", 28000.0, "Minuman"),
            Produk(4, "Croissant Butter", 15000.0, "Makanan"),
            Produk(5, "Nasi Goreng Spesial", 35000.0, "Makanan"),
            Produk(6, "Kentang Goreng", 20000.0, "Camilan"),
            Produk(7, "Pisang Bakar Keju", 18000.0, "Camilan"),
            Produk(8, "Jasa Bungkus Gift", 5000.0, "Jasa"),
            Produk(9, "Ongkos Kirim Lokal", 10000.0, "Jasa")
        )
        _uiState.update { 
            it.copy(
                allProducts = mockData,
                filteredProducts = mockData
            )
        }
    }

    // ─── Search & Filter ──────────────────────────────────────

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFilter()
    }

    fun onCategorySelect(category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
        applyFilter()
    }

    private fun applyFilter() {
        _uiState.update { state ->
            val filtered = state.allProducts.filter { produk ->
                val matchCategory = state.selectedCategory == "Semua" || produk.kategori == state.selectedCategory
                val matchSearch = produk.nama.contains(state.searchQuery, ignoreCase = true)
                matchCategory && matchSearch
            }
            state.copy(filteredProducts = filtered)
        }
    }

    // ─── Cart Management ──────────────────────────────────────

    fun addToCart(produk: Produk) {
        _uiState.update { state ->
            val currentCart = state.cartItems.toMutableMap()
            val existingItem = currentCart[produk.id]
            
            if (existingItem != null) {
                currentCart[produk.id] = existingItem.copy(quantity = existingItem.quantity + 1)
            } else {
                currentCart[produk.id] = CartItem(produk, 1)
            }
            
            calculateTotals(state.copy(cartItems = currentCart))
        }
    }

    fun removeFromCart(produk: Produk) {
        _uiState.update { state ->
            val currentCart = state.cartItems.toMutableMap()
            val existingItem = currentCart[produk.id] ?: return@update state
            
            if (existingItem.quantity > 1) {
                currentCart[produk.id] = existingItem.copy(quantity = existingItem.quantity - 1)
            } else {
                currentCart.remove(produk.id)
            }
            
            calculateTotals(state.copy(cartItems = currentCart))
        }
    }

    fun clearCart() {
        _uiState.update { state ->
            calculateTotals(state.copy(cartItems = emptyMap(), showCartSheet = false))
        }
    }

    fun toggleCartSheet(show: Boolean) {
        _uiState.update { it.copy(showCartSheet = show) }
    }

    private fun calculateTotals(state: KasirUiState): KasirUiState {
        val total = state.cartItems.values.sumOf { it.produk.harga * it.quantity }
        val count = state.cartItems.values.sumOf { it.quantity }
        return state.copy(
            cartItems = state.cartItems,
            totalPrice = total,
            totalItems = count
        )
    }
}
