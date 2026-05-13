package com.kutirakone.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kutirakone.app.KutiraKoneApp
import com.kutirakone.app.data.model.KutiraUser
import com.kutirakone.app.data.model.Scrap
import com.kutirakone.app.data.model.UserRole
import com.kutirakone.app.ui.common.materialEmoji
import com.kutirakone.app.ui.theme.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onSignOut: () -> Unit,
    onBackClick: () -> Unit
) {
    val context    = LocalContext.current
    val container  = (context.applicationContext as KutiraKoneApp).container
    val auth       = FirebaseAuth.getInstance()
    val db         = FirebaseFirestore.getInstance()
    val scope      = rememberCoroutineScope()

    // Get uid safely — empty string if guest
    val currentUid = auth.currentUser?.uid ?: ""
    val phone      = auth.currentUser?.phoneNumber ?: "Guest User"
    val isGuest    = currentUid.isEmpty()

    var user           by remember { mutableStateOf<KutiraUser?>(null) }
    var myListings     by remember { mutableStateOf<List<Scrap>>(emptyList()) }
    var isLoading      by remember { mutableStateOf(true) }
    var showEditDialog by remember { mutableStateOf(false) }

    var editName         by remember { mutableStateOf("") }
    var editRole         by remember { mutableStateOf(UserRole.BOTH) }
    var editNeighborhood by remember { mutableStateOf("") }
    var isSaving         by remember { mutableStateOf(false) }
    var saveMsg          by remember { mutableStateOf("") }

    // Load user profile — only if logged in
    LaunchedEffect(currentUid) {
        if (!isGuest) {
            try {
                val doc = db.collection("users").document(currentUid).get().await()
                user = doc.toObject(KutiraUser::class.java)
                // If user doc doesn't exist yet, create it
                if (user == null) {
                    val newUser = KutiraUser(
                        userId = currentUid,
                        phone  = phone,
                        name   = "",
                        role   = UserRole.BOTH,
                        neighborhood = "Mangaluru"
                    )
                    db.collection("users").document(currentUid).set(newUser).await()
                    user = newUser
                }
                editName         = user?.name ?: ""
                editRole         = user?.role ?: UserRole.BOTH
                editNeighborhood = user?.neighborhood ?: ""
            } catch (e: Exception) {
                // Network error — use defaults
                editName = ""; editRole = UserRole.BOTH; editNeighborhood = ""
            }
        }
        isLoading = false
    }

    // Load my listings — only if logged in
    LaunchedEffect(currentUid) {
        if (!isGuest) {
            container.scrapRepository.getMyListings()
                .catch { }
                .collect { list -> myListings = list }
        } else {
            isLoading = false
        }
    }

    val displayName = when {
        isGuest            -> "Guest"
        user?.name?.isNotEmpty() == true -> user!!.name
        else               -> phone
    }
    val initials    = displayName.take(2).uppercase()
    val role        = user?.role?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Artisan"
    val neighborhood = user?.neighborhood?.ifEmpty { "Mangaluru" } ?: "Mangaluru"
    val rating      = if ((user?.rating ?: 0f) > 0f) user!!.rating else 4.5f
    val wasteKg     = user?.wasteKgSaved ?: (myListings.size * 0.5)
    val susScore    = ((wasteKg / 10.0) * 100).coerceIn(0.0, 100.0).toInt()
        .coerceAtLeast(if (myListings.isNotEmpty()) 20 else 0)
    val tradeCount  = myListings.count { it.status.name == "TRADED" }
    val activeCount = myListings.count { it.status.name == "ACTIVE" }

    // Edit Profile Dialog
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false; saveMsg = "" },
            title = { Text("Edit Profile", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text  = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Full Name") },
                        placeholder = { Text("e.g. Lavanya") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        leadingIcon = { Icon(Icons.Filled.Person, null, tint = Green600) },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editNeighborhood,
                        onValueChange = { editNeighborhood = it },
                        label = { Text("Neighborhood / Area") },
                        placeholder = { Text("e.g. Padil, Mangaluru") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        leadingIcon = { Icon(Icons.Filled.LocationOn, null, tint = Green600) },
                        singleLine = true
                    )
                    Text("Your Role", fontSize = 12.sp, color = Color.Gray)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            UserRole.TAILOR  to "🧵 Tailor",
                            UserRole.ARTISAN to "🎨 Artisan",
                            UserRole.BOTH    to "✨ Both"
                        ).forEach { (r, label) ->
                            val sel = editRole == r
                            OutlinedButton(
                                onClick = { editRole = r },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (sel) Green50 else Color.White),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp, if (sel) Green600 else Color.LightGray),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                Text(label, fontSize = 10.sp,
                                    color = if (sel) Green600 else Color.Gray)
                            }
                        }
                    }
                    if (saveMsg.isNotEmpty()) {
                        Text(saveMsg, fontSize = 12.sp,
                            color = if (saveMsg.startsWith("✅")) Green600 else Color.Red,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth())
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editName.isBlank()) {
                            saveMsg = "⚠️ Please enter your name"
                            return@Button
                        }
                        // Block save for guest users
                        if (isGuest) {
                            saveMsg = "⚠️ Please login first to save profile"
                            return@Button
                        }
                        isSaving = true
                        saveMsg  = "Saving..."
                        scope.launch {
                            try {
                                // Use set with merge to create doc if doesn't exist
                                val updates = mapOf(
                                    "userId"       to currentUid,
                                    "name"         to editName.trim(),
                                    "role"         to editRole.name,
                                    "neighborhood" to editNeighborhood.trim(),
                                    "phone"        to phone
                                )
                                db.collection("users")
                                    .document(currentUid)
                                    .set(updates, com.google.firebase.firestore.SetOptions.merge())
                                    .await()

                                user = user?.copy(
                                    name         = editName.trim(),
                                    role         = editRole,
                                    neighborhood = editNeighborhood.trim()
                                ) ?: KutiraUser(
                                    userId       = currentUid,
                                    name         = editName.trim(),
                                    role         = editRole,
                                    neighborhood = editNeighborhood.trim(),
                                    phone        = phone
                                )
                                saveMsg  = "✅ Profile saved!"
                                isSaving = false
                                kotlinx.coroutines.delay(1000)
                                showEditDialog = false
                                saveMsg = ""
                            } catch (e: Exception) {
                                saveMsg  = "❌ Failed: ${e.message}"
                                isSaving = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Green600),
                    shape  = RoundedCornerShape(8.dp),
                    enabled = !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(color = Color.White,
                            modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Save")
                    }
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showEditDialog = false; saveMsg = "" },
                    shape = RoundedCornerShape(8.dp)) { Text("Cancel") }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile", fontWeight = FontWeight.SemiBold) },
                actions = {
                    if (!isGuest) {
                        IconButton(onClick = {
                            editName = user?.name ?: ""
                            editRole = user?.role ?: UserRole.BOTH
                            editNeighborhood = user?.neighborhood ?: ""
                            showEditDialog = true
                        }) {
                            Icon(Icons.Filled.Edit, "Edit Profile", tint = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Green600,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
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

        LazyColumn(Modifier.fillMaxSize().padding(padding)) {

            // Profile header
            item {
                Box(Modifier.fillMaxWidth().background(Green600)) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(64.dp).background(Color.White, CircleShape),
                                contentAlignment = Alignment.Center) {
                                Text(initials, fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold, color = Green600)
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Text(displayName, color = Color.White,
                                    fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                                Text("$role · $neighborhood",
                                    color = Color.White.copy(0.8f), fontSize = 13.sp)
                                Text(if (isGuest) "Guest mode" else phone,
                                    color = Color.White.copy(0.6f), fontSize = 11.sp,
                                    modifier = Modifier.padding(top = 2.dp))
                            }
                            if (!isGuest) {
                                IconButton(onClick = {
                                    editName = user?.name ?: ""
                                    editRole = user?.role ?: UserRole.BOTH
                                    editNeighborhood = user?.neighborhood ?: ""
                                    showEditDialog = true
                                }) {
                                    Icon(Icons.Filled.Edit, "Edit",
                                        tint = Color.White.copy(0.8f))
                                }
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                        Row(Modifier.fillMaxWidth()
                            .background(Color.White.copy(0.12f), RoundedCornerShape(12.dp))) {
                            listOf(
                                "$activeCount" to "Listings",
                                "${"%.1f".format(rating)}" to "Rating",
                                "$tradeCount" to "Trades",
                                "${"%.1f".format(wasteKg)}kg" to "Saved"
                            ).forEachIndexed { i, (num, lbl) ->
                                Column(Modifier.weight(1f).padding(vertical = 12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(num, color = Color.White,
                                        fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    Text(lbl, color = Color.White.copy(0.7f), fontSize = 10.sp)
                                }
                                if (i < 3) {
                                    Divider(modifier = Modifier.fillMaxHeight().width(0.5.dp)
                                        .padding(vertical = 8.dp),
                                        color = Color.White.copy(0.2f))
                                }
                            }
                        }
                    }
                }
            }

            // Guest warning
            if (isGuest) {
                item {
                    Spacer(Modifier.height(12.dp))
                    Surface(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        color = Amber50, shape = RoundedCornerShape(12.dp)) {
                        Row(Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Filled.Warning, null, tint = Amber600)
                            Column {
                                Text("You are browsing as Guest",
                                    fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                                    color = Amber600)
                                Text("Login with OTP to save your profile and listings",
                                    fontSize = 11.sp, color = Amber600.copy(0.8f))
                            }
                        }
                    }
                }
            }

            // Complete profile banner
            if (!isGuest && user?.name.isNullOrEmpty()) {
                item {
                    Spacer(Modifier.height(12.dp))
                    Surface(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        color = Amber50, shape = RoundedCornerShape(12.dp)) {
                        Row(Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Filled.Edit, null, tint = Amber600)
                            Column(Modifier.weight(1f)) {
                                Text("Complete your profile",
                                    fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                                    color = Amber600)
                                Text("Add your name, role and area",
                                    fontSize = 11.sp, color = Amber600.copy(0.7f))
                            }
                            TextButton(onClick = {
                                editName = ""; editRole = UserRole.BOTH
                                editNeighborhood = ""; showEditDialog = true
                            }) {
                                Text("Edit now", color = Amber600,
                                    fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }

            // Sustainability score
            item {
                Spacer(Modifier.height(12.dp))
                Surface(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    color = Green50, shape = RoundedCornerShape(12.dp)) {
                    Column(Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🌿", fontSize = 26.sp)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Sustainability Score: $susScore/100",
                                    fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                                    color = Green600)
                                Text("${"%.1f".format(wasteKg)} kg saved from landfill",
                                    fontSize = 12.sp, color = Green600.copy(0.7f))
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { (susScore / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth(),
                            color = Green600, trackColor = Green600.copy(0.2f)
                        )
                    }
                }
            }

            // My listings
            item {
                Spacer(Modifier.height(16.dp))
                Text("MY ACTIVE LISTINGS",
                    fontSize = 11.sp, color = Color.Gray,
                    fontWeight = FontWeight.Medium, letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(Modifier.height(8.dp))
            }

            val active = myListings.filter { it.status.name == "ACTIVE" }.take(10)
            if (active.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(16.dp),
                        contentAlignment = Alignment.Center) {
                        Text(
                            if (isGuest) "Login to see your listings"
                            else "No active listings yet.\nTap + to upload your first fabric scrap!",
                            fontSize = 13.sp, color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(active.size) { i ->
                    val scrap = active[i]
                    Card(modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(1.dp)) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(44.dp)
                                .background(Color(0xFFF1EFE8), RoundedCornerShape(8.dp)),
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
                            Surface(color = Color(0xFFE1F5EE), shape = RoundedCornerShape(8.dp)) {
                                Text("Active", color = Color(0xFF0F6E56),
                                    fontSize = 10.sp, fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                            }
                        }
                    }
                }
            }

            // Sign out
            item {
                Spacer(Modifier.height(24.dp))
                OutlinedButton(
                    onClick = { auth.signOut(); onSignOut() },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
                ) {
                    Icon(Icons.Filled.Logout, null, tint = Color.Gray,
                        modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (isGuest) "Exit Guest Mode" else "Sign Out",
                        color = Color.Gray, fontSize = 14.sp)
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}