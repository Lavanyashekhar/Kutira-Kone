package com.kutirakone.app.ui.screens.trades

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
<<<<<<< HEAD
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
=======
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
>>>>>>> d94e41b (Initial project upload)
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
<<<<<<< HEAD
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
=======
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kutirakone.app.KutiraKoneApp
import com.kutirakone.app.data.model.Scrap
import com.kutirakone.app.data.model.SwapRequest
import com.kutirakone.app.data.model.SwapStatus
import com.kutirakone.app.ui.common.materialEmoji
import com.kutirakone.app.ui.common.sampleScraps
import com.kutirakone.app.ui.theme.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
>>>>>>> d94e41b (Initial project upload)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTradesScreen(
    onRateUser: (String) -> Unit,
    onScrapClick: (String) -> Unit,
    onBackClick: () -> Unit
) {
<<<<<<< HEAD
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

=======
    val context    = LocalContext.current
    val container  = (context.applicationContext as KutiraKoneApp).container
    val scope      = rememberCoroutineScope()
    val db         = FirebaseFirestore.getInstance()
    val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val isGuest    = currentUid.isEmpty()

    var myListings       by remember { mutableStateOf<List<Scrap>>(emptyList()) }
    var incomingRequests by remember { mutableStateOf<List<SwapRequest>>(emptyList()) }
    var outgoingRequests by remember { mutableStateOf<List<SwapRequest>>(emptyList()) }
    var isLoading        by remember { mutableStateOf(true) }

    // Selected tab — 0=Listings, 1=Incoming, 2=Outgoing, 3=History
    var selectedTab by remember { mutableStateOf(0) }

    // Load my listings
    LaunchedEffect(currentUid) {
        if (!isGuest) {
            container.scrapRepository.getMyListings()
                .catch { myListings = sampleScraps.take(2) }
                .collect { list ->
                    myListings = list
                    isLoading  = false
                }
        } else {
            isLoading = false
        }
    }

    // Load incoming requests (someone wants MY scrap)
    LaunchedEffect(currentUid) {
        if (!isGuest) {
            container.swapRepository.getIncomingRequests()
                .catch { }
                .collect { list -> incomingRequests = list }
        }
    }

    // Load outgoing requests (I requested someone's scrap)
    LaunchedEffect(currentUid) {
        if (!isGuest) {
            container.swapRepository.getOutgoingRequests()
                .catch { }
                .collect { list -> outgoingRequests = list }
        }
    }

    val activeListings  = myListings.filter { it.status.name == "ACTIVE" }
    val tradedListings  = myListings.filter { it.status.name == "TRADED" }
    val pendingIncoming = incomingRequests.filter { it.status == SwapStatus.PENDING }
    val pendingOutgoing = outgoingRequests.filter { it.status == SwapStatus.PENDING }
    val acceptedTrades  = outgoingRequests.filter { it.status == SwapStatus.ACCEPTED }

>>>>>>> d94e41b (Initial project upload)
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
<<<<<<< HEAD
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
=======

        if (isGuest) {
            Box(Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(32.dp)) {
                    Text("🔐", fontSize = 48.sp)
                    Text("Login Required", fontSize = 18.sp,
                        fontWeight = FontWeight.Bold)
                    Text("Please login with OTP to view your listings and trades",
                        fontSize = 14.sp, color = Color.Gray,
                        textAlign = TextAlign.Center)
                }
            }
            return@Scaffold
        }

        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
>>>>>>> d94e41b (Initial project upload)
                CircularProgressIndicator(color = Green600)
            }
            return@Scaffold
        }

<<<<<<< HEAD
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
=======
        Column(Modifier.fillMaxSize().padding(padding)) {

            // Tab row
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor   = Color.White,
                contentColor     = Green600,
                edgePadding      = 8.dp
            ) {
                listOf(
                    "My Listings" to activeListings.size,
                    "Requests In" to pendingIncoming.size,
                    "Requests Out" to pendingOutgoing.size,
                    "History"     to tradedListings.size
                ).forEachIndexed { i, (title, count) ->
                    Tab(
                        selected = selectedTab == i,
                        onClick  = { selectedTab = i },
                        text = {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically) {
                                Text(title, fontSize = 12.sp)
                                if (count > 0) {
                                    Surface(
                                        color = if (selectedTab == i) Green600
                                        else Color.LightGray,
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("$count", fontSize = 10.sp,
                                            color = Color.White,
                                            modifier = Modifier.padding(
                                                horizontal = 6.dp, vertical = 1.dp))
                                    }
                                }
                            }
                        }
>>>>>>> d94e41b (Initial project upload)
                    )
                }
            }

<<<<<<< HEAD
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
=======
            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                // ── Tab 0: My Active Listings ─────────────────────
                if (selectedTab == 0) {
                    if (activeListings.isEmpty()) {
                        item {
                            EmptyState(
                                emoji = "📦",
                                title = "No active listings",
                                subtitle = "Tap the + button to upload your first fabric scrap"
                            )
                        }
                    } else {
                        items(activeListings) { scrap ->
                            ListingCard(
                                scrap     = scrap,
                                onClick   = { onScrapClick(scrap.scrapId) },
                                onMarkTraded = {
                                    scope.launch {
                                        container.scrapRepository.updateStatus(
                                            scrap.scrapId,
                                            com.kutirakone.app.data.model.ScrapStatus.TRADED
                                        )
                                    }
                                }
                            )
                        }
                    }
                }

                // ── Tab 1: Incoming Requests (seller view) ────────
                if (selectedTab == 1) {
                    if (pendingIncoming.isEmpty()) {
                        item {
                            EmptyState(
                                emoji = "📭",
                                title = "No pending requests",
                                subtitle = "When someone requests your fabric, it appears here"
                            )
                        }
                    } else {
                        item {
                            Surface(color = Green50, shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    "✅ Accept to confirm trade  ❌ Reject to decline",
                                    fontSize = 12.sp, color = Green600,
                                    modifier = Modifier.padding(10.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        items(pendingIncoming) { request ->
                            IncomingRequestCard(
                                request   = request,
                                onAccept  = {
                                    scope.launch {
                                        // Update status in Firestore
                                        db.collection("swapRequests")
                                            .document(request.requestId)
                                            .update("status", SwapStatus.ACCEPTED.name)
                                            .await()
                                        // Refresh list
                                        incomingRequests = incomingRequests.map {
                                            if (it.requestId == request.requestId)
                                                it.copy(status = SwapStatus.ACCEPTED)
                                            else it
                                        }
                                    }
                                },
                                onReject  = {
                                    scope.launch {
                                        db.collection("swapRequests")
                                            .document(request.requestId)
                                            .update("status", SwapStatus.REJECTED.name)
                                            .await()
                                        incomingRequests = incomingRequests.filter {
                                            it.requestId != request.requestId
                                        }
                                    }
                                }
                            )
                        }
                    }
                }

                // ── Tab 2: Outgoing Requests (buyer view) ─────────
                if (selectedTab == 2) {
                    if (pendingOutgoing.isEmpty() && acceptedTrades.isEmpty()) {
                        item {
                            EmptyState(
                                emoji = "🤝",
                                title = "No trade requests sent",
                                subtitle = "Browse fabric scraps and tap 'Request Trade' to start"
                            )
                        }
                    }

                    if (acceptedTrades.isNotEmpty()) {
                        item {
                            Text("ACCEPTED — Ready to meet!",
                                fontSize = 11.sp, color = Green600,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 0.5.sp)
                        }
                        items(acceptedTrades) { request ->
                            OutgoingRequestCard(request = request, isAccepted = true)
                        }
                    }

                    if (pendingOutgoing.isNotEmpty()) {
                        item {
                            Text("WAITING FOR RESPONSE",
                                fontSize = 11.sp, color = Color.Gray,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 0.5.sp)
                        }
                        items(pendingOutgoing) { request ->
                            OutgoingRequestCard(request = request, isAccepted = false)
                        }
                    }
                }

                // ── Tab 3: Trade History ───────────────────────────
                if (selectedTab == 3) {
                    val rejectedOut = outgoingRequests.filter {
                        it.status == SwapStatus.REJECTED }

                    if (tradedListings.isEmpty() && rejectedOut.isEmpty()) {
                        item {
                            EmptyState(
                                emoji = "📋",
                                title = "No trade history yet",
                                subtitle = "Completed and rejected trades will appear here"
                            )
                        }
                    }

                    if (tradedListings.isNotEmpty()) {
                        item {
                            Text("COMPLETED TRADES",
                                fontSize = 11.sp, color = Color.Gray,
                                fontWeight = FontWeight.Medium, letterSpacing = 0.5.sp)
                        }
                        items(tradedListings) { scrap ->
                            TradeHistoryCard(
                                emoji    = materialEmoji(scrap.materialType),
                                title    = "${scrap.materialType.name} ${scrap.color}",
                                subtitle = "Traded · ${scrap.neighborhood}",
                                status   = "Done",
                                statusBg = Green50,
                                statusFg = Green600,
                                onRate   = { onRateUser("trade_${scrap.scrapId}") }
                            )
                        }
                    }

                    if (rejectedOut.isNotEmpty()) {
                        item {
                            Spacer(Modifier.height(4.dp))
                            Text("REJECTED REQUESTS",
                                fontSize = 11.sp, color = Color.Gray,
                                fontWeight = FontWeight.Medium, letterSpacing = 0.5.sp)
                        }
                        items(rejectedOut) { request ->
                            TradeHistoryCard(
                                emoji    = "❌",
                                title    = "Swap request rejected",
                                subtitle = "Your offer was not accepted",
                                status   = "Rejected",
                                statusBg = Coral50,
                                statusFg = Coral600
                            )
>>>>>>> d94e41b (Initial project upload)
                        }
                    }
                }
            }
<<<<<<< HEAD

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
=======
        }
    }
}

// ── Reusable components ───────────────────────────────────────────

@Composable
fun EmptyState(emoji: String, title: String, subtitle: String) {
    Box(Modifier.fillMaxWidth().padding(vertical = 32.dp),
        contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(emoji, fontSize = 40.sp)
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Text(subtitle, fontSize = 13.sp, color = Color.Gray,
                textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun ListingCard(scrap: Scrap, onClick: () -> Unit, onMarkTraded: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).background(Color(0xFFEAF3DE), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center) {
                Text(materialEmoji(scrap.materialType), fontSize = 22.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("${scrap.materialType.name} ${scrap.color} (${scrap.sizeMeters}m)",
                    fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Text("${scrap.mode.name} · ${scrap.neighborhood}",
                    fontSize = 11.sp, color = Color.Gray)
            }
            Column(horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Surface(color = Teal50, shape = RoundedCornerShape(8.dp)) {
                    Text("Active", color = Teal600, fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                }
                TextButton(onClick = onMarkTraded,
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)) {
                    Text("Mark Traded", fontSize = 10.sp, color = Color.Gray)
                }
>>>>>>> d94e41b (Initial project upload)
            }
        }
    }
}

@Composable
<<<<<<< HEAD
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
=======
fun IncomingRequestCard(
    request: SwapRequest,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)) {
        Column(Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(40.dp).background(Amber50, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center) {
                    Text("🔄", fontSize = 20.sp)
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text("Swap offer received",
                        fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text("${request.offeredScrapIds.size} scrap(s) offered in exchange",
                        fontSize = 12.sp, color = Color.Gray)
                }
                Surface(color = Amber50, shape = RoundedCornerShape(8.dp)) {
                    Text("Pending", color = Amber600, fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                }
            }

            Text("Buyer wants your fabric. Do you accept this swap?",
                fontSize = 12.sp, color = Color.Gray, lineHeight = 18.sp)

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // Accept
                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Green600),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Filled.Check, null,
                        modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Accept", fontSize = 13.sp)
                }
                // Reject
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier.weight(1f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Coral600),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Filled.Close, null,
                        tint = Coral600, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Reject", fontSize = 13.sp, color = Coral600)
                }
            }
        }
    }
}

@Composable
fun OutgoingRequestCard(request: SwapRequest, isAccepted: Boolean) {
    val bg    = if (isAccepted) Green50   else Color(0xFFF8F8F8)
    val badge = if (isAccepted) "Accepted ✅" else "Waiting ⏳"
    val badgeBg = if (isAccepted) Green50 else Amber50
    val badgeFg = if (isAccepted) Green600 else Amber600

    Card(modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bg),
        elevation = CardDefaults.cardElevation(1.dp)) {
        Column(Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(if (isAccepted) "✅" else "⏳", fontSize = 24.sp)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(if (isAccepted) "Trade Accepted!"
                    else "Request Sent",
                        fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text("Offered ${request.offeredScrapIds.size} scrap(s)",
                        fontSize = 12.sp, color = Color.Gray)
                }
                Surface(color = badgeBg, shape = RoundedCornerShape(8.dp)) {
                    Text(badge, color = badgeFg, fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                }
            }
            if (isAccepted) {
                Surface(color = Green600.copy(0.1f), shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "🤝 Seller accepted your offer!\nContact them to arrange the exchange meetup.",
                        fontSize = 12.sp, color = Green600, lineHeight = 18.sp,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            } else {
                Text("Waiting for seller to respond...",
                    fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun TradeHistoryCard(
    emoji: String, title: String, subtitle: String,
    status: String, statusBg: Color, statusFg: Color,
    onRate: (() -> Unit)? = null
) {
    Card(modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).background(Color(0xFFF1EFE8), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center) {
                Text(emoji, fontSize = 22.sp)
            }
>>>>>>> d94e41b (Initial project upload)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Text(subtitle, fontSize = 11.sp, color = Color.Gray,
                    modifier = Modifier.padding(top = 2.dp))
            }
            Column(horizontalAlignment = Alignment.End) {
<<<<<<< HEAD
                Surface(color = badgeBg, shape = RoundedCornerShape(8.dp)) {
                    Text(badge, color = badgeText,
                        fontSize = 10.sp, fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                }
                if (onRate != null) {
                    TextButton(onClick = onRate, contentPadding = PaddingValues(0.dp)) {
                        Text("Rate", fontSize = 10.sp, color = Amber600)
=======
                Surface(color = statusBg, shape = RoundedCornerShape(8.dp)) {
                    Text(status, color = statusFg, fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                }
                if (onRate != null) {
                    TextButton(onClick = onRate,
                        contentPadding = PaddingValues(0.dp)) {
                        Text("⭐ Rate", fontSize = 11.sp, color = Amber600)
>>>>>>> d94e41b (Initial project upload)
                    }
                }
            }
        }
    }
}