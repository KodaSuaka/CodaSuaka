package com.example.codasuaka.ui.screen.riwayat_kehadiran

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.codasuaka.ui.screen.kelola_outlet.Outlet
import kotlinx.coroutines.delay
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
 *
 * TODO: Integrasi dengan API backend:
 *   - GET /api/presensi?outlet_id=&tanggal=
 *   - GET /api/persetujuan?outlet_id=
 *   - GET /api/rekap-bulanan?tahun=&bulan=&outlet_id=
 *   - PUT /api/persetujuan/{id}/setujui
 *   - PUT /api/persetujuan/{id}/tolak
 */
class RiwayatKehadiranViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RiwayatKehadiranUiState())
    val uiState: StateFlow<RiwayatKehadiranUiState> = _uiState

    private var nextPresensiId = 1
    private var nextPersetujuanId = 1

    init {
        loadInitialData()
    }

    // ─── Load Initial Data ───

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            delay(600)

            // Dummy outlets
            val dummyOutlets = listOf(
                Outlet(id = 1, namaOutlet = "Outlet Pusat", alamatOutlet = "Jl. Merdeka No. 123, Jakarta"),
                Outlet(id = 2, namaOutlet = "Outlet Cabang", alamatOutlet = "Jl. Sudirman No. 45, Bandung")
            )

            // Set default date (hari ini)
            val today = java.time.LocalDate.now().toString()

            _uiState.value = _uiState.value.copy(
                outlets = dummyOutlets,
                selectedOutletId = 1,
                selectedDate = today,
                isLoading = false
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
     * Memuat daftar presensi berdasarkan outlet & tanggal terpilih.
     * TODO: Panggil API GET /api/presensi?outlet_id=&tanggal=
     */
    private fun loadPresensi() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            delay(400)

            val state = _uiState.value
            val outletId = state.selectedOutletId

            // Dummy data presensi
            val dummyPresensi = listOf(
                Presensi(
                    id = (nextPresensiId++).toString(),
                    karyawanId = 1,
                    namaKaryawan = "Ahmad Fauzi",
                    outlet = "Outlet Pusat",
                    outletId = 1,
                    role = "Kasir",
                    jamKehadiran = "07:15",
                    status = StatusKehadiran.HADIR
                ),
                Presensi(
                    id = (nextPresensiId++).toString(),
                    karyawanId = 2,
                    namaKaryawan = "Siti Rahmawati",
                    outlet = "Outlet Pusat",
                    outletId = 1,
                    role = "Koki",
                    jamKehadiran = "07:45",
                    status = StatusKehadiran.TERLAMBAT
                ),
                Presensi(
                    id = (nextPresensiId++).toString(),
                    karyawanId = 3,
                    namaKaryawan = "Budi Santoso",
                    outlet = "Outlet Cabang",
                    outletId = 2,
                    role = "Pelayan",
                    jamKehadiran = "07:05",
                    status = StatusKehadiran.HADIR
                ),
                Presensi(
                    id = (nextPresensiId++).toString(),
                    karyawanId = 4,
                    namaKaryawan = "Dewi Lestari",
                    outlet = "Outlet Pusat",
                    outletId = 1,
                    role = "Kurir",
                    jamKehadiran = "08:10",
                    status = StatusKehadiran.TERLAMBAT
                ),
                Presensi(
                    id = (nextPresensiId++).toString(),
                    karyawanId = 5,
                    namaKaryawan = "Rudi Hartono",
                    outlet = "Outlet Cabang",
                    outletId = 2,
                    role = "Pencuci",
                    jamKehadiran = "07:00",
                    status = StatusKehadiran.HADIR
                )
            )

            // Filter by outlet
            val filtered = if (outletId != null) {
                dummyPresensi.filter { it.outletId == outletId }
            } else {
                dummyPresensi
            }

            _uiState.value = _uiState.value.copy(
                presensiList = filtered,
                isLoading = false
            )
        }
    }

    // ─── Load Persetujuan ───

    /**
     * Memuat daftar pengajuan persetujuan berdasarkan outlet terpilih.
     * TODO: Panggil API GET /api/persetujuan?outlet_id=
     */
    private fun loadPersetujuan() {
        viewModelScope.launch {
            delay(300)

            val state = _uiState.value
            val outletId = state.selectedOutletId

            val reasons = listOf(
                "Izin Acara Keluarga",
                "Sakit Demam",
                "Keperluan Mendesak",
                "Izin Cuti Tahunan",
                "Urusan Pribadi"
            )

            val dummyPersetujuan = listOf(
                PengajuanPersetujuan(
                    id = (nextPersetujuanId++).toString(),
                    karyawanId = 1,
                    namaKaryawan = "Ahmad Fauzi",
                    outlet = "Outlet Pusat",
                    outletId = 1,
                    alasanIzin = reasons[0],
                    tanggal = "15 Juni 2026",
                    statusPersetujuan = StatusPersetujuan.PENDING
                ),
                PengajuanPersetujuan(
                    id = (nextPersetujuanId++).toString(),
                    karyawanId = 2,
                    namaKaryawan = "Siti Rahmawati",
                    outlet = "Outlet Pusat",
                    outletId = 1,
                    alasanIzin = reasons[1],
                    tanggal = "14 Juni 2026",
                    statusPersetujuan = StatusPersetujuan.DISETUJUI
                ),
                PengajuanPersetujuan(
                    id = (nextPersetujuanId++).toString(),
                    karyawanId = 3,
                    namaKaryawan = "Budi Santoso",
                    outlet = "Outlet Cabang",
                    outletId = 2,
                    alasanIzin = reasons[2],
                    tanggal = "13 Juni 2026",
                    statusPersetujuan = StatusPersetujuan.PENDING
                ),
                PengajuanPersetujuan(
                    id = (nextPersetujuanId++).toString(),
                    karyawanId = 4,
                    namaKaryawan = "Dewi Lestari",
                    outlet = "Outlet Pusat",
                    outletId = 1,
                    alasanIzin = reasons[3],
                    tanggal = "12 Juni 2026",
                    statusPersetujuan = StatusPersetujuan.DITOLAK
                ),
                PengajuanPersetujuan(
                    id = (nextPersetujuanId++).toString(),
                    karyawanId = 5,
                    namaKaryawan = "Rudi Hartono",
                    outlet = "Outlet Cabang",
                    outletId = 2,
                    alasanIzin = reasons[4],
                    tanggal = "11 Juni 2026",
                    statusPersetujuan = StatusPersetujuan.PENDING
                )
            )

            val filtered = if (outletId != null) {
                dummyPersetujuan.filter { it.outletId == outletId }
            } else {
                dummyPersetujuan
            }

            _uiState.value = _uiState.value.copy(persetujuanList = filtered)
        }
    }

    // ─── Load Rekap Bulanan ───

    /**
     * Memuat rekap bulanan — total kehadiran PER KARYAWAN dalam satu bulan.
     * TODO: Panggil API GET /api/rekap-bulanan?tahun=&bulan=&outlet_id=
     */
    private fun loadRekapBulanan() {
        viewModelScope.launch {
            delay(200)

            val state = _uiState.value
            val offset = state.recapMonthOffset
            val now = java.time.LocalDate.now()
            val targetDate = now.plusMonths(offset.toLong())
            val tahun = targetDate.year
            val bulan = targetDate.monthValue - 1 // 0-based

            val daysInMonth = java.time.YearMonth.of(tahun, targetDate.month).lengthOfMonth()
            val outletId = state.selectedOutletId

            // Dummy karyawan dengan akumulasi kehadiran masing-masing
            val dummyKaryawanRekap = listOf(
                RekapKaryawan(
                    karyawanId = 1,
                    namaKaryawan = "Ahmad Fauzi",
                    role = "Kasir",
                    outlet = "Outlet Pusat",
                    outletId = 1,
                    totalHadir = 18,
                    totalTerlambat = 2,
                    totalIzin = 1,
                    totalSakit = 0,
                    totalAlpha = 0
                ),
                RekapKaryawan(
                    karyawanId = 2,
                    namaKaryawan = "Siti Rahmawati",
                    role = "Koki",
                    outlet = "Outlet Pusat",
                    outletId = 1,
                    totalHadir = 15,
                    totalTerlambat = 1,
                    totalIzin = 2,
                    totalSakit = 1,
                    totalAlpha = 0
                ),
                RekapKaryawan(
                    karyawanId = 3,
                    namaKaryawan = "Budi Santoso",
                    role = "Pelayan",
                    outlet = "Outlet Cabang",
                    outletId = 2,
                    totalHadir = 20,
                    totalTerlambat = 0,
                    totalIzin = 0,
                    totalSakit = 0,
                    totalAlpha = 1
                ),
                RekapKaryawan(
                    karyawanId = 4,
                    namaKaryawan = "Dewi Lestari",
                    role = "Kurir",
                    outlet = "Outlet Pusat",
                    outletId = 1,
                    totalHadir = 14,
                    totalTerlambat = 3,
                    totalIzin = 1,
                    totalSakit = 2,
                    totalAlpha = 0
                ),
                RekapKaryawan(
                    karyawanId = 5,
                    namaKaryawan = "Rudi Hartono",
                    role = "Pencuci",
                    outlet = "Outlet Cabang",
                    outletId = 2,
                    totalHadir = 16,
                    totalTerlambat = 1,
                    totalIzin = 0,
                    totalSakit = 1,
                    totalAlpha = 1
                )
            )

            // Filter by outlet
            val filtered = if (outletId != null) {
                dummyKaryawanRekap.filter { it.outletId == outletId }
            } else {
                dummyKaryawanRekap
            }

            val totalHadir = filtered.sumOf { it.totalHadir }
            val totalTerlambat = filtered.sumOf { it.totalTerlambat }
            val totalIzin = filtered.sumOf { it.totalIzin }
            val totalSakit = filtered.sumOf { it.totalSakit }
            val totalAlpha = filtered.sumOf { it.totalAlpha }

            _uiState.value = _uiState.value.copy(
                rekapBulanan = RekapBulanan(
                    tahun = tahun,
                    bulan = bulan,
                    rekapKaryawan = filtered,
                    totalHadir = totalHadir,
                    totalTerlambat = totalTerlambat,
                    totalIzin = totalIzin,
                    totalSakit = totalSakit,
                    totalAlpha = totalAlpha
                )
            )
        }
    }

    // ─── Approve / Reject ───

    /**
     * Menyetujui pengajuan izin.
     * TODO: Panggil API PUT /api/persetujuan/{id}/setujui
     */
    fun setujuiPersetujuan(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isApproving = true)
            delay(400)

            val updatedList = _uiState.value.persetujuanList.map {
                if (it.id == id) it.copy(statusPersetujuan = StatusPersetujuan.DISETUJUI)
                else it
            }

            _uiState.value = _uiState.value.copy(
                persetujuanList = updatedList,
                isApproving = false,
                successMessage = "Pengajuan berhasil disetujui."
            )
        }
    }

    /**
     * Menolak pengajuan izin.
     * TODO: Panggil API PUT /api/persetujuan/{id}/tolak
     */
    fun tolakPersetujuan(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isApproving = true)
            delay(400)

            val updatedList = _uiState.value.persetujuanList.map {
                if (it.id == id) it.copy(statusPersetujuan = StatusPersetujuan.DITOLAK)
                else it
            }

            _uiState.value = _uiState.value.copy(
                persetujuanList = updatedList,
                isApproving = false,
                successMessage = "Pengajuan berhasil ditolak."
            )
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }
}
