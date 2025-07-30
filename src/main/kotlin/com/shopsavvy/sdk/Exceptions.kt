package com.shopsavvy.sdk

/**
 * Base exception for ShopSavvy API errors
 */
open class ShopSavvyException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * Exception thrown when API key authentication fails
 */
class ShopSavvyAuthenticationException(message: String, cause: Throwable? = null) : ShopSavvyException(message, cause)

/**
 * Exception thrown when a requested resource is not found
 */
class ShopSavvyNotFoundException(message: String, cause: Throwable? = null) : ShopSavvyException(message, cause)

/**
 * Exception thrown when request parameters fail validation
 */
class ShopSavvyValidationException(message: String, cause: Throwable? = null) : ShopSavvyException(message, cause)

/**
 * Exception thrown when API rate limits are exceeded
 */
class ShopSavvyRateLimitException(message: String, cause: Throwable? = null) : ShopSavvyException(message, cause)

/**
 * Exception thrown when network errors occur
 */
class ShopSavvyNetworkException(message: String, cause: Throwable? = null) : ShopSavvyException(message, cause)