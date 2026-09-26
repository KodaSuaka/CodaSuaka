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
    val outlet: Outlet? = null,
    val sisaCuti: Int? = null,
    /** Format yyyy-MM-dd, null jika belum diisi. */
    val tanggalMulaiKerja: String? = null,
    // ── Biodata opsional ──
    val tempatLahir: String? = null,
    /** Format yyyy-MM-dd, null jika belum diisi. */
    val tanggalLahir: String? = null
) {
    /** Masa kerja dihitung on-the-fly dari [tanggalMulaiKerja], contoh: "2 tahun 3 bulan". */
    val masaKerja: String?
        get() {
            val mulai = tanggalMulaiKerja?.let {
                runCatching { java.time.LocalDate.parse(it) }.getOrNull()
            } ?: return null
            val period = java.time.Period.between(mulai, java.time.LocalDate.now())
            val bagian = buildList {
                if (period.years > 0) add("${period.years} tahun")
                if (period.months > 0) add("${period.months} bulan")
                if (isEmpty() && period.days >= 0) add("${period.days} hari")
            }
            return bagian.joinToString(" ")
        }
}

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
    /** Bug #15: Flag untuk UI — apakah ada outlet yang tersedia */
    val hasOutlets: Boolean = false,
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
    val formEmail: String = "",
    val formPassword: String = "",
    val formRoleId: Int = 0,
    val formOutletId: Int = 0,
    val formSisaCuti: String = "",
    /** Format yyyy-MM-dd. */
    val formTanggalMulaiKerja: String = "",
    // ── Biodata opsional ──
    val formTempatLahir: String = "",
    /** Format yyyy-MM-dd. */
    val formTanggalLahir: String = "",
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

            // Load roles — exclude platform-level roles (Super Admin & Owner)
            // karena pemilik dianggap entitas terpisah, bukan karyawan
            val excludedRoleNames = setOf("Super Admin", "Owner")
            karyawanRepository.getRoles().onSuccess { dtos ->
                loadedRoles = dtos
                    .filter { it.namaRole !in excludedRoleNames }
                    .map { it.toRole() }
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
                hasOutlets = loadedOutlets.isNotEmpty(),
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
            formEmail = "",
            formPassword = "",
            formRoleId = 0,
            formOutletId = 0,
            formTanggalMulaiKerja = "",
            formTempatLahir = "",
            formTanggalLahir = "",
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
            formEmail = "",
            formPassword = "",
            formRoleId = karyawan.role?.id ?: 0,
            formOutletId = karyawan.outlet?.id ?: 0,
            formSisaCuti = karyawan.sisaCuti?.toString() ?: "",
            formTanggalMulaiKerja = karyawan.tanggalMulaiKerja ?: "",
            formTempatLahir = karyawan.tempatLahir ?: "",
            formTanggalLahir = karyawan.tanggalLahir ?: "",
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
            formEmail = "",
            formPassword = "",
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

    fun onFormEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(formEmail = value, errorMessage = null)
    }

    fun onFormPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(formPassword = value, errorMessage = null)
    }

    fun onFormRoleChange(roleId: Int) {
        _uiState.value = _uiState.value.copy(formRoleId = roleId, errorMessage = null)
    }

    fun onFormOutletChange(outletId: Int) {
        _uiState.value = _uiState.value.copy(formOutletId = outletId, errorMessage = null)
    }

    fun onFormSisaCutiChange(value: String) {
        _uiState.value = _uiState.value.copy(formSisaCuti = value, errorMessage = null)
    }

    fun onFormTanggalMulaiKerjaChange(value: String) {
        _uiState.value = _uiState.value.copy(formTanggalMulaiKerja = value, errorMessage = null)
    }

    fun onFormTempatLahirChange(value: String) {
        _uiState.value = _uiState.value.copy(formTempatLahir = value, errorMessage = null)
    }

    fun onFormTanggalLahirChange(value: String) {
        _uiState.value = _uiState.value.copy(formTanggalLahir = value, errorMessage = null)
    }

    // ─── Actions ───

    /**
     * Menyimpan karyawan baru ke API.
     * Karyawan mendapat email dan password untuk login ke sistem.
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
        if (state.formEmail.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Email karyawan harus diisi untuk login.")
            return
        }
        if (state.formPassword.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Password karyawan harus diisi untuk login.")
            return
        }
        if (state.formPassword.length < 6) {
            _uiState.value = state.copy(errorMessage = "Password minimal 6 karakter.")
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
                email = state.formEmail.trim(),
                password = state.formPassword,
                alamat = state.formAlamat.trim(),
                tempatLahir = state.formTempatLahir.trim().ifBlank { null },
                tanggalLahir = state.formTanggalLahir.ifBlank { null },
                roleId = state.formRoleId,
                outletId = outletId,
                tanggalMulaiKerja = state.formTanggalMulaiKerja.ifBlank { null }
            )

            karyawanRepository.createKaryawan(request).onSuccess { dto ->
                val newKaryawan = dto.toKaryawan(state.roles, state.outlets)
                _uiState.value = _uiState.value.copy(
                    karyawanList = _uiState.value.karyawanList + newKaryawan,
                    isSaving = false,
                    dialogMode = KaryawanDialogMode.Closed,
                    formNama = "",
                    formAlamat = "",
                    formEmail = "",
                    formPassword = "",
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
                tempatLahir = state.formTempatLahir.trim().ifBlank { null },
                tanggalLahir = state.formTanggalLahir.ifBlank { null },
                outletId = state.formOutletId.takeIf { it > 0 },
                sisaCuti = state.formSisaCuti.toIntOrNull(),
                tanggalMulaiKerja = state.formTanggalMulaiKerja.ifBlank { null }
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
                outlet = outlet,
                sisaCuti = this.sisaCuti,
                tanggalMulaiKerja = this.tanggalMulaiKerja,
                tempatLahir = this.tempatLahir,
                tanggalLahir = this.tanggalLahir
            )
        }
    }
}
