package com.shopsavvy.sdk

import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * `GET /v1/products/offers` carries, per product, an `in_store_prices` array: shelf prices
 * read from shoppers' scanned receipts, labeled `source: "user-collected"`. Fed through the
 * real client and decoder.
 */
class InStorePricesTest {
    private lateinit var server: MockWebServer
    private lateinit var client: ShopSavvyClient

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

    private fun fixture(): String =
        requireNotNull(javaClass.getResource("/offers-in-store-response.json")) { "fixture missing" }.readText()

    @Test
    fun parsesUserCollectedInStorePrices() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(fixture()))

        val response = client.getCurrentOffers("011110838360")

        assertEquals("/v1/products/offers", server.takeRequest().requestUrl!!.encodedPath)
        val product = response.data.first()
        val inStore = product.inStorePrices.single()
        assertEquals("user-collected", inStore.source)
        assertEquals("Kroger", inStore.retailer)
        assertEquals("1014 Vine St, Cincinnati, OH 45202, USA", inStore.store.address)
        assertEquals(39.1086, inStore.store.latitude)
        assertEquals(3.49, inStore.price)
        assertEquals("2026-11-07", inStore.observedOn)
        assertEquals("Walmart", product.offers.first().retailer)
    }

    @Test
    fun emptyWhenTheServerSendsNone() = runTest {
        val body = fixture().replace(Regex("(?s),\\s*\"in_store_prices\": \\[.*?\\]\\s*\\}\\s*\\]"), "}]")
        server.enqueue(MockResponse().setResponseCode(200).setBody(body))

        val response = client.getCurrentOffers("011110838360")

        assertTrue(response.data.first().inStorePrices.isEmpty())
    }
}
