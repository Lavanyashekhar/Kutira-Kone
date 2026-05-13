package com.kutirakone.app.ui.screens.swap

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kutirakone.app.KutiraKoneApp
import com.kutirakone.app.data.model.Scrap
import com.kutirakone.app.ui.common.materialEmoji
import com.kutirakone.app.ui.common.sampleScraps
import com.kutirakone.app.ui.theme.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwapRequestScreen(
    targetScrapId: String,
    onRequestSent: () -> Unit,
    onBackClick: () -> Unit
) {
    val context   = LocalContext.current
    val container = (context.applicationContext as KutiraKoneApp).container
    val scope     = rememberCoroutineScope()

    var target      by remember { mutableStateOf<Scrap?>(null) }
    var myInventory by remember { mutableStateOf<List<Scrap>>(emptyList()) }
    var selectedIds by remember { mutableStateOf(setOf<String>()) }
    var isSending   by remember { mutableStateOf(false) }
    var isSuccess   by remember { mutableStateOf(false) }
    var errorMsg    by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        // Load target scrap
        target = container.scrapRepository.getScrap(targetScrapId)
            ?: sampleScraps.find { it.scrapId == targetScrapId }
                    ?: sampleScraps.first()

        // Load my inventory
        container.scrapRepository.getMyListings()
            .catch { myInventory = sampleScraps.drop(1).take(3) }
            .collect { list ->
                myInventory = list.ifEmpty { sampleScraps.drop(1).take(3) }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Request Trade", fontWeight = FontWeight.SemiBold) },
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

        // Show success screen after sending
        if (isSuccess) {
            Box(Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(32.dp)) {

                    Text("✅", fontSize = 56.sp)

                    Text("Request Sent!", fontSize = 22.sp,
                        fontWeight = FontWeight.Bold, color = Green600)

                    Text(
                        "Your trade request has been sent to the seller.\nYou will be notified when they respond.",
                        fontSize = 14.sp, color = Color.Gray,
                        textAlign = TextAlign.Center, lineHeight = 22.sp
                    )

                    // How it works info
                    Surface(color = Green50, shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("What happens next?", fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold, color = Green600)

                            listOf(
                                "📱" to "Seller gets a notification about your offer",
                                "✅" to "If accepted — contact seller to arrange meetup",
                                "❌" to "If rejected — try offering different scraps",
                                "🤝" to "Meet in person and exchange the fabric"
                            ).forEach { (emoji, text) ->
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.Top) {
                                    Text(emoji, fontSize = 16.sp)
                                    Text(text, fontSize = 12.sp,
                                        color = Color.Gray, lineHeight = 18.sp,
                                        modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    Text("Track your request in My Trades tab",
                        fontSize = 12.sp, color = Color.Gray,
                        textAlign = TextAlign.Center)

                    Button(
                        onClick = onRequestSent,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape  = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Green600)
                    ) {
                        Text("Go to My Trades", fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            return@Scaffold
        }

        Column(Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(
                Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Target scrap
                item {
                    Surface(color = Color(0xFFF8F8F8),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(14.dp)) {
                            Text("You are requesting:", fontSize = 12.sp, color = Color.Gray)
                            Spacer(Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Box(Modifier.size(40.dp)
                                    .background(Color(0xFFEAF3DE), RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center) {
                                    Text(materialEmoji(target?.materialType
                                        ?: com.kutirakone.app.data.model.MaterialType.COTTON),
                                        fontSize = 20.sp)
                                }
                                Column {
                                    Text("${target?.materialType?.name ?: ""} ${target?.color ?: ""}",
                                        fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                    Text("${target?.sizeMeters ?: ""}m · ${target?.mode?.name ?: ""}",
                                        fontSize = 12.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }

                item {
                    Text("Select scraps from your inventory to offer:",
                        fontSize = 13.sp, color = Color.Gray,
                        fontWeight = FontWeight.Medium)
                }

                if (myInventory.isEmpty()) {
                    item {
                        Surface(color = Color(0xFFFFF8E1),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("📦", fontSize = 32.sp)
                                Spacer(Modifier.height(8.dp))
                                Text("No scraps in your inventory",
                                    fontSize = 13.sp, fontWeight = FontWeight.Medium,
                                    color = Color(0xFF795548))
                                Text("Upload some fabric scraps first to offer in trade",
                                    fontSize = 12.sp, color = Color.Gray,
                                    textAlign = TextAlign.Center)
                            }
                        }
                    }
                } else {
                    items(myInventory) { scrap ->
                        val selected = scrap.scrapId in selectedIds
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedIds = if (selected)
                                        selectedIds - scrap.scrapId
                                    else selectedIds + scrap.scrapId
                                }
                                .border(
                                    width = if (selected) 1.5.dp else 0.5.dp,
                                    color = if (selected) Green600 else Color.LightGray,
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            shape  = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selected) Green50 else Color.White
                            ),
                            elevation = CardDefaults.cardElevation(
                                if (selected) 0.dp else 1.dp
                            )
                        ) {
                            Row(Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    Modifier.size(44.dp)
                                        .background(Color(0xFFF1EFE8), RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(materialEmoji(scrap.materialType), fontSize = 22.sp)
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text("${scrap.materialType.name} ${scrap.color} (${scrap.sizeMeters}m)",
                                        fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                    Text("${scrap.condition.name.replace("_"," ")} · ${scrap.mode.name}",
                                        fontSize = 11.sp, color = Color.Gray)
                                }
                                if (selected) {
                                    Icon(Icons.Filled.CheckCircle, null,
                                        tint = Green600, modifier = Modifier.size(22.dp))
                                }
                            }
                        }
                    }
                }

                // Info box
                item {
                    Surface(color = Color(0xFFE1F5EE),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("ℹ️ How trade request works:",
                                fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF0F6E56))
                            Text("1. You select scraps to offer",
                                fontSize = 11.sp, color = Color(0xFF0F6E56))
                            Text("2. Seller gets notified instantly",
                                fontSize = 11.sp, color = Color(0xFF0F6E56))
                            Text("3. Seller accepts or rejects in My Trades",
                                fontSize = 11.sp, color = Color(0xFF0F6E56))
                            Text("4. You get notified of their response",
                                fontSize = 11.sp, color = Color(0xFF0F6E56))
                            Text("5. Meet in person to exchange fabric",
                                fontSize = 11.sp, color = Color(0xFF0F6E56))
                        }
                    }
                }

                if (errorMsg.isNotEmpty()) {
                    item {
                        Text(errorMsg, color = Color.Red, fontSize = 13.sp)
                    }
                }
            }

            // Send button
            Surface(shadowElevation = 8.dp) {
                Button(
                    onClick = {
                        if (selectedIds.isEmpty()) {
                            errorMsg = "⚠️ Please select at least one scrap to offer"
                            return@Button
                        }
                        isSending = true
                        errorMsg  = ""
                        scope.launch {
                            val success = container.swapRepository.sendSwapRequest(
                                targetScrapId   = targetScrapId,
                                sellerId        = target?.sellerId ?: "",
                                offeredScrapIds = selectedIds.toList()
                            )
                            isSending = false
                            if (success) {
                                isSuccess = true
                            } else {
                                errorMsg = "❌ Failed to send. Check your internet."
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(52.dp),
                    shape  = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Green600),
                    enabled = !isSending
                ) {
                    if (isSending) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CircularProgressIndicator(color = Color.White,
                                modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Text("Sending...", fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold)
                        }
                    } else {
                        Text(
                            if (selectedIds.isEmpty()) "Select scraps to offer"
                            else "Send Trade Request (${selectedIds.size} selected)",
                            fontSize = 14.sp, fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}