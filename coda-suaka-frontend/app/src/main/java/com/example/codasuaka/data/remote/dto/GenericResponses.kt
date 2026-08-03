package com.example.codasuaka.data.remote.dto

import com.example.codasuaka.di.NullableIntAdapter
import com.google.gson.annotations.JsonAdapter
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
    @SerializedName("total_omset") val totalOmset: Double,
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
    @SerializedName("instansi_id") val instansiId: String?,
    @SerializedName("is_active") val isActive: Boolean?,
    @SerializedName("karyawans_count") val karyawansCount: Int? = null,
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
    @SerializedName("tempat_lahir") val tempatLahir: String? = null,
    @SerializedName("tanggal_lahir") val tanggalLahir: String? = null,
    @SerializedName("foto_profil") val fotoProfil: String?,
    @SerializedName("outlet_id") val outletId: Int?,
    @SerializedName("sisa_cuti") val sisaCuti: Int?,
    @SerializedName("tanggal_mulai_kerja") val tanggalMulaiKerja: String?,
    @SerializedName("lama_bekerja") val lamaBekerja: String? = null,
    @SerializedName("user") val user: KaryawanUserDto?,
    @SerializedName("outlet") val outlet: OutletDto?
)

data class KaryawanUserDto(
    @SerializedName("id") val id: Int,
    @SerializedName("email") val email: String?,
    @SerializedName("role") val role: RoleDto?,
    @SerializedName("role_id") val roleId: Int?,
    @SerializedName("name") val name: String?,
    @SerializedName("outlet_id") val outletId: Int? = null
)

data class CreateKaryawanRequest(
    @SerializedName("nama_lengkap") val namaLengkap: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("kontak") val kontak: String? = null,
    @SerializedName("alamat") val alamat: String? = null,
    @SerializedName("tempat_lahir") val tempatLahir: String? = null,
    @SerializedName("tanggal_lahir") val tanggalLahir: String? = null,
    @SerializedName("role_id") val roleId: Int,
    @SerializedName("outlet_id") val outletId: Int? = null,
    @SerializedName("tanggal_mulai_kerja") val tanggalMulaiKerja: String? = null
)

data class UpdateKaryawanRequest(
    @SerializedName("nama_lengkap") val namaLengkap: String? = null,
    @SerializedName("kontak") val kontak: String? = null,
    @SerializedName("alamat") val alamat: String? = null,
    @SerializedName("tempat_lahir") val tempatLahir: String? = null,
    @SerializedName("tanggal_lahir") val tanggalLahir: String? = null,
    @SerializedName("outlet_id") val outletId: Int? = null,
    @SerializedName("sisa_cuti") val sisaCuti: Int? = null,
    @SerializedName("tanggal_mulai_kerja") val tanggalMulaiKerja: String? = null
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
    @SerializedName("status_keterangan") val statusKeterangan: String?,
    @SerializedName("user") val user: KaryawanUserDto?
)

data class PresensiTodayResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: PresensiTodayData
)

data class PresensiTodayData(
    @SerializedName("sudah_checkin") val sudahCheckin: Boolean,
    @SerializedName("sudah_checkout") val sudahCheckout: Boolean,
    @SerializedName("jam_checkin_standar") val jamCheckinStandar: String? = null,
    @SerializedName("jam_checkout_standar") val jamCheckoutStandar: String? = null,
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
    @SerializedName("outlet_id") val outletId: Int? = null,
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
    @JsonAdapter(NullableIntAdapter::class)
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
    @SerializedName("urgency") val urgency: String?,
    @SerializedName("poin") val poin: Int?,
    @SerializedName("is_template") val isTemplate: Boolean?,
    @SerializedName("instansi_id") val instansiId: String?,
    @JsonAdapter(NullableIntAdapter::class)
    @SerializedName("created_by") val createdBy: Int?,
    @SerializedName("accepted_at") val acceptedAt: String?,
    @SerializedName("completed_at") val completedAt: String?,
    @SerializedName("penanggung_jawab") val penanggungJawab: KaryawanDto?,
    @SerializedName("divisi") val divisi: DivisiDto?,
    @SerializedName("pembuat") val pembuat: UserInfoData?,
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
    @SerializedName("instansi_id") val instansiId: String?,
    @SerializedName("outlet_id") val outletId: Int?,
    @SerializedName("role") val role: RoleDto?,
    @SerializedName("profil_karyawan") val profilKaryawan: KaryawanDto?,
    @SerializedName("outlet") val outlet: OutletDto?
)

// ─── Instansi DTOs ──────────────────────────────────────────────

data class InstansiResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: InstansiData?
)

data class InstansiData(
    @SerializedName("id") val id: String,
    @SerializedName("nama_instansi") val namaInstansi: String,
    @SerializedName("alamat") val alamat: String?,
    @SerializedName("timezone") val timezone: String? = null,
    @SerializedName("jam_operasional") val jamOperasional: JamOperasionalData?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?
)

data class JamOperasionalData(
    @SerializedName("jam_buka") val jamBuka: String?,
    @SerializedName("jam_tutup") val jamTutup: String?,
    @SerializedName("hari_operasional") val hariOperasional: List<Int>?
)

data class UpdateInstansiRequest(
    @SerializedName("nama_instansi") val namaInstansi: String? = null,
    @SerializedName("jam_operasional") val jamOperasional: Map<String, Any?>? = null
)

// ─── Keuangan: Kategori Transaksi DTOs ──────────────────────────

data class KategoriTransaksiListResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: List<KategoriTransaksiDto>
)

data class KategoriTransaksiSingleResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: KategoriTransaksiDto?
)

data class KategoriTransaksiDto(
    @SerializedName("id") val id: Int,
    @SerializedName("instansi_id") val instansiId: String?, // nullable karena global template punya null
    @SerializedName("nama_kategori") val namaKategori: String,
    @SerializedName("tipe") val tipe: String, // masuk / keluar
    @SerializedName("sifat") val sifat: String, // operasional / non_operasional
    @SerializedName("termasuk_hpp") val termasukHpp: Boolean,
    @SerializedName("is_default") val isDefault: Boolean,
    @SerializedName("is_active") val isActive: Boolean,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?
)

data class CreateKategoriTransaksiRequest(
    @SerializedName("nama_kategori") val namaKategori: String,
    @SerializedName("tipe") val tipe: String,
    @SerializedName("sifat") val sifat: String,
    @SerializedName("termasuk_hpp") val termasukHpp: Boolean? = null
)

// ─── Keuangan: Transaksi Kas DTOs ──────────────────────────────

data class TransaksiKasListResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: List<TransaksiKasDto>,
    @SerializedName("meta") val meta: PaginationMeta?
)

data class TransaksiKasSingleResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: TransaksiKasDto?
)

data class TransaksiKasDto(
    @SerializedName("id") val id: Int,
    @SerializedName("instansi_id") val instansiId: String,
    @SerializedName("outlet_id") val outletId: Int?,
    @SerializedName("kategori_transaksi_id") val kategoriTransaksiId: Int?,
    @SerializedName("tanggal") val tanggal: String,
    @SerializedName("tipe") val tipe: String, // masuk / keluar
    @SerializedName("nominal") val nominal: Double,
    @SerializedName("metode_pembayaran") val metodePembayaran: String?,
    @SerializedName("keterangan") val keterangan: String?,
    @SerializedName("lampiran_url") val lampiranUrl: String?,
    @SerializedName("dokumen_transaksi_id") val dokumenTransaksiId: Int?,
    @JsonAdapter(NullableIntAdapter::class)
    @SerializedName("created_by") val createdBy: Int?,
    @SerializedName("created_by_user") val createdByUser: UserData?,
    @SerializedName("kategori_transaksi") val kategoriTransaksi: KategoriTransaksiDto?,
    @SerializedName("outlet") val outlet: OutletDto?,
    @SerializedName("status_approval") val statusApproval: String?, // disetujui / pending / ditolak
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?
)

data class CreateTransaksiKasRequest(
    @SerializedName("tanggal") val tanggal: String,
    @SerializedName("tipe") val tipe: String,
    @SerializedName("nominal") val nominal: Double,
    @SerializedName("kategori_transaksi_id") val kategoriTransaksiId: Int? = null,
    @SerializedName("outlet_id") val outletId: Int? = null,
    @SerializedName("metode_pembayaran") val metodePembayaran: String? = null,
    @SerializedName("keterangan") val keterangan: String? = null
)

data class UpdateTransaksiKasRequest(
    @SerializedName("tanggal") val tanggal: String? = null,
    @SerializedName("tipe") val tipe: String? = null,
    @SerializedName("nominal") val nominal: Double? = null,
    @SerializedName("kategori_transaksi_id") val kategoriTransaksiId: Int? = null,
    @SerializedName("outlet_id") val outletId: Int? = null,
    @SerializedName("metode_pembayaran") val metodePembayaran: String? = null,
    @SerializedName("keterangan") val keterangan: String? = null
)

data class SaldoResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: SaldoData
)

data class SaldoData(
    @SerializedName("total_masuk") val totalMasuk: Double,
    @SerializedName("total_keluar") val totalKeluar: Double,
    @SerializedName("saldo_akhir") val saldoAkhir: Double,
    @SerializedName("start_date") val startDate: String?,
    @SerializedName("end_date") val endDate: String?
)

data class LabaRugiResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: LabaRugiData
)

data class LabaRugiData(
    @SerializedName("pendapatan") val pendapatan: Double,
    @SerializedName("hpp") val hpp: Double,
    @SerializedName("beban_operasional") val bebanOperasional: Double,
    @SerializedName("laba_rugi") val labaRugi: Double,
    @SerializedName("start_date") val startDate: String?,
    @SerializedName("end_date") val endDate: String?,
    @SerializedName("pendapatan_per_kategori") val pendapatanPerKategori: Map<String, Double>? = null,
    @SerializedName("hpp_per_kategori") val hppPerKategori: Map<String, Double>? = null,
    @SerializedName("beban_per_kategori") val bebanPerKategori: Map<String, Double>? = null
)

data class ArusKasResponse(
    @SerializedName("status") val status: String?,
    @SerializedName("data") val data: ArusKasData?
)

data class ArusKasData(
    @SerializedName("arus_kas_operasi") val arusKasOperasi: Double,
    @SerializedName("arus_kas_investasi") val arusKasInvestasi: Double,
    @SerializedName("arus_kas_pendanaan") val arusKasPendanaan: Double,
    @SerializedName("kenaikan_bersih_kas") val kenaikanBersihKas: Double,
    @SerializedName("saldo_awal") val saldoAwal: Double,
    @SerializedName("saldo_akhir") val saldoAkhir: Double,
    @SerializedName("start_date") val startDate: String?,
    @SerializedName("end_date") val endDate: String?,
    @SerializedName("detail_operasi") val detailOperasi: List<ArusKasDetail>?,
    @SerializedName("detail_pendanaan") val detailPendanaan: List<ArusKasDetail>?
)

data class ArusKasDetail(
    @SerializedName("kategori") val kategori: String,
    @SerializedName("masuk") val masuk: Double,
    @SerializedName("keluar") val keluar: Double
)

data class RingkasanKeuanganResponse(
    @SerializedName("status") val status: String?,
    @SerializedName("data") val data: RingkasanKeuanganData?
)

data class RingkasanKeuanganData(
    @SerializedName("tahun") val tahun: Int?,
    @SerializedName("series") val series: List<RingkasanBulanan>?
)

data class RingkasanBulanan(
    @SerializedName("bulan") val bulan: String,
    @SerializedName("pendapatan") val pendapatan: Double,
    @SerializedName("beban") val beban: Double,
    @SerializedName("laba") val laba: Double
)

data class PaginationMeta(
    @SerializedName("current_page") val currentPage: Int,
    @SerializedName("last_page") val lastPage: Int,
    @SerializedName("per_page") val perPage: Int,
    @SerializedName("total") val total: Int
)

// ─── Approval Transaksi ───────────────────────────────────────────

data class ApprovalListResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: List<ApprovalLogDto>?,
    @SerializedName("meta") val meta: PaginationMeta?
)

data class ApprovalSingleResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: ApprovalLogDto?
)

data class ApprovalLogDto(
    @SerializedName("id") val id: Int,
    @SerializedName("transaksi_kas_id") val transaksiKasId: Int,
    @SerializedName("diajukan_oleh") val diajukanOleh: Int,
    @SerializedName("disetujui_oleh") val disetujuiOleh: Int?,
    @SerializedName("status") val status: String,
    @SerializedName("catatan") val catatan: String?,
    @SerializedName("tanggal_diajukan") val tanggalDiajukan: String,
    @SerializedName("tanggal_diproses") val tanggalDiproses: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?,
    @SerializedName("transaksi_kas") val transaksiKas: ApprovalTransaksiKasDto?,
    @SerializedName("pengaju") val pengaju: ApprovalUserDto?,
    @SerializedName("pemeriksa") val pemeriksa: ApprovalUserDto?
)

data class ApprovalTransaksiKasDto(
    @SerializedName("id") val id: Int,
    @SerializedName("tanggal") val tanggal: String,
    @SerializedName("tipe") val tipe: String,
    @SerializedName("nominal") val nominal: Double,
    @SerializedName("metode_pembayaran") val metodePembayaran: String?,
    @SerializedName("keterangan") val keterangan: String?,
    @SerializedName("status_approval") val statusApproval: String?,
    @SerializedName("kategori_transaksi") val kategoriTransaksi: KategoriTransaksiDto?,
    @SerializedName("outlet") val outlet: OutletDto?,
    @SerializedName("created_by_user") val createdByUser: ApprovalUserDto?
)

data class ApprovalUserDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("nama_role") val namaRole: String?,
    @SerializedName("nama_outlet") val namaOutlet: String?
)

// ─── Poin Kinerja ───────────────────────────────────────────

data class PoinKinerjaResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: PoinKinerjaData
)

data class PoinKinerjaData(
    @SerializedName("total_poin") val totalPoin: Int,
    @SerializedName("total_tugas_selesai") val totalTugasSelesai: Int,
    @SerializedName("rata_rata_poin") val rataRataPoin: Double,
    @SerializedName("detail_urgency") val detailUrgency: List<DetailUrgency>
)

data class DetailUrgency(
    @SerializedName("urgency") val urgency: String,
    @SerializedName("jumlah") val jumlah: Int,
    @SerializedName("total_poin") val totalPoin: Int
)

// ─── Template Penugasan (sekarang dari tabel penugasan) ────────

data class TemplatePenugasanListResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: TemplatePenugasanListData
)

data class TemplatePenugasanListData(
    @SerializedName("templates") val templates: List<TemplatePenugasanDto>,
    @SerializedName("max_template") val maxTemplate: Int,
    @SerializedName("sisa_slot") val sisaSlot: Int
)

data class TemplatePenugasanSingleResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: TemplatePenugasanDto
)

data class TemplatePenugasanDto(
    @SerializedName("id") val id: Int,
    @SerializedName("judul") val judul: String,
    @SerializedName("deskripsi") val deskripsi: String?,
    @SerializedName("urgency") val urgency: String?,
    @SerializedName("poin") val poin: Int?,
    @SerializedName("is_template") val isTemplate: Boolean?,
    @SerializedName("instansi_id") val instansiId: String?,
    @JsonAdapter(NullableIntAdapter::class)
    @SerializedName("created_by") val createdBy: Int?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?
)

data class CreateTemplatePenugasanRequest(
    @SerializedName("nama_template") val namaTemplate: String,
    @SerializedName("deskripsi_template") val deskripsiTemplate: String?,
    @SerializedName("urgency_default") val urgencyDefault: String?
)

data class UpdateTemplatePenugasanRequest(
    @SerializedName("nama_template") val namaTemplate: String?,
    @SerializedName("deskripsi_template") val deskripsiTemplate: String?,
    @SerializedName("urgency_default") val urgencyDefault: String?
)

// ─── Notifikasi ──────────────────────────────────────────────────────────────

data class NotificationListResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: List<NotificationDto>,
    @SerializedName("meta") val meta: PaginationMeta?
)

data class UnreadCountResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: UnreadCountData
)

data class UnreadCountData(
    @SerializedName("unread_count") val unreadCount: Int
)

data class NotificationDto(
    @SerializedName("id") val id: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("type") val type: String,
    @SerializedName("title") val title: String,
    @SerializedName("body") val body: String,
    @SerializedName("icon") val icon: String?,
    @SerializedName("color") val color: String?,
    @SerializedName("is_read") val isRead: Boolean,
    @SerializedName("related_id") val relatedId: Int?,
    @SerializedName("related_type") val relatedType: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?
)

// ─── Kasir: Barang/Jasa ─────────────────────────────────────────

data class BarangJasaDto(
    @SerializedName("id") val id: Int,
    @SerializedName("instansi_id") val instansiId: String,
    @SerializedName("nama") val nama: String,
    @SerializedName("jenis") val jenis: String, // "barang" / "jasa"
    @SerializedName("kategori") val kategori: String?,
    @SerializedName("satuan") val satuan: String,
    @SerializedName("harga_jual") val hargaJual: Double,
    @SerializedName("harga_beli") val hargaBeli: Double?,
    @SerializedName("stok") val stok: Int?,
    @SerializedName("is_active") val isActive: Boolean,
    @SerializedName("keterangan") val keterangan: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?
)

data class BarangJasaListResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: List<BarangJasaDto>,
    @SerializedName("meta") val meta: PaginationMeta?
)

data class BarangJasaSingleResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: BarangJasaDto?
)

data class BarangJasaRequest(
    @SerializedName("nama") val nama: String,
    @SerializedName("jenis") val jenis: String,
    @SerializedName("kategori") val kategori: String? = null,
    @SerializedName("satuan") val satuan: String,
    @SerializedName("harga_jual") val hargaJual: Double,
    @SerializedName("harga_beli") val hargaBeli: Double? = null,
    @SerializedName("stok") val stok: Int? = null,
    @SerializedName("is_active") val isActive: Boolean? = null,
    @SerializedName("keterangan") val keterangan: String? = null
)

// ─── Kasir: Nota ─────────────────────────────────────────────────

data class NotaItemDto(
    @SerializedName("id") val id: Int,
    @SerializedName("nota_id") val notaId: Int,
    @SerializedName("barang_jasa_id") val barangJasaId: Int?,
    @SerializedName("nama_item") val namaItem: String,
    @SerializedName("jenis") val jenis: String,
    @SerializedName("kuantitas") val kuantitas: Double,
    @SerializedName("satuan") val satuan: String,
    @SerializedName("harga_satuan") val hargaSatuan: Double,
    @SerializedName("subtotal") val subtotal: Double,
    @SerializedName("barang_jasa") val barangJasa: BarangJasaDto?
)

data class NotaDto(
    @SerializedName("id") val id: Int,
    @SerializedName("instansi_id") val instansiId: String,
    @SerializedName("outlet_id") val outletId: Int?,
    @SerializedName("kategori_transaksi_id") val kategoriTransaksiId: Int?,
    @SerializedName("transaksi_kas_id") val transaksiKasId: Int?,
    @SerializedName("tipe") val tipe: String, // "penjualan" / "pembelian"
    @SerializedName("nomor_nota") val nomorNota: String,
    @SerializedName("tanggal") val tanggal: String,
    @SerializedName("pihak_terkait") val pihakTerkait: String?,
    @SerializedName("metode_pembayaran") val metodePembayaran: String?,
    @SerializedName("total") val total: Double,
    @SerializedName("status") val status: String,
    @SerializedName("lampiran_url") val lampiranUrl: String?,
    @SerializedName("catatan") val catatan: String?,
    @SerializedName("items") val items: List<NotaItemDto>?,
    @SerializedName("created_at") val createdAt: String?
)

data class NotaListResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: List<NotaDto>,
    @SerializedName("meta") val meta: PaginationMeta?
)

data class NotaSingleResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: NotaDto?
)

data class NotaItemRequest(
    @SerializedName("barang_jasa_id") val barangJasaId: Int? = null,
    @SerializedName("nama_item") val namaItem: String? = null,
    @SerializedName("jenis") val jenis: String? = null,
    @SerializedName("kuantitas") val kuantitas: Double,
    @SerializedName("satuan") val satuan: String? = null,
    @SerializedName("harga_satuan") val hargaSatuan: Double
)

data class CreateNotaRequest(
    @SerializedName("tipe") val tipe: String,
    @SerializedName("tanggal") val tanggal: String,
    @SerializedName("outlet_id") val outletId: Int? = null,
    @SerializedName("kategori_transaksi_id") val kategoriTransaksiId: Int? = null,
    @SerializedName("pihak_terkait") val pihakTerkait: String? = null,
    @SerializedName("metode_pembayaran") val metodePembayaran: String? = null,
    @SerializedName("catatan") val catatan: String? = null,
    @SerializedName("items") val items: List<NotaItemRequest>
)
