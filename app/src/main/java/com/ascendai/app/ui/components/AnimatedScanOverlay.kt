package com.ascendai.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.ascendai.app.theme.NeonCyan
import com.ascendai.app.theme.NeonPurple

@Composable
fun AnimatedScanOverlay(
    modifier: Modifier = Modifier,
    isScanning: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "scanLaser")
    val laserYRatio by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laserY"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val cornerLen = 28.dp.toPx()
        val strokeW = 3.dp.toPx()

        // 1. Four HUD Corner brackets
        val pad = 20.dp.toPx()
        // Top-left
        drawLine(NeonCyan, Offset(pad, pad), Offset(pad + cornerLen, pad), strokeW)
        drawLine(NeonCyan, Offset(pad, pad), Offset(pad, pad + cornerLen), strokeW)
        // Top-right
        drawLine(NeonCyan, Offset(w - pad, pad), Offset(w - pad - cornerLen, pad), strokeW)
        drawLine(NeonCyan, Offset(w - pad, pad), Offset(w - pad, pad + cornerLen), strokeW)
        // Bottom-left
        drawLine(NeonCyan, Offset(pad, h - pad), Offset(pad + cornerLen, h - pad), strokeW)
        drawLine(NeonCyan, Offset(pad, h - pad), Offset(pad, h - pad - cornerLen), strokeW)
        // Bottom-right
        drawLine(NeonCyan, Offset(w - pad, h - pad), Offset(w - pad - cornerLen, h - pad), strokeW)
        drawLine(NeonCyan, Offset(w - pad, h - pad), Offset(w - pad, h - pad - cornerLen), strokeW)

        // 2. Oval Face Silhouette Guide
        val ovalW = w * 0.65f
        val ovalH = h * 0.60f
        val ovalLeft = (w - ovalW) / 2
        val ovalTop = (h - ovalH) / 2
        drawOval(
            color = NeonCyan.copy(alpha = 0.35f),
            topLeft = Offset(ovalLeft, ovalTop),
            size = Size(ovalW, ovalH),
            style = Stroke(
                width = 1.5.dp.toPx(),
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(12f, 12f))
            )
        )

        // 3. Center Crosshairs
        val center = Offset(w / 2, h / 2)
        val crosshairSize = 10.dp.toPx()
        drawLine(NeonCyan.copy(alpha = 0.5f), Offset(center.x - crosshairSize, center.y), Offset(center.x + crosshairSize, center.y), 1.5.dp.toPx())
        drawLine(NeonCyan.copy(alpha = 0.5f), Offset(center.x, center.y - crosshairSize), Offset(center.x, center.y + crosshairSize), 1.5.dp.toPx())

        // 4. Moving Laser Beam
        if (isScanning) {
            val laserY = h * laserYRatio
            // Laser beam line
            drawLine(
                brush = Brush.horizontalGradient(
                    listOf(
                        Color.Transparent,
                        NeonCyan.copy(alpha = 0.4f),
                        NeonCyan,
                        NeonPurple,
                        NeonCyan,
                        NeonCyan.copy(alpha = 0.4f),
                        Color.Transparent
                    )
                ),
                start = Offset(0f, laserY),
                end = Offset(w, laserY),
                strokeWidth = 2.5.dp.toPx()
            )

            // Laser glow gradient band
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        NeonCyan.copy(alpha = 0.15f),
                        NeonCyan.copy(alpha = 0.0f)
                    ),
                    startY = laserY,
                    endY = laserY + 30.dp.toPx()
                ),
                topLeft = Offset(0f, laserY),
                size = Size(w, 30.dp.toPx())
            )
        }
    }
}
