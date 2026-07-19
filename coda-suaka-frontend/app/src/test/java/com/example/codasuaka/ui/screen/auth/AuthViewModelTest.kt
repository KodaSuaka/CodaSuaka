package com.example.codasuaka.ui.screen.auth

import com.example.codasuaka.data.local.TokenManager
import com.example.codasuaka.domain.repository.AuthRepository
import com.example.codasuaka.domain.model.User
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var authRepository: AuthRepository
    private lateinit var tokenManager: TokenManager
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        authRepository = mockk()
        tokenManager = mockk(relaxed = true)
        viewModel = AuthViewModel(authRepository, tokenManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `verifyToken returns true when token is valid`() = runTest {
        // Arrange
        coEvery { tokenManager.getToken() } returns "valid-token"
        coEvery { authRepository.verifyToken() } returns true

        // Act
        viewModel.checkAuthStatus()
        advanceUntilIdle()

        // Assert — should navigate to authenticated state
        val state = viewModel.authState.value
        assertTrue("Should be Authenticated when token valid", state is AuthState.Authenticated)
    }

    @Test
    fun `verifyToken returns false when token is invalid`() = runTest {
        // Arrange
        coEvery { tokenManager.getToken() } returns "expired-token"
        coEvery { authRepository.verifyToken() } returns false

        // Act
        viewModel.checkAuthStatus()
        advanceUntilIdle()

        // Assert
        val state = viewModel.authState.value
        assertTrue("Should be Unauthenticated when token invalid", state is AuthState.Unauthenticated)
    }

    @Test
    fun `verifyToken goes to unauthenticated when no token exists`() = runTest {
        // Arrange
        coEvery { tokenManager.getToken() } returns null

        // Act
        viewModel.checkAuthStatus()
        advanceUntilIdle()

        // Assert
        val state = viewModel.authState.value
        assertTrue("Should be Unauthenticated when no token", state is AuthState.Unauthenticated)
    }

    @Test
    fun `logout clears auth data and sets unauthenticated state`() = runTest {
        // Arrange
        coEvery { tokenManager.getToken() } returns "some-token"
        coEvery { authRepository.verifyToken() } returns true
        viewModel.checkAuthStatus()
        advanceUntilIdle()
        assertTrue(viewModel.authState.value is AuthState.Authenticated)

        // Act
        viewModel.logout()
        advanceUntilIdle()

        // Assert
        coVerify { authRepository.logout() }
        val state = viewModel.authState.value
        assertTrue("Should be Unauthenticated after logout", state is AuthState.Unauthenticated)
    }

    @Test
    fun `initial state is Loading`() {
        // Assert
        assertTrue("Initial state should be Loading", viewModel.authState.value is AuthState.Loading)
    }
}
