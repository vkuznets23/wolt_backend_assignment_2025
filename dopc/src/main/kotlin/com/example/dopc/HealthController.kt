package com.example.dopc

import com.example.dopc.api.DeliveryOrderPriceResponse
import com.example.dopc.api.DeliveryOrderPriceService
import com.example.dopc.client.HomeAssignmentApiClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class HealthController(
        private val homeAssignmentApiClient: HomeAssignmentApiClient,
        private val deliveryOrderPriceService: DeliveryOrderPriceService,
) {

    @GetMapping("/") fun root(): String = "DOPC API is running"

    @GetMapping("/health")
    fun health(): String {
        return "OK"
    }

    @GetMapping("/api/v1/delivery-order-price")
    fun deliveryOrderPrice(
            @RequestParam("venue_slug") venueSlug: String,
            @RequestParam("cart_value") cartValue: Int,
            @RequestParam("user_lat") userLat: Double,
            @RequestParam("user_lon") userLon: Double,
    ): DeliveryOrderPriceResponse {
        return deliveryOrderPriceService.getDeliveryOrderPrice(
                venueSlug,
                cartValue,
                userLat,
                userLon
        )
    }
}
