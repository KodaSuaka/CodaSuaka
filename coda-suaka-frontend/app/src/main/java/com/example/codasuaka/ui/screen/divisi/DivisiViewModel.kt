package com.example.codasuaka.ui.screen.divisi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.codasuaka.ui.screen.kelola_karyawan.Karyawan
import com.example.codasuaka.ui.screen.kelola_outlet.Outlet
import kotlinx.coroutines.delay
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
    val formKetuaKaryawanId: Int? = null,
    val formOutletId: Int = 0,
    val editingDivisiId: Int? = null,
    // ── Anggota Management ──
    val formAnggota: List<Karyawan> = emptyList(),       // anggota yg sudah dipilih
    val formAvailableKaryawan: List<Karyawan> = emptyList() // karyawan yg tersedia utk ditambah
)

// ─── ViewModel ───

/**
 * ViewModel untuk halaman Divisi.
 * TODO: Integrasi dengan API backend GET/POST/PUT/DELETE /api/divisi
 */
class DivisiViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(DivisiUiState())
    val uiState: StateFlow<DivisiUiState> = _uiState

    private var nextDivisiId = 1

    init {
        loadInitialData()
    }

    /**
     * Memuat data awal (outlets, karyawan, daftar divisi).
     * TODO: Panggil API GET /api/outlet, GET /api/karyawan, GET /api/divisi
     */
    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            delay(600)

            // Dummy outlets
            val dummyOutlets = listOf(
                Outlet(id = 1, namaOutlet = "Outlet Pusat", alamatOutlet = "Jl. Merdeka No. 123, Jakarta"),
                Outlet(id = 2, namaOutlet = "Outlet Cabang", alamatOutlet = "Jl. Sudirman No. 45, Bandung")
            )

            // Dummy karyawan
            val dummyKaryawan = listOf(
                Karyawan(id = "1", namaLengkap = "Ahmad Fauzi", alamat = ""),
                Karyawan(id = "2", namaLengkap = "Siti Rahmawati", alamat = ""),
                Karyawan(id = "3", namaLengkap = "Budi Santoso", alamat = ""),
                Karyawan(id = "4", namaLengkap = "Dewi Lestari", alamat = ""),
                Karyawan(id = "5", namaLengkap = "Rudi Hartono", alamat = "")
            )

            // Dummy divisi
            val dummyDivisi = listOf(
                Divisi(
                    id = nextDivisiId++,
                    namaDivisi = "Dapur",
                    deskripsi = "Tim yang bertanggung jawab atas pembuatan dan penyajian menu makanan",
                    ketua = dummyKaryawan[1], // Siti Rahmawati
                    outlet = dummyOutlets[0],  // Outlet Pusat
                    anggota = listOf(dummyKaryawan[3], dummyKaryawan[4]),
                    anggotaCount = 2
                ),
                Divisi(
                    id = nextDivisiId++,
                    namaDivisi = "Pelayanan",
                    deskripsi = "Tim yang melayani pelanggan di restoran",
                    ketua = dummyKaryawan[2], // Budi Santoso
                    outlet = dummyOutlets[0],  // Outlet Pusat
                    anggota = listOf(dummyKaryawan[0]),
                    anggotaCount = 1
                ),
                Divisi(
                    id = nextDivisiId++,
                    namaDivisi = "Kasir",
                    deskripsi = "Tim yang bertanggung jawab atas transaksi pembayaran",
                    ketua = dummyKaryawan[0], // Ahmad Fauzi
                    outlet = dummyOutlets[1],  // Outlet Cabang
                    anggota = listOf(dummyKaryawan[2]),
                    anggotaCount = 1
                )
            )

            _uiState.value = _uiState.value.copy(
                outlets = dummyOutlets,
                karyawanList = dummyKaryawan,
                divisiList = dummyDivisi,
                isLoading = false
            )
        }
    }

    /**
     * Memuat ulang daftar divisi, opsional filter berdasarkan outlet.
     * TODO: Panggil API GET /api/divisi?outlet_id=...
     */
    fun loadDivisi(outletId: Int? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            delay(400)

            val filtered = if (outletId != null) {
                _uiState.value.divisiList.filter { it.outlet?.id == outletId }
            } else {
                _uiState.value.divisiList
            }

            _uiState.value = _uiState.value.copy(
                divisiList = filtered,
                isLoading = false,
                selectedOutletId = outletId
            )
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
            formKetuaKaryawanId = divisi.ketua?.id?.toIntOrNull(),
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

    fun onFormKetuaChange(karyawanId: Int?) {
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
     * Menyimpan divisi baru dari dialog tambah.
     * TODO: Panggil API POST /api/divisi
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
            delay(500)

            val outlet = state.outlets.find { it.id == state.formOutletId }
                ?: state.outlets.firstOrNull()
            val ketua = state.formKetuaKaryawanId?.let { id ->
                state.karyawanList.find { it.id == id.toString() }
            }

            val newDivisi = Divisi(
                id = nextDivisiId++,
                namaDivisi = state.formNamaDivisi.trim(),
                deskripsi = state.formDeskripsi.trim(),
                ketua = ketua,
                outlet = outlet,
                anggota = state.formAnggota,
                anggotaCount = state.formAnggota.size
            )

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
        }
    }

    /**
     * Memperbarui data divisi dari dialog edit.
     * TODO: Panggil API PUT /api/divisi/{id}
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
            delay(500)

            val outlet = state.outlets.find { it.id == state.formOutletId }
            val ketua = state.formKetuaKaryawanId?.let { karyawanId ->
                state.karyawanList.find { it.id == karyawanId.toString() }
            }

            val updatedDivisi = state.divisiList.find { it.id == id }?.copy(
                namaDivisi = state.formNamaDivisi.trim(),
                deskripsi = state.formDeskripsi.trim(),
                ketua = ketua,
                outlet = outlet,
                anggota = state.formAnggota,
                anggotaCount = state.formAnggota.size
            )

            if (updatedDivisi != null) {
                _uiState.value = _uiState.value.copy(
                    divisiList = _uiState.value.divisiList.map {
                        if (it.id == id) updatedDivisi else it
                    },
                    isSaving = false,
                    dialogMode = DivisiDialogMode.Closed,
                    successMessage = "Divisi berhasil diperbarui."
                )
            }
        }
    }

    /**
     * Menghapus divisi dari dialog edit.
     * TODO: Panggil API DELETE /api/divisi/{id}
     */
    fun hapusDivisi(id: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            delay(300)

            _uiState.value = _uiState.value.copy(
                divisiList = _uiState.value.divisiList.filter { it.id != id },
                isSaving = false,
                dialogMode = DivisiDialogMode.Closed,
                successMessage = "Divisi berhasil dihapus."
            )
        }
    }

    fun setOutlets(outlets: List<Outlet>) {
        _uiState.value = _uiState.value.copy(outlets = outlets)
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }
}
