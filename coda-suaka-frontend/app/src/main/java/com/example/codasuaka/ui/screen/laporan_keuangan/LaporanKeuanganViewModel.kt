package com.example.codasuaka.ui.screen.laporan_keuangan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.codasuaka.data.remote.dto.*
import com.example.codasuaka.domain.repository.KeuanganRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * State untuk halaman Laporan Keuangan (Buku Kas).
 *
 * Metode pembayaran yang valid untuk form.
 */
private val VALID_METODE_PEMBAYARAN = listOf(
    "Tunai", "Transfer", "QRIS", "Kartu Kredit", "Kartu Debit", "Lainnya"
)

data class LaporanKeuanganUiState(
    // Daftar transaksi
    val transaksiList: List<TransaksiKasDto> = emptyList(),
    val isLoadingTransaksi: Boolean = false,
    val transaksiError: String? = null,

    // Saldo
    val saldoData: SaldoData? = null,
    val isLoadingSaldo: Boolean = false,
    val saldoError: String? = null,

    // Laba Rugi
    val labaRugiData: LabaRugiData? = null,
    val isLoadingLabaRugi: Boolean = false,
    val labaRugiError: String? = null,

    // Arus Kas
    val arusKasData: ArusKasData? = null,
    val isLoadingArusKas: Boolean = false,
    val arusKasError: String? = null,

    // Ringkasan Keuangan
    val ringkasanKeuanganData: RingkasanKeuanganData? = null,
    val isLoadingRingkasan: Boolean = false,
    val ringkasanKeuanganError: String? = null,

    // Ekspor
    val isExporting: Boolean = false,
    val exportError: String? = null,
    val exportSuccessPath: String? = null,

    // Kategori
    val kategoriList: List<KategoriTransaksiDto> = emptyList(),
    val isLoadingKategori: Boolean = false,

    // Filter
    val filterTipe: String? = null, // null = semua, "masuk", "keluar"
    val filterKategoriId: Int? = null,
    val filterStartDate: String = LocalDate.now().withDayOfMonth(1).format(DateTimeFormatter.ISO_LOCAL_DATE),
    val filterEndDate: String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),

    // Dialog / Bottom Sheet
    val showFormDialog: Boolean = false,
    val isEditing: Boolean = false,
    val editingTransaksiId: Int? = null,
    val formTipe: String = "masuk",
    val formNominal: String = "",
    val formKategoriId: Int? = null,
    val formTanggal: String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
    val formMetodePembayaran: String = "",
    val formKeterangan: String = "",
    val isSubmitting: Boolean = false,
    val submitError: String? = null,
    val submitSuccess: String? = null,

    // Bottom sheet saldo / laba rugi / arus kas
    val showSaldoSheet: Boolean = false,
    val showLabaRugiSheet: Boolean = false,
    val showArusKasSheet: Boolean = false,

    // Pagination
    val currentPage: Int = 1,
    val lastPage: Int = 1,
    val isLoadingMore: Boolean = false
)

class LaporanKeuanganViewModel(
    private val keuanganRepository: KeuanganRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LaporanKeuanganUiState())
    val uiState: StateFlow<LaporanKeuanganUiState> = _uiState

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        loadKategoriTransaksis()
        loadTransaksiKas()
        loadSaldo()
    }

    // ─── Kategori ────────────────────────────────────────────────

    fun loadKategoriTransaksis() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingKategori = true)
            keuanganRepository.getKategoriTransaksis(activeOnly = true)
                .onSuccess { list ->
                    _uiState.value = _uiState.value.copy(
                        kategoriList = list,
                        isLoadingKategori = false
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoadingKategori = false
                    )
                }
        }
    }

    // ─── Transaksi Kas ───────────────────────────────────────────

    fun loadTransaksiKas(page: Int = 1) {
        viewModelScope.launch {
            if (page == 1) {
                _uiState.value = _uiState.value.copy(
                    isLoadingTransaksi = true,
                    transaksiError = null
                )
            } else {
                _uiState.value = _uiState.value.copy(isLoadingMore = true)
            }

            keuanganRepository.getTransaksiKasList(
                page = page,
                tipe = _uiState.value.filterTipe,
                kategoriTransaksiId = _uiState.value.filterKategoriId,
                startDate = _uiState.value.filterStartDate,
                endDate = _uiState.value.filterEndDate,
                perPage = 50
            ).onSuccess { (list, meta) ->
                _uiState.value = _uiState.value.copy(
                    transaksiList = if (page == 1) list else _uiState.value.transaksiList + list,
                    isLoadingTransaksi = false,
                    isLoadingMore = false,
                    currentPage = meta?.currentPage ?: 1,
                    lastPage = meta?.lastPage ?: 1
                )
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isLoadingTransaksi = false,
                    isLoadingMore = false,
                    transaksiError = e.message ?: "Gagal memuat transaksi"
                )
            }
        }
    }

    fun refreshTransaksi() {
        loadTransaksiKas(page = 1)
        loadSaldo()
    }

    // ─── Filter ──────────────────────────────────────────────────

    fun setFilterTipe(tipe: String?) {
        _uiState.value = _uiState.value.copy(filterTipe = tipe)
        loadTransaksiKas(page = 1)
    }

    fun setFilterKategoriId(kategoriId: Int?) {
        _uiState.value = _uiState.value.copy(filterKategoriId = kategoriId)
        loadTransaksiKas(page = 1)
    }

    fun setFilterDateRange(startDate: String, endDate: String) {
        _uiState.value = _uiState.value.copy(
            filterStartDate = startDate,
            filterEndDate = endDate
        )
        loadTransaksiKas(page = 1)
        loadSaldo()
    }

    // ─── Saldo ───────────────────────────────────────────────────

    fun loadSaldo() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingSaldo = true, saldoError = null)
            keuanganRepository.getSaldo(
                startDate = _uiState.value.filterStartDate,
                endDate = _uiState.value.filterEndDate
            ).onSuccess { data ->
                _uiState.value = _uiState.value.copy(
                    saldoData = data,
                    isLoadingSaldo = false
                )
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isLoadingSaldo = false,
                    saldoError = e.message ?: "Gagal memuat saldo"
                )
            }
        }
    }

    // ─── Laba Rugi ───────────────────────────────────────────────

    fun loadLabaRugi() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingLabaRugi = true, labaRugiError = null)
            keuanganRepository.getLabaRugi(
                startDate = _uiState.value.filterStartDate,
                endDate = _uiState.value.filterEndDate
            ).onSuccess { data ->
                _uiState.value = _uiState.value.copy(
                    labaRugiData = data,
                    isLoadingLabaRugi = false
                )
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isLoadingLabaRugi = false,
                    labaRugiError = e.message ?: "Gagal memuat laba rugi"
                )
            }
        }
    }

    // ─── Dialog / Form ───────────────────────────────────────────

    fun showAddForm(tipe: String = "masuk") {
        _uiState.value = _uiState.value.copy(
            showFormDialog = true,
            isEditing = false,
            editingTransaksiId = null,
            formTipe = tipe,
            formNominal = "",
            formKategoriId = null,
            formTanggal = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
            formMetodePembayaran = "",
            formKeterangan = "",
            submitError = null,
            submitSuccess = null
        )
    }

    fun showEditForm(transaksi: TransaksiKasDto) {
        _uiState.value = _uiState.value.copy(
            showFormDialog = true,
            isEditing = true,
            editingTransaksiId = transaksi.id,
            formTipe = transaksi.tipe,
            formNominal = formatNominalForEdit(transaksi.nominal),
            formKategoriId = transaksi.kategoriTransaksiId,
            formTanggal = transaksi.tanggal,
            formMetodePembayaran = transaksi.metodePembayaran ?: "",
            formKeterangan = transaksi.keterangan ?: "",
            submitError = null,
            submitSuccess = null
        )
    }

    fun hideForm() {
        _uiState.value = _uiState.value.copy(showFormDialog = false)
    }

    fun updateFormField(
        tipe: String? = null,
        nominal: String? = null,
        kategoriId: Int? = null,
        tanggal: String? = null,
        metodePembayaran: String? = null,
        keterangan: String? = null
    ) {
        val current = _uiState.value
        _uiState.value = current.copy(
            formTipe = tipe ?: current.formTipe,
            formNominal = nominal ?: current.formNominal,
            formKategoriId = kategoriId ?: current.formKategoriId,
            formTanggal = tanggal ?: current.formTanggal,
            formMetodePembayaran = metodePembayaran ?: current.formMetodePembayaran,
            formKeterangan = keterangan ?: current.formKeterangan
        )
    }

    fun submitForm() {
        val state = _uiState.value

        // Validasi nominal
        val nominal = state.formNominal.replace(".", "").replace(",", ".").toDoubleOrNull()
        if (nominal == null || nominal <= 0) {
            _uiState.value = _uiState.value.copy(submitError = "Nominal harus diisi dengan angka valid")
            return
        }
        if (nominal > 999999999999.99) {
            _uiState.value = _uiState.value.copy(submitError = "Nominal melebihi batas maksimum")
            return
        }

        // Validasi kategori
        if (state.formKategoriId == null) {
            _uiState.value = _uiState.value.copy(submitError = "Pilih kategori transaksi")
            return
        }

        // Validasi tanggal
        try {
            val tanggal = LocalDate.parse(state.formTanggal, DateTimeFormatter.ISO_LOCAL_DATE)
            if (tanggal.isAfter(LocalDate.now())) {
                _uiState.value = _uiState.value.copy(submitError = "Tanggal tidak boleh melebihi hari ini")
                return
            }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(submitError = "Format tanggal tidak valid")
            return
        }

        // Validasi metode pembayaran (jika diisi)
        if (state.formMetodePembayaran.isNotBlank() &&
            state.formMetodePembayaran !in VALID_METODE_PEMBAYARAN
        ) {
            _uiState.value = _uiState.value.copy(submitError = "Metode pembayaran tidak valid")
            return
        }

        // Validasi keterangan (max 1000 karakter)
        if (state.formKeterangan.length > 1000) {
            _uiState.value = _uiState.value.copy(submitError = "Keterangan maksimal 1000 karakter")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, submitError = null)

            if (state.isEditing && state.editingTransaksiId != null) {
                // Update
                val request = UpdateTransaksiKasRequest(
                    tanggal = state.formTanggal,
                    tipe = state.formTipe,
                    nominal = nominal,
                    kategoriTransaksiId = state.formKategoriId,
                    metodePembayaran = state.formMetodePembayaran.ifBlank { null },
                    keterangan = state.formKeterangan.ifBlank { null }
                )
                keuanganRepository.updateTransaksiKas(state.editingTransaksiId, request)
                    .onSuccess {
                        _uiState.value = _uiState.value.copy(
                            isSubmitting = false,
                            showFormDialog = false,
                            submitSuccess = "Transaksi berhasil diperbarui"
                        )
                        refreshTransaksi()
                    }
                    .onFailure { e ->
                        _uiState.value = _uiState.value.copy(
                            isSubmitting = false,
                            submitError = e.message ?: "Gagal memperbarui transaksi"
                        )
                    }
            } else {
                // Create
                val request = CreateTransaksiKasRequest(
                    tanggal = state.formTanggal,
                    tipe = state.formTipe,
                    nominal = nominal,
                    kategoriTransaksiId = state.formKategoriId,
                    metodePembayaran = state.formMetodePembayaran.ifBlank { null },
                    keterangan = state.formKeterangan.ifBlank { null }
                )
                keuanganRepository.createTransaksiKas(request)
                    .onSuccess {
                        _uiState.value = _uiState.value.copy(
                            isSubmitting = false,
                            showFormDialog = false,
                            submitSuccess = "Transaksi berhasil ditambahkan"
                        )
                        refreshTransaksi()
                    }
                    .onFailure { e ->
                        _uiState.value = _uiState.value.copy(
                            isSubmitting = false,
                            submitError = e.message ?: "Gagal menambah transaksi"
                        )
                    }
            }
        }
    }

    fun deleteTransaksi(id: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingTransaksi = true)
            keuanganRepository.deleteTransaksiKas(id)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        submitSuccess = "Transaksi berhasil dihapus"
                    )
                    refreshTransaksi()
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoadingTransaksi = false,
                        transaksiError = e.message ?: "Gagal menghapus transaksi"
                    )
                }
        }
    }

    // ─── Approval ────────────────────────────────────────────────

    fun ajukanApproval(transaksiId: Int) {
        viewModelScope.launch {
            keuanganRepository.ajukanApproval(transaksiId)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        submitSuccess = "Approval berhasil diajukan"
                    )
                    refreshTransaksi()
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        transaksiError = e.message ?: "Gagal mengajukan approval"
                    )
                }
        }
    }

    // ─── Bottom Sheet Toggle ─────────────────────────────────────

    fun toggleSaldoSheet() {
        _uiState.value = _uiState.value.copy(
            showSaldoSheet = !_uiState.value.showSaldoSheet
        )
        if (_uiState.value.showSaldoSheet) {
            loadSaldo()
        }
    }

    fun toggleLabaRugiSheet() {
        _uiState.value = _uiState.value.copy(
            showLabaRugiSheet = !_uiState.value.showLabaRugiSheet
        )
        if (_uiState.value.showLabaRugiSheet) {
            loadLabaRugi()
        }
    }

    // ─── Arus Kas ─────────────────────────────────────────────────

    fun loadArusKas() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingArusKas = true, arusKasError = null)
            keuanganRepository.getArusKas(
                startDate = _uiState.value.filterStartDate,
                endDate = _uiState.value.filterEndDate
            ).onSuccess { data ->
                _uiState.value = _uiState.value.copy(
                    arusKasData = data,
                    isLoadingArusKas = false
                )
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isLoadingArusKas = false,
                    arusKasError = e.message ?: "Gagal memuat arus kas"
                )
            }
        }
    }

    fun toggleArusKasSheet() {
        _uiState.value = _uiState.value.copy(
            showArusKasSheet = !_uiState.value.showArusKasSheet
        )
        if (_uiState.value.showArusKasSheet) {
            loadArusKas()
        }
    }

    // ─── Ringkasan Keuangan ───────────────────────────────────────

    fun loadRingkasanKeuangan(tahun: Int? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingRingkasan = true, ringkasanKeuanganError = null)
            keuanganRepository.getRingkasanKeuangan(tahun)
                .onSuccess { data ->
                    _uiState.value = _uiState.value.copy(
                        ringkasanKeuanganData = data,
                        isLoadingRingkasan = false
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoadingRingkasan = false,
                        ringkasanKeuanganError = e.message ?: "Gagal memuat ringkasan keuangan"
                    )
                }
        }
    }

    // ─── Ekspor ───────────────────────────────────────────────────

    fun exportBukuKasPdf() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true, exportError = null)
            keuanganRepository.exportBukuKasPdf(
                startDate = _uiState.value.filterStartDate,
                endDate = _uiState.value.filterEndDate
            ).onSuccess { body ->
                saveFile(body, "buku_kas_${_uiState.value.filterStartDate}_${_uiState.value.filterEndDate}.pdf")
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    exportError = e.message ?: "Gagal mengekspor PDF buku kas"
                )
            }
        }
    }

    fun exportBukuKasExcel() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true, exportError = null)
            keuanganRepository.exportBukuKasExcel(
                startDate = _uiState.value.filterStartDate,
                endDate = _uiState.value.filterEndDate
            ).onSuccess { body ->
                saveFile(body, "buku_kas_${_uiState.value.filterStartDate}_${_uiState.value.filterEndDate}.xlsx")
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    exportError = e.message ?: "Gagal mengekspor Excel buku kas"
                )
            }
        }
    }

    fun exportLabaRugiPdf() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true, exportError = null)
            keuanganRepository.exportLabaRugiPdf(
                startDate = _uiState.value.filterStartDate,
                endDate = _uiState.value.filterEndDate
            ).onSuccess { body ->
                saveFile(body, "laba_rugi_${_uiState.value.filterStartDate}_${_uiState.value.filterEndDate}.pdf")
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    exportError = e.message ?: "Gagal mengekspor PDF laba rugi"
                )
            }
        }
    }

    fun exportArusKasPdf() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true, exportError = null)
            keuanganRepository.exportArusKasPdf(
                startDate = _uiState.value.filterStartDate,
                endDate = _uiState.value.filterEndDate
            ).onSuccess { body ->
                saveFile(body, "arus_kas_${_uiState.value.filterStartDate}_${_uiState.value.filterEndDate}.pdf")
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    exportError = e.message ?: "Gagal mengekspor PDF arus kas"
                )
            }
        }
    }

    fun exportArusKasExcel() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true, exportError = null)
            keuanganRepository.exportArusKasExcel(
                startDate = _uiState.value.filterStartDate,
                endDate = _uiState.value.filterEndDate
            ).onSuccess { body ->
                saveFile(body, "arus_kas_${_uiState.value.filterStartDate}_${_uiState.value.filterEndDate}.xlsx")
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    exportError = e.message ?: "Gagal mengekspor Excel arus kas"
                )
            }
        }
    }

    private fun saveFile(body: ResponseBody, filename: String) {
        try {
            val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(
                android.os.Environment.DIRECTORY_DOWNLOADS
            )
            if (!downloadsDir.exists()) downloadsDir.mkdirs()
            val file = File(downloadsDir, filename)
            FileOutputStream(file).use { outputStream ->
                outputStream.write(body.bytes())
            }
            _uiState.value = _uiState.value.copy(
                isExporting = false,
                exportSuccessPath = file.absolutePath
            )
        } catch (e: Exception) {
            // Fallback: simpan di cache
            try {
                val cacheDir = java.io.File(
                    android.os.Environment.getExternalStorageDirectory(),
                    "Android/data/com.example.codasuaka/cache"
                )
                if (!cacheDir.exists()) cacheDir.mkdirs()
                val file = File(cacheDir, filename)
                FileOutputStream(file).use { outputStream ->
                    outputStream.write(body.bytes())
                }
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    exportSuccessPath = file.absolutePath
                )
            } catch (e2: Exception) {
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    exportError = "Gagal menyimpan file: ${e2.message}"
                )
            }
        }
    }

    fun clearExportSuccess() {
        _uiState.value = _uiState.value.copy(exportSuccessPath = null)
    }

    // ─── Utility ─────────────────────────────────────────────────

    fun clearSubmitSuccess() {
        _uiState.value = _uiState.value.copy(submitSuccess = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(
            transaksiError = null,
            saldoError = null,
            labaRugiError = null,
            submitError = null
        )
    }

    private fun formatNominalForEdit(nominal: Double): String {
        // Hilangkan desimal .00 jika tidak ada sen
        return if (nominal == nominal.toLong().toDouble()) {
            nominal.toLong().toString()
        } else {
            nominal.toString()
        }
    }

    companion object {
        fun formatRupiah(amount: Double): String {
            val isNegative = amount < 0
            val absStr = kotlin.math.abs(amount).toLong().toString()
            val sb = StringBuilder()
            var count = 0
            for (i in absStr.lastIndex downTo 0) {
                if (count > 0 && count % 3 == 0) sb.insert(0, '.')
                sb.insert(0, absStr[i])
                count++
            }
            val prefix = if (isNegative) "-Rp " else "Rp "
            return "$prefix$sb"
        }
    }
}
