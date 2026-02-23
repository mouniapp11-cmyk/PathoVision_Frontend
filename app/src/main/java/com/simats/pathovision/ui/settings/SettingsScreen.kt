package com.simats.pathovision.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.simats.pathovision.ui.profile.ProfileViewModel
import com.simats.pathovision.utils.TokenManager
import com.simats.pathovision.utils.UrlUtils
import com.simats.pathovision.utils.Resource
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.launch

@EntryPoint
@InstallIn(SingletonComponent::class)
interface SettingsScreenEntryPoint {
    fun tokenManager(): TokenManager
}

private val BackgroundGray = Color(0xFFF4F6F9)
private val TextDark = Color(0xFF1A1F2E)
private val TextGray = Color(0xFF8C97B2)
private val CardWhite = Color.White
private val AccentBlue = Color(0xFF4A6FE3)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit = {},
    onEditProfile: () -> Unit = {},
    onLogout: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel(),
    changePasswordViewModel: ChangePasswordViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val entryPoint = EntryPointAccessors.fromApplication(
        context.applicationContext,
        SettingsScreenEntryPoint::class.java
    )
    val tokenManager = entryPoint.tokenManager()
    
    val profileState by viewModel.profileState.collectAsState()
    val isDarkModeEnabled = remember { mutableStateOf(false) }
    val isNotificationsEnabled = remember { mutableStateOf(true) }
    var showChangePasswordSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    suspend fun handleLogout() {
        tokenManager.clearAll()
        onLogout()
    }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = "Profile & Settings",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        Icons.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = TextDark
                    )
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE9F0FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        val imageUrl = (profileState as? Resource.Success)
                            ?.data
                            ?.profile_picture
                            ?.let { UrlUtils.resolveMediaUrl(it) }

                        if (imageUrl != null) {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = "Profile",
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                            )
                        } else {
                            Text(
                                text = "PV",
                                fontSize = 14.sp,
                                color = AccentBlue,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    when (val state = profileState) {
                        is Resource.Success -> {
                            val profile = state.data
                            Text(
                                text = profile.name,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0xFFE9EDFF)
                            ) {
                                Text(
                                    text = profile.role,
                                    fontSize = 12.sp,
                                    color = AccentBlue,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = profile.email,
                                fontSize = 12.sp,
                                color = TextGray
                            )
                        }
                        is Resource.Loading -> {
                            Text(text = "Loading...", color = TextGray, fontSize = 12.sp)
                        }
                        is Resource.Error -> {
                            Text(text = "Unable to load profile", color = TextGray, fontSize = 12.sp)
                        }
                        null -> {
                            Text(text = "Loading...", color = TextGray, fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "PREFERENCES",
                fontSize = 12.sp,
                color = TextGray,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(10.dp))

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    SettingsSwitchRow(
                        icon = Icons.Rounded.Star,
                        title = "Dark Mode",
                        isChecked = isDarkModeEnabled.value,
                        onCheckedChange = { isDarkModeEnabled.value = it }
                    )
                    DividerLine()
                    SettingsSwitchRow(
                        icon = Icons.Rounded.Notifications,
                        title = "Notifications",
                        isChecked = isNotificationsEnabled.value,
                        onCheckedChange = { isNotificationsEnabled.value = it }
                    )
                    DividerLine()
                    SettingsNavigationRow(
                        icon = Icons.Rounded.Lock,
                        title = "Change Password",
                        onClick = { showChangePasswordSheet = true }
                    )
                    DividerLine()
                    SettingsNavigationRow(
                        icon = Icons.Rounded.Info,
                        title = "Help & Support",
                        onClick = { }
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    SettingsNavigationRow(
                        icon = Icons.Rounded.Logout,
                        title = "Log Out",
                        onClick = {
                            scope.launch {
                                handleLogout()
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showChangePasswordSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showChangePasswordSheet = false
                changePasswordViewModel.clearState()
            },
            sheetState = sheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            ChangePasswordBottomSheet(
                onDismiss = {
                    scope.launch {
                        sheetState.hide()
                        showChangePasswordSheet = false
                        changePasswordViewModel.clearState()
                    }
                },
                viewModel = changePasswordViewModel
            )
        }
    }
}

@Composable
private fun DividerLine() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color(0xFFE8ECF4))
    )
}

@Composable
private fun SettingsSwitchRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = title, tint = TextDark)
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = TextDark,
            modifier = Modifier.weight(1f)
        )
        Switch(checked = isChecked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SettingsNavigationRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = title, tint = TextDark)
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = TextDark,
            modifier = Modifier.weight(1f)
        )
        Icon(Icons.Rounded.ChevronRight, contentDescription = "Open", tint = TextGray)
    }
}

@Composable
private fun ChangePasswordBottomSheet(
    onDismiss: () -> Unit,
    viewModel: ChangePasswordViewModel
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
        if (changePasswordState is Resource.Success) {
            onDismiss()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 40.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Change Password",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Close",
                    tint = TextGray
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "CURRENT PASSWORD",
            fontSize = 11.sp,
            color = TextGray,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = currentPassword,
            onValueChange = { currentPassword = it; errorMessage = null },
            placeholder = { Text("Enter current password", color = Color(0xFFB0B7C3)) },
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
                unfocusedContainerColor = Color(0xFFF7F8F9),
                focusedContainerColor = Color(0xFFF7F8F9)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "NEW PASSWORD",
            fontSize = 11.sp,
            color = TextGray,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = newPassword,
            onValueChange = { newPassword = it; errorMessage = null },
            placeholder = { Text("Minimum 8 characters", color = Color(0xFFB0B7C3)) },
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
                unfocusedContainerColor = Color(0xFFF7F8F9),
                focusedContainerColor = Color(0xFFF7F8F9)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "CONFIRM PASSWORD",
            fontSize = 11.sp,
            color = TextGray,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it; errorMessage = null },
            placeholder = { Text("Re-enter new password", color = Color(0xFFB0B7C3)) },
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
                unfocusedContainerColor = Color(0xFFF7F8F9),
                focusedContainerColor = Color(0xFFF7F8F9)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage!!,
                color = Color(0xFFD32F2F),
                fontSize = 12.sp
            )
        }

        when (val state = changePasswordState) {
            is Resource.Error -> {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = state.message,
                    color = Color(0xFFD32F2F),
                    fontSize = 12.sp
                )
            }
            is Resource.Loading -> {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Updating password...",
                    color = TextGray,
                    fontSize = 12.sp
                )
            }
            else -> {}
        }

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = {
                when {
                    currentPassword.isBlank() -> {
                        errorMessage = "Please enter your current password"
                    }
                    newPassword.length < 8 -> {
                        errorMessage = "New password must be at least 8 characters"
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
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentBlue
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Update Password",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
