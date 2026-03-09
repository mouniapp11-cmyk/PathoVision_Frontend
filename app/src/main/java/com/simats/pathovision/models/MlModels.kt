package com.simats.pathovision.models

data class MlPredictionResponse(
    val prediction: Int,  // 0 = Benign, 1 = Malignant
    val confidence: Double,
    val class_name: String
)

data class MlModelInfo(
    val test_acc: Double,
    val test_auc: Double,
    val best_val_auc: Double
)
