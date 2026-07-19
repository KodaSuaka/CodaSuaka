package com.example.codasuaka.data.repository

import com.example.codasuaka.data.remote.ApiService
import com.example.codasuaka.data.remote.dto.*
import com.example.codasuaka.domain.repository.JadwalRepository

class JadwalRepositoryImpl(
    private val apiService: ApiService
) : JadwalRepository {

    override suspend fun getJadwals(
        outletId: Int?,
        bulan: Int?,
        tahun: Int?,
        tanggal: String?
    ): Result<List<JadwalDto>> = runCatching {
        val response = apiService.getJadwals(outletId, bulan, tahun, tanggal)
        if (response.isSuccessful && response.body()?.status == "success") {
            response.body()?.data ?: emptyList()
        } else {
            throw Exception("Gagal memuat jadwal: ${response.code()}")
        }
    }

    override suspend fun getJadwal(id: Int): Result<JadwalDto> = runCatching {
        val response = apiService.getJadwal(id)
        if (response.isSuccessful && response.body()?.status == "success") {
            response.body()?.data ?: throw Exception("Jadwal tidak ditemukan")
        } else {
            throw Exception("Gagal memuat jadwal: ${response.code()}")
        }
    }

    override suspend fun createJadwal(request: CreateJadwalRequest): Result<JadwalDto> = runCatching {
        val response = apiService.createJadwal(request)
        if (response.isSuccessful && response.body()?.status == "success") {
            response.body()?.data ?: throw Exception("Gagal membuat jadwal")
        } else {
            throw Exception("Gagal membuat jadwal: ${response.code()}")
        }
    }

    override suspend fun updateJadwal(id: Int, request: UpdateJadwalRequest): Result<JadwalDto> = runCatching {
        val response = apiService.updateJadwal(id, request)
        if (response.isSuccessful && response.body()?.status == "success") {
            response.body()?.data ?: throw Exception("Gagal memperbarui jadwal")
        } else {
            throw Exception("Gagal memperbarui jadwal: ${response.code()}")
        }
    }

    override suspend fun deleteJadwal(id: Int): Result<Unit> = runCatching {
        val response = apiService.deleteJadwal(id)
        if (!response.isSuccessful) {
            throw Exception("Gagal menghapus jadwal: ${response.code()}")
        }
    }
}
