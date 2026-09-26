package com.example.codasuaka.ui.screen.kasir

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.codasuaka.data.remote.dto.BarangJasaDto
import com.example.codasuaka.data.remote.dto.CreateNotaRequest
import com.example.codasuaka.data.remote.dto.NotaDto
import com.example.codasuaka.data.remote.dto.NotaItemRequest
import com.example.codasuaka.domain.repository.KasirRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

// ─── Data Models ──────────────────────────────────────────

data class CartItem(
    val produk: BarangJasaDto,
    val quantity: Int
)

data class KasirUiState(
    val allProducts: List<BarangJasaDto> = emptyList(),
    val filteredProducts: List<BarangJasaDto> = emptyList(),
    val categories: List<String> = listOf("Semua"),
    val cartItems: Map<Int, CartItem> = emptyMap(), // Key: Produk ID
    val selectedCategory: String = "Semua",
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val loadError: String? = null,
    val showCartSheet: Boolean = false,
    val totalPrice: Double = 0.0,
    val totalItems: Int = 0,
    val isCheckingOut: Boolean = false,
    val checkoutError: String? = null,
    val checkoutSuccessNota: NotaDto? = null
)

class KasirViewModel(private val kasirRepository: KasirRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(KasirUiState())
    val uiState: StateFlow<KasirUiState> = _uiState.asStateFlow()

    init {
        loadBarangJasa()
    }

    fun loadBarangJasa() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadError = null) }
            kasirRepository.getBarangJasaList(jenis = null, isActive = true)
                .onSuccess { products ->
                    val categories = listOf("Semua") + products.mapNotNull { it.kategori }.distinct()
                    _uiState.update { state ->
                        state.copy(
                            allProducts = products,
                            categories = categories,
                            isLoading = false
                        )
                    }
                    applyFilter()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, loadError = e.message) }
                }
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
                val kategoriEfektif = produk.kategori ?: "Lainnya"
                val matchCategory = state.selectedCategory == "Semua" || kategoriEfektif == state.selectedCategory
                val matchSearch = produk.nama.contains(state.searchQuery, ignoreCase = true)
                matchCategory && matchSearch
            }
            state.copy(filteredProducts = filtered)
        }
    }

    // ─── Cart Management ──────────────────────────────────────

    fun addToCart(produk: BarangJasaDto) {
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

    fun removeFromCart(produk: BarangJasaDto) {
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
        val total = state.cartItems.values.sumOf { it.produk.hargaJual * it.quantity }
        val count = state.cartItems.values.sumOf { it.quantity }
        return state.copy(
            cartItems = state.cartItems,
            totalPrice = total,
            totalItems = count
        )
    }

    // ─── Checkout ──────────────────────────────────────────────

    fun checkout() {
        val cart = _uiState.value.cartItems
        if (cart.isEmpty() || _uiState.value.isCheckingOut) return

        viewModelScope.launch {
            _uiState.update { it.copy(isCheckingOut = true, checkoutError = null) }
            val request = CreateNotaRequest(
                tipe = "penjualan",
                tanggal = LocalDate.now().toString(),
                items = cart.values.map { item ->
                    NotaItemRequest(
                        barangJasaId = item.produk.id,
                        kuantitas = item.quantity.toDouble(),
                        hargaSatuan = item.produk.hargaJual
                    )
                }
            )
            kasirRepository.createNota(request)
                .onSuccess { nota ->
                    clearCart()
                    _uiState.update { it.copy(isCheckingOut = false, checkoutSuccessNota = nota) }
                    loadBarangJasa()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isCheckingOut = false, checkoutError = e.message) }
                }
        }
    }

    fun clearCheckoutSuccess() {
        _uiState.update { it.copy(checkoutSuccessNota = null) }
    }

    fun clearCheckoutError() {
        _uiState.update { it.copy(checkoutError = null) }
    }

    fun clearLoadError() {
        _uiState.update { it.copy(loadError = null) }
    }
}
