package com.example.dopc.controller

import com.example.dopc.server.ValidationService
import com.example.dopc.api.DeliveryOrderPriceService
import com.example.dopc.api.dto.DeliveryOrderPriceResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class DeliveryOrderPriceController(
        private val deliveryOrderPriceService: DeliveryOrderPriceService,
        private val validationService: ValidationService,
) {

        @GetMapping("/api/v1/delivery-order-price")
        fun deliveryOrderPrice(
                @RequestParam("venue_slug") venueSlug: String,
                @RequestParam("cart_value") cartValue: Int,
                @RequestParam("user_lat") userLat: Double,
                @RequestParam("user_lon") userLon: Double,
        ): DeliveryOrderPriceResponse {
                validationService.validate(venueSlug, cartValue, userLat, userLon)
                
                return deliveryOrderPriceService.getDeliveryOrderPrice(
                        venueSlug,
                        cartValue,
                        userLat,
                        userLon
                )
        }
}
