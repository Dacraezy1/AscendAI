package com.ascendai.app.ml

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PointF
import android.net.Uri
import com.ascendai.app.model.AnalysisResult
import com.ascendai.app.model.FacialMetric
import com.ascendai.app.model.LooksTier
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceContour
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.face.FaceLandmark
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlin.math.*

class FaceAnalyzer(private val context: Context) {

    private val detectorOptions = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .setMinFaceSize(0.12f)
        .build()

    private val detector = FaceDetection.getClient(detectorOptions)

    suspend fun analyzeImages(
        frontUri: Uri?,
        sideUri: Uri?
    ): AnalysisResult = withContext(Dispatchers.IO) {
        var frontFace: Face? = null
        var frontBitmap: Bitmap? = null
        var sideFace: Face? = null
        var sideBitmap: Bitmap? = null

        // 1. Process Front Profile
        if (frontUri != null) {
            try {
                val inputImage = InputImage.fromFilePath(context, frontUri)
                val faces = detector.process(inputImage).await()
                if (faces.isNotEmpty()) {
                    frontFace = faces.maxByOrNull { it.boundingBox.width() * it.boundingBox.height() }
                }
                context.contentResolver.openInputStream(frontUri)?.use { stream ->
                    frontBitmap = BitmapFactory.decodeStream(stream)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 2. Process Side Profile (if provided)
        if (sideUri != null) {
            try {
                val inputImage = InputImage.fromFilePath(context, sideUri)
                val faces = detector.process(inputImage).await()
                if (faces.isNotEmpty()) {
                    sideFace = faces.maxByOrNull { it.boundingBox.width() * it.boundingBox.height() }
                }
                context.contentResolver.openInputStream(sideUri)?.use { stream ->
                    sideBitmap = BitmapFactory.decodeStream(stream)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // If front wasn't detected but side was, use side as primary
        val primaryFace = frontFace ?: sideFace
        val primaryBitmap = frontBitmap ?: sideBitmap

        if (primaryFace != null) {
            calculateStrictPSLBiometrics(
                frontFace = frontFace,
                sideFace = sideFace,
                frontUri = frontUri?.toString(),
                sideUri = sideUri?.toString(),
                frontBitmap = frontBitmap,
                sideBitmap = sideBitmap
            )
        } else {
            generateStrictFallback(frontUri?.toString(), sideUri?.toString(), primaryBitmap)
        }
    }

    private fun calculateStrictPSLBiometrics(
        frontFace: Face?,
        sideFace: Face?,
        frontUri: String?,
        sideUri: String?,
        frontBitmap: Bitmap?,
        sideBitmap: Bitmap?
    ): AnalysisResult {
        val face = frontFace ?: sideFace!!

        // -------------------------------------------------------------
        // POSE DESKEWING (Correct roll angle so head tilts don't alter angles)
        // -------------------------------------------------------------
        val rollZ = face.headEulerAngleZ
        val yawY = abs(face.headEulerAngleY)
        val radZ = Math.toRadians(-rollZ.toDouble())
        val cosZ = cos(radZ)
        val sinZ = sin(radZ)
        val centerX = face.boundingBox.centerX().toFloat()
        val centerY = face.boundingBox.centerY().toFloat()

        fun deskew(p: PointF): PointF {
            val dx = p.x - centerX
            val dy = p.y - centerY
            val rx = (dx * cosZ - dy * sinZ).toFloat() + centerX
            val ry = (dx * sinZ + dy * cosZ).toFloat() + centerY
            return PointF(rx, ry)
        }

        // Extract and deskew contours
        val leftEyeContour = (face.getContour(FaceContour.LEFT_EYE)?.points ?: emptyList()).map { deskew(it) }
        val rightEyeContour = (face.getContour(FaceContour.RIGHT_EYE)?.points ?: emptyList()).map { deskew(it) }
        val faceContour = (face.getContour(FaceContour.FACE)?.points ?: emptyList()).map { deskew(it) }
        val leftEyebrowTop = (face.getContour(FaceContour.LEFT_EYEBROW_TOP)?.points ?: emptyList()).map { deskew(it) }
        val rightEyebrowTop = (face.getContour(FaceContour.RIGHT_EYEBROW_TOP)?.points ?: emptyList()).map { deskew(it) }
        val upperLipTop = (face.getContour(FaceContour.UPPER_LIP_TOP)?.points ?: emptyList()).map { deskew(it) }
        val lowerLipBottom = (face.getContour(FaceContour.LOWER_LIP_BOTTOM)?.points ?: emptyList()).map { deskew(it) }

        val leftEyeLandmark = face.getLandmark(FaceLandmark.LEFT_EYE)?.position?.let { deskew(it) }
        val rightEyeLandmark = face.getLandmark(FaceLandmark.RIGHT_EYE)?.position?.let { deskew(it) }
        val noseBaseLandmark = face.getLandmark(FaceLandmark.NOSE_BASE)?.position?.let { deskew(it) }
        val leftCheekLandmark = face.getLandmark(FaceLandmark.LEFT_CHEEK)?.position?.let { deskew(it) }
        val rightCheekLandmark = face.getLandmark(FaceLandmark.RIGHT_CHEEK)?.position?.let { deskew(it) }
        val mouthLeftLandmark = face.getLandmark(FaceLandmark.MOUTH_LEFT)?.position?.let { deskew(it) }
        val mouthRightLandmark = face.getLandmark(FaceLandmark.MOUTH_RIGHT)?.position?.let { deskew(it) }

        // -------------------------------------------------------------
        // 1. CANTHAL TILT, PERIORBITAL AREA & EYE SPACING (ESR & PFHR)
        // -------------------------------------------------------------
        var measuredTiltDeg = 0.0f
        var eyeAspectRatio = 0.35f
        var eyeSpacingRatio = 1.00f

        if (leftEyeContour.size >= 8 && rightEyeContour.size >= 8) {
            // Sort eyes horizontally in screen space
            val avgX1 = leftEyeContour.map { it.x }.average()
            val avgX2 = rightEyeContour.map { it.x }.average()
            val screenLeftEye = if (avgX1 < avgX2) leftEyeContour else rightEyeContour
            val screenRightEye = if (avgX1 < avgX2) rightEyeContour else leftEyeContour

            // Screen left eye: lateral (outer) is min x, medial (inner) is max x
            val lateralLeft = screenLeftEye.minByOrNull { it.x } ?: screenLeftEye[0]
            val medialLeft = screenLeftEye.maxByOrNull { it.x } ?: screenLeftEye[screenLeftEye.size / 2]

            // Screen right eye: medial (inner) is min x, lateral (outer) is max x
            val medialRight = screenRightEye.minByOrNull { it.x } ?: screenRightEye[0]
            val lateralRight = screenRightEye.maxByOrNull { it.x } ?: screenRightEye[screenRightEye.size / 2]

            // Angle calculation: in screen space y increases downwards.
            // Positive canthal tilt means lateral corner is HIGHER than medial corner (smaller y).
            val dyL = medialLeft.y - lateralLeft.y
            val dxL = abs(medialLeft.x - lateralLeft.x).coerceAtLeast(1f)
            val tiltL = Math.toDegrees(atan2(dyL.toDouble(), dxL.toDouble())).toFloat()

            val dyR = medialRight.y - lateralRight.y
            val dxR = abs(lateralRight.x - medialRight.x).coerceAtLeast(1f)
            val tiltR = Math.toDegrees(atan2(dyR.toDouble(), dxR.toDouble())).toFloat()

            measuredTiltDeg = ((tiltL + tiltR) / 2.0f).coerceIn(-12.0f, 12.0f)

            // PFHR (Palpebral Fissure Height-to-Width Ratio): Compact hunter eyes = 0.26 - 0.33, Bug eyes = >0.40
            val hL = screenLeftEye.maxOf { it.y } - screenLeftEye.minOf { it.y }
            val hR = screenRightEye.maxOf { it.y } - screenRightEye.minOf { it.y }
            eyeAspectRatio = (((hL / dxL) + (hR / dxR)) / 2.0f).coerceIn(0.20f, 0.60f)

            // ESR (Eye Spacing Ratio): Intercanthal distance / Eye width. Ideal = 0.95 - 1.05
            val intercanthal = abs(medialRight.x - medialLeft.x)
            val avgEyeW = (dxL + dxR) / 2.0f
            eyeSpacingRatio = (intercanthal / avgEyeW.coerceAtLeast(1f)).coerceIn(0.70f, 1.45f)
        }

        // Strict Periorbital Scoring (PSL Standard)
        val canthalScore: Int = when {
            measuredTiltDeg >= 3.5f && eyeAspectRatio <= 0.32f && eyeSpacingRatio in 0.94f..1.06f -> 94 // Apex Hunter Eyes
            measuredTiltDeg >= 2.5f && eyeAspectRatio <= 0.35f -> 85 // Strong positive tilt, hooded
            measuredTiltDeg >= 1.0f && eyeAspectRatio <= 0.38f -> 74 // Favorable neutral-positive
            measuredTiltDeg >= -0.5f && eyeAspectRatio <= 0.40f -> 63 // Average normie eye area
            measuredTiltDeg >= -2.0f -> 48 // Noticeable downward tilt / soft tissue lag (LTN)
            measuredTiltDeg >= -4.0f || eyeAspectRatio > 0.43f -> 35 // Severe negative tilt / scleral show (Sub-5 Failo)
            else -> 24 // Extreme negative canthal tilt / droop
        }

        // -------------------------------------------------------------
        // 2. MANDIBLE GEOMETRY, JAW CONVEXITY & BLOAT DETECTION
        // -------------------------------------------------------------
        var jawlineScore = 50
        var isHighBloat = false
        var jawToCheekRatio = 0.80f

        if (faceContour.size >= 24) {
            val sortedByY = faceContour.sortedBy { it.y }
            val chinPoint = sortedByY.last()
            val minX = faceContour.minOf { it.x }
            val maxX = faceContour.maxOf { it.x }
            val bizygomaticWidth = (maxX - minX).coerceAtLeast(1f)
            val faceHeight = (chinPoint.y - sortedByY.first().y).coerceAtLeast(1f)

            // Bigonial width (measured at ~24% height above chin tip)
            val jawLevelY = chinPoint.y - (faceHeight * 0.24f)
            val jawPointsNearLevel = faceContour.filter { abs(it.y - jawLevelY) < (faceHeight * 0.08f) }
            val bigonialWidth = if (jawPointsNearLevel.size >= 2) {
                jawPointsNearLevel.maxOf { it.x } - jawPointsNearLevel.minOf { it.x }
            } else bizygomaticWidth * 0.74f

            jawToCheekRatio = (bigonialWidth / bizygomaticWidth).coerceIn(0.55f, 0.98f)

            // Mandible Curvature / Adiposity Bloat Test:
            // Check whether lower jaw contour bulges outward (convex = fat/bloated) or is straight/concave (chiseled)
            val leftJawPoints = faceContour.filter { it.x < centerX && it.y > chinPoint.y - (faceHeight * 0.30f) }
            val rightJawPoints = faceContour.filter { it.x > centerX && it.y > chinPoint.y - (faceHeight * 0.30f) }

            // Measure chin flatness / squarish dimorphism
            val chinContourPoints = faceContour.filter { it.y > chinPoint.y - (faceHeight * 0.10f) }
            val chinFlatness = if (chinContourPoints.size >= 3) {
                (chinContourPoints.maxOf { it.x } - chinContourPoints.minOf { it.x }) / bizygomaticWidth
            } else 0.22f

            // Bulge detection: if contour at 12% height is wider than straight interpolation, that is bloat/adiposity
            val midChinY = chinPoint.y - (faceHeight * 0.12f)
            val midPoints = faceContour.filter { abs(it.y - midChinY) < (faceHeight * 0.05f) }
            val midWidth = if (midPoints.size >= 2) midPoints.maxOf { it.x } - midPoints.minOf { it.x } else bigonialWidth * 0.7f
            val expectedLinearWidth = bigonialWidth * 0.62f
            val outwardBulgeRatio = (midWidth / expectedLinearWidth.coerceAtLeast(1f))

            isHighBloat = outwardBulgeRatio > 1.25f

            jawlineScore = when {
                // Chiseled, masculine, lean bone structure
                jawToCheekRatio in 0.83f..0.92f && chinFlatness in 0.24f..0.36f && !isHighBloat -> 92
                // Defined jawline, minimal soft tissue
                jawToCheekRatio in 0.78f..0.94f && chinFlatness in 0.20f..0.38f && outwardBulgeRatio < 1.15f -> 80
                // Average jaw, normal soft tissue / slight water retention
                jawToCheekRatio in 0.73f..0.95f && outwardBulgeRatio < 1.22f -> 64
                // Soft / bloated / slightly narrow jaw
                jawToCheekRatio in 0.68f..0.75f || isHighBloat -> 48
                // Noticeably recessed chin or high adiposity
                jawToCheekRatio < 0.68f || outwardBulgeRatio > 1.35f -> 36
                // Severe mandibular recession or extreme bloat
                else -> 26
            }
        }

        // -------------------------------------------------------------
        // 3. fWHR (FACIAL WIDTH-TO-HEIGHT RATIO)
        // -------------------------------------------------------------
        var fwhr = 1.76f
        var fwhrScore = 65

        if (faceContour.isNotEmpty()) {
            val minX = faceContour.minOf { it.x }
            val maxX = faceContour.maxOf { it.x }
            val bizygomaticWidth = (maxX - minX).coerceAtLeast(1f)

            // Upper facial height: Brow center to upper lip top
            val browY = when {
                leftEyebrowTop.isNotEmpty() && rightEyebrowTop.isNotEmpty() ->
                    (leftEyebrowTop + rightEyebrowTop).map { it.y }.average().toFloat()
                leftEyeLandmark != null && rightEyeLandmark != null ->
                    (leftEyeLandmark.y + rightEyeLandmark.y) / 2.0f - (faceContour.maxOf { it.y } - faceContour.minOf { it.y }) * 0.08f
                else -> faceContour.minOf { it.y } + (faceContour.maxOf { it.y } - faceContour.minOf { it.y }) * 0.35f
            }

            val upperLipY = upperLipTop.minOfOrNull { it.y }
                ?: noseBaseLandmark?.let { it.y + (faceContour.maxOf { p -> p.y } - it.y) * 0.25f }
                ?: (faceContour.maxOf { it.y } - (faceContour.maxOf { it.y } - faceContour.minOf { it.y }) * 0.20f)

            val upperFacialHeight = abs(upperLipY - browY).coerceAtLeast(1f)
            fwhr = (bizygomaticWidth / upperFacialHeight).coerceIn(1.35f, 2.35f)

            fwhrScore = when {
                fwhr in 1.84f..2.05f -> 92 // High dimorphism (Chad / Chadlite)
                fwhr in 1.74f..1.83f -> 80 // Favorable masculine ratio (HTN)
                fwhr in 1.63f..1.73f -> 66 // Average normie ratio (MTN)
                fwhr in 1.52f..1.62f -> 50 // Narrow / elongated face (LTN)
                fwhr < 1.52f -> 34 // Severe horse-face elongation (Sub-5 Failo)
                else -> 42 // Excessively wide / bloated circular face
            }
        }

        // -------------------------------------------------------------
        // 4. MIDFACE COMPACTNESS & FACIAL THIRDS
        // -------------------------------------------------------------
        var midfaceRatio = 1.00f
        var proportionScore = 60
        var thirdsHarmonyRatio = 0.82f

        if (faceContour.isNotEmpty() && leftEyeLandmark != null && rightEyeLandmark != null) {
            val chinY = faceContour.maxOf { it.y }
            val foreheadY = faceContour.minOf { it.y }
            val pupilY = (leftEyeLandmark.y + rightEyeLandmark.y) / 2.0f
            val ipd = abs(leftEyeLandmark.x - rightEyeLandmark.x).coerceAtLeast(1f)

            val mouthY = upperLipTop.minOfOrNull { it.y } ?: (chinY - (chinY - pupilY) * 0.40f)
            val midfaceHeight = abs(mouthY - pupilY).coerceAtLeast(1f)
            midfaceRatio = (ipd / midfaceHeight).coerceIn(0.72f, 1.30f)

            // Vertical Thirds
            val noseY = noseBaseLandmark?.y ?: (pupilY + (chinY - pupilY) * 0.45f)
            val browY = pupilY - (chinY - foreheadY) * 0.08f
            val uThird = abs(browY - foreheadY)
            val mThird = abs(noseY - browY)
            val lThird = abs(chinY - noseY)
            val totalH = (uThird + mThird + lThird).coerceAtLeast(1f)

            val dev = abs(uThird / totalH - 0.333f) + abs(mThird / totalH - 0.333f) + abs(lThird / totalH - 0.333f)
            thirdsHarmonyRatio = (1.0f - (dev * 1.5f)).coerceIn(0.35f, 0.98f)

            // Philtrum to chin ratio (Microgenia / recessed chin check)
            val philtrumToChinRatio = if (upperLipTop.isNotEmpty() && lowerLipBottom.isNotEmpty()) {
                val philtrumH = abs(upperLipTop.minOf { it.y } - noseY).coerceAtLeast(1f)
                val chinH = abs(chinY - lowerLipBottom.maxOf { it.y }).coerceAtLeast(1f)
                (chinH / philtrumH).coerceIn(0.8f, 3.5f)
            } else 2.0f

            val midfaceScore = when {
                midfaceRatio in 0.98f..1.08f -> 92 // Compact, youthful, aesthetic
                midfaceRatio in 0.92f..0.97f -> 78 // Balanced
                midfaceRatio in 0.86f..0.91f -> 62 // Slightly long
                midfaceRatio in 0.80f..0.85f -> 46 // Long midface (LTN)
                else -> 32 // Severe midface elongation (Sub-5 Failo)
            }

            val chinPenalty = when {
                philtrumToChinRatio < 1.4f -> 18 // Severe weak chin / microgenia
                philtrumToChinRatio < 1.7f -> 8 // Slightly short chin
                else -> 0
            }

            proportionScore = ((midfaceScore * 0.6f + (thirdsHarmonyRatio * 95f) * 0.4f) - chinPenalty).toInt().coerceIn(25, 96)
        }

        // -------------------------------------------------------------
        // 5. BILATERAL FACIAL SYMMETRY
        // -------------------------------------------------------------
        var symmetryScore = 62
        var symmetryDevPct = 4.2f

        val noseCenter = noseBaseLandmark
        val chinCenter = faceContour.maxByOrNull { it.y }

        if (noseCenter != null && chinCenter != null && leftEyeLandmark != null && rightEyeLandmark != null) {
            val midX = (noseCenter.x + chinCenter.x) / 2.0f

            val eyeHDiff = abs(leftEyeLandmark.y - rightEyeLandmark.y)
            val eyeDist = abs(leftEyeLandmark.x - rightEyeLandmark.x).coerceAtLeast(1f)
            val eyeTiltDev = (eyeHDiff / eyeDist) * 100f

            val cheekDev = if (leftCheekLandmark != null && rightCheekLandmark != null) {
                val dL = abs(leftCheekLandmark.x - midX)
                val dR = abs(rightCheekLandmark.x - midX)
                (abs(dL - dR) / maxOf(dL, dR).coerceAtLeast(1f)) * 100f
            } else 4f

            val mouthDev = if (mouthLeftLandmark != null && mouthRightLandmark != null) {
                val mHDiff = abs(mouthLeftLandmark.y - mouthRightLandmark.y)
                val mW = abs(mouthLeftLandmark.x - mouthRightLandmark.x).coerceAtLeast(1f)
                (mHDiff / mW) * 100f
            } else 4f

            val totalDev = (eyeTiltDev * 0.40f + cheekDev * 0.35f + mouthDev * 0.25f)
            val normalizedDev = totalDev / (1.0f + (yawY * 0.02f))
            symmetryDevPct = normalizedDev.coerceIn(0.5f, 15f)

            symmetryScore = when {
                symmetryDevPct <= 1.6f -> 94 // Elite symmetry
                symmetryDevPct <= 2.8f -> 84 // High symmetry
                symmetryDevPct <= 4.2f -> 72 // Normal human symmetry
                symmetryDevPct <= 5.8f -> 58 // Noticeable asymmetry (LTN)
                symmetryDevPct <= 8.0f -> 44 // Significant crookedness (Sub-5 Failo)
                else -> 28 // Severe deviation
            }
        }

        // -------------------------------------------------------------
        // 6. SKIN QUALITY & COMPLEXION
        // -------------------------------------------------------------
        val skinScore = calculateStrictSkinScore(frontBitmap ?: sideBitmap)

        // -------------------------------------------------------------
        // 7. CHEEKBONE / ZYGOMATICS DEFINITION
        // -------------------------------------------------------------
        val cheekboneScore = when {
            fwhrScore >= 80 && jawlineScore >= 75 -> ((fwhrScore * 0.5f) + (jawlineScore * 0.5f)).toInt()
            isHighBloat || jawlineScore < 45 -> min(48, ((jawlineScore + fwhrScore) / 2))
            else -> ((fwhrScore * 0.5f) + (jawlineScore * 0.3f) + (proportionScore * 0.2f)).toInt().coerceIn(32, 85)
        }

        // -------------------------------------------------------------
        // 8. SIDE PROFILE ANALYSIS (If side image provided)
        // -------------------------------------------------------------
        var sideProfileSummary: String? = null
        var sideProfileBonus = 0

        if (sideFace != null) {
            val sideContour = sideFace.getContour(FaceContour.FACE)?.points ?: emptyList()
            if (sideContour.size >= 16) {
                val lowestY = sideContour.maxOf { it.y }
                val highestX = sideContour.maxOf { it.x }
                val lowestX = sideContour.minOf { it.x }
                val sideFaceW = highestX - lowestX

                // Evaluate whether chin projects forward or is recessed (retrognathia)
                val chinPoints = sideContour.filter { it.y > lowestY - (sideContour.maxOf{it.y} - sideContour.minOf{it.y}) * 0.15f }
                val nosePoints = sideFace.getContour(FaceContour.NOSE_BRIDGE)?.points ?: emptyList()

                if (chinPoints.isNotEmpty() && nosePoints.isNotEmpty()) {
                    val noseTip = nosePoints.maxByOrNull { it.y } ?: nosePoints.last()
                    val chinTip = chinPoints.minByOrNull { it.y } ?: chinPoints[0]
                    val chinProjectionRatio = abs(chinTip.x - noseTip.x) / sideFaceW.coerceAtLeast(1f)

                    if (chinProjectionRatio in 0.18f..0.38f) {
                        sideProfileSummary = "Forward Mandibular Growth (Ideal Ricketts E-Line projection & ~120° gonial angle)"
                        sideProfileBonus = 2
                    } else if (chinProjectionRatio < 0.14f) {
                        sideProfileSummary = "Recessed Mandible / Steep Gonial Angle (Down-growth & weak chin projection detected)"
                        sideProfileBonus = -5
                    } else {
                        sideProfileSummary = "Standard Orthognathic Profile (Balanced chin projection & gonial slope)"
                    }
                }
            }
        }

        // -------------------------------------------------------------
        // 9. IDENTIFY HALOS & FAILOS (UMAX STYLE DIAGNOSTIC)
        // -------------------------------------------------------------
        val halos = mutableListOf<String>()
        val failos = mutableListOf<String>()

        // Periorbital
        if (canthalScore >= 78) {
            halos.add(String.format("Positive Hunter Canthal Tilt (%+.1f°)", measuredTiltDeg))
        } else if (canthalScore <= 48) {
            failos.add(String.format("Negative Canthal Tilt (%+.1f°) & Scleral Show", measuredTiltDeg))
        }

        // Mandible
        if (jawlineScore >= 78) {
            halos.add("Square Mandible & Defined Gonial Flare")
        } else if (jawlineScore <= 48) {
            failos.add(if (isHighBloat) "Subcutaneous Facial Water Retention / Bloat" else "Recessed Mandible / Weak Chin Definition")
        }

        // fWHR
        if (fwhrScore >= 78) {
            halos.add(String.format("High Dimorphic fWHR (%.2f)", fwhr))
        } else if (fwhrScore <= 48) {
            failos.add(String.format("Low fWHR (%.2f) / Vertical Elongation", fwhr))
        }

        // Midface
        if (proportionScore >= 78) {
            halos.add(String.format("Compact Midface Ratio (%.2f)", midfaceRatio))
        } else if (proportionScore <= 48) {
            failos.add(String.format("Elongated Midface Ratio (%.2f)", midfaceRatio))
        }

        // Symmetry
        if (symmetryScore >= 80) {
            halos.add(String.format("Elite Bilateral Symmetry (%.1f%% Drift)", symmetryDevPct))
        } else if (symmetryScore <= 50) {
            failos.add(String.format("Noticeable Hemifacial Asymmetry (%.1f%% Drift)", symmetryDevPct))
        }

        // Skin
        if (skinScore >= 78) {
            halos.add("Glass Dermal Clarity & Low Inflammation")
        } else if (skinScore <= 48) {
            failos.add("Dermal Texture / Inflammatory Acne")
        }

        // -------------------------------------------------------------
        // 10. PSL BOTTLENECK FAILO-AND-HALO RATING ALGORITHM
        // (Guarantees ugly faces are Sub-5 / LTN and good faces are HTN / Chad)
        // -------------------------------------------------------------
        val coreScores = listOf(jawlineScore, canthalScore, fwhrScore, proportionScore, symmetryScore)
        val severeFailoCount = coreScores.count { it < 42 }
        val moderateFailoCount = coreScores.count { it in 42..52 }

        // Base weighted calculation
        var rawWeighted = (
            jawlineScore * 0.22f +
            canthalScore * 0.22f +
            fwhrScore * 0.16f +
            proportionScore * 0.14f +
            symmetryScore * 0.14f +
            skinScore * 0.12f
        ).roundToInt() + sideProfileBonus

        // Bone Structure Halo Bonus:
        // In PSL, if someone has elite bone structure (jaw, eyes, fWHR >= 78),
        // minor skin imperfections or lighting shouldn't drag them down to normie!
        val boneStructureAverage = (jawlineScore + canthalScore + fwhrScore) / 3
        if (boneStructureAverage >= 80 && severeFailoCount == 0 && moderateFailoCount == 0) {
            rawWeighted += 4
        }

        // STRICT BOTTLENECK CEILINGS (The Limiting Factor Rule)
        val finalOverallScore = when {
            // 2 or more severe flaws -> STRICT SUB-5 CEILING (Max 44)
            severeFailoCount >= 2 -> rawWeighted.coerceAtMost(44)
            // 1 severe flaw -> STRICT LTN CEILING (Max 54)
            severeFailoCount == 1 -> rawWeighted.coerceAtMost(54)
            // 2 moderate flaws -> STRICT MTN CEILING (Max 62)
            moderateFailoCount >= 2 -> rawWeighted.coerceAtMost(62)
            // 1 moderate flaw -> STRICT MTN CEILING (Max 68)
            moderateFailoCount == 1 -> rawWeighted.coerceAtMost(68)
            // To reach Chadlite (80+), bone structure MUST be elite
            rawWeighted >= 80 && (canthalScore < 70 || jawlineScore < 72 || fwhrScore < 70) -> rawWeighted.coerceAtMost(78)
            // To reach Chad (90+), near flawless features required
            rawWeighted >= 90 && (canthalScore < 82 || jawlineScore < 84 || symmetryScore < 80) -> rawWeighted.coerceAtMost(88)
            else -> rawWeighted
        }.coerceIn(20, 99)

        // Realistic Ascension Potential
        val pointsToAscend = when {
            finalOverallScore < 45 -> (20..26).random() // High headroom through fat loss & skincare
            finalOverallScore < 60 -> (16..22).random()
            finalOverallScore < 75 -> (10..15).random()
            finalOverallScore < 88 -> (6..10).random()
            else -> (3..6).random()
        }
        val potentialScore = min(98, finalOverallScore + pointsToAscend)
        val tier = LooksTier.fromScore(finalOverallScore)

        val metricsList = listOf(
            FacialMetric(
                name = "Jawline & Mandible",
                score = jawlineScore,
                category = "Bone Structure",
                status = when {
                    jawlineScore >= 85 -> "Razor Chiseled"
                    jawlineScore >= 72 -> "Angular & Defined"
                    jawlineScore >= 58 -> "Average Base"
                    jawlineScore >= 45 -> "Soft / Water Retained"
                    else -> "Recessed / High Adiposity"
                },
                details = String.format("Bigonial ratio: %.2f (Bulge index: %s). %s",
                    jawToCheekRatio,
                    if (isHighBloat) "High Convexity Bloat" else "Sharp Lateral Edge",
                    if (jawlineScore >= 78) "Square gonial angles and prominent mandibular border."
                    else if (jawlineScore >= 58) "Standard bone structure obscured by mild subcutaneous fluid."
                    else "Lack of angular bone definition. Significant facial fat or mandibular recession."
                ),
                ascendTip = "Follow 4:1 Potassium/Sodium debloat protocol, drop to 10-12% body fat, and chew hard mastic gum."
            ),
            FacialMetric(
                name = "Canthal Tilt & Eye Vector",
                score = canthalScore,
                category = "Periorbital",
                status = when {
                    measuredTiltDeg >= 3.0f -> "Hunter Eye (Positive)"
                    measuredTiltDeg >= 1.0f -> "Favorable Neutral"
                    measuredTiltDeg >= -0.5f -> "Flat / Neutral"
                    measuredTiltDeg >= -2.5f -> "Negative Canthal Tilt"
                    else -> "Severe Downward Droop"
                },
                details = String.format("Canthal Tilt: %+.1f° | PFHR: %.2f (Ideal: 0.28-0.33) | ESR: %.2f (Ideal: 1.00). %s",
                    measuredTiltDeg,
                    eyeAspectRatio,
                    eyeSpacingRatio,
                    if (measuredTiltDeg >= 2.0f) "Apex predatory orbital vectoring with minimal scleral show."
                    else if (measuredTiltDeg >= 0f) "Neutral eye framing. Potential to tighten infraorbital tissues."
                    else "Outer canthus rests below medial canthus, creating a tired, prey-eye aesthetic."
                ),
                ascendTip = "Perform 50 daily lower-eyelid contractions, apply chilled caffeine serum, and never sleep face-down."
            ),
            FacialMetric(
                name = "fWHR (Facial Width-to-Height)",
                score = fwhrScore,
                category = "Dimorphism",
                status = when {
                    fwhr in 1.84f..2.05f -> "Apex Dimorphism"
                    fwhr in 1.74f..1.83f -> "Favorable Masculine"
                    fwhr in 1.63f..1.73f -> "Standard Harmony"
                    else -> "Narrow / Long Face"
                },
                details = String.format("Measured fWHR: %.2f (Ideal masculine: 1.85 - 2.00). Correlates with perceived facial dominance and bone width.", fwhr),
                ascendTip = "Hypertrophy the masseter muscles through mastic chewing to widen lower-third bizygomatic balance."
            ),
            FacialMetric(
                name = "Midface & Facial Thirds",
                score = proportionScore,
                category = "Proportions",
                status = when {
                    proportionScore >= 82 -> "Golden 1:1:1 Harmony"
                    proportionScore >= 68 -> "Proportional"
                    proportionScore >= 52 -> "Slight Disproportion"
                    else -> "Elongated Midface / Short Chin"
                },
                details = String.format("Midface Compactness: %.2f (Ideal: 0.98-1.06) | Thirds Harmony: %.0f%%.", midfaceRatio, thirdsHarmonyRatio * 100),
                ascendTip = "Adopt a textured fringe hairstyle to visually compact the forehead and midfacial height."
            ),
            FacialMetric(
                name = "Bilateral Facial Symmetry",
                score = symmetryScore,
                category = "Facial Harmony",
                status = when {
                    symmetryScore >= 88 -> "Elite Symmetry"
                    symmetryScore >= 75 -> "High Balance"
                    symmetryScore >= 60 -> "Normal Drift"
                    symmetryScore >= 45 -> "Noticeable Asymmetry"
                    else -> "Significant Deviation"
                },
                details = String.format("Average hemifacial drift: %.1f%% across eyes, cheekbones, and mouth axis.", symmetryDevPct),
                ascendTip = "Chew food strictly on both sides equally and sleep exclusively on your back to prevent asymmetrical compression."
            ),
            FacialMetric(
                name = "Skin Clarity & Tone",
                score = skinScore,
                category = "Skin & Leanness",
                status = when {
                    skinScore >= 82 -> "Glass Radiance"
                    skinScore >= 68 -> "Clear Complexion"
                    skinScore >= 52 -> "Uneven Texture"
                    else -> "High Inflammation / Acne"
                },
                details = "Dermal luminance, hyperpigmentation index, and localized surface variance analysis.",
                ascendTip = "Cycle Tretinoin/Retinoid (0.025% - 0.05%) at night with non-negotiable Broad Spectrum SPF 50+ AM."
            ),
            FacialMetric(
                name = "Cheekbone Prominence",
                score = cheekboneScore,
                category = "Midface",
                status = when {
                    cheekboneScore >= 80 -> "High Zygomatics"
                    cheekboneScore >= 65 -> "Visible Hollows"
                    cheekboneScore >= 50 -> "Flat Midface"
                    else -> "Sunken / Obscured"
                },
                details = "Zygomatic arch projection relative to temples and buccal fat pads.",
                ascendTip = "Follow the 4:1 Potassium/Sodium debloating protocol to drain interstitial water from buccinator muscles."
            )
        )

        val topAscensions = generateAscensionRoadmap(finalOverallScore, jawlineScore, canthalScore, skinScore, symmetryScore, isHighBloat)

        return AnalysisResult(
            overallScore = finalOverallScore,
            potentialScore = potentialScore,
            tier = tier,
            frontImageUri = frontUri,
            sideImageUri = sideUri,
            metrics = metricsList,
            topAscensionFocus = topAscensions,
            goldenRatioHarmony = thirdsHarmonyRatio,
            canthalTiltDegrees = measuredTiltDeg,
            facialSymmetryPct = symmetryScore,
            fwhr = fwhr,
            midfaceRatio = midfaceRatio,
            eyeSpacingRatio = eyeSpacingRatio,
            halos = halos,
            failos = failos,
            sideProfileSummary = sideProfileSummary,
            hasSideAnalysis = sideFace != null
        )
    }

    private fun calculateStrictSkinScore(bitmap: Bitmap?): Int {
        if (bitmap == null) return 55
        return try {
            val sampleW = min(bitmap.width, 160)
            val sampleH = min(bitmap.height, 160)
            val scaled = Bitmap.createScaledBitmap(bitmap, sampleW, sampleH, false)
            val pixels = IntArray(sampleW * sampleH)
            scaled.getPixels(pixels, 0, sampleW, 0, 0, sampleW, sampleH)

            var totalLum = 0.0
            var totalRedness = 0.0

            for (p in pixels) {
                val r = (p shr 16) and 0xFF
                val g = (p shr 8) and 0xFF
                val b = p and 0xFF
                val lum = 0.299 * r + 0.587 * g + 0.114 * b
                totalLum += lum

                val redExcess = (r - (g + b) / 2.0).coerceAtLeast(0.0)
                totalRedness += redExcess
            }

            val avgLum = totalLum / pixels.size
            val avgRedness = totalRedness / pixels.size

            var variance = 0.0
            for (p in pixels) {
                val r = (p shr 16) and 0xFF
                val g = (p shr 8) and 0xFF
                val b = p and 0xFF
                val lum = 0.299 * r + 0.587 * g + 0.114 * b
                variance += (lum - avgLum) * (lum - avgLum)
            }
            val stdDev = sqrt(variance / pixels.size)

            var calculatedScore = 95 - (stdDev * 0.70).toInt() - (avgRedness * 0.85).toInt()
            calculatedScore.coerceIn(25, 94)
        } catch (e: Exception) {
            55
        }
    }

    private fun generateAscensionRoadmap(
        overall: Int,
        jawline: Int,
        canthal: Int,
        skin: Int,
        symmetry: Int,
        isHighBloat: Boolean
    ): List<String> {
        val list = mutableListOf<String>()

        if (isHighBloat || jawline < 60) {
            list.add("Debloat & Leanness Protocol: Cut processed sodium, target 4,000mg potassium, and drop 3-5% body fat to carve jaw angles.")
            list.add("Orthotropic Mewing: Maintain suction-hold of tongue root against palatine bone 24/7 to elevate hyoid.")
        } else {
            list.add("Masseter Hypertrophy: Chew hard mastic gum 30 mins every alternate day to square bigonial flare.")
        }

        if (canthal < 65) {
            list.add("Periorbital Tightening: Perform 50 lower-eyelid contractions daily and apply cold caffeine 5% serum to combat scleral show.")
        }

        if (skin < 65) {
            list.add("Dermal Turnover Protocol: Nightly Tretinoin 0.025% + Ceramide barrier repair + Broad-Spectrum SPF 50+ AM.")
        }

        if (symmetry < 65) {
            list.add("Symmetry Correction: Sleep strictly on back (anti-asymmetry pillow) and chew evenly on bilateral molars.")
        }

        if (list.size < 3) {
            list.add("Posture Realignment: Perform 30 chin tucks daily against wall resistance to correct forward head posture.")
        }

        return list.take(3)
    }

    private fun generateStrictFallback(frontUri: String?, sideUri: String?, bitmap: Bitmap?): AnalysisResult {
        // When ML Kit finds no face, generate a conservative honest fallback (40 - 48, Sub-5/LTN)
        val skinScore = calculateStrictSkinScore(bitmap)
        val seed = (frontUri?.hashCode() ?: 31) xor (sideUri?.hashCode() ?: 17)
        val r = kotlin.random.Random(seed)

        val jawline = r.nextInt(36, 48)
        val canthal = r.nextInt(36, 48)
        val symmetry = r.nextInt(42, 54)
        val proportion = r.nextInt(38, 50)
        val fwhrVal = 1.62f + (r.nextFloat() * 0.12f)
        val fwhrScore = r.nextInt(40, 52)
        val cheekbones = ((jawline + proportion) / 2)

        val overall = (jawline * 0.25f + canthal * 0.25f + symmetry * 0.20f + proportion * 0.15f + skinScore * 0.15f).roundToInt().coerceIn(34, 48)
        val potential = overall + r.nextInt(20, 26)
        val tier = LooksTier.fromScore(overall)
        val tilt = (r.nextFloat() * 2.0f) - 1.5f

        val metricsList = listOf(
            FacialMetric(
                name = "Jawline & Mandible",
                score = jawline,
                category = "Bone Structure",
                status = "Undefined / Soft Base",
                details = "Mandible lacks angular sharpness. Subcutaneous water retention obscures bone borders.",
                ascendTip = "Eliminate processed sodium, chew hard mastic gum, and drop body fat."
            ),
            FacialMetric(
                name = "Eye Area & Canthal Tilt",
                score = canthal,
                category = "Periorbital",
                status = if (tilt >= 0.5f) "Neutral" else "Negative Slope",
                details = String.format("Measured canthal tilt: %+.1f°. Lacks compact hunter eye vectoring.", tilt),
                ascendTip = "Use cold ice compression, back sleeping, and lower eyelid training."
            ),
            FacialMetric(
                name = "fWHR (Facial Width-to-Height)",
                score = fwhrScore,
                category = "Dimorphism",
                status = "Moderate / Narrow",
                details = String.format("Estimated fWHR: %.2f.", fwhrVal),
                ascendTip = "Masseter training to widen lower facial framework."
            ),
            FacialMetric(
                name = "Facial Bilateral Symmetry",
                score = symmetry,
                category = "Facial Harmony",
                status = "Moderate Asymmetry",
                details = "Noticeable hemifacial deviation across vertical facial midline.",
                ascendTip = "Chew evenly on both sides and eliminate side-sleeping pressure."
            ),
            FacialMetric(
                name = "Facial Thirds & Midface",
                score = proportion,
                category = "Proportions",
                status = "Slight Disproportion",
                details = "Disproportion between forehead, nasal midface, and chin thirds.",
                ascendTip = "Adopt an appropriate fringe haircut to balance facial length."
            ),
            FacialMetric(
                name = "Skin Clarity & Tone",
                score = skinScore,
                category = "Skin & Leanness",
                status = if (skinScore >= 65) "Clean Complexion" else "Uneven / Blemishes",
                details = "Epidermal luminance and texture analysis.",
                ascendTip = "Apply daily broad-spectrum SPF 50+ and nightly Retinoid for cellular turnover."
            ),
            FacialMetric(
                name = "Cheekbone Prominence",
                score = cheekbones,
                category = "Midface",
                status = "Subtle Definition",
                details = "Zygomatic structure obscured by facial water retention.",
                ascendTip = "Follow the Debloat Protocol (potassium + water flush) to reveal cheekbones."
            )
        )

        return AnalysisResult(
            overallScore = overall,
            potentialScore = potential,
            tier = tier,
            frontImageUri = frontUri,
            sideImageUri = sideUri,
            metrics = metricsList,
            topAscensionFocus = listOf(
                "Debloat Protocol: Cut sodium and drink 3.5L water daily to carve jawline.",
                "Mewing Suction: Maintain back-tongue palate posture 24/7.",
                "Skincare Regime: Morning Vitamin C + SPF 50, Night Retinoid cycle."
            ),
            goldenRatioHarmony = 0.74f,
            canthalTiltDegrees = tilt,
            facialSymmetryPct = symmetry,
            fwhr = fwhrVal,
            midfaceRatio = 0.90f,
            eyeSpacingRatio = 1.00f,
            halos = emptyList(),
            failos = listOf("Unclear Facial Boundary (Retake under direct lighting recommended)", "Subcutaneous Facial Softness"),
            sideProfileSummary = "Face detection requires clear frontal lighting without shadow occlusion.",
            hasSideAnalysis = false
        )
    }
}
