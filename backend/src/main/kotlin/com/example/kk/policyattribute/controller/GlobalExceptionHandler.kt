package com.example.kk.policyattribute.controller

import com.example.kk.policyattribute.exception.AttributeValidationException
import com.example.kk.policyattribute.exception.ResourceNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.Instant

/**
 * Global exception handler for REST API errors.
 * Provides consistent error response format.
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    /** 400 — Attribute validation failures (data type, regex). */
    @ExceptionHandler(AttributeValidationException::class)
    fun handleValidation(ex: AttributeValidationException): ResponseEntity<Map<String, Any>> {
        log.warn("Validation error: {}", ex.message)
        return buildResponse(HttpStatus.BAD_REQUEST, ex.message ?: "Validation error")
    }

    /** 400 — Illegal argument (e.g. invalid file type, malformed policy number). */
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<Map<String, Any>> {
        log.warn("Illegal argument: {}", ex.message)
        return buildResponse(HttpStatus.BAD_REQUEST, ex.message ?: "Bad request")
    }

    /** 400 — Bean validation failures (@Valid). */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleBeanValidation(ex: MethodArgumentNotValidException): ResponseEntity<Map<String, Any>> {
        val details = ex.bindingResult.fieldErrors.joinToString("; ") { "${it.field}: ${it.defaultMessage}" }
        log.warn("Bean validation error: {}", details)
        return buildResponse(HttpStatus.BAD_REQUEST, details)
    }

    /** 404 — Resource not found. */
    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleNotFound(ex: ResourceNotFoundException): ResponseEntity<Map<String, Any>> {
        log.warn("Not found: {}", ex.message)
        return buildResponse(HttpStatus.NOT_FOUND, ex.message ?: "Not found")
    }

    /** 409 — Optimistic locking conflict. */
    @ExceptionHandler(ObjectOptimisticLockingFailureException::class)
    fun handleOptimisticLock(ex: ObjectOptimisticLockingFailureException): ResponseEntity<Map<String, Any>> {
        log.warn("Optimistic lock conflict: {}", ex.message)
        return buildResponse(HttpStatus.CONFLICT, "The record was modified by another user. Please refresh and try again.")
    }

    /** 500 — Catch-all for unexpected errors. */
    @ExceptionHandler(Exception::class)
    fun handleGeneral(ex: Exception): ResponseEntity<Map<String, Any>> {
        log.error("Unexpected error", ex)
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred. Please try again later.")
    }

    private fun buildResponse(status: HttpStatus, message: String): ResponseEntity<Map<String, Any>> {
        val body: Map<String, Any> = mapOf(
            "timestamp" to Instant.now().toString(),
            "status"    to status.value(),
            "error"     to status.reasonPhrase,
            "message"   to message
        )
        return ResponseEntity.status(status).body(body)
    }
}
