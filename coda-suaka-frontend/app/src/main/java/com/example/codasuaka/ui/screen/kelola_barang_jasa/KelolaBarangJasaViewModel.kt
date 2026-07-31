package com.example.codasuaka.ui.screen.kelola_barang_jasa

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.codasuaka.data.remote.dto.BarangJasaDto
import com.example.codasuaka.data.remote.dto.BarangJasaRequest
import com.example.codasuaka.domain.repository.KasirRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Mode dialog yang sedang aktif di layar Kelola Barang/Jasa.
 */
sealed class BarangJasaDialogMode {
    /** Tidak ada dialog */
    data object Closed : BarangJasaDialogMode()
    /** Dialog form tambah barang/jasa baru */
    data object Tambah : BarangJasaDialogMode()
    /** Dialog form edit barang/jasa yang sudah ada */
    data class Edit(val item: BarangJasaDto) : BarangJasaDialogMode()
}

/**
 * State halaman Kelola Barang/Jasa.
 */
data class KelolaBarangJasaUiState(
    val items: List<BarangJasaDto> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val dialogMode: BarangJasaDialogMode = BarangJasaDialogMode.Closed,
    val showDeleteConfirm: Boolean = false,
    /** Pesan error khusus dari percobaan hapus (mis. 422 "sudah dipakai di nota") */
    val deleteError: String? = null,
    // Form fields
    val formNama: String = "",
    val formJenis: String = "barang",
    val formKategori: String = "",
    val formSatuan: String = "",
    val formHargaJual: String = "",
    val formHargaBeli: String = "",
    val formStok: String = "",
    val formKeterangan: String = ""
) {
    val isEditing: Boolean get() = dialogMode is BarangJasaDialogMode.Edit
}

/**
 * ViewModel untuk halaman Kelola Barang/Jasa (katalog Kasir).
 */
class KelolaBarangJasaViewModel(
    private val kasirRepository: KasirRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(KelolaBarangJasaUiState())
    val uiState: StateFlow<KelolaBarangJasaUiState> = _uiState

    init {
        loadBarangJasa()
    }

    /**
     * Memuat daftar barang/jasa dari API (termasuk yang nonaktif).
     */
    fun loadBarangJasa() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            kasirRepository.getBarangJasaList()
                .onSuccess { list ->
                    _uiState.value = _uiState.value.copy(items = list, isLoading = false)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = error.message)
                }
        }
    }

    // ─── Dialog ──────────────────────────────────────────────

    fun openDialogTambah() {
        _uiState.value = _uiState.value.copy(
            dialogMode = BarangJasaDialogMode.Tambah,
            formNama = "",
            formJenis = "barang",
            formKategori = "",
            formSatuan = "",
            formHargaJual = "",
            formHargaBeli = "",
            formStok = "",
            formKeterangan = "",
            errorMessage = null,
            deleteError = null,
            successMessage = null
        )
    }

    fun openDialogEdit(item: BarangJasaDto) {
        _uiState.value = _uiState.value.copy(
            dialogMode = BarangJasaDialogMode.Edit(item),
            formNama = item.nama,
            formJenis = item.jenis,
            formKategori = item.kategori ?: "",
            formSatuan = item.satuan,
            formHargaJual = item.hargaJual.toFormInput(),
            formHargaBeli = item.hargaBeli?.toFormInput() ?: "",
            formStok = item.stok?.toString() ?: "",
            formKeterangan = item.keterangan ?: "",
            errorMessage = null,
            deleteError = null,
            successMessage = null
        )
    }

    fun closeDialog() {
        _uiState.value = _uiState.value.copy(
            dialogMode = BarangJasaDialogMode.Closed,
            showDeleteConfirm = false,
            errorMessage = null,
            deleteError = null
        )
    }

    // ─── Form Input ──────────────────────────────────────────

    fun onFormNamaChange(value: String) {
        _uiState.value = _uiState.value.copy(formNama = value, errorMessage = null)
    }

    fun onFormJenisChange(value: String) {
        _uiState.value = _uiState.value.copy(
            formJenis = value,
            // Stok cuma relevan untuk barang — kosongkan kalau pindah ke jasa
            formStok = if (value == "jasa") "" else _uiState.value.formStok,
            errorMessage = null
        )
    }

    fun onFormKategoriChange(value: String) {
        _uiState.value = _uiState.value.copy(formKategori = value, errorMessage = null)
    }

    fun onFormSatuanChange(value: String) {
        _uiState.value = _uiState.value.copy(formSatuan = value, errorMessage = null)
    }

    fun onFormHargaJualChange(value: String) {
        _uiState.value = _uiState.value.copy(formHargaJual = value, errorMessage = null)
    }

    fun onFormHargaBeliChange(value: String) {
        _uiState.value = _uiState.value.copy(formHargaBeli = value, errorMessage = null)
    }

    fun onFormStokChange(value: String) {
        _uiState.value = _uiState.value.copy(formStok = value, errorMessage = null)
    }

    fun onFormKeteranganChange(value: String) {
        _uiState.value = _uiState.value.copy(formKeterangan = value, errorMessage = null)
    }

    /**
     * Menyimpan barang/jasa (create kalau Tambah, update kalau Edit).
     */
    fun simpan() {
        val state = _uiState.value

        if (state.formNama.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Nama harus diisi.")
            return
        }
        if (state.formSatuan.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Satuan harus diisi.")
            return
        }
        val hargaJual = state.formHargaJual.trim().toDoubleOrNull()
        if (hargaJual == null) {
            _uiState.value = state.copy(errorMessage = "Harga jual harus diisi dengan angka.")
            return
        }
        val hargaBeliInput = state.formHargaBeli.trim()
        val hargaBeli = if (hargaBeliInput.isBlank()) null else hargaBeliInput.toDoubleOrNull()
        if (hargaBeliInput.isNotBlank() && hargaBeli == null) {
            _uiState.value = state.copy(errorMessage = "Harga beli harus berupa angka.")
            return
        }
        val stokInput = state.formStok.trim()
        val stok = if (state.formJenis == "jasa" || stokInput.isBlank()) null else stokInput.toIntOrNull()
        if (state.formJenis == "barang" && stokInput.isNotBlank() && stok == null) {
            _uiState.value = state.copy(errorMessage = "Stok harus berupa angka bulat.")
            return
        }

        val request = BarangJasaRequest(
            nama = state.formNama.trim(),
            jenis = state.formJenis,
            kategori = state.formKategori.trim().ifBlank { null },
            satuan = state.formSatuan.trim(),
            hargaJual = hargaJual,
            hargaBeli = hargaBeli,
            stok = stok,
            keterangan = state.formKeterangan.trim().ifBlank { null }
        )

        val editingItem = (state.dialogMode as? BarangJasaDialogMode.Edit)?.item

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)

            val result = if (editingItem != null) {
                kasirRepository.updateBarangJasa(editingItem.id, request)
            } else {
                kasirRepository.createBarangJasa(request)
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
                        dialogMode = BarangJasaDialogMode.Closed,
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

    // ─── Hapus ───────────────────────────────────────────────

    fun requestDelete() {
        _uiState.value = _uiState.value.copy(showDeleteConfirm = true, deleteError = null)
    }

    fun cancelDelete() {
        _uiState.value = _uiState.value.copy(showDeleteConfirm = false)
    }

    fun confirmDelete() {
        val item = (_uiState.value.dialogMode as? BarangJasaDialogMode.Edit)?.item ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeleting = true, deleteError = null)

            kasirRepository.deleteBarangJasa(item.id)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        items = _uiState.value.items.filter { it.id != item.id },
                        isDeleting = false,
                        showDeleteConfirm = false,
                        dialogMode = BarangJasaDialogMode.Closed,
                        successMessage = "\"${item.nama}\" berhasil dihapus."
                    )
                }
                .onFailure { error ->
                    // Item TIDAK dihapus dari list lokal — dialog edit tetap terbuka
                    // supaya pesan error (mis. "sudah dipakai di nota") terlihat.
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
