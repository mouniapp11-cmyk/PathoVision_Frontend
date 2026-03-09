package com.simats.pathovision.ui.report

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.pathovision.repository.CaseRepository
import com.simats.pathovision.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReportData(
    val caseTitle: String,
    val patientName: String,
    val patientDob: String,
    val patientMrn: String,
    val specimenSource: String,
    val aiPrediction: String,
    val confidence: Double,
    val findings: List<String>
)

@HiltViewModel
class DigitalReportViewModel @Inject constructor(
    private val caseRepository: CaseRepository
) : ViewModel() {

    companion object {
        private const val TAG = "DigitalReportViewModel"
    }

    private val _isLoadingData = MutableStateFlow(true)
    val isLoadingData: StateFlow<Boolean> = _isLoadingData

    private val _reportData = MutableStateFlow<ReportData?>(null)
    val reportData: StateFlow<ReportData?> = _reportData

    private val _isGeneratingReport = MutableStateFlow(false)
    val isGeneratingReport: StateFlow<Boolean> = _isGeneratingReport

    private val _generatedReport = MutableStateFlow<com.simats.pathovision.models.PathologyReport?>(null)
    val generatedReport: StateFlow<com.simats.pathovision.models.PathologyReport?> = _generatedReport

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun loadCaseData(caseId: String) {
        viewModelScope.launch {
            try {
                _isLoadingData.value = true
                _errorMessage.value = null
                Log.d(TAG, "Loading case data for: $caseId")
                
                // Auto-generate report using Gemini API - this will fetch real data
                generateReport(caseId)
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load case data: ${e.message}", e)
                _isLoadingData.value = false
                _errorMessage.value = "Failed to load case data: ${e.message}"
            }
        }
    }

    fun generateReport(caseId: String) {
        viewModelScope.launch {
            try {
                _isGeneratingReport.value = true
                _errorMessage.value = null
                Log.d(TAG, "Generating report for case: $caseId")
                
                // Call backend API to generate report using Gemini
                val result = caseRepository.generateReport(caseId)
                
                when (result) {
                    is Resource.Success -> {
                        val response = result.data
                        _generatedReport.value = response.report
                        
                        // Update report data with real patient information from backend
                        _reportData.value = ReportData(
                            caseTitle = "RPC-$caseId",
                            patientName = response.patientName ?: "Anonymous",
                            patientDob = response.patientDob ?: "Not provided",
                            patientMrn = response.mrn ?: "N/A",
                            specimenSource = response.report?.grossDescription?.take(100) ?: "Tissue specimen",
                            aiPrediction = response.aiPrediction ?: "Unknown",
                            confidence = response.confidence ?: 0.0,
                            findings = response.report?.let { report ->
                                listOfNotNull(
                                    report.finalDiagnosis?.take(100),
                                    report.microscopicDescription?.take(100),
                                    report.margins?.take(100)
                                ).filter { it.isNotEmpty() }
                            } ?: listOf("Analysis in progress")
                        )
                        
                        if (response.cached) {
                            Log.d(TAG, "Report retrieved from cache")
                        } else {
                            Log.d(TAG, "Report generated successfully with MRN: ${response.mrn}")
                        }
                    }
                    is Resource.Error -> {
                        Log.e(TAG, "Failed to generate report: ${result.message}")
                        _errorMessage.value = result.message
                    }
                    is Resource.Loading -> {
                        // Loading state handled by _isGeneratingReport
                    }
                }
                
                _isGeneratingReport.value = false
                _isLoadingData.value = false
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to generate report: ${e.message}", e)
                _isGeneratingReport.value = false
                _isLoadingData.value = false
                _errorMessage.value = "Failed to generate report: ${e.message}"
            }
        }
    }

    fun signOffReport(caseId: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Signing off report for case: $caseId")
                
                val result = caseRepository.signOffReport(caseId)
                
                when (result) {
                    is Resource.Success -> {
                        Log.d(TAG, "Report signed off successfully: ${result.data.message}")
                    }
                    is Resource.Error -> {
                        Log.e(TAG, "Failed to sign off report: ${result.message}")
                        _errorMessage.value = result.message
                    }
                    is Resource.Loading -> {
                        // Loading state
                    }
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to sign off report: ${e.message}", e)
                _errorMessage.value = "Failed to sign off report: ${e.message}"
            }
        }
    }
}

