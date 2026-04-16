package com.example.dopc.api.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class DeliveryOrderPriceResponse(
        @JsonProperty("total_price") val totalPrice: Int,
        @JsonProperty("small_order_surcharge") val smallOrderSurcharge: Int,
        @JsonProperty("cart_value") val cartValue: Int,
        val delivery: DeliveryInfo,
)

data class DeliveryInfo(
        val fee: Int,
        val distance: Int,
)
