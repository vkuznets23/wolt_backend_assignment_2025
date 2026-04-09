package com.example.dopc.client.dto

import com.fasterxml.jackson.annotation.JsonProperty

/*
 * Response from Home Assignment API
 *
 * {
 *   "venue_raw": { // -> @JsonProperty("venue_raw")
 *     "location": {
 *       "coordinates": [12.3456, 78.9012]
 *     }
 */

// @JsonProperty("venue_raw") is used to say that JSON property name is "venue_raw"
data class StaticResponse(@JsonProperty("venue_raw") val venueRaw: VenueRawStatic)

data class VenueRawStatic(val location: Location)

data class Location(
        // [longitude, latitude]
        val coordinates: List<Double>
)
