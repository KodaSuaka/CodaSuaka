package com.example.codasuaka.ui.screen.dashboard_karyawan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.codasuaka.data.remote.dto.JadwalDto
import com.example.codasuaka.data.remote.dto.PenugasanDto
import com.example.codasuaka.domain.repository.DashboardRepository
import com.example.codasuaka.domain.repository.JadwalRepository
import com.example.codasuaka.domain.repository.KaryawanRepository
import com.example.codasuaka.domain.repository.PenugasanRepository
import com.example.codasuaka.domain.repository.PresensiRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

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
    val isSelesai: Boolean = false,
    val urgency: String? = null,
    val poin: Int? = null,
    val isTugasKhusus: Boolean = false
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
    val statusKeterangan: String? = null,
    val jamCheckinStandar: String? = null,
    val jamCheckoutStandar: String? = null,
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

    // ── Jadwal / Event Popup ──
    val jadwalList: List<JadwalDto> = emptyList(),
    val showJadwalDialog: Boolean = false,

    // ── Tugas Popup ──
    val showTugasDialog: Boolean = false,

    val isRefreshing: Boolean = false,

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
 */
class DashboardKaryawanViewModel(
    private val presensiRepository: PresensiRepository,
    private val penugasanRepository: PenugasanRepository,
    private val karyawanRepository: KaryawanRepository,
    private val dashboardRepository: DashboardRepository,
    private val jadwalRepository: JadwalRepository,
    private val chatRepository: com.example.codasuaka.domain.repository.ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardKaryawanUiState())
    val uiState: StateFlow<DashboardKaryawanUiState> = _uiState

    private var unreadCheckJob: Job? = null
    private val isProcessingAbsensi = AtomicBoolean(false)

    init {
        startUnreadMessagesPolling()
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            loadAllData()
        }
    }

    fun refreshDashboard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
            try {
                loadAllData()
            } finally {
                _uiState.update { it.copy(isRefreshing = false) }
            }
        }
    }

    private suspend fun loadAllData() {
        try {
            coroutineScope {
                val karyawanDef = async { karyawanRepository.getKaryawanMe() }
                val presensiDef = async { presensiRepository.getPresensiToday() }
                val tugasDef = async { penugasanRepository.getPenugasans(status = null) }
                val dashboardDef = async { dashboardRepository.getKaryawanDashboard() }
                val poinDef = async { dashboardRepository.getPoinKinerja() }
                val jadwalDef = async {
                    val now = java.time.LocalDate.now()
                    jadwalRepository.getJadwals(bulan = now.monthValue, tahun = now.year)
                }

                val karyawanRes = karyawanDef.await()
                val presensiRes = presensiDef.await()
                val tugasRes = tugasDef.await()
                val dashboardRes = dashboardDef.await()
                val poinRes = poinDef.await()
                val jadwalRes = jadwalDef.await()

                var firstError: String? = null

                _uiState.update { currentState ->
                    var newState = currentState

                    karyawanRes.onSuccess { karyawan ->
                        newState = newState.copy(
                            employeeInfo = newState.employeeInfo.copy(
                                id = karyawan.id,
                                nama = karyawan.namaLengkap,
                                jabatan = karyawan.user?.role?.namaRole ?: "Staff",
                                fotoUrl = karyawan.fotoProfil
                            ),
                            sisaCuti = karyawan.sisaCuti ?: 0
                        )
                    }.onFailure { firstError = firstError ?: it.message }

                    poinRes.onSuccess { poinData ->
                        newState = newState.copy(
                            employeeInfo = newState.employeeInfo.copy(poinPerforma = poinData.totalPoin),
                            poinKinerja = poinData.totalPoin
                        )
                    }.onFailure { firstError = firstError ?: it.message }

                    presensiRes.onSuccess { today ->
                        val status = when {
                            today.sudahCheckin && today.sudahCheckout -> AbsensiStatus.COMPLETED
                            today.sudahCheckin -> AbsensiStatus.CHECKED_IN
                            else -> AbsensiStatus.CHECKED_OUT
                        }
                        val time = when (status) {
                            AbsensiStatus.COMPLETED -> "${today.presensi?.jamCheckin ?: "-"} - ${today.presensi?.jamCheckout ?: "-"}"
                            AbsensiStatus.CHECKED_IN -> today.presensi?.jamCheckin
                            else -> null
                        }
                        newState = newState.copy(
                            absensiStatus = status,
                            absensiTime = time,
                            statusKeterangan = today.presensi?.statusKeterangan,
                            jamCheckinStandar = today.jamCheckinStandar,
                            jamCheckoutStandar = today.jamCheckoutStandar
                        )
                    }.onFailure { firstError = firstError ?: it.message }

                    tugasRes.onSuccess { tugasList ->
                        newState = newState.copy(
                            totalTugas = tugasList.size,
                            tugasSelesai = tugasList.count { it.status == "selesai" },
                            daftarTugas = tugasList.map {
                                TugasItem(
                                    id = it.id,
                                    judul = it.judul,
                                    tenggat = it.tenggat ?: "-",
                                    isSelesai = it.status == "selesai",
                                    urgency = it.urgency,
                                    poin = it.poin,
                                    isTugasKhusus = it.isTemplate != true
                                )
                            }
                        )
                    }.onFailure { firstError = firstError ?: it.message }

                    dashboardRes.onSuccess { data ->
                        newState = newState.copy(
                            roleMenuItems = data.roleMenuItems?.map { RoleMenuItem(it.id, it.label, it.icon, it.route) } ?: emptyList(),
                            additionalContent = data.additionalContent?.map { AdditionalMenuItem(it.id, it.label, it.icon, it.route) } ?: emptyList()
                        )
                    }.onFailure { firstError = firstError ?: it.message }

                    jadwalRes.onSuccess {
                        newState = newState.copy(jadwalList = it)
                    }.onFailure { firstError = firstError ?: it.message }

                    newState.copy(isLoading = false, errorMessage = firstError)
                }
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
        }
    }

    fun toggleAbsensi() {
        if (!isProcessingAbsensi.compareAndSet(false, true)) return
        viewModelScope.launch {
            val current = _uiState.value.absensiStatus
            if (current == AbsensiStatus.COMPLETED) {
                isProcessingAbsensi.set(false)
                return@launch
            }
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                when (current) {
                    AbsensiStatus.CHECKED_OUT -> {
                        presensiRepository.checkin().onSuccess { p ->
                            _uiState.update { it.copy(absensiStatus = AbsensiStatus.CHECKED_IN, absensiTime = p.jamCheckin, statusKeterangan = p.statusKeterangan, isLoading = false) }
                        }.onFailure { e -> _uiState.update { it.copy(isLoading = false, errorMessage = e.message) } }
                    }
                    AbsensiStatus.CHECKED_IN -> {
                        presensiRepository.checkout().onSuccess { p ->
                            _uiState.update { it.copy(absensiStatus = AbsensiStatus.COMPLETED, absensiTime = "${p.jamCheckin ?: "-"} - ${p.jamCheckout ?: "-"}", isLoading = false) }
                        }.onFailure { e -> _uiState.update { it.copy(isLoading = false, errorMessage = e.message) } }
                    }
                    else -> {}
                }
            } finally {
                isProcessingAbsensi.set(false)
            }
        }
    }

    fun toggleJadwalDialog(show: Boolean) = _uiState.update { it.copy(showJadwalDialog = show) }
    fun toggleTugasDialog(show: Boolean) = _uiState.update { it.copy(showTugasDialog = show) }
    fun onBottomNavSelected(index: Int) = _uiState.update { it.copy(selectedBottomNav = index) }

    private fun startUnreadMessagesPolling() {
        unreadCheckJob?.cancel()
        unreadCheckJob = viewModelScope.launch {
            while (true) {
                chatRepository.getContacts().onSuccess { groups ->
                    val totalUnread = groups.sumOf { it.contacts.sumOf { c -> c.unreadCount } }
                    _uiState.update { it.copy(hasUnreadMessages = totalUnread > 0) }
                }
                delay(15_000L)
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(errorMessage = null) }
}
