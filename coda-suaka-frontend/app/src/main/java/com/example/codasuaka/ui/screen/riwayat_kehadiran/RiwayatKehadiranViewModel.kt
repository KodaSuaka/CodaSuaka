package com.example.codasuaka.ui.screen.riwayat_kehadiran

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.codasuaka.data.remote.dto.PengajuanDto
import com.example.codasuaka.data.remote.dto.PresensiDto
import com.example.codasuaka.data.remote.dto.RekapKehadiranDto
import com.example.codasuaka.domain.repository.OutletRepository
import com.example.codasuaka.domain.repository.PengajuanRepository
import com.example.codasuaka.domain.repository.PresensiRepository
import com.example.codasuaka.ui.screen.kelola_outlet.Outlet
import com.example.codasuaka.util.DateTimeUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.YearMonth

enum class TabRiwayat(val label: String) { LOG_PRESENSI("Log Presensi"), PERSETUJUAN("Persetujuan") }
enum class StatusKehadiran(val label: String) { HADIR("Hadir"), TERLAMBAT("Terlambat"), IZIN("Izin"), SAKIT("Sakit"), ALPHA("Alpha") }
enum class StatusPersetujuan(val label: String) { PENDING("Pending"), DISETUJUI("Disetujui"), DITOLAK("Ditolak") }

data class Presensi(val id: String = "", val karyawanId: Int = 0, val namaKaryawan: String = "", val outlet: String = "", val outletId: Int = 0, val role: String = "", val jamKehadiran: String = "", val status: StatusKehadiran = StatusKehadiran.HADIR)
data class PengajuanPersetujuan(val id: String = "", val karyawanId: Int = 0, val namaKaryawan: String = "", val outlet: String = "", val outletId: Int = 0, val alasanIzin: String = "", val tanggal: String = "", val statusPersetujuan: StatusPersetujuan = StatusPersetujuan.PENDING)
data class RekapKaryawan(val karyawanId: Int = 0, val namaKaryawan: String = "", val role: String = "", val outlet: String = "", val outletId: Int = 0, val totalHadir: Int = 0, val totalTerlambat: Int = 0, val totalIzin: Int = 0, val totalSakit: Int = 0, val totalAlpha: Int = 0)
data class RekapBulanan(val tahun: Int = 2026, val bulan: Int = 0, val rekapKaryawan: List<RekapKaryawan> = emptyList(), val totalHadir: Int = 0, val totalTerlambat: Int = 0, val totalIzin: Int = 0, val totalSakit: Int = 0, val totalAlpha: Int = 0)

data class RiwayatKehadiranUiState(
    val selectedTab: TabRiwayat = TabRiwayat.LOG_PRESENSI,
    val outlets: List<Outlet> = emptyList(),
    val selectedOutletId: Int? = null,
    val selectedDate: String = "",
    val isLoading: Boolean = false,
    val isApproving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val presensiList: List<Presensi> = emptyList(),
    val persetujuanList: List<PengajuanPersetujuan> = emptyList(),
    val rekapBulanan: RekapBulanan = RekapBulanan(),
    val currentRecapMonth: YearMonth = YearMonth.now(DateTimeUtil.zoneId)
)

class RiwayatKehadiranViewModel(
    private val presensiRepository: PresensiRepository,
    private val pengajuanRepository: PengajuanRepository,
    private val outletRepository: OutletRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(RiwayatKehadiranUiState())
    val uiState: StateFlow<RiwayatKehadiranUiState> = _uiState


    private var pollingJob: kotlinx.coroutines.Job? = null

    init {
        loadInitialData()
        startPeriodicPolling()
    }

    private fun startPeriodicPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(POLL_INTERVAL_MS)
                refreshData(showLoading = false)
            }
        }
    }

    private fun refreshData(showLoading: Boolean = true) {
        if (showLoading) _uiState.update { it.copy(isLoading = true) }
        loadPresensi()
        loadPersetujuan()
        loadRekapBulanan()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            outletRepository.getOutlets().onSuccess { dtos ->
                val loaded = dtos.map { Outlet(it.id, it.namaOutlet, it.alamatOutlet ?: "") }
                _uiState.update { it.copy(outlets = loaded, selectedOutletId = loaded.firstOrNull()?.id, selectedDate = java.time.LocalDate.now().toString(), isLoading = false) }
                loadPresensi(); loadPersetujuan(); loadRekapBulanan()
            }
        }
    }

    fun onTabSelected(tab: TabRiwayat) = _uiState.update { it.copy(selectedTab = tab) }
    fun onOutletSelected(id: Int?) { _uiState.update { it.copy(selectedOutletId = id) }; refreshData() }
    fun onDateSelected(date: String) { _uiState.update { it.copy(selectedDate = date) }; loadPresensi() }

    fun onRecapPrevMonth() { _uiState.update { it.copy(currentRecapMonth = it.currentRecapMonth.minusMonths(1)) }; loadRekapBulanan() }
    fun onRecapNextMonth() { _uiState.update { it.copy(currentRecapMonth = it.currentRecapMonth.plusMonths(1)) }; loadRekapBulanan() }
    fun onRecapMonthYearSelected(m: Int, y: Int) { _uiState.update { it.copy(currentRecapMonth = YearMonth.of(y, m + 1)) }; loadRekapBulanan() }

    fun onRefresh() {
        refreshData(showLoading = true)
    }

    private fun loadPresensi() {
        viewModelScope.launch {
            val date = _uiState.value.selectedDate
            presensiRepository.getPresensis(if (date.isEmpty()) null else date).onSuccess { dtos ->
                val mapped = dtos.map { it.toPresensi() }
                val filtered = if (_uiState.value.selectedOutletId != null) mapped.filter { it.outletId == _uiState.value.selectedOutletId } else mapped
                _uiState.update { it.copy(presensiList = filtered, isLoading = false) }
            }
        }
    }

    private fun loadPersetujuan() {
        viewModelScope.launch {
            pengajuanRepository.getPengajuans().onSuccess { dtos ->
                val mapped = dtos.map { it.toPengajuanPersetujuan() }
                val filtered = if (_uiState.value.selectedOutletId != null) mapped.filter { it.outletId == _uiState.value.selectedOutletId } else mapped
                _uiState.update { it.copy(persetujuanList = filtered) }
            }
        }
    }

    private fun loadRekapBulanan() {
        viewModelScope.launch {
            val date = _uiState.value.currentRecapMonth
            presensiRepository.getRekapKehadiran(date.monthValue, date.year).onSuccess { dtos ->
                val mapped = dtos.map { it.toRekapKaryawan() }
                val filtered = if (_uiState.value.selectedOutletId != null) mapped.filter { it.outletId == _uiState.value.selectedOutletId } else mapped
                _uiState.update { it.copy(rekapBulanan = RekapBulanan(date.year, date.monthValue - 1, filtered, filtered.sumOf { it.totalHadir }, 0, filtered.sumOf { it.totalIzin }, filtered.sumOf { it.totalSakit }, filtered.sumOf { it.totalAlpha })) }
            }
        }
    }

    fun setujuiPersetujuan(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isApproving = true) }
            pengajuanRepository.approvePengajuan(id.toInt()).onSuccess {
                _uiState.update { s -> s.copy(persetujuanList = s.persetujuanList.map { if (it.id == id) it.copy(statusPersetujuan = StatusPersetujuan.DISETUJUI) else it }, successMessage = "Berhasil disetujui", isApproving = false) }
                refreshData(showLoading = false)
            }.onFailure { e ->
                _uiState.update { it.copy(isApproving = false, errorMessage = e.message) }
            }
        }
    }

    fun tolakPersetujuan(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isApproving = true) }
            pengajuanRepository.rejectPengajuan(id.toInt(), "Ditolak admin").onSuccess {
                _uiState.update { s -> s.copy(persetujuanList = s.persetujuanList.map { if (it.id == id) it.copy(statusPersetujuan = StatusPersetujuan.DITOLAK) else it }, successMessage = "Berhasil ditolak", isApproving = false) }
                refreshData(showLoading = false)
            }.onFailure { e ->
                _uiState.update { it.copy(isApproving = false, errorMessage = e.message) }
            }
        }
    }

    fun clearMessages() = _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    fun clearError() = _uiState.update { it.copy(errorMessage = null) }
    fun clearSuccess() = _uiState.update { it.copy(successMessage = null) }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
    }

    companion object {
        private const val POLL_INTERVAL_MS = 30_000L

        fun PresensiDto.toPresensi() = Presensi(id.toString(), userId ?: 0, user?.name ?: "", "", user?.outletId ?: 0, user?.role?.namaRole ?: "", DateTimeUtil.formatIsoToTime(jamCheckin), when (status?.lowercase()) { "terlambat" -> StatusKehadiran.TERLAMBAT; "izin" -> StatusKehadiran.IZIN; "sakit" -> StatusKehadiran.SAKIT; "alpha" -> StatusKehadiran.ALPHA; else -> StatusKehadiran.HADIR })
        fun PengajuanDto.toPengajuanPersetujuan() = PengajuanPersetujuan(id.toString(), userId, user?.name ?: "", "", user?.outletId ?: 0, keterangan ?: "", tanggalMulai ?: createdAt ?: "", when (status.lowercase()) { "disetujui" -> StatusPersetujuan.DISETUJUI; "ditolak" -> StatusPersetujuan.DITOLAK; else -> StatusPersetujuan.PENDING })
        fun RekapKehadiranDto.toRekapKaryawan() = RekapKaryawan(userId, namaLengkap ?: "", "", "", outletId ?: 0, totalHadir, 0, totalIzin, totalSakit, totalAlpha)
    }
}
