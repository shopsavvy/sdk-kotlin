package com.shopsavvy.sdk

import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

/**
 * SDK version
 */
const val VERSION = "1.1.1"

/**
 * Official Kotlin client for ShopSavvy Data API
 *
 * Provides access to product data, pricing information, and price history
 * across thousands of retailers and millions of products.
 *
 * Example usage:
 * ```kotlin
 * val client = ShopSavvyClient("ss_live_your_api_key_here")
 *
 * runBlocking {
 *     val product = client.getProductDetails("012345678901")
 *     println("Product: ${product.data[0].title}")
 * }
 *
 * client.close()
 * ```
 */
class ShopSavvyClient @JvmOverloads constructor(
    private val apiKey: String,
    private val baseUrl: String = DEFAULT_BASE_URL,
    timeoutSeconds: Long = 30
) : AutoCloseable {

    companion object {
        private const val DEFAULT_BASE_URL = "https://api.shopsavvy.com/v1"
        private val API_KEY_PATTERN = Pattern.compile("^ss_(live|test)_[a-zA-Z0-9]+$")
    }

    private val httpClient: OkHttpClient
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    init {
        require(apiKey.isNotBlank()) { "API key is required. Get one at https://shopsavvy.com/data" }
        require(API_KEY_PATTERN.matcher(apiKey).matches()) {
            "Invalid API key format. API keys should start with ss_live_ or ss_test_"
        }

        httpClient = OkHttpClient.Builder()
            .connectTimeout(timeoutSeconds, TimeUnit.SECONDS)
            .readTimeout(timeoutSeconds, TimeUnit.SECONDS)
            .writeTimeout(timeoutSeconds, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val originalRequest = chain.request()
                val newRequest = originalRequest.newBuilder()
                    .header("Authorization", "Bearer $apiKey")
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "ShopSavvy-Kotlin-SDK/$VERSION")
                    .build()
                chain.proceed(newRequest)
            }
            .build()
    }

    // MARK: - Search

    /**
     * Search for products by keyword
     *
     * @param query Search query or keyword
     * @param limit Optional maximum number of results
     * @param offset Optional pagination offset
     * @return Search results with pagination
     * @throws ShopSavvyException if the API request fails
     */
    suspend fun searchProducts(query: String, limit: Int? = null, offset: Int? = null): ProductSearchResult {
        val urlBuilder = StringBuilder("$baseUrl/products/search?q=${java.net.URLEncoder.encode(query, "UTF-8")}")
        limit?.let { urlBuilder.append("&limit=$it") }
        offset?.let { urlBuilder.append("&offset=$it") }

        return executeRequest(urlBuilder.toString())
    }

    // MARK: - Product Details

    /**
     * Look up product details by identifier
     *
     * @param identifier Product identifier (barcode, ASIN, URL, model number, or ShopSavvy product ID)
     * @param format Response format ('json' or 'csv')
     * @return Product details
     * @throws ShopSavvyException if the API request fails
     */
    suspend fun getProductDetails(identifier: String, format: String? = null): ApiResponse<List<ProductDetails>> {
        val urlBuilder = StringBuilder("$baseUrl/products?ids=${java.net.URLEncoder.encode(identifier, "UTF-8")}")
        format?.let { urlBuilder.append("&format=$it") }

        return executeRequest(urlBuilder.toString())
    }

    /**
     * Look up details for multiple products
     *
     * @param identifiers List of product identifiers
     * @param format Response format ('json' or 'csv')
     * @return List of product details
     * @throws ShopSavvyException if the API request fails
     */
    suspend fun getProductDetailsBatch(identifiers: List<String>, format: String? = null): ApiResponse<List<ProductDetails>> {
        val ids = identifiers.joinToString(",") { java.net.URLEncoder.encode(it, "UTF-8") }
        val urlBuilder = StringBuilder("$baseUrl/products?ids=$ids")
        format?.let { urlBuilder.append("&format=$it") }

        return executeRequest(urlBuilder.toString())
    }

    // MARK: - Current Offers

    /**
     * Get current offers for a product
     *
     * @param identifier Product identifier
     * @param retailer Optional retailer to filter by
     * @param format Response format ('json' or 'csv')
     * @return Current offers
     * @throws ShopSavvyException if the API request fails
     */
    suspend fun getCurrentOffers(identifier: String, retailer: String? = null, format: String? = null): ApiResponse<List<ProductWithOffers>> {
        val urlBuilder = StringBuilder("$baseUrl/products/offers?ids=${java.net.URLEncoder.encode(identifier, "UTF-8")}")
        retailer?.let { urlBuilder.append("&retailer=${java.net.URLEncoder.encode(it, "UTF-8")}") }
        format?.let { urlBuilder.append("&format=$it") }

        return executeRequest(urlBuilder.toString())
    }

    /**
     * Get current offers for multiple products
     *
     * @param identifiers List of product identifiers
     * @param retailer Optional retailer to filter by
     * @param format Response format ('json' or 'csv')
     * @return Products with their offers
     * @throws ShopSavvyException if the API request fails
     */
    suspend fun getCurrentOffersBatch(identifiers: List<String>, retailer: String? = null, format: String? = null): ApiResponse<List<ProductWithOffers>> {
        val ids = identifiers.joinToString(",") { java.net.URLEncoder.encode(it, "UTF-8") }
        val urlBuilder = StringBuilder("$baseUrl/products/offers?ids=$ids")
        retailer?.let { urlBuilder.append("&retailer=${java.net.URLEncoder.encode(it, "UTF-8")}") }
        format?.let { urlBuilder.append("&format=$it") }

        return executeRequest(urlBuilder.toString())
    }

    // MARK: - Price History

    /**
     * Get price history for a product
     *
     * @param identifier Product identifier
     * @param startDate Start date (YYYY-MM-DD format)
     * @param endDate End date (YYYY-MM-DD format)
     * @param retailer Optional retailer to filter by
     * @param format Response format ('json' or 'csv')
     * @return Offers with price history
     * @throws ShopSavvyException if the API request fails
     */
    suspend fun getPriceHistory(
        identifier: String,
        startDate: String,
        endDate: String,
        retailer: String? = null,
        format: String? = null
    ): ApiResponse<List<OfferWithHistory>> {
        val urlBuilder = StringBuilder("$baseUrl/products/offers/history?ids=${java.net.URLEncoder.encode(identifier, "UTF-8")}")
        urlBuilder.append("&start_date=$startDate")
        urlBuilder.append("&end_date=$endDate")
        retailer?.let { urlBuilder.append("&retailer=${java.net.URLEncoder.encode(it, "UTF-8")}") }
        format?.let { urlBuilder.append("&format=$it") }

        return executeRequest(urlBuilder.toString())
    }

    // MARK: - Monitoring

    /**
     * Schedule product monitoring
     *
     * @param identifier Product identifier
     * @param frequency How often to refresh ('hourly', 'daily', 'weekly')
     * @param retailer Optional retailer to monitor
     * @return Scheduling confirmation
     * @throws ShopSavvyException if the API request fails
     */
    suspend fun scheduleProductMonitoring(identifier: String, frequency: String, retailer: String? = null): ApiResponse<ScheduleResponse> {
        val url = "$baseUrl/products/schedule"
        val request = ScheduleRequest(identifier, frequency, retailer)

        return executeRequest(url, "POST", request)
    }

    /**
     * Get all scheduled products
     *
     * @return List of scheduled products
     * @throws ShopSavvyException if the API request fails
     */
    suspend fun getScheduledProducts(): ApiResponse<List<ScheduledProduct>> {
        val url = "$baseUrl/products/scheduled"
        return executeRequest(url)
    }

    /**
     * Remove product from monitoring schedule
     *
     * @param identifier Product identifier to remove
     * @return Removal confirmation
     * @throws ShopSavvyException if the API request fails
     */
    suspend fun removeProductFromSchedule(identifier: String): ApiResponse<RemoveResponse> {
        val url = "$baseUrl/products/schedule"
        val request = RemoveRequest(identifier)

        return executeRequest(url, "DELETE", request)
    }

    // MARK: - Usage

    /**
     * Get API usage information
     *
     * @return Current usage and credit information
     * @throws ShopSavvyException if the API request fails
     */
    suspend fun getUsage(): ApiResponse<UsageInfo> {
        val url = "$baseUrl/usage"
        return executeRequest(url)
    }

    /**
     * Browse current shopping deals
     */
    suspend fun getDeals(
        sort: String = "hot",
        limit: Int = 25,
        offset: Int = 0,
        category: String? = null,
        retailer: String? = null,
        grade: String? = null
    ): DealsResponse {
        val params = mutableListOf("sort=$sort", "limit=$limit", "offset=$offset")
        category?.let { params.add("category=$it") }
        retailer?.let { params.add("retailer=$it") }
        grade?.let { params.add("grade=$it") }
        val url = "$baseUrl/deals?${params.joinToString("&")}"
        return executeRequest(url)
    }

    /**
     * Look up multiple products at once (sync for <=20, async for >20)
     */
    suspend fun batchLookup(identifiers: List<String>, include: List<String>? = null): Map<String, Any> {
        val body = mutableMapOf<String, Any>("identifiers" to identifiers)
        include?.let { body["include"] = it }
        val json = kotlinx.serialization.json.Json.encodeToString(kotlinx.serialization.serializer<Map<String, kotlinx.serialization.json.JsonElement>>(), emptyMap())
        // Use raw OkHttp for POST with JSON body
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val jsonBody = org.json.JSONObject(body as Map<*, *>).toString()
        val request = Request.Builder()
            .url("$baseUrl/products/batch")
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("User-Agent", "ShopSavvy-Kotlin-SDK/$VERSION")
            .post(jsonBody.toRequestBody(mediaType))
            .build()
        return withContext(Dispatchers.IO) {
            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: "{}"
            @Suppress("UNCHECKED_CAST")
            org.json.JSONObject(responseBody).toMap() as Map<String, Any>
        }
    }

    /**
     * Poll for async batch job results
     */
    suspend fun getBatchStatus(batchId: String): Map<String, Any> {
        val request = Request.Builder()
            .url("$baseUrl/batch/$batchId")
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("User-Agent", "ShopSavvy-Kotlin-SDK/$VERSION")
            .get()
            .build()
        return withContext(Dispatchers.IO) {
            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: "{}"
            @Suppress("UNCHECKED_CAST")
            org.json.JSONObject(responseBody).toMap() as Map<String, Any>
        }
    }

    suspend fun createWebhook(url: String, events: List<String>): Map<String, Any> {
        val body = org.json.JSONObject(mapOf("url" to url, "events" to events) as Map<*, *>).toString()
        val request = Request.Builder().url("$baseUrl/webhooks")
            .addHeader("Authorization", "Bearer $apiKey").addHeader("User-Agent", "ShopSavvy-Kotlin-SDK/$VERSION")
            .post(body.toRequestBody("application/json; charset=utf-8".toMediaType())).build()
        return withContext(Dispatchers.IO) {
            @Suppress("UNCHECKED_CAST")
            org.json.JSONObject(httpClient.newCall(request).execute().body?.string() ?: "{}").toMap() as Map<String, Any>
        }
    }

    suspend fun listWebhooks(): Map<String, Any> {
        val request = Request.Builder().url("$baseUrl/webhooks")
            .addHeader("Authorization", "Bearer $apiKey").addHeader("User-Agent", "ShopSavvy-Kotlin-SDK/$VERSION").get().build()
        return withContext(Dispatchers.IO) {
            @Suppress("UNCHECKED_CAST")
            org.json.JSONObject(httpClient.newCall(request).execute().body?.string() ?: "{}").toMap() as Map<String, Any>
        }
    }

    suspend fun testWebhook(webhookId: String): Map<String, Any> {
        val request = Request.Builder().url("$baseUrl/webhooks/$webhookId/test")
            .addHeader("Authorization", "Bearer $apiKey").addHeader("User-Agent", "ShopSavvy-Kotlin-SDK/$VERSION")
            .post("".toRequestBody(null)).build()
        return withContext(Dispatchers.IO) {
            @Suppress("UNCHECKED_CAST")
            org.json.JSONObject(httpClient.newCall(request).execute().body?.string() ?: "{}").toMap() as Map<String, Any>
        }
    }

    /**
     * Update a webhook. All parameters are optional, but at least one of
     * [url], [events], or [isActive] must be non-null.
     */
    suspend fun updateWebhook(webhookId: String, url: String? = null, events: List<String>? = null, isActive: Boolean? = null): Map<String, Any> {
        require(url != null || events != null || isActive != null) {
            "updateWebhook requires at least one of url, events, or isActive"
        }
        val payload = mutableMapOf<String, Any>()
        if (url != null) payload["url"] = url
        if (events != null) payload["events"] = events
        if (isActive != null) payload["is_active"] = isActive
        val body = org.json.JSONObject(payload as Map<*, *>).toString()
        val request = Request.Builder().url("$baseUrl/webhooks/$webhookId")
            .addHeader("Authorization", "Bearer $apiKey").addHeader("User-Agent", "ShopSavvy-Kotlin-SDK/$VERSION")
            .put(body.toRequestBody("application/json; charset=utf-8".toMediaType())).build()
        return withContext(Dispatchers.IO) {
            @Suppress("UNCHECKED_CAST")
            org.json.JSONObject(httpClient.newCall(request).execute().body?.string() ?: "{}").toMap() as Map<String, Any>
        }
    }

    suspend fun deleteWebhook(webhookId: String): Map<String, Any> {
        val request = Request.Builder().url("$baseUrl/webhooks/$webhookId")
            .addHeader("Authorization", "Bearer $apiKey").addHeader("User-Agent", "ShopSavvy-Kotlin-SDK/$VERSION").delete().build()
        return withContext(Dispatchers.IO) {
            @Suppress("UNCHECKED_CAST")
            org.json.JSONObject(httpClient.newCall(request).execute().body?.string() ?: "{}").toMap() as Map<String, Any>
        }
    }

    /**
     * Get TLDR review for a product
     */
    suspend fun getProductReview(identifier: String): ReviewResponse {
        val url = "$baseUrl/products/reviews?id=${java.net.URLEncoder.encode(identifier, "UTF-8")}"
        return executeRequest(url)
    }

    // MARK: - Private Methods

    private suspend inline fun <reified T> executeRequest(
        url: String,
        method: String = "GET",
        body: Any? = null
    ): T = withContext(Dispatchers.IO) {
        val requestBuilder = Request.Builder().url(url)

        when (method) {
            "POST" -> {
                val requestBody = body?.let {
                    json.encodeToString(kotlinx.serialization.serializer(), it)
                        .toRequestBody("application/json".toMediaType())
                } ?: "".toRequestBody("application/json".toMediaType())
                requestBuilder.post(requestBody)
            }
            "DELETE" -> {
                val requestBody = body?.let {
                    json.encodeToString(kotlinx.serialization.serializer(), it)
                        .toRequestBody("application/json".toMediaType())
                } ?: "".toRequestBody("application/json".toMediaType())
                requestBuilder.delete(requestBody)
            }
            else -> requestBuilder.get()
        }

        val request = requestBuilder.build()

        try {
            httpClient.newCall(request).execute().use { response ->
                val responseBody = response.body?.string() ?: ""

                if (!response.isSuccessful) {
                    throw createExceptionFromResponse(response.code, responseBody)
                }

                json.decodeFromString<T>(responseBody)
            }
        } catch (e: IOException) {
            throw ShopSavvyNetworkException("Network error: ${e.message}", e)
        }
    }

    private fun createExceptionFromResponse(statusCode: Int, responseBody: String): ShopSavvyException {
        val errorMessage = try {
            val errorResponse = json.decodeFromString<Map<String, String>>(responseBody)
            errorResponse["error"] ?: "Unknown error"
        } catch (e: Exception) {
            responseBody.ifEmpty { "Unknown error" }
        }

        return when (statusCode) {
            401 -> ShopSavvyAuthenticationException("Authentication failed. Check your API key.")
            404 -> ShopSavvyNotFoundException("Resource not found")
            422 -> ShopSavvyValidationException("Request validation failed. Check your parameters.")
            429 -> ShopSavvyRateLimitException("Rate limit exceeded. Please slow down your requests.")
            else -> ShopSavvyException("HTTP $statusCode: $errorMessage")
        }
    }

    override fun close() {
        httpClient.dispatcher.executorService.shutdown()
        httpClient.connectionPool.evictAll()
    }
}
