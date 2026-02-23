package com.simats.pathovision.ui.cases

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.HelpOutline
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.simats.pathovision.models.UserData
import com.simats.pathovision.utils.Resource

private val PrimaryBlue = Color(0xFF2F5FE3)
private val BackgroundGray = Color(0xFFF4F6F9)
private val TextDark = Color(0xFF1A1F2E)
private val TextGray = Color(0xFF8C97B2)
private val CardWhite = Color.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewCaseScreen(
    onNavigateBack: () -> Unit = {},
    onStartAnalysis: (caseId: String, caseTitle: String) -> Unit = { _, _ -> },
    onUploadClick: () -> Unit = {},
    viewModel: NewCaseViewModel = hiltViewModel()
) {
    val patientsState by viewModel.patientsState.collectAsState()
    val createCaseState by viewModel.createCaseState.collectAsState()
    
    var selectedPatient by remember { mutableStateOf<UserData?>(null) }
    var selectedTissueType by remember { mutableStateOf("") }
    val clinicalNotes = remember { mutableStateOf("") }
    val selectedFileName = remember { mutableStateOf<String?>(null) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    var patientDropdownExpanded by remember { mutableStateOf(false) }
    var tissueDropdownExpanded by remember { mutableStateOf(false) }
    
    // Handle case creation state
    LaunchedEffect(createCaseState) {
        when (createCaseState) {
            is Resource.Success -> {
                val createdCase = (createCaseState as Resource.Success).data
                onStartAnalysis(createdCase.id, createdCase.title)
                viewModel.resetCreateCaseState()
            }
            is Resource.Error -> {
                errorMessage = (createCaseState as Resource.Error).message
                showError = true
            }
            else -> {}
        }
    }
    
    val tissueTypes = listOf(
        "Skin",
        "Liver",
        "Kidney",
        "Lung",
        "Breast",
        "Prostate",
        "Colon",
        "Brain",
        "Bone",
        "Lymph Node",
        "Other"
    )
    
    val uploadLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        selectedFileName.value = uri?.lastPathSegment
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = "NEW CASE",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
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
                        Icons.Rounded.HelpOutline,
                        contentDescription = "Help",
                        tint = PrimaryBlue
                    )
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Upload Slide Image",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Text(
                text = "Supported formats: .svs, .tiff, .ndpi (Max 2GB)",
                fontSize = 12.sp,
                color = TextGray,
                modifier = Modifier.padding(top = 6.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Transparent)
                    .clickable {
                        onUploadClick()
                        uploadLauncher.launch(arrayOf("*/*"))
                    },
                color = CardWhite
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(CardWhite)
                        .dashedBorder(color = Color(0xFFB7C6F8)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE9F0FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.CloudUpload,
                                contentDescription = "Upload",
                                tint = PrimaryBlue
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Tap to browse files",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextDark
                        )
                        if (selectedFileName.value.isNullOrBlank()) {
                            Text(
                                text = "or drag and drop here",
                                fontSize = 12.sp,
                                color = TextGray,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        } else {
                            Text(
                                text = "Selected: ${selectedFileName.value}",
                                fontSize = 12.sp,
                                color = TextGray,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Case Details",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Patient Selection Dropdown
            ExposedDropdownMenuBox(
                expanded = patientDropdownExpanded,
                onExpandedChange = { patientDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedPatient?.name ?: "",
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Select Patient") },
                    placeholder = { Text("Choose a patient") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = patientDropdownExpanded)
                    },
                    colors = OutlinedTextFieldDefaults.colors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                
                ExposedDropdownMenu(
                    expanded = patientDropdownExpanded,
                    onDismissRequest = { patientDropdownExpanded = false }
                ) {
                    when (val state = patientsState) {
                        is Resource.Loading -> {
                            DropdownMenuItem(
                                text = { Text("Loading patients...") },
                                onClick = { },
                                enabled = false
                            )
                        }
                        is Resource.Success -> {
                            if (state.data.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("No patients found") },
                                    onClick = { },
                                    enabled = false
                                )
                            } else {
                                state.data.forEach { patient ->
                                    DropdownMenuItem(
                                        text = { 
                                            Column {
                                                Text(text = patient.name, fontWeight = FontWeight.Medium)
                                                Text(
                                                    text = patient.email,
                                                    fontSize = 12.sp,
                                                    color = TextGray
                                                )
                                            }
                                        },
                                        onClick = {
                                            selectedPatient = patient
                                            patientDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                        is Resource.Error -> {
                            DropdownMenuItem(
                                text = { Text("Error loading patients") },
                                onClick = { },
                                enabled = false
                            )
                        }
                        null -> {
                            DropdownMenuItem(
                                text = { Text("Loading...") },
                                onClick = { },
                                enabled = false
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tissue Type Dropdown
            ExposedDropdownMenuBox(
                expanded = tissueDropdownExpanded,
                onExpandedChange = { tissueDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedTissueType,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Tissue Type") },
                    placeholder = { Text("Select tissue type") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = tissueDropdownExpanded)
                    },
                    colors = OutlinedTextFieldDefaults.colors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                
                ExposedDropdownMenu(
                    expanded = tissueDropdownExpanded,
                    onDismissRequest = { tissueDropdownExpanded = false }
                ) {
                    tissueTypes.forEach { tissue ->
                        DropdownMenuItem(
                            text = { Text(tissue) },
                            onClick = {
                                selectedTissueType = tissue
                                tissueDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = clinicalNotes.value,
                onValueChange = { clinicalNotes.value = it },
                label = { Text("Clinical Notes (Optional)") },
                placeholder = { Text("Add relevant clinical history...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    // Validate inputs
                    when {
                        selectedPatient == null -> {
                            errorMessage = "Please select a patient"
                            showError = true
                        }
                        selectedTissueType.isBlank() -> {
                            errorMessage = "Please select a tissue type"
                            showError = true
                        }
                        else -> {
                            // Create the case
                            viewModel.createCase(
                                patientId = selectedPatient!!.id,
                                tissueType = selectedTissueType,
                                clinicalNotes = clinicalNotes.value.ifBlank { null }
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                enabled = createCaseState !is Resource.Loading
            ) {
                if (createCaseState is Resource.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Smart Scan & Analyze",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
    
    // Error Snackbar
    if (showError) {
        LaunchedEffect(showError) {
            kotlinx.coroutines.delay(3000)
            showError = false
        }
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Snackbar(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(errorMessage)
            }
        }
    }
}

private fun Modifier.dashedBorder(
    color: Color,
    strokeWidth: Float = 2f,
    on: Float = 10f,
    off: Float = 8f
): Modifier {
    return this.then(
        Modifier.drawBehind {
            val stroke = Stroke(width = strokeWidth, pathEffect = PathEffect.dashPathEffect(floatArrayOf(on, off), 0f))
            val radius = 16.dp.toPx()
            drawRoundRect(
                color = color,
                style = stroke,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius, radius)
            )
        }
    )
}
