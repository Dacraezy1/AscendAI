package com.ascendai.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TipsAndUpdates
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

        // Quick Biometric Stats Pill Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickStatCard(
                label = "CANTHAL TILT",
                value = String.format("%+.1f°", result.canthalTiltDegrees),
                subtext = if (result.canthalTiltDegrees >= 2.0f) "Hunter Eye" else "Neutral",
                accentColor = NeonCyan,
                modifier = Modifier.weight(1f)
            )
            QuickStatCard(
                label = "SYMMETRY",
                value = "${result.facialSymmetryPct}%",
                subtext = if (result.facialSymmetryPct >= 90) "Elite Match" else "Balanced",
                accentColor = NeonPurple,
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

        Spacer(modifier = Modifier.height(28.dp))

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
            .padding(12.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = label,
                style = Typography.labelSmall.copy(
                    fontSize = 9.sp,
                    letterSpacing = 1.sp,
                    color = TextMuted
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = Typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    color = accentColor
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtext,
                style = Typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )
            )
        }
    }
}
