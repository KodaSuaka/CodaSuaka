package com.example.codasuaka.ui.screen.laporan_keuangan

import android.content.ContentValues
import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.codasuaka.data.remote.dto.*
import com.example.codasuaka.domain.repository.KeuanganRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import java.io.IOException
import java.time.LocalDate
import com.example.codasuaka.ui.util.formatRupiah
import java.time.format.DateTimeFormatter

/**
 * State untuk halaman Laporan Keuangan (Buku Kas).
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

    // Detail Popup
    val selectedTransaksiDetail: TransaksiKasDto? = null,
    val showDetailDialog: Boolean = false,

    // Pagination
    val currentPage: Int = 1,
    val lastPage: Int = 1,
    val isLoadingMore: Boolean = false
)

class LaporanKeuanganViewModel(
    private val keuanganRepository: KeuanganRepository,
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(LaporanKeuanganUiState())
    val uiState: StateFlow<LaporanKeuanganUiState> = _uiState

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingTransaksi = true, isLoadingSaldo = true, isLoadingKategori = true) }
            try {
                coroutineScope {
                    val kategoriDeferred = async { keuanganRepository.getKategoriTransaksis(activeOnly = true) }
                    val transaksiDeferred = async {
                        keuanganRepository.getTransaksiKasList(
                            page = 1,
                            tipe = _uiState.value.filterTipe,
                            kategoriTransaksiId = _uiState.value.filterKategoriId,
                            startDate = _uiState.value.filterStartDate,
                            endDate = _uiState.value.filterEndDate,
                            perPage = 50
                        )
                    }
                    val saldoDeferred = async {
                        keuanganRepository.getSaldo(
                            startDate = _uiState.value.filterStartDate,
                            endDate = _uiState.value.filterEndDate
                        )
                    }

                    val kategoriResult = kategoriDeferred.await()
                    val transaksiResult = transaksiDeferred.await()
                    val saldoResult = saldoDeferred.await()

                    _uiState.update { state ->
                        state.copy(
                            kategoriList = kategoriResult.getOrDefault(emptyList()),
                            isLoadingKategori = false,
                            transaksiList = transaksiResult.getOrNull()?.first ?: emptyList(),
                            isLoadingTransaksi = false,
                            currentPage = transaksiResult.getOrNull()?.second?.currentPage ?: 1,
                            lastPage = transaksiResult.getOrNull()?.second?.lastPage ?: 1,
                            saldoData = saldoResult.getOrNull(),
                            isLoadingSaldo = false
                        )
                    }
                }
            } catch (_: Exception) {
                _uiState.update { it.copy(isLoadingTransaksi = false, isLoadingSaldo = false, isLoadingKategori = false) }
            }
        }
    }

    fun loadKategoriTransaksis() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingKategori = true) }
            keuanganRepository.getKategoriTransaksis(activeOnly = true)
                .onSuccess { list ->
                    _uiState.update { it.copy(kategoriList = list, isLoadingKategori = false) }
                }
                .onFailure { _uiState.update { it.copy(isLoadingKategori = false) } }
        }
    }

    fun loadTransaksiKas(page: Int = 1) {
        viewModelScope.launch {
            if (page == 1) {
                _uiState.update { it.copy(isLoadingTransaksi = true, transaksiError = null) }
            } else {
                _uiState.update { it.copy(isLoadingMore = true) }
            }

            keuanganRepository.getTransaksiKasList(
                page = page,
                tipe = _uiState.value.filterTipe,
                kategoriTransaksiId = _uiState.value.filterKategoriId,
                startDate = _uiState.value.filterStartDate,
                endDate = _uiState.value.filterEndDate,
                perPage = 50
            ).onSuccess { (list, meta) ->
                _uiState.update { it.copy(
                    transaksiList = if (page == 1) list else it.transaksiList + list,
                    isLoadingTransaksi = false,
                    isLoadingMore = false,
                    currentPage = meta?.currentPage ?: 1,
                    lastPage = meta?.lastPage ?: 1
                ) }
            }.onFailure { e ->
                _uiState.update { it.copy(
                    isLoadingTransaksi = false,
                    isLoadingMore = false,
                    transaksiError = e.message ?: "Gagal memuat transaksi"
                ) }
            }
        }
    }

    fun refreshTransaksi() {
        loadTransaksiKas(page = 1)
        loadSaldo()
    }

    fun setFilterTipe(tipe: String?) {
        _uiState.update { it.copy(filterTipe = tipe) }
        loadTransaksiKas(page = 1)
    }

    fun setFilterKategoriId(kategoriId: Int?) {
        _uiState.update { it.copy(filterKategoriId = kategoriId) }
        loadTransaksiKas(page = 1)
    }

    fun setFilterDateRange(startDate: String, endDate: String) {
        _uiState.update { it.copy(filterStartDate = startDate, filterEndDate = endDate) }
        loadTransaksiKas(page = 1)
        loadSaldo()
    }

    fun loadSaldo() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingSaldo = true, saldoError = null) }
            keuanganRepository.getSaldo(
                startDate = _uiState.value.filterStartDate,
                endDate = _uiState.value.filterEndDate
            ).onSuccess { data ->
                _uiState.update { it.copy(saldoData = data, isLoadingSaldo = false) }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoadingSaldo = false, saldoError = e.message ?: "Gagal memuat saldo") }
            }
        }
    }

    fun loadLabaRugi() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingLabaRugi = true, labaRugiError = null) }
            keuanganRepository.getLabaRugi(
                startDate = _uiState.value.filterStartDate,
                endDate = _uiState.value.filterEndDate
            ).onSuccess { data ->
                _uiState.update { it.copy(labaRugiData = data, isLoadingLabaRugi = false) }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoadingLabaRugi = false, labaRugiError = e.message ?: "Gagal memuat laba rugi") }
            }
        }
    }

    fun showAddForm(tipe: String = "masuk") {
        _uiState.update { it.copy(
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
        ) }
    }

    fun showEditForm(transaksi: TransaksiKasDto) {
        _uiState.update { it.copy(
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
        ) }
    }

    fun hideForm() {
        _uiState.update { it.copy(showFormDialog = false) }
    }

    fun updateFormField(
        tipe: String? = null,
        nominal: String? = null,
        kategoriId: Int? = null,
        tanggal: String? = null,
        metodePembayaran: String? = null,
        keterangan: String? = null
    ) {
        _uiState.update { current ->
            current.copy(
                formTipe = tipe ?: current.formTipe,
                formNominal = nominal ?: current.formNominal,
                formKategoriId = kategoriId ?: current.formKategoriId,
                formTanggal = tanggal ?: current.formTanggal,
                formMetodePembayaran = metodePembayaran ?: current.formMetodePembayaran,
                formKeterangan = keterangan ?: current.formKeterangan
            )
        }
    }

    fun submitForm() {
        val state = _uiState.value
        val nominal = state.formNominal.replace(".", "").replace(",", ".").toDoubleOrNull()
        if (nominal == null || nominal <= 0) {
            _uiState.update { it.copy(submitError = "Nominal harus diisi dengan angka valid") }
            return
        }
        if (state.formKategoriId == null) {
            _uiState.update { it.copy(submitError = "Pilih kategori transaksi") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, submitError = null) }
            if (state.isEditing && state.editingTransaksiId != null) {
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
                        _uiState.update { it.copy(isSubmitting = false, showFormDialog = false, submitSuccess = "Transaksi berhasil diperbarui") }
                        refreshTransaksi()
                    }
                    .onFailure { e -> _uiState.update { it.copy(isSubmitting = false, submitError = e.message ?: "Gagal memperbarui transaksi") } }
            } else {
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
                        _uiState.update { it.copy(isSubmitting = false, showFormDialog = false, submitSuccess = "Transaksi berhasil ditambahkan") }
                        refreshTransaksi()
                    }
                    .onFailure { e -> _uiState.update { it.copy(isSubmitting = false, submitError = e.message ?: "Gagal menambah transaksi") } }
            }
        }
    }

    fun deleteTransaksi(id: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingTransaksi = true) }
            keuanganRepository.deleteTransaksiKas(id)
                .onSuccess {
                    _uiState.update { it.copy(isLoadingTransaksi = false, submitSuccess = "Transaksi berhasil dihapus") }
                    refreshTransaksi()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoadingTransaksi = false, transaksiError = e.message ?: "Gagal menghapus transaksi") }
                }
        }
    }

    fun ajukanApproval(transaksiId: Int) {
        viewModelScope.launch {
            keuanganRepository.ajukanApproval(transaksiId)
                .onSuccess {
                    _uiState.update { it.copy(submitSuccess = "Approval berhasil diajukan") }
                    refreshTransaksi()
                }
                .onFailure { e -> _uiState.update { it.copy(transaksiError = e.message ?: "Gagal mengajukan approval") } }
        }
    }

    fun toggleSaldoSheet() {
        _uiState.update { it.copy(showSaldoSheet = !it.showSaldoSheet) }
        if (_uiState.value.showSaldoSheet) loadSaldo()
    }

    fun toggleLabaRugiSheet() {
        _uiState.update { it.copy(showLabaRugiSheet = !it.showLabaRugiSheet) }
        if (_uiState.value.showLabaRugiSheet) loadLabaRugi()
    }

    fun loadArusKas() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingArusKas = true, arusKasError = null) }
            keuanganRepository.getArusKas(
                startDate = _uiState.value.filterStartDate,
                endDate = _uiState.value.filterEndDate
            ).onSuccess { data -> _uiState.update { it.copy(arusKasData = data, isLoadingArusKas = false) } }
            .onFailure { e -> _uiState.update { it.copy(isLoadingArusKas = false, arusKasError = e.message ?: "Gagal memuat arus kas") } }
        }
    }

    fun toggleArusKasSheet() {
        _uiState.update { it.copy(showArusKasSheet = !it.showArusKasSheet) }
        if (_uiState.value.showArusKasSheet) loadArusKas()
    }

    fun loadRingkasanKeuangan(tahun: Int? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingRingkasan = true, ringkasanKeuanganError = null) }
            keuanganRepository.getRingkasanKeuangan(tahun)
                .onSuccess { data -> _uiState.update { it.copy(ringkasanKeuanganData = data, isLoadingRingkasan = false) } }
                .onFailure { e -> _uiState.update { it.copy(isLoadingRingkasan = false, ringkasanKeuanganError = e.message ?: "Gagal memuat ringkasan keuangan") } }
        }
    }

    fun exportBukuKasPdf() {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, exportError = null) }
            keuanganRepository.exportBukuKasPdf(
                startDate = _uiState.value.filterStartDate,
                endDate = _uiState.value.filterEndDate
            ).onSuccess { body -> saveFile(body, "buku_kas_${_uiState.value.filterStartDate}_${_uiState.value.filterEndDate}.pdf") }
            .onFailure { e -> _uiState.update { it.copy(isExporting = false, exportError = e.message ?: "Gagal mengekspor PDF buku kas") } }
        }
    }

    fun exportBukuKasExcel() {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, exportError = null) }
            keuanganRepository.exportBukuKasExcel(
                startDate = _uiState.value.filterStartDate,
                endDate = _uiState.value.filterEndDate
            ).onSuccess { body -> saveFile(body, "buku_kas_${_uiState.value.filterStartDate}_${_uiState.value.filterEndDate}.xlsx") }
            .onFailure { e -> _uiState.update { it.copy(isExporting = false, exportError = e.message ?: "Gagal mengekspor Excel buku kas") } }
        }
    }

    fun exportLabaRugiPdf() {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, exportError = null) }
            keuanganRepository.exportLabaRugiPdf(
                startDate = _uiState.value.filterStartDate,
                endDate = _uiState.value.filterEndDate
            ).onSuccess { body -> saveFile(body, "laba_rugi_${_uiState.value.filterStartDate}_${_uiState.value.filterEndDate}.pdf") }
            .onFailure { e -> _uiState.update { it.copy(isExporting = false, exportError = e.message ?: "Gagal mengekspor PDF laba rugi") } }
        }
    }

    fun exportArusKasPdf() {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, exportError = null) }
            keuanganRepository.exportArusKasPdf(
                startDate = _uiState.value.filterStartDate,
                endDate = _uiState.value.filterEndDate
            ).onSuccess { body -> saveFile(body, "arus_kas_${_uiState.value.filterStartDate}_${_uiState.value.filterEndDate}.pdf") }
            .onFailure { e -> _uiState.update { it.copy(isExporting = false, exportError = e.message ?: "Gagal mengekspor PDF arus kas") } }
        }
    }

    fun exportArusKasExcel() {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, exportError = null) }
            keuanganRepository.exportArusKasExcel(
                startDate = _uiState.value.filterStartDate,
                endDate = _uiState.value.filterEndDate
            ).onSuccess { body -> saveFile(body, "arus_kas_${_uiState.value.filterStartDate}_${_uiState.value.filterEndDate}.xlsx") }
            .onFailure { e -> _uiState.update { it.copy(isExporting = false, exportError = e.message ?: "Gagal mengekspor Excel arus kas") } }
        }
    }

    private fun saveFile(body: ResponseBody, filename: String) {
        try {
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/octet-stream")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                ?: throw IOException("Gagal membuat entri file di Downloads")
            resolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(body.bytes())
            } ?: throw IOException("Gagal membuka output stream")
            _uiState.value = _uiState.value.copy(
                isExporting = false,
                exportSuccessPath = filename
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isExporting = false,
                exportError = "Gagal menyimpan file: ${e.message}"
            )
        }
    }

    fun clearExportSuccess() {
        _uiState.update { it.copy(exportSuccessPath = null) }
    }

    fun clearSubmitSuccess() {
        _uiState.update { it.copy(submitSuccess = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(transaksiError = null, saldoError = null, labaRugiError = null, submitError = null) }
    }

    // ─── Detail Popup ───────────────────────────────────────────

    fun showDetail(transaksi: TransaksiKasDto) {
        _uiState.update { it.copy(
            selectedTransaksiDetail = transaksi,
            showDetailDialog = true
        ) }
    }

    fun hideDetail() {
        _uiState.update { it.copy(
            selectedTransaksiDetail = null,
            showDetailDialog = false
        ) }
    }

    private fun formatNominalForEdit(nominal: Double): String {
        return if (nominal == nominal.toLong().toDouble()) {
            nominal.toLong().toString()
        } else {
            nominal.toString()
        }
    }

    companion object {
        /** Delegasi ke util terpusat (FormatUtil) agar format konsisten di semua layar. */
        fun formatRupiah(amount: Double): String = com.example.codasuaka.ui.util.formatRupiah(amount)
    }
}
