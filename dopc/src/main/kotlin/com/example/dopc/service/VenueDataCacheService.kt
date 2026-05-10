package com.example.dopc.service

import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import com.example.dopc.client.HomeAssignmentClient
import com.example.dopc.client.dto.StaticResponse
import com.example.dopc.client.dto.DynamicResponse

@Service class VenueDataCacheService (private val homeAssignmentClient: HomeAssignmentClient) {
    @Cacheable(cacheNames = ["static"], key = "#venueSlug")
    fun getStatic(venueSlug: String): StaticResponse = homeAssignmentClient.fetchStatic(venueSlug)

    @Cacheable(cacheNames = ["dynamic"], key = "#venueSlug")
    fun getDynamic(venueSlug: String): DynamicResponse = homeAssignmentClient.fetchDynamic(venueSlug)
}