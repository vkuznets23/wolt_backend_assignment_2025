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

data class VenueRawDynamic(val delivery_specs: DeliverySpecs)

data class DeliverySpecs(
        val order_minimum_no_surcharge: Int,
        val delivery_pricing: DeliveryPricing
)

data class DeliveryPricing(val base_price: Int, val distance_ranges: List<DistanceRange>)

data class DistanceRange(val max: Int, val a: Int, val b: Int, val flag: String?)
