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
import com.example.codasuaka.domain.repository.StokRepository
import com.example.codasuaka.data.remote.dto.OutletDto
import com.example.codasuaka.data.remote.dto.StokDto
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
    val jenis: String,
    val barangJasaId: Int?,
    // Barang produksi → diarahkan ke tabel Stok. Salah satu dari barangJasaId/stokId
    // yang terisi; keduanya null = item lepas (tidak menambah stok mana pun).
    val stokId: Int? = null,
    val kuantitas: Double,
    val satuan: String,
    val hargaSatuan: Double
) {
    val subtotal: Double get() = kuantitas * hargaSatuan

    /** true bila item ini barang produksi (menunjuk Stok). */
    val isProduksi: Boolean get() = stokId != null
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
    val isHeaderExpanded: Boolean = false,

    // Pencarian katalog & Bottom Sheets
    val searchQuery: String = "",
    val filteredKatalog: List<BarangJasaDto> = emptyList(),
    val isKatalogOpen: Boolean = false,
    val isManualInputOpen: Boolean = false,
    val isQtyPromptOpen: Boolean = false,

    // Katalog barang produksi (Stok)
    val stokKatalog: List<StokDto> = emptyList(),
    val filteredStok: List<StokDto> = emptyList(),
    val isStokKatalogOpen: Boolean = false,

    // Keranjang manual
    val cartItems: List<PembelianCartItem> = emptyList(),
    val cartTotal: Double = 0.0,

    // Field item yang sedang diinput
    val itemKatalogId: Int? = null,
    val itemStokId: Int? = null,
    val itemNama: String = "",
    val itemJenis: String = "barang",
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
    private val outletRepository: OutletRepository,
    private val stokRepository: StokRepository
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
                    val stokDeferred = async { stokRepository.getStokList(isActive = true) }

                    val katalogResult = katalogDeferred.await()
                    val outletResult = outletDeferred.await()
                    val stokList = stokDeferred.await().getOrNull()?.first ?: emptyList()

                    val katalog = katalogResult.getOrNull() ?: emptyList()
                    _uiState.update { state ->
                        state.copy(
                            katalog = katalog,
                            filteredKatalog = katalog,
                            stokKatalog = stokList,
                            filteredStok = stokList,
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
    fun toggleHeader(expanded: Boolean) = _uiState.update { it.copy(isHeaderExpanded = expanded) }

    // ─── Katalog & Manual Search ───────────────────────────

    fun toggleKatalog(open: Boolean) {
        _uiState.update { it.copy(isKatalogOpen = open, searchQuery = "", filteredKatalog = it.katalog) }
    }

    fun toggleStokKatalog(open: Boolean) {
        _uiState.update { it.copy(isStokKatalogOpen = open, searchQuery = "", filteredStok = it.stokKatalog) }
    }

    fun toggleManualInput(open: Boolean) {
        _uiState.update { 
            it.copy(
                isManualInputOpen = open,
                itemNama = if (open) "" else it.itemNama,
                itemHarga = if (open) "" else it.itemHarga,
                itemKuantitas = if (open) "" else it.itemKuantitas
            ) 
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { state ->
            state.copy(
                searchQuery = query,
                filteredKatalog = state.katalog.filter { it.nama.contains(query, ignoreCase = true) },
                filteredStok = state.stokKatalog.filter { it.nama.contains(query, ignoreCase = true) },
            )
        }
    }

    // ─── Item entry (keranjang manual) ─────────────────────

    fun selectKatalogItem(produk: BarangJasaDto) {
        _uiState.update { state ->
            state.copy(
                itemKatalogId = produk.id,
                itemStokId = null,
                itemNama = produk.nama,
                itemJenis = produk.jenis,
                itemSatuan = produk.satuan,
                itemHarga = (produk.hargaBeli ?: 0.0).let { if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString() },
                itemKuantitas = "1",
                isKatalogOpen = false,
                isQtyPromptOpen = true // Buka prompt jumlah setelah pilih produk
            )
        }
    }

    /** Pilih item barang produksi dari katalog Stok → diarahkan ke tabel Stok. */
    fun selectStokItem(stok: StokDto) {
        _uiState.update { state ->
            state.copy(
                itemStokId = stok.id,
                itemKatalogId = null,
                itemNama = stok.nama,
                itemJenis = "barang",
                itemSatuan = stok.satuan,
                itemHarga = (stok.hargaBeli ?: 0.0).let { if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString() },
                itemKuantitas = "1",
                isStokKatalogOpen = false,
                isQtyPromptOpen = true
            )
        }
    }

    fun closeQtyPrompt() {
        _uiState.update { it.copy(isQtyPromptOpen = false) }
    }

    fun updateItemNama(value: String) = _uiState.update { it.copy(itemNama = value) }

    fun updateItemJenis(value: String) = _uiState.update { it.copy(itemJenis = value) }

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
                    jenis = state.itemJenis,
                    barangJasaId = state.itemKatalogId,
                    stokId = state.itemStokId,
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
                        itemStokId = null,
                        itemNama = "",
                        itemJenis = "barang",
                        itemKuantitas = "",
                        itemSatuan = "pcs",
                        itemHarga = "",
                        isManualInputOpen = false, // Tutup sheet manual
                        isQtyPromptOpen = false    // Tutup prompt qty
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
                        barangJasaId = item.barangJasaId,
                        stokId = item.stokId,
                        namaItem = item.namaItem,
                        jenis = item.jenis,
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
                itemJenis = "barang",
                itemKuantitas = "",
                itemSatuan = "pcs",
                itemHarga = "",
                pihakTerkait = "",
                metodePembayaran = "",
                catatan = ""
            )
        }
        // Reload katalog agar stok terbaru terlihat setelah submit pembelian
        loadInitialData()
    }

    // ─── Cleanup ───────────────────────────────────────────

    fun clearSubmitSuccess() {
        _uiState.update { it.copy(submitSuccessNota = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(loadError = null, submitError = null) }
    }
}
