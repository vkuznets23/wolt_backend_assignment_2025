package com.example.dopc.client

import com.example.dopc.client.dto.DynamicResponse
import com.example.dopc.client.dto.StaticResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.converter.HttpMessageConversionException
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestClient
import org.springframework.web.server.ResponseStatusException

@Service
class HomeAssignmentClient(builder: RestClient.Builder) {
    private val log = LoggerFactory.getLogger(HomeAssignmentClient::class.java)
    private val webClient =
            builder.baseUrl(
                            "https://consumer-api.development.dev.woltapi.com/home-assignment-api/v1"
                    )
                    .build()

    fun fetchStatic(venueSlug: String): StaticResponse {
        try {
            return webClient
                    .get()
                    .uri("/venues/{slug}/static", venueSlug)
                    .retrieve() // send request and get response
                    .body(StaticResponse::class.java) // convert response to StaticResponse object
                    ?: throw ResponseStatusException(
                            HttpStatus.BAD_GATEWAY,
                            "Empty static response"
                    ) // null body
        } catch (e: HttpClientErrorException.NotFound) {
            log.warn("[CLIENT] Venue not found: {}", venueSlug, e)
            throw ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Venue not found: $venueSlug"
            ) // 404
        } catch (e: HttpClientErrorException.TooManyRequests) {
            log.warn("[CLIENT] Upstream rate limit hit for slug={}", venueSlug, e)
            throw ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many requests") // 429
        } catch (e: HttpClientErrorException) {
            // all other 4xx (401/403/409/422/...)
            log.warn("[CLIENT] Upstream returned 4xx {} for slug={}", e.statusCode, venueSlug, e)
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Request could not be processed")
        } catch (e: HttpMessageConversionException) {
            // upstream returned payload that doesn't match expected schema
            log.error("[CLIENT] Invalid upstream response format for slug={}", venueSlug, e)
            throw ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Invalid upstream response format"
            )
        } catch (e: HttpServerErrorException) {
            log.error("[CLIENT] Upstream 5xx {} for slug={}", e.statusCode, venueSlug, e)
            throw ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Upstream service error"
            ) // 502 external service returned an error
        } catch (e: ResourceAccessException) {
            log.error("[CLIENT] Upstream timeout/network error for slug={}", venueSlug, e)
            throw ResponseStatusException(
                    HttpStatus.GATEWAY_TIMEOUT,
                    "Upstream timeout"
            ) // 504 external service timeout
        }
    }

    fun fetchDynamic(venueSlug: String): DynamicResponse {
        try {
            return webClient
                    .get()
                    .uri("/venues/{slug}/dynamic", venueSlug)
                    .retrieve()
                    .body(DynamicResponse::class.java)
                    ?: throw ResponseStatusException(
                            HttpStatus.BAD_GATEWAY,
                            "Empty dynamic response"
                    )
        } catch (e: HttpClientErrorException.NotFound) {
            log.warn("[CLIENT]Venue not found: {}", venueSlug, e)
            throw ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Venue not found: $venueSlug"
            ) // 404
        } catch (e: HttpClientErrorException.TooManyRequests) {
            log.warn("[CLIENT] Upstream rate limit hit for slug={}", venueSlug, e)
            throw ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many requests") // 429
        } catch (e: HttpClientErrorException) {
            // all other 4xx (401/403/409/422/...)
            log.warn("[CLIENT] Upstream returned 4xx {} for slug={}", e.statusCode, venueSlug, e)
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Request could not be processed")
        } catch (e: HttpMessageConversionException) {
            // upstream returned payload that doesn't match expected schema
            log.error("[CLIENT] Invalid upstream response format for slug={}", venueSlug, e)
            throw ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Invalid upstream response format"
            )
        } catch (e: HttpServerErrorException) {
            log.error("[CLIENT] Upstream 5xx {} for slug={}", e.statusCode, venueSlug, e)
            throw ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Upstream service error"
            ) // 502 external service returned an error
        } catch (e: ResourceAccessException) {
            log.error("[CLIENT] Upstream timeout/network error for slug={}", venueSlug, e)
            throw ResponseStatusException(
                    HttpStatus.GATEWAY_TIMEOUT,
                    "Upstream timeout"
            ) // 504 external service timeout
        }
    }
}
