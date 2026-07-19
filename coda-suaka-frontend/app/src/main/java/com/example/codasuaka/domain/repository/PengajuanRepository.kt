package com.example.codasuaka.domain.repository

import com.example.codasuaka.data.remote.dto.PengajuanDto
import com.example.codasuaka.data.remote.dto.CreatePengajuanRequest
import com.example.codasuaka.data.remote.dto.RejectPengajuanRequest

interface PengajuanRepository {
    suspend fun getPengajuans(status: String? = null): Result<List<PengajuanDto>>
    suspend fun getPengajuan(id: Int): Result<PengajuanDto>
    suspend fun createPengajuan(request: CreatePengajuanRequest): Result<PengajuanDto>
    suspend fun approvePengajuan(id: Int): Result<PengajuanDto>
    suspend fun rejectPengajuan(id: Int, alasan: String): Result<PengajuanDto>
}
