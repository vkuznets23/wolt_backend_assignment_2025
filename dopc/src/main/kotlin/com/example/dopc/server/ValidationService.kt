package com.example.dopc.server

import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import org.springframework.http.HttpStatus
import com.example.dopc.utils.validateInput

@Service
class ValidationService {

    fun validate(venueSlug: String,
    cartValue: Int,
    userLat: Double,
    userLon: Double) {
        val validationError = validateInput(venueSlug, cartValue, userLat, userLon)
        if (validationError != null) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, validationError)
        }
    }
}