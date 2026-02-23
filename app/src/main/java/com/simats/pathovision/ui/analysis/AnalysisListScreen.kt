package com.simats.pathovision.ui.analysis

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.simats.pathovision.utils.Resource

private val PrimaryBlue = Color(0xFF2F5FE3)
private val BackgroundGray = Color(0xFFF4F6F9)
private val TextDark = Color(0xFF1A1F2E)
private val TextGray = Color(0xFF8C97B2)
private val CardWhite = Color.White
private val SuccessGreen = Color(0xFF1FA971)
private val WarningOrange = Color(0xFFF59E0B)
private val DangerRed = Color(0xFFEF4444)

@Composable
fun AnalysisListScreen(
    viewModel: AnalysisViewModel = hiltViewModel()
) {
    val selectedFilter = remember { mutableStateOf("All") }
    val casesState by viewModel.casesState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "AI Analysis", fontSize = 12.sp, color = TextGray)
                Text(
                    text = "Task Manager",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
            }
            IconButton(onClick = { }) {
                Icon(Icons.Rounded.Search, contentDescription = "Search", tint = TextDark)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = selectedFilter.value == "All",
                onClick = { selectedFilter.value = "All" },
                label = { Text("All Confidence") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PrimaryBlue,
                    selectedLabelColor = Color.White
                )
            )
            FilterChip(
                selected = selectedFilter.value == "High",
                onClick = { selectedFilter.value = "High" },
                label = { Text("High (>90%)") }
            )
            FilterChip(
                selected = selectedFilter.value == "Medium",
                onClick = { selectedFilter.value = "Medium" },
                label = { Text("Medium") }
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        when (val state = casesState) {
            is Resource.Loading -> {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            }
            is Resource.Error -> {
                Text(
                    text = state.message ?: "Failed to load analyses",
                    color = TextGray,
                    fontSize = 12.sp
                )
            }
            is Resource.Success -> {
                val items = state.data.map { case ->
                    val score = (case.confidence_score ?: 0.0).toFloat()
                    val status = when {
                        score >= 0.90f -> "High Confidence"
                        score >= 0.80f -> "Ambiguous"
                        else -> "Review Required"
                    }
                    val statusColor = when {
                        score >= 0.90f -> SuccessGreen
                        score >= 0.80f -> WarningOrange
                        else -> DangerRed
                    }
                    AnalysisItem(
                        caseId = "Case #${case.id.take(6)}",
                        subtitle = case.title,
                        status = status,
                        progress = score.coerceIn(0f, 1f),
                        statusColor = statusColor
                    )
                }

                val filteredItems = when (selectedFilter.value) {
                    "High" -> items.filter { it.progress >= 0.90f }
                    "Medium" -> items.filter { it.progress in 0.70f..0.89f }
                    else -> items
                }

                val urgentItems = filteredItems.filter { it.progress < 0.90f }
                val completedItems = filteredItems.filter { it.progress >= 0.90f }

                SectionHeader(
                    title = "Priority AI Reviews",
                    badgeText = if (urgentItems.isNotEmpty()) "${urgentItems.size} Urgent" else null
                )

                if (urgentItems.isEmpty()) {
                    Text(
                        text = "No urgent analyses right now",
                        fontSize = 12.sp,
                        color = TextGray,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                } else {
                    urgentItems.forEach { item ->
                        Spacer(modifier = Modifier.height(10.dp))
                        AnalysisCard(item)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                SectionHeader(title = "Completed Analyses")

                if (completedItems.isEmpty()) {
                    Text(
                        text = "No completed analyses yet",
                        fontSize = 12.sp,
                        color = TextGray,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                } else {
                    completedItems.forEach { item ->
                        Spacer(modifier = Modifier.height(10.dp))
                        AnalysisCard(item)
                    }
                }
            }
            null -> {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
private fun SectionHeader(title: String, badgeText: String? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextGray
        )
        Spacer(modifier = Modifier.weight(1f))
        if (badgeText != null) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFFFE4E6)
            ) {
                Text(
                    text = badgeText,
                    fontSize = 11.sp,
                    color = DangerRed,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun AnalysisCard(item: AnalysisItem) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CardWhite
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFEFF2F7)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFD3DCF5))
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.caseId, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                Text(text = item.subtitle, fontSize = 12.sp, color = TextGray)
                Text(
                    text = item.status,
                    fontSize = 11.sp,
                    color = item.statusColor,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFE6E9F0))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(item.progress)
                            .height(6.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(item.statusColor)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Icon(Icons.Rounded.MoreVert, contentDescription = "More", tint = TextGray)
                Text(
                    text = "${(item.progress * 100).toInt()}%",
                    fontSize = 12.sp,
                    color = item.statusColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

private data class AnalysisItem(
    val caseId: String,
    val subtitle: String,
    val status: String,
    val progress: Float,
    val statusColor: Color
)
