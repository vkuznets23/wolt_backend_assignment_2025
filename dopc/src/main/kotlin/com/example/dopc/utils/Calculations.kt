package com.example.dopc.utils

import kotlin.math.*

fun calculateSmallOrderSurcharge(
        cartValue: Int,
        orderMinimumNoSurcharge: Int,
): Int {
    return max(0, orderMinimumNoSurcharge - cartValue)
}

data class DistanceRange(val min: Int, val max: Int, val a: Int, val b: Int)

// fee = base_price + a + round(b * distance / 10)
fun calculateDeliveryFee(basePrice: Int, distance: Int, distanceRanges: List<DistanceRange>): Int? {
    val range =
            distanceRanges.find { range ->
                distance >= range.min && (range.max == 0 || distance < range.max)
            }

    if (range == null || range.max == 0) return null

    return basePrice + range.a + round(range.b * distance / 10.0).toInt()
}

fun calculateDistance(userLat: Double, userLon: Double, venueLat: Double, venueLon: Double): Int {
    val earthRadius = 6371000 // m
    val dLat = Math.toRadians(venueLat - userLat)
    val dLon = Math.toRadians(venueLon - userLon)

    val a =
            sin(dLat / 2).pow(2) +
                    cos(Math.toRadians(userLat)) *
                            cos(Math.toRadians(venueLat)) *
                            sin(dLon / 2).pow(2)

    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    return (earthRadius * c).toInt()
}

fun calculateTotalPrice(cartValue: Int, surcharge: Int, deliveryFee: Int): Int {
    return cartValue + surcharge + deliveryFee
}
