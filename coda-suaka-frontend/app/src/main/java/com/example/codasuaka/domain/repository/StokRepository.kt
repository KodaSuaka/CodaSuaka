package com.example.codasuaka.domain.repository

import com.example.codasuaka.data.remote.dto.PaginationMeta
import com.example.codasuaka.data.remote.dto.StokDto
import com.example.codasuaka.data.remote.dto.StokMutationDto
import com.example.codasuaka.data.remote.dto.StokMutationRequest
import com.example.codasuaka.data.remote.dto.StokRequest

interface StokRepository {

    suspend fun getStokList(
        page: Int = 1,
        kategori: String? = null,
        isActive: Boolean? = null,
        search: String? = null
    ): Result<Pair<List<StokDto>, PaginationMeta?>>

    suspend fun getStokDetail(id: Int): Result<StokDto>

    suspend fun createStok(request: StokRequest): Result<StokDto>

    suspend fun updateStok(id: Int, request: StokRequest): Result<StokDto>

    suspend fun deleteStok(id: Int): Result<Unit>

    suspend fun mutateStok(id: Int, request: StokMutationRequest): Result<StokMutationDto>

    suspend fun getStokRiwayat(
        id: Int,
        page: Int = 1,
        jenis: String? = null
    ): Result<Pair<List<StokMutationDto>, PaginationMeta?>>
}
