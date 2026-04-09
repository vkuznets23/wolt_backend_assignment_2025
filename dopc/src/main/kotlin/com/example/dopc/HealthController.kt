package com.example.dopc

import com.example.dopc.client.DeliveryInfo
import com.example.dopc.client.DeliveryOrderPriceResponse
import com.example.dopc.client.HomeAssignmentApiClient
import com.example.dopc.client.dto.DynamicResponse
import com.example.dopc.client.dto.StaticResponse
import com.example.dopc.utils.calculateDeliveryFee
import com.example.dopc.utils.calculateDistance
import com.example.dopc.utils.calculateSmallOrderSurcharge
import com.example.dopc.utils.calculateTotalPrice
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
class HealthController(private val homeAssignmentApiClient: HomeAssignmentApiClient) {

    @GetMapping("/") fun root(): String = "DOPC API is running"

    @GetMapping("/health")
    fun health(): String {
        return "OK"
    }

    @GetMapping("/venue/{slug}/static")
    fun getVenueStatic(@PathVariable slug: String): StaticResponse {
        return homeAssignmentApiClient.fetchStatic(venueSlug = slug)
    }

    @GetMapping("/venue/{slug}/dynamic")
    fun getVenueDynamic(@PathVariable slug: String): DynamicResponse {
        return homeAssignmentApiClient.fetchDynamic(venueSlug = slug)
    }

    @GetMapping("/api/v1/delivery-order-price")
    fun getDeliveryOrderPrice(
            @RequestParam("venue_slug") venueSlug: String,
            @RequestParam("cart_value") cartValue: Int,
            @RequestParam("user_lat") userLat: Double,
            @RequestParam("user_lon") userLon: Double,
    ): DeliveryOrderPriceResponse {
        // validate input parameters
        if (cartValue < 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "cart_value must be >= 0")
        }
        if (userLat !in -90.0..90.0) {
            throw ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "user_lat must be between -90 and 90"
            )
        }
        if (userLon !in -180.0..180.0) {
            throw ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "user_lon must be between -180 and 180"
            )
        }
        //

        val static = homeAssignmentApiClient.fetchStatic(venueSlug)
        val dynamic = homeAssignmentApiClient.fetchDynamic(venueSlug)

        val coordinates = static.venueRaw.location.coordinates
        if (coordinates.size != 2) {
            throw ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid venue coordinates format from upstream API"
            )
        }

        val venueLon = coordinates[0]
        val venueLat = coordinates[1]

        val distance = calculateDistance(userLat, userLon, venueLat, venueLon)

        val orderMinimumNoSurcharge = dynamic.venueRaw.deliverySpecs.orderMinimumNoSurcharge
        val deliveryPricing = dynamic.venueRaw.deliverySpecs.deliveryPricing
        val basePrice = deliveryPricing.basePrice
        val distanceRanges = deliveryPricing.distanceRanges

        val surcharge = calculateSmallOrderSurcharge(cartValue, orderMinimumNoSurcharge)
        val deliveryFee =
                calculateDeliveryFee(basePrice, distance, distanceRanges)
                        ?: throw ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Delivery is not possible for this distance"
                        )

        val total = calculateTotalPrice(cartValue, surcharge, deliveryFee)

        return DeliveryOrderPriceResponse(
                totalPrice = total,
                smallOrderSurcharge = surcharge,
                cartValue = cartValue,
                delivery = DeliveryInfo(fee = deliveryFee, distance = distance)
        )
    }
}
