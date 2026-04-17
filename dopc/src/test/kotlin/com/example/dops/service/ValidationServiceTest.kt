package com.example.dopc.service

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import com.example.dopc.service.ValidationService
import org.springframework.web.server.ResponseStatusException
import org.springframework.http.HttpStatus

class ValidationServiceTest {
    private val service = ValidationService()

    @Test
    fun `happy path`() {
        service.validate("home-assignment-venue-helsinki", 100, 60.1699, 24.9384)
    }

    @Test
    fun `invalid venue slug`() {
        val ex = assertFailsWith<ResponseStatusException> {
            service.validate("  ", 100, 60.1699, 24.9384)
        }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
        assertEquals("venue_slug is required", ex.reason)
    }

    @Test
    fun `invalid cart value`() {
        val ex = assertFailsWith<ResponseStatusException> {
            service.validate("home-assignment-venue-helsinki", -1, 60.1699, 24.9384)
        }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
        assertEquals("cart_value must be >= 0", ex.reason)
    }
    
    @Test
    fun `invalid user lat`() {
        val ex = assertFailsWith<ResponseStatusException> {
            service.validate("home-assignment-venue-helsinki", 100, 91.0, 24.9384)
        }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
        assertEquals("user_lat must be between -90 and 90", ex.reason)
    }

    @Test
    fun `invalid user lon`() {
        val ex = assertFailsWith<ResponseStatusException> {
            service.validate("home-assignment-venue-helsinki", 100, 60.1699, 181.0)
        }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
        assertEquals("user_lon must be between -180 and 180", ex.reason)
    }
}