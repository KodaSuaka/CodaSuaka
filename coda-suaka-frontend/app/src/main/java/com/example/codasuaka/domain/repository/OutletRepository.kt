package com.example.codasuaka.domain.repository

import com.example.codasuaka.data.remote.dto.OutletDto
import com.example.codasuaka.data.remote.dto.OutletRequest

interface OutletRepository {
    suspend fun getOutlets(instansiId: Int? = null): Result<List<OutletDto>>
    suspend fun getOutlet(id: Int): Result<OutletDto>
    suspend fun createOutlet(request: OutletRequest): Result<OutletDto>
    suspend fun updateOutlet(id: Int, request: OutletRequest): Result<OutletDto>
    suspend fun deleteOutlet(id: Int): Result<Unit>
}
