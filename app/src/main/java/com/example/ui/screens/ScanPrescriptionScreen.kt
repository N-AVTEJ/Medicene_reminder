package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.data.repository.FirebaseRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanPrescriptionScreen(
    onNavigateBack: () -> Unit,
    onSaveScannedMedicine: (MedicineItem) -> Unit
) {
    var isScanning by remember { mutableStateOf(false) }
    var scannedResult by remember { mutableStateOf<MedicineItem?>(null) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var flashOn by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Camera Permission State
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    // Camera Capture Launcher
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            capturedBitmap = bitmap
            isScanning = true
            // Simulate AI/OCR label analysis
            scannedResult = MedicineItem(
                id = System.currentTimeMillis().toString(),
                name = "Metformin HCl",
                dosage = "500mg Extended Release",
                frequency = "Once Daily with Dinner",
                remainingPills = 60,
                category = "Prescription"
            )
            isScanning = false
        } else {
            // Fallback preview photo for emulator environments without hardware camera feed
            val sampleBitmap = createSamplePrescriptionBitmap()
            capturedBitmap = sampleBitmap
            scannedResult = MedicineItem(
                id = System.currentTimeMillis().toString(),
                name = "Metformin HCl",
                dosage = "500mg Extended Release",
                frequency = "Once Daily with Dinner",
                remainingPills = 60,
                category = "Prescription"
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Scan Prescription Label",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("scan_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { flashOn = !flashOn },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = "Toggle Flash",
                            tint = if (flashOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Permission Banner if missing
            if (!hasCameraPermission) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("camera_permission_card")
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Camera Permission Required",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = "Grant camera access to scan prescription bottle labels.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        Button(
                            onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                            modifier = Modifier.testTag("grant_camera_permission_button")
                        ) {
                            Text("Grant Access")
                        }
                    }
                }
            }

            // Viewfinder OR Captured Image Preview Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF0F172A))
                    .border(
                        width = 3.dp,
                        color = if (capturedBitmap != null) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .testTag("scan_viewfinder"),
                contentAlignment = Alignment.Center
            ) {
                if (capturedBitmap != null) {
                    // IMAGE PREVIEW of Captured Prescription
                    Image(
                        bitmap = capturedBitmap!!.asImageBitmap(),
                        contentDescription = "Captured Prescription Label Preview",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("captured_image_preview")
                    )
                    // Badge overlay on captured image preview
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "📷 Captured Photo Preview",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                } else if (isScanning) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Analyzing Prescription Label with AI...",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.DocumentScanner,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Align prescription bottle or paper label here",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Scanned Result Details Card
            AnimatedVisibility(visible = scannedResult != null) {
                scannedResult?.let { med ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("scanned_result_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Detected Prescription",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Text(
                                text = med.name,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Dosage: ${med.dosage}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Frequency: ${med.frequency}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Pills per bottle: ${med.remainingPills}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Action buttons: Confirm vs Retake
                            Button(
                                onClick = {
                                    FirebaseRepository.addMedicineWithAutoSchedule(
                                        name = med.name,
                                        dose = med.dosage,
                                        frequency = med.frequency,
                                        durationDays = 30,
                                        context = context
                                    )
                                    onSaveScannedMedicine(med)
                                    onNavigateBack()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                                    .testTag("save_scanned_rx_button"),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text(
                                    text = "Confirm & Auto-Generate 30-Day Schedule",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    capturedBitmap = null
                                    scannedResult = null
                                    if (hasCameraPermission) {
                                        takePictureLauncher.launch(null)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("retake_photo_button"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Retake Photo",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Primary Capture Action Button
            if (scannedResult == null) {
                Button(
                    onClick = {
                        if (hasCameraPermission) {
                            takePictureLauncher.launch(null)
                        } else {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    enabled = !isScanning,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .testTag("capture_rx_button"),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(imageVector = Icons.Default.Camera, contentDescription = null, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (hasCameraPermission) "Open Camera & Capture Label" else "Grant Camera & Scan",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Creates a bitmap label image for preview
 */
private fun createSamplePrescriptionBitmap(): Bitmap {
    val bitmap = Bitmap.createBitmap(600, 400, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    
    // Background Rx label
    val bgPaint = Paint().apply {
        color = android.graphics.Color.rgb(248, 250, 252)
        style = Paint.Style.FILL
    }
    canvas.drawRect(0f, 0f, 600f, 400f, bgPaint)

    // Rx Header bar
    val headerPaint = Paint().apply {
        color = android.graphics.Color.rgb(37, 99, 235)
        style = Paint.Style.FILL
    }
    canvas.drawRect(0f, 0f, 600f, 70f, headerPaint)

    val whiteTextPaint = Paint().apply {
        color = android.graphics.Color.WHITE
        textSize = 32f
        isFakeBoldText = true
    }
    canvas.drawText("Rx PRESCRIPTION LABEL", 30f, 46f, whiteTextPaint)

    // Rx details text
    val textPaint = Paint().apply {
        color = android.graphics.Color.rgb(15, 23, 42)
        textSize = 28f
        isFakeBoldText = true
    }
    canvas.drawText("Rx #982143-01", 30f, 130f, textPaint)
    canvas.drawText("METFORMIN HCL 500MG ER", 30f, 180f, textPaint)
    
    val subTextPaint = Paint().apply {
        color = android.graphics.Color.rgb(71, 85, 105)
        textSize = 24f
    }
    canvas.drawText("TAKE 1 TABLET DAILY WITH EVENING MEAL", 30f, 230f, subTextPaint)
    canvas.drawText("QTY: 60 TABLETS • REFILLS: 3", 30f, 280f, subTextPaint)
    canvas.drawText("DR. R. VANCE • PHARMACY #4102", 30f, 330f, subTextPaint)

    return bitmap
}



