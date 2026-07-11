package com.example.codasuaka.ui.screen.divisi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.codasuaka.data.remote.dto.CreateDivisiRequest
import com.example.codasuaka.data.remote.dto.DivisiDto
import com.example.codasuaka.data.remote.dto.OutletDto
import com.example.codasuaka.data.remote.dto.KaryawanDto
import com.example.codasuaka.data.remote.dto.UpdateDivisiRequest
import com.example.codasuaka.domain.repository.DivisiRepository
import com.example.codasuaka.domain.repository.KaryawanRepository
import com.example.codasuaka.domain.repository.OutletRepository
import com.example.codasuaka.ui.screen.kelola_karyawan.Karyawan
import com.example.codasuaka.ui.screen.kelola_outlet.Outlet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// ─── Data Models ───

data class Divisi(
    val id: Int = 0,
    val namaDivisi: String = "",
    val deskripsi: String = "",
    val ketua: Karyawan? = null,
    val outlet: Outlet? = null,
    val anggota: List<Karyawan> = emptyList(),
    val anggotaCount: Int = 0
)

// ─── Dialog Mode ───

sealed class DivisiDialogMode {
    data object Closed : DivisiDialogMode()
    data object Tambah : DivisiDialogMode()
    data class Edit(val divisi: Divisi) : DivisiDialogMode()
}

// ─── UI State ───

data class DivisiUiState(
    val divisiList: List<Divisi> = emptyList(),
    val outlets: List<Outlet> = emptyList(),
    val karyawanList: List<Karyawan> = emptyList(),
    val selectedOutletId: Int? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val dialogMode: DivisiDialogMode = DivisiDialogMode.Closed,
    // ── Form Fields ──
    val formNamaDivisi: String = "",
    val formDeskripsi: String = "",
    val formKetuaKaryawanId: String? = null,
    val formOutletId: Int = 0,
    val editingDivisiId: Int? = null,
    // ── Anggota Management ──
    val formAnggota: List<Karyawan> = emptyList(),       // anggota yg sudah dipilih
    val formAvailableKaryawan: List<Karyawan> = emptyList() // karyawan yg tersedia utk ditambah
)

// ─── ViewModel ───

/**
 * ViewModel untuk halaman Divisi.
 */
class DivisiViewModel(
    private val divisiRepository: DivisiRepository,
    private val karyawanRepository: KaryawanRepository,
    private val outletRepository: OutletRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DivisiUiState())
    val uiState: StateFlow<DivisiUiState> = _uiState

    init {
        loadInitialData()
    }

    /**
     * Memuat data awal (outlets, karyawan, daftar divisi) dari API.
     */
    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            var loadedOutlets = emptyList<Outlet>()
            var loadedKaryawan = emptyList<Karyawan>()
            var loadedDivisi = emptyList<Divisi>()
            var errorMsg: String? = null

            // Load outlets
            outletRepository.getOutlets().onSuccess { dtos ->
                loadedOutlets = dtos.map { it.toOutlet() }
            }.onFailure {
                errorMsg = it.message
            }

            // Load karyawan
            karyawanRepository.getKaryawans().onSuccess { dtos ->
                loadedKaryawan = dtos.map { it.toKaryawanNoRole() }
            }.onFailure {
                errorMsg = errorMsg ?: it.message
            }

            // Load divisi
            divisiRepository.getDivisis().onSuccess { dtos ->
                loadedDivisi = dtos.map { it.toDivisi(loadedKaryawan, loadedOutlets) }
            }.onFailure {
                errorMsg = errorMsg ?: it.message
            }

            _uiState.value = _uiState.value.copy(
                outlets = loadedOutlets,
                karyawanList = loadedKaryawan,
                divisiList = loadedDivisi,
                isLoading = false,
                errorMessage = errorMsg
            )
        }
    }

    /**
     * Memuat ulang daftar divisi, opsional filter berdasarkan outlet.
     */
    fun loadDivisi(outletId: Int? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            divisiRepository.getDivisis(outletId).onSuccess { dtos ->
                _uiState.value = _uiState.value.copy(
                    divisiList = dtos.map { it.toDivisi(_uiState.value.karyawanList, _uiState.value.outlets) },
                    isLoading = false,
                    selectedOutletId = outletId
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = it.message ?: "Gagal memuat divisi"
                )
            }
        }
    }

    // ─── Dialog ───

    fun openDialogTambah() {
        val state = _uiState.value
        _uiState.value = _uiState.value.copy(
            dialogMode = DivisiDialogMode.Tambah,
            formNamaDivisi = "",
            formDeskripsi = "",
            formKetuaKaryawanId = null,
            formOutletId = state.outlets.firstOrNull()?.id ?: 0,
            editingDivisiId = null,
            formAnggota = emptyList(),
            formAvailableKaryawan = state.karyawanList,
            errorMessage = null,
            successMessage = null
        )
    }

    fun openDialogEdit(divisi: Divisi) {
        val state = _uiState.value
        val anggotaIds = divisi.anggota.map { it.id }
        val available = state.karyawanList.filter { it.id !in anggotaIds }
        _uiState.value = _uiState.value.copy(
            dialogMode = DivisiDialogMode.Edit(divisi),
            formNamaDivisi = divisi.namaDivisi,
            formDeskripsi = divisi.deskripsi,
            formKetuaKaryawanId = divisi.ketua?.id,
            formOutletId = divisi.outlet?.id ?: 0,
            editingDivisiId = divisi.id,
            formAnggota = divisi.anggota.toMutableList(),
            formAvailableKaryawan = available,
            errorMessage = null,
            successMessage = null
        )
    }

    fun closeDialog() {
        _uiState.value = _uiState.value.copy(
            dialogMode = DivisiDialogMode.Closed,
            formNamaDivisi = "",
            formDeskripsi = "",
            formKetuaKaryawanId = null,
            formOutletId = 0,
            editingDivisiId = null,
            formAnggota = emptyList(),
            formAvailableKaryawan = emptyList(),
            errorMessage = null
        )
    }

    // ─── Form Input ───

    fun onFormNamaDivisiChange(value: String) {
        _uiState.value = _uiState.value.copy(formNamaDivisi = value, errorMessage = null)
    }

    fun onFormDeskripsiChange(value: String) {
        _uiState.value = _uiState.value.copy(formDeskripsi = value, errorMessage = null)
    }

    fun onFormKetuaChange(karyawanId: String?) {
        _uiState.value = _uiState.value.copy(formKetuaKaryawanId = karyawanId, errorMessage = null)
    }

    fun onFormOutletChange(outletId: Int) {
        _uiState.value = _uiState.value.copy(formOutletId = outletId, errorMessage = null)
    }

    // ─── Anggota Management ───

    /**
     * Menambahkan karyawan ke daftar anggota divisi (form).
     */
    fun tambahAnggota(karyawan: Karyawan) {
        val state = _uiState.value
        if (karyawan.id in state.formAnggota.map { it.id }) return // already added

        _uiState.value = _uiState.value.copy(
            formAnggota = state.formAnggota + karyawan,
            formAvailableKaryawan = state.formAvailableKaryawan.filter { it.id != karyawan.id },
            errorMessage = null
        )
    }

    /**
     * Menghapus karyawan dari daftar anggota divisi (form).
     */
    fun hapusAnggota(karyawan: Karyawan) {
        val state = _uiState.value
        _uiState.value = _uiState.value.copy(
            formAnggota = state.formAnggota.filter { it.id != karyawan.id },
            formAvailableKaryawan = state.formAvailableKaryawan + karyawan,
            errorMessage = null
        )
    }

    // ─── Actions ───

    /**
     * Menyimpan divisi baru ke API.
     */
    fun simpanDivisi() {
        val state = _uiState.value

        // Validasi
        if (state.formNamaDivisi.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Nama divisi harus diisi.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)

            val request = CreateDivisiRequest(
                namaDivisi = state.formNamaDivisi.trim(),
                deskripsi = state.formDeskripsi.trim().ifEmpty { null },
                ketuaKaryawanId = state.formKetuaKaryawanId,
                outletId = state.formOutletId.takeIf { it > 0 }
                    ?: (state.outlets.firstOrNull()?.id ?: 0)
            )
            divisiRepository.createDivisi(request).onSuccess { dto ->
                val newDivisi = dto.toDivisi(state.karyawanList, state.outlets)
                _uiState.value = _uiState.value.copy(
                    divisiList = _uiState.value.divisiList + newDivisi,
                    isSaving = false,
                    dialogMode = DivisiDialogMode.Closed,
                    formNamaDivisi = "",
                    formDeskripsi = "",
                    formKetuaKaryawanId = null,
                    formOutletId = 0,
                    formAnggota = emptyList(),
                    formAvailableKaryawan = emptyList(),
                    successMessage = "Divisi \"${newDivisi.namaDivisi}\" berhasil ditambahkan."
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = it.message ?: "Gagal menambahkan divisi"
                )
            }
        }
    }

    /**
     * Memperbarui data divisi via API.
     */
    fun updateDivisi() {
        val state = _uiState.value
        val id = state.editingDivisiId ?: return

        if (state.formNamaDivisi.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Nama divisi harus diisi.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)

            val request = UpdateDivisiRequest(
                namaDivisi = state.formNamaDivisi.trim(),
                deskripsi = state.formDeskripsi.trim().ifEmpty { null },
                ketuaKaryawanId = state.formKetuaKaryawanId,
                outletId = state.formOutletId.takeIf { it > 0 }
            )
            divisiRepository.updateDivisi(id, request).onSuccess {
                // Reload divisi list
                divisiRepository.getDivisis().onSuccess { dtos ->
                    _uiState.value = _uiState.value.copy(
                        divisiList = dtos.map { it.toDivisi(state.karyawanList, state.outlets) },
                        isSaving = false,
                        dialogMode = DivisiDialogMode.Closed,
                        successMessage = "Divisi berhasil diperbarui."
                    )
                }.onFailure {
                    // Even if reload fails, consider update successful
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        dialogMode = DivisiDialogMode.Closed,
                        successMessage = "Divisi berhasil diperbarui."
                    )
                }
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = it.message ?: "Gagal memperbarui divisi"
                )
            }
        }
    }

    /**
     * Menghapus divisi via API.
     */
    fun hapusDivisi(id: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)

            divisiRepository.deleteDivisi(id).onSuccess {
                _uiState.value = _uiState.value.copy(
                    divisiList = _uiState.value.divisiList.filter { it.id != id },
                    isSaving = false,
                    dialogMode = DivisiDialogMode.Closed,
                    successMessage = "Divisi berhasil dihapus."
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = it.message ?: "Gagal menghapus divisi"
                )
            }
        }
    }

    fun setOutlets(outlets: List<Outlet>) {
        _uiState.value = _uiState.value.copy(outlets = outlets)
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }

    companion object {
        fun OutletDto.toOutlet() = Outlet(
            id = this.id,
            namaOutlet = this.namaOutlet,
            alamatOutlet = this.alamatOutlet ?: ""
        )

        fun KaryawanDto.toKaryawanNoRole() = Karyawan(
            id = this.id,
            namaLengkap = this.namaLengkap,
            alamat = this.alamat ?: "",
            kontak = this.kontak ?: ""
        )

        fun DivisiDto.toDivisi(karyawanList: List<Karyawan>, outlets: List<Outlet>): Divisi {
            val ketua = this.ketuaKaryawan?.let { pk ->
                karyawanList.find { it.id == pk.id }
            }
            val outlet = this.outlet?.let { o ->
                outlets.find { it.id == o.id }
            }
            val anggotaKaryawan = this.anggota?.mapNotNull { anggotaDto ->
                anggotaDto.karyawan?.let { kDto ->
                    karyawanList.find { it.id == kDto.id }
                }
            } ?: emptyList()

            return Divisi(
                id = this.id,
                namaDivisi = this.namaDivisi,
                deskripsi = this.deskripsi ?: "",
                ketua = ketua,
                outlet = outlet,
                anggota = anggotaKaryawan,
                anggotaCount = this.anggotaCount
            )
        }
    }
}
