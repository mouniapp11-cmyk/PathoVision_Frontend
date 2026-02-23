package com.simats.pathovision.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.simats.pathovision.models.CaseItem
import com.simats.pathovision.models.UserData
import com.simats.pathovision.utils.Resource
import com.simats.pathovision.utils.UrlUtils

private val PrimaryBlue = Color(0xFF4A6FE3)
private val BackgroundGray = Color(0xFFF4F6F9)
private val TextDark = Color(0xFF1A1F2E)
private val TextGray = Color(0xFF8C97B2)
private val CardWhite = Color.White

@Composable
fun ChatWithDoctorScreen(
    onNavigateBack: () -> Unit = {},
    onStartChat: (caseId: String, receiverId: String, receiverName: String) -> Unit = { _, _, _ -> },
    viewModel: ChatWithDoctorViewModel = hiltViewModel()
) {
    val doctorsState by viewModel.doctorsState.collectAsState()
    val casesState by viewModel.casesState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedDoctor by remember { mutableStateOf<UserData?>(null) }
    var showCasePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                    text = "Chat with Doctor",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextDark
                )
            }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search doctors...", color = TextGray, fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = "Search", tint = TextGray) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryBlue,
                unfocusedBorderColor = Color(0xFFE5E7EB),
                focusedContainerColor = CardWhite,
                unfocusedContainerColor = CardWhite
            )
        )

        when (val state = doctorsState) {
            is Resource.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            }

            is Resource.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.message ?: "Failed to load doctors", color = TextGray)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.loadDoctors() },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }

            is Resource.Success -> {
                val doctors = state.data ?: emptyList()
                val filtered = if (searchQuery.isBlank()) {
                    doctors
                } else {
                    doctors.filter {
                        it.name.contains(searchQuery, ignoreCase = true) ||
                            it.email.contains(searchQuery, ignoreCase = true)
                    }
                }

                if (filtered.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No doctors found", color = TextGray)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filtered) { doctor ->
                            DoctorListItem(
                                doctor = doctor,
                                onClick = {
                                    selectedDoctor = doctor
                                    showCasePicker = true
                                }
                            )
                        }
                    }
                }
            }

            null -> {}
        }
    }

    if (showCasePicker && selectedDoctor != null) {
        CasePickerDialog(
            doctor = selectedDoctor!!,
            casesState = casesState,
            onDismiss = { showCasePicker = false },
            onCaseSelected = { caseItem ->
                showCasePicker = false
                onStartChat(caseItem.id, selectedDoctor!!.id, selectedDoctor!!.name)
            }
        )
    }
}

@Composable
private fun DoctorListItem(
    doctor: UserData,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .height(84.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!doctor.profile_picture.isNullOrBlank()) {
                AsyncImage(
                    model = UrlUtils.resolveMediaUrl(doctor.profile_picture),
                    contentDescription = doctor.name,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Surface(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape),
                    color = Color(0xFFE9F0FF)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = doctor.name.firstOrNull()?.toString() ?: "?",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                    }
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = doctor.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = doctor.email,
                    fontSize = 12.sp,
                    color = TextGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun CasePickerDialog(
    doctor: UserData,
    casesState: Resource<List<CaseItem>>?,
    onDismiss: () -> Unit,
    onCaseSelected: (CaseItem) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        title = {
            Text(
                text = "Choose a case for ${doctor.name}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
        },
        text = {
            when (val state = casesState) {
                is Resource.Loading -> {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryBlue)
                    }
                }
                is Resource.Error -> {
                    Text(state.message ?: "Failed to load cases", color = TextGray)
                }
                is Resource.Success -> {
                    val cases = state.data ?: emptyList()
                    if (cases.isEmpty()) {
                        Text("No cases available yet. Please wait for a case to be created.", color = TextGray)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            cases.take(6).forEach { caseItem ->
                                CasePickerRow(caseItem = caseItem, onClick = { onCaseSelected(caseItem) })
                            }
                        }
                    }
                }
                null -> {
                    Text("Loading cases...", color = TextGray)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = TextGray)
            }
        }
    )
}

@Composable
private fun CasePickerRow(
    caseItem: CaseItem,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() },
        color = Color(0xFFF7F8FA)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = caseItem.title.ifBlank { "Case #${caseItem.id.take(6)}" },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val subtitle = caseItem.ai_prediction?.let { "AI: $it" } ?: "Awaiting analysis"
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = TextGray
                )
            }
        }
    }
}
