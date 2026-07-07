package com.example.codasuaka.data.repository

import com.example.codasuaka.data.remote.ApiService
import com.example.codasuaka.data.remote.dto.DashboardData
import com.example.codasuaka.data.remote.dto.KaryawanDashboardData
import com.example.codasuaka.data.remote.dto.OmsetData
import com.example.codasuaka.domain.repository.DashboardRepository

class DashboardRepositoryImpl(
    private val apiService: ApiService
) : DashboardRepository {

    override suspend fun getDashboard(): Result<DashboardData> = runCatching {
        val response = apiService.getDashboard()
        if (response.isSuccessful) {
            response.body()?.data ?: throw Exception("Data dashboard kosong")
        } else {
            throw Exception("Gagal memuat dashboard: ${response.code()}")
        }
    }

    override suspend fun getKaryawanDashboard(): Result<KaryawanDashboardData> = runCatching {
        val response = apiService.getKaryawanDashboard()
        if (response.isSuccessful) {
            response.body()?.data ?: throw Exception("Data dashboard karyawan kosong")
        } else {
            throw Exception("Gagal memuat dashboard karyawan: ${response.code()}")
        }
    }

    override suspend fun getOmset(startDate: String?, endDate: String?): Result<OmsetData> = runCatching {
        val response = apiService.getDashboardOmset(startDate, endDate)
        if (response.isSuccessful) {
            response.body()?.data ?: throw Exception("Data omset kosong")
        } else {
            throw Exception("Gagal memuat omset: ${response.code()}")
        }
    }
}
