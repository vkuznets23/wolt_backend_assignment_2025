package com.example.dopc.controller

import com.example.dopc.exception.GlobalExceptionHandler
import com.example.dopc.service.DeliveryOrderPriceService
import com.example.dopc.service.ValidationService
import com.example.dopc.service.dto.DeliveryInfo
import com.example.dopc.service.dto.DeliveryOrderPriceResponse
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(DeliveryOrderPriceController::class)
@Import(GlobalExceptionHandler::class)
class DeliveryOrderPriceControllerTest(
    @Autowired private val mockMvc: MockMvc
) {
    @MockBean lateinit var deliveryOrderPriceService: DeliveryOrderPriceService
    @MockBean lateinit var validationService: ValidationService

    @Test
    fun `returns 200 when all parameters are valid`() {
        val response = DeliveryOrderPriceResponse(
            totalPrice = 1190,
            smallOrderSurcharge = 0,
            cartValue = 1000,
            delivery = DeliveryInfo(
                fee = 190,
                distance = 177
            )
        )

        `when`(
            deliveryOrderPriceService.getDeliveryOrderPrice(
                "home-assignment-venue-helsinki",
                1000,
                60.1699,
                24.93087
            )
        ).thenReturn(response)

        mockMvc.perform(
            get("/api/v1/delivery-order-price")
                .param("venue_slug", "home-assignment-venue-helsinki")
                .param("cart_value", "1000")
                .param("user_lat", "60.1699")
                .param("user_lon", "24.93087")
        )
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.total_price").value(1190))
        .andExpect(jsonPath("$.small_order_surcharge").value(0))
        .andExpect(jsonPath("$.cart_value").value(1000))
        .andExpect(jsonPath("$.delivery.fee").value(190))
        .andExpect(jsonPath("$.delivery.distance").value(177))
    }

    @Test
    fun `returns 400 when cart_value is missing`() {
        mockMvc.perform(
            get("/api/v1/delivery-order-price")
                .param("venue_slug", "home-assignment-venue-helsinki")
                .param("user_lat", "60.1699")
                .param("user_lon", "24.93087")
        )
        .andExpect(status().isBadRequest)
        .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
    }

    @Test
    fun `returns 400 when user_lat is missing`() {
        mockMvc.perform(
            get("/api/v1/delivery-order-price")
                .param("venue_slug", "home-assignment-venue-helsinki")
                .param("cart_value", "1000")
                .param("user_lon", "24.93087")
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
    }

    @Test
    fun `returns 400 when user_lon is missing`() {
        mockMvc.perform(
            get("/api/v1/delivery-order-price")
                .param("venue_slug", "home-assignment-venue-helsinki")
                .param("cart_value", "1000")
                .param("user_lat", "60.1699")
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
    }

    @Test
    fun `returns 400 when cart_value is invalid`() {
        mockMvc.perform(
            get("/api/v1/delivery-order-price")
                .param("venue_slug", "home-assignment-venue-helsinki")
                .param("cart_value", "not-a-number")
                .param("user_lat", "60.1699")
                .param("user_lon", "24.93087")
        )
        .andExpect(status().isBadRequest)
        .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
    }

    @Test
    fun `returns 400 when user_lat is invalid`() {
        mockMvc.perform(
            get("/api/v1/delivery-order-price")
                .param("venue_slug", "home-assignment-venue-helsinki")
                .param("cart_value", "1000")
                .param("user_lat", "not-a-number")
                .param("user_lon", "24.93087")
        )
        .andExpect(status().isBadRequest)
        .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
    }

    @Test
    fun `returns 400 when user_lon is invalid`() {
        mockMvc.perform(
            get("/api/v1/delivery-order-price")
                .param("venue_slug", "home-assignment-venue-helsinki")
                .param("cart_value", "1000")
                .param("user_lat", "60.1699")
                .param("user_lon", "not-a-number")
        )
        .andExpect(status().isBadRequest)
        .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
    }

    @Test
    fun `missing venue slug`() {
        mockMvc.perform(
            get("/api/v1/delivery-order-price")
                .param("cart_value", "1000")
                .param("user_lat", "60.1699")
                .param("user_lon", "24.93087")
        )
        .andExpect(status().isBadRequest)
        .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
    }

    @Test
    fun `missing cart_value`() {
        mockMvc.perform(
            get("/api/v1/delivery-order-price")
                .param("venue_slug", "home-assignment-venue-helsinki")
                .param("user_lat", "60.1699")
                .param("user_lon", "24.93087")
        )
        .andExpect(status().isBadRequest)
        .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
    }

    @Test
    fun `missing user_lat`() {
        mockMvc.perform(
            get("/api/v1/delivery-order-price")
                .param("venue_slug", "home-assignment-venue-helsinki")
                .param("cart_value", "1000")
                .param("user_lon", "24.93087")
        )
        .andExpect(status().isBadRequest)
        .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
    }

    @Test
    fun `extra parameters`() {
        mockMvc.perform(
            get("/api/v1/delivery-order-price")
                .param("venue_slug", "home-assignment-venue-helsinki")
                .param("cart_value", "1000")
                .param("user_lat", "60.1699")
                .param("user_lon", "24.93087")
                .param("extra", "test")
        )
        .andExpect(status().isOk)
    }
}