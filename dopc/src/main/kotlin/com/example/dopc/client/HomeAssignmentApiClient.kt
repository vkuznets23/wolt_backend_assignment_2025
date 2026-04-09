package com.example.dopc.client

import com.example.dopc.client.dto.DynamicResponse
import com.example.dopc.client.dto.StaticResponse
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient

@Service
class HomeAssignmentApiClient(builder: RestClient.Builder) {
    private val webClient =
            builder.baseUrl(
                            "https://consumer-api.development.dev.woltapi.com/home-assignment-api/v1"
                    )
                    .build()

    fun fetchStatic(venueSlug: String): StaticResponse {
        return webClient
                .get()
                .uri("/venues/{slug}/static", venueSlug)
                .retrieve() // send request and get response
                .body(StaticResponse::class.java) // convert response to StaticResponse object
         ?: throw IllegalStateException("Empty static response")
    }

    fun fetchDynamic(venueSlug: String): DynamicResponse {
        return webClient
                .get()
                .uri("/venues/{slug}/dynamic", venueSlug)
                .retrieve()
                .body(DynamicResponse::class.java)
                ?: throw IllegalStateException("Empty dynamic response")
    }
}
