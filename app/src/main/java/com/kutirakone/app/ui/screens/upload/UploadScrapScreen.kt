package com.kutirakone.app.ui.screens.upload

import android.Manifest
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.firestore.GeoPoint
import com.kutirakone.app.KutiraKoneApp
import com.kutirakone.app.data.model.*
import com.kutirakone.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadScrapScreen(
    onPublished: () -> Unit,
    onBackClick: () -> Unit
) {
    val context   = LocalContext.current
    val container = (context.applicationContext as KutiraKoneApp).container
    val scope     = rememberCoroutineScope()

    val photoUris    = remember { mutableStateListOf<Uri>() }
    val photoBitmaps = remember { mutableStateListOf<Bitmap>() }
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }

    var materialType     by remember { mutableStateOf(MaterialType.COTTON) }
    var aiDetected       by remember { mutableStateOf<String?>(null) }
    var aiConfidence     by remember { mutableStateOf(0f) }
    var sizeInput        by remember { mutableStateOf("") }
    var colorInput       by remember { mutableStateOf("") }
    var condition        by remember { mutableStateOf(Condition.GOOD) }
    var mode             by remember { mutableStateOf(ListingMode.SWAP) }
    var priceInput       by remember { mutableStateOf("") }
    var isAnalyzing      by remember { mutableStateOf(false) }
    var isPublishing     by remember { mutableStateOf(false) }
    var showPhotoError   by remember { mutableStateOf(false) }
    var statusMsg        by remember { mutableStateOf("") }
    var showSourceDialog by remember { mutableStateOf(false) }

    // Process photo — decode bitmap and run AI
    fun processUri(uri: Uri) {
        if (photoUris.size >= 4) return
        photoUris.add(uri)
        showPhotoError = false

        try {
            val stream = context.contentResolver.openInputStream(uri)
            val bmp    = BitmapFactory.decodeStream(stream)
            stream?.close()
            if (bmp != null) {
                photoBitmaps.add(bmp)
                // Run Gemini AI only on first photo
                if (photoUris.size == 1) {
                    scope.launch {
                        isAnalyzing = true
                        val (mat, conf) = container.geminiRepository.classifyFabric(bmp)
                        materialType = mat
                        aiDetected   = mat.name
                        aiConfidence = conf
                        isAnalyzing  = false
                    }
                }
            }
        } catch (e: Exception) {
            // Continue even if bitmap decode fails
        }
    }

    // Gallery picker
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { processUri(it) } }

    // Camera result — called after photo is taken
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            // Use the URI we created before launching camera
            cameraImageUri?.let { uri ->
                processUri(uri)
            }
        }
    }

    // Create a URI in MediaStore for camera to write to
    fun createCameraUri(): Uri? {
        return try {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME,
                    "kutira_${System.currentTimeMillis()}.jpg")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/KutiraKone")
                }
            }
            context.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
            )
        } catch (e: Exception) { null }
    }

    // Camera permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = createCameraUri()
            if (uri != null) {
                cameraImageUri = uri
                cameraLauncher.launch(uri)
            }
        }
    }

    fun openCamera() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            val uri = createCameraUri()
            if (uri != null) {
                cameraImageUri = uri
                cameraLauncher.launch(uri)
            }
        }
    }

    // Photo source chooser dialog
    if (showSourceDialog) {
        AlertDialog(
            onDismissRequest = { showSourceDialog = false },
            title = { Text("Add Fabric Photo", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Camera
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable {
                            showSourceDialog = false
                            openCamera()
                        },
                        color = Color(0xFFF1F8F1),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            Box(Modifier.size(44.dp)
                                .background(Green50, CircleShape),
                                contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.CameraAlt, null,
                                    tint = Green600, modifier = Modifier.size(24.dp))
                            }
                            Column {
                                Text("Take Photo", fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium)
                                Text("Use camera to click fabric now",
                                    fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                    // Gallery
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable {
                            showSourceDialog = false
                            galleryLauncher.launch("image/*")
                        },
                        color = Color(0xFFF1F8F1),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            Box(Modifier.size(44.dp)
                                .background(Green50, CircleShape),
                                contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.Photo, null,
                                    tint = Green600, modifier = Modifier.size(24.dp))
                            }
                            Column {
                                Text("Choose from Gallery",
                                    fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                Text("Pick an existing photo",
                                    fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showSourceDialog = false }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Upload Scrap", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Green600,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {

            // Photos header
            Row(Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text("Fabric Photos (min. 1 required)",
                    fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                Text("${photoUris.size}/4", fontSize = 11.sp, color = Color.LightGray)
            }
            Spacer(Modifier.height(8.dp))

            // Photo row
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {

                // Existing photos
                itemsIndexed(photoUris) { index, uri ->
                    Box(modifier = Modifier
                        .size(110.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                    ) {
                        // Use AsyncImage — works for both gallery and camera URIs
                        AsyncImage(
                            model = uri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        // Delete button
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(22.dp)
                                .background(Color.Red, CircleShape)
                                .clickable {
                                    photoUris.removeAt(index)
                                    if (index < photoBitmaps.size) photoBitmaps.removeAt(index)
                                    if (index == 0) { aiDetected = null; aiConfidence = 0f }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Close, "Delete",
                                tint = Color.White, modifier = Modifier.size(14.dp))
                        }

                        // Main label
                        if (index == 0) {
                            Surface(
                                modifier = Modifier.align(Alignment.BottomStart).padding(4.dp),
                                color = Color.Black.copy(0.6f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text("Main", color = Color.White, fontSize = 9.sp,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                            }
                        }
                    }
                }

                // Add photo button
                if (photoUris.size < 4) {
                    item {
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    if (showPhotoError) 2.dp else 1.5.dp,
                                    if (showPhotoError) Color.Red else Color(0xFF8BC34A),
                                    RoundedCornerShape(12.dp)
                                )
                                .background(Color(0xFFF1F8F1))
                                .clickable { showSourceDialog = true },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isAnalyzing) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    CircularProgressIndicator(color = Green600,
                                        modifier = Modifier.size(26.dp), strokeWidth = 2.dp)
                                    Text("AI scanning...", color = Green600, fontSize = 10.sp)
                                }
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Filled.AddAPhoto, null,
                                        tint = Green600, modifier = Modifier.size(32.dp))
                                    Text(
                                        if (photoUris.isEmpty()) "Add Photo" else "Add More",
                                        color = Green600, fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium)
                                    Text("Camera / Gallery",
                                        color = Color.Gray, fontSize = 9.sp)
                                }
                            }
                        }
                    }
                }
            }

            if (showPhotoError) {
                Spacer(Modifier.height(4.dp))
                Text("⚠️ Please upload at least 1 photo",
                    color = Color.Red, fontSize = 12.sp)
            }

            // AI chip
            if (aiDetected != null) {
                Spacer(Modifier.height(12.dp))
                Surface(color = Purple50, shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("✨", fontSize = 18.sp)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text("AI detected: $aiDetected · ${(aiConfidence*100).toInt()}% confidence",
                                fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                                color = Purple600)
                            Text("Gemini auto-classified your fabric",
                                fontSize = 11.sp, color = Purple600.copy(0.7f))
                        }
                        TextButton(onClick = {
                            photoBitmaps.firstOrNull()?.let { bmp ->
                                scope.launch {
                                    isAnalyzing = true; aiDetected = null
                                    val (mat, conf) = container.geminiRepository.classifyFabric(bmp)
                                    materialType = mat; aiDetected = mat.name
                                    aiConfidence = conf; isAnalyzing = false
                                }
                            }
                        }) { Text("Re-scan", color = Purple600, fontSize = 11.sp) }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Material
            Text("Material type", fontSize = 12.sp, color = Color.Gray)
            Spacer(Modifier.height(6.dp))
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = materialType.name.lowercase().replaceFirstChar { it.uppercase() } +
                            if (aiDetected != null) " (AI)" else "",
                    onValueChange = {}, readOnly = true,
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    shape = RoundedCornerShape(10.dp)
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    MaterialType.values().forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.name.lowercase().replaceFirstChar { it.uppercase() }) },
                            onClick = { materialType = type; expanded = false })
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Text("Size (meters)", fontSize = 12.sp, color = Color.Gray)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(value = sizeInput, onValueChange = { sizeInput = it },
                modifier = Modifier.fillMaxWidth(), placeholder = { Text("e.g. 0.5") },
                shape = RoundedCornerShape(10.dp), singleLine = true)

            Spacer(Modifier.height(12.dp))
            Text("Color", fontSize = 12.sp, color = Color.Gray)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(value = colorInput, onValueChange = { colorInput = it },
                modifier = Modifier.fillMaxWidth(), placeholder = { Text("e.g. Forest Green") },
                shape = RoundedCornerShape(10.dp), singleLine = true)

            Spacer(Modifier.height(12.dp))
            Text("Condition", fontSize = 12.sp, color = Color.Gray)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Condition.values().forEach { c ->
                    val sel = condition == c
                    OutlinedButton(onClick = { condition = c }, modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (sel) Green50 else Color.White),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp, if (sel) Green600 else Color.LightGray),
                        contentPadding = PaddingValues(vertical = 8.dp)) {
                        Text(c.name.replace("_"," "), fontSize = 11.sp,
                            color = if (sel) Green600 else Color.Gray)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Text("Listing mode", fontSize = 12.sp, color = Color.Gray)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                data class ModeOpt(val m: ListingMode, val label: String,
                                   val bg: Color, val fg: Color)
                listOf(
                    ModeOpt(ListingMode.SWAP,"🔄 Swap",Amber50,Amber600),
                    ModeOpt(ListingMode.FREE,"🆓 Free",Teal50,Teal600),
                    ModeOpt(ListingMode.SELL,"💰 Sell",Coral50,Coral600)
                ).forEach { opt ->
                    val sel = mode == opt.m
                    OutlinedButton(onClick = { mode = opt.m }, modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (sel) opt.bg else Color.White),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp, if (sel) opt.fg else Color.LightGray),
                        contentPadding = PaddingValues(vertical = 8.dp)) {
                        Text(opt.label, fontSize = 11.sp,
                            color = if (sel) opt.fg else Color.Gray)
                    }
                }
            }

            if (mode == ListingMode.SELL) {
                Spacer(Modifier.height(12.dp))
                Text("Price (₹)", fontSize = 12.sp, color = Color.Gray)
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(value = priceInput, onValueChange = { priceInput = it },
                    modifier = Modifier.fillMaxWidth(), placeholder = { Text("e.g. 80") },
                    shape = RoundedCornerShape(10.dp), singleLine = true)
            }

            if (statusMsg.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(statusMsg, color = Green600, fontSize = 13.sp,
                    fontWeight = FontWeight.Medium)
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    if (photoUris.isEmpty()) { showPhotoError = true; return@Button }
                    isPublishing = true
                    scope.launch {
                        try {
                            // Upload all photos first — get download URLs
                            val uploadedUrls = mutableListOf<String>()
                            photoUris.forEachIndexed { i, uri ->
                                statusMsg = "Uploading photo ${i+1}/${photoUris.size}..."
                                val url = container.scrapRepository.uploadPhoto(uri)
                                if (url.isNotEmpty()) uploadedUrls.add(url)
                            }

                            statusMsg = "Saving listing..."

                            // Save to Firestore with photo URLs
                            val scrap = Scrap(
                                sellerId    = container.scrapRepository.currentUserId,
                                photos      = uploadedUrls,
                                materialType = materialType,
                                aiMaterialConfidence = aiConfidence,
                                sizeMeters  = sizeInput.replace("m","").trim()
                                    .toDoubleOrNull() ?: 0.5,
                                color       = colorInput.ifEmpty { "Mixed" },
                                condition   = condition,
                                mode        = mode,
                                priceRs     = priceInput.toIntOrNull() ?: 0,
                                neighborhood = "Mangaluru",
                                geoPoint    = GeoPoint(12.9141, 74.8560),
                                status      = ScrapStatus.ACTIVE
                            )
                            container.scrapRepository.createListing(scrap)

                            statusMsg    = "✅ Published!"
                            isPublishing = false
                            kotlinx.coroutines.delay(600)
                            onPublished()
                        } catch (e: Exception) {
                            statusMsg    = "❌ Error: ${e.message}"
                            isPublishing = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape  = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Green600),
                enabled = !isPublishing && !isAnalyzing
            ) {
                if (isPublishing) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CircularProgressIndicator(color = Color.White,
                            modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Text(statusMsg.ifEmpty { "Publishing..." },
                            fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    Text("Publish Listing", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}