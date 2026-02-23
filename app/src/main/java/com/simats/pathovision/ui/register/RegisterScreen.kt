package com.simats.pathovision.ui.register

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
fun RegisterScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onRegistrationSuccess: (role: String) -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val registerState by viewModel.registerState.collectAsState()

    var selectedRole by remember { mutableStateOf("pathologist") }
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    // React to register state changes
    LaunchedEffect(registerState) {
        when (val state = registerState) {
            is Resource.Loading -> isLoading = true
            is Resource.Success -> {
                isLoading = false
                Toast.makeText(context, "Account created successfully!", Toast.LENGTH_SHORT).show()
                onRegistrationSuccess(selectedRole)
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
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 48.dp, bottom = 16.dp)
        ) {
            // Back Arrow
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = TextDark,
                modifier = Modifier
                    .size(28.dp)
                    .clickable { onNavigateBack() }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Title
            Text(
                text = "Create Account",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextDark
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Subtitle
            Text(
                text = "Join PathoVision to get started",
                fontSize = 15.sp,
                color = PrimaryBlue,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Role selector label
            Text(
                text = "I am a...",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextDark
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Role Selector Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf(
                    "pathologist" to "Pathologist",
                    "patient" to "Patient",
                    "student" to "Student"
                ).forEach { (roleKey, roleLabel) ->
                    RoleChip(
                        label = roleLabel,
                        isSelected = selectedRole == roleKey,
                        onClick = {
                            selectedRole = roleKey
                            // Update placeholder hint
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Full Name
            val namePlaceholder = if (selectedRole == "pathologist") "Dr. Full Name" else "Full Name"
            FormLabel("Full Name")
            Spacer(modifier = Modifier.height(6.dp))
            FormTextField(
                value = fullName,
                onValueChange = { fullName = it },
                placeholder = namePlaceholder
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Email Address
            FormLabel("Email Address")
            Spacer(modifier = Modifier.height(6.dp))
            FormTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = "name@example.com",
                keyboardType = KeyboardType.Email
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Password
            FormLabel("Password")
            Spacer(modifier = Modifier.height(6.dp))
            PasswordTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = "Create a password",
                isVisible = passwordVisible,
                onToggleVisibility = { passwordVisible = !passwordVisible }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Confirm Password
            FormLabel("Confirm Password")
            Spacer(modifier = Modifier.height(6.dp))
            PasswordTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                placeholder = "Confirm your password",
                isVisible = confirmPasswordVisible,
                onToggleVisibility = { confirmPasswordVisible = !confirmPasswordVisible }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Create Account Button
            Button(
                onClick = {
                    when {
                        fullName.isBlank() -> Toast.makeText(context, "Please enter your name", Toast.LENGTH_SHORT).show()
                        email.isBlank() -> Toast.makeText(context, "Please enter your email", Toast.LENGTH_SHORT).show()
                        password.length < 6 -> Toast.makeText(context, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                        password != confirmPassword -> Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                    else -> viewModel.register(fullName.trim(), email.trim(), password, selectedRole.uppercase())
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(14.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        text = "Create Account",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = White
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Already have an account
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(text = "Already have an account? ", fontSize = 14.sp, color = TextGray)
                Text(
                    text = "Sign In",
                    fontSize = 14.sp,
                    color = PrimaryBlue,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onNavigateToLogin() }
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun RoleChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    val bgColor = if (isSelected) PrimaryBlue else White
    val textColor = if (isSelected) White else TextDark
    val borderColor = if (isSelected) PrimaryBlue else Color(0xFFD1D5DB)

    Box(
        modifier = Modifier
            .border(1.dp, borderColor, RoundedCornerShape(50.dp))
            .background(bgColor, RoundedCornerShape(50.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, fontSize = 14.sp, color = textColor, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun FormLabel(text: String) {
    Text(
        text = text,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = TextDark
    )
}

@Composable
fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(text = placeholder, color = TextGray, fontSize = 14.sp)
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryBlue,
            unfocusedBorderColor = Color(0xFFE5E7EB),
            focusedContainerColor = White,
            unfocusedContainerColor = White,
        )
    )
}

@Composable
fun PasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isVisible: Boolean,
    onToggleVisibility: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(text = placeholder, color = TextGray, fontSize = 14.sp)
        },
        singleLine = true,
        visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            Text(
                text = if (isVisible) "Hide" else "Show",
                fontSize = 13.sp,
                color = TextGray,
                modifier = Modifier
                    .padding(end = 12.dp)
                    .clickable { onToggleVisibility() }
            )
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryBlue,
            unfocusedBorderColor = Color(0xFFE5E7EB),
            focusedContainerColor = White,
            unfocusedContainerColor = White,
        )
    )
}
