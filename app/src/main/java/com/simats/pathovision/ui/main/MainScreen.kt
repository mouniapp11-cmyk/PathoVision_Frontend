package com.simats.pathovision.ui.main

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import com.simats.pathovision.ui.cases.CasesScreen
import com.simats.pathovision.ui.analysis.AnalysisScreen
import com.simats.pathovision.ui.analysis.AnalysisListScreen
import com.simats.pathovision.ui.analysis.CaseAnalysisScreen
import com.simats.pathovision.ui.cases.NewCaseScreen
import com.simats.pathovision.ui.dashboard.BackgroundGray
import com.simats.pathovision.ui.dashboard.DashboardBottomBar
import com.simats.pathovision.ui.dashboard.DashboardContent
import com.simats.pathovision.ui.dashboard.PatientDashboard
import com.simats.pathovision.ui.profile.EditProfileScreen
import com.simats.pathovision.ui.profile.ProfileScreen
import com.simats.pathovision.ui.profile.ProfileViewModel
import com.simats.pathovision.ui.settings.SettingsScreen
import com.simats.pathovision.ui.chat.MessagesScreen
import com.simats.pathovision.ui.chat.ChatDetailScreen
import com.simats.pathovision.ui.chat.ChatWithDoctorScreen
import androidx.compose.material3.Surface
import androidx.hilt.navigation.compose.hiltViewModel
import com.simats.pathovision.utils.Resource

/**
 * MainScreen hosts the shared bottom navigation bar and swaps content between tabs.
 * This ensures the bottom nav is always visible when on Dashboard, Cases, Analysis or Profile.
 */
@Composable
fun MainScreen(onLogout: () -> Unit = {}) {
    var selectedTab by remember { mutableStateOf(0) }
    var isEditingProfile by remember { mutableStateOf(false) }
    var isCreatingCase by remember { mutableStateOf(false) }
    var isViewingProfile by remember { mutableStateOf(false) }
    var isViewingMessages by remember { mutableStateOf(false) }
    var isViewingDoctorChat by remember { mutableStateOf(false) }
    var isAnalyzingCase by remember { mutableStateOf(false) }
    var analyzingCaseId by remember { mutableStateOf("") }
    var analyzingCaseTitle by remember { mutableStateOf("") }
    var selectedChatCaseId by remember { mutableStateOf("") }
    var selectedChatReceiverId by remember { mutableStateOf("") }
    var selectedChatReceiverName by remember { mutableStateOf("") }

    val profileViewModel: ProfileViewModel = hiltViewModel()
    val profileState by profileViewModel.profileState.collectAsState()

    LaunchedEffect(Unit) {
        profileViewModel.loadProfile()
    }

    val dashboardName = when (val state = profileState) {
        is Resource.Success -> state.data.name
        else -> "Bezawada"
    }

    val userRole = when (val state = profileState) {
        is Resource.Success -> state.data.role
        else -> null  // Don't default, show loading instead
    }

    val profilePicture = when (val state = profileState) {
        is Resource.Success -> state.data.profile_picture
        else -> null
    }

        Scaffold(
        containerColor = BackgroundGray,
        bottomBar = {
            if (!isViewingMessages && userRole != null) {
                when (userRole) {
                    "PATHOLOGIST" -> {
                        DashboardBottomBar(
                            selectedTab = selectedTab,
                            onTabSelected = { selectedTab = it },
                            onCenterClick = { selectedTab = 4 }
                        )
                    }
                    "PATIENT", "STUDENT" -> {
                        // Patient/Student use PatientDashboard bottom bar
                    }
                    else -> {
                        DashboardBottomBar(
                            selectedTab = selectedTab,
                            onTabSelected = { selectedTab = it },
                            onCenterClick = { selectedTab = 4 }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (!isViewingMessages && !isEditingProfile && !isCreatingCase && !isViewingProfile && !isAnalyzingCase && selectedTab == 0 && userRole == "PATHOLOGIST") {
                FloatingActionButton(
                    onClick = { isViewingMessages = true },
                    modifier = Modifier.offset(x = (-16).dp, y = (-50).dp),
                    containerColor = Color(0xFF4A6FE3),
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Chat,
                        contentDescription = "Messages",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
        ) {
            when (profileState) {
                null, is Resource.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF4A6FE3))
                    }
                }
                is Resource.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Failed to load profile. Please try again.")
                    }
                }
                is Resource.Success -> when {
                isAnalyzingCase -> {
                    CaseAnalysisScreen(
                        caseId = analyzingCaseId,
                        caseTitle = analyzingCaseTitle,
                        slideImageUrl = null,
                        isAnalyzing = true,
                        analysisComplete = false,
                        onNavigateBack = {
                            isAnalyzingCase = false
                            analyzingCaseId = ""
                            analyzingCaseTitle = ""
                        }
                    )
                }
                isViewingMessages && selectedChatCaseId.isNotEmpty() -> {
                    ChatDetailScreen(
                        caseId = selectedChatCaseId,
                        receiverId = selectedChatReceiverId,
                        receiverName = selectedChatReceiverName,
                        onNavigateBack = {
                            selectedChatCaseId = ""
                            selectedChatReceiverId = ""
                            selectedChatReceiverName = ""
                        }
                    )
                }
                isViewingDoctorChat -> {
                    ChatWithDoctorScreen(
                        onNavigateBack = { isViewingDoctorChat = false },
                        onStartChat = { caseId, receiverId, receiverName ->
                            selectedChatCaseId = caseId
                            selectedChatReceiverId = receiverId
                            selectedChatReceiverName = receiverName
                            isViewingDoctorChat = false
                            isViewingMessages = true
                        }
                    )
                }
                isViewingMessages -> {
                    MessagesScreen(
                        onNavigateBack = { isViewingMessages = false },
                        onConversationClick = { conversation ->
                            selectedChatCaseId = conversation.case_id
                            selectedChatReceiverId = conversation.other_user.id
                            selectedChatReceiverName = conversation.other_user.name
                        }
                    )
                }
                isEditingProfile -> {
                    EditProfileScreen(
                        onNavigateBack = { isEditingProfile = false },
                        onProfileUpdated = { }
                    )
                }
                isCreatingCase -> {
                    NewCaseScreen(
                        onNavigateBack = { isCreatingCase = false },
                        onStartAnalysis = { caseId, caseTitle ->
                            // Navigate to analysis screen with created case
                            analyzingCaseId = caseId
                            analyzingCaseTitle = caseTitle
                            isCreatingCase = false
                            isAnalyzingCase = true
                        },
                        onUploadClick = { }
                    )
                }
                isViewingProfile -> ProfileScreen(
                    onNavigateBack = { isViewingProfile = false },
                    onNavigateToEditProfile = { isEditingProfile = true },
                    onLogout = onLogout,
                    profileData = (profileState as? Resource.Success)?.data
                )
                selectedTab == 0 -> {
                    when (userRole) {
                        "PATHOLOGIST" -> {
                            DashboardContent(
                                userName = dashboardName,
                                profilePicture = profilePicture,
                                onNavigateToProfile = { isViewingProfile = true },
                                onNavigateToCases = { selectedTab = 1 }
                            )
                        }
                        "PATIENT", "STUDENT" -> {
                            PatientDashboard(
                                userName = dashboardName,
                                profilePicture = profilePicture,
                                onNavigateToProfile = { isViewingProfile = true },
                                onChatWithDoctor = { isViewingDoctorChat = true }
                            )
                        }
                        else -> {
                            // This should not happen as we show loading above if userRole is null
                            DashboardContent(
                                userName = dashboardName,
                                profilePicture = profilePicture,
                                onNavigateToProfile = { isViewingProfile = true },
                                onNavigateToCases = { selectedTab = 1 }
                            )
                        }
                    }
                }
                selectedTab == 1 -> CasesScreen()
                selectedTab == 2 -> AnalysisListScreen()
                selectedTab == 3 -> SettingsScreen(
                    onNavigateBack = { selectedTab = 0 },
                    onEditProfile = { isEditingProfile = true },
                    onLogout = onLogout
                )
                selectedTab == 4 -> AnalysisScreen(
                    onNavigateBack = { selectedTab = 0 },
                    onStartUpload = { isCreatingCase = true }
                )
                }  // Close nested when for is Resource.Success
            }  // Close outer when(profileState)
        }
    }
}

@Composable
fun PlaceholderTab(name: String) {
    Surface(modifier = Modifier.fillMaxSize(), color = BackgroundGray) {
        // TODO: implement $name screen
    }
}
