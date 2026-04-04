package com.shopsavvy.sdk

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// MARK: - API Meta

/**
 * API response metadata containing credit usage info
 */
@Serializable
data class ApiMeta(
    @SerialName("credits_used")
    val creditsUsed: Int,
    @SerialName("credits_remaining")
    val creditsRemaining: Int,
    @SerialName("rate_limit_remaining")
    val rateLimitRemaining: Int? = null
)

// MARK: - API Response

/**
 * Generic API response wrapper
 */
@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T,
    val message: String? = null,
    val meta: ApiMeta? = null
) {
    /** Get credits used from meta object */
    fun creditsUsed(): Int = meta?.creditsUsed ?: 0

    /** Get credits remaining from meta object */
    fun creditsRemaining(): Int = meta?.creditsRemaining ?: 0
}

// MARK: - Pagination

/**
 * Pagination info for search results
 */
@Serializable
data class PaginationInfo(
    val total: Int,
    val limit: Int,
    val offset: Int,
    val returned: Int
)

/**
 * Product search result with pagination
 */
@Serializable
data class ProductSearchResult(
    val success: Boolean,
    val data: List<ProductDetails>,
    val pagination: PaginationInfo? = null,
    val meta: ApiMeta? = null
) {
    /** Get credits used from meta object */
    fun creditsUsed(): Int = meta?.creditsUsed ?: 0

    /** Get credits remaining from meta object */
    fun creditsRemaining(): Int = meta?.creditsRemaining ?: 0
}

// MARK: - Product Models

/**
 * Product details model
 */
@Serializable
data class ProductDetails(
    val title: String,
    val shopsavvy: String,
    val brand: String? = null,
    val category: String? = null,
    val images: List<String>? = null,
    val barcode: String? = null,
    val amazon: String? = null,
    val model: String? = null,
    val mpn: String? = null,
    val color: String? = null,
    @SerialName("title_short") val titleShort: String? = null,
    val slug: String? = null,
    val description: String? = null,
    val categories: List<String>? = null,
    val attributes: Map<String, String>? = null,
    val rating: Map<String, kotlinx.serialization.json.JsonElement>? = null,
    val score: Map<String, kotlinx.serialization.json.JsonElement>? = null,
    val keywords: List<String>? = null,
    val identifiers: Map<String, kotlinx.serialization.json.JsonElement>? = null
) {
    // Backward-compatible aliases

    /** @deprecated Use title instead */
    @Deprecated("Use title instead", ReplaceWith("title"))
    val name: String get() = title

    /** @deprecated Use shopsavvy instead */
    @Deprecated("Use shopsavvy instead", ReplaceWith("shopsavvy"))
    val productId: String get() = shopsavvy

    /** @deprecated Use amazon instead */
    @Deprecated("Use amazon instead", ReplaceWith("amazon"))
    val asin: String? get() = amazon

    /** @deprecated Use images?.firstOrNull() instead */
    @Deprecated("Use images?.firstOrNull() instead", ReplaceWith("images?.firstOrNull()"))
    val imageUrl: String? get() = images?.firstOrNull()
}

/**
 * Product with nested offers (returned by offers endpoint)
 */
@Serializable
data class ProductWithOffers(
    val title: String,
    val shopsavvy: String,
    val brand: String? = null,
    val category: String? = null,
    val images: List<String>? = null,
    val barcode: String? = null,
    val amazon: String? = null,
    val model: String? = null,
    val mpn: String? = null,
    val color: String? = null,
    val offers: List<Offer> = emptyList()
)

// MARK: - Offer Models

/**
 * Historical price point
 */
@Serializable
data class PriceHistoryEntry(
    val date: String,
    val price: Double,
    val availability: String
)

/**
 * Product offer model
 */
@Serializable
data class Offer(
    val id: String,
    val retailer: String? = null,
    val price: Double? = null,
    val currency: String? = null,
    val availability: String? = null,
    val condition: String? = null,
    @SerialName("URL")
    val url: String? = null,
    val seller: String? = null,
    val timestamp: String? = null,
    val history: List<PriceHistoryEntry>? = null
) {
    // Backward-compatible aliases

    /** @deprecated Use id instead */
    @Deprecated("Use id instead", ReplaceWith("id"))
    val offerId: String get() = id

    /** @deprecated Use url instead */
    @Deprecated("Use url instead", ReplaceWith("url"))
    val offerUrl: String? get() = url

    /** @deprecated Use timestamp instead */
    @Deprecated("Use timestamp instead", ReplaceWith("timestamp"))
    val lastUpdated: String? get() = timestamp
}

/**
 * Offer with price history
 */
@Serializable
data class OfferWithHistory(
    val id: String,
    val retailer: String? = null,
    val price: Double? = null,
    val currency: String? = null,
    val availability: String? = null,
    val condition: String? = null,
    @SerialName("URL")
    val url: String? = null,
    val seller: String? = null,
    val timestamp: String? = null,
    @SerialName("price_history")
    val priceHistory: List<PriceHistoryEntry> = emptyList()
)

// MARK: - Monitoring Models

/**
 * Request model for scheduling product monitoring
 */
@Serializable
data class ScheduleRequest(
    val identifier: String,
    val frequency: String,
    val retailer: String? = null
)

/**
 * Response model for scheduling operations
 */
@Serializable
data class ScheduleResponse(
    val scheduled: Boolean,
    @SerialName("product_id")
    val productId: String
)

/**
 * Response from batch scheduling
 */
@Serializable
data class ScheduleBatchResponse(
    val identifier: String,
    val scheduled: Boolean,
    @SerialName("product_id")
    val productId: String
)

/**
 * Scheduled product model
 */
@Serializable
data class ScheduledProduct(
    @SerialName("product_id")
    val productId: String,
    val identifier: String,
    val frequency: String,
    val retailer: String? = null,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("last_refreshed")
    val lastRefreshed: String? = null
)

/**
 * Request model for removing scheduled products
 */
@Serializable
data class RemoveRequest(
    val identifier: String
)

/**
 * Response model for removal operations
 */
@Serializable
data class RemoveResponse(
    val removed: Boolean
)

/**
 * Response from batch removal
 */
@Serializable
data class RemoveBatchResponse(
    val identifier: String,
    val removed: Boolean
)

// MARK: - Usage Models

/**
 * Current billing period details
 */
@Serializable
data class UsagePeriod(
    @SerialName("start_date")
    val startDate: String,
    @SerialName("end_date")
    val endDate: String,
    @SerialName("credits_used")
    val creditsUsed: Int,
    @SerialName("credits_limit")
    val creditsLimit: Int,
    @SerialName("credits_remaining")
    val creditsRemaining: Int,
    @SerialName("requests_made")
    val requestsMade: Int
)

/**
 * API usage information model
 */
@Serializable
data class UsageInfo(
    @SerialName("current_period")
    val currentPeriod: UsagePeriod,
    @SerialName("usage_percentage")
    val usagePercentage: Double
) {
    // Backward-compatible methods

    /** @deprecated Use currentPeriod.creditsUsed instead */
    @Deprecated("Use currentPeriod.creditsUsed instead", ReplaceWith("currentPeriod.creditsUsed"))
    fun getCreditsUsed(): Int = currentPeriod.creditsUsed

    /** @deprecated Use currentPeriod.creditsRemaining instead */
    @Deprecated("Use currentPeriod.creditsRemaining instead", ReplaceWith("currentPeriod.creditsRemaining"))
    fun getCreditsRemaining(): Int = currentPeriod.creditsRemaining

    /** @deprecated Use currentPeriod.creditsLimit instead */
    @Deprecated("Use currentPeriod.creditsLimit instead", ReplaceWith("currentPeriod.creditsLimit"))
    fun getCreditsTotal(): Int = currentPeriod.creditsLimit

    /** @deprecated Use currentPeriod.startDate instead */
    @Deprecated("Use currentPeriod.startDate instead", ReplaceWith("currentPeriod.startDate"))
    fun getBillingPeriodStart(): String = currentPeriod.startDate

    /** @deprecated Use currentPeriod.endDate instead */
    @Deprecated("Use currentPeriod.endDate instead", ReplaceWith("currentPeriod.endDate"))
    fun getBillingPeriodEnd(): String = currentPeriod.endDate
}

// MARK: - Deals

@Serializable
data class Deal(
    val path: String,
    val title: String,
    val subtitle: String? = null,
    val description: String? = null,
    val emoji: String? = null,
    val grade: Map<String, kotlinx.serialization.json.JsonElement>,
    val pricing: Map<String, kotlinx.serialization.json.JsonElement>,
    val retailer: Map<String, String>,
    val product: String? = null,
    val url: String,
    val image: Map<String, kotlinx.serialization.json.JsonElement>? = null,
    val votes: Map<String, kotlinx.serialization.json.JsonElement>,
    @SerialName("comment_count") val commentCount: Int = 0,
    val tags: List<Map<String, String>>? = null,
    @SerialName("expires_at") val expiresAt: String? = null,
    @SerialName("created_at") val createdAt: String
)

@Serializable
data class DealsResponse(
    val success: Boolean,
    val deals: List<Deal>,
    val pagination: Map<String, kotlinx.serialization.json.JsonElement>,
    val meta: ApiMeta? = null
)

// MARK: - Reviews

@Serializable
data class TLDRReview(
    val slug: String,
    val headline: String,
    val pros: List<String> = emptyList(),
    val cons: List<String> = emptyList(),
    @SerialName("bottom_line") val bottomLine: String = "",
    val scores: Map<String, kotlinx.serialization.json.JsonElement>? = null
)

@Serializable
data class ReviewResponse(
    val success: Boolean,
    val product: Map<String, String>,
    val review: TLDRReview? = null,
    val meta: ApiMeta? = null
)
