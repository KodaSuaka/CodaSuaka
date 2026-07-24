package com.example.codasuaka.ui.screen.login

import com.example.codasuaka.domain.model.User
import com.example.codasuaka.domain.repository.AuthRepository
import com.example.codasuaka.domain.usecase.LoginUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var authRepository: AuthRepository
    private lateinit var loginUseCase: LoginUseCase

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        authRepository = mockk()
        loginUseCase = LoginUseCase(authRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is default LoginUiState`() {
        // Arrange & Act
        val viewModel = LoginViewModel(loginUseCase)

        // Assert
        val state = viewModel.uiState.value
        assertEquals("Initial email should be empty", "", state.email)
        assertEquals("Initial password should be empty", "", state.password)
        assertFalse("Initial isLoading should be false", state.isLoading)
        assertNull("Initial errorMessage should be null", state.errorMessage)
        assertFalse("Initial loginSuccess should be false", state.loginSuccess)
        assertNull("Initial userRole should be null", state.userRole)
    }

    @Test
    fun `onEmailChange updates email state`() {
        // Arrange
        val viewModel = LoginViewModel(loginUseCase)
        val expectedEmail = "user@test.com"

        // Act
        viewModel.onEmailChange(expectedEmail)

        // Assert
        assertEquals(expectedEmail, viewModel.uiState.value.email)
        assertNull("Error should be cleared on email change", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `onPasswordChange updates password state`() {
        // Arrange
        val viewModel = LoginViewModel(loginUseCase)
        val expectedPassword = "secret123"

        // Act
        viewModel.onPasswordChange(expectedPassword)

        // Assert
        assertEquals(expectedPassword, viewModel.uiState.value.password)
        assertNull("Error should be cleared on password change", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `login success updates state with user role and permissions`() = runTest {
        // Arrange
        val user = User(
            id = 1,
            namaLengkap = "Test Owner",
            email = "owner@test.com",
            role = "Owner",
            instansiId = null,
            outletId = null,
            token = "fake-token",
            permissions = listOf("manage_karyawan", "view_laporan")
        )
        coEvery { authRepository.login(any(), any()) } returns Result.success(user)

        val viewModel = LoginViewModel(loginUseCase)
        viewModel.onEmailChange("owner@test.com")
        viewModel.onPasswordChange("password")

        // Act
        viewModel.login()
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertTrue("loginSuccess should be true", state.loginSuccess)
        assertEquals("Owner", state.userRole)
        assertEquals(listOf("manage_karyawan", "view_laporan"), state.userPermissions)
        assertFalse("isLoading should be false after success", state.isLoading)
        coVerify { authRepository.login("owner@test.com", "password") }
    }

    @Test
    fun `login success with Karyawan role provides karyawan role`() = runTest {
        // Arrange
        val user = User(
            id = 2,
            namaLengkap = "Test Karyawan",
            email = "karyawan@test.com",
            role = "Karyawan",
            instansiId = null,
            outletId = null,
            token = "fake-token-karyawan"
        )
        coEvery { authRepository.login(any(), any()) } returns Result.success(user)

        val viewModel = LoginViewModel(loginUseCase)
        viewModel.onEmailChange("karyawan@test.com")
        viewModel.onPasswordChange("password")

        // Act
        viewModel.login()
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertTrue("loginSuccess should be true", state.loginSuccess)
        assertEquals("Karyawan", state.userRole)
    }

    @Test
    fun `login failure updates state to error`() = runTest {
        // Arrange
        val errorMessage = "Email atau password salah"
        coEvery { authRepository.login(any(), any()) } returns Result.failure(Exception(errorMessage))

        val viewModel = LoginViewModel(loginUseCase)
        viewModel.onEmailChange("wrong@test.com")
        viewModel.onPasswordChange("wrong")

        // Act
        viewModel.login()
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertFalse("loginSuccess should be false", state.loginSuccess)
        assertNotNull("errorMessage should not be null", state.errorMessage)
        assertTrue("errorMessage should contain 'salah'", state.errorMessage!!.contains("salah"))
        assertFalse("isLoading should be false after failure", state.isLoading)
    }

    @Test
    fun `login with empty email should show validation error`() = runTest {
        // Arrange
        val viewModel = LoginViewModel(loginUseCase)
        viewModel.onEmailChange("")
        viewModel.onPasswordChange("somepassword")

        // Act
        viewModel.login()
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertFalse("loginSuccess should be false", state.loginSuccess)
        assertNotNull("errorMessage should not be null for empty email", state.errorMessage)
    }

    @Test
    fun `clearError resets errorMessage to null`() {
        // Arrange
        val viewModel = LoginViewModel(loginUseCase)
        viewModel.onEmailChange("")
        viewModel.onPasswordChange("somepassword")
        viewModel.login()
        // At this point, uiState.value.errorMessage should be set

        // Act
        viewModel.clearError()

        // Assert
        assertNull("errorMessage should be null after clearError", viewModel.uiState.value.errorMessage)
    }
}
