package com.kutirakone.app.ui.screens.chat



import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kutirakone.app.ui.theme.Green600

data class ChatMessage(val text: String, val isMine: Boolean, val time: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(sellerId: String, scrapId: String, onBackClick: () -> Unit) {
    var input    by remember { mutableStateOf("") }
    val messages = remember {
        mutableStateListOf(
            ChatMessage("Hi, is the green silk scrap still available?", true, "9:41 AM"),
            ChatMessage("Yes it is! Good condition, freshly washed.", false, "9:43 AM"),
            ChatMessage("Great! I'd like to swap it for my orange cotton (1.5m). Interested?", true, "9:44 AM"),
            ChatMessage("That sounds fair! When can we meet?", false, "9:45 AM"),
        )
    }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        listState.animateScrollToItem(messages.size - 1)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Priya Devi", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        Text("Green Silk Scrap", fontSize = 11.sp,
                            color = Color.White.copy(0.75f))
                    }
                },
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
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).background(Color(0xFFF5F5F0)),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { msg ->
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = if (msg.isMine)
                            Arrangement.End else Arrangement.Start
                    ) {
                        Column(horizontalAlignment =
                            if (msg.isMine) Alignment.End else Alignment.Start
                        ) {
                            Surface(
                                color = if (msg.isMine) Green600 else Color.White,
                                shape = RoundedCornerShape(
                                    topStart = 12.dp, topEnd = 12.dp,
                                    bottomStart = if (msg.isMine) 12.dp else 2.dp,
                                    bottomEnd   = if (msg.isMine) 2.dp else 12.dp
                                ),
                                shadowElevation = 1.dp
                            ) {
                                Text(msg.text,
                                    color = if (msg.isMine) Color.White else Color.Black,
                                    fontSize = 13.sp, lineHeight = 19.sp,
                                    modifier = Modifier.padding(
                                        horizontal = 12.dp, vertical = 8.dp)
                                        .widthIn(max = 240.dp))
                            }
                            Text(msg.time, fontSize = 10.sp,
                                color = Color.Gray, modifier = Modifier.padding(top = 2.dp))
                        }
                    }
                }
            }
            // Input bar
            Surface(shadowElevation = 8.dp) {
                Row(
                    Modifier.fillMaxWidth().padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Type a message...") },
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = Green600,
                            unfocusedBorderColor = Color.LightGray
                        )
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (input.isNotBlank()) {
                                messages.add(ChatMessage(input.trim(), true, "Now"))
                                input = ""
                            }
                        },
                        modifier = Modifier.size(48.dp).background(Green600, RoundedCornerShape(50))
                    ) {
                        Icon(Icons.Filled.Send, null, tint = Color.White,
                            modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}