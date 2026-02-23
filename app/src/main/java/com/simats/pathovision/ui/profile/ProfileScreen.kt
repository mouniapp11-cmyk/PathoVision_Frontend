package com.simats.pathovision.ui.profile

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
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.HelpOutline
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.simats.pathovision.models.ProfileResponse
import com.simats.pathovision.utils.UrlUtils

private val PrimaryBlue = Color(0xFF4A6FE3)
private val BackgroundGray = Color(0xFFF4F5F7)
private val TextDark = Color(0xFF1A1F2E)
private val TextGray = Color(0xFF8E9BB5)
private val White = Color.White
private val LightBlue = Color(0xFFE8EDFF)

@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToEditProfile: () -> Unit = {},
    onLogout: () -> Unit = {},
    profileData: ProfileResponse? = null
) {
    // Accept profile data from parent instead of loading separately
    // This prevents duplicate API calls and recomposition loops

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        // Header with back button and edit button
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
            Text(
                text = "Profile",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                modifier = Modifier.weight(1f).wrapContentWidth()
            )
            IconButton(onClick = onNavigateToEditProfile) {
                Icon(
                    Icons.Rounded.Edit,
                    contentDescription = "Edit",
                    tint = PrimaryBlue,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Content - directly display profile data passed from parent
        if (profileData != null) {
            ProfileContent(profile = profileData, onLogout = onLogout)
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryBlue)
            }
        }
    }
}

@Composable
fun ProfileContent(
    profile: ProfileResponse,
    onLogout: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Profile Picture
        val imageUrl = UrlUtils.resolveMediaUrl(profile.profile_picture)
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(LightBlue),
            contentAlignment = Alignment.Center
        ) {
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
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

        Spacer(modifier = Modifier.height(20.dp))

        // Name
        Text(
            text = profile.name,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1F2E)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Role Badge
        Surface(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp)),
            color = LightBlue
        ) {
            Text(
                text = profile.role,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryBlue,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Account Info Card
        AccountInfoCard(profile)

        Spacer(modifier = Modifier.height(16.dp))

        // Settings Options
        SettingsOptionsCard(onLogout = onLogout)

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun AccountInfoCard(profile: ProfileResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Account Info",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Full Name
            InfoField(label = "FULL NAME", value = profile.name)

            Spacer(modifier = Modifier.height(16.dp))

            // Phone Number
            InfoField(
                label = "PHONE NUMBER",
                value = profile.phone_number ?: "Not provided"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Email (Security)
            InfoField(label = "SECURITY", value = profile.email)
        }
    }
}

@Composable
fun InfoField(label: String, value: String) {
    Column {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextGray,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = TextDark
        )
    }
}

@Composable
fun SettingsOptionsCard(onLogout: () -> Unit = {}) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            SettingsOptionItem(
                icon = Icons.Rounded.Shield,
                label = "Privacy & Security",
                onClick = { /* TODO: Navigate to Privacy & Security */ }
            )
            
            Divider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = Color(0xFFE5E7EB),
                thickness = 0.5.dp
            )
            
            SettingsOptionItem(
                icon = Icons.Rounded.HelpOutline,
                label = "Help & Support",
                onClick = { /* TODO: Navigate to Help & Support */ }
            )
            
            Divider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = Color(0xFFE5E7EB),
                thickness = 0.5.dp
            )
            
            SettingsOptionItem(
                icon = Icons.Rounded.Logout,
                label = "Sign Out",
                iconTint = Color(0xFFEF4444),
                textColor = Color(0xFFEF4444),
                onClick = onLogout
            )
        }
    }
}

@Composable
fun SettingsOptionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    iconTint: Color = TextGray,
    textColor: Color = TextDark,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = label,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
        }
        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = "Navigate",
            tint = TextGray,
            modifier = Modifier.size(20.dp)
        )
    }
}
