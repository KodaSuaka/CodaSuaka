package com.example.codasuaka.data.repository

import com.example.codasuaka.data.remote.ApiService
import com.example.codasuaka.data.remote.dto.*
import com.example.codasuaka.domain.repository.DivisiRepository

class DivisiRepositoryImpl(
    private val apiService: ApiService
) : DivisiRepository {

    override suspend fun getDivisis(outletId: Int?): Result<List<DivisiDto>> = runCatching {
        val response = apiService.getDivisis(outletId)
        if (response.isSuccessful && response.body()?.status == "success") {
            response.body()?.data ?: emptyList()
        } else {
            throw Exception("Gagal memuat divisi: ${response.code()}")
        }
    }

    override suspend fun getDivisi(id: Int): Result<DivisiDto> = runCatching {
        val response = apiService.getDivisi(id)
        if (response.isSuccessful && response.body()?.status == "success") {
            response.body()?.data ?: throw Exception("Divisi tidak ditemukan")
        } else {
            throw Exception("Gagal memuat divisi: ${response.code()}")
        }
    }

    override suspend fun createDivisi(request: CreateDivisiRequest): Result<DivisiDto> = runCatching {
        val response = apiService.createDivisi(request)
        if (response.isSuccessful && response.body()?.status == "success") {
            response.body()?.data ?: throw Exception("Gagal membuat divisi")
        } else {
            throw Exception("Gagal membuat divisi: ${response.code()}")
        }
    }

    override suspend fun updateDivisi(id: Int, request: UpdateDivisiRequest): Result<DivisiDto> = runCatching {
        val response = apiService.updateDivisi(id, request)
        if (response.isSuccessful && response.body()?.status == "success") {
            response.body()?.data ?: throw Exception("Gagal memperbarui divisi")
        } else {
            throw Exception("Gagal memperbarui divisi: ${response.code()}")
        }
    }

    override suspend fun deleteDivisi(id: Int): Result<Unit> = runCatching {
        val response = apiService.deleteDivisi(id)
        if (!response.isSuccessful) {
            throw Exception("Gagal menghapus divisi: ${response.code()}")
        }
    }

    override suspend fun getAnggotaDivisis(): Result<List<AnggotaDivisiDto>> = runCatching {
        val response = apiService.getAnggotaDivisis()
        if (response.isSuccessful && response.body()?.status == "success") {
            response.body()?.data ?: emptyList()
        } else {
            throw Exception("Gagal memuat anggota divisi: ${response.code()}")
        }
    }

    override suspend fun createAnggotaDivisi(request: CreateAnggotaDivisiRequest): Result<Unit> = runCatching {
        val response = apiService.createAnggotaDivisi(request)
        if (!response.isSuccessful) {
            throw Exception("Gagal menambah anggota: ${response.code()}")
        }
    }

    override suspend fun deleteAnggotaDivisi(id: Int): Result<Unit> = runCatching {
        val response = apiService.deleteAnggotaDivisi(id)
        if (!response.isSuccessful) {
            throw Exception("Gagal menghapus anggota: ${response.code()}")
        }
    }
}
