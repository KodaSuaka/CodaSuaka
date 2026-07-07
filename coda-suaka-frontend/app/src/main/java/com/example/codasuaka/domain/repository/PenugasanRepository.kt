package com.example.codasuaka.domain.repository

import com.example.codasuaka.data.remote.dto.PenugasanDto
import com.example.codasuaka.data.remote.dto.CreatePenugasanRequest
import com.example.codasuaka.data.remote.dto.UpdatePenugasanRequest

interface PenugasanRepository {
    suspend fun getPenugasans(
        divisiId: Int? = null,
        status: String? = null,
        penanggungJawabId: String? = null
    ): Result<List<PenugasanDto>>

    suspend fun getPenugasan(id: Int): Result<PenugasanDto>
    suspend fun createPenugasan(request: CreatePenugasanRequest): Result<PenugasanDto>
    suspend fun updatePenugasan(id: Int, request: UpdatePenugasanRequest): Result<PenugasanDto>
    suspend fun deletePenugasan(id: Int): Result<Unit>
}
