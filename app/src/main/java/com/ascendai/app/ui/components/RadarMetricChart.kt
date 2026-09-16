package com.ascendai.app.ui.components

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ascendai.app.model.FacialMetric
import com.ascendai.app.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RadarMetricChart(
    metrics: List<FacialMetric>,
    size: Dp = 260.dp,
    modifier: Modifier = Modifier
) {
    if (metrics.isEmpty()) return

    val labels = listOf("Jawline", "Eyes", "Symmetry", "Thirds", "Skin", "Cheeks")
    val scores = metrics.take(6).map { it.score.toFloat() / 100f }
    val displayScores = if (scores.size < 6) {
        scores + List(6 - scores.size) { 0.8f }
    } else scores

    Box(modifier = modifier.size(size)) {
        Canvas(modifier = Modifier.fillMaxSize().padding(28.dp)) {
            val center = Offset(this.size.width / 2, this.size.height / 2)
            val radius = this.size.minDimension / 2.2f
            val numAxes = 6
            val angleStep = (2 * Math.PI / numAxes).toFloat()

            // 1. Draw web grid levels (20%, 40%, 60%, 80%, 100%)
            val gridLevels = listOf(0.25f, 0.50f, 0.75f, 1.0f)
            for (level in gridLevels) {
                val gridPath = Path()
                for (i in 0 until numAxes) {
                    val angle = i * angleStep - (Math.PI / 2).toFloat()
                    val x = center.x + radius * level * cos(angle)
                    val y = center.y + radius * level * sin(angle)
                    if (i == 0) gridPath.moveTo(x, y) else gridPath.lineTo(x, y)
                }
                gridPath.close()
                drawPath(
                    path = gridPath,
                    color = BorderGlass,
                    style = Stroke(width = 1.dp.toPx())
                )
            }

            // 2. Draw axis lines
            for (i in 0 until numAxes) {
                val angle = i * angleStep - (Math.PI / 2).toFloat()
                val endX = center.x + radius * cos(angle)
                val endY = center.y + radius * sin(angle)
                drawLine(
                    color = BorderSubtle,
                    start = center,
                    end = Offset(endX, endY),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // 3. Draw Data Polygon
            val dataPath = Path()
            for (i in 0 until numAxes) {
                val angle = i * angleStep - (Math.PI / 2).toFloat()
                val score = displayScores[i].coerceIn(0.1f, 1.0f)
                val x = center.x + (radius * score) * cos(angle)
                val y = center.y + (radius * score) * sin(angle)
                if (i == 0) dataPath.moveTo(x, y) else dataPath.lineTo(x, y)
            }
            dataPath.close()

            // Fill polygon with neon gradient
            drawPath(
                path = dataPath,
                brush = Brush.radialGradient(
                    colors = listOf(NeonCyan.copy(alpha = 0.45f), NeonPurple.copy(alpha = 0.25f)),
                    center = center,
                    radius = radius
                )
            )

            // Stroke polygon
            drawPath(
                path = dataPath,
                color = NeonCyan,
                style = Stroke(width = 2.5.dp.toPx())
            )

            // Draw vertex dots
            for (i in 0 until numAxes) {
                val angle = i * angleStep - (Math.PI / 2).toFloat()
                val score = displayScores[i].coerceIn(0.1f, 1.0f)
                val x = center.x + (radius * score) * cos(angle)
                val y = center.y + (radius * score) * sin(angle)
                drawCircle(
                    color = NeonCyan,
                    radius = 4.dp.toPx(),
                    center = Offset(x, y)
                )
                drawCircle(
                    color = Color.White,
                    radius = 2.dp.toPx(),
                    center = Offset(x, y)
                )
            }

            // Draw Text Labels on Android native canvas
            val textPaint = Paint().apply {
                color = TextSecondary.toArgb()
                textSize = 28f
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
            }

            for (i in 0 until numAxes) {
                val angle = i * angleStep - (Math.PI / 2).toFloat()
                val labelDistance = radius + 22.dp.toPx()
                val labelX = center.x + labelDistance * cos(angle)
                val labelY = center.y + labelDistance * sin(angle) + 8f
                drawContext.canvas.nativeCanvas.drawText(
                    labels.getOrElse(i) { "" },
                    labelX,
                    labelY,
                    textPaint
                )
            }
        }
    }
}
