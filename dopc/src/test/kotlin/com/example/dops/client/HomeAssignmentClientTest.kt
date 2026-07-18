package com.example.dopc.client

import com.example.dopc.client.dto.StaticResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.converter.HttpMessageConversionException
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClient
import org.springframework.web.server.ResponseStatusException
import org.mockito.Mockito.times
import org.mockito.Mockito.verify

class HomeAssignmentClientTest {

    private val builder = mock(RestClient.Builder::class.java)
    private val restClient = mock(RestClient::class.java)
    private val uriSpec = mock(RestClient.RequestHeadersUriSpec::class.java) as RestClient.RequestHeadersUriSpec<*>
    private val headersSpec = mock(RestClient.RequestHeadersSpec::class.java) as RestClient.RequestHeadersSpec<*>
    private val responseSpec = mock(RestClient.ResponseSpec::class.java)

    private fun setUpStaticChain() {
        `when`(builder.baseUrl(ArgumentMatchers.anyString())).thenReturn(builder)
        `when`(builder.build()).thenReturn(restClient)

        `when`(restClient.get()).thenReturn(uriSpec)
        `when`(
            uriSpec.uri(
                ArgumentMatchers.eq("/venues/{slug}/static"),
                ArgumentMatchers.any<Any>()
            )
        ).thenReturn(headersSpec)
        `when`(headersSpec.retrieve()).thenReturn(responseSpec)
    }

    @Test
    fun `maps upstream 404 to 404 NOT_FOUND and does not retry`() {
        setUpStaticChain()

        val upstream404 = HttpClientErrorException.create(
            HttpStatus.NOT_FOUND,
            "not found",
            HttpHeaders.EMPTY,
            ByteArray(0),
            null
        )

        `when`(responseSpec.body(StaticResponse::class.java)).thenThrow(upstream404)

        val client = HomeAssignmentClient(builder)

        val ex = assertFailsWith<ResponseStatusException> {
            client.fetchStatic("missing-venue")
        }

        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
        assertEquals("Venue not found: missing-venue", ex.reason)
        verify(responseSpec, times(1)).body(StaticResponse::class.java)
    }

    @Test
    fun `maps upstream 4xx (non-404 non-429) to 400 BAD_REQUEST and does not retry`() {
        setUpStaticChain()

        val upstream4xx = HttpClientErrorException.create(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "unprocessable",
            HttpHeaders.EMPTY,
            ByteArray(0),
            null
        )

        `when`(responseSpec.body(StaticResponse::class.java)).thenThrow(upstream4xx)

        val client = HomeAssignmentClient(builder)

        val ex = assertFailsWith<ResponseStatusException> {
            client.fetchStatic("any-slug")
        }

        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
        assertEquals("Request could not be processed", ex.reason)
        verify(responseSpec, times(1)).body(StaticResponse::class.java)
    }

    @Test
    fun `maps invalid upstream payload to 502 BAD_GATEWAY and does not retry`() {
        setUpStaticChain()

        `when`(responseSpec.body(StaticResponse::class.java))
            .thenThrow(HttpMessageConversionException("bad payload"))

        val client = HomeAssignmentClient(builder)

        val ex = assertFailsWith<ResponseStatusException> {
            client.fetchStatic("any-slug")
        }

        assertEquals(HttpStatus.BAD_GATEWAY, ex.statusCode)
        assertEquals("Invalid upstream response format", ex.reason)
        verify(responseSpec, times(1)).body(StaticResponse::class.java)
    }
}