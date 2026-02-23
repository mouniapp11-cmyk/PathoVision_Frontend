package com.simats.pathovision.ui.cases

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.simats.pathovision.models.CaseItem
import com.simats.pathovision.utils.Resource

private val PrimaryBlue = Color(0xFF4A6FE3)
private val BackgroundGray = Color(0xFFF4F5F7)
private val TextDark = Color(0xFF1A1F2E)
private val TextGray = Color(0xFF8E9BB5)
private val White = Color.White
private val GreenSuccess = Color(0xFF2EB872)
private val RedDanger = Color(0xFFEF4444)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CasesScreen(
    onCaseClick: (caseId: String) -> Unit = {},
    viewModel: CasesViewModel = hiltViewModel()
) {
    val casesState by viewModel.casesState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Pending", "Processing", "Reviewed")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 52.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Cases",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextDark
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { viewModel.loadCases() }
                ) {
                    Icon(Icons.Rounded.Sort, contentDescription = "Sort", tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Sort (New)", fontSize = 13.sp, color = PrimaryBlue, fontWeight = FontWeight.SemiBold)
                }
                Icon(Icons.Rounded.Tune, contentDescription = "Filter", tint = TextGray, modifier = Modifier.size(22.dp))
            }
        }

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by patient name or case ID...", color = TextGray, fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = "Search", tint = TextGray) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryBlue,
                unfocusedBorderColor = Color(0xFFE5E7EB),
                focusedContainerColor = White,
                unfocusedContainerColor = White
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Filter chips
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
                        .background(if (isSelected) PrimaryBlue else White)
                        .clickable { selectedFilter = filter }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = filter,
                        fontSize = 13.sp,
                        color = if (isSelected) White else TextGray,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Content
        when (val state = casesState) {
            is Resource.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            }

            is Resource.Error -> {
                EmptyState(
                    icon = Icons.Rounded.ErrorOutline,
                    title = "Failed to load cases",
                    subtitle = state.message ?: "Check your connection and try again.",
                    actionText = "Retry",
                    onAction = { viewModel.loadCases() }
                )
            }

            is Resource.Success -> {
                val allCases = state.data
                val filteredCases = allCases.filter { case ->
                    val matchesSearch = searchQuery.isBlank() ||
                            case.title.contains(searchQuery, ignoreCase = true) ||
                            case.id.contains(searchQuery, ignoreCase = true) ||
                            case.Patient?.name?.contains(searchQuery, ignoreCase = true) == true
                    matchesSearch
                }

                if (filteredCases.isEmpty()) {
                    EmptyState(
                        icon = Icons.Rounded.SearchOff,
                        title = "No cases found",
                        subtitle = "Try adjusting your search or filters.",
                        actionText = null,
                        onAction = {}
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 20.dp, top = 4.dp, end = 20.dp, bottom = 100.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredCases) { case ->
                            CaseCard(case = case, onClick = { onCaseClick(case.id) })
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                }
            }

            null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            }
        }
    }
}

@Composable
fun CaseCard(case: CaseItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Prediction indicator circle
            val ismalignant = case.ai_prediction == "Malignant"
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (ismalignant) Color(0xFFFFEEEE) else Color(0xFFE8F8EE),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (ismalignant) Icons.Rounded.Warning else Icons.Rounded.CheckCircle,
                    contentDescription = case.ai_prediction,
                    tint = if (ismalignant) RedDanger else GreenSuccess,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = case.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                case.Patient?.name?.let { patientName ->
                    Text(
                        text = "Patient: $patientName",
                        fontSize = 12.sp,
                        color = TextGray
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // AI Prediction badge
                    case.ai_prediction?.let {
                        PredictionBadge(text = it, ismalignant = ismalignant)
                    }
                    // Confidence score
                    case.confidence_score?.let {
                        Text(
                            text = "${(it * 100).toInt()}% conf",
                            fontSize = 11.sp,
                            color = TextGray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Date
            Column(horizontalAlignment = Alignment.End) {
                case.created_at?.let { dateStr ->
                    val shortDate = dateStr.take(10) // "2025-01-15"
                    Text(text = shortDate, fontSize = 11.sp, color = TextGray)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = "View",
                    tint = Color(0xFFCBD0DB),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun PredictionBadge(text: String, ismalignant: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (ismalignant) Color(0xFFFFEEEE) else Color(0xFFE8F8EE))
            .padding(horizontal = 7.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            color = if (ismalignant) RedDanger else GreenSuccess,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun EmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    actionText: String?,
    onAction: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(Color(0xFFE8EDFF), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = PrimaryBlue, modifier = Modifier.size(36.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = title, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextDark)
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = subtitle, fontSize = 13.sp, color = TextGray)
        if (actionText != null) {
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(actionText, color = White, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
