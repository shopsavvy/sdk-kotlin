package com.shopsavvy.sdk

/**
 * Base exception for ShopSavvy API errors.
 *
 * Errors returned by the API also carry the structured detail from the response body
 * ([code], [action], [docsUrl], [requestId]), the HTTP [statusCode] and, for rate limits, the
 * `Retry-After` header as [retryAfterSeconds]. Each is null when the server did not send it
 * (older responses have no `error_detail`).
 */
open class ShopSavvyException(
    message: String,
    cause: Throwable? = null,
    /** HTTP status of the failed response; null when the failure was not an HTTP response. */
    val statusCode: Int? = null,
    /** API error code such as `ERR_RATE_LIMIT`. */
    val code: String? = null,
    /** One-line fix suggested by the API. */
    val action: String? = null,
    /** Link to the documentation for this error code. */
    val docsUrl: String? = null,
    /** Server-side request id; quote it when contacting support. */
    val requestId: String? = null,
    /** Seconds from the `Retry-After` header; only meaningful for `ERR_RATE_LIMIT`. */
    val retryAfterSeconds: Int? = null
) : Exception(message, cause)

/**
 * Exception thrown when API key authentication fails
 */
class ShopSavvyAuthenticationException(
    message: String, cause: Throwable? = null, statusCode: Int? = null, code: String? = null,
    action: String? = null, docsUrl: String? = null, requestId: String? = null, retryAfterSeconds: Int? = null
) : ShopSavvyException(message, cause, statusCode, code, action, docsUrl, requestId, retryAfterSeconds)

/**
 * Exception thrown when a requested resource is not found
 */
class ShopSavvyNotFoundException(
    message: String, cause: Throwable? = null, statusCode: Int? = null, code: String? = null,
    action: String? = null, docsUrl: String? = null, requestId: String? = null, retryAfterSeconds: Int? = null
) : ShopSavvyException(message, cause, statusCode, code, action, docsUrl, requestId, retryAfterSeconds)

/**
 * Exception thrown when request parameters fail validation
 */
class ShopSavvyValidationException(
    message: String, cause: Throwable? = null, statusCode: Int? = null, code: String? = null,
    action: String? = null, docsUrl: String? = null, requestId: String? = null, retryAfterSeconds: Int? = null
) : ShopSavvyException(message, cause, statusCode, code, action, docsUrl, requestId, retryAfterSeconds)

/**
 * Exception thrown when the API rate limit is exceeded (`ERR_RATE_LIMIT`, or a 429 without a code).
 * Wait [retryAfterSeconds] seconds and retry. Not thrown for exhausted credits, see
 * [ShopSavvyCreditsExhaustedException].
 */
class ShopSavvyRateLimitException(
    message: String, cause: Throwable? = null, statusCode: Int? = null, code: String? = null,
    action: String? = null, docsUrl: String? = null, requestId: String? = null, retryAfterSeconds: Int? = null
) : ShopSavvyException(message, cause, statusCode, code, action, docsUrl, requestId, retryAfterSeconds)

/**
 * Exception thrown for `ERR_CREDITS_EXHAUSTED` (HTTP 429). Do not retry: credits come back when the
 * billing period resets or the plan is upgraded. Deliberately NOT a [ShopSavvyRateLimitException],
 * so code that retries on rate limits does not loop on exhausted credits.
 */
class ShopSavvyCreditsExhaustedException(
    message: String, cause: Throwable? = null, statusCode: Int? = null, code: String? = null,
    action: String? = null, docsUrl: String? = null, requestId: String? = null, retryAfterSeconds: Int? = null
) : ShopSavvyException(message, cause, statusCode, code, action, docsUrl, requestId, retryAfterSeconds)

/**
 * Exception thrown when network errors occur
 */
class ShopSavvyNetworkException(message: String, cause: Throwable? = null) : ShopSavvyException(message, cause)
