package com.example.codasuaka.domain.usecase

import com.example.codasuaka.domain.model.User
import com.example.codasuaka.domain.repository.AuthRepository

/**
 * Use case untuk register.
 * Validasi bisnis dilakukan disini.
 */
class RegisterUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        namaInstansi: String,
        namaPemilik: String,
        email: String,
        password: String,
        passwordConfirmation: String
    ): Result<User> {
        // Validasi bisnis
        if (namaInstansi.isBlank()) {
            return Result.failure(Exception("Nama instansi wajib diisi."))
        }
        if (namaPemilik.isBlank()) {
            return Result.failure(Exception("Nama pemilik wajib diisi."))
        }
        if (email.isBlank()) {
            return Result.failure(Exception("Email wajib diisi."))
        }
        if (password.isBlank()) {
            return Result.failure(Exception("Password wajib diisi."))
        }
        if (password.length < 8) {
            return Result.failure(Exception("Password minimal harus 8 karakter."))
        }
        if (password != passwordConfirmation) {
            return Result.failure(Exception("Konfirmasi password tidak cocok."))
        }

        return authRepository.register(namaInstansi, namaPemilik, email, password)
    }
}
