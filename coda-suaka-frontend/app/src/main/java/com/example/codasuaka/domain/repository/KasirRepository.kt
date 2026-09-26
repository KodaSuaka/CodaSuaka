package com.example.codasuaka.domain.repository

import com.example.codasuaka.data.remote.dto.*

interface KasirRepository {

    // ─── Barang/Jasa ────────────────────────────────────────────

    suspend fun getBarangJasaList(
        jenis: String? = null,
        isActive: Boolean? = null
    ): Result<List<BarangJasaDto>>

    suspend fun createBarangJasa(request: BarangJasaRequest): Result<BarangJasaDto>

    suspend fun updateBarangJasa(id: Int, request: BarangJasaRequest): Result<BarangJasaDto>

    suspend fun deleteBarangJasa(id: Int): Result<Unit>

    // ─── Nota ───────────────────────────────────────────────────

    suspend fun getNotaList(
        tipe: String? = null,
        status: String? = null,
        page: Int = 1,
        outletId: Int? = null,
        startDate: String? = null,
        endDate: String? = null,
        perPage: Int? = null
    ): Result<Pair<List<NotaDto>, PaginationMeta?>>

    suspend fun createNota(request: CreateNotaRequest): Result<NotaDto>

    suspend fun getNotaDetail(id: Int): Result<NotaDto>

    suspend fun deleteNota(id: Int): Result<Unit>

    suspend fun getNotaPdf(id: Int): Result<okhttp3.ResponseBody>

    suspend fun importNotaPembelian(
        fileBytes: ByteArray,
        fileName: String,
        tanggal: String,
        outletId: Int?,
        pihakTerkait: String?,
        metodePembayaran: String?,
        kategoriTransaksiId: Int?,
        catatan: String?
    ): Result<NotaDto>
}
