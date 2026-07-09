package com.example.codasuaka.ui.screen.laporan_keuangan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.codasuaka.data.remote.dto.*
import com.example.codasuaka.domain.repository.KeuanganRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * State untuk halaman Laporan Keuangan (Buku Kas).
 */
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

    // Bottom sheet saldo / laba rugi
    val showSaldoSheet: Boolean = false,
    val showLabaRugiSheet: Boolean = false,

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
        val nominal = state.formNominal.replace(".", "").replace(",", ".").toDoubleOrNull()
        if (nominal == null || nominal <= 0) {
            _uiState.value = _uiState.value.copy(submitError = "Nominal harus diisi dengan angka valid")
            return
        }
        if (state.formKategoriId == null) {
            _uiState.value = _uiState.value.copy(submitError = "Pilih kategori transaksi")
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
            val str = amount.toLong().toString()
            val sb = StringBuilder()
            var count = 0
            for (i in str.lastIndex downTo 0) {
                if (count > 0 && count % 3 == 0) sb.insert(0, '.')
                sb.insert(0, str[i])
                count++
            }
            return "Rp $sb"
        }
    }
}
