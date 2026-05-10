package com.example.dopc.service

import com.example.dopc.service.dto.DeliveryInfo
import com.example.dopc.service.dto.DeliveryOrderPriceResponse
import com.example.dopc.utils.calculateDeliveryFee
import com.example.dopc.utils.calculateDistance
import com.example.dopc.utils.calculateSmallOrderSurcharge
import com.example.dopc.utils.calculateTotalPrice
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import org.slf4j.LoggerFactory  
import com.example.dopc.service.VenueDataCacheService

@Service
class DeliveryOrderPriceService(private val venueDataCacheService: VenueDataCacheService) {
    private val log = LoggerFactory.getLogger(DeliveryOrderPriceService::class.java)

    fun getDeliveryOrderPrice(
            venueSlug: String,
            cartValue: Int,
            userLat: Double,
            userLon: Double,
    ): DeliveryOrderPriceResponse {
        val static = venueDataCacheService.getStatic(venueSlug)
        val dynamic = venueDataCacheService.getDynamic(venueSlug)

        val coordinates = static.venueRaw.location.coordinates
        if (coordinates.size != 2) {
            log.error("[SERVICE] Invalid venue coordinates format from upstream API")
            throw ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
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
                        ?: run {
                                log.error("[SERVICE] Delivery is not possible for this distance")
                                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Delivery is not possible for this distance")
                        }

        val total = calculateTotalPrice(cartValue, surcharge, deliveryFee)

        return DeliveryOrderPriceResponse(
                totalPrice = total,
                smallOrderSurcharge = surcharge,
                cartValue = cartValue,
                delivery = DeliveryInfo(fee = deliveryFee, distance = distance)
        )
    }
}
