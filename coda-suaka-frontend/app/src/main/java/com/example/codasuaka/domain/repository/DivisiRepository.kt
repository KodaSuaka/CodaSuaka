package com.example.codasuaka.domain.repository

import com.example.codasuaka.data.remote.dto.DivisiDto
import com.example.codasuaka.data.remote.dto.CreateDivisiRequest
import com.example.codasuaka.data.remote.dto.UpdateDivisiRequest
import com.example.codasuaka.data.remote.dto.AnggotaDivisiDto
import com.example.codasuaka.data.remote.dto.CreateAnggotaDivisiRequest

interface DivisiRepository {
    suspend fun getDivisis(outletId: Int? = null): Result<List<DivisiDto>>
    suspend fun getDivisi(id: Int): Result<DivisiDto>
    suspend fun createDivisi(request: CreateDivisiRequest): Result<DivisiDto>
    suspend fun updateDivisi(id: Int, request: UpdateDivisiRequest): Result<DivisiDto>
    suspend fun deleteDivisi(id: Int): Result<Unit>
    suspend fun getAnggotaDivisis(): Result<List<AnggotaDivisiDto>>
    suspend fun createAnggotaDivisi(request: CreateAnggotaDivisiRequest): Result<Unit>
    suspend fun deleteAnggotaDivisi(id: Int): Result<Unit>
}
