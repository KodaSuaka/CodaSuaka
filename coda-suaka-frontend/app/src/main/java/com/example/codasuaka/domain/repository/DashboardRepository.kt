package com.example.codasuaka.domain.repository

import com.example.codasuaka.data.remote.dto.DashboardData
import com.example.codasuaka.data.remote.dto.KaryawanDashboardData
import com.example.codasuaka.data.remote.dto.OmsetData
import com.example.codasuaka.data.remote.dto.PoinKinerjaData

interface DashboardRepository {
    suspend fun getDashboard(): Result<DashboardData>
    suspend fun getKaryawanDashboard(): Result<KaryawanDashboardData>
    suspend fun getOmset(startDate: String?, endDate: String?): Result<OmsetData>
    suspend fun getPoinKinerja(): Result<PoinKinerjaData>
}
