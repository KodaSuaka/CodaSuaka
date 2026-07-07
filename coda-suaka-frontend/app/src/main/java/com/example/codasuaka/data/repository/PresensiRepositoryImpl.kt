package com.example.codasuaka.data.repository

import com.example.codasuaka.data.remote.ApiService
import com.example.codasuaka.data.remote.dto.PresensiDto
import com.example.codasuaka.data.remote.dto.PresensiTodayData
import com.example.codasuaka.data.remote.dto.RekapKehadiranDto
import com.example.codasuaka.domain.repository.PresensiRepository

class PresensiRepositoryImpl(
    private val apiService: ApiService
) : PresensiRepository {

    override suspend fun getPresensis(
        tanggal: String?,
        bulan: Int?,
        tahun: Int?,
        userId: Int?
    ): Result<List<PresensiDto>> = runCatching {
        val response = apiService.getPresensis(tanggal, bulan, tahun, userId)
        if (response.isSuccessful && response.body()?.status == "success") {
            response.body()?.data ?: emptyList()
        } else {
            throw Exception("Gagal memuat presensi: ${response.code()}")
        }
    }

    override suspend fun checkin(lokasi: String?): Result<PresensiDto> = runCatching {
        val body = if (lokasi != null) mapOf("lokasi" to lokasi) else null
        val response = apiService.checkin(body)
        if (response.isSuccessful && response.body()?.status == "success") {
            response.body()?.data ?: throw Exception("Gagal checkin")
        } else {
            throw Exception("Gagal checkin: ${response.code()}")
        }
    }

    override suspend fun checkout(): Result<PresensiDto> = runCatching {
        val response = apiService.checkout()
        if (response.isSuccessful && response.body()?.status == "success") {
            response.body()?.data ?: throw Exception("Gagal checkout")
        } else {
            throw Exception("Gagal checkout: ${response.code()}")
        }
    }

    override suspend fun getPresensiToday(): Result<PresensiTodayData> = runCatching {
        val response = apiService.getPresensiToday()
        if (response.isSuccessful && response.body()?.status == "success") {
            response.body()?.data ?: throw Exception("Data presensi hari ini kosong")
        } else {
            throw Exception("Gagal memuat presensi hari ini: ${response.code()}")
        }
    }

    override suspend fun getRekapKehadiran(bulan: Int?, tahun: Int?): Result<List<RekapKehadiranDto>> = runCatching {
        val response = apiService.getRekapKehadiran(bulan, tahun)
        if (response.isSuccessful && response.body()?.status == "success") {
            response.body()?.data ?: emptyList()
        } else {
            throw Exception("Gagal memuat rekap kehadiran: ${response.code()}")
        }
    }
}
