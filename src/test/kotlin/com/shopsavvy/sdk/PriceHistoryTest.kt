package com.shopsavvy.sdk

import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Feeds a response of the exact shape `GET /v1/products/offers/history` returns (one entry per
 * product, each offer carrying its own `history`) through the real client and decoder.
 */
class PriceHistoryTest {
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
        requireNotNull(javaClass.getResource("/price-history-response.json")) { "fixture missing" }.readText()

    @Test
    fun sendsStartAndEndQueryParams() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(fixture()))

        client.getPriceHistory("611247373064", "2022-11-20", "2022-11-27", retailer = "amazon.com")

        val url = server.takeRequest().requestUrl!!
        assertEquals("/v1/products/offers/history", url.encodedPath)
        assertEquals("611247373064", url.queryParameter("ids"))
        assertEquals("2022-11-20", url.queryParameter("start"))
        assertEquals("2022-11-27", url.queryParameter("end"))
        assertEquals("amazon.com", url.queryParameter("retailer"))
        assertNull(url.queryParameter("start_date"))
        assertNull(url.queryParameter("end_date"))
    }

    @Test
    fun parsesProductsOffersAndHistory() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(fixture()))

        val response = client.getPriceHistory("611247373064,611247369449", "2022-11-20", "2022-11-27")

        assertTrue(response.success)
        assertEquals(2, response.data.size)
        assertEquals(14, response.creditsUsed())
        assertEquals(986, response.creditsRemaining())
        assertEquals("req-7f3c9a", response.meta?.requestId)
        assertEquals(999, response.meta?.rateLimitRemaining)

        val kMini = response.data[0]
        assertEquals("Keurig K-Mini Single Serve Coffee Maker, Black", kMini.title)
        assertEquals("3ONn300xybP3y66ibqc1", kMini.shopsavvy)
        assertEquals("611247373064", kMini.barcode)
        assertEquals("B07G14HTBZ", kMini.amazon)
        assertEquals("Keurig K-Mini", kMini.titleShort)
        assertEquals(1, kMini.images?.size)
        assertEquals(2, kMini.offers.size)

        val amazon = kMini.offers[0]
        assertEquals("0IUouCFtZEhxeOablTPl", amazon.id)
        assertEquals("Amazon", amazon.retailer)
        assertEquals(74.96, amazon.price)
        assertEquals("USD", amazon.currency)
        assertEquals("in", amazon.availability)
        assertEquals("new", amazon.condition)
        assertEquals("ACME Deals", amazon.seller)
        assertEquals("https://www.amazon.com/dp/B07G14HTBZ?m=A1GKQADQC2VI6E", amazon.url)
        assertEquals("2022-11-27T22:36:33.236Z", amazon.timestamp)
        assertEquals(3, amazon.history.size)
        assertEquals(PriceHistoryEntry("2022-11-27T22:36:33.236Z", 74.96, "USD", "in"), amazon.history[0])
        assertEquals(PriceHistoryEntry("2022-11-24T10:00:00.000Z", 70.99, "USD", "out"), amazon.history[1])
        // null currency and missing availability on an archived point
        val oldest = amazon.history[2]
        assertEquals(79.99, oldest.price)
        assertEquals("2022-11-21T08:15:00.000Z", oldest.timestamp)
        assertNull(oldest.currency)
        assertNull(oldest.availability)

        val bestBuy = kMini.offers[1]
        assertEquals("Best Buy", bestBuy.retailer)
        assertNull(bestBuy.seller)
        assertNull(bestBuy.availability)
        assertEquals(listOf(59.99, 64.99), bestBuy.history.map { it.price })

        val kElite = response.data[1]
        assertEquals("DrKWneG0MpFlZpwZXNYa", kElite.shopsavvy)
        assertEquals("611247369449", kElite.barcode)
        assertNull(kElite.category)
        assertNull(kElite.amazon)
        assertNull(kElite.mpn)
        assertEquals(emptyList(), kElite.images)
        assertEquals(1, kElite.offers.size)
        assertEquals("eBay", kElite.offers[0].retailer)
        assertEquals("used", kElite.offers[0].condition)
        assertEquals(emptyList(), kElite.offers[0].history)
    }
}
