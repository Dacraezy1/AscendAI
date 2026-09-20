package com.ascendai.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ascendai.app.model.AnalysisResult
import com.ascendai.app.model.LooksTier
import com.ascendai.app.theme.*
import com.ascendai.app.ui.components.AttributeProgressBar
import com.ascendai.app.ui.components.GlowingScoreCard
import com.ascendai.app.ui.components.RadarMetricChart
import com.ascendai.app.ui.components.TierBadge
import com.ascendai.app.ui.viewmodel.ScanViewModel

@Composable
fun ResultsScreen(
    viewModel: ScanViewModel,
    onNavigateToGuides: () -> Unit,
    onNavigateToRoutines: () -> Unit,
    onRescan: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val result = uiState.currentResult

    if (result == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("No scan result found", style = Typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onRescan) {
                    Text("Start a Scan")
                }
            }
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Header
        Text(
            text = "FACIAL AESTHETIC REPORT",
            style = Typography.labelSmall.copy(
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold,
                color = NeonCyan
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Main Score Gauge
        GlowingScoreCard(
            score = result.overallScore,
            potentialScore = result.potentialScore,
            size = 210.dp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Looksmax Tier Badge
        TierBadge(tier = result.tier)

        Spacer(modifier = Modifier.height(12.dp))

        // Tier Description
        Text(
            text = result.tier.description,
            style = Typography.bodyMedium.copy(
                color = TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            ),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Quick Biometric Stats - Row 1
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickStatCard(
                label = "CANTHAL TILT",
                value = String.format("%+.1f°", result.canthalTiltDegrees),
                subtext = when {
                    result.canthalTiltDegrees >= 2.0f -> "Hunter Eye"
                    result.canthalTiltDegrees >= -0.5f -> "Neutral"
                    else -> "Negative Tilt"
                },
                accentColor = if (result.canthalTiltDegrees >= 1.5f) NeonCyan else if (result.canthalTiltDegrees >= -0.5f) NeonGold else NeonRose,
                modifier = Modifier.weight(1f)
            )
            QuickStatCard(
                label = "SYMMETRY",
                value = "${result.facialSymmetryPct}%",
                subtext = when {
                    result.facialSymmetryPct >= 85 -> "Elite Match"
                    result.facialSymmetryPct >= 70 -> "Balanced"
                    result.facialSymmetryPct >= 55 -> "Moderate"
                    else -> "Asymmetric"
                },
                accentColor = if (result.facialSymmetryPct >= 75) NeonPurple else if (result.facialSymmetryPct >= 60) NeonGold else NeonRose,
                modifier = Modifier.weight(1f)
            )
            QuickStatCard(
                label = "POTENTIAL",
                value = "${result.potentialScore}",
                subtext = "+${result.potentialScore - result.overallScore} Pts Up",
                accentColor = NeonGold,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Quick Biometric Stats - Row 2 (PSL Dimensions)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickStatCard(
                label = "fWHR",
                value = String.format("%.2f", result.fwhr),
                subtext = when {
                    result.fwhr in 1.84f..2.06f -> "Apex Dimorphic"
                    result.fwhr in 1.74f..1.84f -> "Favorable"
                    result.fwhr in 1.64f..1.74f -> "Standard"
                    result.fwhr > 2.06f -> "Compact Wide"
                    else -> "Narrow Face"
                },
                accentColor = if (result.fwhr in 1.74f..2.10f) NeonCyan else if (result.fwhr >= 1.62f) NeonGold else NeonRose,
                modifier = Modifier.weight(1f)
            )
            QuickStatCard(
                label = "MIDFACE RATIO",
                value = String.format("%.2f", result.midfaceRatio),
                subtext = when {
                    result.midfaceRatio in 0.98f..1.12f -> "Compact Ideal"
                    result.midfaceRatio in 0.92f..0.98f -> "Balanced"
                    result.midfaceRatio > 1.12f -> "Ultra Compact"
                    else -> "Elongated"
                },
                accentColor = if (result.midfaceRatio in 0.96f..1.15f) NeonCyan else if (result.midfaceRatio in 0.88f..0.96f) NeonGold else NeonRose,
                modifier = Modifier.weight(1f)
            )
            QuickStatCard(
                label = "EYE SPACING",
                value = String.format("%.2f", result.eyeSpacingRatio),
                subtext = when {
                    result.eyeSpacingRatio in 0.95f..1.05f -> "Golden 1.0"
                    result.eyeSpacingRatio < 0.95f -> "Close-Set"
                    else -> "Wide-Set"
                },
                accentColor = if (result.eyeSpacingRatio in 0.94f..1.06f) NeonPurple else NeonGold,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // -----------------------------------------------------------------
        // HALOS & FAILOS DIAGNOSTIC (UMAX STYLE)
        // -----------------------------------------------------------------
        if (result.halos.isNotEmpty() || result.failos.isNotEmpty()) {
            Text(
                text = "AESTHETIC DIAGNOSTIC",
                style = Typography.labelSmall.copy(
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                ),
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Halos Card (Strengths)
            if (result.halos.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(SurfaceCard)
                        .border(1.dp, NeonEmerald.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = NeonEmerald,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "HALOS (STANDOUT STRENGTHS)",
                                style = Typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    color = NeonEmerald,
                                    letterSpacing = 1.sp
                                )
                            )
                        }
                        result.halos.forEach { halo ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(NeonEmerald)
                                )
                                Text(
                                    text = halo,
                                    style = Typography.bodyMedium.copy(
                                        color = TextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Failos Card (Bottlenecks)
            if (result.failos.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(SurfaceCard)
                        .border(1.dp, NeonRose.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = NeonRose,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "FAILOS (LIMITING BOTTLENECKS)",
                                style = Typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    color = NeonRose,
                                    letterSpacing = 1.sp
                                )
                            )
                        }
                        result.failos.forEach { failo ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(NeonRose)
                                )
                                Text(
                                    text = failo,
                                    style = Typography.bodyMedium.copy(
                                        color = TextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        // Side Profile Biometrics Card
        if (result.sideProfileSummary != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceCard)
                    .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Face,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "LATERAL PROFILE & GONIAL BIOMETRICS",
                            style = Typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                color = NeonCyan,
                                letterSpacing = 1.sp
                            )
                        )
                    }
                    Text(
                        text = result.sideProfileSummary,
                        style = Typography.bodyMedium.copy(
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 6-Axis Radar Chart Section
        Text(
            text = "FACIAL HARMONY MATRIX",
            style = Typography.labelSmall.copy(
                letterSpacing = 1.5.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            ),
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceCard)
                .border(1.dp, BorderGlass, RoundedCornerShape(16.dp))
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            RadarMetricChart(metrics = result.metrics, size = 260.dp)
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Detailed Breakdown Section
        Text(
            text = "DETAILED ATTRIBUTE ANALYSIS",
            style = Typography.labelSmall.copy(
                letterSpacing = 1.5.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            ),
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            result.metrics.forEach { metric ->
                AttributeProgressBar(metric = metric)
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Top 3 Ascension Priorities Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    brush = Brush.verticalGradient(
                        listOf(SurfaceCardElevated, SurfaceCard)
                    )
                )
                .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                .padding(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.TipsAndUpdates,
                        contentDescription = null,
                        tint = NeonGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "TOP ASCENSION ROADMAP",
                        style = Typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = TextPrimary,
                            letterSpacing = 1.sp
                        )
                    )
                }

                result.topAscensionFocus.forEachIndexed { idx, tip ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "${idx + 1}.",
                            style = Typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan
                            )
                        )
                        Text(
                            text = tip,
                            style = Typography.bodyMedium.copy(
                                color = TextPrimary,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Action Buttons
        Button(
            onClick = onNavigateToGuides,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = BackgroundDark)
            Spacer(modifier = Modifier.width(8.dp))
            Text("EXPLORE ASCENSION GUIDES", style = Typography.labelLarge.copy(color = BackgroundDark))
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = onNavigateToRoutines,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonPurple),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, NeonPurple),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = NeonPurple)
            Spacer(modifier = Modifier.width(8.dp))
            Text("START DAILY ROUTINES", style = Typography.labelLarge.copy(color = NeonPurple))
        }

        Spacer(modifier = Modifier.height(10.dp))

        TextButton(
            onClick = {
                viewModel.clearImages()
                onRescan()
            }
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("SCAN ANOTHER PROFILE", style = Typography.bodyMedium.copy(color = TextSecondary))
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun QuickStatCard(
    label: String,
    value: String,
    subtext: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceCard)
            .border(1.dp, BorderGlass, RoundedCornerShape(14.dp))
            .padding(10.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = label,
                style = Typography.labelSmall.copy(
                    fontSize = 8.5.sp,
                    letterSpacing = 1.sp,
                    color = TextMuted
                ),
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = Typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 17.sp,
                    color = accentColor
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtext,
                style = Typography.labelSmall.copy(
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                ),
                maxLines = 1
            )
        }
    }
}
