package com.example.codasuaka.ui.screen.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.codasuaka.ui.screen.components.CodaSuakaLogo
import com.example.codasuaka.ui.screen.components.CustomTextField
import com.example.codasuaka.ui.components.NotificationBannerStatic
import com.example.codasuaka.ui.theme.*

@Composable
fun LoginScreen(
    onLoginSuccess: (role: String, permissions: List<String>?) -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: LoginViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.loginSuccess, uiState.userRole) {
        if (uiState.loginSuccess && uiState.userRole != null) {
            onLoginSuccess(uiState.userRole!!, uiState.userPermissions)
        }
    }

    // Mandatory Privacy Policy Popup once per app install before login (Fully Scrollable Custom Dialog)
    if (uiState.showPrivacyPopup) {
        var agreeChecked by remember { mutableStateOf(false) }

        Dialog(
            onDismissRequest = { /* Cannot dismiss without agreeing */ },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .fillMaxHeight(0.85f),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    // Title
                    Text(
                        text = "KEBIJAKAN PRIVASI (PRIVACY POLICY)\nCodaSuaka - Sistem Manajemen Bisnis All-in-One",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Secondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Scrollable text container taking remaining vertical space
                    val scrollState = rememberScrollState()
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(scrollState)
                        ) {
                            Text(
                                text = PRIVACY_POLICY_TEXT,
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Checkbox agreement
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = agreeChecked,
                            onCheckedChange = { agreeChecked = it },
                            colors = CheckboxDefaults.colors(checkedColor = Primary)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Saya telah membaca, memahami, dan menyetujui seluruh ketentuan dalam Kebijakan Privasi ini.",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurface,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Confirm Button
                    Button(
                        onClick = { viewModel.acceptPrivacyPolicy() },
                        enabled = agreeChecked,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary,
                            disabledContainerColor = Primary.copy(alpha = 0.4f)
                        )
                    ) {
                        Text(
                            text = "Setuju & Lanjutkan",
                            fontWeight = FontWeight.Bold,
                            color = OnPrimary
                        )
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Tertiary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Logo
            CodaSuakaLogo(iconSize = 80.dp)

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Selamat Datang",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Secondary
            )

            Text(
                text = "Masuk ke akun Anda",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceVariant
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Error Message (User-Friendly Notification)
            if (uiState.errorMessage != null) {
                NotificationBannerStatic(
                    message = uiState.errorMessage ?: "",
                    mapFromServer = true,
                    onDismiss = { viewModel.clearError() }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Email Input
            CustomTextField(
                value = uiState.email,
                onValueChange = { viewModel.onEmailChange(it) },
                label = "Email",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password Input
            CustomTextField(
                value = uiState.password,
                onValueChange = { viewModel.onPasswordChange(it) },
                label = "Password",
                visualTransformation = if (passwordVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (passwordVisible) "Sembunyikan password" else "Tampilkan password",
                            tint = OnSurfaceVariant
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Login Button
            Button(
                onClick = { viewModel.login() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !uiState.isLoading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                    contentColor = OnPrimary,
                    disabledContainerColor = Primary.copy(alpha = 0.4f)
                )
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = OnPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Masuk",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Register Link
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Belum punya akun? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant
                )
                Text(
                    text = "Daftar Sekarang",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Primary,
                    modifier = Modifier.clickable { onNavigateToRegister() }
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
