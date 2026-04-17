package com.example.dopc.exception

import com.example.dopc.exception.dto.ErrorResponse
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException::class)
    // when in code there is ResponseStatusException
    // takes status code from HttpStatus and return status code and body
    fun handleResponseStatusException(
        ex: ResponseStatusException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val status = HttpStatus.valueOf(ex.statusCode.value())
        val body = ErrorResponse(
            code = status.name,
            message = ex.reason ?: "Request failed",
            status = status.value(),
            path = request.requestURI,
            timestamp = LocalDateTime.now()
        )
        return ResponseEntity.status(status).body(body)
    }

    // when there is bean validation error (@Valid @NotBlank, @Min, @Max, etc.)
    // takes first error message and return status code and body
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(
        ex: MethodArgumentNotValidException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val message = ex.bindingResult.fieldErrors
            .firstOrNull()
            ?.defaultMessage
            ?: "Validation failed"

        val body = ErrorResponse(
            code = "VALIDATION_ERROR",
            message = message,
            status = HttpStatus.BAD_REQUEST.value(),
            path = request.requestURI,
            timestamp = LocalDateTime.now()
        )
        return ResponseEntity.badRequest().body(body)
    }

    // when there is unexpected exception
    // returns internal server error status code and body
    @ExceptionHandler(Exception::class)
    fun handleUnexpectedException(
        ex: Exception,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val body = ErrorResponse(
            code = "INTERNAL_ERROR",
            message = "Internal server error",
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            path = request.requestURI,
            timestamp = LocalDateTime.now()
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body)
    }
}