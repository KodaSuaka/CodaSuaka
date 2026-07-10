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
}
