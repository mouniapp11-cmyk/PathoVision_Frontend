package com.simats.pathovision.ui.report

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

// Theme colors
private val PrimaryBlue = Color(0xFF2F5FE3)
private val SuccessGreen = Color(0xFF10B981)
private val BackgroundGray = Color(0xFFF4F6F9)
private val TextDark = Color(0xFF1A1F2E)
private val TextSecondary = Color(0xFF8C97B2)
private val ErrorRed = Color(0xFFEF4444)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DigitalReportScreen(
    caseId: String,
    onNavigateBack: () -> Unit,
    onSignOffReport: () -> Unit,
    viewModel: DigitalReportViewModel = hiltViewModel()
) {
    val isLoadingData by viewModel.isLoadingData.collectAsStateWithLifecycle()
    val reportData by viewModel.reportData.collectAsStateWithLifecycle()
    val isGeneratingReport by viewModel.isGeneratingReport.collectAsStateWithLifecycle()
    val generatedReport by viewModel.generatedReport.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    
    // Load case data on screen load
    LaunchedEffect(caseId) {
        viewModel.loadCaseData(caseId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Pathology Report #${reportData?.caseTitle ?: caseId}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = reportData?.patientName ?: "Loading...",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Rounded.ArrowBack, "Back")
                    }
                },
                actions = {
                    Text(
                        text = "Finalized",
                        fontSize = 12.sp,
                        color = SuccessGreen,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundGray)
                .padding(padding)
        ) {
            if (isLoadingData || isGeneratingReport) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = PrimaryBlue)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (isLoadingData) "Loading case data..." else "Generating detailed pathology report...",
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                    }
                }
            } else if (reportData == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Warning,
                            contentDescription = "Error",
                            tint = ErrorRed,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = errorMessage ?: "Report data is unavailable.",
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.loadCaseData(caseId) },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(text = "Retry")
                        }
                    }
                }
            } else reportData?.let { data ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Patient Information Card
                    PatientInfoCard(
                        name = data.patientName,
                        dob = data.patientDob,
                        mrn = data.patientMrn,
                        specimenSource = data.specimenSource
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // AI Analysis Card
                    AIAnalysisCard(
                        prediction = data.aiPrediction,
                        confidence = data.confidence,
                        findings = data.findings
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Final Diagnosis
                    generatedReport?.let { report ->
                        ReportSection(
                            title = "FINAL DIAGNOSIS",
                            content = report.finalDiagnosis,
                            isWarning = true
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Margins
                        if (report.margins.isNotEmpty()) {
                            ReportSection(
                                title = "Margins",
                                content = report.margins
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        
                        // Microscopic Description
                        ReportSection(
                            title = "Microscopic Description",
                            content = report.microscopicDescription,
                            isCollapsible = true
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Clinical History
                        ReportSection(
                            title = "CLINICAL HISTORY",
                            content = report.clinicalHistory
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Gross Description
                        ReportSection(
                            title = "GROSS DESCRIPTION",
                            content = report.grossDescription
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Pathologist Signature
                        DoctorSignature(
                            doctorName = "Dr. A. Smith, MD",
                            reportDate = "Oct 21, 2024 at 16:36"
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(80.dp))
                }
                
                // Sign Off Button at bottom
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    shadowElevation = 8.dp
                ) {
                    Button(
                        onClick = {
                            viewModel.signOffReport(caseId)
                            onSignOffReport()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryBlue
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Check,
                            contentDescription = "Sign Off",
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sign Off Report",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PatientInfoCard(
    name: String,
    dob: String,
    mrn: String,
    specimenSource: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Rounded.Person,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            InfoRow("DOB", dob)
            InfoRow("MRN", mrn)
            
            Spacer(modifier = Modifier.height(8.dp))
            Divider()
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "SPECIMEN SOURCE",
                fontSize = 11.sp,
                color = TextSecondary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = specimenSource,
                fontSize = 14.sp,
                color = TextDark
            )
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = "$label:",
            fontSize = 13.sp,
            color = TextSecondary,
            modifier = Modifier.width(80.dp)
        )
        Text(
            text = value,
            fontSize = 13.sp,
            color = TextDark,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun AIAnalysisCard(
    prediction: String,
    confidence: Double,
    findings: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF0F9FF)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "AI ANALYSES",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${String.format("%.1f", confidence)}% Confidence",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SuccessGreen
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = prediction,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            
            if (findings.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                findings.forEach { finding ->
                    Text(
                        text = "• $finding",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun ReportSection(
    title: String,
    content: String,
    isWarning: Boolean = false,
    isCollapsible: Boolean = false
) {
    var isExpanded by remember { mutableStateOf(true) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isWarning) Color(0xFFFEF2F2) else Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isWarning) {
                        Icon(
                            imageVector = Icons.Rounded.Warning,
                            contentDescription = null,
                            tint = ErrorRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isWarning) ErrorRed else TextSecondary
                    )
                }
                
                if (isCollapsible) {
                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Text(
                            text = if (isExpanded) "▼" else "▶",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
            
            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = content,
                    fontSize = 13.sp,
                    color = TextDark,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
fun DoctorSignature(
    doctorName: String,
    reportDate: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Rounded.Person,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = doctorName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark
                )
                Text(
                    text = "Report generated $reportDate",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }
    }
}
