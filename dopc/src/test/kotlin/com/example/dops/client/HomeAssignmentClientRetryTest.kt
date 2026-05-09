package com.example.dopc.client

import com.example.dopc.DopcApplication
import com.example.dopc.client.dto.StaticResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito.*
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClient
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.HttpServerErrorException

@SpringBootTest(classes = [DopcApplication::class])
@Import(HomeAssignmentClientRetryTest.MockConfig::class)
class HomeAssignmentClientRetryTest {

    @TestConfiguration
    class MockConfig {

        @Bean
        fun restClient(): RestClient = mock(RestClient::class.java)

        @Bean
        fun builder(restClient: RestClient): RestClient.Builder {
            val b = mock(RestClient.Builder::class.java)
            `when`(b.baseUrl(anyString())).thenReturn(b)
            `when`(b.build()).thenReturn(restClient)
            return b
        }

        @Bean
        fun uriSpec(): RestClient.RequestHeadersUriSpec<*> =
            mock(RestClient.RequestHeadersUriSpec::class.java) as RestClient.RequestHeadersUriSpec<*>

        @Bean
        fun headersSpec(): RestClient.RequestHeadersSpec<*> =
            mock(RestClient.RequestHeadersSpec::class.java) as RestClient.RequestHeadersSpec<*>

        @Bean
        fun responseSpec(): RestClient.ResponseSpec = mock(RestClient.ResponseSpec::class.java)
    }

    @jakarta.annotation.Resource
    lateinit var client: HomeAssignmentClient

    @jakarta.annotation.Resource
    lateinit var builder: RestClient.Builder

    @jakarta.annotation.Resource
    lateinit var restClient: RestClient

    @jakarta.annotation.Resource
    lateinit var uriSpec: RestClient.RequestHeadersUriSpec<*>

    @jakarta.annotation.Resource
    lateinit var headersSpec: RestClient.RequestHeadersSpec<*>

    @jakarta.annotation.Resource
    lateinit var responseSpec: RestClient.ResponseSpec

    @BeforeEach
    fun setup() {
        reset(builder, restClient, uriSpec, headersSpec, responseSpec)

        `when`(builder.baseUrl(anyString())).thenReturn(builder)
        `when`(builder.build()).thenReturn(restClient)
        `when`(restClient.get()).thenReturn(uriSpec)
        `when`(uriSpec.uri(eq("/venues/{slug}/static"), any<Any>())).thenReturn(headersSpec)
        `when`(headersSpec.retrieve()).thenReturn(responseSpec)
    }

    @Test
    fun `retries 3 times on 429 then returns too many requests`() {
        val upstream429 = HttpClientErrorException.create(
            HttpStatus.TOO_MANY_REQUESTS,
            "too many requests",
            HttpHeaders.EMPTY,
            ByteArray(0),
            null
        )

        `when`(responseSpec.body(StaticResponse::class.java)).thenThrow(upstream429)

        val ex = assertFailsWith<ResponseStatusException> {
            client.fetchStatic("venue-1")
        }

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, ex.statusCode)
        assertEquals("Too many requests", ex.reason)

        verify(responseSpec, times(3)).body(StaticResponse::class.java)
    }

    @Test
    fun `retries 3 times on 5xx then returns bad gateway`() {
        val upstream5xx = HttpServerErrorException.create(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "upstream error",
            HttpHeaders.EMPTY,
            ByteArray(0),
            null
        )

        `when`(responseSpec.body(StaticResponse::class.java)).thenThrow(upstream5xx)

        val ex = assertFailsWith<ResponseStatusException> {
            client.fetchStatic("venue-1")
        }

        assertEquals(HttpStatus.BAD_GATEWAY, ex.statusCode)
        assertEquals("Upstream service error", ex.reason)
        verify(responseSpec, times(3)).body(StaticResponse::class.java)
    }

    @Test
    fun `retries 3 times on timeout then returns gateway timeout`() {
        `when`(responseSpec.body(StaticResponse::class.java))
            .thenThrow(ResourceAccessException("timeout"))

        val ex = assertFailsWith<ResponseStatusException> {
            client.fetchStatic("venue-1")
        }

        assertEquals(HttpStatus.GATEWAY_TIMEOUT, ex.statusCode)
        assertEquals("Upstream timeout", ex.reason)
        verify(responseSpec, times(3)).body(StaticResponse::class.java)
    }
}