package com.example.codasuaka.di

import com.example.codasuaka.domain.repository.AuthRepository
import com.example.codasuaka.domain.usecase.LoginUseCase
import com.example.codasuaka.domain.usecase.RegisterUseCase
import org.koin.dsl.module

/**
 * Module Koin untuk dependency domain layer.
 * Menyediakan use cases yang dibutuhkan ViewModel.
 */
val domainModule = module {
    factory { LoginUseCase(authRepository = get<AuthRepository>()) }
    factory { RegisterUseCase(authRepository = get<AuthRepository>()) }
}
