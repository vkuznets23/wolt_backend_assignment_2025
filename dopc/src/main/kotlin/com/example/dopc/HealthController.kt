package com.example.dopc

import com.example.dopc.api.DeliveryOrderPriceService
import com.example.dopc.client.HomeAssignmentApiClient
import com.example.dopc.client.dto.DynamicResponse
import com.example.dopc.client.dto.StaticResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
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

    @GetMapping("/api/v1/venue/{slug}/static")
    fun venueStatic(@PathVariable slug: String): StaticResponse {
        return homeAssignmentApiClient.fetchStatic(slug)
    }

    @GetMapping("/api/v1/venue/{slug}/dynamic")
    fun venueDynamic(@PathVariable slug: String): DynamicResponse {
        return homeAssignmentApiClient.fetchDynamic(slug)
    }
}
