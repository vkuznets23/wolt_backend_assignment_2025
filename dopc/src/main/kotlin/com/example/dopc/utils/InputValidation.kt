package com.example.dopc.utils

fun validateInput(
        venueSlug: String,
        cartValue: Int,
        userLat: Double,
        userLon: Double,
): String? {
    if (venueSlug.isBlank()) {
        return "venue_slug is required"
    }
    if (cartValue < 0) {
        return "cart_value must be >= 0"
    }
    if (userLat !in -90.0..90.0) {
        return "user_lat must be between -90 and 90"
    }
    if (userLon !in -180.0..180.0) {
        return "user_lon must be between -180 and 180"
    }
    return null
}
