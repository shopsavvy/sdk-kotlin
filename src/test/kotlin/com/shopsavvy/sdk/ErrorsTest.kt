package com.shopsavvy.sdk

import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Error surface: structured error_detail fields, Retry-After, and the distinct credits-exhausted
 * type. Runs the real client against a local HTTP server returning the API's error JSON.
 */
class ErrorsTest {
    private lateinit var server: MockWebServer
    private lateinit var client: ShopSavvyClient

    @BeforeTest
    fun setUp() {
        server = MockWebServer()
        server.start()
        client = ShopSavvyClient("ss_test_fixturekey123", server.url("/v1").toString().trimEnd('/'), 5)
    }

    @AfterTest
    fun tearDown() {
        client.close()
        server.shutdown()
    }

    private fun errorBody(code: String, message: String) =
        """{"success":false,"error":"$message","error_detail":{"code":"$code","message":"$message","action":"Do the thing","docs_url":"https://shopsavvy.com/data/documentation/errors#${code.lowercase()}"},"meta":{"request_id":"req_err_1"}}"""

    private fun enqueueError(status: Int, body: String, retryAfter: String? = null) {
        val response = MockResponse().setResponseCode(status).setHeader("Content-Type", "application/json").setBody(body)
        if (retryAfter != null) response.setHeader("Retry-After", retryAfter)
        server.enqueue(response)
    }

    @Test
    fun rateLimitCarriesDetailAndRetryAfter() = runTest {
        enqueueError(429, errorBody("ERR_RATE_LIMIT", "Rate limit exceeded"), "7")
        val e = assertFailsWith<ShopSavvyRateLimitException> { client.getUsage() }
        assertEquals("ERR_RATE_LIMIT", e.code)
        assertEquals("Do the thing", e.action)
        assertEquals("https://shopsavvy.com/data/documentation/errors#err_rate_limit", e.docsUrl)
        assertEquals("req_err_1", e.requestId)
        assertEquals(429, e.statusCode)
        assertEquals(7, e.retryAfterSeconds)
    }

    @Test
    fun creditsExhaustedIsItsOwnTypeAndNotARateLimit() = runTest {
        enqueueError(429, errorBody("ERR_CREDITS_EXHAUSTED", "Credits exhausted"))
        val e = assertFailsWith<ShopSavvyException> { client.getUsage() }
        assertIs<ShopSavvyCreditsExhaustedException>(e)
        assertFalse(e is ShopSavvyRateLimitException)
        assertEquals("ERR_CREDITS_EXHAUSTED", e.code)
        assertEquals(429, e.statusCode)
        assertNull(e.retryAfterSeconds)
        assertEquals("req_err_1", e.requestId)
    }

    @Test
    fun plainRateLimitWithoutErrorDetailStaysRateLimit() = runTest {
        enqueueError(429, """{"success":false,"error":"Too many requests"}""", "3")
        val e = assertFailsWith<ShopSavvyRateLimitException> { client.getUsage() }
        assertNull(e.code)
        assertNull(e.action)
        assertNull(e.docsUrl)
        assertNull(e.requestId)
        assertEquals(3, e.retryAfterSeconds)
    }

    @Test
    fun authenticationErrorCarriesDetail() = runTest {
        enqueueError(401, errorBody("ERR_AUTH_REVOKED", "API key revoked"))
        val e = assertFailsWith<ShopSavvyAuthenticationException> { client.getUsage() }
        assertEquals("ERR_AUTH_REVOKED", e.code)
        assertEquals(401, e.statusCode)
        assertNotNull(e.docsUrl)
    }

    @Test
    fun validationAndNotFoundCarryDetail() = runTest {
        enqueueError(400, errorBody("ERR_INVALID_PARAMS", "Bad params"))
        val v = assertFailsWith<ShopSavvyValidationException> { client.getUsage() }
        assertEquals("ERR_INVALID_PARAMS", v.code)

        enqueueError(404, errorBody("ERR_PRODUCT_NOT_FOUND", "No such product"))
        val n = assertFailsWith<ShopSavvyNotFoundException> { client.getUsage() }
        assertEquals("ERR_PRODUCT_NOT_FOUND", n.code)
    }

    @Test
    fun internalErrorIsBaseTypeWithDetail() = runTest {
        enqueueError(500, errorBody("ERR_INTERNAL", "Something broke"))
        val e = assertFailsWith<ShopSavvyException> { client.getUsage() }
        assertEquals(ShopSavvyException::class, e::class)
        assertEquals("ERR_INTERNAL", e.code)
        assertEquals(500, e.statusCode)
        assertTrue(e.message!!.contains("Something broke"))
    }

    @Test
    fun mapReturningMethodsRaiseTypedErrorsToo() = runTest {
        enqueueError(429, errorBody("ERR_CREDITS_EXHAUSTED", "Credits exhausted"))
        assertFailsWith<ShopSavvyCreditsExhaustedException> { client.batchLookup(listOf("611247373064")) }
        enqueueError(429, errorBody("ERR_RATE_LIMIT", "Rate limit exceeded"), "2")
        val e = assertFailsWith<ShopSavvyRateLimitException> { client.listWebhooks() }
        assertEquals(2, e.retryAfterSeconds)
    }

    @Test
    fun liveBogusKeyIsRevokedWithCodeAndDocsUrl() = runTest {
        if (System.getenv("SHOPSAVVY_LIVE_TESTS") != "1") return@runTest // opt-in: set SHOPSAVVY_LIVE_TESTS=1
        ShopSavvyClient("ss_live_" + "a".repeat(32)).use { live ->
            val e = assertFailsWith<ShopSavvyAuthenticationException> { live.getUsage() }
            assertEquals("ERR_AUTH_REVOKED", e.code)
            assertNotNull(e.docsUrl)
            assertEquals(401, e.statusCode)
        }
    }
}
