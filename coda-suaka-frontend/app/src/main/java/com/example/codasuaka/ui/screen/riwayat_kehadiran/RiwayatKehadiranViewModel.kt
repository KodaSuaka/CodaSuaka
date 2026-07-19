package com.example.codasuaka.ui.screen.riwayat_kehadiran

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.codasuaka.data.remote.dto.PengajuanDto
import com.example.codasuaka.data.remote.dto.PresensiDto
import com.example.codasuaka.data.remote.dto.RekapKehadiranDto
import com.example.codasuaka.data.remote.dto.RejectPengajuanRequest
import com.example.codasuaka.domain.repository.OutletRepository
import com.example.codasuaka.domain.repository.PengajuanRepository
import com.example.codasuaka.domain.repository.PresensiRepository
import com.example.codasuaka.ui.screen.kelola_outlet.Outlet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// ─── Enums ───

enum class TabRiwayat(val label: String) {
    LOG_PRESENSI("Log Presensi"),
    PERSETUJUAN("Persetujuan")
}

enum class StatusKehadiran(val label: String) {
    HADIR("Hadir"),
    TERLAMBAT("Terlambat"),
    IZIN("Izin"),
    SAKIT("Sakit"),
    ALPHA("Alpha")
}

enum class StatusPersetujuan(val label: String) {
    PENDING("Pending"),
    DISETUJUI("Disetujui"),
    DITOLAK("Ditolak")
}

// ─── Data Models ───

data class Presensi(
    val id: String = "",
    val karyawanId: Int = 0,
    val namaKaryawan: String = "",
    val outlet: String = "",
    val outletId: Int = 0,
    val role: String = "",
    val jamKehadiran: String = "",
    val status: StatusKehadiran = StatusKehadiran.HADIR
)

data class PengajuanPersetujuan(
    val id: String = "",
    val karyawanId: Int = 0,
    val namaKaryawan: String = "",
    val outlet: String = "",
    val outletId: Int = 0,
    val alasanIzin: String = "",
    val tanggal: String = "",
    val statusPersetujuan: StatusPersetujuan = StatusPersetujuan.PENDING
)

data class RekapKaryawan(
    val karyawanId: Int = 0,
    val namaKaryawan: String = "",
    val role: String = "",
    val outlet: String = "",
    val outletId: Int = 0,
    val totalHadir: Int = 0,
    val totalTerlambat: Int = 0,
    val totalIzin: Int = 0,
    val totalSakit: Int = 0,
    val totalAlpha: Int = 0
) {
    val totalKehadiran: Int
        get() = totalHadir + totalTerlambat + totalIzin + totalSakit + totalAlpha
}

data class RekapBulanan(
    val tahun: Int = 2026,
    val bulan: Int = 0, // 0-based
    val rekapKaryawan: List<RekapKaryawan> = emptyList(),
    val totalHadir: Int = 0,
    val totalTerlambat: Int = 0,
    val totalIzin: Int = 0,
    val totalSakit: Int = 0,
    val totalAlpha: Int = 0
)

// ─── UI State ───

data class RiwayatKehadiranUiState(
    val selectedTab: TabRiwayat = TabRiwayat.LOG_PRESENSI,
    val outlets: List<Outlet> = emptyList(),
    val selectedOutletId: Int? = null,
    val selectedDate: String = "", // format: yyyy-MM-dd
    val isLoading: Boolean = false,
    val isApproving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,

    // ── Tab: Log Presensi ──
    val presensiList: List<Presensi> = emptyList(),

    // ── Tab: Persetujuan ──
    val persetujuanList: List<PengajuanPersetujuan> = emptyList(),

    // ── Rekap Bulanan ──
    val rekapBulanan: RekapBulanan = RekapBulanan(),
    val recapMonthOffset: Int = 0 // 0 = bulan saat ini
)

// ─── ViewModel ───

/**
 * ViewModel untuk halaman Riwayat Kehadiran.
 */
class RiwayatKehadiranViewModel(
    private val presensiRepository: PresensiRepository,
    private val pengajuanRepository: PengajuanRepository,
    private val outletRepository: OutletRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RiwayatKehadiranUiState())
    val uiState: StateFlow<RiwayatKehadiranUiState> = _uiState

    init {
        loadInitialData()
    }

    // ─── Load Initial Data ───

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            var loadedOutlets = emptyList<Outlet>()
            var errorMsg: String? = null

            outletRepository.getOutlets().onSuccess { dtos ->
                loadedOutlets = dtos.map { Outlet(id = it.id, namaOutlet = it.namaOutlet, alamatOutlet = it.alamatOutlet ?: "") }
            }.onFailure {
                errorMsg = it.message
            }

            // Set default date (hari ini)
            val today = java.time.LocalDate.now().toString()

            _uiState.value = _uiState.value.copy(
                outlets = loadedOutlets,
                selectedOutletId = loadedOutlets.firstOrNull()?.id,
                selectedDate = today,
                isLoading = false,
                errorMessage = errorMsg
            )

            // Muat data default
            loadPresensi()
            loadPersetujuan()
            loadRekapBulanan()
        }
    }

    // ─── Tab Selection ───

    fun onTabSelected(tab: TabRiwayat) {
        _uiState.value = _uiState.value.copy(selectedTab = tab, errorMessage = null)
    }

    // ─── Outlet Filter ───

    fun onOutletSelected(outletId: Int?) {
        _uiState.value = _uiState.value.copy(selectedOutletId = outletId, errorMessage = null)
        loadPresensi()
        loadPersetujuan()
        loadRekapBulanan()
    }

    // ─── Date Filter ───

    fun onDateSelected(date: String) {
        _uiState.value = _uiState.value.copy(selectedDate = date, errorMessage = null)
        loadPresensi()
    }

    // ─── Recap Month Navigation ───

    fun onRecapPrevMonth() {
        val current = _uiState.value.recapMonthOffset
        _uiState.value = _uiState.value.copy(recapMonthOffset = current - 1)
        loadRekapBulanan()
    }

    fun onRecapNextMonth() {
        val current = _uiState.value.recapMonthOffset
        _uiState.value = _uiState.value.copy(recapMonthOffset = current + 1)
        loadRekapBulanan()
    }

    // ─── Load Presensi ───

    /**
     * Memuat daftar presensi berdasarkan outlet & tanggal terpilih dari API.
     */
    private fun loadPresensi() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val state = _uiState.value
            val result = presensiRepository.getPresensis(tanggal = state.selectedDate.ifEmpty { null })

            result.onSuccess { dtos ->
                val mapped = dtos.map { it.toPresensi() }
                val filtered = if (state.selectedOutletId != null) {
                    mapped.filter { it.outletId == state.selectedOutletId }
                } else {
                    mapped
                }
                _uiState.value = _uiState.value.copy(
                    presensiList = filtered,
                    isLoading = false
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = it.message ?: "Gagal memuat presensi"
                )
            }
        }
    }

    // ─── Load Persetujuan ───

    /**
     * Memuat daftar pengajuan persetujuan dari API.
     */
    private fun loadPersetujuan() {
        viewModelScope.launch {
            val state = _uiState.value

            val result = pengajuanRepository.getPengajuans()
            result.onSuccess { dtos ->
                val mapped = dtos.map { it.toPengajuanPersetujuan() }
                val filtered = if (state.selectedOutletId != null) {
                    mapped.filter { it.outletId == state.selectedOutletId }
                } else {
                    mapped
                }
                _uiState.value = _uiState.value.copy(persetujuanList = filtered)
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    errorMessage = it.message ?: "Gagal memuat persetujuan"
                )
            }
        }
    }

    // ─── Load Rekap Bulanan ───

    /**
     * Memuat rekap bulanan dari API.
     */
    private fun loadRekapBulanan() {
        viewModelScope.launch {
            val state = _uiState.value
            val offset = state.recapMonthOffset
            val now = java.time.LocalDate.now()
            val targetDate = now.plusMonths(offset.toLong())
            val tahun = targetDate.year
            val bulan = targetDate.monthValue // 1-based

            val result = presensiRepository.getRekapKehadiran(bulan = bulan, tahun = tahun)
            result.onSuccess { dtos ->
                val mapped = dtos.map { it.toRekapKaryawan() }
                val filtered = if (state.selectedOutletId != null) {
                    mapped.filter { it.outletId == state.selectedOutletId }
                } else {
                    mapped
                }

                val totalHadir = filtered.sumOf { it.totalHadir }
                val totalTerlambat = filtered.sumOf { it.totalTerlambat }
                val totalIzin = filtered.sumOf { it.totalIzin }
                val totalSakit = filtered.sumOf { it.totalSakit }
                val totalAlpha = filtered.sumOf { it.totalAlpha }

                _uiState.value = _uiState.value.copy(
                    rekapBulanan = RekapBulanan(
                        tahun = tahun,
                        bulan = bulan - 1, // 0-based for display
                        rekapKaryawan = filtered,
                        totalHadir = totalHadir,
                        totalTerlambat = totalTerlambat,
                        totalIzin = totalIzin,
                        totalSakit = totalSakit,
                        totalAlpha = totalAlpha
                    )
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    errorMessage = it.message ?: "Gagal memuat rekap bulanan"
                )
            }
        }
    }

    // ─── Approve / Reject ───

    /**
     * Menyetujui pengajuan izin via API.
     */
    fun setujuiPersetujuan(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isApproving = true)

            pengajuanRepository.approvePengajuan(id.toIntOrNull() ?: return@launch)
                .onSuccess {
                    val updatedList = _uiState.value.persetujuanList.map {
                        if (it.id == id) it.copy(statusPersetujuan = StatusPersetujuan.DISETUJUI)
                        else it
                    }
                    _uiState.value = _uiState.value.copy(
                        persetujuanList = updatedList,
                        isApproving = false,
                        successMessage = "Pengajuan berhasil disetujui."
                    )
                }.onFailure {
                    _uiState.value = _uiState.value.copy(
                        isApproving = false,
                        errorMessage = it.message ?: "Gagal menyetujui pengajuan"
                    )
                }
        }
    }

    /**
     * Menolak pengajuan izin via API.
     */
    fun tolakPersetujuan(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isApproving = true)

            pengajuanRepository.rejectPengajuan(
                id = id.toIntOrNull() ?: return@launch,
                alasan = "Ditolak oleh admin"
            ).onSuccess {
                val updatedList = _uiState.value.persetujuanList.map {
                    if (it.id == id) it.copy(statusPersetujuan = StatusPersetujuan.DITOLAK)
                    else it
                }
                _uiState.value = _uiState.value.copy(
                    persetujuanList = updatedList,
                    isApproving = false,
                    successMessage = "Pengajuan berhasil ditolak."
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isApproving = false,
                    errorMessage = it.message ?: "Gagal menolak pengajuan"
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }

    companion object {
        fun PresensiDto.toPresensi(): Presensi {
            val status = when (this.status?.lowercase()) {
                "terlambat" -> StatusKehadiran.TERLAMBAT
                "izin" -> StatusKehadiran.IZIN
                "sakit" -> StatusKehadiran.SAKIT
                "alpha" -> StatusKehadiran.ALPHA
                else -> StatusKehadiran.HADIR
            }
            return Presensi(
                id = this.id.toString(),
                karyawanId = this.userId ?: 0,
                namaKaryawan = this.user?.name ?: "",
                outlet = "",
                outletId = 0,
                role = this.user?.role?.namaRole ?: "",
                jamKehadiran = this.jamCheckin ?: "",
                status = status
            )
        }

        fun PengajuanDto.toPengajuanPersetujuan(): PengajuanPersetujuan {
            val status = when (this.status.lowercase()) {
                "disetujui" -> StatusPersetujuan.DISETUJUI
                "ditolak" -> StatusPersetujuan.DITOLAK
                else -> StatusPersetujuan.PENDING
            }
            return PengajuanPersetujuan(
                id = this.id.toString(),
                karyawanId = this.userId,
                namaKaryawan = this.user?.name ?: "",
                outlet = "",
                outletId = 0,
                alasanIzin = this.keterangan ?: "",
                tanggal = this.tanggalMulai ?: this.createdAt ?: "",
                statusPersetujuan = status
            )
        }

        fun RekapKehadiranDto.toRekapKaryawan(): RekapKaryawan {
            return RekapKaryawan(
                karyawanId = this.userId,
                namaKaryawan = this.namaLengkap ?: "",
                totalHadir = this.totalHadir,
                totalTerlambat = 0, // tidak tersedia dari API rekap
                totalIzin = this.totalIzin,
                totalSakit = this.totalSakit,
                totalAlpha = this.totalAlpha
            )
        }
    }
}
