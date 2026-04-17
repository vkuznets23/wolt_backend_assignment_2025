package com.example.dopc.client

import com.example.dopc.client.dto.DynamicResponse
import com.example.dopc.client.dto.StaticResponse
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestClient
import org.springframework.web.server.ResponseStatusException

@Service
class HomeAssignmentClient(builder: RestClient.Builder) {
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
             ?: throw ResponseStatusException(HttpStatus.BAD_GATEWAY, "Empty static response") // null body
        } catch (e: HttpClientErrorException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Venue not found: $venueSlug") // 404
        } catch(e: HttpClientErrorException.TooManyRequests) {
            throw ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many requests") // 429
        } catch (e: HttpClientErrorException) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Upstream rejected request") // 400
        } catch (e: HttpClientErrorException.BadRequest) {
            throw ResponseStatusException(HttpStatus.BAD_GATEWAY, "Invalid upstream response format") // bad JSON/schema от upstream
        } catch (e: HttpServerErrorException) {
            throw ResponseStatusException(HttpStatus.BAD_GATEWAY, "Upstream service error") // 502 external service returned an error
        } catch (e: ResourceAccessException) {
            throw ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT, "Upstream timeout") // 504 external service timeout
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
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Venue not found: $venueSlug")
        }catch(e: HttpClientErrorException.TooManyRequests) {
            throw ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many requests")
        } catch (e: HttpClientErrorException) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Upstream rejected request")
        }catch (e: HttpClientErrorException.BadRequest) {
            throw ResponseStatusException(HttpStatus.BAD_GATEWAY, "Invalid upstream response format")
        } catch (e: HttpServerErrorException) {
            throw ResponseStatusException(HttpStatus.BAD_GATEWAY, "Upstream service error")
        } catch (e: ResourceAccessException) {
            throw ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT, "Upstream timeout")
        }
    }
}
