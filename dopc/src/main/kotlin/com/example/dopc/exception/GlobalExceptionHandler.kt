package com.example.dopc.exception

import com.example.dopc.exception.dto.ErrorResponse
import jakarta.servlet.http.HttpServletRequest
import java.time.Instant
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.server.ResponseStatusException
import jakarta.validation.ConstraintViolationException
import org.springframework.retry.ExhaustedRetryException

@RestControllerAdvice
class GlobalExceptionHandler {
    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(ResponseStatusException::class)
    // when in code there is ResponseStatusException
    // takes status code from HttpStatus and return status code and body
    fun handleResponseStatusException(
            ex: ResponseStatusException,
            request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val status = HttpStatus.valueOf(ex.statusCode.value())
        val body =
                ErrorResponse(
                        code = status.name,
                        message = ex.reason ?: "Request failed",
                        status = status.value(),
                        path = request.requestURI,
                        timestamp = Instant.now()
                )
        return ResponseEntity.status(status).body(body)
    }

    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingParam(
            ex: MissingServletRequestParameterException,
            request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        log.warn("[ERROR HANDLER] MissingServletRequestParameterException: ${ex.message}")
        val body =
                ErrorResponse(
                        code = "VALIDATION_ERROR",
                        message = ex.message ?: "Validation failed",
                        status = HttpStatus.BAD_REQUEST.value(),
                        path = request.requestURI,
                        timestamp = Instant.now()
                )
        return ResponseEntity.badRequest().body(body)
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatch(
            ex: MethodArgumentTypeMismatchException,
            request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        log.warn("[ERROR HANDLER] MethodArgumentTypeMismatchException: ${ex.message}")
        val body =
                ErrorResponse(
                        code = "VALIDATION_ERROR",
                        message = ex.message ?: "Validation failed",
                        status = HttpStatus.BAD_REQUEST.value(),
                        path = request.requestURI,
                        timestamp = Instant.now()
                )
        return ResponseEntity.badRequest().body(body)
    }

    // when there is bean validation error (@Valid @NotBlank, @Min, @Max, etc.)
    // takes first error message and return status code and body
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(
            ex: MethodArgumentNotValidException,
            request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        log.warn("[ERROR HANDLER] MethodArgumentNotValidException: ${ex.bindingResult.fieldErrors}")
        val message =
                ex.bindingResult.fieldErrors.firstOrNull()?.defaultMessage ?: "Validation failed"

        val body =
                ErrorResponse(
                        code = "VALIDATION_ERROR",
                        message = message,
                        status = HttpStatus.BAD_REQUEST.value(),
                        path = request.requestURI,
                        timestamp = Instant.now()
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
        log.error("[ERROR HANDLER]Exception: ${ex.message}", ex)
        val body =
                ErrorResponse(
                        code = "INTERNAL_ERROR",
                        message = "Internal server error",
                        status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        path = request.requestURI,
                        timestamp = Instant.now()
                )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body)
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolationException(
            ex: ConstraintViolationException,
            request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        log.warn("[ERROR HANDLER] ConstraintViolationException: ${ex.message}")
        val message = ex.constraintViolations.firstOrNull()?.message ?: "Validation failed"
        val body =
                ErrorResponse(
                        code = "VALIDATION_ERROR",
                        message = message,
                        status = HttpStatus.BAD_REQUEST.value(),
                        path = request.requestURI,
                        timestamp = Instant.now()
                )
        return ResponseEntity.badRequest().body(body)
    }
    
    // when retry is exhausted and recover method is not found
    @ExceptionHandler(ExhaustedRetryException::class)
        fun handleExhaustedRetryException(
        ex: ExhaustedRetryException,
        request: HttpServletRequest
        ): ResponseEntity<ErrorResponse> {
        log.warn("[ERROR HANDLER] ExhaustedRetryException: ${ex.message}")

        val cause = ex.cause
        if (cause is ResponseStatusException) {
                val status = HttpStatus.valueOf(cause.statusCode.value())
                val body = ErrorResponse(
                code = status.name,
                message = cause.reason ?: "Request failed",
                status = status.value(),
                path = request.requestURI,
                timestamp = Instant.now()
                )
                return ResponseEntity.status(status).body(body)
        }

        val body = ErrorResponse(
                code = "RETRY_ERROR",
                message = ex.message ?: "Retry failed",
                status = HttpStatus.BAD_GATEWAY.value(),
                path = request.requestURI,
                timestamp = Instant.now()
        )
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(body)
        }
}
