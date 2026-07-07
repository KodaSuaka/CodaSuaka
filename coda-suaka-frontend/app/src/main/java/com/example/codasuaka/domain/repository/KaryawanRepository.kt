package com.example.codasuaka.domain.repository

import com.example.codasuaka.data.remote.dto.KaryawanDto
import com.example.codasuaka.data.remote.dto.RoleDto
import com.example.codasuaka.data.remote.dto.CreateKaryawanRequest
import com.example.codasuaka.data.remote.dto.UpdateKaryawanRequest

interface KaryawanRepository {
    suspend fun getKaryawans(outletId: Int? = null): Result<List<KaryawanDto>>
    suspend fun getKaryawanMe(): Result<KaryawanDto>
    suspend fun getKaryawan(id: String): Result<KaryawanDto>
    suspend fun createKaryawan(request: CreateKaryawanRequest): Result<KaryawanDto>
    suspend fun updateKaryawan(id: String, request: UpdateKaryawanRequest): Result<KaryawanDto>
    suspend fun deleteKaryawan(id: String): Result<Unit>
    suspend fun getRoles(): Result<List<RoleDto>>
}
