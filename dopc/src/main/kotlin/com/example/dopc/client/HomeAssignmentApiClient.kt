package com.example.dopc.client

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
                .retrieve()
                .body(StaticResponse::class.java)
                ?: throw IllegalStateException("Empty static response")
    }
}
