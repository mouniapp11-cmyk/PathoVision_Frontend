package com.simats.pathovision.ui.analysis

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.simats.pathovision.models.AnalysisFinding

private val PrimaryBlue = Color(0xFF2F5FE3)
private val SuccessGreen = Color(0xFF10B981)
private val BackgroundGray = Color(0xFFF4F6F9)
private val TextDark = Color(0xFF1A1F2E)
private val TextGray = Color(0xFF8C97B2)
private val CardWhite = Color.White
private val ErrorRed = Color(0xFFEF4444)
private val WarningOrange = Color(0xFFF59E0B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaseAnalysisScreen(
    caseId: String,
    caseTitle: String,
    slideImageUrl: String?,
    isAnalyzing: Boolean = false,
    analysisComplete: Boolean = false,
    onNavigateBack: () -> Unit = {},
    onViewFullImage: () -> Unit = {},
    viewModel: CaseAnalysisViewModel = hiltViewModel()
) {
    val isAnalyzingState by viewModel.isAnalyzing.collectAsStateWithLifecycle()
    val analysisCompleteState by viewModel.analysisComplete.collectAsStateWithLifecycle()
    val findings by viewModel.findings.collectAsStateWithLifecycle()

    // Start analysis when screen loads if not already complete
    LaunchedEffect(caseId) {
        if (!analysisCompleteState && !isAnalyzingState) {
            viewModel.startAnalysis(caseId)
        }
    }

    val currentIsAnalyzing = isAnalyzing || isAnalyzingState
    val currentAnalysisComplete = analysisComplete || analysisCompleteState
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        // Top App Bar
        CenterAlignedTopAppBar(
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = caseTitle.ifBlank { "Case #${caseId.take(8)}" },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    // Status indicator
                    when {
                        currentIsAnalyzing -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(12.dp),
                                    strokeWidth = 2.dp,
                                    color = PrimaryBlue
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Analyzing...",
                                    fontSize = 11.sp,
                                    color = PrimaryBlue,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        currentAnalysisComplete -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.CheckCircle,
                                    contentDescription = "Complete",
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Analysis Complete",
                                    fontSize = 11.sp,
                                    color = SuccessGreen,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
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
            actions = {
                IconButton(onClick = { }) {
                    Icon(
                        Icons.Rounded.MoreVert,
                        contentDescription = "Menu",
                        tint = TextDark
                    )
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color.Transparent
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Slide Image with Zoom Controls
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Slide Image
                    if (slideImageUrl != null) {
                        AsyncImage(
                            model = slideImageUrl,
                            contentDescription = "Histopathology Slide",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // Placeholder for slide
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFFE5E7EB)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.Image,
                                contentDescription = "No Image",
                                tint = TextGray,
                                modifier = Modifier.size(64.dp)
                            )
                        }
                    }

                    // AI Markers Overlay (only show when analysis complete)
                    if (currentAnalysisComplete && findings.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = PrimaryBlue,
                                modifier = Modifier.padding(4.dp)
                            ) {
                                Text(
                                    text = "AI",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    // Zoom Controls (only show when analysis complete)
                    if (currentAnalysisComplete) {
                        Column(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ZoomButton(
                                icon = Icons.Rounded.RemoveRedEye,
                                backgroundColor = Color.White,
                                onClick = onViewFullImage
                            )
                            ZoomButton(
                                icon = Icons.Rounded.Add,
                                backgroundColor = Color.White,
                                onClick = { }
                            )
                            ZoomButton(
                                icon = Icons.Rounded.Remove,
                                backgroundColor = Color.White,
                                onClick = { }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Analysis Status Section
            if (currentIsAnalyzing) {
                AnalyzingProgress()
            } else if (currentAnalysisComplete && findings.isNotEmpty()) {
                AnalysisFindings(findings = findings, anomaliesCount = findings.size)
            } else if (currentAnalysisComplete && findings.isEmpty()) {
                NoFindingsCard()
            }
        }
    }
}

@Composable
fun ZoomButton(
    icon: ImageVector,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = CircleShape,
        color = backgroundColor,
        shadowElevation = 4.dp,
        modifier = Modifier.size(40.dp)
    ) {
        IconButton(onClick = onClick) {
            Icon(
                icon,
                contentDescription = null,
                tint = PrimaryBlue,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun AnalyzingProgress() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Animated progress indicator
            val infiniteTransition = rememberInfiniteTransition(label = "progress")
            val progress by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "progress"
            )

            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.size(64.dp),
                color = PrimaryBlue,
                strokeWidth = 6.dp,
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "AI Analysis in Progress",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Our AI is examining the slide for anomalies\nThis may take a few moments...",
                fontSize = 13.sp,
                color = TextGray,
                lineHeight = 18.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = PrimaryBlue,
                trackColor = Color(0xFFE9F0FF)
            )
        }
    }
}

@Composable
fun AnalysisFindings(findings: List<AnalysisFinding>, anomaliesCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Analysis Findings",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )

                Surface(
                    shape = CircleShape,
                    color = SuccessGreen,
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Rounded.Check,
                            contentDescription = "Complete",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Text(
                text = "$anomaliesCount Anomalies detected",
                fontSize = 13.sp,
                color = TextGray,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            // Findings List
            findings.forEachIndexed { index, finding ->
                FindingItem(finding = finding)
                if (index < findings.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = Color(0xFFE5E7EB)
                    )
                }
            }
        }
    }
}

@Composable
fun FindingItem(finding: AnalysisFinding) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Severity Icon
        val (iconColor, icon) = when (finding.severity) {
            "high" -> ErrorRed to Icons.Rounded.Warning
            "medium" -> WarningOrange to Icons.Rounded.Report
            else -> TextGray to Icons.Rounded.GridOn
        }

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = iconColor.copy(alpha = 0.1f),
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon,
                    contentDescription = finding.severity,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Finding Details
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = finding.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextDark
            )
            Text(
                text = finding.region,
                fontSize = 12.sp,
                color = TextGray,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        // Match Percentage
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${finding.matchPercentage}% Match",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = when {
                    finding.matchPercentage >= 90 -> ErrorRed
                    finding.matchPercentage >= 70 -> WarningOrange
                    else -> SuccessGreen
                }
            )
            Text(
                text = "Auto-detected",
                fontSize = 10.sp,
                color = TextGray,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
fun NoFindingsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Rounded.CheckCircle,
                contentDescription = "No Issues",
                tint = SuccessGreen,
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "No Anomalies Detected",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "The AI analysis did not detect any significant anomalies in this slide.",
                fontSize = 13.sp,
                color = TextGray,
                lineHeight = 18.sp
            )
        }
    }
}
