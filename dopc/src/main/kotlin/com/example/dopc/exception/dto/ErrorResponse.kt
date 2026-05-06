package com.example.dopc.exception.dto

import java.time.Instant

data class ErrorResponse(
        val status: Int,
        val code: String,
        val message: String,
        val path: String,
        val timestamp: Instant
)
