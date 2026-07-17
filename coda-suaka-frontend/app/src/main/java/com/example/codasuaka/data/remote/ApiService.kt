package com.example.codasuaka.data.remote

import com.example.codasuaka.data.remote.dto.*
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ─── Auth ─────────────────────────────────────────────────

    @POST("api/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    @POST("api/logout")
    suspend fun logout(): Response<ApiStatusResponse>

    @GET("api/user")
    suspend fun getUser(): Response<UserInfoResponse>

    // ─── Dashboard ────────────────────────────────────────────

    @GET("api/dashboard")
    suspend fun getDashboard(): Response<DashboardResponse>

    @GET("api/dashboard/omset")
    suspend fun getDashboardOmset(
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null
    ): Response<OmsetResponse>

    @GET("api/karyawan/dashboard")
    suspend fun getKaryawanDashboard(): Response<KaryawanDashboardResponse>

    // ─── Instansi ─────────────────────────────────────────────

    @GET("api/instansi")
    suspend fun getInstansi(): Response<InstansiResponse>

    // ─── Outlet ───────────────────────────────────────────────

    @GET("api/outlets")
    suspend fun getOutlets(
        @Query("instansi_id") instansiId: Int? = null
    ): Response<OutletListResponse>

    @GET("api/outlets/{id}")
    suspend fun getOutlet(@Path("id") id: Int): Response<OutletSingleResponse>

    @POST("api/outlets")
    suspend fun createOutlet(@Body request: OutletRequest): Response<OutletSingleResponse>

    @PUT("api/outlets/{id}")
    suspend fun updateOutlet(
        @Path("id") id: Int,
        @Body request: OutletRequest
    ): Response<OutletSingleResponse>

    @DELETE("api/outlets/{id}")
    suspend fun deleteOutlet(@Path("id") id: Int): Response<ApiStatusResponse>

    // ─── Role ─────────────────────────────────────────────────

    @GET("api/roles")
    suspend fun getRoles(): Response<RoleListResponse>

    // ─── Karyawan ─────────────────────────────────────────────

    @GET("api/karyawans")
    suspend fun getKaryawans(
        @Query("outlet_id") outletId: Int? = null
    ): Response<KaryawanListResponse>

    @GET("api/karyawans/me")
    suspend fun getKaryawanMe(): Response<KaryawanSingleResponse>

    @GET("api/karyawans/{id}")
    suspend fun getKaryawan(@Path("id") id: String): Response<KaryawanSingleResponse>

    @POST("api/karyawans")
    suspend fun createKaryawan(@Body request: CreateKaryawanRequest): Response<KaryawanSingleResponse>

    @PUT("api/karyawans/{id}")
    suspend fun updateKaryawan(
        @Path("id") id: String,
        @Body request: UpdateKaryawanRequest
    ): Response<KaryawanSingleResponse>

    @DELETE("api/karyawans/{id}")
    suspend fun deleteKaryawan(@Path("id") id: String): Response<ApiStatusResponse>

    // ─── Divisi ───────────────────────────────────────────────

    @GET("api/divisis")
    suspend fun getDivisis(
        @Query("outlet_id") outletId: Int? = null
    ): Response<DivisiListResponse>

    @GET("api/divisis/{id}")
    suspend fun getDivisi(@Path("id") id: Int): Response<DivisiSingleResponse>

    @POST("api/divisis")
    suspend fun createDivisi(@Body request: CreateDivisiRequest): Response<DivisiSingleResponse>

    @PUT("api/divisis/{id}")
    suspend fun updateDivisi(
        @Path("id") id: Int,
        @Body request: UpdateDivisiRequest
    ): Response<DivisiSingleResponse>

    @DELETE("api/divisis/{id}")
    suspend fun deleteDivisi(@Path("id") id: Int): Response<ApiStatusResponse>

    // ─── Anggota Divisi ───────────────────────────────────────

    @GET("api/anggota-divisis")
    suspend fun getAnggotaDivisis(): Response<AnggotaDivisiListResponse>

    @POST("api/anggota-divisis")
    suspend fun createAnggotaDivisi(@Body request: CreateAnggotaDivisiRequest): Response<ApiStatusResponse>

    @DELETE("api/anggota-divisis/{id}")
    suspend fun deleteAnggotaDivisi(@Path("id") id: Int): Response<ApiStatusResponse>

    // ─── Presensi / Attandence ────────────────────────────────

    @GET("api/presensis")
    suspend fun getPresensis(
        @Query("tanggal") tanggal: String? = null,
        @Query("bulan") bulan: Int? = null,
        @Query("tahun") tahun: Int? = null,
        @Query("user_id") userId: Int? = null
    ): Response<PresensiListResponse>

    @POST("api/presensis/checkin")
    suspend fun checkin(@Body request: Map<String, String>? = null): Response<PresensiSingleResponse>

    @POST("api/presensis/checkout")
    suspend fun checkout(): Response<PresensiSingleResponse>

    @GET("api/presensis/today")
    suspend fun getPresensiToday(): Response<PresensiTodayResponse>

    @GET("api/rekap-kehadiran")
    suspend fun getRekapKehadiran(
        @Query("bulan") bulan: Int? = null,
        @Query("tahun") tahun: Int? = null
    ): Response<RekapKehadiranResponse>

    // ─── Pengajuan ────────────────────────────────────────────

    @GET("api/pengajuans")
    suspend fun getPengajuans(
        @Query("status") status: String? = null
    ): Response<PengajuanListResponse>

    @POST("api/pengajuans")
    suspend fun createPengajuan(@Body request: CreatePengajuanRequest): Response<PengajuanSingleResponse>

    @GET("api/pengajuans/{id}")
    suspend fun getPengajuan(@Path("id") id: Int): Response<PengajuanSingleResponse>

    @PUT("api/pengajuans/{id}/approve")
    suspend fun approvePengajuan(@Path("id") id: Int): Response<PengajuanSingleResponse>

    @PUT("api/pengajuans/{id}/reject")
    suspend fun rejectPengajuan(
        @Path("id") id: Int,
        @Body request: RejectPengajuanRequest
    ): Response<PengajuanSingleResponse>

    // ─── Jadwal / Kalender ────────────────────────────────────

    @GET("api/jadwals")
    suspend fun getJadwals(
        @Query("outlet_id") outletId: Int? = null,
        @Query("bulan") bulan: Int? = null,
        @Query("tahun") tahun: Int? = null,
        @Query("tanggal") tanggal: String? = null
    ): Response<JadwalListResponse>

    @GET("api/jadwals/{id}")
    suspend fun getJadwal(@Path("id") id: Int): Response<JadwalSingleResponse>

    @POST("api/jadwals")
    suspend fun createJadwal(@Body request: CreateJadwalRequest): Response<JadwalSingleResponse>

    @PUT("api/jadwals/{id}")
    suspend fun updateJadwal(
        @Path("id") id: Int,
        @Body request: UpdateJadwalRequest
    ): Response<JadwalSingleResponse>

    @DELETE("api/jadwals/{id}")
    suspend fun deleteJadwal(@Path("id") id: Int): Response<ApiStatusResponse>

    // ─── Penugasan ────────────────────────────────────────────

    @GET("api/penugasans")
    suspend fun getPenugasans(
        @Query("divisi_id") divisiId: Int? = null,
        @Query("status") status: String? = null,
        @Query("penanggung_jawab_id") penanggungJawabId: String? = null
    ): Response<PenugasanListResponse>

    @GET("api/penugasans/{id}")
    suspend fun getPenugasan(@Path("id") id: Int): Response<PenugasanSingleResponse>

    @POST("api/penugasans")
    suspend fun createPenugasan(@Body request: CreatePenugasanRequest): Response<PenugasanSingleResponse>

    @PUT("api/penugasans/{id}")
    suspend fun updatePenugasan(
        @Path("id") id: Int,
        @Body request: UpdatePenugasanRequest
    ): Response<PenugasanSingleResponse>

    @DELETE("api/penugasans/{id}")
    suspend fun deletePenugasan(@Path("id") id: Int): Response<ApiStatusResponse>

    // ─── Chat / Kontak ────────────────────────────────────────

    @GET("api/chat/contacts")
    suspend fun getChatContacts(): Response<ChatContactResponse>

    @GET("api/chat/messages/{userId}")
    suspend fun getChatMessages(
        @Path("userId") userId: Int
    ): Response<ChatMessageResponse>

    @POST("api/chat/send")
    suspend fun sendChatMessage(
        @Body request: SendMessageRequest
    ): Response<SendMessageResponse>

    @PUT("api/chat/read/{userId}")
    suspend fun markChatRead(
        @Path("userId") userId: Int
    ): Response<ApiStatusResponse>

    // ─── Keuangan: Kategori Transaksi ──────────────────────────

    @GET("api/kategori-transaksis")
    suspend fun getKategoriTransaksis(
        @Query("tipe") tipe: String? = null,
        @Query("active_only") activeOnly: Boolean? = null
    ): Response<KategoriTransaksiListResponse>

    @GET("api/kategori-transaksis/{id}")
    suspend fun getKategoriTransaksi(
        @Path("id") id: Int
    ): Response<KategoriTransaksiSingleResponse>

    @POST("api/kategori-transaksis")
    suspend fun createKategoriTransaksi(
        @Body request: CreateKategoriTransaksiRequest
    ): Response<KategoriTransaksiSingleResponse>

    @PUT("api/kategori-transaksis/{id}")
    suspend fun updateKategoriTransaksi(
        @Path("id") id: Int,
        @Body request: Map<String, @JvmSuppressWildcards Any>
    ): Response<KategoriTransaksiSingleResponse>

    @DELETE("api/kategori-transaksis/{id}")
    suspend fun deleteKategoriTransaksi(
        @Path("id") id: Int
    ): Response<ApiStatusResponse>

    // ─── Keuangan: Transaksi Kas ──────────────────────────────

    @GET("api/transaksi-kas")
    suspend fun getTransaksiKasList(
        @Query("page") page: Int? = null,
        @Query("outlet_id") outletId: Int? = null,
        @Query("tipe") tipe: String? = null,
        @Query("kategori_transaksi_id") kategoriTransaksiId: Int? = null,
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null,
        @Query("per_page") perPage: Int? = null
    ): Response<TransaksiKasListResponse>

    @GET("api/transaksi-kas/saldo")
    suspend fun getSaldo(
        @Query("outlet_id") outletId: Int? = null,
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null
    ): Response<SaldoResponse>

    @GET("api/transaksi-kas/laporan/laba-rugi")
    suspend fun getLabaRugi(
        @Query("outlet_id") outletId: Int? = null,
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null
    ): Response<LabaRugiResponse>

    @GET("api/transaksi-kas/{id}")
    suspend fun getTransaksiKas(
        @Path("id") id: Int
    ): Response<TransaksiKasSingleResponse>

    @POST("api/transaksi-kas")
    suspend fun createTransaksiKas(
        @Body request: CreateTransaksiKasRequest
    ): Response<TransaksiKasSingleResponse>

    @PUT("api/transaksi-kas/{id}")
    suspend fun updateTransaksiKas(
        @Path("id") id: Int,
        @Body request: UpdateTransaksiKasRequest
    ): Response<TransaksiKasSingleResponse>

    @DELETE("api/transaksi-kas/{id}")
    suspend fun deleteTransaksiKas(
        @Path("id") id: Int
    ): Response<ApiStatusResponse>

    // ─── Laporan Keuangan ─────────────────────────────────────────

    @GET("api/laporan/arus-kas")
    suspend fun getArusKas(
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null,
        @Query("outlet_id") outletId: Int? = null
    ): Response<ArusKasResponse>

    @GET("api/laporan/ringkasan-keuangan")
    suspend fun getRingkasanKeuangan(
        @Query("tahun") tahun: Int? = null
    ): Response<RingkasanKeuanganResponse>

    // ─── Ekspor PDF/Excel ─────────────────────────────────────────

    @GET("api/laporan/buku-kas/export/pdf")
    @Streaming
    suspend fun exportBukuKasPdf(
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null,
        @Query("outlet_id") outletId: Int? = null
    ): Response<ResponseBody>

    @GET("api/laporan/buku-kas/export/excel")
    @Streaming
    suspend fun exportBukuKasExcel(
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null,
        @Query("outlet_id") outletId: Int? = null
    ): Response<ResponseBody>

    @GET("api/laporan/laba-rugi/export/pdf")
    @Streaming
    suspend fun exportLabaRugiPdf(
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null,
        @Query("outlet_id") outletId: Int? = null
    ): Response<ResponseBody>

    @GET("api/laporan/arus-kas/export/pdf")
    @Streaming
    suspend fun exportArusKasPdf(
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null,
        @Query("outlet_id") outletId: Int? = null
    ): Response<ResponseBody>

    @GET("api/laporan/arus-kas/export/excel")
    @Streaming
    suspend fun exportArusKasExcel(
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null,
        @Query("outlet_id") outletId: Int? = null
    ): Response<ResponseBody>

    // ─── Approval Transaksi ─────────────────────────────────────
    @GET("api/approval/pending")
    suspend fun getApprovalPending(
        @Query("outlet_id") outletId: Int? = null,
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null,
        @Query("per_page") perPage: Int? = null
    ): Response<ApprovalListResponse>

    @GET("api/approval/riwayat")
    suspend fun getApprovalRiwayat(
        @Query("status") status: String? = null,
        @Query("outlet_id") outletId: Int? = null,
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null,
        @Query("per_page") perPage: Int? = null
    ): Response<ApprovalListResponse>

    @POST("api/approval/{transaksiKasId}/ajukan")
    suspend fun ajukanApproval(
        @Path("transaksiKasId") transaksiKasId: Int
    ): Response<ApprovalSingleResponse>

    @POST("api/approval/{approvalLogId}/setujui")
    suspend fun setujuiApproval(
        @Path("approvalLogId") approvalLogId: Int,
        @Body catatan: Map<String, String>? = null
    ): Response<ApprovalSingleResponse>

    @POST("api/approval/{approvalLogId}/tolak")
    suspend fun tolakApproval(
        @Path("approvalLogId") approvalLogId: Int,
        @Body catatan: Map<String, String>
    ): Response<ApprovalSingleResponse>
}
