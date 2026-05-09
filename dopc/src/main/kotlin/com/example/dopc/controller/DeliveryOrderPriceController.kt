package com.example.dopc.controller

import com.example.dopc.service.DeliveryOrderPriceService
import com.example.dopc.service.dto.DeliveryOrderPriceResponse
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.DecimalMax
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.validation.annotation.Validated

@Validated
@RestController
class DeliveryOrderPriceController(
        private val deliveryOrderPriceService: DeliveryOrderPriceService,
) {

        @GetMapping("/api/v1/delivery-order-price")
        fun deliveryOrderPrice(
                @RequestParam("venue_slug")
                @NotBlank(message = "venue_slug is required") 
                venueSlug: String,
                @RequestParam("cart_value") 
                @Min(value = 0, message = "cart_value must be >= 0") 
                cartValue: Int,
                @RequestParam("user_lat") 
                @DecimalMin(value = "-90.0", message = "user_lat must be between -90 and 90")
                @DecimalMax(value = "90.0", message = "user_lat must be between -90 and 90") 
                userLat: Double,
                @RequestParam("user_lon")
                @DecimalMax(value = "180.0", message = "user_lon must be between -180 and 180") 
                @DecimalMin(value = "-180.0", message = "user_lon must be between -180 and 180") 
                userLon: Double,
        ): DeliveryOrderPriceResponse {
                
                return deliveryOrderPriceService.getDeliveryOrderPrice(
                        venueSlug,
                        cartValue,
                        userLat,
                        userLon
                )
        }
}
