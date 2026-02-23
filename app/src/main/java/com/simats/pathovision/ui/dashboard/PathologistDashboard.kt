package com.simats.pathovision.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.simats.pathovision.utils.UrlUtils

val PrimaryBlue = Color(0xFF4A6FE3)
val BackgroundGray = Color(0xFFF4F5F7)
val TextDark = Color(0xFF1A1F2E)
val TextGray = Color(0xFF8E9BB5)
val White = Color.White
val CardWhite = Color.White

@Composable
fun PathologistDashboard(
    onNavigateToCases: () -> Unit = {},
    onNavigateToAnalysis: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    // Kept for NavGraph backwards compat — wraps DashboardContent in its own Scaffold
    var selectedTab by remember { mutableStateOf(0) }
    Scaffold(
        containerColor = BackgroundGray,
        bottomBar = {
            DashboardBottomBar(
                selectedTab = selectedTab,
                onTabSelected = { tab ->
                    selectedTab = tab
                    when (tab) {
                        1 -> onNavigateToCases()
                        3 -> onNavigateToProfile()
                        else -> {}
                    }
                },
                onCenterClick = onNavigateToAnalysis
            )
        }
    ) { paddingValues ->
        DashboardContent(
            modifier = Modifier.padding(paddingValues),
            onNavigateToCases = onNavigateToCases
        )
    }
}

/**
 * The scrollable content of the Pathologist Dashboard.
 * Used by both PathologistDashboard (standalone) and MainScreen (shared bottom nav).
 */
@Composable
fun DashboardContent(
    modifier: Modifier = Modifier,
    userName: String = "Bezawada",
    profilePicture: String? = null,
    onNavigateToProfile: () -> Unit = {},
    onNavigateToCases: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        DashboardHeader(
            userName = userName, 
            profilePicture = profilePicture,
            onProfileClick = onNavigateToProfile
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Stats Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                icon = Icons.Rounded.CheckCircle,
                iconBg = Color(0xFFE8F8EE),
                iconTint = Color(0xFF2EB872),
                percentage = "0%",
                value = "0",
                label = "ANALYSED",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                icon = Icons.Rounded.Bolt,
                iconBg = Color(0xFFFFF8E1),
                iconTint = Color(0xFFFFA726),
                percentage = "0%",
                value = "0.0%",
                label = "AVG. CONF",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // High Priority Cases
        SectionHeader(
            title = "High Priority Cases",
            actionText = "View All",
            onAction = onNavigateToCases
        )

        Spacer(modifier = Modifier.height(12.dp))

        EmptyStateCard(
            icon = Icons.Rounded.Inbox,
            title = "No urgent cases",
            subtitle = "Cases flagged as urgent will appear here"
        )

        Spacer(modifier = Modifier.height(28.dp))

        // AI Processing Queue
        SectionHeader(
            title = "AI Processing Queue",
            actionText = "Refresh",
            onAction = {}
        )

        Spacer(modifier = Modifier.height(12.dp))

        EmptyStateCard(
            icon = Icons.Rounded.AutoAwesome,
            title = "Queue is empty",
            subtitle = "Cases submitted for AI analysis will appear here"
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun DashboardHeader(
    userName: String, 
    profilePicture: String? = null,
    onProfileClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 48.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val firstName = userName.trim().substringBefore(" ").ifBlank { userName }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp)
        ) {
            Text(
                text = "Hello, $firstName",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "You have 0 cases to review today.",
                fontSize = 14.sp,
                color = TextGray
            )
        }

        // Avatar with profile picture
        val imageUrl = UrlUtils.resolveMediaUrl(profilePicture)
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(PrimaryBlue)
                .clickable(onClick = onProfileClick),
            contentAlignment = Alignment.Center
        ) {
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.Person,
                    contentDescription = "Profile",
                    tint = White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
fun StatCard(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    percentage: String,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(iconBg, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
                }
                Text(
                    text = "$percentage ↗",
                    fontSize = 12.sp,
                    color = Color(0xFF2EB872),
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = value, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = TextDark)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                color = TextGray,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.8.sp
            )
        }
    }
}

@Composable
fun SectionHeader(title: String, actionText: String, onAction: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = TextDark)
        Text(
            text = actionText,
            fontSize = 14.sp,
            color = PrimaryBlue,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable(onClick = onAction)
        )
    }
}

@Composable
fun EmptyStateCard(icon: ImageVector, title: String, subtitle: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(Color(0xFFE8EDFF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(30.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = TextGray
            )
        }
    }
}

data class BottomNavItem(val label: String, val icon: ImageVector, val index: Int)

val bottomNavItems = listOf(
    BottomNavItem("Dashboard", Icons.Rounded.GridView, 0),
    BottomNavItem("Cases", Icons.Rounded.Folder, 1),
    BottomNavItem("Analysis", Icons.Rounded.BarChart, 2),
    BottomNavItem("Settings", Icons.Rounded.Settings, 3)
)

@Composable
fun DashboardBottomBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onCenterClick: () -> Unit
) {
    // Geometry:
    // FAB diameter = 56dp, protrusion above bar = 28dp (half the diameter)
    // White bar height = 68dp (enough for icon + label)
    // Total Box height = 68 + 28 = 96dp
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
    ) {
        // White bar — 68dp tall, pinned to bottom
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .align(Alignment.BottomCenter),
            color = White,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),  // NO navigationBarsPadding here
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavBarItem(
                    icon = Icons.Rounded.GridView,
                    label = "Dashboard",
                    selected = selectedTab == 0,
                    onClick = { onTabSelected(0) },
                    modifier = Modifier.weight(1f)
                )
                NavBarItem(
                    icon = Icons.Rounded.Folder,
                    label = "Cases",
                    selected = selectedTab == 1,
                    onClick = { onTabSelected(1) },
                    modifier = Modifier.weight(1f)
                )
                // Center gap for the FAB
                Spacer(modifier = Modifier.weight(1f))
                NavBarItem(
                    icon = Icons.Rounded.BarChart,
                    label = "Analysis",
                    selected = selectedTab == 2,
                    onClick = { onTabSelected(2) },
                    modifier = Modifier.weight(1f)
                )
                NavBarItem(
                    icon = Icons.Rounded.Settings,
                    label = "Settings",
                    selected = selectedTab == 3,
                    onClick = { onTabSelected(3) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // FAB at TopCenter: 56dp circle at y=0..56dp, bar starts at y=28dp
        // → FAB protrudes 28dp above bar, 28dp inside bar ✓
        Box(
            modifier = Modifier
                .size(56.dp)
                .align(Alignment.TopCenter)
                .background(PrimaryBlue, CircleShape)
                .clickable(onClick = onCenterClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Science,
                contentDescription = "Scan",
                tint = White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun NavBarItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) PrimaryBlue else TextGray,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = if (selected) PrimaryBlue else TextGray,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1
        )
    }
}
