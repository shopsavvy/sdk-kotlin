# ShopSavvy Data API - Kotlin SDK

[![Maven Central](https://img.shields.io/maven-central/v/com.shopsavvy/shopsavvy-sdk-kotlin.svg)](https://search.maven.org/artifact/com.shopsavvy/shopsavvy-sdk-kotlin)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.10+-blue.svg)](https://kotlinlang.org/)
[![Android API](https://img.shields.io/badge/API-21+-green.svg)](https://developer.android.com/about/versions/android-5.0)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Documentation](https://img.shields.io/badge/docs-shopsavvy.com-blue)](https://shopsavvy.com/data/documentation)

Official Kotlin SDK for the [ShopSavvy Data API](https://shopsavvy.com/data). Access comprehensive product data, real-time pricing, and historical price trends across **thousands of retailers** and **millions of products**. Built for **Android**, **JVM**, and **Kotlin Multiplatform** projects.

## ⚡ 30-Second Quick Start

```kotlin
// Add to build.gradle.kts:
// implementation("com.shopsavvy:shopsavvy-sdk-kotlin:1.0.0")

// Use in Android/JVM:
import com.shopsavvy.sdk.*
import kotlinx.coroutines.*

val client = ShopSavvyClient("ss_live_your_api_key_here")

lifecycleScope.launch {
    val product = client.getProductDetails("012345678901")
    val offers = client.getCurrentOffers("012345678901")
    val bestOffer = offers.data.minByOrNull { it.price ?: Double.MAX_VALUE }
    
    println("${product.data.name} - Best price: $${bestOffer?.price} at ${bestOffer?.retailer}")
}
```

## 🚀 Installation & Setup

### Requirements

- Kotlin 1.9.10+
- Android API 21+ (for Android projects)
- JVM 8+ (for JVM projects)

### Gradle (Kotlin DSL)

```kotlin
dependencies {
    implementation("com.shopsavvy:shopsavvy-sdk-kotlin:1.0.0")
    
    // For Android projects
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // For JVM projects  
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
}
```

### Gradle (Groovy)

```gradle
implementation 'com.shopsavvy:shopsavvy-sdk-kotlin:1.0.0'
implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3'
```

### Maven

```xml
<dependency>
    <groupId>com.shopsavvy</groupId>
    <artifactId>shopsavvy-sdk-kotlin</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Get Your API Key

1. **Sign up**: Visit [shopsavvy.com/data](https://shopsavvy.com/data)
2. **Choose plan**: Select based on your usage needs  
3. **Get API key**: Copy from your dashboard
4. **Test**: Run the 30-second example above

## 📖 Complete API Reference

### Client Configuration

```kotlin
import com.shopsavvy.sdk.*
import kotlinx.coroutines.*

// Basic configuration
val client = ShopSavvyClient("ss_live_your_api_key_here")

// Advanced configuration
val client = ShopSavvyClient(
    apiKey = "ss_live_your_api_key_here",
    baseUrl = "https://api.shopsavvy.com/v1",  // Custom base URL
    timeoutSeconds = 60,                       // Request timeout
    retryAttempts = 3,                         // Retry failed requests
    userAgent = "MyApp/1.0.0"                 // Custom user agent
)

// Environment variable configuration (Android)
val apiKey = BuildConfig.SHOPSAVVY_API_KEY ?: System.getenv("SHOPSAVVY_API_KEY")
val client = ShopSavvyClient(apiKey)
```

### Product Lookup

#### Single Product
```kotlin
// Look up by barcode, ASIN, URL, model number, or ShopSavvy ID
val product = client.getProductDetails("012345678901")
val amazonProduct = client.getProductDetails("B08N5WRWNW")
val urlProduct = client.getProductDetails("https://www.amazon.com/dp/B08N5WRWNW")
val modelProduct = client.getProductDetails("MQ023LL/A") // iPhone model number

println("📦 Product: ${product.data.name}")
println("🏷️ Brand: ${product.data.brand ?: "N/A"}")
println("📂 Category: ${product.data.category ?: "N/A"}")
println("🔢 Product ID: ${product.data.id}")

product.data.asin?.let { println("📦 ASIN: $it") }
product.data.modelNumber?.let { println("🔧 Model: $it") }
```

#### Bulk Product Lookup
```kotlin
// Process up to 100 products at once (Pro plan)
val identifiers = listOf(
    "012345678901", "B08N5WRWNW", "045496590048",
    "https://www.bestbuy.com/site/product/123456",
    "MQ023LL/A", "SM-S911U"  // iPhone and Samsung model numbers
)

val products = client.getProductDetailsBatch(identifiers)

products.data.forEachIndexed { index, product ->
    when (product) {
        null -> println("❌ Failed to find product: ${identifiers[index]}")
        else -> println("✓ Found: ${product.name} by ${product.brand ?: "Unknown"}")
    }
}
```

### Real-Time Pricing

#### Android Compose Price Comparison
```kotlin
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PriceComparisonScreen(viewModel: PriceComparisonViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        TopAppBar(title = { Text("Price Comparison") })
        
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            uiState.error != null -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.1f))
                ) {
                    Text(
                        text = "Error: ${uiState.error}",
                        modifier = Modifier.padding(16.dp),
                        color = Color.Red
                    )
                }
            }
            
            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.offers) { offer ->
                        OfferCard(
                            offer = offer,
                            isBestPrice = offer == uiState.bestOffer
                        )
                    }
                }
            }
        }
    }
    
    LaunchedEffect(Unit) {
        viewModel.loadOffers("012345678901")
    }
}

@Composable
fun OfferCard(offer: Offer, isBestPrice: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isBestPrice) Color.Green.copy(alpha = 0.1f) else Color.White
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = offer.retailer,
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = offer.availability ?: "Unknown availability",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$${String.format("%.2f", offer.price ?: 0.0)}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = if (isBestPrice) Color.Green else Color.Black
                )
                
                if (isBestPrice) {
                    Text(
                        text = "BEST PRICE",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Green
                    )
                }
            }
        }
    }
}

class PriceComparisonViewModel : ViewModel() {
    private val client = ShopSavvyClient("ss_live_your_api_key_here")
    
    private val _uiState = MutableStateFlow(PriceComparisonUiState())
    val uiState: StateFlow<PriceComparisonUiState> = _uiState.asStateFlow()
    
    fun loadOffers(identifier: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val response = client.getCurrentOffers(identifier)
                val sortedOffers = response.data.sortedBy { it.price ?: Double.MAX_VALUE }
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    offers = sortedOffers,
                    bestOffer = sortedOffers.firstOrNull()
                )
            } catch (e: ShopSavvyException) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        client.close()
    }
}

data class PriceComparisonUiState(
    val isLoading: Boolean = false,
    val offers: List<Offer> = emptyList(),
    val bestOffer: Offer? = null,
    val error: String? = null
)
```

#### Advanced Price Analysis
```kotlin
suspend fun analyzeOffers(identifier: String) {
    try {
        val response = client.getCurrentOffers(identifier)
        val offers = response.data
        
        println("Found ${offers.size} offers across retailers")
        
        // Sort by price
        val sortedOffers = offers.sortedBy { it.price ?: Double.MAX_VALUE }
        
        val cheapest = sortedOffers.firstOrNull()
        val mostExpensive = sortedOffers.lastOrNull()
        
        val validPrices = offers.mapNotNull { it.price }
        val average = validPrices.average()
        
        cheapest?.let { 
            println("💰 Best price: ${it.retailer} - $${String.format("%.2f", it.price ?: 0.0)}")
        }
        mostExpensive?.let {
            println("💸 Highest price: ${it.retailer} - $${String.format("%.2f", it.price ?: 0.0)}")
        }
        println("📊 Average price: $${String.format("%.2f", average)}")
        
        val savings = (mostExpensive?.price ?: 0.0) - (cheapest?.price ?: 0.0)
        println("💡 Potential savings: $${String.format("%.2f", savings)}")
        
        // Filter by availability and condition
        val inStockOffers = offers.filter { it.availability == "in_stock" }
        val newConditionOffers = offers.filter { it.condition == "new" }
        
        println("✅ In-stock offers: ${inStockOffers.size}")
        println("🆕 New condition: ${newConditionOffers.size}")
        
    } catch (e: ShopSavvyException) {
        println("Error analyzing offers: ${e.message}")
    }
}
```

## 🚀 Production Deployment

### Android App with MVVM + Repository Pattern

```kotlin
// Repository
class ProductRepository @Inject constructor(
    private val client: ShopSavvyClient,
    private val dao: ProductDao
) {
    suspend fun searchProduct(identifier: String): Result<ProductDetails> {
        return try {
            val response = client.getProductDetails(identifier)
            
            // Cache in local database
            dao.insertProduct(response.data.toEntity())
            
            Result.success(response.data)
        } catch (e: ShopSavvyException) {
            // Try local cache if API fails
            dao.getProduct(identifier)?.let { cached ->
                Result.success(cached.toDomain())
            } ?: Result.failure(e)
        }
    }
    
    suspend fun getPriceAlerts(identifier: String): Flow<List<PriceAlert>> {
        return dao.getPriceAlertsFlow(identifier).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    suspend fun monitorPriceChanges(identifier: String, targetPrice: Double) {
        // Schedule monitoring with API
        client.scheduleProductMonitoring(identifier, "daily")
        
        // Store local alert
        dao.insertPriceAlert(
            PriceAlertEntity(
                productId = identifier,
                targetPrice = targetPrice,
                isActive = true,
                createdAt = System.currentTimeMillis()
            )
        )
    }
}

// ViewModel
@HiltViewModel
class ProductViewModel @Inject constructor(
    private val repository: ProductRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ProductUiState())
    val uiState: StateFlow<ProductUiState> = _uiState.asStateFlow()
    
    fun searchProduct(identifier: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            repository.searchProduct(identifier)
                .onSuccess { product ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        product = product
                    )
                    loadOffers(identifier)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
        }
    }
    
    private suspend fun loadOffers(identifier: String) {
        try {
            val response = repository.client.getCurrentOffers(identifier)
            val sortedOffers = response.data.sortedBy { it.price ?: Double.MAX_VALUE }
            
            _uiState.value = _uiState.value.copy(
                offers = sortedOffers,
                bestOffer = sortedOffers.firstOrNull()
            )
        } catch (e: ShopSavvyException) {
            // Handle offer loading errors separately
            _uiState.value = _uiState.value.copy(
                offersError = e.message
            )
        }
    }
    
    fun setPriceAlert(targetPrice: Double) {
        viewModelScope.launch {
            _uiState.value.product?.let { product ->
                try {
                    repository.monitorPriceChanges(product.id, targetPrice)
                    _uiState.value = _uiState.value.copy(
                        priceAlertSet = true
                    )
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to set price alert: ${e.message}"
                    )
                }
            }
        }
    }
}

data class ProductUiState(
    val isLoading: Boolean = false,
    val product: ProductDetails? = null,
    val offers: List<Offer> = emptyList(),
    val bestOffer: Offer? = null,
    val error: String? = null,
    val offersError: String? = null,
    val priceAlertSet: Boolean = false
)
```

### Background Price Monitoring with WorkManager

```kotlin
@HiltWorker
class PriceMonitorWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val client: ShopSavvyClient,
    private val dao: ProductDao,
    private val notificationManager: NotificationManager
) : CoroutineWorker(context, workerParams) {
    
    override suspend fun doWork(): Result {
        return try {
            val activeAlerts = dao.getActivePriceAlerts()
            
            for (alert in activeAlerts) {
                checkPriceAlert(alert)
                delay(1000) // Rate limiting
            }
            
            Result.success()
        } catch (e: Exception) {
            Log.e("PriceMonitorWorker", "Failed to monitor prices", e)
            Result.retry()
        }
    }
    
    private suspend fun checkPriceAlert(alert: PriceAlertEntity) {
        try {
            val offers = client.getCurrentOffers(alert.productId)
            val bestOffer = offers.data.minByOrNull { it.price ?: Double.MAX_VALUE }
            
            bestOffer?.price?.let { currentPrice ->
                if (currentPrice <= alert.targetPrice) {
                    // Send notification
                    sendPriceAlertNotification(alert, currentPrice, bestOffer.retailer)
                    
                    // Update alert status
                    dao.updatePriceAlert(alert.copy(lastTriggered = System.currentTimeMillis()))
                }
                
                // Update price history
                dao.insertPriceHistory(
                    PriceHistoryEntity(
                        productId = alert.productId,
                        retailer = bestOffer.retailer,
                        price = currentPrice,
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
        } catch (e: ShopSavvyException) {
            Log.w("PriceMonitorWorker", "Failed to check price for ${alert.productId}", e)
        }
    }
    
    private fun sendPriceAlertNotification(
        alert: PriceAlertEntity,
        currentPrice: Double,
        retailer: String
    ) {
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_price_alert)
            .setContentTitle("Price Alert!")
            .setContentText("Target price reached: $$currentPrice at $retailer")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(alert.productId.hashCode(), notification)
    }
    
    @AssistedFactory
    interface Factory {
        fun create(context: Context, params: WorkerParameters): PriceMonitorWorker
    }
    
    companion object {
        private const val CHANNEL_ID = "price_alerts"
        
        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<PriceMonitorWorker>(
                repeatInterval = 1,
                repeatIntervalTimeUnit = TimeUnit.HOURS
            )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
            
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    "price_monitoring",
                    ExistingPeriodicWorkPolicy.KEEP,
                    request
                )
        }
    }
}
```

### Kotlin Multiplatform Integration

```kotlin
// shared/src/commonMain/kotlin
expect class PlatformShopSavvyClient

class SharedProductService {
    private val client = PlatformShopSavvyClient()
    
    suspend fun searchProducts(query: String): List<ProductDetails> {
        return client.searchProducts(query)
    }
    
    suspend fun comparePrice(identifier: String): PriceComparison {
        val offers = client.getCurrentOffers(identifier)
        return PriceComparison(
            bestPrice = offers.data.minByOrNull { it.price ?: Double.MAX_VALUE },
            averagePrice = offers.data.mapNotNull { it.price }.average(),
            totalOffers = offers.data.size
        )
    }
}

// androidMain/src/main/kotlin
actual class PlatformShopSavvyClient {
    private val client = ShopSavvyClient("ss_live_your_api_key_here")
    
    actual suspend fun searchProducts(query: String): List<ProductDetails> {
        return client.getProductDetailsBatch(listOf(query)).data.filterNotNull()
    }
    
    actual suspend fun getCurrentOffers(identifier: String): ApiResponse<List<Offer>> {
        return client.getCurrentOffers(identifier)
    }
}

// iosMain/src/main/kotlin  
actual class PlatformShopSavvyClient {
    private val client = ShopSavvyClient("ss_live_your_api_key_here")
    
    actual suspend fun searchProducts(query: String): List<ProductDetails> {
        return client.getProductDetailsBatch(listOf(query)).data.filterNotNull()
    }
    
    actual suspend fun getCurrentOffers(identifier: String): ApiResponse<List<Offer>> {
        return client.getCurrentOffers(identifier)
    }
}
```

## Exception Handling

The SDK provides comprehensive exception handling with Kotlin-specific patterns:

```kotlin
suspend fun handleProductLookup(identifier: String) {
    runCatching {
        client.getProductDetails(identifier)
    }.fold(
        onSuccess = { response ->
            println("✅ Found product: ${response.data.name}")
        },
        onFailure = { exception ->
            when (exception) {
                is ShopSavvyAuthenticationException -> {
                    println("🔐 Authentication failed: ${exception.message}")
                    // Redirect to login or refresh token
                }
                is ShopSavvyNotFoundException -> {
                    println("❌ Product not found: ${exception.message}")
                    // Show "not found" UI
                }
                is ShopSavvyValidationException -> {
                    println("⚠️ Invalid parameters: ${exception.message}")
                    // Show validation error to user
                }
                is ShopSavvyRateLimitException -> {
                    println("🚦 Rate limit exceeded: ${exception.message}")
                    // Implement exponential backoff
                    delay(exception.retryAfterSeconds * 1000L)
                }
                is ShopSavvyNetworkException -> {
                    println("🌐 Network error: ${exception.message}")
                    // Show offline mode or retry option
                }
                is ShopSavvyException -> {
                    println("❌ API error: ${exception.message}")
                    // Generic error handling
                }
                else -> {
                    println("💥 Unexpected error: ${exception.message}")
                    // Log to crash reporting service
                }
            }
        }
    )
}
```

### Retry Logic with Exponential Backoff

```kotlin
suspend fun <T> retryWithBackoff(
    maxAttempts: Int = 3,
    initialDelay: Long = 1000,
    maxDelay: Long = 30000,
    factor: Double = 2.0,
    block: suspend () -> T
): T {
    var currentDelay = initialDelay
    repeat(maxAttempts - 1) { attempt ->
        try {
            return block()
        } catch (e: ShopSavvyRateLimitException) {
            // Use server-specified retry delay if available
            val retryDelay = e.retryAfterSeconds?.let { it * 1000L } ?: currentDelay
            delay(retryDelay)
            currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
        } catch (e: ShopSavvyNetworkException) {
            delay(currentDelay)
            currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
        }
    }
    return block() // Last attempt
}

// Usage
val product = retryWithBackoff {
    client.getProductDetails("012345678901")
}
```

## 🛠️ Development & Testing

### Local Development Setup

```bash
# Clone the repository
git clone https://github.com/shopsavvy/sdk-kotlin.git
cd sdk-kotlin

# Build the project
./gradlew build

# Run tests
./gradlew test

# Run Android tests
./gradlew connectedAndroidTest

# Publish to local repository
./gradlew publishToMavenLocal
```

### Testing Your Integration

```kotlin
class SDKTester {
    private val client = ShopSavvyClient("ss_test_your_test_key_here")
    
    suspend fun runTests() {
        runCatching {
            // Test product lookup
            val product = client.getProductDetails("012345678901")
            println("✅ Product lookup: ${product.data.name}")
            
            // Test current offers
            val offers = client.getCurrentOffers("012345678901")
            println("✅ Current offers: ${offers.data.size} found")
            
            // Test usage info
            val usage = client.getUsage()
            println("✅ API usage: ${usage.data.creditsRemaining ?: 0} credits remaining")
            
            println("\n🎉 All tests passed! SDK is working correctly.")
        }.onFailure { error ->
            println("❌ Test failed: ${error.message}")
        }
    }
}
```

## Data Models

All models are implemented as Kotlin data classes with kotlinx.serialization and null-safety:

### ProductDetails
```kotlin
@Serializable
data class ProductDetails(
    val id: String,
    val name: String,
    val description: String? = null,
    val brand: String? = null,
    val category: String? = null,
    val upc: String? = null,
    val asin: String? = null,
    val modelNumber: String? = null,
    val images: List<String> = emptyList(),
    val specifications: Map<String, String> = emptyMap(),
    val createdAt: String? = null,
    val updatedAt: String? = null
) {
    // Computed properties for convenience
    val hasImages: Boolean get() = images.isNotEmpty()
    val hasSpecifications: Boolean get() = specifications.isNotEmpty()
    val displayName: String get() = "$brand $name".trim()
}
```

### Offer
```kotlin
@Serializable
data class Offer(
    val retailer: String,
    val price: Double? = null,
    val currency: String? = "USD",
    val availability: String? = null,
    val condition: String? = null,
    val shippingCost: Double? = null,
    val url: String? = null,
    val lastUpdated: String? = null
) {
    // Computed properties
    val isInStock: Boolean get() = availability == "in_stock"
    val isNew: Boolean get() = condition == "new"
    val totalCost: Double get() = (price ?: 0.0) + (shippingCost ?: 0.0)
    val formattedPrice: String get() = "$${String.format("%.2f", price ?: 0.0)}"
}
```

### UsageInfo
```kotlin
@Serializable
data class UsageInfo(
    val creditsUsed: Int? = null,
    val creditsRemaining: Int? = null,
    val creditsLimit: Int? = null,
    val resetDate: String? = null,
    val currentPeriodStart: String? = null,
    val currentPeriodEnd: String? = null
) {
    // Computed properties
    val usagePercentage: Double get() {
        val used = creditsUsed ?: 0
        val limit = creditsLimit ?: 1
        return (used.toDouble() / limit.toDouble()) * 100
    }
    
    val isNearLimit: Boolean get() = usagePercentage > 80.0
}
```

## 📚 Additional Resources

- **[ShopSavvy Data API Documentation](https://shopsavvy.com/data/documentation)** - Complete API reference
- **[API Dashboard](https://shopsavvy.com/data/dashboard)** - Manage your API keys and usage
- **[GitHub Repository](https://github.com/shopsavvy/sdk-kotlin)** - Source code and issues
- **[Maven Central](https://search.maven.org/artifact/com.shopsavvy/shopsavvy-sdk-kotlin)** - Package releases
- **[Kotlin Documentation](https://kotlinlang.org/docs/)** - Kotlin language reference
- **[Android Developers](https://developer.android.com/)** - Android development guides
- **[Support](mailto:business@shopsavvy.com)** - Get help from our team

## 🤝 Contributing

We welcome contributions! Please see our [Contributing Guide](CONTRIBUTING.md) for details on:

- Reporting bugs and feature requests
- Setting up development environment  
- Submitting pull requests
- Code standards and testing
- Kotlin and Android best practices

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🏢 About ShopSavvy

**ShopSavvy** is the world's first mobile shopping app, helping consumers find the best deals since 2008. With over **40 million downloads** and millions of active users, ShopSavvy has saved consumers billions of dollars.

### Our Data API Powers:
- 🛒 **E-commerce platforms** with competitive intelligence  
- 📊 **Market research** with real-time pricing data
- 🏪 **Retailers** with inventory and pricing optimization
- 📱 **Mobile apps** with product lookup and price comparison
- 🤖 **Business intelligence** with automated price monitoring

### Why Choose ShopSavvy Data API?
- ✅ **Trusted by millions** - Proven at scale since 2008
- ✅ **Comprehensive coverage** - 1000+ retailers, millions of products  
- ✅ **Real-time accuracy** - Fresh data updated continuously
- ✅ **Developer-friendly** - Easy integration, great documentation
- ✅ **Reliable infrastructure** - 99.9% uptime, enterprise-grade
- ✅ **Flexible pricing** - Plans for every use case and budget

### Perfect for Kotlin & Android:
- 🚀 **Coroutines-first** - Built for modern async programming
- 📱 **Android optimized** - MVVM, Compose, WorkManager examples
- 🔄 **Multiplatform ready** - Share code across Android, iOS, JVM
- 🛡️ **Type-safe** - Leverages Kotlin's null safety and sealed classes
- ⚡ **Performance focused** - Minimal overhead, efficient serialization

---

**Ready to get started?** [Sign up for your API key](https://shopsavvy.com/data) • **Need help?** [Contact us](mailto:business@shopsavvy.com)