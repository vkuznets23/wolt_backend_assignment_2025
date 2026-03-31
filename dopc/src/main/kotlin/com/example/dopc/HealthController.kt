package com.example.dopc

import com.example.dopc.client.HomeAssignmentApiClient
import com.example.dopc.client.dto.StaticResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

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
}
