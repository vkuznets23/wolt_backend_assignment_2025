package com.example.dopc.api

import com.example.dopc.client.HomeAssignmentApiClient
import com.example.dopc.client.dto.DeliveryPricing
import com.example.dopc.client.dto.DeliverySpecs
import com.example.dopc.client.dto.DistanceRange
import com.example.dopc.client.dto.DynamicResponse
import com.example.dopc.client.dto.Location
import com.example.dopc.client.dto.StaticResponse
import com.example.dopc.client.dto.VenueRawDynamic
import com.example.dopc.client.dto.VenueRawStatic
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class DeliveryOrderPriceServiceTest {
        private val client = mock(HomeAssignmentApiClient::class.java)
        private val service = DeliveryOrderPriceService(client)

        @Test
        fun `returns expected response on happy path`() {
                `when`(client.fetchStatic("venue-1"))
                        .thenReturn(
                                StaticResponse(VenueRawStatic(Location(listOf(24.93087, 60.17094))))
                        )
                `when`(client.fetchDynamic("venue-1"))
                        .thenReturn(
                                DynamicResponse(
                                        VenueRawDynamic(
                                                DeliverySpecs(
                                                        orderMinimumNoSurcharge = 1000,
                                                        deliveryPricing =
                                                                DeliveryPricing(
                                                                        basePrice = 190,
                                                                        distanceRanges =
                                                                                listOf(
                                                                                        DistanceRange(
                                                                                                max =
                                                                                                        500,
                                                                                                min =
                                                                                                        0,
                                                                                                a =
                                                                                                        0,
                                                                                                b =
                                                                                                        0,
                                                                                                flag =
                                                                                                        null
                                                                                        ),
                                                                                        DistanceRange(
                                                                                                max =
                                                                                                        0,
                                                                                                min =
                                                                                                        500,
                                                                                                a =
                                                                                                        0,
                                                                                                b =
                                                                                                        0,
                                                                                                flag =
                                                                                                        null
                                                                                        )
                                                                                )
                                                                )
                                                )
                                        )
                                )
                        )

                val result = service.getDeliveryOrderPrice("venue-1", 1000, 60.17094, 24.93087)

                assertEquals(1190, result.totalPrice)
                assertEquals(0, result.smallOrderSurcharge)
                assertEquals(1000, result.cartValue)
                assertEquals(190, result.delivery.fee)
                assertEquals(0, result.delivery.distance)
        }

        @Test
        fun `return bad request when cartValue is negative`() {
                val ex =
                        assertFailsWith<ResponseStatusException> {
                                service.getDeliveryOrderPrice("venue-1", -1000, 60.17094, 24.93087)
                        }
                assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
                assertEquals("400 BAD_REQUEST \"cart_value must be >= 0\"", ex.message)
        }

        @Test
        fun `return bad request when userLat is out of range`() {
                val ex =
                        assertFailsWith<ResponseStatusException> {
                                service.getDeliveryOrderPrice("venue-1", 1000, 91.0, 24.93087)
                        }
                assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
                assertEquals("400 BAD_REQUEST \"user_lat must be between -90 and 90\"", ex.message)
        }

        @Test
        fun `return bad request when userLon is out of range`() {
                val ex =
                        assertFailsWith<ResponseStatusException> {
                                service.getDeliveryOrderPrice("venue-1", 1000, 60.17094, 181.0)
                        }
                assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
                assertEquals(
                        "400 BAD_REQUEST \"user_lon must be between -180 and 180\"",
                        ex.message
                )
        }

        @Test
        fun `throws bad request for blank venue slug`() {
                val ex =
                        assertFailsWith<ResponseStatusException> {
                                service.getDeliveryOrderPrice("   ", 1000, 60.17094, 24.93087)
                        }
                assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
                assertEquals("400 BAD_REQUEST \"venue_slug is required\"", ex.message)
                verifyNoInteractions(client)
        }

        @Test
        fun `throws bad gateway for invalid upstream coordinates`() {
                `when`(client.fetchStatic("venue-1"))
                        .thenReturn(StaticResponse(VenueRawStatic(Location(listOf(24.93087)))))
                `when`(client.fetchDynamic("venue-1"))
                        .thenReturn(
                                DynamicResponse(
                                        VenueRawDynamic(
                                                DeliverySpecs(
                                                        orderMinimumNoSurcharge = 1000,
                                                        deliveryPricing =
                                                                DeliveryPricing(
                                                                        basePrice = 190,
                                                                        distanceRanges = emptyList()
                                                                )
                                                )
                                        )
                                )
                        )

                val ex =
                        assertFailsWith<ResponseStatusException> {
                                service.getDeliveryOrderPrice("venue-1", 1000, 60.17094, 24.93087)
                        }
                assertEquals(HttpStatus.BAD_GATEWAY, ex.statusCode)
        }

        @Test
        fun `throws bad request when delivery is not possible`() {
                `when`(client.fetchStatic("venue-1"))
                        .thenReturn(
                                StaticResponse(VenueRawStatic(Location(listOf(24.93087, 60.17094))))
                        )
                `when`(client.fetchDynamic("venue-1"))
                        .thenReturn(
                                DynamicResponse(
                                        VenueRawDynamic(
                                                DeliverySpecs(
                                                        orderMinimumNoSurcharge = 1000,
                                                        deliveryPricing =
                                                                DeliveryPricing(
                                                                        basePrice = 190,
                                                                        distanceRanges =
                                                                                listOf(
                                                                                        DistanceRange(
                                                                                                max =
                                                                                                        0,
                                                                                                min =
                                                                                                        0,
                                                                                                a =
                                                                                                        0,
                                                                                                b =
                                                                                                        0,
                                                                                                flag =
                                                                                                        null
                                                                                        )
                                                                                )
                                                                )
                                                )
                                        )
                                )
                        )

                val ex =
                        assertFailsWith<ResponseStatusException> {
                                service.getDeliveryOrderPrice("venue-1", 1000, 60.17094, 24.93087)
                        }
                assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
                assertEquals(
                        "400 BAD_REQUEST \"Delivery is not possible for this distance\"",
                        ex.message
                )
        }
}
