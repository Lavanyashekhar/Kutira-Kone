package com.kutirakone.app.ui.screens.design



import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kutirakone.app.ui.theme.Amber600
import com.kutirakone.app.ui.theme.Green600
import com.kutirakone.app.ui.theme.Purple50
import com.kutirakone.app.ui.theme.Purple600
import com.kutirakone.app.ui.theme.Teal600

data class IdeaItem(val emoji: String, val name: String, val difficulty: String,
                    val minutes: Int, val steps: String, val materials: List<String>)

val sampleIdeas = listOf(
    IdeaItem("🎭","Festive Fabric Mask","Easy",30,
        "Cut fabric to size, fold edges, stitch sides, add ear loops",
        listOf("Elastic band","Needle","Thread")),
    IdeaItem("👜","Silk Coin Pouch","Easy",45,
        "Cut two rectangles, sew 3 sides, add zip or button closure",
        listOf("Zipper or button","Needle","Thread")),
    IdeaItem("🌸","Hair Accessory Set","Medium",60,
        "Cut strips, fold and twist into scrunchies or clips",
        listOf("Hair tie","Bobby pins","Fabric glue")),
    IdeaItem("🪆","Patchwork Doll Dress","Medium",90,
        "Measure doll, cut panels, stitch together with seam allowance",
        listOf("Doll","Ribbon","Needle","Thread")),
    IdeaItem("🛍️","Gift Wrap Furoshiki","Easy",20,
        "Hem all edges, practice 3 basic wrapping techniques",
        listOf("Iron","Scissors")),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DesignIdeasScreen(scrapId: String, onBackClick: () -> Unit) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Design Ideas", fontWeight = FontWeight.SemiBold) },
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
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Surface(color = Purple50, shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("✨", fontSize = 22.sp)
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("Gemini AI generated these ideas",
                                fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                                color = Purple600)
                            Text("Based on material type, size and color",
                                fontSize = 11.sp, color = Purple600.copy(0.7f))
                        }
                    }
                }
            }
            itemsIndexed(sampleIdeas) { _, idea ->
                IdeaCard(idea)
            }
        }
    }
}

@Composable
fun IdeaCard(idea: IdeaItem) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(idea.emoji, fontSize = 26.sp)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(idea.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(idea.difficulty, fontSize = 11.sp,
                            color = if (idea.difficulty == "Easy") Teal600 else Amber600,
                            fontWeight = FontWeight.Medium)
                        Text("· ${idea.minutes} mins", fontSize = 11.sp, color = Color.Gray)
                    }
                }
                TextButton(onClick = { expanded = !expanded }) {
                    Text(if (expanded) "Less" else "More",
                        fontSize = 12.sp, color = Green600)
                }
            }
            if (expanded) {
                Divider(Modifier.padding(vertical = 10.dp))
                Text("Steps:", fontSize = 12.sp,
                    fontWeight = FontWeight.Medium, color = Color.DarkGray)
                Text(idea.steps, fontSize = 12.sp, color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp), lineHeight = 18.sp)
                Spacer(Modifier.height(8.dp))
                Text("You'll need:", fontSize = 12.sp,
                    fontWeight = FontWeight.Medium, color = Color.DarkGray)
                idea.materials.forEach { mat ->
                    Text("• $mat", fontSize = 12.sp, color = Color.Gray)
                }
            }
        }
    }
}