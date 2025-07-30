# ShopSavvy Kotlin SDK

Official Kotlin SDK for ShopSavvy Data API - Access product data, pricing, and price history across thousands of retailers and millions of products.

## Installation

### Gradle (Kotlin DSL)

Add this dependency to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.shopsavvy:shopsavvy-sdk-kotlin:1.0.0")
}
```

### Gradle (Groovy)

Add this dependency to your `build.gradle`:

```gradle
implementation 'com.shopsavvy:shopsavvy-sdk-kotlin:1.0.0'
```

### Maven

Add this dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.shopsavvy</groupId>
    <artifactId>shopsavvy-sdk-kotlin</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Usage

First, get your API key from [ShopSavvy Data API](https://shopsavvy.com/data).

### Kotlin Coroutines (Recommended)

```kotlin
import com.shopsavvy.sdk.*
import kotlinx.coroutines.*

// Initialize the client
val client = ShopSavvyClient("ss_live_your_api_key_here")

// Example usage with coroutines
runBlocking {
    try {
        // Get product details
        val productResponse = client.getProductDetails("012345678901")
        println("Product: ${productResponse.data.name}")
        
        // Get current offers
        val offersResponse = client.getCurrentOffers("012345678901")
        offersResponse.data.forEach { offer ->
            println("${offer.retailer}: $${offer.price}")
        }
        
        // Get price history
        val historyResponse = client.getPriceHistory(
            identifier = "012345678901",
            startDate = "2024-01-01",
            endDate = "2024-12-31"
        )
        
        // Schedule monitoring
        val scheduleResponse = client.scheduleProductMonitoring(
            identifier = "012345678901",
            frequency = "daily"
        )
        
        // Check API usage
        val usageResponse = client.getUsage()
        println("Credits remaining: ${usageResponse.data.creditsRemaining}")
        
    } catch (e: ShopSavvyException) {
        println("API Error: ${e.message}")
    } finally {
        // Always close the client when done
        client.close()
    }
}
```

### Java Interoperability

The Kotlin SDK is fully compatible with Java:

```java
import com.shopsavvy.sdk.*;
import kotlinx.coroutines.future.FutureKt;

// Initialize the client
ShopSavvyClient client = new ShopSavvyClient("ss_live_your_api_key_here");

// Use with CompletableFuture
CompletableFuture<ApiResponse<ProductDetails>> future = 
    FutureKt.future(GlobalScope.INSTANCE, EmptyCoroutineContext.INSTANCE, CoroutineStart.DEFAULT,
        (scope, continuation) -> client.getProductDetails("012345678901", continuation));

try {
    ApiResponse<ProductDetails> response = future.get();
    System.out.println("Product: " + response.getData().getName());
} catch (Exception e) {
    System.err.println("Error: " + e.getMessage());
} finally {
    client.close();
}
```

### Android Usage

For Android projects, make sure to call API methods from a background thread:

```kotlin
class ProductViewModel : ViewModel() {
    private val client = ShopSavvyClient("ss_live_your_api_key_here")
    
    fun loadProduct(identifier: String) {
        viewModelScope.launch {
            try {
                val response = client.getProductDetails(identifier)
                // Update UI with response.data
            } catch (e: ShopSavvyException) {
                // Handle error
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        client.close()
    }
}
```

## API Methods

### Product Details
- `getProductDetails(identifier, format)` - Get details for a single product
- `getProductDetailsBatch(identifiers, format)` - Get details for multiple products

### Current Offers
- `getCurrentOffers(identifier, retailer, format)` - Get current offers for a product
- `getCurrentOffersBatch(identifiers, retailer, format)` - Get current offers for multiple products

### Price History
- `getPriceHistory(identifier, startDate, endDate, retailer, format)` - Get price history for a product

### Monitoring
- `scheduleProductMonitoring(identifier, frequency, retailer)` - Schedule product monitoring
- `getScheduledProducts()` - Get all scheduled products
- `removeProductFromSchedule(identifier)` - Remove product from monitoring

### Usage
- `getUsage()` - Get API usage information

## Exception Handling

The SDK provides specific exception types for different error conditions:

```kotlin
try {
    val response = client.getProductDetails("012345678901")
    // Handle success
} catch (e: ShopSavvyAuthenticationException) {
    println("Authentication failed: ${e.message}")
} catch (e: ShopSavvyNotFoundException) {
    println("Product not found: ${e.message}")
} catch (e: ShopSavvyValidationException) {
    println("Invalid parameters: ${e.message}")
} catch (e: ShopSavvyRateLimitException) {
    println("Rate limit exceeded: ${e.message}")
} catch (e: ShopSavvyNetworkException) {
    println("Network error: ${e.message}")
} catch (e: ShopSavvyException) {
    println("Other API error: ${e.message}")
}
```

## Configuration

You can customize the client configuration:

```kotlin
// Custom base URL and timeout
val client = ShopSavvyClient(
    apiKey = "ss_live_your_api_key_here",
    baseUrl = "https://api.shopsavvy.com/v1",
    timeoutSeconds = 60
)
```

## Data Models

All models are implemented as Kotlin data classes with kotlinx.serialization:

### ProductDetails
```kotlin
data class ProductDetails(
    val id: String,
    val name: String,
    val description: String?,
    val brand: String?,
    val category: String?,
    val upc: String?,
    val asin: String?,
    val modelNumber: String?,
    val images: List<String>?,
    val specifications: Map<String, String>?,
    val createdAt: String?,
    val updatedAt: String?
)
```

### Offer
```kotlin
data class Offer(
    val retailer: String,
    val price: Double?,
    val currency: String?,
    val availability: String?,
    val condition: String?,
    val shippingCost: Double?,
    val url: String?,
    val lastUpdated: String?
)
```

### UsageInfo
```kotlin
data class UsageInfo(
    val creditsUsed: Int?,
    val creditsRemaining: Int?,
    val creditsLimit: Int?,
    val resetDate: String?,
    val currentPeriodStart: String?,
    val currentPeriodEnd: String?
)
```

## Requirements

- Kotlin 1.9.10 or higher
- Java 8 or higher
- OkHttp for HTTP requests
- kotlinx.serialization for JSON processing
- kotlinx.coroutines for async operations

## License

MIT License - see [LICENSE](LICENSE) file for details.

## Support

For support, please visit [ShopSavvy Data API Documentation](https://shopsavvy.com/data) or contact business@shopsavvy.com.