package com.example.dopc.client.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class StaticResponse(@JsonProperty("venue_raw") val venueRaw: VenueRaw)

data class VenueRaw(val location: Location)

data class Location(
        // [longitude, latitude]
        val coordinates: List<Double>
)
