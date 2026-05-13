
package com.kutirakone.app.ui.screens.scrap

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kutirakone.app.KutiraKoneApp
import com.kutirakone.app.data.model.*
import com.kutirakone.app.ui.common.materialEmoji
import com.kutirakone.app.ui.common.sampleScraps
import com.kutirakone.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScrapDetailScreen(
    scrapId: String,
    onBackClick: () -> Unit,
    onSwapClick: () -> Unit,
    onChatClick: (String) -> Unit,
    onIdeasClick: () -> Unit
) {
    val context   = LocalContext.current
    val container = (context.applicationContext as KutiraKoneApp).container
    val scope     = rememberCoroutineScope()

    var scrap      by remember { mutableStateOf<Scrap?>(null) }
    var isLoading  by remember { mutableStateOf(true) }
    var ideas      by remember { mutableStateOf<List<Triple<String,String,String>>>(emptyList()) }
    var loadingAI  by remember { mutableStateOf(false) }

    // Load scrap from Firestore
    LaunchedEffect(scrapId) {
        val loaded = container.scrapRepository.getScrap(scrapId)
        scrap     = loaded ?: sampleScraps.find { it.scrapId == scrapId } ?: sampleScraps.first()
        isLoading = false

        // Load AI design ideas
        scrap?.let { s ->
            loadingAI = true
            val aiIdeas = container.geminiRepository.generateDesignIdeas(
                material   = s.materialType,
                sizeMeters = s.sizeMeters,
                color      = s.color
            )
            ideas = if (aiIdeas.isNotEmpty()) {
                aiIdeas.map { idea ->
                    val emoji = when (idea.projectName.lowercase()) {
                        else -> "🎨"
                    }
                    Triple(emoji, idea.projectName, "${idea.difficulty.name} · ${idea.estimatedMinutes} mins")
                }
            } else {
                listOf(
                    Triple("🎭", "Festive Fabric Mask",   "Easy · 30 mins"),
                    Triple("👜", "Silk Coin Pouch",       "Easy · 45 mins"),
                    Triple("🌸", "Hair Accessory Set",    "Medium · 1 hr"),
                    Triple("🪆", "Patchwork Doll Dress",  "Medium · 1.5 hrs"),
                    Triple("🛍️", "Gift Wrap Furoshiki",   "Easy · 20 mins"),
                )
            }
            loadingAI = false
        }
    }

    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Green600)
        }
        return
    }

    val s = scrap ?: return

    val (badgeBg, badgeText, badgeLabel) = when (s.mode) {
        ListingMode.SWAP -> Triple(Amber50,  Amber600, "Swap")
        ListingMode.FREE -> Triple(Teal50,   Teal600,  "Free")
        ListingMode.SELL -> Triple(Coral50,  Coral600, "Sell")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scrap Detail", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Filled.Favorite, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Green600,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Hero image
            Box(
                Modifier.fillMaxWidth().height(220.dp)
                    .background(Color(0xFFEAF3DE)),
                contentAlignment = Alignment.Center
            ) {
                if (s.photos.isNotEmpty()) {
                    AsyncImage(
                        model = s.photos.first(), contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(materialEmoji(s.materialType), fontSize = 72.sp)
                }
                Surface(
                    modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
                    color = badgeBg, shape = RoundedCornerShape(12.dp)
                ) {
                    Text(badgeLabel, color = badgeText,
                        fontSize = 12.sp, fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                }
            }

            Column(Modifier.padding(16.dp)) {
                Row(Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top) {
                    Text(
                        "${s.materialType.name} ${s.color} Scrap",
                        fontSize = 20.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                }
                Text("📍 ${s.neighborhood.ifEmpty{"Nearby"}} · Mangaluru",
                    fontSize = 13.sp, color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp))

                // Seller info
                Spacer(Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(38.dp).background(Green50, CircleShape),
                        contentAlignment = Alignment.Center) {
                        Text("🧵", fontSize = 18.sp)
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("Local Artisan", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text("★★★★★ ${s.sellerRating} · Kutira-Kone seller",
                            fontSize = 12.sp, color = Color.Gray)
                    }
                }

                // Tags
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()) {
                    listOf(s.materialType.name, "${s.sizeMeters}m",
                        s.color, s.condition.name.replace("_"," "))
                        .forEach { tag ->
                            Surface(color = Color(0xFFF1EFE8), shape = RoundedCornerShape(20.dp)) {
                                Text(tag, fontSize = 11.sp, color = Color.DarkGray,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                            }
                        }
                }

                if (s.mode == ListingMode.SELL && s.priceRs > 0) {
                    Spacer(Modifier.height(8.dp))
                    Text("Price: ₹${s.priceRs}", fontSize = 15.sp,
                        fontWeight = FontWeight.Bold, color = Coral600)
                }

                // AI Design Ideas
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text("✨ AI Design Ideas", fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold)
                    TextButton(onClick = onIdeasClick) {
                        Text("See all", color = Green600, fontSize = 12.sp)
                    }
                }

                if (loadingAI) {
                    Box(Modifier.fillMaxWidth().padding(8.dp),
                        contentAlignment = Alignment.Center) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CircularProgressIndicator(Modifier.size(16.dp),
                                color = Purple600, strokeWidth = 2.dp)
                            Text("Generating ideas with Gemini AI...",
                                fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                } else {
                    ideas.take(3).forEach { (emoji, name, diff) ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(1.dp)
                        ) {
                            Row(Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically) {
                                Text(emoji, fontSize = 22.sp)
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(name, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                    Text(diff, fontSize = 11.sp,
                                        color = if (diff.startsWith("Easy")) Teal600 else Amber600)
                                }
                            }
                        }
                    }
                }

                // Action buttons
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onSwapClick,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Green600)
                ) {
                    Text("🔄 Request Trade", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(10.dp))
                OutlinedButton(
                    onClick = { onChatClick(s.sellerId) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Green600)
                ) {
                    Text("💬 Message Seller", color = Green600,
                        fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
