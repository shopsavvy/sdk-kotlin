# Changelog

All notable changes to this SDK are documented here. The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## 1.6.0 - 2026-11-17

### Added
- Every API error now carries the API's structured detail: `code` (for example `ERR_RATE_LIMIT`), `action`, `docsUrl`, `requestId`, `statusCode` and `retryAfterSeconds` (parsed from the `Retry-After` header). Each is `null` when the server did not send it, so responses without `error_detail` still work.
- `ShopSavvyCreditsExhaustedException` for `ERR_CREDITS_EXHAUSTED`.
- The map-returning methods (batch lookup and status, webhooks) now raise the same typed errors on a non-2xx response, and report network failures as `ShopSavvyNetworkException`.
- "Errors, rate limits and credits" section in the README, and a quick start that reads `SHOPSAVVY_API_KEY`.

### Changed
- Behavior change: exhausted credits (`ERR_CREDITS_EXHAUSTED`, HTTP 429) is no longer reported as `ShopSavvyRateLimitException`. Code that retried on `ShopSavvyRateLimitException` would loop on exhausted credits; catch `ShopSavvyCreditsExhaustedException` and stop instead. A 429 without a code still raises `ShopSavvyRateLimitException`.
- HTTP 400 responses now raise `ShopSavvyValidationException` (a subclass of `ShopSavvyException`, so existing catch blocks still match).
- README no longer says "Sign up for a free account"; it points to https://shopsavvy.com/data/api-keys after starting a plan.

## 1.5.0 - 2026-11-09

### Added
- `in_store_prices` (user-collected receipt prices) on current offers and batch results.

## 1.4.0 - 2026-09-27

### Fixed
- `getPriceHistory` returns products, each with offers carrying history, and sends `start`/`end`.
- Schedule and unschedule send their parameters as query parameters to `PUT`/`DELETE /products/scheduled`; scheduling responses decode the real shapes.
- The Gradle build compiles and the Maven build runs tests.

## 1.2.0 - 2026-07-19

### Changed
- Documented that scores are on a 0-1 scale with a free-form aspects map.

## 1.1.0 - 2026-04-07

### Added
- Deals and product reviews endpoints, batch lookup and batch status, and webhook management (create, list, test, delete). `updateWebhook` followed on 2026-05-20.

## 1.0.1 - 2025-12-16

### Fixed
- Models match the actual API response format.

## 1.0.0 - 2025-07-30

### Added
- Initial release.
