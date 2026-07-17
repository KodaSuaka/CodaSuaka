package com.example.codasuaka.domain.repository

import com.example.codasuaka.data.remote.dto.*

interface KeuanganRepository {

    // ─── Kategori Transaksi ───────────────────────────────────

    suspend fun getKategoriTransaksis(
        tipe: String? = null,
        activeOnly: Boolean? = null
    ): Result<List<KategoriTransaksiDto>>

    suspend fun getKategoriTransaksi(id: Int): Result<KategoriTransaksiDto>

    suspend fun createKategoriTransaksi(
        request: CreateKategoriTransaksiRequest
    ): Result<KategoriTransaksiDto>

    suspend fun updateKategoriTransaksi(
        id: Int,
        request: Map<String, Any>
    ): Result<KategoriTransaksiDto>

    suspend fun deleteKategoriTransaksi(id: Int): Result<Unit>

    // ─── Transaksi Kas ────────────────────────────────────────

    suspend fun getTransaksiKasList(
        page: Int? = null,
        outletId: Int? = null,
        tipe: String? = null,
        kategoriTransaksiId: Int? = null,
        startDate: String? = null,
        endDate: String? = null,
        perPage: Int? = null
    ): Result<Pair<List<TransaksiKasDto>, PaginationMeta?>>

    suspend fun getTransaksiKas(id: Int): Result<TransaksiKasDto>

    suspend fun createTransaksiKas(
        request: CreateTransaksiKasRequest
    ): Result<TransaksiKasDto>

    suspend fun updateTransaksiKas(
        id: Int,
        request: UpdateTransaksiKasRequest
    ): Result<TransaksiKasDto>

    suspend fun deleteTransaksiKas(id: Int): Result<Unit>

    // ─── Saldo & Laba Rugi ────────────────────────────────────

    suspend fun getSaldo(
        outletId: Int? = null,
        startDate: String? = null,
        endDate: String? = null
    ): Result<SaldoData>

    suspend fun getLabaRugi(
        outletId: Int? = null,
        startDate: String? = null,
        endDate: String? = null
    ): Result<LabaRugiData>

    // ─── Arus Kas ─────────────────────────────────────────────

    suspend fun getArusKas(
        startDate: String? = null,
        endDate: String? = null,
        outletId: Int? = null
    ): Result<ArusKasData>

    // ─── Ringkasan Keuangan ───────────────────────────────────

    suspend fun getRingkasanKeuangan(
        tahun: Int? = null
    ): Result<RingkasanKeuanganData>

    // ─── Ekspor Buku Kas ──────────────────────────────────────

    suspend fun exportBukuKasPdf(
        startDate: String? = null,
        endDate: String? = null,
        outletId: Int? = null
    ): Result<okhttp3.ResponseBody>

    suspend fun exportBukuKasExcel(
        startDate: String? = null,
        endDate: String? = null,
        outletId: Int? = null
    ): Result<okhttp3.ResponseBody>

    // ─── Ekspor Laba Rugi ─────────────────────────────────────

    suspend fun exportLabaRugiPdf(
        startDate: String? = null,
        endDate: String? = null,
        outletId: Int? = null
    ): Result<okhttp3.ResponseBody>

    // ─── Ekspor Arus Kas ──────────────────────────────────────

    suspend fun exportArusKasPdf(
        startDate: String? = null,
        endDate: String? = null,
        outletId: Int? = null
    ): Result<okhttp3.ResponseBody>

    suspend fun exportArusKasExcel(
        startDate: String? = null,
        endDate: String? = null,
        outletId: Int? = null
    ): Result<okhttp3.ResponseBody>

    // ─── Approval Transaksi ──────────────────────────────────────

    suspend fun getApprovalPending(
        outletId: Int? = null,
        startDate: String? = null,
        endDate: String? = null
    ): Result<List<ApprovalLogDto>>

    suspend fun getApprovalRiwayat(
        status: String? = null,
        outletId: Int? = null,
        startDate: String? = null,
        endDate: String? = null
    ): Result<List<ApprovalLogDto>>

    suspend fun ajukanApproval(transaksiKasId: Int): Result<ApprovalLogDto>

    suspend fun setujuiApproval(
        approvalLogId: Int,
        catatan: String? = null
    ): Result<ApprovalLogDto>

    suspend fun tolakApproval(
        approvalLogId: Int,
        catatan: String
    ): Result<ApprovalLogDto>
}
