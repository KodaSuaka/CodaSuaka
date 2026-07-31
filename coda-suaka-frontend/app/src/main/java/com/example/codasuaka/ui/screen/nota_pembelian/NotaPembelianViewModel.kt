package com.example.codasuaka.ui.screen.nota_pembelian

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.codasuaka.data.remote.dto.BarangJasaDto
import com.example.codasuaka.data.remote.dto.CreateNotaRequest
import com.example.codasuaka.data.remote.dto.NotaDto
import com.example.codasuaka.data.remote.dto.NotaItemRequest
import com.example.codasuaka.domain.repository.KasirRepository
import com.example.codasuaka.domain.repository.OutletRepository
import com.example.codasuaka.data.remote.dto.OutletDto
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

// ─── Data Models ──────────────────────────────────────────

/** Item keranjang nota pembelian (entry manual). */
data class PembelianCartItem(
    val namaItem: String,
    val kuantitas: Double,
    val satuan: String,
    val hargaSatuan: Double
) {
    val subtotal: Double get() = kuantitas * hargaSatuan
}

/** File Excel terpilih untuk impor nota pembelian. */
data class SelectedImportFile(
    val uri: Uri,
    val fileName: String,
    val sizeBytes: Long
)

/** Mode input nota pembelian. */
enum class NotaPembelianMode { MANUAL, IMPORT }

data class NotaPembelianUiState(
    // Katalog & outlet untuk dropdown
    val katalog: List<BarangJasaDto> = emptyList(),
    val outletList: List<OutletDto> = emptyList(),
    val isLoadingKatalog: Boolean = false,
    val loadError: String? = null,

    // Mode & form umum
    val mode: NotaPembelianMode = NotaPembelianMode.MANUAL,
    val tanggal: String = LocalDate.now().toString(),
    val selectedOutletId: Int? = null,
    val pihakTerkait: String = "",
    val metodePembayaran: String = "",
    val catatan: String = "",

    // Pencarian katalog
    val searchQuery: String = "",
    val filteredKatalog: List<BarangJasaDto> = emptyList(),

    // Keranjang manual
    val cartItems: List<PembelianCartItem> = emptyList(),
    val cartTotal: Double = 0.0,

    // Field item yang sedang diinput
    val itemKatalogId: Int? = null,
    val itemNama: String = "",
    val itemKuantitas: String = "",
    val itemSatuan: String = "pcs",
    val itemHarga: String = "",

    // Impor Excel
    val importFile: SelectedImportFile? = null,

    // Submit
    val isSubmitting: Boolean = false,
    val submitError: String? = null,
    val submitSuccessNota: NotaDto? = null
)

class NotaPembelianViewModel(
    private val kasirRepository: KasirRepository,
    private val outletRepository: OutletRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotaPembelianUiState())
    val uiState: StateFlow<NotaPembelianUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingKatalog = true, loadError = null) }
            try {
                coroutineScope {
                    val katalogDeferred = async { kasirRepository.getBarangJasaList(isActive = true) }
                    val outletDeferred = async { outletRepository.getOutlets() }

                    val katalogResult = katalogDeferred.await()
                    val outletResult = outletDeferred.await()

                    val katalog = katalogResult.getOrNull() ?: emptyList()
                    _uiState.update { state ->
                        state.copy(
                            katalog = katalog,
                            filteredKatalog = katalog,
                            outletList = outletResult.getOrNull() ?: emptyList(),
                            isLoadingKatalog = false,
                            loadError = katalogResult.exceptionOrNull()?.message
                        )
                    }
                }
            } catch (_: Exception) {
                _uiState.update { it.copy(isLoadingKatalog = false) }
            }
        }
    }

    // ─── Mode ─────────────────────────────────────────────

    fun setMode(mode: NotaPembelianMode) {
        _uiState.update { it.copy(mode = mode, submitError = null, submitSuccessNota = null) }
    }

    // ─── Form umum ─────────────────────────────────────────

    fun setTanggal(tanggal: String) = _uiState.update { it.copy(tanggal = tanggal) }
    fun setOutlet(outletId: Int?) = _uiState.update { it.copy(selectedOutletId = outletId) }
    fun setPihakTerkait(value: String) = _uiState.update { it.copy(pihakTerkait = value) }
    fun setMetodePembayaran(value: String) = _uiState.update { it.copy(metodePembayaran = value) }
    fun setCatatan(value: String) = _uiState.update { it.copy(catatan = value) }

    // ─── Katalog search ────────────────────────────────────

    fun onSearchQueryChange(query: String) {
        _uiState.update { state ->
            val filtered = state.katalog.filter { it.nama.contains(query, ignoreCase = true) }
            state.copy(searchQuery = query, filteredKatalog = filtered)
        }
    }

    // ─── Item entry (keranjang manual) ─────────────────────

    fun selectKatalogItem(produk: BarangJasaDto) {
        _uiState.update { state ->
            state.copy(
                itemKatalogId = produk.id,
                itemNama = produk.nama,
                itemSatuan = produk.satuan,
                itemHarga = formatDoubleInput(produk.hargaBeli ?: 0.0),
                itemKuantitas = "1"
            )
        }
    }

    fun updateItemNama(value: String) = _uiState.update { it.copy(itemNama = value) }

    fun updateItemKuantitas(value: String) {
        // Terima digit & satu titik desimal
        if (value.matches(Regex("""\d*\.?\d*"""))) {
            _uiState.update { it.copy(itemKuantitas = value) }
        }
    }

    fun updateItemSatuan(value: String) = _uiState.update { it.copy(itemSatuan = value) }

    fun updateItemHarga(value: String) {
        // Terima digit & satu titik desimal
        if (value.matches(Regex("""\d*\.?\d*"""))) {
            _uiState.update { it.copy(itemHarga = value) }
        }
    }

    fun addItemToCart() {
        val state = _uiState.value
        val qty = state.itemKuantitas.toDoubleOrNull()
        val harga = state.itemHarga.toDoubleOrNull()
        val nama = state.itemNama.trim()

        when {
            nama.isEmpty() -> _uiState.update { it.copy(submitError = "Nama item wajib diisi") }
            qty == null || qty <= 0 -> _uiState.update { it.copy(submitError = "Kuantitas harus angka lebih dari 0") }
            harga == null || harga < 0 -> _uiState.update { it.copy(submitError = "Harga satuan harus angka valid") }
            else -> {
                val item = PembelianCartItem(
                    namaItem = nama,
                    kuantitas = qty,
                    satuan = state.itemSatuan.ifBlank { "pcs" },
                    hargaSatuan = harga
                )
                _uiState.update { s ->
                    val newCart = s.cartItems + item
                    s.copy(
                        cartItems = newCart,
                        cartTotal = newCart.sumOf { it.subtotal },
                        submitError = null,
                        itemKatalogId = null,
                        itemNama = "",
                        itemKuantitas = "",
                        itemSatuan = "pcs",
                        itemHarga = ""
                    )
                }
            }
        }
    }

    fun removeCartItem(index: Int) {
        _uiState.update { state ->
            val newCart = state.cartItems.filterIndexed { i, _ -> i != index }
            state.copy(cartItems = newCart, cartTotal = newCart.sumOf { it.subtotal })
        }
    }

    fun clearCart() {
        _uiState.update { it.copy(cartItems = emptyList(), cartTotal = 0.0) }
    }

    // ─── Impor Excel ───────────────────────────────────────

    fun onFileSelected(context: Context, uri: Uri) {
        val fileName = uri.lastPathSegment?.substringAfterLast('/') ?: "nota_pembelian.xlsx"
        val sizeBytes = runCatching {
            context.contentResolver.openFileDescriptor(uri, "r")?.use { it.statSize } ?: 0L
        }.getOrDefault(0L)

        _uiState.update { it.copy(importFile = SelectedImportFile(uri, fileName, sizeBytes), submitError = null) }
    }

    fun clearImportFile() {
        _uiState.update { it.copy(importFile = null) }
    }

    // ─── Submit ────────────────────────────────────────────

    /** Simpan nota pembelian manual (POST /api/notas tipe=pembelian). */
    fun submitManual() {
        val state = _uiState.value
        if (state.cartItems.isEmpty()) {
            _uiState.update { it.copy(submitError = "Belum ada item di keranjang") }
            return
        }
        if (state.isSubmitting) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, submitError = null) }
            val request = CreateNotaRequest(
                tipe = "pembelian",
                tanggal = state.tanggal,
                outletId = state.selectedOutletId,
                pihakTerkait = state.pihakTerkait.ifBlank { null },
                metodePembayaran = state.metodePembayaran.ifBlank { null },
                catatan = state.catatan.ifBlank { null },
                items = state.cartItems.map { item ->
                    NotaItemRequest(
                        barangJasaId = null,
                        namaItem = item.namaItem,
                        jenis = "barang",
                        kuantitas = item.kuantitas,
                        satuan = item.satuan,
                        hargaSatuan = item.hargaSatuan
                    )
                }
            )
            kasirRepository.createNota(request)
                .onSuccess { nota ->
                    resetAfterSubmit()
                    _uiState.update { it.copy(isSubmitting = false, submitSuccessNota = nota) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isSubmitting = false, submitError = e.message ?: "Gagal menyimpan nota") }
                }
        }
    }

    /** Impor nota pembelian dari file .xlsx (POST /api/notas/import). */
    fun submitImport(context: Context) {
        val state = _uiState.value
        val file = state.importFile ?: run {
            _uiState.update { it.copy(submitError = "Pilih file Excel (.xlsx) terlebih dahulu") }
            return
        }
        if (state.isSubmitting) return

        if (!file.fileName.endsWith(".xlsx", ignoreCase = true)) {
            _uiState.update { it.copy(submitError = "File harus berformat .xlsx") }
            return
        }
        if (file.sizeBytes > 5L * 1024 * 1024) {
            _uiState.update { it.copy(submitError = "Ukuran file maksimal 5MB") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, submitError = null) }
            val bytes = runCatching {
                context.contentResolver.openInputStream(file.uri)?.use { it.readBytes() }
                    ?: throw IllegalStateException("Gagal membaca file")
            }

            bytes.fold(
                onSuccess = { data ->
                    kasirRepository.importNotaPembelian(
                        fileBytes = data,
                        fileName = file.fileName,
                        tanggal = state.tanggal,
                        outletId = state.selectedOutletId,
                        pihakTerkait = state.pihakTerkait.ifBlank { null },
                        metodePembayaran = state.metodePembayaran.ifBlank { null },
                        kategoriTransaksiId = null,
                        catatan = state.catatan.ifBlank { null }
                    ).onSuccess { nota ->
                        resetAfterSubmit()
                        _uiState.update { it.copy(isSubmitting = false, submitSuccessNota = nota) }
                    }.onFailure { e ->
                        _uiState.update { it.copy(isSubmitting = false, submitError = e.message ?: "Gagal mengimpor nota") }
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isSubmitting = false, submitError = e.message ?: "Gagal membaca file") }
                }
            )
        }
    }

    private fun resetAfterSubmit() {
        _uiState.update { state ->
            state.copy(
                cartItems = emptyList(),
                cartTotal = 0.0,
                importFile = null,
                itemKatalogId = null,
                itemNama = "",
                itemKuantitas = "",
                itemSatuan = "pcs",
                itemHarga = "",
                pihakTerkait = "",
                metodePembayaran = "",
                catatan = ""
            )
        }
    }

    // ─── Cleanup ───────────────────────────────────────────

    fun clearSubmitSuccess() {
        _uiState.update { it.copy(submitSuccessNota = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(loadError = null, submitError = null) }
    }

    private fun formatDoubleInput(value: Double): String {
        return if (value == value.toLong().toDouble()) {
            value.toLong().toString()
        } else {
            value.toString()
        }
    }
}
