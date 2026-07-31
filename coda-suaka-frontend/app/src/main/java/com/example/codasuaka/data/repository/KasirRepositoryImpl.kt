package com.example.codasuaka.data.repository

import com.example.codasuaka.data.remote.ApiService
import com.example.codasuaka.data.remote.dto.*
import com.example.codasuaka.domain.repository.KasirRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class KasirRepositoryImpl(
    private val apiService: ApiService
) : KasirRepository {

    // ─── Barang/Jasa ────────────────────────────────────────────

    override suspend fun getBarangJasaList(
        jenis: String?,
        isActive: Boolean?
    ): Result<List<BarangJasaDto>> = runCatching {
        val response = apiService.getBarangJasaList(jenis = jenis, isActive = isActive)
        if (response.isSuccessful) {
            response.body()?.data ?: emptyList()
        } else {
            throw Exception(parseErrorMessage(response, "Gagal memuat barang/jasa: ${response.code()}"))
        }
    }

    override suspend fun createBarangJasa(request: BarangJasaRequest): Result<BarangJasaDto> = runCatching {
        val response = apiService.createBarangJasa(request)
        if (response.isSuccessful) {
            response.body()?.data ?: throw Exception("Gagal membuat barang/jasa")
        } else {
            throw Exception(parseErrorMessage(response, "Gagal membuat barang/jasa: ${response.code()}"))
        }
    }

    override suspend fun updateBarangJasa(id: Int, request: BarangJasaRequest): Result<BarangJasaDto> = runCatching {
        val response = apiService.updateBarangJasa(id, request)
        if (response.isSuccessful) {
            response.body()?.data ?: throw Exception("Gagal mengupdate barang/jasa")
        } else {
            throw Exception(parseErrorMessage(response, "Gagal mengupdate barang/jasa: ${response.code()}"))
        }
    }

    override suspend fun deleteBarangJasa(id: Int): Result<Unit> = runCatching {
        val response = apiService.deleteBarangJasa(id)
        if (!response.isSuccessful) {
            throw Exception(parseErrorMessage(response, "Gagal menghapus barang/jasa: ${response.code()}"))
        }
    }

    /**
     * Ekstrak pesan error asli dari body response (mis. pesan 422 kustom dari
     * backend seperti "Barang/jasa sudah dipakai di nota, tidak bisa dihapus.")
     * supaya tidak hilang jadi cuma kode HTTP generik.
     */
    private fun parseErrorMessage(response: retrofit2.Response<*>, fallback: String): String {
        val body = response.errorBody()?.string()
        if (body.isNullOrBlank()) return fallback
        return try {
            val json = org.json.JSONObject(body)
            val errors = json.optJSONObject("errors")
            if (errors != null && errors.length() > 0) {
                val messages = mutableListOf<String>()
                errors.keys().forEach { key ->
                    errors.optJSONArray(key)?.let { arr ->
                        if (arr.length() > 0) messages.add(arr.getString(0))
                    }
                }
                messages.joinToString("\n").ifBlank { json.optString("message", fallback) }
            } else {
                json.optString("message", fallback)
            }
        } catch (e: Exception) {
            fallback
        }
    }

    // ─── Nota ───────────────────────────────────────────────────

    override suspend fun getNotaList(
        tipe: String?,
        status: String?,
        page: Int,
        outletId: Int?,
        startDate: String?,
        endDate: String?,
        perPage: Int?
    ): Result<Pair<List<NotaDto>, PaginationMeta?>> = runCatching {
        val response = apiService.getNotaList(
            page = page,
            tipe = tipe,
            outletId = outletId,
            startDate = startDate,
            endDate = endDate,
            status = status,
            perPage = perPage
        )
        if (response.isSuccessful) {
            val body = response.body()
            Pair(body?.data ?: emptyList(), body?.meta)
        } else {
            throw Exception("Gagal memuat nota: ${response.code()}")
        }
    }

    override suspend fun createNota(request: CreateNotaRequest): Result<NotaDto> = runCatching {
        val response = apiService.createNota(request)
        if (response.isSuccessful) {
            response.body()?.data ?: throw Exception("Gagal membuat nota")
        } else {
            throw Exception("Gagal membuat nota: ${response.code()}")
        }
    }

    override suspend fun getNotaDetail(id: Int): Result<NotaDto> = runCatching {
        val response = apiService.getNotaDetail(id)
        if (response.isSuccessful) {
            response.body()?.data ?: throw Exception("Nota tidak ditemukan")
        } else {
            throw Exception("Gagal memuat nota: ${response.code()}")
        }
    }

    override suspend fun deleteNota(id: Int): Result<Unit> = runCatching {
        val response = apiService.deleteNota(id)
        if (!response.isSuccessful) {
            throw Exception("Gagal menghapus nota: ${response.code()}")
        }
    }

    override suspend fun getNotaPdf(id: Int): Result<okhttp3.ResponseBody> = runCatching {
        val response = apiService.getNotaPdf(id)
        if (response.isSuccessful) {
            response.body() ?: throw Exception("File PDF nota kosong")
        } else {
            throw Exception("Gagal mengekspor PDF nota: ${response.code()}")
        }
    }

    override suspend fun importNotaPembelian(
        fileBytes: ByteArray,
        fileName: String,
        tanggal: String,
        outletId: Int?,
        pihakTerkait: String?,
        metodePembayaran: String?,
        kategoriTransaksiId: Int?,
        catatan: String?
    ): Result<NotaDto> = runCatching {
        fun String.toPart(): RequestBody = this.toRequestBody("text/plain".toMediaTypeOrNull())

        val requestFile = fileBytes.toRequestBody("application/octet-stream".toMediaTypeOrNull())
        val filePart = MultipartBody.Part.createFormData("file", fileName, requestFile)

        val response = apiService.importNotaPembelian(
            file = filePart,
            tanggal = tanggal.toPart(),
            outletId = outletId?.toString()?.toPart(),
            pihakTerkait = pihakTerkait?.toPart(),
            metodePembayaran = metodePembayaran?.toPart(),
            kategoriTransaksiId = kategoriTransaksiId?.toString()?.toPart(),
            catatan = catatan?.toPart()
        )
        if (response.isSuccessful) {
            response.body()?.data ?: throw Exception("Gagal mengimpor nota pembelian")
        } else {
            throw Exception("Gagal mengimpor nota pembelian: ${response.code()}")
        }
    }
}
