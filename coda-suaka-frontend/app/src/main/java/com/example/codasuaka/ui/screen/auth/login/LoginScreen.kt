package com.example.codasuaka.ui.screen.auth.login

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.codasuaka.R
import com.example.codasuaka.ui.theme.PoppinsFontFamily

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            
            Image(
                painter = painterResource(id = R.drawable.ic_logo_no_text),
                contentDescription = "Logo",
                modifier = Modifier.size(120.dp) // Adjusted size
            )
            
            Spacer(modifier = Modifier.height(60.dp)) // Increased spacing
            
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Ownername",
                    fontFamily = PoppinsFontFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = viewModel.ownername,
                    onValueChange = { viewModel.onOwnernameChange(it) },
                    placeholder = { Text("Username", fontFamily = PoppinsFontFamily, color = Color.LightGray) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.LightGray,
                        unfocusedBorderColor = Color.LightGray
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Password",
                    fontFamily = PoppinsFontFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = viewModel.password,
                    onValueChange = { viewModel.onPasswordChange(it) },
                    placeholder = { Text("Password", fontFamily = PoppinsFontFamily, color = Color.LightGray) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    shape = MaterialTheme.shapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.LightGray,
                        unfocusedBorderColor = Color.LightGray
                    )
                )
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = viewModel.rememberMe,
                    onCheckedChange = { viewModel.onRememberMeChange(it) }
                )
                Text(
                    text = "Ingat Saya",
                    fontFamily = PoppinsFontFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            OutlinedButton(
                onClick = { viewModel.onLoginClick() },
                modifier = Modifier
                    .width(220.dp) // Adjusted width
                    .height(55.dp),
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(1.dp, Color.LightGray),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                )
            ) {
                Text(text = "Login", fontFamily = PoppinsFontFamily, fontSize = 18.sp)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onNavigateToRegister,
                modifier = Modifier
                    .width(220.dp) // Adjusted width
                    .height(55.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF5AB2FF)
                )
            ) {
                Text(text = "Buat Account", fontFamily = PoppinsFontFamily, fontSize = 18.sp)
            }
            
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
