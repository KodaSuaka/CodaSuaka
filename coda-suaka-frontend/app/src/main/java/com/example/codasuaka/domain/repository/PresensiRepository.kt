package com.example.codasuaka.domain.repository

import com.example.codasuaka.data.remote.dto.PresensiDto
import com.example.codasuaka.data.remote.dto.PresensiTodayData
import com.example.codasuaka.data.remote.dto.RekapKehadiranDto

interface PresensiRepository {
    suspend fun getPresensis(
        tanggal: String? = null,
        bulan: Int? = null,
        tahun: Int? = null,
        userId: Int? = null
    ): Result<List<PresensiDto>>

    suspend fun checkin(lokasi: String? = null): Result<PresensiDto>
    suspend fun checkout(): Result<PresensiDto>
    suspend fun getPresensiToday(): Result<PresensiTodayData>
    suspend fun getRekapKehadiran(bulan: Int?, tahun: Int?): Result<List<RekapKehadiranDto>>
}
