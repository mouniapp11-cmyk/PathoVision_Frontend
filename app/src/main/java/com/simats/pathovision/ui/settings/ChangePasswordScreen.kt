package com.simats.pathovision.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.simats.pathovision.utils.Resource

private val BackgroundGray = Color(0xFFF4F6F9)
private val TextDark = Color(0xFF1A1F2E)
private val TextGray = Color(0xFF8C97B2)
private val TextLight = Color(0xFFB0B8C8)
private val CardWhite = Color.White
private val AccentBlue = Color(0xFF4A6FE3)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: ChangePasswordViewModel = hiltViewModel()
) {
    val changePasswordState by viewModel.changePasswordState.collectAsState()
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var currentVisible by remember { mutableStateOf(false) }
    var newVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(changePasswordState) {
        when (val state = changePasswordState) {
            is Resource.Success -> {
                onNavigateBack()
            }
            is Resource.Error -> {
                errorMessage = state.message
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Change Password",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            IconButton(onClick = onNavigateBack) {
                Icon(
                    Icons.Rounded.Close,
                    contentDescription = "Close",
                    tint = TextDark
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            // Current Password
            Text(
                text = "CURRENT PASSWORD",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextGray,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = currentPassword,
                onValueChange = { 
                    currentPassword = it
                    errorMessage = null
                },
                placeholder = { Text("Enter current password", color = TextLight) },
                visualTransformation = if (currentVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { currentVisible = !currentVisible }) {
                        Icon(
                            imageVector = if (currentVisible) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                            contentDescription = "Toggle visibility",
                            tint = TextGray
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color(0xFFE8ECF4),
                    focusedBorderColor = AccentBlue,
                    unfocusedContainerColor = CardWhite,
                    focusedContainerColor = CardWhite
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            // New Password
            Text(
                text = "NEW PASSWORD",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextGray,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = newPassword,
                onValueChange = { 
                    newPassword = it
                    errorMessage = null
                },
                placeholder = { Text("Minimum 8 characters", color = TextLight) },
                visualTransformation = if (newVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { newVisible = !newVisible }) {
                        Icon(
                            imageVector = if (newVisible) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                            contentDescription = "Toggle visibility",
                            tint = TextGray
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color(0xFFE8ECF4),
                    focusedBorderColor = AccentBlue,
                    unfocusedContainerColor = CardWhite,
                    focusedContainerColor = CardWhite
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Confirm Password
            Text(
                text = "CONFIRM PASSWORD",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextGray,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { 
                    confirmPassword = it
                    errorMessage = null
                },
                placeholder = { Text("Re-enter new password", color = TextLight) },
                visualTransformation = if (confirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { confirmVisible = !confirmVisible }) {
                        Icon(
                            imageVector = if (confirmVisible) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                            contentDescription = "Toggle visibility",
                            tint = TextGray
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color(0xFFE8ECF4),
                    focusedBorderColor = AccentBlue,
                    unfocusedContainerColor = CardWhite,
                    focusedContainerColor = CardWhite
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Error message
            errorMessage?.let {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = it,
                    color = Color(0xFFD32F2F),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            // Loading state
            if (changePasswordState is Resource.Loading) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Updating password...",
                    color = TextGray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Update Button
            Button(
                onClick = {
                    when {
                        currentPassword.isBlank() -> {
                            errorMessage = "Please enter your current password"
                        }
                        newPassword.isBlank() -> {
                            errorMessage = "Please enter a new password"
                        }
                        newPassword.length < 8 -> {
                            errorMessage = "Password must be at least 8 characters"
                        }
                        newPassword != confirmPassword -> {
                            errorMessage = "Passwords do not match"
                        }
                        else -> {
                            errorMessage = null
                            viewModel.changePassword(currentPassword, newPassword)
                        }
                    }
                },
                enabled = changePasswordState !is Resource.Loading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentBlue,
                    disabledContainerColor = AccentBlue.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = "Update Password",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}
