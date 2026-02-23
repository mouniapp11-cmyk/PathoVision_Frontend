package com.simats.pathovision.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.simats.pathovision.models.ProfileResponse
import com.simats.pathovision.utils.UrlUtils
import com.simats.pathovision.utils.Resource
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

private val PrimaryBlue = Color(0xFF4A6FE3)
private val BackgroundGray = Color(0xFFF4F5F7)
private val TextDark = Color(0xFF1A1F2E)
private val TextGray = Color(0xFF8E9BB5)
private val White = Color.White
private val LightBlue = Color(0xFFE8EDFF)
private val BorderGray = Color(0xFFE5E7EB)

@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit = {},
    onProfileUpdated: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val profileState by viewModel.profileState.collectAsState()
    val updateState by viewModel.updateState.collectAsState()

    var isLoading by remember { mutableStateOf(false) }
    var hasAttemptedSave by remember { mutableStateOf(false) }
    var fullName by remember { mutableStateOf("") }
    var licenseId by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var institution by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<android.net.Uri?>(null) }

    val context = LocalContext.current
    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        selectedImageUri = uri
    }

    LaunchedEffect(Unit) {
        viewModel.resetUpdateState()
        if (profileState == null) {
            viewModel.loadProfile()
        }
    }

    LaunchedEffect(profileState) {
        when (val state = profileState) {
            is Resource.Success -> {
                val profile = state.data
                fullName = profile.name
                licenseId = profile.license_id ?: ""
                phoneNumber = profile.phone_number ?: ""
                institution = profile.hospital_affiliation ?: ""
            }
            else -> {}
        }
    }

    LaunchedEffect(updateState) {
        when (val state = updateState) {
            is Resource.Loading -> {
                if (hasAttemptedSave) {
                    isLoading = true
                }
            }
            is Resource.Success -> {
                if (hasAttemptedSave) {
                    isLoading = false
                    hasAttemptedSave = false
                    viewModel.loadProfile()
                    viewModel.resetUpdateState()
                    onProfileUpdated()
                    onNavigateBack()
                }
            }
            is Resource.Error -> {
                if (hasAttemptedSave) {
                    isLoading = false
                    hasAttemptedSave = false
                }
                // Show error toast
            }
            null -> {}
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
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    Icons.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = TextDark,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Text(
                    text = "Edit Profile",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                if (isLoading) {
                    Text(
                        text = "Saving...",
                        fontSize = 12.sp,
                        color = PrimaryBlue,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            IconButton(
                onClick = {
                    hasAttemptedSave = true
                    val imagePart = selectedImageUri?.let { uri ->
                        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                            ?: return@let null
                        val mediaType = (context.contentResolver.getType(uri) ?: "image/*").toMediaType()
                        val requestBody = bytes.toRequestBody(mediaType)
                        val fileName = "profile_${System.currentTimeMillis()}.jpg"
                        MultipartBody.Part.createFormData("profile_picture", fileName, requestBody)
                    }
                    viewModel.updateProfile(
                        name = fullName.takeIf { it.isNotBlank() },
                        phoneNumber = phoneNumber,
                        hospitalAffiliation = institution,
                        licenseId = licenseId,
                        profileImage = imagePart
                    )
                },
                enabled = !isLoading
            ) {
                Icon(
                    Icons.Rounded.Check,
                    contentDescription = "Save",
                    tint = if (isLoading) TextGray else PrimaryBlue,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        if (when (profileState) {
                is Resource.Success -> false
                else -> true
            }
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryBlue)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                // Profile Photo Section
                val existingImageUrl = (profileState as? Resource.Success)
                    ?.data
                    ?.profile_picture
                    ?.let { UrlUtils.resolveMediaUrl(it) }
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(vertical = 16.dp)
                        .clickable { imagePicker.launch("image/*") }
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(LightBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedImageUri != null || existingImageUrl != null) {
                            AsyncImage(
                                model = selectedImageUri ?: existingImageUrl,
                                contentDescription = "Profile",
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                            )
                        } else {
                            Icon(
                                Icons.Rounded.Person,
                                contentDescription = "Profile",
                                tint = PrimaryBlue,
                                modifier = Modifier.size(60.dp)
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(PrimaryBlue)
                            .align(Alignment.BottomEnd),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.PhotoCamera,
                            contentDescription = "Change Photo",
                            tint = White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Text(
                    text = "Change Profile Photo",
                    fontSize = 13.sp,
                    color = PrimaryBlue,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 24.dp)
                        .clickable { imagePicker.launch("image/*") }
                )

                // Personal Details Section
                Text(
                    text = "PERSONAL DETAILS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextGray,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Full Name Field
                EditTextField(
                    label = "Full Name",
                    value = fullName,
                    onValueChange = { fullName = it },
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(12.dp))

                // License ID Field
                EditTextField(
                    label = "Medical License ID",
                    value = licenseId,
                    onValueChange = { licenseId = it },
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Phone Field
                EditTextField(
                    label = "Phone Number",
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Institution Field
                EditTextField(
                    label = "Institution",
                    value = institution,
                    onValueChange = { institution = it },
                    leadingIcon = Icons.Rounded.Apartment,
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Account Security Section
                Text(
                    text = "ACCOUNT SECURITY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextGray,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Email Field (Read-only)
                val currentProfile = profileState
                when (currentProfile) {
                    is Resource.Success -> {
                        EditTextField(
                            label = "Email Address",
                            value = currentProfile.data.email,
                            onValueChange = {},
                            enabled = false,
                            leadingIcon = Icons.Rounded.Lock,
                            isReadOnly = true
                        )
                    }
                    else -> {}
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun EditTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean = true,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    isReadOnly: Boolean = false
) {
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label, fontSize = 13.sp) },
            leadingIcon = if (leadingIcon != null) {
                {
                    Icon(
                        leadingIcon,
                        contentDescription = label,
                        tint = TextGray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else null,
            singleLine = true,
            enabled = enabled,
            readOnly = isReadOnly,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryBlue,
                unfocusedBorderColor = BorderGray,
                disabledBorderColor = BorderGray,
                focusedContainerColor = White,
                unfocusedContainerColor = White,
                disabledContainerColor = Color(0xFFFAFBFC),
                disabledTextColor = TextDark,
                disabledLabelColor = TextGray
            ),
            textStyle = androidx.compose.material3.LocalTextStyle.current.copy(fontSize = 15.sp)
        )
    }
}
