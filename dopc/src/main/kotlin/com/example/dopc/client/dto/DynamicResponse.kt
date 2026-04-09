package com.example.dopc.client.dto

import com.fasterxml.jackson.annotation.JsonProperty

/*
* Response from Home Assignment API
*
* {
*   "venue_raw": { // -> @JsonProperty("venue_raw")
*     "delivery_specs": {
*       "order_minimum_no_surcharge": 50,
*       "delivery_pricing": {
*        "base_price": 100,
*        "distance_ranges": [
*          {
*            "max": 500,
*            "min": 0,
*            "a": 0,
             "b": 0,
             "flag": null
*          }
*        ]
*       }
*      }
*     }
*/

data class DynamicResponse(@JsonProperty("venue_raw") val venueRaw: VenueRawDynamic)

data class VenueRawDynamic(@JsonProperty("delivery_specs") val deliverySpecs: DeliverySpecs)

data class DeliverySpecs(
        @JsonProperty("order_minimum_no_surcharge") val orderMinimumNoSurcharge: Int,
        @JsonProperty("delivery_pricing") val deliveryPricing: DeliveryPricing
)

data class DeliveryPricing(
        @JsonProperty("base_price") val basePrice: Int,
        @JsonProperty("distance_ranges") val distanceRanges: List<DistanceRange>
)

data class DistanceRange(val max: Int, val min: Int, val a: Int, val b: Int, val flag: String?)
