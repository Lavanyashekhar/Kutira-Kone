package com.kutirakone.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kutirakone.app.KutiraKoneApp
import com.kutirakone.app.data.model.*
import com.kutirakone.app.ui.common.materialEmoji
import com.kutirakone.app.ui.common.sampleScraps
import com.kutirakone.app.ui.theme.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onScrapClick: (String) -> Unit,
    onUploadClick: () -> Unit,
    onMapClick: () -> Unit,
    onProfileClick: () -> Unit,
    onTradesClick: () -> Unit
) {
    val context    = LocalContext.current
    val container  = (context.applicationContext as KutiraKoneApp).container
    val scope      = rememberCoroutineScope()

    // Get current user ID — refreshed on every recomposition
    val currentUid by remember {
        mutableStateOf(FirebaseAuth.getInstance().currentUser?.uid ?: "")
    }

    var scraps           by remember { mutableStateOf<List<Scrap>>(sampleScraps) }
    var search           by remember { mutableStateOf("") }
    var selectedMaterial by remember { mutableStateOf<MaterialType?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var scrapToDelete    by remember { mutableStateOf<Scrap?>(null) }

    // Load real Firestore data
    LaunchedEffect(Unit) {
        container.scrapRepository.getNearby(12.9141, 74.8560, 10.0)
            .catch { }
            .collect { list ->
                // Always show real data if available, else sample
                scraps = if (list.isNotEmpty()) list else sampleScraps
            }
    }

    // Delete confirmation dialog
    if (showDeleteDialog && scrapToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false; scrapToDelete = null },
            title = { Text("Delete Listing?", fontWeight = FontWeight.Bold) },
            text  = {
                Text(
                    "Delete \"${scrapToDelete?.materialType?.name} ${scrapToDelete?.color}\"?\nThis cannot be undone.",
                    fontSize = 14.sp, color = Color.Gray
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val id = scrapToDelete?.scrapId ?: ""
                        if (id.isNotEmpty()) {
                            scope.launch {
                                try {
                                    FirebaseFirestore.getInstance()
                                        .collection("scraps")
                                        .document(id)
                                        .delete()
                                    // Remove from local list immediately
                                    scraps = scraps.filter { it.scrapId != id }
                                } catch (e: Exception) { }
                            }
                        }
                        showDeleteDialog = false
                        scrapToDelete    = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    shape  = RoundedCornerShape(8.dp)
                ) { Text("Yes, Delete", color = Color.White) }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteDialog = false; scrapToDelete = null },
                    shape = RoundedCornerShape(8.dp)
                ) { Text("Cancel") }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    val filters  = listOf(null to "All") +
            MaterialType.values().map { it to it.name.lowercase()
                .replaceFirstChar { c -> c.uppercase() } }

    val filtered = scraps
        .filter { selectedMaterial == null || it.materialType == selectedMaterial }
        .filter {
            search.isBlank() ||
                    it.color.contains(search, true) ||
                    it.materialType.name.contains(search, true)
        }

    Column(Modifier.fillMaxSize().background(Color(0xFFFAFAF8))) {

        // Header
        Box(Modifier.fillMaxWidth().background(Green600)) {
            Column(Modifier.padding(16.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Kutira-Kone 🧵", color = Color.White,
                            fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                        Text("📍 Mangaluru · within 10 km",
                            color = Color.White.copy(0.8f), fontSize = 12.sp)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(onClick = {}) {
                            Icon(Icons.Filled.Notifications, null, tint = Color.White)
                        }
                        IconButton(onClick = onProfileClick) {
                            Icon(Icons.Filled.Person, null, tint = Color.White)
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    placeholder = {
                        Text("Search fabric, color...",
                            color = Color.White.copy(0.6f), fontSize = 13.sp)
                    },
                    leadingIcon = {
                        Icon(Icons.Filled.Search, null, tint = Color.White.copy(0.7f))
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor    = Color.White.copy(0.3f),
                        focusedBorderColor      = Color.White,
                        unfocusedContainerColor = Color.White.copy(0.15f),
                        focusedContainerColor   = Color.White.copy(0.15f),
                        cursorColor             = Color.White,
                        focusedTextColor        = Color.White,
                        unfocusedTextColor      = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )
            }
        }

        // Filter chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filters) { (type, label) ->
                FilterChip(
                    selected = selectedMaterial == type,
                    onClick  = { selectedMaterial = type },
                    label    = { Text(label, fontSize = 12.sp) },
                    colors   = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Green50,
                        selectedLabelColor     = Green600
                    )
                )
            }
        }

        // Fabric grid
        if (filtered.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "No scraps found nearby.\nBe the first to upload one! 🧵",
                    color = Color.Gray, fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement   = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(
                    items = filtered,
                    // key helps Compose track items correctly for delete
                    key   = { it.scrapId }
                ) { scrap ->
                    FabricCard(
                        scrap      = scrap,
                        currentUid = currentUid,
                        onClick    = { onScrapClick(scrap.scrapId) },
                        onDelete   = {
                            scrapToDelete    = scrap
                            showDeleteDialog = true
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FabricCard(
    scrap: Scrap,
    currentUid: String,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current

    val (badgeBg, badgeText, badgeLabel) = when (scrap.mode) {
        ListingMode.SWAP -> Triple(Amber50, Amber600, "Swap")
        ListingMode.FREE -> Triple(Teal50,  Teal600,  "Free")
        ListingMode.SELL -> Triple(Coral50, Coral600, "Sell")
    }

    // Show delete if:
    // 1. User is logged in AND is the owner
    // 2. OR scrap has no sellerId (sample data — show for demo)
    val isOwner = currentUid.isNotEmpty() &&
            (scrap.sellerId == currentUid || scrap.sellerId.isEmpty())

    Card(
        modifier  = Modifier.fillMaxWidth().clickable { onClick() },
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Box {
            // Photo section
            if (scrap.photos.isNotEmpty()) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(scrap.photos.first())
                        .crossfade(300)
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .size(400, 400)
                        .build(),
                    contentDescription = null,
                    modifier     = Modifier.fillMaxWidth().height(110.dp),
                    contentScale = ContentScale.Crop,
                    loading = {
                        Box(
                            Modifier.fillMaxWidth().height(110.dp)
                                .background(Color(0xFFEAF3DE)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = Green600,
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    },
                    error = {
                        Box(
                            Modifier.fillMaxWidth().height(110.dp)
                                .background(Color(0xFFEAF3DE)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(materialEmoji(scrap.materialType), fontSize = 36.sp)
                        }
                    }
                )
            } else {
                Box(
                    Modifier.fillMaxWidth().height(110.dp)
                        .background(Color(0xFFEAF3DE)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(materialEmoji(scrap.materialType), fontSize = 36.sp)
                }
            }

            // Mode badge — top right
            Surface(
                modifier = Modifier.align(Alignment.TopEnd).padding(6.dp),
                color    = badgeBg,
                shape    = RoundedCornerShape(10.dp)
            ) {
                Text(
                    badgeLabel,
                    color      = badgeText,
                    fontSize   = 10.sp,
                    fontWeight = FontWeight.Medium,
                    modifier   = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                )
            }

            // Delete button — top left
            // Always shown for owner regardless of photo
            if (isOwner) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .size(28.dp)
                        .background(Color.Red.copy(alpha = 0.9f), CircleShape)
                        .clickable { onDelete() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Delete listing",
                        tint     = Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }

        // Card info
        Column(Modifier.padding(10.dp)) {
            Text(
                "${scrap.materialType.name.lowercase()
                    .replaceFirstChar { it.uppercase() }} ${scrap.color}",
                fontSize   = 13.sp,
                fontWeight = FontWeight.Medium,
                maxLines   = 1
            )
            Text(
                "${scrap.sizeMeters}m · ${scrap.condition.name.replace("_", " ")}",
                fontSize = 11.sp, color = Color.Gray,
                modifier = Modifier.padding(top = 2.dp)
            )
            Text(
                "📍 ${scrap.neighborhood.ifEmpty { "Nearby" }}",
                fontSize   = 10.sp,
                color      = Green600,
                fontWeight = FontWeight.Medium,
                modifier   = Modifier.padding(top = 4.dp)
            )
            Text("★★★★★", fontSize = 10.sp, color = Amber600)
        }
    }
}
