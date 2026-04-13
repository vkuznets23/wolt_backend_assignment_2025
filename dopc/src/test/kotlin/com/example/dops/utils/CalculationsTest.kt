package com.example.dopc.utils

import com.example.dopc.client.dto.DistanceRange
import kotlin.test.Test
import kotlin.test.assertEquals

class CalculationsTest {

    @Test
    fun `surcharge is zero when cart value meets minimum`() {
        assertEquals(0, calculateSmallOrderSurcharge(1000, 1000))
    }

    @Test
    fun `surcharge is difference when cart value is below minimum`() {
        assertEquals(200, calculateSmallOrderSurcharge(800, 1000))
    }

    @Test
    fun `surcharge is zero when cart value exceeds minimum`() {
        assertEquals(0, calculateSmallOrderSurcharge(1500, 1000))
    }

    @Test
    fun `fee calculated correctly for first range`() {
        val ranges =
                listOf(
                        DistanceRange(min = 0, max = 500, a = 0, b = 0, flag = null),
                        DistanceRange(min = 500, max = 1000, a = 100, b = 1, flag = null),
                        DistanceRange(min = 1000, max = 0, a = 0, b = 0, flag = null)
                )
        // 199 + 0 + round(0 * 300 / 10) = 199
        assertEquals(
                199,
                calculateDeliveryFee(distance = 300, basePrice = 199, distanceRanges = ranges)
        )
    }

    @Test
    fun `fee calculated correctly for second range`() {
        val ranges =
                listOf(
                        DistanceRange(min = 0, max = 500, a = 0, b = 0, flag = null),
                        DistanceRange(min = 500, max = 1000, a = 100, b = 1, flag = null),
                        DistanceRange(min = 1000, max = 0, a = 0, b = 0, flag = null)
                )
        // 199 + 100 + round(1 * 600 / 10) = 359
        assertEquals(
                359,
                calculateDeliveryFee(distance = 600, basePrice = 199, distanceRanges = ranges)
        )
    }

    @Test
    fun `returns null when delivery not possible`() {
        val ranges =
                listOf(
                        DistanceRange(min = 0, max = 500, a = 0, b = 0, flag = null),
                        DistanceRange(min = 500, max = 1000, a = 100, b = 1, flag = null),
                        DistanceRange(min = 1000, max = 0, a = 0, b = 0, flag = null)
                )
        assertEquals(
                null,
                calculateDeliveryFee(distance = 1000, basePrice = 199, distanceRanges = ranges)
        )
    }

    @Test
    fun `distance is zero for identical coordinates`() {
        val distance =
                calculateDistance(
                        userLat = 60.170833,
                        userLon = 24.9375,
                        venueLat = 60.170833,
                        venueLon = 24.9375
                )
        assertEquals(0, distance)
    }

    @Test
    fun `distance is around one kilometer for known Helsinki points`() {
        val distance =
                calculateDistance(
                        userLat = 60.1699, // Kamppi area
                        userLon = 24.9384,
                        venueLat = 60.1786, // approx 1 km north
                        venueLon = 24.9410
                )
        // Keep tolerance broad enough for haversine rounding to Int
        val expected = 980
        val tolerance = 120
        val lowerBound = expected - tolerance
        val upperBound = expected + tolerance
        kotlin.test.assertTrue(
                distance in lowerBound..upperBound,
                "Expected distance to be in $lowerBound..$upperBound, but was $distance"
        )
    }

    @Test
    fun `delivery fee includes lower bound distance in range`() {
        val ranges =
                listOf(
                        DistanceRange(min = 0, max = 500, a = 0, b = 0, flag = null),
                        DistanceRange(min = 500, max = 1000, a = 100, b = 1, flag = null),
                        DistanceRange(min = 1000, max = 0, a = 0, b = 0, flag = null)
                )

        // distance == min of second range (500) => second range should be selected
        // 199 + 100 + round(1 * 500 / 10) = 199 + 100 + 50 = 349
        assertEquals(
                349,
                calculateDeliveryFee(basePrice = 199, distance = 500, distanceRanges = ranges)
        )
    }

    @Test
    fun `delivery fee rounds b times distance over ten with half up`() {
        val ranges =
                listOf(
                        DistanceRange(min = 0, max = 1000, a = 0, b = 1, flag = null),
                        DistanceRange(min = 1000, max = 0, a = 0, b = 0, flag = null)
                )

        // b * distance / 10 = 1 * 35 / 10 = 3.5 => round(3.5) = 4
        // fee = 199 + 0 + 4 = 203
        assertEquals(
                203,
                calculateDeliveryFee(basePrice = 199, distance = 35, distanceRanges = ranges)
        )
    }

    @Test
    fun `delivery fee is null for empty distance ranges`() {
        assertEquals(
                null,
                calculateDeliveryFee(basePrice = 199, distance = 300, distanceRanges = emptyList())
        )
    }

    @Test
    fun `delivery fee is null when distance falls into gap between ranges`() {
        val ranges =
                listOf(
                        DistanceRange(min = 0, max = 500, a = 0, b = 0, flag = null),
                        DistanceRange(min = 700, max = 1000, a = 100, b = 1, flag = null),
                        DistanceRange(min = 1000, max = 0, a = 0, b = 0, flag = null)
                )

        // 600 is not covered by any range
        assertEquals(
                null,
                calculateDeliveryFee(basePrice = 199, distance = 600, distanceRanges = ranges)
        )
    }

    @Test
    fun `delivery fee is null for open ended range marker when distance above 1000`() {
        val ranges =
                listOf(
                        DistanceRange(min = 0, max = 500, a = 0, b = 0, flag = null),
                        DistanceRange(min = 500, max = 1000, a = 100, b = 1, flag = null),
                        DistanceRange(min = 1000, max = 0, a = 0, b = 0, flag = null)
                )

        // By current implementation, max == 0 acts as "not deliverable"
        assertEquals(
                null,
                calculateDeliveryFee(basePrice = 199, distance = 1200, distanceRanges = ranges)
        )
    }

    @Test
    fun `distance is symmetric from A to B and B to A`() {
        val aLat = 60.1699
        val aLon = 24.9384
        val bLat = 60.1786
        val bLon = 24.9410

        val ab = calculateDistance(aLat, aLon, bLat, bLon)
        val ba = calculateDistance(bLat, bLon, aLat, aLon)

        assertEquals(ab, ba)
    }

    @Test
    fun `total price calculated correclty`() {
        assertEquals(1100, calculateTotalPrice(cartValue = 1000, surcharge = 0, deliveryFee = 100))
    }

    @Test
    fun `total price calculated with 0`() {
        assertEquals(0, calculateTotalPrice(cartValue = 0, surcharge = 0, deliveryFee = 0))
    }
}
