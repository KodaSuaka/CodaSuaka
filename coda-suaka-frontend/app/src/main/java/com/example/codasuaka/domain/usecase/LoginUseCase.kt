package com.example.codasuaka.domain.usecase

import com.example.codasuaka.domain.model.User
import com.example.codasuaka.domain.repository.AuthRepository

/**
 * Use case untuk login.
 * Setiap operasi bisnis dibungkus dalam use case sendiri.
 */
class LoginUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<User> {
        // Validasi bisnis
        if (email.isBlank()) {
            return Result.failure(Exception("Email wajib diisi."))
        }
        if (password.isBlank()) {
            return Result.failure(Exception("Password wajib diisi."))
        }

        return authRepository.login(email, password)
    }
}
