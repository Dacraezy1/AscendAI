package com.ascendai.app.model

data class AnalysisResult(
    val id: String = java.util.UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val overallScore: Int,
    val potentialScore: Int,
    val tier: LooksTier,
    val frontImageUri: String? = null,
    val sideImageUri: String? = null,
    val metrics: List<FacialMetric>,
    val topAscensionFocus: List<String>,
    val goldenRatioHarmony: Float = 0.88f,
    val canthalTiltDegrees: Float = 4.2f,
    val facialSymmetryPct: Int = 89
)
