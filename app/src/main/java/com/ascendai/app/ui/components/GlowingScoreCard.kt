package com.ascendai.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ascendai.app.theme.*

@Composable
fun GlowingScoreCard(
    score: Int,
    maxScore: Int = 100,
    potentialScore: Int = 92,
    size: Dp = 200.dp,
    modifier: Modifier = Modifier
) {
    var startAnimation by remember { mutableStateOf(false) }
    val animatedProgress by animateFloatAsState(
        targetValue = if (startAnimation) (score.toFloat() / maxScore.toFloat()) else 0f,
        animationSpec = tween(durationMillis = 1200),
        label = "scoreProgress"
    )

    LaunchedEffect(score) {
        startAnimation = true
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size)
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            val strokeWidth = 14.dp.toPx()
            val canvasSize = this.size.minDimension
            val radius = (canvasSize - strokeWidth) / 2
            val center = Offset(this.size.width / 2, this.size.height / 2)

            // Background Track
            drawArc(
                color = SurfaceCardElevated,
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Potential Arc (dotted or faded glow)
            val potentialSweep = (potentialScore.toFloat() / maxScore.toFloat()) * 270f
            drawArc(
                brush = Brush.sweepGradient(
                    listOf(NeonPurple.copy(alpha = 0.35f), NeonPurple.copy(alpha = 0.6f))
                ),
                startAngle = 135f,
                sweepAngle = potentialSweep,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Current Score Arc (Glowing Neon)
            val sweep = animatedProgress * 270f
            drawArc(
                brush = Brush.sweepGradient(
                    listOf(NeonCyan, NeonPurple, NeonCyan)
                ),
                startAngle = 135f,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        // Center Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${(animatedProgress * maxScore).toInt()}",
                style = Typography.displayLarge.copy(
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary
                )
            )
            Text(
                text = "OUT OF 100",
                style = Typography.labelSmall.copy(
                    letterSpacing = 2.sp,
                    color = NeonCyan
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Potential: $potentialScore/100",
                style = Typography.bodyMedium.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NeonPurple
                )
            )
        }
    }
}
