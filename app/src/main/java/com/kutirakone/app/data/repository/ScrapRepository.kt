package com.kutirakone.app.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.kutirakone.app.data.model.Scrap
import com.kutirakone.app.data.model.ScrapStatus
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import java.io.ByteArrayOutputStream
import java.util.UUID

class ScrapRepository(private val context: Context) {

    private val firestore = FirebaseFirestore.getInstance()
    private val storage   = FirebaseStorage.getInstance()
    private val auth      = FirebaseAuth.getInstance()
    private val scrapsCol = firestore.collection("scraps")

    val currentUserId: String get() = auth.currentUser?.uid ?: "guest"

    // Real-time nearby scraps
    fun getNearby(
        centerLat: Double,
        centerLng: Double,
        radiusKm: Double = 10.0
    ): Flow<List<Scrap>> = callbackFlow {
        val listener = scrapsCol
            .whereEqualTo("status", ScrapStatus.ACTIVE.name)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val list = snapshot?.toObjects(Scrap::class.java) ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    // Get single scrap
    suspend fun getScrap(scrapId: String): Scrap? {
        return try {
            withTimeout(5_000) {
                scrapsCol.document(scrapId).get().await()
                    .toObject(Scrap::class.java)
            }
        } catch (e: Exception) { null }
    }

    // Upload photo and return download URL
    suspend fun uploadPhoto(uri: Uri): String {
        return try {
            withTimeout(30_000) {
                val compressed = compressImage(uri)
                val filename   = "${UUID.randomUUID()}.jpg"
                val ref = storage.reference
                    .child("scrap_photos/$currentUserId/$filename")
                ref.putBytes(compressed).await()
                // Return the HTTPS download URL — this is what shows in the app
                ref.downloadUrl.await().toString()
            }
        } catch (e: Exception) {
            ""
        }
    }

    // Create listing — saves immediately, photo added after upload
    suspend fun createListing(scrap: Scrap): String {
        return try {
            val docRef   = scrapsCol.document()
            val newScrap = scrap.copy(scrapId = docRef.id, sellerId = currentUserId)
            docRef.set(newScrap).await()
            docRef.id
        } catch (e: Exception) { "" }
    }

    // Update photo URLs after background upload completes
    suspend fun updatePhotos(scrapId: String, photoUrls: List<String>) {
        try {
            scrapsCol.document(scrapId)
                .update("photos", photoUrls)
                .await()
        } catch (e: Exception) { }
    }

    // Update status
    suspend fun updateStatus(scrapId: String, status: ScrapStatus) {
        try {
            scrapsCol.document(scrapId).update("status", status.name).await()
        } catch (e: Exception) { }
    }

    // My listings
    fun getMyListings(): Flow<List<Scrap>> = callbackFlow {
        val listener = scrapsCol
            .whereEqualTo("sellerId", currentUserId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                trySend(snapshot?.toObjects(Scrap::class.java) ?: emptyList())
            }
        awaitClose { listener.remove() }
    }

    // Compress image to under 200 KB
    private fun compressImage(uri: Uri): ByteArray {
        val stream   = context.contentResolver.openInputStream(uri)
        val original = BitmapFactory.decodeStream(stream)
        stream?.close()

        val output  = ByteArrayOutputStream()
        var quality = 85
        do {
            output.reset()
            original.compress(Bitmap.CompressFormat.JPEG, quality, output)
            quality -= 10
        } while (output.toByteArray().size > 200 * 1024 && quality > 10)

        return output.toByteArray()
    }
}