package com.example.codasuaka.ui.screen.stok

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.codasuaka.data.remote.dto.StokDto
import com.example.codasuaka.data.remote.dto.StokMutationDto
import com.example.codasuaka.data.remote.dto.StokMutationRequest
import com.example.codasuaka.data.remote.dto.StokRequest
import com.example.codasuaka.domain.repository.StokRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Mode dialog yang sedang aktif di layar Stok.
 */
sealed class StokDialogMode {
    data object Closed : StokDialogMode()
    data object Tambah : StokDialogMode()
    data class Edit(val item: StokDto) : StokDialogMode()
    data class Mutasi(val item: StokDto) : StokDialogMode()
    data class Riwayat(val item: StokDto) : StokDialogMode()
}

/**
 * State halaman Stok (bahan baku / barang produksi).
 */
data class StokUiState(
    val items: List<StokDto> = emptyList(),
    val searchQuery: String = "",
    val kategoriFilter: String? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isMutating: Boolean = false,
    val isDeleting: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val dialogMode: StokDialogMode = StokDialogMode.Closed,
    val showDeleteConfirm: Boolean = false,
    val deleteError: String? = null,
    // Form fields (tambah/edit)
    val formNama: String = "",
    val formKategori: String = "",
    val formSatuan: String = "",
    val formStokMinimum: String = "",
    val formHargaBeli: String = "",
    val formKeterangan: String = "",
    val namaError: String? = null,
    val satuanError: String? = null,
    val stokMinimumError: String? = null,
    val hargaBeliError: String? = null,
    // Form mutasi
    val mutasiJenis: String = "masuk",
    val mutasiJumlah: String = "",
    val mutasiKeterangan: String = "",
    val mutasiJumlahError: String? = null,
    val mutasiError: String? = null,
    // Riwayat mutasi
    val riwayatList: List<StokMutationDto> = emptyList(),
    val riwayatLoading: Boolean = false,
    val riwayatError: String? = null
) {
    val isEditing: Boolean get() = dialogMode is StokDialogMode.Edit

    val filteredItems: List<StokDto> get() = items.filter { item ->
        val matchesSearch = searchQuery.isBlank() ||
            item.nama.contains(searchQuery, ignoreCase = true) ||
            (item.kategori?.contains(searchQuery, ignoreCase = true) == true)
        val matchesKategori = kategoriFilter == null || item.kategori == kategoriFilter
        matchesSearch && matchesKategori
    }

    val kategoriOptions: List<String> get() = items.mapNotNull { it.kategori }.distinct().sorted()
}

/**
 * ViewModel untuk halaman Stok (bahan baku / barang produksi).
 */
class StokViewModel(
    private val stokRepository: StokRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StokUiState())
    val uiState: StateFlow<StokUiState> = _uiState

    init {
        loadStok()
    }

    fun loadStok() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            stokRepository.getStokList()
                .onSuccess { (list, _) ->
                    _uiState.value = _uiState.value.copy(items = list, isLoading = false)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = error.message)
                }
        }
    }

    // ─── Search & Filter ─────────────────────────────────────

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun onKategoriFilterChange(kategori: String?) {
        _uiState.value = _uiState.value.copy(kategoriFilter = kategori)
    }

    // ─── Dialog ──────────────────────────────────────────────

    fun openDialogTambah() {
        _uiState.value = _uiState.value.copy(
            dialogMode = StokDialogMode.Tambah,
            formNama = "",
            formKategori = "",
            formSatuan = "",
            formStokMinimum = "",
            formHargaBeli = "",
            formKeterangan = "",
            errorMessage = null,
            deleteError = null,
            successMessage = null,
            namaError = null,
            satuanError = null,
            stokMinimumError = null,
            hargaBeliError = null
        )
    }

    fun openDialogEdit(item: StokDto) {
        _uiState.value = _uiState.value.copy(
            dialogMode = StokDialogMode.Edit(item),
            formNama = item.nama,
            formKategori = item.kategori ?: "",
            formSatuan = item.satuan,
            formStokMinimum = item.stokMinimum?.toFormInput() ?: "",
            formHargaBeli = item.hargaBeli?.toFormInput() ?: "",
            formKeterangan = item.keterangan ?: "",
            errorMessage = null,
            deleteError = null,
            successMessage = null,
            namaError = null,
            satuanError = null,
            stokMinimumError = null,
            hargaBeliError = null
        )
    }

    fun openDialogMutasi(item: StokDto) {
        _uiState.value = _uiState.value.copy(
            dialogMode = StokDialogMode.Mutasi(item),
            mutasiJenis = "masuk",
            mutasiJumlah = "",
            mutasiKeterangan = "",
            mutasiJumlahError = null,
            mutasiError = null
        )
    }

    fun openDialogRiwayat(item: StokDto) {
        _uiState.value = _uiState.value.copy(
            dialogMode = StokDialogMode.Riwayat(item),
            riwayatList = emptyList(),
            riwayatError = null
        )
        loadRiwayat(item.id)
    }

    fun closeDialog() {
        _uiState.value = _uiState.value.copy(
            dialogMode = StokDialogMode.Closed,
            showDeleteConfirm = false,
            errorMessage = null,
            deleteError = null,
            mutasiError = null
        )
    }

    // ─── Form Input (tambah/edit) ────────────────────────────

    fun onFormNamaChange(value: String) {
        _uiState.value = _uiState.value.copy(formNama = value, namaError = null)
    }

    fun onFormKategoriChange(value: String) {
        _uiState.value = _uiState.value.copy(formKategori = value)
    }

    fun onFormSatuanChange(value: String) {
        _uiState.value = _uiState.value.copy(formSatuan = value, satuanError = null)
    }

    fun onFormStokMinimumChange(value: String) {
        _uiState.value = _uiState.value.copy(formStokMinimum = value, stokMinimumError = null)
    }

    fun onFormHargaBeliChange(value: String) {
        _uiState.value = _uiState.value.copy(formHargaBeli = value, hargaBeliError = null)
    }

    fun onFormKeteranganChange(value: String) {
        _uiState.value = _uiState.value.copy(formKeterangan = value)
    }

    /**
     * Menyimpan stok (create kalau Tambah, update kalau Edit).
     */
    fun simpan() {
        val state = _uiState.value
        var hasError = false
        var newState = state.copy(
            namaError = null,
            satuanError = null,
            stokMinimumError = null,
            hargaBeliError = null,
            errorMessage = null
        )

        if (state.formNama.isBlank()) {
            newState = newState.copy(namaError = "Nama harus diisi.")
            hasError = true
        }
        if (state.formSatuan.isBlank()) {
            newState = newState.copy(satuanError = "Satuan harus diisi.")
            hasError = true
        }

        val stokMinimumInput = state.formStokMinimum.trim()
        val stokMinimum = if (stokMinimumInput.isBlank()) null else stokMinimumInput.toDoubleOrNull()
        if (stokMinimumInput.isNotBlank() && stokMinimum == null) {
            newState = newState.copy(stokMinimumError = "Stok minimum harus berupa angka.")
            hasError = true
        } else if (stokMinimum != null && stokMinimum < 0) {
            newState = newState.copy(stokMinimumError = "Stok minimum tidak boleh negatif.")
            hasError = true
        }

        val hargaBeliInput = state.formHargaBeli.trim()
        val hargaBeli = if (hargaBeliInput.isBlank()) null else hargaBeliInput.toDoubleOrNull()
        if (hargaBeliInput.isNotBlank() && hargaBeli == null) {
            newState = newState.copy(hargaBeliError = "Harga beli harus berupa angka.")
            hasError = true
        } else if (hargaBeli != null && hargaBeli < 0) {
            newState = newState.copy(hargaBeliError = "Harga beli tidak boleh negatif.")
            hasError = true
        }

        if (hasError) {
            _uiState.value = newState
            return
        }

        val request = StokRequest(
            nama = state.formNama.trim(),
            kategori = state.formKategori.trim().ifBlank { null },
            satuan = state.formSatuan.trim(),
            stokMinimum = stokMinimum,
            hargaBeli = hargaBeli,
            keterangan = state.formKeterangan.trim().ifBlank { null }
        )

        val editingItem = (state.dialogMode as? StokDialogMode.Edit)?.item

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)

            val result = if (editingItem != null) {
                stokRepository.updateStok(editingItem.id, request)
            } else {
                stokRepository.createStok(request)
            }

            result
                .onSuccess { dto ->
                    val newItems = if (editingItem != null) {
                        _uiState.value.items.map { if (it.id == dto.id) dto else it }
                    } else {
                        _uiState.value.items + dto
                    }
                    _uiState.value = _uiState.value.copy(
                        items = newItems,
                        isSaving = false,
                        dialogMode = StokDialogMode.Closed,
                        successMessage = if (editingItem != null) {
                            "\"${dto.nama}\" berhasil diperbarui."
                        } else {
                            "\"${dto.nama}\" berhasil ditambahkan."
                        }
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(isSaving = false, errorMessage = error.message)
                }
        }
    }

    // ─── Form Mutasi ─────────────────────────────────────────

    fun onMutasiJenisChange(value: String) {
        _uiState.value = _uiState.value.copy(mutasiJenis = value, mutasiJumlahError = null, mutasiError = null)
    }

    fun onMutasiJumlahChange(value: String) {
        _uiState.value = _uiState.value.copy(mutasiJumlah = value, mutasiJumlahError = null, mutasiError = null)
    }

    fun onMutasiKeteranganChange(value: String) {
        _uiState.value = _uiState.value.copy(mutasiKeterangan = value)
    }

    fun submitMutasi() {
        val state = _uiState.value
        val item = (state.dialogMode as? StokDialogMode.Mutasi)?.item ?: return

        val jumlah = state.mutasiJumlah.trim().toDoubleOrNull()
        if (state.mutasiJumlah.isBlank()) {
            _uiState.value = _uiState.value.copy(mutasiJumlahError = "Jumlah harus diisi.")
            return
        }
        if (jumlah == null) {
            _uiState.value = _uiState.value.copy(mutasiJumlahError = "Jumlah harus berupa angka.")
            return
        }
        if (jumlah <= 0) {
            _uiState.value = _uiState.value.copy(mutasiJumlahError = "Jumlah harus lebih dari 0.")
            return
        }

        val request = StokMutationRequest(
            jenis = state.mutasiJenis,
            jumlah = jumlah,
            keterangan = state.mutasiKeterangan.trim().ifBlank { null }
        )

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isMutating = true, mutasiError = null)

            stokRepository.mutateStok(item.id, request)
                .onSuccess { mutation ->
                    // Update stok lokal dari snapshot sesudah mutasi
                    val updatedItem = item.copy(stok = mutation.stokSesudah)
                    _uiState.value = _uiState.value.copy(
                        items = _uiState.value.items.map { if (it.id == item.id) updatedItem else it },
                        isMutating = false,
                        dialogMode = StokDialogMode.Closed,
                        successMessage = "Mutasi ${mutation.jenis} \"${item.nama}\" berhasil. Stok sekarang ${mutation.stokSesudah.toFormInput()} ${item.satuan}."
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(isMutating = false, mutasiError = error.message)
                }
        }
    }

    // ─── Riwayat Mutasi ──────────────────────────────────────

    private fun loadRiwayat(stokId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(riwayatLoading = true, riwayatError = null)

            stokRepository.getStokRiwayat(stokId)
                .onSuccess { (list, _) ->
                    _uiState.value = _uiState.value.copy(riwayatList = list, riwayatLoading = false)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(riwayatLoading = false, riwayatError = error.message)
                }
        }
    }

    // ─── Hapus ───────────────────────────────────────────────

    fun requestDelete() {
        _uiState.value = _uiState.value.copy(showDeleteConfirm = true, deleteError = null)
    }

    fun cancelDelete() {
        _uiState.value = _uiState.value.copy(showDeleteConfirm = false)
    }

    fun confirmDelete() {
        val item = (_uiState.value.dialogMode as? StokDialogMode.Edit)?.item ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeleting = true, deleteError = null)

            stokRepository.deleteStok(item.id)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        items = _uiState.value.items.filter { it.id != item.id },
                        isDeleting = false,
                        showDeleteConfirm = false,
                        dialogMode = StokDialogMode.Closed,
                        successMessage = "\"${item.nama}\" berhasil dihapus."
                    )
                }
                .onFailure { error ->
                    // Item TIDAK dihapus dari list lokal — dialog edit tetap terbuka
                    // supaya pesan error (mis. "sudah ada riwayat mutasi") terlihat.
                    _uiState.value = _uiState.value.copy(
                        isDeleting = false,
                        showDeleteConfirm = false,
                        deleteError = error.message
                    )
                }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }

    companion object {
        /** Format angka Double sebagai input form: 15000.0 -> "15000", 15000.5 -> "15000.5" */
        private fun Double.toFormInput(): String =
            if (this == this.toLong().toDouble()) this.toLong().toString() else this.toString()
    }
}
