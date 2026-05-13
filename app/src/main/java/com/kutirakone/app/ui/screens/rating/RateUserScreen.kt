package com.kutirakone.app.ui.screens.rating



import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kutirakone.app.ui.theme.Amber400
import com.kutirakone.app.ui.theme.Green600

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RateUserScreen(tradeId: String, onSubmitted: () -> Unit, onBackClick: () -> Unit) {
    var rating  by remember { mutableIntStateOf(0) }
    var comment by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rate Trader", fontWeight = FontWeight.SemiBold) },
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
            Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))
            Text("How was your trade?", fontSize = 20.sp,
                fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Text("Your honest rating helps the community",
                fontSize = 13.sp, color = Color.Gray,
                modifier = Modifier.padding(top = 6.dp))
            Spacer(Modifier.height(32.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (1..5).forEach { star ->
                    IconButton(onClick = { rating = star }, modifier = Modifier.size(48.dp)) {
                        Icon(
                            if (star <= rating) Icons.Filled.Star else Icons.Outlined.StarOutline,
                            contentDescription = "$star star",
                            tint   = if (star <= rating) Amber400 else Color.LightGray,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }
            Text(
                when (rating) {
                    1 -> "Poor 😞"; 2 -> "Fair 😐"; 3 -> "Good 🙂"
                    4 -> "Great 😊"; 5 -> "Excellent 🌟"; else -> "Tap to rate"
                },
                fontSize = 15.sp, color = if (rating > 0) Green600 else Color.Gray,
                modifier = Modifier.padding(top = 8.dp)
            )
            Spacer(Modifier.height(24.dp))
            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                placeholder = { Text("Share your experience (optional)") },
                shape = RoundedCornerShape(10.dp),
                maxLines = 4
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onSubmitted,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Green600),
                enabled = rating > 0
            ) {
                Text("Submit Rating", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}