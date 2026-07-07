package com.example.codasuaka.ui.screen.kelola_karyawan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.codasuaka.data.remote.dto.CreateKaryawanRequest
import com.example.codasuaka.data.remote.dto.KaryawanDto
import com.example.codasuaka.data.remote.dto.OutletDto
import com.example.codasuaka.data.remote.dto.RoleDto
import com.example.codasuaka.data.remote.dto.UpdateKaryawanRequest
import com.example.codasuaka.domain.repository.KaryawanRepository
import com.example.codasuaka.domain.repository.OutletRepository
import com.example.codasuaka.ui.screen.kelola_outlet.Outlet
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
 */
class KelolaKaryawanViewModel(
    private val karyawanRepository: KaryawanRepository,
    private val outletRepository: OutletRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(KelolaKaryawanUiState())
    val uiState: StateFlow<KelolaKaryawanUiState> = _uiState

    init {
        loadInitialData()
    }

    /**
     * Memuat data awal (roles, outlets, daftar karyawan) dari API.
     */
    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            var loadedRoles = emptyList<Role>()
            var loadedOutlets = emptyList<Outlet>()
            var loadedKaryawan = emptyList<Karyawan>()
            var errorMsg: String? = null

            // Load roles
            karyawanRepository.getRoles().onSuccess { dtos ->
                loadedRoles = dtos.map { it.toRole() }
            }.onFailure {
                errorMsg = it.message
            }

            // Load outlets
            outletRepository.getOutlets().onSuccess { dtos ->
                loadedOutlets = dtos.map { it.toOutlet() }
            }.onFailure {
                errorMsg = errorMsg ?: it.message
            }

            // Load karyawan
            karyawanRepository.getKaryawans().onSuccess { dtos ->
                loadedKaryawan = dtos.map { it.toKaryawan(loadedRoles, loadedOutlets) }
            }.onFailure {
                errorMsg = errorMsg ?: it.message
            }

            _uiState.value = _uiState.value.copy(
                roles = loadedRoles,
                outlets = loadedOutlets,
                karyawanList = loadedKaryawan,
                isLoading = false,
                errorMessage = errorMsg
            )
        }
    }

    /**
     * Memuat ulang daftar karyawan, opsional filter berdasarkan outlet.
     */
    fun loadKaryawan(outletId: Int? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            karyawanRepository.getKaryawans(outletId).onSuccess { dtos ->
                _uiState.value = _uiState.value.copy(
                    karyawanList = dtos.map { it.toKaryawan(_uiState.value.roles, _uiState.value.outlets) },
                    isLoading = false,
                    selectedOutletId = outletId
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = it.message ?: "Gagal memuat karyawan"
                )
            }
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
     * Menyimpan karyawan baru ke API.
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

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)

            val outletId = state.formOutletId.takeIf { it > 0 }

            val request = CreateKaryawanRequest(
                namaLengkap = state.formNama.trim(),
                email = "${state.formNama.trim().lowercase().replace(" ", ".")}@email.com",
                password = "password123",
                alamat = state.formAlamat.trim(),
                roleId = state.formRoleId,
                outletId = outletId
            )

            karyawanRepository.createKaryawan(request).onSuccess { dto ->
                val newKaryawan = dto.toKaryawan(state.roles, state.outlets)
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
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = it.message ?: "Gagal menambahkan karyawan"
                )
            }
        }
    }

    /**
     * Memperbarui data karyawan via API.
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

            val request = UpdateKaryawanRequest(
                namaLengkap = state.formNama.trim(),
                alamat = state.formAlamat.trim(),
                outletId = state.formOutletId.takeIf { it > 0 }
            )

            karyawanRepository.updateKaryawan(id, request).onSuccess {
                // Reload karyawan list
                karyawanRepository.getKaryawans().onSuccess { dtos ->
                    _uiState.value = _uiState.value.copy(
                        karyawanList = dtos.map { dto -> dto.toKaryawan(state.roles, state.outlets) },
                        isSaving = false,
                        dialogMode = KaryawanDialogMode.Closed,
                        successMessage = "Karyawan berhasil diperbarui."
                    )
                }.onFailure {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        dialogMode = KaryawanDialogMode.Closed,
                        successMessage = "Karyawan berhasil diperbarui."
                    )
                }
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = it.message ?: "Gagal memperbarui karyawan"
                )
            }
        }
    }

    /**
     * Menghapus karyawan via API.
     */
    fun hapusKaryawan(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)

            karyawanRepository.deleteKaryawan(id).onSuccess {
                _uiState.value = _uiState.value.copy(
                    karyawanList = _uiState.value.karyawanList.filter { it.id != id },
                    isSaving = false,
                    dialogMode = KaryawanDialogMode.Closed,
                    successMessage = "Karyawan berhasil dihapus."
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = it.message ?: "Gagal menghapus karyawan"
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
        fun RoleDto.toRole() = Role(id = this.id, namaRole = this.namaRole)

        fun OutletDto.toOutlet() = Outlet(
            id = this.id,
            namaOutlet = this.namaOutlet,
            alamatOutlet = this.alamatOutlet ?: ""
        )

        fun KaryawanDto.toKaryawan(roles: List<Role>, outlets: List<Outlet>): Karyawan {
            val role = this.user?.role?.let { roleDto ->
                roles.find { it.id == roleDto.id }
            }
            val outlet = this.outlet?.let { outletDto ->
                outlets.find { it.id == outletDto.id }
            }
            return Karyawan(
                id = this.id,
                namaLengkap = this.namaLengkap,
                alamat = this.alamat ?: "",
                kontak = this.kontak ?: "",
                fotoProfil = this.fotoProfil,
                role = role,
                outlet = outlet
            )
        }
    }
}
