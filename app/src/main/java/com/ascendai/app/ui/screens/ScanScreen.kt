package com.ascendai.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.ascendai.app.theme.*
import com.ascendai.app.ui.components.AnimatedScanOverlay
import com.ascendai.app.ui.viewmodel.ScanViewModel
import com.ascendai.app.util.CameraUtil

@Composable
fun ScanScreen(
    viewModel: ScanViewModel,
    onNavigateToScanning: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var activePhotoSlot by remember { mutableStateOf<String?>(null) } // "front" or "side"
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    // Camera Launcher
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            if (activePhotoSlot == "front") {
                viewModel.setFrontImage(tempCameraUri!!)
            } else if (activePhotoSlot == "side") {
                viewModel.setSideImage(tempCameraUri!!)
            }
        }
    }

    // Permission Launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val uri = CameraUtil.createTempImageUri(context)
            tempCameraUri = uri
            takePictureLauncher.launch(uri)
        } else {
            Toast.makeText(context, "Camera permission is required to capture photos.", Toast.LENGTH_SHORT).show()
        }
    }

    // Gallery Launcher
    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            if (activePhotoSlot == "front") {
                viewModel.setFrontImage(uri)
            } else if (activePhotoSlot == "side") {
                viewModel.setSideImage(uri)
            }
        }
    }

    fun handleCameraCapture(slot: String) {
        activePhotoSlot = slot
        val hasCamPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasCamPermission) {
            val uri = CameraUtil.createTempImageUri(context)
            tempCameraUri = uri
            takePictureLauncher.launch(uri)
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    fun handleGalleryPick(slot: String) {
        activePhotoSlot = slot
        pickMediaLauncher.launch("image/*")
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(NeonCyan)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "ASCEND AI",
                style = Typography.headlineLarge.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 3.sp,
                    color = TextPrimary
                )
            )
        }

        Text(
            text = "Neural Facial Harmony & Looksmaxxing Engine",
            style = Typography.bodyMedium.copy(
                color = TextSecondary,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
        )

        // Photo Upload Cards Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Front Photo Slot (Required)
            PhotoSlotCard(
                title = "Front Profile",
                subtitle = "Mandatory",
                uri = uiState.frontImageUri,
                onCameraClick = { handleCameraCapture("front") },
                onGalleryClick = { handleGalleryPick("front") },
                onClear = { viewModel.clearFrontImage() },
                modifier = Modifier.weight(1f)
            )

            // Side Photo Slot (Recommended)
            PhotoSlotCard(
                title = "Side Profile",
                subtitle = "Gonial & E-Line",
                uri = uiState.sideImageUri,
                onCameraClick = { handleCameraCapture("side") },
                onGalleryClick = { handleGalleryPick("side") },
                onClear = { viewModel.clearSideImage() },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Guidelines Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceCard)
                .border(1.dp, BorderGlass, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "100% PRIVATE & ON-DEVICE",
                        style = Typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan,
                            letterSpacing = 1.sp
                        )
                    )
                }
                Text(
                    text = "• Natural lighting at eye level with neutral expression.\n• Keep hair away from forehead and jaw contours.\n• Side profile accurately measures chin projection and gonial slope.",
                    style = Typography.bodyMedium.copy(
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = TextSecondary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // CTA Button: Analyze & Ascend
        val isReady = uiState.frontImageUri != null || uiState.sideImageUri != null

        Button(
            onClick = {
                if (isReady) {
                    onNavigateToScanning()
                    viewModel.startAnalysis {}
                } else {
                    Toast.makeText(context, "Please upload or take a front photo first!", Toast.LENGTH_SHORT).show()
                }
            },
            enabled = isReady,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = NeonCyan,
                disabledContainerColor = SurfaceCardElevated
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    tint = if (isReady) BackgroundDark else TextMuted
                )
                Text(
                    text = if (isReady) "ANALYZE & ASCEND" else "UPLOAD PHOTO TO START",
                    style = Typography.labelLarge.copy(
                        color = if (isReady) BackgroundDark else TextMuted,
                        letterSpacing = 1.5.sp
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun PhotoSlotCard(
    title: String,
    subtitle: String,
    uri: Uri?,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(220.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceCard)
            .border(
                width = 1.dp,
                brush = if (uri != null) Brush.verticalGradient(listOf(NeonCyan, NeonPurple)) else Brush.verticalGradient(listOf(BorderGlass, BorderGlass)),
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        if (uri != null) {
            // Photo Preview with HUD Overlay
            AsyncImage(
                model = uri,
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            AnimatedScanOverlay(
                isScanning = true,
                modifier = Modifier.fillMaxSize()
            )

            // Success Tag
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(BackgroundDark.copy(alpha = 0.8f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = title,
                        style = Typography.labelSmall.copy(
                            fontSize = 10.sp,
                            color = TextPrimary
                        )
                    )
                }
            }

            // Retake / Actions bottom bar
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(BackgroundDark.copy(alpha = 0.85f))
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(onClick = onCameraClick, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Retake", tint = NeonCyan, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onGalleryClick, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = "Gallery", tint = TextSecondary, modifier = Modifier.size(18.dp))
                }
            }
        } else {
            // Empty State Picker
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(SurfaceCardElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AddAPhoto,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = title,
                    style = Typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                )
                Text(
                    text = subtitle,
                    style = Typography.labelSmall.copy(
                        color = NeonPurple,
                        fontSize = 11.sp
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onCameraClick,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceCardElevated),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cam", fontSize = 11.sp, color = TextPrimary)
                    }

                    Button(
                        onClick = onGalleryClick,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceCardElevated),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Pick", fontSize = 11.sp, color = TextPrimary)
                    }
                }
            }
        }
    }
}
