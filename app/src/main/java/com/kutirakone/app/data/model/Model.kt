package com.kutirakone.app.data.model
// Firebase imports — required for Firestore annotations and types
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ServerTimestamp

// NOTE: CachedScrap (Room entity) is in its own file: CachedScrap.kt
// Room imports cannot be mixed in the same file after Firebase declarations

// ─────────────────────────────────────────────────────────────────
//  Firestore collection: "scraps"
// ─────────────────────────────────────────────────────────────────
data class Scrap(
    @DocumentId
    val scrapId: String = "",

    val sellerId: String = "",
    val sellerName: String = "",
    val sellerRating: Float = 0f,

    val photos: List<String> = emptyList(),   // Firebase Storage download URLs

    val materialType: MaterialType = MaterialType.COTTON,
    val aiMaterialConfidence: Float = 0f,     // 0.0 – 1.0  (FR-03)
    val sizeMeters: Double = 0.0,
    val color: String = "",
    val condition: Condition = Condition.GOOD,

    val mode: ListingMode = ListingMode.SWAP, // swap | free | sell
    val priceRs: Int = 0,                     // only used when mode == SELL

    val geoPoint: GeoPoint? = null,           // coarse neighborhood point (NFR-07)
    val neighborhood: String = "",

    @ServerTimestamp
    val createdAt: Timestamp? = null,
    val expiresAt: Timestamp? = null,
    val status: ScrapStatus = ScrapStatus.ACTIVE
)

enum class MaterialType { SILK, COTTON, WOOL, SYNTHETIC }
enum class Condition    { LIKE_NEW, GOOD, USED }
enum class ListingMode  { SWAP, FREE, SELL }
enum class ScrapStatus  { ACTIVE, TRADED, EXPIRED, DELETED }

// ─────────────────────────────────────────────────────────────────
//  Firestore collection: "users"
// ─────────────────────────────────────────────────────────────────
data class KutiraUser(
    @DocumentId
    val userId: String = "",

    val name: String = "",
    val phone: String = "",
    val role: UserRole = UserRole.BOTH,
    val profilePhoto: String = "",

    val neighborhood: String = "",
    val geoPoint: GeoPoint? = null,          // coarse, neighborhood-level (NFR-07)

    val rating: Float = 0f,
    val totalRatings: Int = 0,
    val totalTrades: Int = 0,

    val sustainabilityScore: Int = 0,        // gamification (Good-to-Have)
    val wasteKgSaved: Double = 0.0,

    val preferredLanguage: Language = Language.ENGLISH,

    @ServerTimestamp
    val createdAt: Timestamp? = null
)

enum class UserRole { TAILOR, ARTISAN, BOTH }
enum class Language { ENGLISH, KANNADA, HINDI }

// ─────────────────────────────────────────────────────────────────
//  Firestore collection: "swapRequests"
// ─────────────────────────────────────────────────────────────────
data class SwapRequest(
    @DocumentId
    val requestId: String = "",

    val buyerId: String = "",
    val sellerId: String = "",
    val targetScrapId: String = "",          // the scrap the buyer wants
    val offeredScrapIds: List<String> = emptyList(), // buyer's offer

    val status: SwapStatus = SwapStatus.PENDING,

    @ServerTimestamp
    val createdAt: Timestamp? = null,
    val resolvedAt: Timestamp? = null
)

enum class SwapStatus { PENDING, ACCEPTED, REJECTED, CANCELLED }

// ─────────────────────────────────────────────────────────────────
//  Firestore collection: "reviews"
// ─────────────────────────────────────────────────────────────────
data class Review(
    @DocumentId
    val reviewId: String = "",

    val fromUserId: String = "",
    val toUserId: String = "",
    val tradeId: String = "",
    val rating: Int = 5,                    // 1–5 stars (FR-09)
    val comment: String = "",

    @ServerTimestamp
    val createdAt: Timestamp? = null
)

// ─────────────────────────────────────────────────────────────────
//  Firestore collection: "designIdeas"  (AI-generated, cached)
// ─────────────────────────────────────────────────────────────────
data class DesignIdea(
    @DocumentId
    val ideaId: String = "",

    val scrapMaterial: MaterialType = MaterialType.COTTON,
    val scrapSizeRange: String = "",         // e.g. "0.5-1.0m"
    val projectName: String = "",
    val difficulty: Difficulty = Difficulty.EASY,
    val estimatedMinutes: Int = 30,
    val stepsPreview: String = "",
    val materialsNeeded: List<String> = emptyList(),
    val aiGenerated: Boolean = true
)

enum class Difficulty { EASY, MEDIUM, HARD }