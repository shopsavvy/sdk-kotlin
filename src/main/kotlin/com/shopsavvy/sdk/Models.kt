package com.shopsavvy.sdk

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// MARK: - API Response

/**
 * Generic API response wrapper
 */
@Serializable
data class ApiResponse<T>(
    val data: T,
    val meta: Meta
)

/**
 * API response metadata
 */
@Serializable
data class Meta(
    val requestId: String? = null,
    val timestamp: String? = null,
    val cached: Boolean? = null,
    @SerialName("credits_used")
    val creditsUsed: Int? = null
)

// MARK: - Product Models

/**
 * Product details model
 */
@Serializable
data class ProductDetails(
    val id: String,
    val name: String,
    val description: String? = null,
    val brand: String? = null,
    val category: String? = null,
    val upc: String? = null,
    val asin: String? = null,
    @SerialName("model_number")
    val modelNumber: String? = null,
    val images: List<String>? = null,
    val specifications: Map<String, String>? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)

/**
 * Product offer model
 */
@Serializable
data class Offer(
    val retailer: String,
    val price: Double? = null,
    val currency: String? = null,
    val availability: String? = null,
    val condition: String? = null,
    @SerialName("shipping_cost")
    val shippingCost: Double? = null,
    val url: String? = null,
    @SerialName("last_updated")
    val lastUpdated: String? = null
)

/**
 * Historical price point
 */
@Serializable
data class PricePoint(
    val date: String,
    val price: Double? = null,
    val availability: String? = null
)

/**
 * Offer with price history
 */
@Serializable
data class OfferWithHistory(
    val retailer: String,
    val price: Double? = null,
    val currency: String? = null,
    val availability: String? = null,
    val condition: String? = null,
    @SerialName("shipping_cost")
    val shippingCost: Double? = null,
    val url: String? = null,
    @SerialName("last_updated")
    val lastUpdated: String? = null,
    @SerialName("price_history")
    val priceHistory: List<PricePoint>? = null
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
    val success: Boolean,
    val message: String? = null,
    val identifier: String? = null,
    val frequency: String? = null
)

/**
 * Scheduled product model
 */
@Serializable
data class ScheduledProduct(
    val identifier: String,
    val frequency: String,
    val retailer: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("last_updated")
    val lastUpdated: String? = null
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
    val success: Boolean,
    val message: String? = null,
    val identifier: String? = null
)

// MARK: - Usage Models

/**
 * API usage information model
 */
@Serializable
data class UsageInfo(
    @SerialName("credits_used")
    val creditsUsed: Int? = null,
    @SerialName("credits_remaining")
    val creditsRemaining: Int? = null,
    @SerialName("credits_limit")
    val creditsLimit: Int? = null,
    @SerialName("reset_date")
    val resetDate: String? = null,
    @SerialName("current_period_start")
    val currentPeriodStart: String? = null,
    @SerialName("current_period_end")
    val currentPeriodEnd: String? = null
)