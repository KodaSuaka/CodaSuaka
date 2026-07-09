package com.example.codasuaka.data.repository

import com.example.codasuaka.data.remote.ApiService
import com.example.codasuaka.data.remote.dto.*
import com.example.codasuaka.domain.repository.KeuanganRepository

class KeuanganRepositoryImpl(
    private val apiService: ApiService
) : KeuanganRepository {

    // ─── Kategori Transaksi ───────────────────────────────────

    override suspend fun getKategoriTransaksis(
        tipe: String?,
        activeOnly: Boolean?
    ): Result<List<KategoriTransaksiDto>> = runCatching {
        val response = apiService.getKategoriTransaksis(tipe, activeOnly)
        if (response.isSuccessful) {
            response.body()?.data ?: throw Exception("Data kategori transaksi kosong")
        } else {
            throw Exception("Gagal memuat kategori: ${response.code()}")
        }
    }

    override suspend fun getKategoriTransaksi(id: Int): Result<KategoriTransaksiDto> = runCatching {
        val response = apiService.getKategoriTransaksi(id)
        if (response.isSuccessful) {
            response.body()?.data ?: throw Exception("Kategori transaksi tidak ditemukan")
        } else {
            throw Exception("Gagal memuat kategori: ${response.code()}")
        }
    }

    override suspend fun createKategoriTransaksi(
        request: CreateKategoriTransaksiRequest
    ): Result<KategoriTransaksiDto> = runCatching {
        val response = apiService.createKategoriTransaksi(request)
        if (response.isSuccessful) {
            response.body()?.data ?: throw Exception("Gagal membuat kategori transaksi")
        } else {
            throw Exception("Gagal membuat kategori: ${response.code()}")
        }
    }

    override suspend fun updateKategoriTransaksi(
        id: Int,
        request: Map<String, Any>
    ): Result<KategoriTransaksiDto> = runCatching {
        val response = apiService.updateKategoriTransaksi(id, request)
        if (response.isSuccessful) {
            response.body()?.data ?: throw Exception("Gagal mengupdate kategori transaksi")
        } else {
            throw Exception("Gagal mengupdate kategori: ${response.code()}")
        }
    }

    override suspend fun deleteKategoriTransaksi(id: Int): Result<Unit> = runCatching {
        val response = apiService.deleteKategoriTransaksi(id)
        if (response.isSuccessful) {
            Unit
        } else {
            throw Exception("Gagal menghapus kategori: ${response.code()}")
        }
    }

    // ─── Transaksi Kas ────────────────────────────────────────

    override suspend fun getTransaksiKasList(
        outletId: Int?,
        tipe: String?,
        kategoriTransaksiId: Int?,
        startDate: String?,
        endDate: String?,
        perPage: Int?
    ): Result<Pair<List<TransaksiKasDto>, PaginationMeta?>> = runCatching {
        val response = apiService.getTransaksiKasList(
            outletId, tipe, kategoriTransaksiId, startDate, endDate, perPage
        )
        if (response.isSuccessful) {
            val body = response.body()
            val list = body?.data ?: emptyList()
            val meta = body?.meta
            Pair(list, meta)
        } else {
            throw Exception("Gagal memuat transaksi kas: ${response.code()}")
        }
    }

    override suspend fun getTransaksiKas(id: Int): Result<TransaksiKasDto> = runCatching {
        val response = apiService.getTransaksiKas(id)
        if (response.isSuccessful) {
            response.body()?.data ?: throw Exception("Transaksi kas tidak ditemukan")
        } else {
            throw Exception("Gagal memuat transaksi: ${response.code()}")
        }
    }

    override suspend fun createTransaksiKas(
        request: CreateTransaksiKasRequest
    ): Result<TransaksiKasDto> = runCatching {
        val response = apiService.createTransaksiKas(request)
        if (response.isSuccessful) {
            response.body()?.data ?: throw Exception("Gagal membuat transaksi kas")
        } else {
            throw Exception("Gagal membuat transaksi: ${response.code()}")
        }
    }

    override suspend fun updateTransaksiKas(
        id: Int,
        request: UpdateTransaksiKasRequest
    ): Result<TransaksiKasDto> = runCatching {
        val response = apiService.updateTransaksiKas(id, request)
        if (response.isSuccessful) {
            response.body()?.data ?: throw Exception("Gagal mengupdate transaksi kas")
        } else {
            throw Exception("Gagal mengupdate transaksi: ${response.code()}")
        }
    }

    override suspend fun deleteTransaksiKas(id: Int): Result<Unit> = runCatching {
        val response = apiService.deleteTransaksiKas(id)
        if (response.isSuccessful) {
            Unit
        } else {
            throw Exception("Gagal menghapus transaksi: ${response.code()}")
        }
    }

    // ─── Saldo & Laba Rugi ────────────────────────────────────

    override suspend fun getSaldo(
        outletId: Int?,
        startDate: String?,
        endDate: String?
    ): Result<SaldoData> = runCatching {
        val response = apiService.getSaldo(outletId, startDate, endDate)
        if (response.isSuccessful) {
            response.body()?.data ?: throw Exception("Data saldo kosong")
        } else {
            throw Exception("Gagal memuat saldo: ${response.code()}")
        }
    }

    override suspend fun getLabaRugi(
        outletId: Int?,
        startDate: String?,
        endDate: String?
    ): Result<LabaRugiData> = runCatching {
        val response = apiService.getLabaRugi(outletId, startDate, endDate)
        if (response.isSuccessful) {
            response.body()?.data ?: throw Exception("Data laba rugi kosong")
        } else {
            throw Exception("Gagal memuat laba rugi: ${response.code()}")
        }
    }
}
