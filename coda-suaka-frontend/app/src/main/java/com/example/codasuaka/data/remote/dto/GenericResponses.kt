package com.example.codasuaka.data.remote.dto

import com.google.gson.annotations.SerializedName

// ─── Generic Wrapper ───────────────────────────────────────────

data class ApiStatusResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String? = null
)

// ─── Dashboard DTOs ────────────────────────────────────────────

data class DashboardResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: DashboardData
)

data class DashboardData(
    @SerializedName("total_karyawan") val totalKaryawan: Int,
    @SerializedName("total_outlet") val totalOutlet: Int,
    @SerializedName("total_divisi") val totalDivisi: Int,
    @SerializedName("presensi_hari_ini") val presensiHariIni: Int,
    @SerializedName("pengajuan_pending") val pengajuanPending: Int,
    @SerializedName("tugas_stats") val tugasStats: TugasStats
)

data class TugasStats(
    @SerializedName("belum") val belum: Int,
    @SerializedName("proses") val proses: Int,
    @SerializedName("selesai") val selesai: Int
)

data class OmsetResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: OmsetData
)

data class OmsetData(
    @SerializedName("start_date") val startDate: String,
    @SerializedName("end_date") val endDate: String,
    @SerializedName("total_omset") val totalOmset: Long,
    @SerializedName("message") val message: String?
)

data class KaryawanDashboardResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: KaryawanDashboardData
)

data class KaryawanDashboardData(
    @SerializedName("karyawan") val karyawan: KaryawanDto?,
    @SerializedName("presensi_hari_ini") val presensiHariIni: PresensiDto?,
    @SerializedName("sudah_checkin") val sudahCheckin: Boolean,
    @SerializedName("sudah_checkout") val sudahCheckout: Boolean,
    @SerializedName("tugas_aktif") val tugasAktif: List<PenugasanDto>,
    @SerializedName("pengajuan_pending_count") val pengajuanPendingCount: Int,
    @SerializedName("sisa_cuti") val sisaCuti: Int,
    @SerializedName("role_menu_items") val roleMenuItems: List<RoleMenuDto>? = null,
    @SerializedName("additional_content") val additionalContent: List<AdditionalContentDto>? = null
)

data class RoleMenuDto(
    @SerializedName("id") val id: String,
    @SerializedName("label") val label: String,
    @SerializedName("icon") val icon: String,
    @SerializedName("route") val route: String?,
    @SerializedName("permission") val permission: String? = null
)

data class AdditionalContentDto(
    @SerializedName("id") val id: String,
    @SerializedName("label") val label: String,
    @SerializedName("icon") val icon: String,
    @SerializedName("route") val route: String? = null
)

// ─── Outlet DTOs ───────────────────────────────────────────────

data class OutletListResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: List<OutletDto>
)

data class OutletSingleResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: OutletDto?
)

data class OutletDto(
    @SerializedName("id") val id: Int,
    @SerializedName("nama_outlet") val namaOutlet: String,
    @SerializedName("alamat_outlet") val alamatOutlet: String?,
    @SerializedName("instansi_id") val instansiId: Int?,
    @SerializedName("is_active") val isActive: Boolean?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?
)

data class OutletRequest(
    @SerializedName("nama_outlet") val namaOutlet: String,
    @SerializedName("alamat_outlet") val alamatOutlet: String? = null
)

// ─── Role DTOs ─────────────────────────────────────────────────

data class RoleListResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: List<RoleDto>
)

data class RoleDto(
    @SerializedName("id") val id: Int,
    @SerializedName("nama_role") val namaRole: String
)

// ─── Karyawan DTOs ─────────────────────────────────────────────

data class KaryawanListResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: List<KaryawanDto>
)

data class KaryawanSingleResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: KaryawanDto?
)

data class KaryawanDto(
    @SerializedName("id") val id: String,
    @SerializedName("user_id") val userId: Int?,
    @SerializedName("nama_lengkap") val namaLengkap: String,
    @SerializedName("kontak") val kontak: String?,
    @SerializedName("alamat") val alamat: String?,
    @SerializedName("foto_profil") val fotoProfil: String?,
    @SerializedName("outlet_id") val outletId: Int?,
    @SerializedName("sisa_cuti") val sisaCuti: Int?,
    @SerializedName("user") val user: KaryawanUserDto?,
    @SerializedName("outlet") val outlet: OutletDto?
)

data class KaryawanUserDto(
    @SerializedName("id") val id: Int,
    @SerializedName("email") val email: String?,
    @SerializedName("role") val role: RoleDto?,
    @SerializedName("role_id") val roleId: Int?,
    @SerializedName("name") val name: String?
)

data class CreateKaryawanRequest(
    @SerializedName("nama_lengkap") val namaLengkap: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("kontak") val kontak: String? = null,
    @SerializedName("alamat") val alamat: String? = null,
    @SerializedName("role_id") val roleId: Int,
    @SerializedName("outlet_id") val outletId: Int? = null
)

data class UpdateKaryawanRequest(
    @SerializedName("nama_lengkap") val namaLengkap: String? = null,
    @SerializedName("kontak") val kontak: String? = null,
    @SerializedName("alamat") val alamat: String? = null,
    @SerializedName("outlet_id") val outletId: Int? = null,
    @SerializedName("sisa_cuti") val sisaCuti: Int? = null
)

// ─── Divisi DTOs ───────────────────────────────────────────────

data class DivisiListResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: List<DivisiDto>
)

data class DivisiSingleResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: DivisiDto?
)

data class DivisiDto(
    @SerializedName("id") val id: Int,
    @SerializedName("nama_divisi") val namaDivisi: String,
    @SerializedName("deskripsi") val deskripsi: String?,
    @SerializedName("ketua_karyawan_id") val ketuaKaryawanId: String?,
    @SerializedName("outlet_id") val outletId: Int?,
    @SerializedName("ketua_karyawan") val ketuaKaryawan: KaryawanDto?,
    @SerializedName("outlet") val outlet: OutletDto?,
    @SerializedName("anggota") val anggota: List<AnggotaDivisiDto>?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?
) {
    val anggotaCount: Int get() = anggota?.size ?: 0
}

data class CreateDivisiRequest(
    @SerializedName("nama_divisi") val namaDivisi: String,
    @SerializedName("deskripsi") val deskripsi: String? = null,
    @SerializedName("ketua_karyawan_id") val ketuaKaryawanId: String? = null,
    @SerializedName("outlet_id") val outletId: Int
)

data class UpdateDivisiRequest(
    @SerializedName("nama_divisi") val namaDivisi: String? = null,
    @SerializedName("deskripsi") val deskripsi: String? = null,
    @SerializedName("ketua_karyawan_id") val ketuaKaryawanId: String? = null,
    @SerializedName("outlet_id") val outletId: Int? = null
)

// ─── Anggota Divisi DTOs ───────────────────────────────────────

data class AnggotaDivisiListResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: List<AnggotaDivisiDto>
)

data class AnggotaDivisiDto(
    @SerializedName("id") val id: Int,
    @SerializedName("divisi_id") val divisiId: Int,
    @SerializedName("karyawan_id") val karyawanId: String,
    @SerializedName("karyawan") val karyawan: KaryawanDto?
)

data class CreateAnggotaDivisiRequest(
    @SerializedName("divisi_id") val divisiId: Int,
    @SerializedName("karyawan_id") val karyawanId: String
)

// ─── Presensi / Attandence DTOs ────────────────────────────────

data class PresensiListResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: List<PresensiDto>
)

data class PresensiSingleResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: PresensiDto?
)

data class PresensiDto(
    @SerializedName("id") val id: Int,
    @SerializedName("user_id") val userId: Int?,
    @SerializedName("tanggal") val tanggal: String?,
    @SerializedName("jam_checkin") val jamCheckin: String?,
    @SerializedName("jam_checkout") val jamCheckout: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("keterangan") val keterangan: String?,
    @SerializedName("lokasi_checkin") val lokasiCheckin: String?,
    @SerializedName("user") val user: KaryawanUserDto?
)

data class PresensiTodayResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: PresensiTodayData
)

data class PresensiTodayData(
    @SerializedName("sudah_checkin") val sudahCheckin: Boolean,
    @SerializedName("sudah_checkout") val sudahCheckout: Boolean,
    @SerializedName("presensi") val presensi: PresensiDto?
)

// ─── Rekap Kehadiran DTOs ──────────────────────────────────────

data class RekapKehadiranResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: List<RekapKehadiranDto>
)

data class RekapKehadiranDto(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("nama_lengkap") val namaLengkap: String?,
    @SerializedName("total_hadir") val totalHadir: Int,
    @SerializedName("total_izin") val totalIzin: Int,
    @SerializedName("total_sakit") val totalSakit: Int,
    @SerializedName("total_alpha") val totalAlpha: Int,
    @SerializedName("total_cuti") val totalCuti: Int
)

// ─── Pengajuan DTOs ────────────────────────────────────────────

data class PengajuanListResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: List<PengajuanDto>
)

data class PengajuanSingleResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: PengajuanDto?
)

data class PengajuanDto(
    @SerializedName("id") val id: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("jenis") val jenis: String,
    @SerializedName("tanggal_mulai") val tanggalMulai: String?,
    @SerializedName("tanggal_selesai") val tanggalSelesai: String?,
    @SerializedName("keterangan") val keterangan: String?,
    @SerializedName("status") val status: String,
    @SerializedName("alasan_penolakan") val alasanPenolakan: String?,
    @SerializedName("tanggal_disetujui") val tanggalDisetujui: String?,
    @SerializedName("disetujui_oleh") val disetujuiOleh: Int?,
    @SerializedName("user") val user: KaryawanUserDto?,
    @SerializedName("created_at") val createdAt: String?
)

data class CreatePengajuanRequest(
    @SerializedName("jenis") val jenis: String,
    @SerializedName("tanggal_mulai") val tanggalMulai: String,
    @SerializedName("tanggal_selesai") val tanggalSelesai: String,
    @SerializedName("keterangan") val keterangan: String? = null
)

data class RejectPengajuanRequest(
    @SerializedName("alasan_penolakan") val alasanPenolakan: String
)

// ─── Jadwal DTOs ───────────────────────────────────────────────

data class JadwalListResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: List<JadwalDto>
)

data class JadwalSingleResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: JadwalDto?
)

data class JadwalDto(
    @SerializedName("id") val id: Int,
    @SerializedName("nama_event") val namaEvent: String,
    @SerializedName("deskripsi") val deskripsi: String?,
    @SerializedName("tanggal") val tanggal: String?,
    @SerializedName("kategori") val kategori: String,
    @SerializedName("outlet_id") val outletId: Int?,
    @SerializedName("created_by") val createdBy: Int?,
    @SerializedName("outlet") val outlet: OutletDto?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?
)

data class CreateJadwalRequest(
    @SerializedName("nama_event") val namaEvent: String,
    @SerializedName("deskripsi") val deskripsi: String? = null,
    @SerializedName("tanggal") val tanggal: String,
    @SerializedName("kategori") val kategori: String,
    @SerializedName("outlet_id") val outletId: Int? = null
)

data class UpdateJadwalRequest(
    @SerializedName("nama_event") val namaEvent: String? = null,
    @SerializedName("deskripsi") val deskripsi: String? = null,
    @SerializedName("tanggal") val tanggal: String? = null,
    @SerializedName("kategori") val kategori: String? = null,
    @SerializedName("outlet_id") val outletId: Int? = null
)

// ─── Penugasan DTOs ────────────────────────────────────────────

data class PenugasanListResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: List<PenugasanDto>
)

data class PenugasanSingleResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: PenugasanDto?
)

data class PenugasanDto(
    @SerializedName("id") val id: Int,
    @SerializedName("judul") val judul: String,
    @SerializedName("deskripsi") val deskripsi: String?,
    @SerializedName("penanggung_jawab_id") val penanggungJawabId: String?,
    @SerializedName("divisi_id") val divisiId: Int?,
    @SerializedName("tenggat") val tenggat: String?,
    @SerializedName("status") val status: String,
    @SerializedName("created_by") val createdBy: Int?,
    @SerializedName("penanggung_jawab") val penanggungJawab: KaryawanDto?,
    @SerializedName("divisi") val divisi: DivisiDto?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?
)

data class CreatePenugasanRequest(
    @SerializedName("judul") val judul: String,
    @SerializedName("deskripsi") val deskripsi: String? = null,
    @SerializedName("penanggung_jawab_id") val penanggungJawabId: String,
    @SerializedName("divisi_id") val divisiId: Int? = null,
    @SerializedName("tenggat") val tenggat: String? = null,
    @SerializedName("status") val status: String? = null
)

data class UpdatePenugasanRequest(
    @SerializedName("judul") val judul: String? = null,
    @SerializedName("deskripsi") val deskripsi: String? = null,
    @SerializedName("penanggung_jawab_id") val penanggungJawabId: String? = null,
    @SerializedName("divisi_id") val divisiId: Int? = null,
    @SerializedName("tenggat") val tenggat: String? = null,
    @SerializedName("status") val status: String? = null
)

// ─── User Info DTO ─────────────────────────────────────────────

data class UserInfoResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: UserInfoData
)

data class UserInfoData(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String?,
    @SerializedName("email") val email: String,
    @SerializedName("role_id") val roleId: Int,
    @SerializedName("instansi_id") val instansiId: Int?,
    @SerializedName("outlet_id") val outletId: Int?,
    @SerializedName("role") val role: RoleDto?,
    @SerializedName("profil_karyawan") val profilKaryawan: KaryawanDto?,
    @SerializedName("outlet") val outlet: OutletDto?
)

// ─── Instansi DTOs ──────────────────────────────────────────────

data class InstansiResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: InstansiData?
)

data class InstansiData(
    @SerializedName("id") val id: Int,
    @SerializedName("nama_instansi") val namaInstansi: String,
    @SerializedName("alamat") val alamat: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?
)
