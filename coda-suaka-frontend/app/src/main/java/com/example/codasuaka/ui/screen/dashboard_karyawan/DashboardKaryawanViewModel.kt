package com.example.codasuaka.ui.screen.dashboard_karyawan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Status absensi (checkin / checkout).
 */
enum class AbsensiStatus {
    CHECKED_OUT,  // Belum checkin / sudah checkout
    CHECKED_IN    // Sudah checkin
}

/**
 * Data class untuk data diri karyawan.
 */
data class EmployeeInfo(
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
    // ── Section Atas: Data Diri ──
    val employeeInfo: EmployeeInfo = EmployeeInfo(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,

    // ── Section Tengah: Menu Personal ──
    val absensiStatus: AbsensiStatus = AbsensiStatus.CHECKED_OUT,
    val absensiTime: String? = null, // jam checkin/checkout terakhir
    val specialEvent: String? = null, // event dari jadwal operasional (random, nullable)
    val showSpecialEvent: Boolean = false, // kadang muncul, kadang tidak

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
    val selectedBottomNav: Int = 0 // 0 = Dashboard, 1 = Pengajuan, 2 = Pesan
)

/**
 * Menu item untuk role / jabatan.
 */
data class RoleMenuItem(
    val id: String,
    val label: String,
    val iconResName: String, // nama ikon, akan dipetakan ke ikon Kompose
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
 */
class DashboardKaryawanViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardKaryawanUiState())
    val uiState: StateFlow<DashboardKaryawanUiState> = _uiState

    init {
        loadDashboardData()
    }

    /**
     * Memuat data awal dashboard.
     * Untuk tahap awal menggunakan data dummy.
     * TODO: Integrasi dengan API endpoint GET /api/karyawan/me, GET /api/absensi/status, dll.
     */
    private fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            // Simulasi loading — ganti dengan panggilan API sesungguhnya
            delay(800)

            // Data dummy (nanti diganti dengan response API)
            _uiState.value = _uiState.value.copy(
                employeeInfo = EmployeeInfo(
                    nama = "Ahmad Fauzi",
                    jabatan = "Staff Operasional",
                    poinPerforma = 85
                ),
                absensiStatus = AbsensiStatus.CHECKED_OUT,
                absensiTime = null,
                specialEvent = "Hari Jadi Outlet ke-3 — Diskon 20%",
                showSpecialEvent = true,
                poinKinerja = 85,
                totalTugas = 5,
                tugasSelesai = 3,
                daftarTugas = listOf(
                    TugasItem(1, "Menyusun laporan stok harian", "2026-07-05", false),
                    TugasItem(2, "Pengecekan kebersihan area", "2026-07-04", true),
                    TugasItem(3, "Koordinasi dengan tim kebersihan", "2026-07-06", false)
                ),
                sisaCuti = 12,
                roleMenuItems = listOf(
                    RoleMenuItem("laporan", "Laporan", "Description", "laporan_keuangan"),
                    RoleMenuItem("absensi", "Riwayat Absensi", "FactCheck", "riwayat_kehadiran"),
                    RoleMenuItem("tugas", "Tugas Tim", "Assignment", "tugas_tim")
                ),
                additionalContent = listOf(
                    AdditionalMenuItem("pelatihan", "Pelatihan", "School", "pelatihan"),
                    AdditionalMenuItem("penghargaan", "Penghargaan", "EmojiEvents", "penghargaan")
                ),
                isLoading = false
            )
        }
    }

    // ─── Absensi (Checkin / Checkout) ──────────────────────────

    /**
     * Melakukan checkin atau checkout.
     * TODO: Panggil API POST /api/absensi/checkin atau /api/absensi/checkout
     */
    fun toggleAbsensi() {
        viewModelScope.launch {
            val current = _uiState.value.absensiStatus
            _uiState.value = _uiState.value.copy(isLoading = true)

            // Simulasi request API
            delay(600)

            if (current == AbsensiStatus.CHECKED_OUT) {
                // Checkin
                _uiState.value = _uiState.value.copy(
                    absensiStatus = AbsensiStatus.CHECKED_IN,
                    absensiTime = "07:45 WIB",
                    isLoading = false
                )
            } else {
                // Checkout
                _uiState.value = _uiState.value.copy(
                    absensiStatus = AbsensiStatus.CHECKED_OUT,
                    absensiTime = "16:30 WIB",
                    isLoading = false
                )
            }
        }
    }

    // ─── Bottom Navigation ─────────────────────────────────────

    fun onBottomNavSelected(index: Int) {
        _uiState.value = _uiState.value.copy(selectedBottomNav = index)
    }

    // ─── Error Handling ────────────────────────────────────────

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
