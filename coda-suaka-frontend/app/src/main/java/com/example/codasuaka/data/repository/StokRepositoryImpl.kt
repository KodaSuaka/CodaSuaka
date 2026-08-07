package com.example.codasuaka.data.repository

import com.example.codasuaka.data.remote.ApiService
import com.example.codasuaka.data.remote.dto.PaginationMeta
import com.example.codasuaka.data.remote.dto.StokDto
import com.example.codasuaka.data.remote.dto.StokMutationDto
import com.example.codasuaka.data.remote.dto.StokMutationRequest
import com.example.codasuaka.data.remote.dto.StokRequest
import com.example.codasuaka.domain.repository.StokRepository

class StokRepositoryImpl(
    private val apiService: ApiService
) : StokRepository {

    override suspend fun getStokList(
        page: Int,
        kategori: String?,
        isActive: Boolean?,
        search: String?
    ): Result<Pair<List<StokDto>, PaginationMeta?>> = runCatching {
        val response = apiService.getStokList(
            page = page,
            kategori = kategori,
            isActive = isActive,
            search = search,
            perPage = 50
        )
        if (response.isSuccessful && response.body()?.status == "success") {
            val body = response.body()!!
            body.data to body.meta
        } else {
            throw RuntimeException(parseErrorMessage(response, "Gagal memuat data stok"))
        }
    }

    override suspend fun getStokDetail(id: Int): Result<StokDto> = runCatching {
        val response = apiService.getStokDetail(id)
        if (response.isSuccessful && response.body()?.status == "success") {
            response.body()!!.data ?: throw RuntimeException("Data stok kosong")
        } else {
            throw RuntimeException(parseErrorMessage(response, "Gagal memuat detail stok"))
        }
    }

    override suspend fun createStok(request: StokRequest): Result<StokDto> = runCatching {
        val response = apiService.createStok(request)
        if (response.isSuccessful && response.body()?.status == "success") {
            response.body()!!.data ?: throw RuntimeException("Data stok kosong")
        } else {
            throw RuntimeException(parseErrorMessage(response, "Gagal menambah stok"))
        }
    }

    override suspend fun updateStok(id: Int, request: StokRequest): Result<StokDto> = runCatching {
        val response = apiService.updateStok(id, request)
        if (response.isSuccessful && response.body()?.status == "success") {
            response.body()!!.data ?: throw RuntimeException("Data stok kosong")
        } else {
            throw RuntimeException(parseErrorMessage(response, "Gagal memperbarui stok"))
        }
    }

    override suspend fun deleteStok(id: Int): Result<Unit> = runCatching {
        val response = apiService.deleteStok(id)
        if (!response.isSuccessful) {
            throw RuntimeException(parseErrorMessage(response, "Gagal menghapus stok"))
        }
    }

    override suspend fun mutateStok(id: Int, request: StokMutationRequest): Result<StokMutationDto> = runCatching {
        val response = apiService.mutateStok(id, request)
        if (response.isSuccessful && response.body()?.status == "success") {
            response.body()!!.data ?: throw RuntimeException("Data mutasi kosong")
        } else {
            throw RuntimeException(parseErrorMessage(response, "Gagal melakukan mutasi stok"))
        }
    }

    override suspend fun getStokRiwayat(
        id: Int,
        page: Int,
        jenis: String?
    ): Result<Pair<List<StokMutationDto>, PaginationMeta?>> = runCatching {
        val response = apiService.getStokRiwayat(id = id, page = page, jenis = jenis, perPage = 50)
        if (response.isSuccessful && response.body()?.status == "success") {
            val body = response.body()!!
            body.data to body.meta
        } else {
            throw RuntimeException(parseErrorMessage(response, "Gagal memuat riwayat stok"))
        }
    }

    private fun parseErrorMessage(response: retrofit2.Response<*>, fallback: String): String {
        return try {
            response.errorBody()?.string()?.let { raw ->
                val json = org.json.JSONObject(raw)
                json.optString("message", fallback)
            } ?: fallback
        } catch (e: Exception) {
            fallback
        }
    }
}
