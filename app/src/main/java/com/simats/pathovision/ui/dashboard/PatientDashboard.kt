package com.simats.pathovision.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.simats.pathovision.models.CaseItem
import com.simats.pathovision.utils.Resource
import com.simats.pathovision.utils.UrlUtils

private val PatientPrimaryBlue = Color(0xFF4A6FE3)
private val PatientBackgroundGray = Color(0xFFF4F6F9)
private val PatientTextDark = Color(0xFF1A1F2E)
private val PatientTextGray = Color(0xFF8C97B2)
private val PatientCardWhite = Color.White
private val PatientBadgeGreen = Color(0xFF2EB872)
private val PatientBadgeRed = Color(0xFFEF4444)

@Composable
fun PatientDashboard(
    userName: String = "John Doe",
    profilePicture: String? = null,
    onNavigateToProfile: () -> Unit = {},
    onChatWithDoctor: () -> Unit = {},
    viewModel: PatientDashboardViewModel = hiltViewModel()
) {
    // Simplified - just show the patient dashboard content without bottom nav
    PatientDashboardContent(
        modifier = Modifier.fillMaxSize(),
        userName = userName,
        profilePicture = profilePicture,
        onNavigateToProfile = onNavigateToProfile,
        onNavigateToCases = {},
        onChatWithDoctor = onChatWithDoctor,
        viewModel = viewModel
    )
}

@Composable
fun PatientDashboardContent(
    modifier: Modifier = Modifier,
    userName: String = "John Doe",
    profilePicture: String? = null,
    onNavigateToProfile: () -> Unit = {},
    onNavigateToCases: () -> Unit = {},  // Kept for now but not used
    onChatWithDoctor: () -> Unit = {},
    viewModel: PatientDashboardViewModel = hiltViewModel()
) {
    val displayName = userName
    val displayProfilePicture = profilePicture
    val casesState by viewModel.casesState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        PatientDashboardHeader(
            userName = displayName,
            profilePicture = displayProfilePicture,
            onProfileClick = onNavigateToProfile
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Latest Analysis Card
        when (val state = casesState) {
            is Resource.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PatientPrimaryBlue)
                }
            }
            is Resource.Success -> {
                val cases = state.data
                if (cases.isNotEmpty()) {
                    LatestAnalysisCard(latestCase = cases.first())
                } else {
                    NoAnalysisCard()
                }
            }
            is Resource.Error -> {
                ErrorCard(message = state.message)
            }
            null -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PatientPrimaryBlue)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Quick Actions
        QuickActionsRow(onChatWithDoctor = onChatWithDoctor)

        Spacer(modifier = Modifier.height(28.dp))

        // Report History Section
        SectionHeader(
            title = "Report History",
            actionText = "View All",
            onAction = {}
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Report History Items from real data
        when (val state = casesState) {
            is Resource.Success -> {
                val cases = state.data
                if (cases.isNotEmpty()) {
                    cases.take(3).forEach { caseItem ->
                        ReportHistoryItem(
                            title = caseItem.title,
                            subtitle = "Analysis",
                            date = formatDate(caseItem.created_at),
                            status = "Reviewed"
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No reports yet",
                            fontSize = 14.sp,
                            color = PatientTextGray
                        )
                    }
                }
            }
            else -> {
                // Show placeholders or loading
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun PatientDashboardHeader(
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
                text = "Welcome back,",
                fontSize = 14.sp,
                color = PatientTextGray,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = firstName,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = PatientTextDark
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Your health reports are up to date",
                fontSize = 12.sp,
                color = PatientTextGray
            )
        }

        // Avatar with profile picture
        val imageUrl = UrlUtils.resolveMediaUrl(profilePicture)
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(PatientPrimaryBlue)
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
                    tint = PatientCardWhite,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
fun LatestAnalysisCard(latestCase: CaseItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PatientCardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "LATEST ANALYSIS",
                fontSize = 10.sp,
                color = PatientTextGray,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = latestCase.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PatientTextDark
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val predictionColor = when (latestCase.ai_prediction) {
                            "Malignant" -> PatientBadgeRed
                            "Benign" -> PatientBadgeGreen
                            else -> PatientTextGray
                        }
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .background(predictionColor, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = latestCase.ai_prediction ?: "Analyzing",
                            fontSize = 12.sp,
                            color = predictionColor,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Icon(
                            imageVector = Icons.Rounded.CalendarToday,
                            contentDescription = "Calendar",
                            tint = PatientTextGray,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formatDate(latestCase.created_at),
                            fontSize = 12.sp,
                            color = PatientTextGray
                        )
                    }
                }
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Rounded.DocumentScanner,
                        contentDescription = "Document",
                        tint = PatientTextGray,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = PatientPrimaryBlue),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("View Full Report", color = PatientCardWhite, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun QuickActionsRow(
    onChatWithDoctor: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickActionButton(
            icon = Icons.Rounded.Chat,
            label = "Chat with Doctor",
            backgroundColor = Color(0xFFE3F2FD),
            iconTint = PatientPrimaryBlue,
            modifier = Modifier.weight(1f),
            onClick = onChatWithDoctor
        )
        QuickActionButton(
            icon = Icons.Rounded.Folder,
            label = "All Reports",
            backgroundColor = Color(0xFFF3E5F5),
            iconTint = Color(0xFF9C27B0),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    backgroundColor: Color,
    iconTint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = PatientTextDark,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun ReportHistoryItem(
    title: String,
    subtitle: String,
    date: String,
    status: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PatientCardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color(0xFFE8EDFF), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.DocumentScanner,
                        contentDescription = "Report",
                        tint = PatientPrimaryBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PatientTextDark
                    )
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = PatientTextGray
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = date,
                    fontSize = 12.sp,
                    color = PatientTextGray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = Color(0xFFE8F8EE),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = status,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PatientBadgeGreen,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PatientDashboardBottomBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onCenterClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .align(Alignment.BottomCenter),
            color = PatientCardWhite,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
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
                    label = "Reports",
                    selected = selectedTab == 1,
                    onClick = { onTabSelected(1) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.weight(1f))
                NavBarItem(
                    icon = Icons.Rounded.BarChart,
                    label = "Tracking",
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

        Box(
            modifier = Modifier
                .size(56.dp)
                .align(Alignment.TopCenter)
                .background(PatientPrimaryBlue, CircleShape)
                .clickable(onClick = onCenterClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.LocalPharmacy,
                contentDescription = "Lab",
                tint = PatientCardWhite,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun NoAnalysisCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PatientCardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Rounded.Folder,
                contentDescription = "No Cases",
                tint = PatientTextGray,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No Analysis Yet",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = PatientTextDark
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Your analysis reports will appear here",
                fontSize = 12.sp,
                color = PatientTextGray
            )
        }
    }
}

@Composable
fun ErrorCard(message: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PatientCardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Rounded.ErrorOutline,
                contentDescription = "Error",
                tint = PatientBadgeRed,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Failed to load data",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = PatientTextDark
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = message,
                fontSize = 12.sp,
                color = PatientTextGray
            )
        }
    }
}

// Helper function to format date
fun formatDate(dateString: String?): String {
    if (dateString == null) return "N/A"
    
    return try {
        // Parse ISO date string and format to "MMM dd, yyyy"
        val instant = java.time.Instant.parse(dateString)
        val zoneId = java.time.ZoneId.systemDefault()
        val dateTime = java.time.LocalDateTime.ofInstant(instant, zoneId)
        val formatter = java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy")
        dateTime.format(formatter)
    } catch (e: Exception) {
        // Fallback for invalid format
        dateString.substringBefore("T")
    }
}
