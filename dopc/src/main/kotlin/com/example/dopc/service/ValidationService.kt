package com.example.dopc.service

import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import org.springframework.http.HttpStatus
import com.example.dopc.utils.validateInput
import org.slf4j.LoggerFactory

@Service
class ValidationService {
    private val log = LoggerFactory.getLogger(ValidationService::class.java)

    fun validate(venueSlug: String,
    cartValue: Int,
    userLat: Double,
    userLon: Double) {
        val validationError = validateInput(venueSlug, cartValue, userLat, userLon)
        if (validationError != null) {
            log.error("[VALIDATION] Validation error: {}", validationError)
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, validationError)
        }
    }
}