package com.example.codasuaka.ui.screen.kelola_karyawan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.codasuaka.ui.screen.kelola_outlet.Outlet
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// ─── Data Models ───

data class Karyawan(
    val id: String = "",
    val namaLengkap: String = "",
    val alamat: String = "",
    val kontak: String = "",
    val fotoProfil: String? = null,
    val role: Role? = null,
    val outlet: Outlet? = null
)

data class Role(
    val id: Int = 0,
    val namaRole: String = ""
)

// ─── Dialog Mode ───

sealed class KaryawanDialogMode {
    data object Closed : KaryawanDialogMode()
    data object Tambah : KaryawanDialogMode()
    data class Edit(val karyawan: Karyawan) : KaryawanDialogMode()
}

// ─── UI State ───

data class KelolaKaryawanUiState(
    val karyawanList: List<Karyawan> = emptyList(),
    val outlets: List<Outlet> = emptyList(),
    val roles: List<Role> = emptyList(),
    val selectedOutletId: Int? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val dialogMode: KaryawanDialogMode = KaryawanDialogMode.Closed,
    // ── Form Fields ──
    val formNama: String = "",
    val formAlamat: String = "",
    val formRoleId: Int = 0,
    val formOutletId: Int = 0,
    val editingKaryawanId: String? = null
)

// ─── ViewModel ───

/**
 * ViewModel untuk halaman Kelola Karyawan.
 * TODO: Integrasi dengan API backend GET/POST/PUT/DELETE /api/karyawan
 */
class KelolaKaryawanViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(KelolaKaryawanUiState())
    val uiState: StateFlow<KelolaKaryawanUiState> = _uiState

    private var nextKaryawanId = 1

    init {
        loadInitialData()
    }

    /**
     * Memuat data awal (roles, outlets, daftar karyawan).
     * TODO: Panggil API GET /api/roles, GET /api/outlet, GET /api/karyawan
     */
    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            delay(600)

            // Dummy roles
            val dummyRoles = listOf(
                Role(id = 1, namaRole = "Kasir"),
                Role(id = 2, namaRole = "Koki"),
                Role(id = 3, namaRole = "Pelayan"),
                Role(id = 4, namaRole = "Kurir"),
                Role(id = 5, namaRole = "Pencuci")
            )

            // Dummy outlets
            val dummyOutlets = listOf(
                Outlet(id = 1, namaOutlet = "Outlet Pusat", alamatOutlet = "Jl. Merdeka No. 123, Jakarta"),
                Outlet(id = 2, namaOutlet = "Outlet Cabang", alamatOutlet = "Jl. Sudirman No. 45, Bandung")
            )

            // Dummy karyawan
            val dummyKaryawan = listOf(
                Karyawan(
                    id = (nextKaryawanId++).toString(),
                    namaLengkap = "Ahmad Fauzi",
                    alamat = "Jl. Merdeka No. 45, Jakarta",
                    role = Role(id = 1, namaRole = "Kasir"),
                    outlet = Outlet(id = 1, namaOutlet = "Outlet Pusat")
                ),
                Karyawan(
                    id = (nextKaryawanId++).toString(),
                    namaLengkap = "Siti Rahmawati",
                    alamat = "Jl. Kebon Jeruk No. 12, Jakarta",
                    role = Role(id = 2, namaRole = "Koki"),
                    outlet = Outlet(id = 1, namaOutlet = "Outlet Pusat")
                ),
                Karyawan(
                    id = (nextKaryawanId++).toString(),
                    namaLengkap = "Budi Santoso",
                    alamat = "Jl. Asia Afrika No. 88, Bandung",
                    role = Role(id = 3, namaRole = "Pelayan"),
                    outlet = Outlet(id = 2, namaOutlet = "Outlet Cabang")
                )
            )

            _uiState.value = _uiState.value.copy(
                roles = dummyRoles,
                outlets = dummyOutlets,
                karyawanList = dummyKaryawan,
                isLoading = false
            )
        }
    }

    /**
     * Memuat ulang daftar karyawan, opsional filter berdasarkan outlet.
     * TODO: Panggil API GET /api/karyawan?outlet_id=...
     */
    fun loadKaryawan(outletId: Int? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            delay(400)

            // Filter dummy data
            val filtered = if (outletId != null) {
                _uiState.value.karyawanList.filter { it.outlet?.id == outletId }
            } else {
                _uiState.value.karyawanList
            }

            _uiState.value = _uiState.value.copy(
                karyawanList = filtered,
                isLoading = false,
                selectedOutletId = outletId
            )
        }
    }

    // ─── Dialog ───

    fun openDialogTambah() {
        _uiState.value = _uiState.value.copy(
            dialogMode = KaryawanDialogMode.Tambah,
            formNama = "",
            formAlamat = "",
            formRoleId = 0,
            formOutletId = 0,
            editingKaryawanId = null,
            errorMessage = null,
            successMessage = null
        )
    }

    fun openDialogEdit(karyawan: Karyawan) {
        _uiState.value = _uiState.value.copy(
            dialogMode = KaryawanDialogMode.Edit(karyawan),
            formNama = karyawan.namaLengkap,
            formAlamat = karyawan.alamat,
            formRoleId = karyawan.role?.id ?: 0,
            formOutletId = karyawan.outlet?.id ?: 0,
            editingKaryawanId = karyawan.id,
            errorMessage = null,
            successMessage = null
        )
    }

    fun closeDialog() {
        _uiState.value = _uiState.value.copy(
            dialogMode = KaryawanDialogMode.Closed,
            formNama = "",
            formAlamat = "",
            formRoleId = 0,
            formOutletId = 0,
            editingKaryawanId = null,
            errorMessage = null
        )
    }

    // ─── Form Input ───

    fun onFormNamaChange(value: String) {
        _uiState.value = _uiState.value.copy(formNama = value, errorMessage = null)
    }

    fun onFormAlamatChange(value: String) {
        _uiState.value = _uiState.value.copy(formAlamat = value, errorMessage = null)
    }

    fun onFormRoleChange(roleId: Int) {
        _uiState.value = _uiState.value.copy(formRoleId = roleId, errorMessage = null)
    }

    fun onFormOutletChange(outletId: Int) {
        _uiState.value = _uiState.value.copy(formOutletId = outletId, errorMessage = null)
    }

    // ─── Actions ───

    /**
     * Menyimpan karyawan baru dari dialog tambah.
     * TODO: Panggil API POST /api/karyawan
     */
    fun simpanKaryawan() {
        val state = _uiState.value

        // Validasi
        if (state.formNama.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Nama karyawan harus diisi.")
            return
        }
        if (state.formAlamat.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Alamat karyawan harus diisi.")
            return
        }
        if (state.formRoleId <= 0) {
            _uiState.value = state.copy(errorMessage = "Role karyawan harus dipilih.")
            return
        }

        // Cek maksimal 5 karyawan per outlet
        val outletId = state.formOutletId.takeIf { it > 0 }
            ?: (state.outlets.firstOrNull()?.id ?: 0)
        val currentCount = state.karyawanList.count { it.outlet?.id == outletId }
        if (currentCount >= 5) {
            _uiState.value = state.copy(
                errorMessage = "Maksimal 5 karyawan per outlet. Hapus salah satu untuk menambah."
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)
            delay(500)

            val role = state.roles.find { it.id == state.formRoleId }
            val outlet = state.outlets.find { it.id == outletId }

            val newKaryawan = Karyawan(
                id = (nextKaryawanId++).toString(),
                namaLengkap = state.formNama.trim(),
                alamat = state.formAlamat.trim(),
                role = role,
                outlet = outlet
            )

            _uiState.value = _uiState.value.copy(
                karyawanList = _uiState.value.karyawanList + newKaryawan,
                isSaving = false,
                dialogMode = KaryawanDialogMode.Closed,
                formNama = "",
                formAlamat = "",
                formRoleId = 0,
                formOutletId = 0,
                successMessage = "Karyawan \"${newKaryawan.namaLengkap}\" berhasil ditambahkan."
            )
        }
    }

    /**
     * Memperbarui data karyawan dari dialog edit.
     * TODO: Panggil API PUT /api/karyawan/{id}
     */
    fun updateKaryawan() {
        val state = _uiState.value
        val id = state.editingKaryawanId ?: return

        if (state.formNama.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Nama karyawan harus diisi.")
            return
        }
        if (state.formAlamat.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Alamat karyawan harus diisi.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)
            delay(500)

            val outletId = state.formOutletId.takeIf { it > 0 }
            val outlet = state.outlets.find { it.id == outletId }

            val updatedKaryawan = state.karyawanList.find { it.id == id }?.copy(
                namaLengkap = state.formNama.trim(),
                alamat = state.formAlamat.trim(),
                outlet = outlet
            )

            if (updatedKaryawan != null) {
                _uiState.value = _uiState.value.copy(
                    karyawanList = _uiState.value.karyawanList.map {
                        if (it.id == id) updatedKaryawan else it
                    },
                    isSaving = false,
                    dialogMode = KaryawanDialogMode.Closed,
                    successMessage = "Karyawan berhasil diperbarui."
                )
            }
        }
    }

    /**
     * Menghapus karyawan dari dialog edit.
     * TODO: Panggil API DELETE /api/karyawan/{id}
     */
    fun hapusKaryawan(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            delay(300)

            _uiState.value = _uiState.value.copy(
                karyawanList = _uiState.value.karyawanList.filter { it.id != id },
                isSaving = false,
                dialogMode = KaryawanDialogMode.Closed,
                successMessage = "Karyawan berhasil dihapus."
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
