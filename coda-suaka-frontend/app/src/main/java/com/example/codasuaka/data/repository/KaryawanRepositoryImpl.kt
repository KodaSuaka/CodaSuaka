package com.example.codasuaka.data.repository

import com.example.codasuaka.data.remote.ApiService
import com.example.codasuaka.data.remote.dto.*
import com.example.codasuaka.domain.repository.KaryawanRepository

class KaryawanRepositoryImpl(
    private val apiService: ApiService
) : KaryawanRepository {

    override suspend fun getKaryawans(outletId: Int?): Result<List<KaryawanDto>> = runCatching {
        val response = apiService.getKaryawans(outletId)
        if (response.isSuccessful && response.body()?.status == "success") {
            response.body()?.data ?: emptyList()
        } else {
            throw Exception("Gagal memuat karyawan: ${response.code()}")
        }
    }

    override suspend fun getKaryawanMe(): Result<KaryawanDto> = runCatching {
        val response = apiService.getKaryawanMe()
        if (response.isSuccessful && response.body()?.status == "success") {
            response.body()?.data ?: throw Exception("Profil karyawan tidak ditemukan")
        } else {
            throw Exception("Gagal memuat profil: ${response.code()}")
        }
    }

    override suspend fun getKaryawan(id: String): Result<KaryawanDto> = runCatching {
        val response = apiService.getKaryawan(id)
        if (response.isSuccessful && response.body()?.status == "success") {
            response.body()?.data ?: throw Exception("Karyawan tidak ditemukan")
        } else {
            throw Exception("Gagal memuat karyawan: ${response.code()}")
        }
    }

    override suspend fun createKaryawan(request: CreateKaryawanRequest): Result<KaryawanDto> = runCatching {
        val response = apiService.createKaryawan(request)
        if (response.isSuccessful && response.body()?.status == "success") {
            response.body()?.data ?: throw Exception("Gagal membuat karyawan")
        } else {
            throw Exception("Gagal membuat karyawan: ${response.code()}")
        }
    }

    override suspend fun updateKaryawan(id: String, request: UpdateKaryawanRequest): Result<KaryawanDto> = runCatching {
        val response = apiService.updateKaryawan(id, request)
        if (response.isSuccessful && response.body()?.status == "success") {
            response.body()?.data ?: throw Exception("Gagal memperbarui karyawan")
        } else {
            throw Exception("Gagal memperbarui karyawan: ${response.code()}")
        }
    }

    override suspend fun deleteKaryawan(id: String): Result<Unit> = runCatching {
        val response = apiService.deleteKaryawan(id)
        if (!response.isSuccessful) {
            throw Exception("Gagal menghapus karyawan: ${response.code()}")
        }
    }

    override suspend fun getRoles(): Result<List<RoleDto>> = runCatching {
        val response = apiService.getRoles()
        if (response.isSuccessful && response.body()?.status == "success") {
            response.body()?.data ?: emptyList()
        } else {
            throw Exception("Gagal memuat role: ${response.code()}")
        }
    }
}
