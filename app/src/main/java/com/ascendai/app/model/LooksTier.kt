package com.ascendai.app.model

import androidx.compose.ui.graphics.Color
import com.ascendai.app.theme.*

enum class LooksTier(
    val title: String,
    val shortName: String,
    val minScore: Int,
    val maxScore: Int,
    val color: Color,
    val badgeTag: String,
    val description: String
) {
    SUB_5(
        title = "Sub-5",
        shortName = "SUB-5",
        minScore = 0,
        maxScore = 49,
        color = TierSub5Color,
        badgeTag = "FOUNDATIONAL STAGE",
        description = "High room for growth. Immediate gains can be unlocked through body fat reduction, skin barrier repair, and neck posture alignment."
    ),
    LTN(
        title = "Low Tier Normie",
        shortName = "LTN",
        minScore = 50,
        maxScore = 59,
        color = TierLTNColor,
        badgeTag = "HIGH POTENTIAL",
        description = "Solid natural framework. You are holding water retention and unoptimized styling. Ascending to High Tier is well within your grasp."
    ),
    MTN(
        title = "Mid Tier Normie",
        shortName = "MTN",
        minScore = 60,
        maxScore = 69,
        color = TierMTNColor,
        badgeTag = "BALANCED HARMONY",
        description = "Above average facial symmetry. Jaw definition and periorbital refinement will push you into standout territory."
    ),
    HTN(
        title = "High Tier Normie",
        shortName = "HTN",
        minScore = 70,
        maxScore = 79,
        color = TierHTNColor,
        badgeTag = "STANDOUT ATTRACTIVENESS",
        description = "Prominent cheekbones, sharp jawline, and favorable eye tilt. Top tier in everyday rooms. Minor tweaks will break you into Chadlite."
    ),
    CHADLITE(
        title = "Chadlite",
        shortName = "CHADLITE",
        minScore = 80,
        maxScore = 89,
        color = TierChadliteColor,
        badgeTag = "ELITE BONE STRUCTURE",
        description = "Striking dimorphic harmony, hollow cheeks, positive canthal tilt, and square gonial angle. In the top 5% bracket."
    ),
    CHAD(
        title = "Chad",
        shortName = "CHAD",
        minScore = 90,
        maxScore = 95,
        color = TierChadColor,
        badgeTag = "TOP 1% STATURE",
        description = "Exceptional hunter eyes, razor mandible, ideal facial thirds, and peak masculine dimorphism. Irresistible facial presence."
    ),
    TRUE_ADAM(
        title = "True Adam / Gigachad",
        shortName = "TRUE ADAM",
        minScore = 96,
        maxScore = 100,
        color = TierTrueAdamColor,
        badgeTag = "AESTHETIC DEITY",
        description = "Flawless golden ratio proportions, absolute bilateral symmetry, apex jawline projection, and pristine facial aesthetics."
    );

    companion object {
        fun fromScore(score: Int): LooksTier {
            return when {
                score >= 96 -> TRUE_ADAM
                score >= 90 -> CHAD
                score >= 80 -> CHADLITE
                score >= 70 -> HTN
                score >= 60 -> MTN
                score >= 50 -> LTN
                else -> SUB_5
            }
        }
    }
}
