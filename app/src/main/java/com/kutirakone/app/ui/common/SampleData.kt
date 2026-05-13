package com.kutirakone.app.ui.common



import com.kutirakone.app.data.model.Condition
import com.kutirakone.app.data.model.ListingMode
import com.kutirakone.app.data.model.MaterialType
import com.kutirakone.app.data.model.Scrap
import com.kutirakone.app.data.model.ScrapStatus

// Shared helper — material emoji
fun materialEmoji(type: MaterialType) = when (type) {
    MaterialType.SILK      -> "🌸"
    MaterialType.COTTON    -> "🌿"
    MaterialType.WOOL      -> "🧶"
    MaterialType.SYNTHETIC -> "⚡"
}

// Shared sample data — used as fallback when Firestore is empty
val sampleScraps = listOf(
    Scrap(scrapId = "1", materialType = MaterialType.SILK,
        color = "Green",  sizeMeters = 0.8, mode = ListingMode.SWAP,
        neighborhood = "Mangaluru",   sellerRating = 4.9f,
        condition = Condition.GOOD,   status = ScrapStatus.ACTIVE),
    Scrap(scrapId = "2", materialType = MaterialType.COTTON,
        color = "Orange", sizeMeters = 1.5, mode = ListingMode.FREE,
        neighborhood = "Bejai",       sellerRating = 4.5f,
        condition = Condition.LIKE_NEW, status = ScrapStatus.ACTIVE),
    Scrap(scrapId = "3", materialType = MaterialType.WOOL,
        color = "Red",    sizeMeters = 2.0, mode = ListingMode.SELL,
        neighborhood = "Kadri",       sellerRating = 4.8f,
        condition = Condition.GOOD,   status = ScrapStatus.ACTIVE),
    Scrap(scrapId = "4", materialType = MaterialType.SYNTHETIC,
        color = "Blue",   sizeMeters = 0.5, mode = ListingMode.SWAP,
        neighborhood = "Hampankatta", sellerRating = 3.5f,
        condition = Condition.USED,   status = ScrapStatus.ACTIVE),
    Scrap(scrapId = "5", materialType = MaterialType.SILK,
        color = "Yellow", sizeMeters = 0.3, mode = ListingMode.FREE,
        neighborhood = "Balmatta",    sellerRating = 4.2f,
        condition = Condition.USED,   status = ScrapStatus.ACTIVE),
    Scrap(scrapId = "6", materialType = MaterialType.COTTON,
        color = "Purple", sizeMeters = 1.2, mode = ListingMode.SELL,
        neighborhood = "Falnir",      sellerRating = 4.9f,
        condition = Condition.LIKE_NEW, status = ScrapStatus.ACTIVE),
)