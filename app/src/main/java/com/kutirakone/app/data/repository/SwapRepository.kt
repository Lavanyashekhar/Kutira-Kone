package com.kutirakone.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.kutirakone.app.data.model.SwapRequest
import com.kutirakone.app.data.model.SwapStatus
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout

class SwapRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth      = FirebaseAuth.getInstance()
    private val swapsCol  = firestore.collection("swapRequests")

    val currentUserId: String get() = auth.currentUser?.uid ?: ""

    // Send swap request — instant with 5 second timeout
    suspend fun sendSwapRequest(
        targetScrapId: String,
        sellerId: String,
        offeredScrapIds: List<String>
    ): Boolean {
        return try {
            withTimeout(5_000) {
                val docRef  = swapsCol.document()
                val request = SwapRequest(
                    requestId       = docRef.id,
                    buyerId         = currentUserId,
                    sellerId        = sellerId,
                    targetScrapId   = targetScrapId,
                    offeredScrapIds = offeredScrapIds,
                    status          = SwapStatus.PENDING
                )
                // set() without await — fires instantly, syncs in background
                docRef.set(request)
                true
            }
        } catch (e: Exception) { false }
    }

    // Get incoming requests (seller sees these)
    fun getIncomingRequests(): Flow<List<SwapRequest>> = callbackFlow {
        val listener = swapsCol
            .whereEqualTo("sellerId", currentUserId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                trySend(snap?.toObjects(SwapRequest::class.java) ?: emptyList())
            }
        awaitClose { listener.remove() }
    }

    // Get outgoing requests (buyer sees these — to track status)
    fun getOutgoingRequests(): Flow<List<SwapRequest>> = callbackFlow {
        val listener = swapsCol
            .whereEqualTo("buyerId", currentUserId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                trySend(snap?.toObjects(SwapRequest::class.java) ?: emptyList())
            }
        awaitClose { listener.remove() }
    }

    // Accept — instant
    suspend fun acceptRequest(requestId: String): Boolean {
        return try {
            swapsCol.document(requestId)
                .update("status", SwapStatus.ACCEPTED.name)
            true
        } catch (e: Exception) { false }
    }

    // Reject — instant
    suspend fun rejectRequest(requestId: String): Boolean {
        return try {
            swapsCol.document(requestId)
                .update("status", SwapStatus.REJECTED.name)
            true
        } catch (e: Exception) { false }
    }
}