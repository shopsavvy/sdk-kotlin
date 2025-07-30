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
 *     println("Product: ${product.data.name}")
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
                    .header("User-Agent", "ShopSavvy-Kotlin-SDK/1.0.0")
                    .build()
                chain.proceed(newRequest)
            }
            .build()
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
    suspend fun getProductDetails(identifier: String, format: String? = null): ApiResponse<ProductDetails> {
        val url = HttpUrl.Builder()
            .scheme("https")
            .host(baseUrl.removePrefix("https://").removePrefix("http://"))
            .addPathSegments("products/details")
            .addQueryParameter("identifier", identifier)
            .apply { format?.let { addQueryParameter("format", it) } }
            .build()
        
        return executeRequest(url.toString())
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
        val url = HttpUrl.Builder()
            .scheme("https")
            .host(baseUrl.removePrefix("https://").removePrefix("http://"))
            .addPathSegments("products/details")
            .addQueryParameter("identifiers", identifiers.joinToString(","))
            .apply { format?.let { addQueryParameter("format", it) } }
            .build()
        
        return executeRequest(url.toString())
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
    suspend fun getCurrentOffers(identifier: String, retailer: String? = null, format: String? = null): ApiResponse<List<Offer>> {
        val url = HttpUrl.Builder()
            .scheme("https")
            .host(baseUrl.removePrefix("https://").removePrefix("http://"))
            .addPathSegments("products/offers")
            .addQueryParameter("identifier", identifier)
            .apply { 
                retailer?.let { addQueryParameter("retailer", it) }
                format?.let { addQueryParameter("format", it) }
            }
            .build()
        
        return executeRequest(url.toString())
    }
    
    /**
     * Get current offers for multiple products
     * 
     * @param identifiers List of product identifiers
     * @param retailer Optional retailer to filter by
     * @param format Response format ('json' or 'csv')
     * @return Map of identifiers to their offers
     * @throws ShopSavvyException if the API request fails
     */
    suspend fun getCurrentOffersBatch(identifiers: List<String>, retailer: String? = null, format: String? = null): ApiResponse<Map<String, List<Offer>>> {
        val url = HttpUrl.Builder()
            .scheme("https")
            .host(baseUrl.removePrefix("https://").removePrefix("http://"))
            .addPathSegments("products/offers")
            .addQueryParameter("identifiers", identifiers.joinToString(","))
            .apply { 
                retailer?.let { addQueryParameter("retailer", it) }
                format?.let { addQueryParameter("format", it) }
            }
            .build()
        
        return executeRequest(url.toString())
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
        val url = HttpUrl.Builder()
            .scheme("https")
            .host(baseUrl.removePrefix("https://").removePrefix("http://"))
            .addPathSegments("products/history")
            .addQueryParameter("identifier", identifier)
            .addQueryParameter("start_date", startDate)
            .addQueryParameter("end_date", endDate)
            .apply { 
                retailer?.let { addQueryParameter("retailer", it) }
                format?.let { addQueryParameter("format", it) }
            }
            .build()
        
        return executeRequest(url.toString())
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