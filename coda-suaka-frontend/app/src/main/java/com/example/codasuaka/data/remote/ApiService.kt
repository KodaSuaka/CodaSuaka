package com.example.codasuaka.data.remote

import com.example.codasuaka.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ─── Auth ─────────────────────────────────────────────────

    @Headers("Accept: application/json")
    @POST("api/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @Headers("Accept: application/json")
    @POST("api/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    @Headers("Accept: application/json")
    @POST("api/logout")
    suspend fun logout(): Response<ApiStatusResponse>

    @Headers("Accept: application/json")
    @GET("api/user")
    suspend fun getUser(): Response<UserInfoResponse>

    // ─── Dashboard ────────────────────────────────────────────

    @Headers("Accept: application/json")
    @GET("api/dashboard")
    suspend fun getDashboard(): Response<DashboardResponse>

    @Headers("Accept: application/json")
    @GET("api/dashboard/omset")
    suspend fun getDashboardOmset(
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null
    ): Response<OmsetResponse>

    @Headers("Accept: application/json")
    @GET("api/karyawan/dashboard")
    suspend fun getKaryawanDashboard(): Response<KaryawanDashboardResponse>

    // ─── Instansi ─────────────────────────────────────────────

    @Headers("Accept: application/json")
    @GET("api/instansi")
    suspend fun getInstansi(): Response<Any>

    // ─── Outlet ───────────────────────────────────────────────

    @Headers("Accept: application/json")
    @GET("api/outlets")
    suspend fun getOutlets(
        @Query("instansi_id") instansiId: Int? = null
    ): Response<OutletListResponse>

    @Headers("Accept: application/json")
    @GET("api/outlets/{id}")
    suspend fun getOutlet(@Path("id") id: Int): Response<OutletSingleResponse>

    @Headers("Accept: application/json")
    @POST("api/outlets")
    suspend fun createOutlet(@Body request: OutletRequest): Response<OutletSingleResponse>

    @Headers("Accept: application/json")
    @PUT("api/outlets/{id}")
    suspend fun updateOutlet(
        @Path("id") id: Int,
        @Body request: OutletRequest
    ): Response<OutletSingleResponse>

    @Headers("Accept: application/json")
    @DELETE("api/outlets/{id}")
    suspend fun deleteOutlet(@Path("id") id: Int): Response<ApiStatusResponse>

    // ─── Role ─────────────────────────────────────────────────

    @Headers("Accept: application/json")
    @GET("api/roles")
    suspend fun getRoles(): Response<RoleListResponse>

    // ─── Karyawan ─────────────────────────────────────────────

    @Headers("Accept: application/json")
    @GET("api/karyawans")
    suspend fun getKaryawans(
        @Query("outlet_id") outletId: Int? = null
    ): Response<KaryawanListResponse>

    @Headers("Accept: application/json")
    @GET("api/karyawans/me")
    suspend fun getKaryawanMe(): Response<KaryawanSingleResponse>

    @Headers("Accept: application/json")
    @GET("api/karyawans/{id}")
    suspend fun getKaryawan(@Path("id") id: String): Response<KaryawanSingleResponse>

    @Headers("Accept: application/json")
    @POST("api/karyawans")
    suspend fun createKaryawan(@Body request: CreateKaryawanRequest): Response<KaryawanSingleResponse>

    @Headers("Accept: application/json")
    @PUT("api/karyawans/{id}")
    suspend fun updateKaryawan(
        @Path("id") id: String,
        @Body request: UpdateKaryawanRequest
    ): Response<KaryawanSingleResponse>

    @Headers("Accept: application/json")
    @DELETE("api/karyawans/{id}")
    suspend fun deleteKaryawan(@Path("id") id: String): Response<ApiStatusResponse>

    // ─── Divisi ───────────────────────────────────────────────

    @Headers("Accept: application/json")
    @GET("api/divisis")
    suspend fun getDivisis(
        @Query("outlet_id") outletId: Int? = null
    ): Response<DivisiListResponse>

    @Headers("Accept: application/json")
    @GET("api/divisis/{id}")
    suspend fun getDivisi(@Path("id") id: Int): Response<DivisiSingleResponse>

    @Headers("Accept: application/json")
    @POST("api/divisis")
    suspend fun createDivisi(@Body request: CreateDivisiRequest): Response<DivisiSingleResponse>

    @Headers("Accept: application/json")
    @PUT("api/divisis/{id}")
    suspend fun updateDivisi(
        @Path("id") id: Int,
        @Body request: UpdateDivisiRequest
    ): Response<DivisiSingleResponse>

    @Headers("Accept: application/json")
    @DELETE("api/divisis/{id}")
    suspend fun deleteDivisi(@Path("id") id: Int): Response<ApiStatusResponse>

    // ─── Anggota Divisi ───────────────────────────────────────

    @Headers("Accept: application/json")
    @GET("api/anggota-divisis")
    suspend fun getAnggotaDivisis(): Response<AnggotaDivisiListResponse>

    @Headers("Accept: application/json")
    @POST("api/anggota-divisis")
    suspend fun createAnggotaDivisi(@Body request: CreateAnggotaDivisiRequest): Response<ApiStatusResponse>

    @Headers("Accept: application/json")
    @DELETE("api/anggota-divisis/{id}")
    suspend fun deleteAnggotaDivisi(@Path("id") id: Int): Response<ApiStatusResponse>

    // ─── Presensi / Attandence ────────────────────────────────

    @Headers("Accept: application/json")
    @GET("api/presensis")
    suspend fun getPresensis(
        @Query("tanggal") tanggal: String? = null,
        @Query("bulan") bulan: Int? = null,
        @Query("tahun") tahun: Int? = null,
        @Query("user_id") userId: Int? = null
    ): Response<PresensiListResponse>

    @Headers("Accept: application/json")
    @POST("api/presensis/checkin")
    suspend fun checkin(@Body request: Map<String, String>? = null): Response<PresensiSingleResponse>

    @Headers("Accept: application/json")
    @POST("api/presensis/checkout")
    suspend fun checkout(): Response<PresensiSingleResponse>

    @Headers("Accept: application/json")
    @GET("api/presensis/today")
    suspend fun getPresensiToday(): Response<PresensiTodayResponse>

    @Headers("Accept: application/json")
    @GET("api/rekap-kehadiran")
    suspend fun getRekapKehadiran(
        @Query("bulan") bulan: Int? = null,
        @Query("tahun") tahun: Int? = null
    ): Response<RekapKehadiranResponse>

    // ─── Pengajuan ────────────────────────────────────────────

    @Headers("Accept: application/json")
    @GET("api/pengajuans")
    suspend fun getPengajuans(
        @Query("status") status: String? = null
    ): Response<PengajuanListResponse>

    @Headers("Accept: application/json")
    @POST("api/pengajuans")
    suspend fun createPengajuan(@Body request: CreatePengajuanRequest): Response<PengajuanSingleResponse>

    @Headers("Accept: application/json")
    @GET("api/pengajuans/{id}")
    suspend fun getPengajuan(@Path("id") id: Int): Response<PengajuanSingleResponse>

    @Headers("Accept: application/json")
    @PUT("api/pengajuans/{id}/approve")
    suspend fun approvePengajuan(@Path("id") id: Int): Response<PengajuanSingleResponse>

    @Headers("Accept: application/json")
    @PUT("api/pengajuans/{id}/reject")
    suspend fun rejectPengajuan(
        @Path("id") id: Int,
        @Body request: RejectPengajuanRequest
    ): Response<PengajuanSingleResponse>

    // ─── Jadwal / Kalender ────────────────────────────────────

    @Headers("Accept: application/json")
    @GET("api/jadwals")
    suspend fun getJadwals(
        @Query("outlet_id") outletId: Int? = null,
        @Query("bulan") bulan: Int? = null,
        @Query("tahun") tahun: Int? = null,
        @Query("tanggal") tanggal: String? = null
    ): Response<JadwalListResponse>

    @Headers("Accept: application/json")
    @GET("api/jadwals/{id}")
    suspend fun getJadwal(@Path("id") id: Int): Response<JadwalSingleResponse>

    @Headers("Accept: application/json")
    @POST("api/jadwals")
    suspend fun createJadwal(@Body request: CreateJadwalRequest): Response<JadwalSingleResponse>

    @Headers("Accept: application/json")
    @PUT("api/jadwals/{id}")
    suspend fun updateJadwal(
        @Path("id") id: Int,
        @Body request: UpdateJadwalRequest
    ): Response<JadwalSingleResponse>

    @Headers("Accept: application/json")
    @DELETE("api/jadwals/{id}")
    suspend fun deleteJadwal(@Path("id") id: Int): Response<ApiStatusResponse>

    // ─── Penugasan ────────────────────────────────────────────

    @Headers("Accept: application/json")
    @GET("api/penugasans")
    suspend fun getPenugasans(
        @Query("divisi_id") divisiId: Int? = null,
        @Query("status") status: String? = null,
        @Query("penanggung_jawab_id") penanggungJawabId: String? = null
    ): Response<PenugasanListResponse>

    @Headers("Accept: application/json")
    @GET("api/penugasans/{id}")
    suspend fun getPenugasan(@Path("id") id: Int): Response<PenugasanSingleResponse>

    @Headers("Accept: application/json")
    @POST("api/penugasans")
    suspend fun createPenugasan(@Body request: CreatePenugasanRequest): Response<PenugasanSingleResponse>

    @Headers("Accept: application/json")
    @PUT("api/penugasans/{id}")
    suspend fun updatePenugasan(
        @Path("id") id: Int,
        @Body request: UpdatePenugasanRequest
    ): Response<PenugasanSingleResponse>

    @Headers("Accept: application/json")
    @DELETE("api/penugasans/{id}")
    suspend fun deletePenugasan(@Path("id") id: Int): Response<ApiStatusResponse>

    // ─── Chat / Kontak ────────────────────────────────────────

    @Headers("Accept: application/json")
    @GET("api/chat/contacts")
    suspend fun getChatContacts(): Response<ChatContactResponse>

    @Headers("Accept: application/json")
    @GET("api/chat/messages/{userId}")
    suspend fun getChatMessages(
        @Path("userId") userId: Int
    ): Response<ChatMessageResponse>

    @Headers("Accept: application/json")
    @POST("api/chat/send")
    suspend fun sendChatMessage(
        @Body request: SendMessageRequest
    ): Response<SendMessageResponse>

    @Headers("Accept: application/json")
    @PUT("api/chat/read/{userId}")
    suspend fun markChatRead(
        @Path("userId") userId: Int
    ): Response<Unit>
}
