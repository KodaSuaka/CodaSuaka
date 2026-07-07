package com.example.codasuaka.data.repository

import com.example.codasuaka.data.remote.ApiService
import com.example.codasuaka.data.remote.dto.*
import com.example.codasuaka.domain.repository.PenugasanRepository

class PenugasanRepositoryImpl(
    private val apiService: ApiService
) : PenugasanRepository {

    override suspend fun getPenugasans(
        divisiId: Int?,
        status: String?,
        penanggungJawabId: String?
    ): Result<List<PenugasanDto>> = runCatching {
        val response = apiService.getPenugasans(divisiId, status, penanggungJawabId)
        if (response.isSuccessful && response.body()?.status == "success") {
            response.body()?.data ?: emptyList()
        } else {
            throw Exception("Gagal memuat penugasan: ${response.code()}")
        }
    }

    override suspend fun getPenugasan(id: Int): Result<PenugasanDto> = runCatching {
        val response = apiService.getPenugasan(id)
        if (response.isSuccessful && response.body()?.status == "success") {
            response.body()?.data ?: throw Exception("Penugasan tidak ditemukan")
        } else {
            throw Exception("Gagal memuat penugasan: ${response.code()}")
        }
    }

    override suspend fun createPenugasan(request: CreatePenugasanRequest): Result<PenugasanDto> = runCatching {
        val response = apiService.createPenugasan(request)
        if (response.isSuccessful && response.body()?.status == "success") {
            response.body()?.data ?: throw Exception("Gagal membuat penugasan")
        } else {
            throw Exception("Gagal membuat penugasan: ${response.code()}")
        }
    }

    override suspend fun updatePenugasan(id: Int, request: UpdatePenugasanRequest): Result<PenugasanDto> = runCatching {
        val response = apiService.updatePenugasan(id, request)
        if (response.isSuccessful && response.body()?.status == "success") {
            response.body()?.data ?: throw Exception("Gagal memperbarui penugasan")
        } else {
            throw Exception("Gagal memperbarui penugasan: ${response.code()}")
        }
    }

    override suspend fun deletePenugasan(id: Int): Result<Unit> = runCatching {
        val response = apiService.deletePenugasan(id)
        if (!response.isSuccessful) {
            throw Exception("Gagal menghapus penugasan: ${response.code()}")
        }
    }
}
