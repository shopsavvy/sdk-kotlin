package com.shopsavvy.sdk

import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The scheduling endpoints read query parameters only (`ids`, `schedule`, `retailer`), so these
 * tests assert the exact method, path and query string the real client sends, that no body is
 * sent, and that the real response shapes decode.
 */
class SchedulingTest {
    private lateinit var server: MockWebServer
    private lateinit var client: ShopSavvyClient

    private val scheduleResponse = """
        {
          "success": true,
          "data": [
            { "title": "Keurig K-Mini", "shopsavvy": "3ONn300xybP3y66ibqc1", "barcode": "611247373064",
              "brand": "Keurig", "category": null, "images": [], "schedule": "daily", "retailer": "amazon.com" },
            { "title": "Keurig K-Elite", "shopsavvy": "DrKWneG0MpFlZpwZXNYa", "barcode": "611247369449",
              "images": [], "schedule": "daily" }
          ],
          "meta": { "request_id": "req-1", "credits_used": 2, "credits_remaining": 998, "rate_limit_remaining": 999 }
        }
    """.trimIndent()

    private val removeResponse = """
        {
          "success": true,
          "message": "Products successfully removed from schedule",
          "meta": { "request_id": "req-2", "credits_used": 0, "credits_remaining": 0, "rate_limit_remaining": 0 }
        }
    """.trimIndent()

    @BeforeTest
    fun setUp() {
        server = MockWebServer()
        server.start()
        client = ShopSavvyClient("ss_test_abc123", server.url("/v1").toString().trimEnd('/'))
    }

    @AfterTest
    fun tearDown() {
        client.close()
        server.shutdown()
    }

    private fun assertNoBody(request: RecordedRequest) = assertEquals(0L, request.bodySize)

    @Test
    fun scheduleSingleSendsPutWithQueryParams() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(scheduleResponse))

        val response = client.scheduleProductMonitoring("611247373064", "daily")

        val request = server.takeRequest()
        assertEquals("PUT", request.method)
        assertEquals("/v1/products/scheduled", request.requestUrl!!.encodedPath)
        assertEquals("ids=611247373064&schedule=daily", request.requestUrl!!.encodedQuery)
        assertNoBody(request)

        assertTrue(response.success)
        assertEquals(2, response.data.size)
        assertEquals("daily", response.data[0].schedule)
        assertEquals("amazon.com", response.data[0].retailer)
        assertNull(response.data[0].category)
        assertNull(response.data[1].retailer)
        assertEquals(2, response.creditsUsed())
    }

    @Test
    fun scheduleSingleWithRetailer() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(scheduleResponse))

        client.scheduleProductMonitoring("611247373064", "hourly", retailer = "amazon.com")

        val request = server.takeRequest()
        assertEquals("PUT", request.method)
        assertEquals("/v1/products/scheduled", request.requestUrl!!.encodedPath)
        assertEquals("ids=611247373064&schedule=hourly&retailer=amazon.com", request.requestUrl!!.encodedQuery)
        assertNoBody(request)
    }

    @Test
    fun scheduleBatchJoinsAndEncodesIds() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(scheduleResponse))

        val response = client.scheduleProductsMonitoring(
            listOf("611247373064", "https://www.amazon.com/dp/B07G14HTBZ"),
            "weekly"
        )

        val request = server.takeRequest()
        assertEquals("PUT", request.method)
        assertEquals("/v1/products/scheduled", request.requestUrl!!.encodedPath)
        assertEquals(
            "ids=611247373064,https%3A%2F%2Fwww.amazon.com%2Fdp%2FB07G14HTBZ&schedule=weekly",
            request.requestUrl!!.encodedQuery
        )
        assertEquals("611247373064,https://www.amazon.com/dp/B07G14HTBZ", request.requestUrl!!.queryParameter("ids"))
        assertNoBody(request)
        assertEquals("DrKWneG0MpFlZpwZXNYa", response.data[1].shopsavvy)
    }

    @Test
    fun scheduleBatchWithRetailer() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(scheduleResponse))

        client.scheduleProductsMonitoring(listOf("611247373064", "611247369449"), "daily", retailer = "bestbuy.com")

        val request = server.takeRequest()
        assertEquals("PUT", request.method)
        assertEquals("/v1/products/scheduled", request.requestUrl!!.encodedPath)
        assertEquals("ids=611247373064,611247369449&schedule=daily&retailer=bestbuy.com", request.requestUrl!!.encodedQuery)
        assertNoBody(request)
    }

    @Test
    fun unscheduleSingleSendsDeleteWithIds() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(removeResponse))

        val response = client.removeProductFromSchedule("611247373064")

        val request = server.takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/v1/products/scheduled", request.requestUrl!!.encodedPath)
        assertEquals("ids=611247373064", request.requestUrl!!.encodedQuery)
        assertNoBody(request)

        assertTrue(response.success)
        assertEquals("Products successfully removed from schedule", response.message)
        assertEquals("req-2", response.meta?.requestId)
    }

    @Test
    fun unscheduleBatchSendsDeleteWithJoinedIds() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(removeResponse))

        client.removeProductsFromSchedule(listOf("611247373064", "611247369449"))

        val request = server.takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/v1/products/scheduled", request.requestUrl!!.encodedPath)
        assertEquals("ids=611247373064,611247369449", request.requestUrl!!.encodedQuery)
        assertNoBody(request)
    }

    @Test
    fun listScheduledDecodesProductsWithSchedule() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(scheduleResponse))

        val response = client.getScheduledProducts()

        val request = server.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/v1/products/scheduled", request.requestUrl!!.encodedPath)
        assertEquals("Keurig K-Mini", response.data[0].title)
        assertEquals("611247373064", response.data[0].barcode)
        assertEquals("daily", response.data[1].schedule)
    }
}
