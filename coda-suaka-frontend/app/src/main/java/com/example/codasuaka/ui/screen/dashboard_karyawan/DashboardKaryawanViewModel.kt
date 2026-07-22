package com.example.codasuaka.ui.screen.dashboard_karyawan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.codasuaka.data.remote.dto.PenugasanDto
import com.example.codasuaka.domain.repository.DashboardRepository
import com.example.codasuaka.domain.repository.KaryawanRepository
import com.example.codasuaka.domain.repository.PengajuanRepository
import com.example.codasuaka.domain.repository.PenugasanRepository
import com.example.codasuaka.domain.repository.PresensiRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Status absensi (checkin / checkout).
 */
enum class AbsensiStatus {
    CHECKED_OUT,  // Belum checkin
    CHECKED_IN,   // Sudah checkin
    COMPLETED     // Sudah checkin & checkout
}

/**
 * Data class untuk data diri karyawan.
 */
data class EmployeeInfo(
    val id: String = "",
    val nama: String = "Karyawan",
    val jabatan: String = "Staff",
    val poinPerforma: Int = 0,
    val fotoUrl: String? = null
)

/**
 * Data class untuk tugas yang ditampilkan di daftar tugas.
 */
data class TugasItem(
    val id: Int,
    val judul: String,
    val tenggat: String,
    val isSelesai: Boolean = false
)

/**
 * State untuk halaman Dashboard Karyawan.
 */
data class DashboardKaryawanUiState(
    val employeeInfo: EmployeeInfo = EmployeeInfo(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,

    // ── Section Tengah: Menu Personal ──
    val absensiStatus: AbsensiStatus = AbsensiStatus.CHECKED_OUT,
    val absensiTime: String? = null,
    val specialEvent: String? = null,
    val showSpecialEvent: Boolean = false,

    // ── Section Tengah: Menu Jabatan ──
    val roleMenuItems: List<RoleMenuItem> = emptyList(),

    // ── Section Bawah 1 ──
    val poinKinerja: Int = 0,
    val totalTugas: Int = 0,
    val tugasSelesai: Int = 0,
    val daftarTugas: List<TugasItem> = emptyList(),

    // ── Section Bawah 2 ──
    val sisaCuti: Int = 12,
    val additionalContent: List<AdditionalMenuItem> = emptyList(),

    // ── Bottom Nav ──
    val selectedBottomNav: Int = 0, // 0 = Dashboard, 1 = Pengajuan, 2 = Pesan
    val hasUnreadMessages: Boolean = false
)

/**
 * Menu item untuk role / jabatan.
 */
data class RoleMenuItem(
    val id: String,
    val label: String,
    val iconResName: String,
    val route: String? = null
)

/**
 * Menu item tambahan (konten terikat role).
 */
data class AdditionalMenuItem(
    val id: String,
    val label: String,
    val iconResName: String,
    val route: String? = null
)

/**
 * ViewModel untuk Dashboard Karyawan.
 * Mengelola state data diri, absensi, tugas, cuti, dan navigasi.
 * Terintegrasi dengan API backend.
 */
class DashboardKaryawanViewModel(
    private val presensiRepository: PresensiRepository,
    private val penugasanRepository: PenugasanRepository,
    private val karyawanRepository: KaryawanRepository,
    private val pengajuanRepository: PengajuanRepository,
    private val dashboardRepository: DashboardRepository,
    private val chatRepository: com.example.codasuaka.domain.repository.ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardKaryawanUiState())
    val uiState: StateFlow<DashboardKaryawanUiState> = _uiState

    private var unreadCheckJob: Job? = null

    init {
        loadDashboardData()
        startUnreadMessagesPolling()
    }

    /**
     * Memuat data awal dashboard dari API.
     */
    private fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            try {
                // Muat data dari berbagai endpoint secara paralel
                val karyawanResult = karyawanRepository.getKaryawanMe()
                val presensiResult = presensiRepository.getPresensiToday()
                val tugasResult = penugasanRepository.getPenugasans(status = "belum,proses")
                val pengajuanResult = pengajuanRepository.getPengajuans()
                val dashboardResult = dashboardRepository.getKaryawanDashboard()

                karyawanResult.onSuccess { karyawan ->
                    _uiState.value = _uiState.value.copy(
                        employeeInfo = EmployeeInfo(
                            id = karyawan.id,
                            nama = karyawan.namaLengkap,
                            jabatan = karyawan.user?.role?.namaRole ?: "Staff",
                            poinPerforma = 0,
                            fotoUrl = karyawan.fotoProfil
                        ),
                        sisaCuti = karyawan.sisaCuti ?: 12
                    )
                }

                presensiResult.onSuccess { today ->
                    val status = when {
                        today.sudahCheckin && today.sudahCheckout -> AbsensiStatus.COMPLETED
                        today.sudahCheckin -> AbsensiStatus.CHECKED_IN
                        else -> AbsensiStatus.CHECKED_OUT
                    }
                    val time = when (status) {
                        AbsensiStatus.COMPLETED -> {
                            val checkin = today.presensi?.jamCheckin ?: "-"
                            val checkout = today.presensi?.jamCheckout ?: "-"
                            "$checkin - $checkout"
                        }
                        AbsensiStatus.CHECKED_IN -> today.presensi?.jamCheckin
                        else -> null
                    }
                    _uiState.value = _uiState.value.copy(
                        absensiStatus = status,
                        absensiTime = time
                    )
                }

                tugasResult.onSuccess { tugasList ->
                    val tugasItems = tugasList.map { tugas ->
                        TugasItem(
                            id = tugas.id,
                            judul = tugas.judul,
                            tenggat = tugas.tenggat ?: "-",
                            isSelesai = tugas.status == "selesai"
                        )
                    }
                    _uiState.value = _uiState.value.copy(
                        totalTugas = tugasList.size,
                        tugasSelesai = tugasList.count { it.status == "selesai" },
                        daftarTugas = tugasItems
                    )
                }

                pengajuanResult.onSuccess { pengajuanList ->
                    val pendingCount = pengajuanList.count { it.status == "pending" }
                }

                dashboardResult.onSuccess { dashboardData ->
                    val roleMenus = dashboardData.roleMenuItems?.map {
                        RoleMenuItem(it.id, it.label, it.icon, it.route)
                    } ?: emptyList()

                    val additionalItems = dashboardData.additionalContent?.map {
                        AdditionalMenuItem(it.id, it.label, it.icon, it.route)
                    } ?: emptyList()

                    _uiState.value = _uiState.value.copy(
                        roleMenuItems = roleMenus,
                        additionalContent = additionalItems
                    )
                }

                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }

    // ─── Absensi (Checkin / Checkout) ──────────────────────────

    /**
     * Melakukan checkin atau checkout via API.
     */
    fun toggleAbsensi() {
        viewModelScope.launch {
            val current = _uiState.value.absensiStatus
            if (current == AbsensiStatus.COMPLETED) return@launch
            
            _uiState.value = _uiState.value.copy(isLoading = true)

            if (current == AbsensiStatus.CHECKED_OUT) {
                // Checkin
                presensiRepository.checkin()
                    .onSuccess { presensi ->
                        _uiState.value = _uiState.value.copy(
                            absensiStatus = AbsensiStatus.CHECKED_IN,
                            absensiTime = presensi.jamCheckin?.let { "$it WIB" },
                            isLoading = false
                        )
                    }
                    .onFailure { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = error.message
                        )
                    }
            } else if (current == AbsensiStatus.CHECKED_IN) {
                // Checkout
                presensiRepository.checkout()
                    .onSuccess { presensi ->
                        val checkinTime = presensi.jamCheckin ?: "-"
                        val checkoutTime = presensi.jamCheckout ?: "-"
                        _uiState.value = _uiState.value.copy(
                            absensiStatus = AbsensiStatus.COMPLETED,
                            absensiTime = "$checkinTime - $checkoutTime",
                            isLoading = false
                        )
                    }
                    .onFailure { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = error.message
                        )
                    }
            }
        }
    }

    // ─── Bottom Navigation ─────────────────────────────────────

    fun onBottomNavSelected(index: Int) {
        _uiState.value = _uiState.value.copy(selectedBottomNav = index)
    }

    private fun startUnreadMessagesPolling() {
        unreadCheckJob?.cancel()
        unreadCheckJob = viewModelScope.launch {
            while (true) {
                chatRepository.getContacts().onSuccess { groups ->
                    val totalUnread = groups.sumOf { it.contacts.sumOf { c -> c.unreadCount } }
                    _uiState.value = _uiState.value.copy(hasUnreadMessages = totalUnread > 0)
                }
                delay(15_000L)
            }
        }
    }

    // ─── Error Handling ────────────────────────────────────────

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
