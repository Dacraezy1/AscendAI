package com.ascendai.app.model

data class FacialMetric(
    val name: String,
    val score: Int,
    val category: String,
    val status: String,
    val details: String,
    val ascendTip: String
)
