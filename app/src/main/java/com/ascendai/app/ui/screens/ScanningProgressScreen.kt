package com.ascendai.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ascendai.app.theme.*
import com.ascendai.app.ui.viewmodel.ScanViewModel

@Composable
fun ScanningProgressScreen(
    viewModel: ScanViewModel,
    onAnalysisFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isAnalyzing, uiState.currentResult) {
        if (!uiState.isAnalyzing && uiState.currentResult != null) {
            onAnalysisFinished()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "scanHud")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "hudRotate"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseGlow"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Holographic Target Rings
        Box(
            modifier = Modifier.size(240.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                val radius = size.minDimension / 2.4f

                // Outer rotating dashed ring
                drawCircle(
                    color = NeonCyan.copy(alpha = pulseAlpha),
                    radius = radius,
                    center = center,
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(18f, 14f))
                    )
                )

                // Middle ring
                drawCircle(
                    color = NeonPurple.copy(alpha = 0.5f),
                    radius = radius * 0.75f,
                    center = center,
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                    )
                )

                // Center glowing dot
                drawCircle(
                    color = NeonCyan,
                    radius = 6.dp.toPx(),
                    center = center
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${(uiState.analysisProgress * 100).toInt()}%",
                    style = Typography.displayLarge.copy(
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                )
                Text(
                    text = "NEURAL SCAN",
                    style = Typography.labelSmall.copy(
                        letterSpacing = 2.sp,
                        color = NeonCyan
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Dynamic Step Text
        Text(
            text = uiState.currentStepText.ifEmpty { "Extracting facial geometry..." },
            style = Typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Progress Bar
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(SurfaceCardElevated)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = uiState.analysisProgress.coerceIn(0.02f, 1.0f))
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(NeonCyan, NeonPurple)
                        )
                    )
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Measuring golden ratio, canthal tilt angle, mandible width, and bilateral facial symmetry.",
            style = Typography.bodyMedium.copy(
                fontSize = 12.sp,
                color = TextMuted,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}
