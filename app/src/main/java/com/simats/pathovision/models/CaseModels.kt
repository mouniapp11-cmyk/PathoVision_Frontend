package com.simats.pathovision.models

data class CaseItem(
    val id: String,
    val title: String,
    val image_url: String?,
    val ai_prediction: String?,       // "Benign" or "Malignant"
    val confidence_score: Double?,
    val doctor_notes: String?,
    val pathologist_id: String?,
    val patient_id: String?,
    val created_at: String?,
    val Pathologist: CaseUser?,
    val Patient: CaseUser?
)

data class CaseUser(
    val name: String?,
    val email: String?
)

data class CreateCaseRequest(
    val title: String,
    val doctor_notes: String?,
    val patient_id: String?
)

data class AnalysisFinding(
    val id: String,
    val name: String,
    val region: String,
    val matchPercentage: Int,
    val severity: String // "high", "medium", "low"
)

data class AnalysisResult(
    val caseId: String,
    val status: String, // "analyzing", "complete", "failed"
    val findings: List<AnalysisFinding>,
    val anomaliesCount: Int
)
