package com.simats.pathovision.ui.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.pathovision.models.AnalysisFinding
import com.simats.pathovision.models.AnalysisResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CaseAnalysisViewModel @Inject constructor(
    // Add repository when backend is ready
) : ViewModel() {

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing

    private val _analysisComplete = MutableStateFlow(false)
    val analysisComplete: StateFlow<Boolean> = _analysisComplete

    private val _findings = MutableStateFlow<List<AnalysisFinding>>(emptyList())
    val findings: StateFlow<List<AnalysisFinding>> = _findings

    // Simulate analysis process (replace with actual API call when backend is ready)
    fun startAnalysis(caseId: String) {
        viewModelScope.launch {
            _isAnalyzing.value = true
            _analysisComplete.value = false

            // Simulate analysis delay (3-5 seconds)
            delay(4000)

            // Mock findings (replace with actual API response)
            _findings.value = listOf(
                AnalysisFinding(
                    id = "1",
                    name = "Invasive Carcinoma",
                    region = "Region A • 2.4mm",
                    matchPercentage = 96,
                    severity = "high"
                ),
                AnalysisFinding(
                    id = "2",
                    name = "Atypical Hyperplasia",
                    region = "Region B • 0.8mm",
                    matchPercentage = 87,
                    severity = "medium"
                ),
                AnalysisFinding(
                    id = "3",
                    name = "Mitotic Figure",
                    region = "Region C • Count: 12",
                    matchPercentage = 74,
                    severity = "low"
                )
            )

            _isAnalyzing.value = false
            _analysisComplete.value = true
        }
    }

    fun resetAnalysis() {
        _isAnalyzing.value = false
        _analysisComplete.value = false
        _findings.value = emptyList()
    }
}
