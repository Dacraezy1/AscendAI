package com.ascendai.app.model

enum class GuideCategory(val label: String) {
    ALL("All"),
    JAWLINE("Jaw & Mandible"),
    EYES("Hunter Eyes"),
    SKIN("Glass Skin"),
    DEBLOAT("Debloating & Lean"),
    HAIR("Hair & Synergy")
}

data class AscensionGuide(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: GuideCategory,
    val readTime: String,
    val difficulty: String,
    val impact: String,
    val summary: String,
    val steps: List<String>,
    val scientificBasis: String
)
