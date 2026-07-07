package com.example.codasuaka.domain.repository

import com.example.codasuaka.data.remote.dto.JadwalDto
import com.example.codasuaka.data.remote.dto.CreateJadwalRequest
import com.example.codasuaka.data.remote.dto.UpdateJadwalRequest

interface JadwalRepository {
    suspend fun getJadwals(
        outletId: Int? = null,
        bulan: Int? = null,
        tahun: Int? = null,
        tanggal: String? = null
    ): Result<List<JadwalDto>>

    suspend fun getJadwal(id: Int): Result<JadwalDto>
    suspend fun createJadwal(request: CreateJadwalRequest): Result<JadwalDto>
    suspend fun updateJadwal(id: Int, request: UpdateJadwalRequest): Result<JadwalDto>
    suspend fun deleteJadwal(id: Int): Result<Unit>
}
