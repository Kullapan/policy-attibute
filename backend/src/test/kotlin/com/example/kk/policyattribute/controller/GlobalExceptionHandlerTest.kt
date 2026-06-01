package com.example.kk.policyattribute.controller

import com.example.kk.policyattribute.exception.AttributeValidationException
import com.example.kk.policyattribute.exception.ResourceNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.dao.OptimisticLockingFailureException

/**
 * Direct unit tests for GlobalExceptionHandler.
 * Tests the handler methods in isolation, verifying status codes and response shape.
 */
@DisplayName("GlobalExceptionHandler Unit Tests")
class GlobalExceptionHandlerTest {

    private val handler = GlobalExceptionHandler()

    @Test @DisplayName("AttributeValidationException → 400 Bad Request with message")
    fun handleValidation_returns400() {
        val ex = AttributeValidationException("CODE", "invalid value")
        val response = handler.handleValidation(ex)

        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(response.body).containsKey("timestamp")
        assertThat(response.body!!["status"]).isEqualTo(400)
        assertThat(response.body!!["error"]).isEqualTo("Bad Request")
        assertThat(response.body!!["message"] as String).contains("invalid value")
    }

    @Test @DisplayName("ResourceNotFoundException → 404 Not Found with message")
    fun handleNotFound_returns404() {
        val ex = ResourceNotFoundException("AttributeMaster", "GHOST")
        val response = handler.handleNotFound(ex)

        assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
        assertThat(response.body!!["status"]).isEqualTo(404)
        assertThat(response.body!!["message"] as String).contains("GHOST")
    }

    @Test @DisplayName("OptimisticLockingFailureException → 409 Conflict with user-friendly message")
    fun handleOptimisticLock_returns409() {
        val ex = object : OptimisticLockingFailureException("conflict") {}
        val response = handler.handleOptimisticLock(ex)

        assertThat(response.statusCode).isEqualTo(HttpStatus.CONFLICT)
        assertThat(response.body!!["status"]).isEqualTo(409)
        assertThat(response.body!!["message"] as String).contains("modified by another user")
    }

    @Test @DisplayName("Generic Exception → 500 Internal Server Error with safe message")
    fun handleGeneral_returns500() {
        val ex = RuntimeException("unexpected database failure")
        val response = handler.handleGeneral(ex)

        assertThat(response.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        assertThat(response.body!!["status"]).isEqualTo(500)
        assertThat(response.body!!["message"] as String)
            .doesNotContain("database")
            .contains("unexpected error")
    }

    @Test @DisplayName("Response body always contains timestamp, status, error, message fields")
    fun responseBody_hasRequiredFields() {
        val ex = AttributeValidationException("any message")
        val response = handler.handleValidation(ex)

        assertThat(response.body).containsKeys("timestamp", "status", "error", "message")
        assertThat(response.body!!["timestamp"] as String).isNotBlank()
    }

    @Test @DisplayName("AttributeValidationException with single-arg constructor → includes full message")
    fun handleValidation_singleArgConstructor() {
        val ex = AttributeValidationException("CSV file is empty")
        val response = handler.handleValidation(ex)

        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(response.body!!["message"] as String).contains("CSV file is empty")
    }
}
