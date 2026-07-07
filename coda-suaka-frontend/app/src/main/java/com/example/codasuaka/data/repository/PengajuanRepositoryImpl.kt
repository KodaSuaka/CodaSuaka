package com.example.codasuaka.data.repository

import com.example.codasuaka.data.remote.ApiService
import com.example.codasuaka.data.remote.dto.*
import com.example.codasuaka.domain.repository.PengajuanRepository

class PengajuanRepositoryImpl(
    private val apiService: ApiService
) : PengajuanRepository {

    override suspend fun getPengajuans(status: String?): Result<List<PengajuanDto>> = runCatching {
        val response = apiService.getPengajuans(status)
        if (response.isSuccessful && response.body()?.status == "success") {
            response.body()?.data ?: emptyList()
        } else {
            throw Exception("Gagal memuat pengajuan: ${response.code()}")
        }
    }

    override suspend fun getPengajuan(id: Int): Result<PengajuanDto> = runCatching {
        val response = apiService.getPengajuan(id)
        if (response.isSuccessful && response.body()?.status == "success") {
            response.body()?.data ?: throw Exception("Pengajuan tidak ditemukan")
        } else {
            throw Exception("Gagal memuat pengajuan: ${response.code()}")
        }
    }

    override suspend fun createPengajuan(request: CreatePengajuanRequest): Result<PengajuanDto> = runCatching {
        val response = apiService.createPengajuan(request)
        if (response.isSuccessful && response.body()?.status == "success") {
            response.body()?.data ?: throw Exception("Gagal membuat pengajuan")
        } else {
            throw Exception("Gagal membuat pengajuan: ${response.code()}")
        }
    }

    override suspend fun approvePengajuan(id: Int): Result<PengajuanDto> = runCatching {
        val response = apiService.approvePengajuan(id)
        if (response.isSuccessful && response.body()?.status == "success") {
            response.body()?.data ?: throw Exception("Gagal menyetujui pengajuan")
        } else {
            throw Exception("Gagal menyetujui pengajuan: ${response.code()}")
        }
    }

    override suspend fun rejectPengajuan(id: Int, alasan: String): Result<PengajuanDto> = runCatching {
        val response = apiService.rejectPengajuan(id, RejectPengajuanRequest(alasan))
        if (response.isSuccessful && response.body()?.status == "success") {
            response.body()?.data ?: throw Exception("Gagal menolak pengajuan")
        } else {
            throw Exception("Gagal menolak pengajuan: ${response.code()}")
        }
    }
}
