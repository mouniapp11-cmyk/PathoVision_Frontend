package com.simats.pathovision.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Sort
import androidx.compose.material.icons.rounded.Warning
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
import com.simats.pathovision.ui.cases.CasesViewModel
import com.simats.pathovision.utils.Resource
import com.simats.pathovision.utils.UrlUtils

private val StudentPrimaryBlue = Color(0xFF4A6FE3)
private val StudentBackgroundGray = Color(0xFFF4F6F9)
private val StudentTextDark = Color(0xFF1A1F2E)
private val StudentTextGray = Color(0xFF8C97B2)
private val StudentCardWhite = Color.White
private val StudentBadgeGreen = Color(0xFF2EB872)
private val StudentBadgeRed = Color(0xFFEF4444)
private val StudentBadgeAmber = Color(0xFFFFA726)

@Composable
fun StudentDashboard(
    userName: String = "Student",
    profilePicture: String? = null,
    onNavigateToProfile: () -> Unit = {},
    onCaseClick: (CaseItem) -> Unit = {},
    viewModel: CasesViewModel = hiltViewModel()
) {
    val casesState by viewModel.casesState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var selectedSort by remember { mutableStateOf("Newest") }

    val filters = listOf("All", "Malignant", "Benign")
    val sortOptions = listOf("Newest", "Highest Confidence", "Lowest Confidence")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StudentBackgroundGray)
    ) {
        StudentDashboardHeader(
            userName = userName,
            profilePicture = profilePicture,
            onProfileClick = onNavigateToProfile
        )

        Spacer(modifier = Modifier.height(20.dp))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            SectionTitle(title = "Recommended Cases", actionText = "View All")
            Spacer(modifier = Modifier.height(12.dp))

            when (val state = casesState) {
                is Resource.Loading -> {
                    LoadingRow()
                }
                is Resource.Success -> {
                    val recommended = state.data
                        .sortedByDescending { it.confidence_score ?: 0.0 }
                        .take(3)
                    if (recommended.isEmpty()) {
                        EmptyRow(message = "No recommended cases yet")
                    } else {
                        recommended.forEach { caseItem ->
                            RecommendedCaseCard(caseItem, onCaseClick)
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
                is Resource.Error -> {
                    EmptyRow(message = state.message ?: "Failed to load cases")
                }
                null -> LoadingRow()
            }

            Spacer(modifier = Modifier.height(24.dp))

            SectionTitle(title = "Case Library", actionText = "Sort")
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text("Search by Case ID, Tissue Type, or Diagnosis", color = StudentTextGray, fontSize = 13.sp)
                },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = "Search", tint = StudentTextGray) },
                trailingIcon = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            val currentIndex = sortOptions.indexOf(selectedSort).coerceAtLeast(0)
                            selectedSort = sortOptions[(currentIndex + 1) % sortOptions.size]
                        }
                    ) {
                        Icon(Icons.Rounded.Sort, contentDescription = "Sort", tint = StudentPrimaryBlue)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = selectedSort,
                            fontSize = 12.sp,
                            color = StudentPrimaryBlue,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = StudentPrimaryBlue,
                    unfocusedBorderColor = Color(0xFFE5E7EB),
                    focusedContainerColor = StudentCardWhite,
                    unfocusedContainerColor = StudentCardWhite
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            FilterRow(
                filters = filters,
                selectedFilter = selectedFilter,
                onFilterSelected = { selectedFilter = it }
            )

            Spacer(modifier = Modifier.height(12.dp))

            when (val state = casesState) {
                is Resource.Loading -> {
                    LoadingColumn()
                }
                is Resource.Success -> {
                    val sortedCases = state.data
                        .filter { caseItem ->
                            val matchesSearch = searchQuery.isBlank() ||
                                caseItem.title.contains(searchQuery, ignoreCase = true) ||
                                caseItem.id.contains(searchQuery, ignoreCase = true)
                            val matchesFilter = when (selectedFilter) {
                                "Malignant" -> caseItem.ai_prediction == "Malignant"
                                "Benign" -> caseItem.ai_prediction == "Benign"
                                else -> true
                            }
                            matchesSearch && matchesFilter
                        }
                        .let { items ->
                            when (selectedSort) {
                                "Highest Confidence" -> items.sortedByDescending { it.confidence_score ?: 0.0 }
                                "Lowest Confidence" -> items.sortedBy { it.confidence_score ?: 0.0 }
                                else -> items.sortedByDescending { it.created_at ?: "" }
                            }
                        }

                    if (sortedCases.isEmpty()) {
                        EmptyRow(message = "No cases match your filters")
                    } else {
                        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                            sortedCases.forEach { caseItem ->
                                StudentCaseCard(caseItem = caseItem, onCaseClick = onCaseClick)
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }
                }
                is Resource.Error -> {
                    EmptyRow(message = state.message ?: "Failed to load cases")
                }
                null -> LoadingColumn()
            }

            Spacer(modifier = Modifier.height(24.dp))

            SectionTitle(title = "Recent Activity", actionText = "See All")
            Spacer(modifier = Modifier.height(12.dp))

            when (val state = casesState) {
                is Resource.Success -> {
                    val recent = state.data.take(3)
                    if (recent.isEmpty()) {
                        EmptyRow(message = "No recent activity")
                    } else {
                        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                            recent.forEach { caseItem ->
                                RecentActivityRow(caseItem)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
                else -> EmptyRow(message = "No recent activity")
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun StudentDashboardHeader(
    userName: String,
    profilePicture: String? = null,
    onProfileClick: () -> Unit
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

        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = "Case Library",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = StudentTextDark
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Anonymized AI-assisted learning cases",
                fontSize = 13.sp,
                color = StudentTextGray
            )
        }

        val imageUrl = UrlUtils.resolveMediaUrl(profilePicture)
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(StudentPrimaryBlue)
                .clickable(onClick = onProfileClick),
            contentAlignment = Alignment.Center
        ) {
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Profile",
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = firstName.take(2).uppercase(),
                    color = StudentCardWhite,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}


@Composable
private fun SectionTitle(title: String, actionText: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = StudentTextDark)
        Text(text = actionText, fontSize = 12.sp, color = StudentPrimaryBlue, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun FilterRow(
    filters: List<String>,
    selectedFilter: String,
    onFilterSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        filters.forEach { filter ->
            val isSelected = selectedFilter == filter
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSelected) StudentPrimaryBlue else StudentCardWhite)
                    .clickable { onFilterSelected(filter) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = filter,
                    fontSize = 12.sp,
                    color = if (isSelected) StudentCardWhite else StudentTextGray,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun RecommendedCaseCard(caseItem: CaseItem, onCaseClick: (CaseItem) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clickable { onCaseClick(caseItem) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = StudentCardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CaseThumbnail(caseItem)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = caseItem.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = StudentTextDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = caseItem.ai_prediction ?: "Unknown",
                    fontSize = 11.sp,
                    color = StudentTextGray
                )
            }
            ConfidenceBadge(caseItem)
        }
    }
}

@Composable
private fun StudentCaseCard(caseItem: CaseItem, onCaseClick: (CaseItem) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCaseClick(caseItem) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = StudentCardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CaseThumbnail(caseItem)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = caseItem.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = StudentTextDark
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val isMalignant = caseItem.ai_prediction == "Malignant"
                    StatusPill(
                        text = caseItem.ai_prediction ?: "Unknown",
                        color = if (isMalignant) StudentBadgeRed else StudentBadgeGreen
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    caseItem.confidence_score?.let {
                        Text(
                            text = "${(it * 100).toInt()}% confidence",
                            fontSize = 11.sp,
                            color = StudentTextGray
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "View Case Study",
                    fontSize = 12.sp,
                    color = StudentPrimaryBlue,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun RecentActivityRow(caseItem: CaseItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(StudentCardWhite, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Rounded.BarChart, contentDescription = null, tint = StudentPrimaryBlue)
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = caseItem.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = StudentTextDark
            )
            Text(
                text = "Reviewed ${studentFormatDate(caseItem.created_at)}",
                fontSize = 11.sp,
                color = StudentTextGray
            )
        }
        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = StudentTextGray)
    }
}

@Composable
private fun CaseThumbnail(caseItem: CaseItem) {
    val imageUrl = UrlUtils.resolveMediaUrl(caseItem.image_url)
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF0F2F5)),
        contentAlignment = Alignment.Center
    ) {
        if (imageUrl != null) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Case image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(Icons.Rounded.Warning, contentDescription = null, tint = StudentBadgeAmber)
        }
    }
}

@Composable
private fun StatusPill(text: String, color: Color) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text = text, fontSize = 10.sp, color = color, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ConfidenceBadge(caseItem: CaseItem) {
    val confidence = caseItem.confidence_score?.let { (it * 100).toInt() } ?: 0
    val color = if (confidence >= 90) StudentBadgeGreen else StudentBadgeAmber
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text = "${confidence}%", fontSize = 11.sp, color = color, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun LoadingRow() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = StudentPrimaryBlue)
    }
}

@Composable
private fun LoadingColumn() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = StudentPrimaryBlue)
    }
}

@Composable
private fun EmptyRow(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = message, fontSize = 13.sp, color = StudentTextGray)
    }
}

private fun studentFormatDate(isoDate: String?): String {
    if (isoDate.isNullOrBlank()) return "recently"
    return isoDate.take(10)
}

@Composable
fun StudentCaseDetailScreen(
    caseItem: CaseItem,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StudentBackgroundGray)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.ArrowBack,
                contentDescription = "Back",
                tint = StudentTextGray,
                modifier = Modifier
                    .size(24.dp)
                    .clickable(onClick = onNavigateBack)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Case Detail",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = StudentTextDark
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = StudentCardWhite)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = caseItem.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = StudentTextDark
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Case ID: ${caseItem.id}",
                    fontSize = 12.sp,
                    color = StudentTextGray
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val isMalignant = caseItem.ai_prediction == "Malignant"
                    StatusPill(
                        text = caseItem.ai_prediction ?: "Unknown",
                        color = if (isMalignant) StudentBadgeRed else StudentBadgeGreen
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    caseItem.confidence_score?.let {
                        Text(
                            text = "${(it * 100).toInt()}% confidence",
                            fontSize = 12.sp,
                            color = StudentTextGray
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = caseItem.doctor_notes ?: "No case notes available.",
                    fontSize = 13.sp,
                    color = StudentTextGray
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
