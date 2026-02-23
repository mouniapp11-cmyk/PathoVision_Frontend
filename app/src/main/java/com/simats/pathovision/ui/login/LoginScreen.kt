package com.simats.pathovision.ui.login

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.simats.pathovision.utils.Resource

val PrimaryBlue = Color(0xFF4A6FE3)
val BackgroundGray = Color(0xFFF4F5F7)
val TextDark = Color(0xFF1A1F2E)
val TextGray = Color(0xFF8E9BB5)
val White = Color.White

@Composable
fun LoginScreen(
    role: String,
    onNavigateBack: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: (role: String) -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val loginState by viewModel.loginState.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val roleSubtitle = when (role) {
        "pathologist" -> "Pathologist Access"
        "patient" -> "Patient Access"
        else -> "Medical Student Access"
    }

    val emailPlaceholder = when (role) {
        "pathologist" -> "name@hospital.org"
        "patient" -> "name@example.com"
        else -> "name@university.edu"
    }

    LaunchedEffect(loginState) {
        when (val state = loginState) {
            is Resource.Loading -> isLoading = true
            is Resource.Success -> {
                isLoading = false
                val userRole = state.data.user?.role ?: role
                Toast.makeText(context, "Welcome back!", Toast.LENGTH_SHORT).show()
                onLoginSuccess(userRole)
                viewModel.resetState()
            }
            is Resource.Error -> {
                isLoading = false
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            null -> isLoading = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .padding(horizontal = 24.dp)
            .padding(top = 48.dp, bottom = 16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Back Arrow
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = TextDark,
                modifier = Modifier
                    .size(28.dp)
                    .clickable { onNavigateBack() }
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Title
            Text(
                text = "Log In",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextDark
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Role-based subtitle
            Text(
                text = roleSubtitle,
                fontSize = 16.sp,
                color = PrimaryBlue,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Email
            Text("Email Address", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text(emailPlaceholder, color = TextGray, fontSize = 14.sp) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = Color(0xFFE5E7EB),
                    focusedContainerColor = White,
                    unfocusedContainerColor = White
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Password
            Text("Password", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("Enter your password", color = TextGray, fontSize = 14.sp) },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    Text(
                        text = if (passwordVisible) "Hide" else "Show",
                        fontSize = 13.sp,
                        color = TextGray,
                        modifier = Modifier.padding(end = 12.dp).clickable { passwordVisible = !passwordVisible }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = Color(0xFFE5E7EB),
                    focusedContainerColor = White,
                    unfocusedContainerColor = White
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Forgot Password
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Text(
                    text = "Forgot Password?",
                    fontSize = 13.sp,
                    color = PrimaryBlue,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { /* TODO */ }
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Login Button
            Button(
                onClick = {
                    when {
                        email.isBlank() -> Toast.makeText(context, "Please enter your email", Toast.LENGTH_SHORT).show()
                        password.isBlank() -> Toast.makeText(context, "Please enter your password", Toast.LENGTH_SHORT).show()
                        else -> viewModel.login(email.trim(), password)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(14.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = White, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                } else {
                    Text("Log In", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = White)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Don't have an account
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Text("Don't have an account? ", fontSize = 14.sp, color = TextGray)
                Text(
                    text = "Create Account",
                    fontSize = 14.sp,
                    color = PrimaryBlue,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onNavigateToRegister() }
                )
            }
        }
    }
}
