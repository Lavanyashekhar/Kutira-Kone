package com.kutirakone.app.ui.screens.map

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.kutirakone.app.KutiraKoneApp
import com.kutirakone.app.data.model.ListingMode
import com.kutirakone.app.data.model.MaterialType
import com.kutirakone.app.data.model.Scrap
import com.kutirakone.app.ui.common.sampleScraps
import com.kutirakone.app.ui.theme.Green600
import kotlinx.coroutines.flow.catch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onScrapClick: (String) -> Unit,
    onBackClick: () -> Unit
) {
    val context   = LocalContext.current
    val container = (context.applicationContext as KutiraKoneApp).container

    var radiusKm         by remember { mutableStateOf(5f) }
    var selectedMaterial by remember { mutableStateOf<MaterialType?>(null) }
    var scraps           by remember { mutableStateOf<List<Scrap>>(sampleScraps) }

    // Load real scraps from Firestore
    LaunchedEffect(Unit) {
        container.scrapRepository
            .getNearby(12.9141, 74.8560, 10.0)
            .catch { }
            .collect { list ->
                if (list.isNotEmpty()) scraps = list
            }
    }

    val mangaluru   = LatLng(12.9141, 74.8560)
    val cameraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(mangaluru, 13f)
    }

    // Sample pin positions for demo
    val pinPositions = listOf(
        LatLng(12.920, 74.852),
        LatLng(12.910, 74.862),
        LatLng(12.905, 74.870),
        LatLng(12.918, 74.868),
        LatLng(12.925, 74.858),
        LatLng(12.908, 74.856),
    )

    val filters = listOf(null to "All") +
            MaterialType.values().map { it to it.name.lowercase()
                .replaceFirstChar { c -> c.uppercase() } }

    val filtered = scraps.filter {
        selectedMaterial == null || it.materialType == selectedMaterial
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Browse Map", fontWeight = FontWeight.SemiBold) },
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
        Column(Modifier.fillMaxSize().padding(padding)) {

            // Google Map
            Box(Modifier.weight(1f)) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraState,
                    properties = MapProperties(isMyLocationEnabled = false)
                ) {
                    filtered.forEachIndexed { i, scrap ->
                        val position = pinPositions.getOrElse(i) { mangaluru }
                        val hue = when (scrap.mode) {
                            ListingMode.SWAP -> BitmapDescriptorFactory.HUE_ORANGE
                            ListingMode.FREE -> BitmapDescriptorFactory.HUE_GREEN
                            ListingMode.SELL -> BitmapDescriptorFactory.HUE_RED
                        }
                        Marker(
                            state   = MarkerState(position = position),
                            title   = "${scrap.materialType.name} ${scrap.color}",
                            snippet = "${scrap.sizeMeters}m · ${scrap.mode.name}",
                            icon    = BitmapDescriptorFactory.defaultMarker(hue),
                            onClick = { onScrapClick(scrap.scrapId); false }
                        )
                    }
                }

                // Radius indicator
                Surface(
                    modifier = Modifier.align(Alignment.TopEnd).padding(10.dp),
                    color = Color.White,
                    shape = RoundedCornerShape(8.dp),
                    shadowElevation = 4.dp
                ) {
                    Text("${radiusKm.toInt()} km",
                        fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                        color = Green600,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                }

                // Legend
                Surface(
                    modifier = Modifier.align(Alignment.BottomStart).padding(10.dp),
                    color = Color.White, shape = RoundedCornerShape(8.dp),
                    shadowElevation = 4.dp
                ) {
                    Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text("🟠 Swap  🟢 Free  🔴 Sell",
                            fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }

            // Controls
            Surface(color = Color.White, shadowElevation = 8.dp) {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                    Row(Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Search radius:", fontSize = 13.sp, color = Color.Gray)
                        Text("${radiusKm.toInt()} km",
                            fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                            color = Green600)
                    }
                    Slider(
                        value = radiusKm,
                        onValueChange = { radiusKm = it },
                        valueRange = 1f..25f,
                        steps = 23,
                        colors = SliderDefaults.colors(
                            thumbColor = Green600,
                            activeTrackColor = Green600
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(filters) { (type, label) ->
                            FilterChip(
                                selected = selectedMaterial == type,
                                onClick  = { selectedMaterial = type },
                                label    = { Text(label, fontSize = 12.sp) },
                                colors   = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFEAF3DE),
                                    selectedLabelColor     = Green600
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
