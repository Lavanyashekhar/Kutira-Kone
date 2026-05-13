package com.kutirakone.app.ui.screens.trades

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kutirakone.app.KutiraKoneApp
import com.kutirakone.app.data.model.Scrap
import com.kutirakone.app.data.model.SwapRequest
import com.kutirakone.app.ui.common.materialEmoji
import com.kutirakone.app.ui.common.sampleScraps
import com.kutirakone.app.ui.theme.Amber600
import com.kutirakone.app.ui.theme.Coral50
import com.kutirakone.app.ui.theme.Coral600
import com.kutirakone.app.ui.theme.Green50
import com.kutirakone.app.ui.theme.Green600
import com.kutirakone.app.ui.theme.Teal50
import com.kutirakone.app.ui.theme.Teal600
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTradesScreen(
    onRateUser: (String) -> Unit,
    onScrapClick: (String) -> Unit,
    onBackClick: () -> Unit
) {
    val context   = LocalContext.current
    val container = (context.applicationContext as KutiraKoneApp).container
    val scope     = rememberCoroutineScope()

    var myListings       by remember { mutableStateOf<List<Scrap>>(emptyList()) }
    var incomingRequests by remember { mutableStateOf<List<SwapRequest>>(emptyList()) }
    var isLoading        by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        container.scrapRepository.getMyListings()
            .catch { myListings = sampleScraps.take(2) }
            .collect { list ->
                myListings = list.ifEmpty { sampleScraps.take(2) }
                isLoading  = false
            }
    }

    LaunchedEffect(Unit) {
        container.swapRepository.getIncomingRequests()
            .catch { }
            .collect { requests -> incomingRequests = requests }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Listings & Trades", fontWeight = FontWeight.SemiBold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Green600,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Green600)
            }
            return@Scaffold
        }

        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Active Listings
            item { SectionLabel("Active Listings") }

            if (myListings.isEmpty()) {
                item {
                    Text(
                        "No active listings yet. Upload your first scrap!",
                        fontSize = 13.sp, color = Color.Gray,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            } else {
                items(myListings) { scrap ->
                    TradeRow(
                        emoji     = materialEmoji(scrap.materialType),
                        title     = "${scrap.materialType.name} ${scrap.color} (${scrap.sizeMeters}m)",
                        subtitle  = "${scrap.mode.name} · ${scrap.neighborhood}",
                        badge     = "Active",
                        badgeBg   = Teal50,
                        badgeText = Teal600,
                        onClick   = { onScrapClick(scrap.scrapId) }
                    )
                }
            }

            // Pending Requests
            if (incomingRequests.isNotEmpty()) {
                item { Spacer(Modifier.height(8.dp)); SectionLabel("Pending Requests") }
                items(incomingRequests) { request ->
                    Card(
                        modifier  = Modifier.fillMaxWidth(),
                        shape     = RoundedCornerShape(12.dp),
                        colors    = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Row(
                            Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                Modifier.size(44.dp)
                                    .background(Color(0xFFF1EFE8), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) { Text("🔄", fontSize = 22.sp) }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Swap offer received",
                                    fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                Text("${request.offeredScrapIds.size} scrap(s) offered",
                                    fontSize = 11.sp, color = Color.Gray)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Surface(
                                    color = Green50,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.clickable {
                                        scope.launch {
                                            container.swapRepository.acceptRequest(request.requestId)
                                        }
                                    }
                                ) {
                                    Text("Accept", color = Green600,
                                        fontSize = 11.sp, fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
                                }
                                Surface(
                                    color = Coral50,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.clickable {
                                        scope.launch {
                                            container.swapRepository.rejectRequest(request.requestId)
                                        }
                                    }
                                ) {
                                    Text("Reject", color = Coral600,
                                        fontSize = 11.sp, fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
                                }
                            }
                        }
                    }
                }
            }

            // Trade History
            item { Spacer(Modifier.height(8.dp)); SectionLabel("Trade History") }
            item {
                TradeRow(
                    emoji     = "🌸",
                    title     = "Green Silk → Red Wool",
                    subtitle  = "Traded with Meera K. · ★★★★★",
                    badge     = "Done",
                    badgeBg   = Green50,
                    badgeText = Green600,
                    onRate    = { onRateUser("trade_001") }
                )
            }
            item {
                TradeRow(
                    emoji     = "🌿",
                    title     = "Yellow Silk given free",
                    subtitle  = "To NGO Craft Group · ★★★★★",
                    badge     = "Done",
                    badgeBg   = Green50,
                    badgeText = Green600
                )
            }
        }
    }
}

@Composable
fun SectionLabel(text: String) {
    Text(
        text.uppercase(),
        fontSize   = 11.sp,
        fontWeight = FontWeight.Medium,
        color      = Color.Gray,
        letterSpacing = 0.5.sp,
        modifier   = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
fun TradeRow(
    emoji: String,
    title: String,
    subtitle: String,
    badge: String,
    badgeBg: Color,
    badgeText: Color,
    onClick: (() -> Unit)? = null,
    onRate: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(44.dp)
                    .background(Color(0xFFF1EFE8), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) { Text(emoji, fontSize = 22.sp) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Text(subtitle, fontSize = 11.sp, color = Color.Gray,
                    modifier = Modifier.padding(top = 2.dp))
            }
            Column(horizontalAlignment = Alignment.End) {
                Surface(color = badgeBg, shape = RoundedCornerShape(8.dp)) {
                    Text(badge, color = badgeText,
                        fontSize = 10.sp, fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                }
                if (onRate != null) {
                    TextButton(onClick = onRate, contentPadding = PaddingValues(0.dp)) {
                        Text("Rate", fontSize = 10.sp, color = Amber600)
                    }
                }
            }
        }
    }
}