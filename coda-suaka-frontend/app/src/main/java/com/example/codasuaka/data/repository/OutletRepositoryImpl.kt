package com.example.codasuaka.data.repository

import com.example.codasuaka.data.remote.ApiService
import com.example.codasuaka.data.remote.dto.OutletDto
import com.example.codasuaka.data.remote.dto.OutletRequest
import com.example.codasuaka.domain.repository.OutletRepository

class OutletRepositoryImpl(
    private val apiService: ApiService
) : OutletRepository {

    override suspend fun getOutlets(instansiId: Int?): Result<List<OutletDto>> = runCatching {
        val response = apiService.getOutlets(instansiId)
        if (response.isSuccessful && response.body()?.status == "success") {
            response.body()?.data ?: emptyList()
        } else {
            throw Exception("Gagal memuat outlet: ${response.code()}")
        }
    }

    override suspend fun getOutlet(id: Int): Result<OutletDto> = runCatching {
        val response = apiService.getOutlet(id)
        if (response.isSuccessful && response.body()?.status == "success") {
            response.body()?.data ?: throw Exception("Outlet tidak ditemukan")
        } else {
            throw Exception("Gagal memuat outlet: ${response.code()}")
        }
    }

    override suspend fun createOutlet(request: OutletRequest): Result<OutletDto> = runCatching {
        val response = apiService.createOutlet(request)
        if (response.isSuccessful && response.body()?.status == "success") {
            response.body()?.data ?: throw Exception("Gagal membuat outlet")
        } else {
            throw Exception("Gagal membuat outlet: ${response.code()}")
        }
    }

    override suspend fun updateOutlet(id: Int, request: OutletRequest): Result<OutletDto> = runCatching {
        val response = apiService.updateOutlet(id, request)
        if (response.isSuccessful && response.body()?.status == "success") {
            response.body()?.data ?: throw Exception("Gagal memperbarui outlet")
        } else {
            throw Exception("Gagal memperbarui outlet: ${response.code()}")
        }
    }

    override suspend fun deleteOutlet(id: Int): Result<Unit> = runCatching {
        val response = apiService.deleteOutlet(id)
        if (!response.isSuccessful) {
            throw Exception("Gagal menghapus outlet: ${response.code()}")
        }
    }
}
